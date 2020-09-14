package com.cosmo.entity;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @author ZhaoZiQu
 * @version 2020/6/29
 */
@Data
public class UserInfo {
    private Long id;

    private String openId;

    private String wxName;

    private String phone;

    private String email;

    private Integer loginCount;

    private String createTime;

    private String lastLoginTime;

    private Long pid;

    private String referralCode;

    private BigDecimal price;

    private Integer goldCoin;

    private String serialNumber;

    private Long memberId;

    private Integer status;

    private BigDecimal memberPrice;

    private BigDecimal withdrawPrice;


}