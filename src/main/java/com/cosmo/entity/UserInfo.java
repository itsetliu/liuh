package com.cosmo.entity;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @author ZhaoZiQu
 * @version 2020/6/29
 */
@Data
public class UserInfo {
    private String id;

    private String openId;

    private String wxName;

    private String name;

    private String phone;

    private String identity;

    private String email;

    private Integer loginCount;

    private String createTime;

    private String lastLoginTime;

    private String pid;

    private String referralCode;

    private BigDecimal price;

    private Integer goldCoin;

    private String serialNumber;

    private String memberId;

    private Integer status;

    private BigDecimal memberPrice;

    private BigDecimal withdrawPrice;


}