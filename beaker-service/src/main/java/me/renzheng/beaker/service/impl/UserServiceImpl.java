package me.renzheng.beaker.service.impl;

import jakarta.annotation.Resource;
import me.renzheng.beaker.common.util.EntityUtil;
import me.renzheng.beaker.dao.bo.UserBO;
import me.renzheng.beaker.dao.converter.UserConverter;
import me.renzheng.beaker.dao.entity.UserDO;
import me.renzheng.beaker.dao.example.UserExample;
import me.renzheng.beaker.dao.mapper.UserMapper;
import me.renzheng.beaker.service.UserService;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

/**
 * UserServiceImpl
 *
 * @author Renzheng Zhang
 * @since 2024/4/28
 */
@Service
public class UserServiceImpl implements UserService {

    @Resource
    private UserMapper userMapper;

    @Override
    public void insert(UserBO entity) {
        if (Objects.isNull(entity)) {
            throw new IllegalArgumentException("entity is null");
        }

        UserDO user = UserConverter.convertToDO(entity);
        EntityUtil.populateCreationFields(Objects.requireNonNull(user));
        userMapper.insert(user);
    }

    @Override
    public void update(UserBO entity) {
        if (Objects.isNull(entity)) {
            throw new IllegalArgumentException("entity is null");
        }

        UserDO user = UserConverter.convertToDO(entity);
        EntityUtil.populateUpdateFields(Objects.requireNonNull(user));
        userMapper.updateByPrimaryKeySelective(user);
    }

    @Override
    public UserBO select(Long id) {
        if (Objects.isNull(id)) {
            throw new IllegalArgumentException("id is null");
        }

        UserDO user = userMapper.selectByPrimaryKey(id);
        return UserConverter.convertToBO(user);
    }

    @Override
    public void deleteByPrimaryKey(Long id) {
        if (Objects.isNull(id)) {
            throw new IllegalArgumentException("id is null");
        }

        UserExample example = new UserExample();
        example.createCriteria().andDeletedEqualTo(false).andIdEqualTo(id);

        UserDO user = new UserDO();
        user.setId(id);
        user.setDeleted(true);

        EntityUtil.populateUpdateFields(user);
        userMapper.updateByExampleSelective(user, example);
    }

    @Override
    public UserBO selectByPhoneNumber(String phoneNumber) {
        UserExample example = new UserExample();
        example.createCriteria()
                .andDeletedEqualTo(false)
                .andPhoneNumberEqualTo(phoneNumber);

        List<UserDO> userList = userMapper.selectByExample(example);
        if (CollectionUtils.isEmpty(userList)) {
            return null;
        }
        if (userList.size() > 1) {
            throw new IllegalStateException("phone number is not unique");
        }

        return UserConverter.convertToBO(userList.get(0));
    }

    @Override
    public UserBO selectByUsername(String username) {
        UserExample example = new UserExample();
        example.createCriteria()
                .andDeletedEqualTo(false)
                .andUsernameEqualTo(username);

        List<UserDO> userList = userMapper.selectByExample(example);
        if (CollectionUtils.isEmpty(userList)) {
            return null;
        }
        if (userList.size() > 1) {
            throw new IllegalStateException("username is not unique");
        }

        return UserConverter.convertToBO(userList.get(0));
    }

    @Override
    public UserBO selectByEmail(String email) {
        UserExample example = new UserExample();
        example.createCriteria()
                .andDeletedEqualTo(false)
                .andEmailEqualTo(email);

        List<UserDO> userList = userMapper.selectByExample(example);
        if (CollectionUtils.isEmpty(userList)) {
            return null;
        }
        if (userList.size() > 1) {
            throw new IllegalStateException("email is not unique");
        }

        return UserConverter.convertToBO(userMapper.selectByExample(example).get(0));
    }
}
