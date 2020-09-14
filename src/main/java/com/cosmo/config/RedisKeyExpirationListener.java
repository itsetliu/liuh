package com.cosmo.config;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.cosmo.dao.*;
import com.cosmo.entity.*;
import com.cosmo.util.FileUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.listener.KeyExpirationEventMessageListener;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;



/**
 * 主要作用就是:接收过期的redis消息,获取到key,key就是订单号,然后去更新订单号的状态(说明一下:用户5分钟不支付的话取消用户的订单)
 */
@Transactional
@Component
@Slf4j
public class RedisKeyExpirationListener extends KeyExpirationEventMessageListener {

    @Resource
    private OrderFormMapper orderFormMapper;
    @Resource
    private CouponMapper couponMapper;
    @Resource
    private OrderModelMapper orderModelMapper;
    @Resource
    private UserLockMapper userLockMapper;

    SimpleDateFormat ft = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");


    public RedisKeyExpirationListener(RedisMessageListenerContainer listenerContainer) {
        super(listenerContainer);
    }


    @Override
    public void onMessage(Message message, byte[] pattern) {

        String orderCode = message.toString();
        if (!StringUtils.isBlank(orderCode)) {
            String[] strs = orderCode.split(",");
            if (strs.length==2){
                if ("intentionGold".equals(strs[0])){
                    QueryWrapper<OrderForm> orderFormQueryWrapper = new QueryWrapper<>();
                    orderFormQueryWrapper.eq("order_number",strs[1]).eq("order_status",0);
                    orderFormMapper.delete(orderFormQueryWrapper);
                    System.err.println("删除的订单号是: " + strs[1]);
                } else if ("orderCode".equals(strs[0])){
                    QueryWrapper<OrderForm> orderFormQueryWrapper = new QueryWrapper<>();
                    orderFormQueryWrapper.eq("order_number",strs[1]).eq("order_status",1);
                    List<OrderForm> orderForms = orderFormMapper.selectList(orderFormQueryWrapper);
                    if (orderForms.size()>0){
                        OrderForm orderForm = orderForms.get(0);
                        orderForm.setOrderStatus(4);
                        orderFormMapper.updateById(orderForm);
                        System.err.println("过期的订单号是: " + strs[1]);
                    }
                } else if ("couponId".equals(strs[0])){
                    QueryWrapper<Coupon> couponQueryWrapper = new QueryWrapper<>();
                    couponQueryWrapper.eq("id",strs[1]).eq("status",0);
                    List<Coupon> coupons = couponMapper.selectList(couponQueryWrapper);
                    if (coupons.size()>0){
                        Coupon coupon = coupons.get(0);
                        coupon.setStatus(2);
                        couponMapper.updateById(coupon);
                        System.err.println("过期的红包是: " + strs[1]);
                    }
                }else if ("shopTrolley".equals(strs[0])){
                    OrderModel orderModel = orderModelMapper.selectById(Integer.parseInt(strs[1]));
                    if (orderModel.getLabelType()==1) FileUtil.delFile(orderModel.getLabelInfo());
                    if (orderModel.getCartonType()==1)FileUtil.delFile(orderModel.getCartonInfo());
                    orderModelMapper.deleteById(Integer.parseInt(strs[1]));
                    System.err.println("过期的购物车订单是: " + strs[1]);
                }else if ("lockPrice1".equals(strs[0])){
                    userLockMapper.deleteById(Integer.parseInt(strs[1]));
                    System.err.println("删除的锁价数据是: " + strs[1]);
                }else if ("lockPrice2".equals(strs[0])){
                    UserLock userLock = userLockMapper.selectById(Integer.parseInt(strs[1]));
                    userLock.setStatus(0);
                    userLockMapper.updateById(userLock);
                    System.err.println("过期的锁价数据是: " + strs[1]);
                }
            }
        }
    }
}
