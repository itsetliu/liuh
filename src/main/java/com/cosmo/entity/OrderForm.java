package com.cosmo.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * @author ZhaoZiQu
 * @version 2020/6/29
 */
@Data
public class OrderForm {
    private String id;

    private String userId;

    private String orderNumber;

    private Integer todayNumber;

    private BigDecimal orderFreight;

    private BigDecimal productPrice;

    private BigDecimal orderPrice;

    private String orderWeight;

    private String invoiceId;

    private Integer invoiceType;

    private String orderRemark;

    private Integer orderStatus;

    private Date orderTimeCreate;

    private Date orderTimeEnd;

    private Date orderTimeDelivery;

    private Integer orderTotalTrayNumber;

    private String orderCouponId;

    private Integer cashDepositType;

    private BigDecimal cashDepositPrice;

    private String wxMerchantsOrderNumber;

    private String orderDemander;

    private BigDecimal agoOrderFreight;

    private BigDecimal agoProductPrice;

    private BigDecimal agoOrderPrice;

    private Integer ifLook;

    //非数据库字段
    @TableField(exist = false)
    private List<OrderAddress> orderAddressList;

    //非数据库字段
    @TableField(exist = false)
    private List<OrderModel> orderModelList;

    //非数据库字段 锁价id 非锁价订单时为0
    @TableField(exist = false)
    private String userLockId;

    //非数据库字段 锁价待付款剩余 非锁价订单时为0
    @TableField(exist = false)
    private BigDecimal userLockOrderPrice;

    //非数据库字段 合同默认是什么类型的订单 订单地址状态 0单地址收货，1自提，2待定
    @TableField(exist = false)
    private Integer orderDefaultStatus;

    //非数据库字段 优惠卷对象
    @TableField(exist = false)
    private Coupon orderCouponInfo;

    //非数据库字段 优惠卷使用后的订单金额
    @TableField(exist = false)
    private BigDecimal orderPriceCoupon;

    //非数据库字段 微信名
    @TableField(exist = false)
    private String wxName;

    //非数据库字段 名字
    @TableField(exist = false)
    private String name;

    //非数据库字段 发货地址公司抬头
    @TableField(exist = false)
    private String companyName;

}