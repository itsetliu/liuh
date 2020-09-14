package com.cosmo.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cosmo.entity.Model;
import java.util.List;

public interface ModelMapper extends BaseMapper<Model> {

    /**
     * 多表连接查询model
     * @return
     */
    List<Model> selectModel(String type);
}