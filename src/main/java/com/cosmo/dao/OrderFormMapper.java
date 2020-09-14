package com.cosmo.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cosmo.entity.OrderForm;
import java.util.Map;


public interface OrderFormMapper extends BaseMapper<OrderForm> {

    /**
     * 通过订单id集合绑定发票
     * @param map
     * @return
     */
    int updateInvoiceId(Map<String,Object> map);

    /**
     * 查询今天订单总数
     * @param time
     * @return
     */
    int count(Map<String,String> time);
}