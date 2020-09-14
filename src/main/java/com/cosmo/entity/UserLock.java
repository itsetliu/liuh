package com.cosmo.entity;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @author ZhaoZiQu
 * @version 2020/6/29
 */
@Data
public class UserLock {
    private Long id;

    private Long userId;

    private String number;

    private String lockPrice;

    private Integer status;

    private String time;

    private BigDecimal balance;

    private String timestamp;


}