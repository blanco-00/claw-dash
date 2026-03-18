package com.clawdash.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.clawdash.entity.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<User> {
}
