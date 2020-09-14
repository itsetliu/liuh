package com.cosmo.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cosmo.entity.Freight;

public interface FreightMapper extends BaseMapper<Freight> {

    /**
     * 通过hatid查询value
     * @param hatid
     * @return
     */
    String value(String hatid);
}