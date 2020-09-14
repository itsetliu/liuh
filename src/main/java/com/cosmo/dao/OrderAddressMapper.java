package com.cosmo.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cosmo.entity.OrderAddress;
import java.util.List;
import java.util.Map;


public interface OrderAddressMapper extends BaseMapper<OrderAddress> {

    /**
     * 查询所有发货地址
     * @param map
     * @return
     */
    IPage<OrderAddress> selectOrderAddressList(Page<OrderAddress> page, Map<String, String> map);
}