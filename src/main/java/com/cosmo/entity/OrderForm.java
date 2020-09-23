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

    @TableField(exist = false)
    private List<OrderAddress> orderAddressList;

    @TableField(exist = false)
    private List<OrderModel> orderModelList;


}