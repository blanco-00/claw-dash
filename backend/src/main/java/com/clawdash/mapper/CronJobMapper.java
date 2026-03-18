package com.clawdash.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.clawdash.entity.CronJob;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CronJobMapper extends BaseMapper<CronJob> {
}
