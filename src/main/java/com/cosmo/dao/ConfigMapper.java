package com.cosmo.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cosmo.entity.Config;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Param;

public interface ConfigMapper extends BaseMapper<Config> {


    /**
     * 根据id，value List<Map>修改
     * @param configList
     * @return
     */
    Integer updateByIds(List<Map<String, Object>> configList);
}