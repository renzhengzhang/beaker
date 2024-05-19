package me.renzheng.beaker.service.impl;

import jakarta.annotation.Resource;
import me.renzheng.beaker.common.util.EntityUtil;
import me.renzheng.beaker.dao.bo.UserBO;
import me.renzheng.beaker.dao.converter.UserConverter;
import me.renzheng.beaker.dao.entity.UserDO;
import me.renzheng.beaker.dao.mapper.UserMapper;
import me.renzheng.beaker.service.UserService;
import org.springframework.stereotype.Service;

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
        EntityUtil.populateCreationFields(user);
        userMapper.insert(user);
    }

    @Override
    public void update(UserBO entity) {

    }

    @Override
    public UserBO select(Long id) {
        return null;
    }

    @Override
    public void deleteByPrimaryKey(Long id) {

    }
}
