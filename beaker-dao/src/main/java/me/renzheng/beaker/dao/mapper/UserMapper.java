package me.renzheng.beaker.dao.mapper;

import me.renzheng.beaker.dao.entity.UserDO;
import me.renzheng.beaker.dao.example.UserExample;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface UserMapper {
    long countByExample(UserExample example);

    int deleteByExample(UserExample example);

    int deleteByPrimaryKey(Long id);

    int insert(UserDO record);

    int insertSelective(UserDO record);

    List<UserDO> selectByExample(UserExample example);

    UserDO selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") UserDO record, @Param("example") UserExample example);

    int updateByExample(@Param("record") UserDO record, @Param("example") UserExample example);

    int updateByPrimaryKeySelective(UserDO record);

    int updateByPrimaryKey(UserDO record);
}