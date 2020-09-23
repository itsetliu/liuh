package com.cosmo.entity;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @author ZhaoZiQu
 * @version 2020/9/16
 */
@Data
public class UserWithdrawPriceApply {
    private String id;
    private String userId;
    private String name;
    private String phone;
    private String bankName;
    private String bankNumber;
    private String cardholder;
    private Integer status;
    private Date time;
    private BigDecimal withdrawPrice;
}
