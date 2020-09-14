package com.cosmo.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cosmo.entity.ModelSuttle;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Param;

public interface ModelSuttleMapper extends BaseMapper<ModelSuttle> {

    /**
     * 批量新增净重
     * @param mapList
     * @return
     */
    Integer addModelSuttles(List<Map<String, String>> mapList);
}