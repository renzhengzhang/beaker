/**
 * =================================================================================
 * AuthHelper - 前端认证助手模块
 * =================================================================================
 *
 * 描述:
 * 这是一个可复用的前端认证模块，旨在简化和统一项目中所有与JWT相关的操作。
 * 它遵循安全最佳实践，将 Access Token 存储在内存中，并依赖HttpOnly Cookie来处理 Refresh Token。
 *
 * 核心功能:
 * 1.  AccessToken内存管理: 安全地在内存中存储和检索 Access Token。
 * 2.  API登录: 封装 `/api/auth/login` 接口调用。
 * 3.  无感刷新: 封装 `/api/auth/refresh` 接口调用，实现 Access Token 的自动续期。
 *     此刷新机制同时支持 API 登录和传统的表单登录。
 * 4.  认证请求封装 (fetchWithAuth):
 *     - 自动为API请求添加 `Authorization` 头。
 *     - 在遇到401/403错误时，自动尝试刷新Token并重试原始请求，对调用者透明。
 * 5.  安全登出: 清理客户端状态并可调用后端接口。
 * 6.  自动初始化: 提供 `initializeAuth` 方法，在应用加载时自动尝试恢复会话。
 *
 * 使用示例:
 * ```javascript
 * const auth = new AuthHelper();
 *
 * // API 登录
 * await auth.login('user', 'password');
 *
 * // 调用受保护的API (推荐方式)
 * const userData = await auth.fetchWithAuth('/api/user/info');
 *
 * // 登出
 * auth.logout();
 * ```
 */
class AuthHelper {
    constructor() {
        // Access Token 存储在内存中，符合安全最佳实践
        this._accessToken = null;
        // 防止在令牌刷新期间发生多次重复的刷新请求
        this._isRefreshing = false;
        // 用于存储刷新请求的Promise，确保并发请求能共享同一个刷新结果
        this._refreshPromise = null;
    }

    /**
     * 获取当前存储在内存中的 Access Token。
     * @returns {string|null} 当前的 Access Token，如果不存在则返回 null。
     */
    getAccessToken() {
        return this._accessToken;
    }

    /**
     * 通过用户名和密码登录。
     * @param {string} username - 用户名。
     * @param {string} password - 密码。
     * @returns {Promise<object>} 成功时返回后端响应的数据。
     * @throws {Error} 登录失败时抛出错误。
     */
    async login(username, password) {
        const formData = new FormData();
        formData.append('username', username);
        formData.append('password', password);

        const response = await fetch('/api/auth/login', {
            method: 'POST',
            body: formData,
        });

        const data = await response.json();
        if (!response.ok) {
            throw new Error(data.message || '登录失败');
        }

        this._accessToken = data.accessToken;
        console.log("AuthHelper: Login successful. Access token stored in memory.");
        return data;
    }

    /**
     * 初始化认证状态。
     * 如果内存中没有 Access Token，此方法会尝试使用 HttpOnly Cookie 中的
     * Refresh Token 来静默获取一个新的 Access Token。
     * 推荐在应用程序加载时调用此方法。
     * @returns {Promise<boolean>} 如果成功获取或已存在有效的 Access Token，则返回 true，否则返回 false。
     */
    async initializeAuth() {
        if (this._accessToken) {
            console.log("AuthHelper: Session already active with in-memory access token.");
            return true;
        }

        console.log("AuthHelper: No access token in memory. Attempting to refresh on startup...");
        try {
            // refreshToken 会自动处理 HttpOnly cookie 的存在与否
            await this.refreshToken();
            if (this._accessToken) {
                console.log("AuthHelper: Session successfully restored via refresh token.");
                return true;
            } else {
                console.log("AuthHelper: No active session found via refresh token.");
                return false;
            }
        } catch (error) {
            // 这是预期的行为，例如当 refresh token 不存在或已过期
            console.log(`AuthHelper: Could not restore session on startup. ${error.message}`);
            return false;
        }
    }

    /**
     * 使用存储在HttpOnly Cookie中的Refresh Token来刷新Access Token。
     * 这是一个内部方法，由 `fetchWithAuth` 在需要时自动调用。
     * @returns {Promise<string>} 成功时返回新的 Access Token。
     * @throws {Error} 刷新失败时抛出错误。
     */
    async refreshToken() {
        // 如果当前没有正在进行的刷新操作，则启动一个新的
        if (!this._isRefreshing) {
            console.log("AuthHelper: Access token expired or invalid. Initiating refresh...");
            this._isRefreshing = true;
            this._refreshPromise = (async () => {
                try {
                    // Refresh Token 是通过HttpOnly Cookie自动发送的，所以请求体为空
                    const response = await fetch('/api/auth/refresh', {
                        method: 'POST'
                    });

                    const data = await response.json();
                    if (!response.ok) {
                        throw new Error(data.message || '刷新令牌失败或已过期');
                    }

                    this._accessToken = data.accessToken;
                    console.log("AuthHelper: Token refresh successful. New access token stored.");
                    return this._accessToken;
                } catch (error) {
                    console.error("AuthHelper: Failed to refresh token.", error);
                    // 刷新失败，可能是refresh token也过期了，清除所有状态
                    this.logout();
                    throw error;
                } finally {
                    this._isRefreshing = false;
                    this._refreshPromise = null;
                }
            })();
        }

        // 返回当前正在进行的刷新请求的Promise
        return this._refreshPromise;
    }

    /**
     * 封装 fetch API，自动处理认证头和Token刷新。
     * 这是调用受保护API的推荐方法。
     * 它统一处理了"无Token"和"Token过期"两种情况。
     * @param {string} url - 请求的URL。
     * @param {object} options - fetch API 的配置选项。
     * @param {number} retries - 内部使用的重试计数器。
     * @returns {Promise<object>} 成功时返回解析后的JSON数据。
     * @throws {Error} 请求失败或刷新令牌失败时抛出错误。
     */
    async fetchWithAuth(url, options = {}, retries = 1) {
        // 构造请求头。如果 this._accessToken 为 null，后端会返回401，
        // 这将自动触发下面的刷新逻辑，统一了处理方式。
        const headers = new Headers(options.headers || {});
        headers.append('Authorization', `Bearer ${this._accessToken}`);
        if (options.body) {
            headers.append('Content-Type', 'application/json');
        }

        const response = await fetch(url, { ...options, headers });

        // 如果token失效 (401/403)，并且还有重试机会，这里是统一的刷新逻辑入口。
        if ((response.status === 401 || response.status === 403) && retries > 0) {
            try {
                // 尝试刷新token
                await this.refreshToken();
                // 递归调用，但将重试次数减1，防止无限循环
                return this.fetchWithAuth(url, options, retries - 1);
            } catch (refreshError) {
                // 如果刷新失败，则直接抛出刷新错误
                throw refreshError;
            }
        }

        const data = await response.json();
        if (!response.ok) {
            throw new Error(data.message || `API Error: ${response.statusText}`);
        }
        return data;
    }

    /**
     * 清除客户端的认证状态。
     * 注意：此方法只清除内存中的Access Token。后端的HttpOnly Refresh Token Cookie
     * 需要通过调用后端的登出接口来清除。
     */
    logout() {
        this._accessToken = null;
        console.log("AuthHelper: Access token cleared from memory.");
        // 可以在这里添加调用后端/logout接口的逻辑，以使HttpOnly cookie失效
        // fetch('/logout', { method: 'POST' });
    }
} 