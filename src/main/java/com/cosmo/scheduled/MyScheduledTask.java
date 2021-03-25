package com.cosmo.scheduled;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.cosmo.dao.OrderFormMapper;
import com.cosmo.dao.OrderModelMapper;
import com.cosmo.dao.UserLockMapper;
import com.cosmo.entity.OrderForm;
import com.cosmo.entity.OrderModel;
import com.cosmo.entity.UserLock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author ZhaoZiQu
 * @date 2020/12/17 11:20
 */
@Component
public class MyScheduledTask {

    @Autowired
    private OrderModelMapper orderModelMapper;
    @Autowired
    private OrderFormMapper orderFormMapper;
    @Autowired
    private UserLockMapper userLockMapper;

    /**
     * 每天早上9点
     *  清空购物车
     *  未付款订单过期
     *  清空未付款锁价订单
     */
    @Scheduled(cron = "0 0 9 * * ?")
    public void daysAM9(){
        //清空购物车
        QueryWrapper<OrderModel> orderModelQueryWrapper = new QueryWrapper<>();
        orderModelQueryWrapper.eq("order_model_status",0);
        orderModelMapper.delete(orderModelQueryWrapper);

        //未付款订单过期
        QueryWrapper<OrderForm> orderFormQueryWrapper = new QueryWrapper<>();
        orderFormQueryWrapper.eq("order_status",1);
        OrderForm orderForm = new OrderForm();
        orderForm.setOrderStatus(4);
        orderFormMapper.update(orderForm,orderFormQueryWrapper);

        //清空未付款锁价订单
        QueryWrapper<UserLock> userLockQueryWrapper = new QueryWrapper<>();
        userLockQueryWrapper.eq("status",1);
        userLockMapper.delete(userLockQueryWrapper);

    }

    /*@Scheduled(cron = "0 1-59 * * * ? ")
    public void sss(){
        System.err.println("ssss");
    }*/
}
