package me.renzheng.beaker.dao.converter;

import me.renzheng.beaker.common.enums.Gender;
import me.renzheng.beaker.dao.bo.UserBO;
import me.renzheng.beaker.dao.entity.UserDO;
import org.apache.commons.collections4.CollectionUtils;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * UserConverter
 *
 * @author Renzheng Zhang
 * @since 2024/4/28
 */
public class UserConverter {

    public static UserDO convertToDO(UserBO userBO) {
        UserDO userDO = new UserDO();
        userDO.setUsername(userBO.getUsername());
        userDO.setGender(Optional.ofNullable(userBO.getGender()).map(Gender::getCode).orElse(Gender.UNKNOWN.getCode()));
        userDO.setBirthday(userBO.getBirthday());
        userDO.setPhoneNumber(userBO.getPhoneNumber());
        userDO.setEmail(userBO.getEmail());
        userDO.setPasswd(userBO.getPasswd());
        userDO.setBaned(userBO.getBaned());
        userDO.setId(userBO.getId());
        userDO.setCreator(userBO.getCreator());
        userDO.setModifier(userBO.getModifier());
        userDO.setCreatedAt(userBO.getCreatedAt());
        userDO.setUpdatedAt(userBO.getUpdatedAt());
        userDO.setDeleted(userBO.isDeleted());

        return userDO;
    }

    public static UserBO convertToBO(UserDO userDO) {
        UserBO userBO = new UserBO();
        userBO.setUsername(userDO.getUsername());
        userBO.setGender(Gender.fromCode(userDO.getGender()));
        userBO.setBirthday(userDO.getBirthday());
        userBO.setPhoneNumber(userDO.getPhoneNumber());
        userBO.setEmail(userDO.getEmail());
        userBO.setPasswd(userDO.getPasswd());
        userBO.setBaned(userDO.getBaned());
        userBO.setId(userDO.getId());
        userBO.setCreator(userDO.getCreator());
        userBO.setModifier(userDO.getModifier());
        userBO.setCreatedAt(userDO.getCreatedAt());
        userBO.setUpdatedAt(userDO.getUpdatedAt());
        userBO.setDeleted(userDO.isDeleted());

        return userBO;
    }

    public static List<UserDO> convertToDO(List<UserBO> userBOList) {
        if (CollectionUtils.isEmpty(userBOList)) {
            return Collections.emptyList();
        }
        return userBOList.stream().map(UserConverter::convertToDO).toList();
    }

    public static List<UserBO> convertToBO(List<UserDO> userDOList) {
        if (CollectionUtils.isEmpty(userDOList)) {
            return Collections.emptyList();
        }
        return userDOList.stream().map(UserConverter::convertToBO).toList();
    }
}
