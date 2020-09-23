package com.cosmo.entity;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @author ZhaoZiQu
 * @version 2020/9/12
 */
@Data
public class OrderParent {
    private String id;

    private String orderId;

    private String userId;

    private BigDecimal orderPrice;

    private BigDecimal orderPriceSon;

    private Integer status;

    private Date orderTimeCreate;
}
