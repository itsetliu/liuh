package com.cosmo.entity;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @author ZhaoZiQu
 * @version 2020/6/29
 */
@Data
public class UserLock {
    private String id;

    private String userId;

    private String number;

    private String lockPrice;

    private Integer status;

    private String timeCreate;

    private String timeEnd;

    private BigDecimal margin;

    private BigDecimal obligation;

    private BigDecimal price;

    private BigDecimal priceMargin;

    private String timestamp;


}