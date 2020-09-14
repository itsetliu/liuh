package com.cosmo.entity;

import lombok.Data;

/**
 * @author ZhaoZiQu
 * @version 2020/6/29
 */
@Data
public class PayWx {
    private Long id;

    private String nonceStr;

    private String totalFee;

    private String body;

    private String orderNo;

    private String tradeType;

    private String openId;

    private Integer status;

    private Integer type;


}