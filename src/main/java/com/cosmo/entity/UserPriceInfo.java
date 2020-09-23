package com.cosmo.entity;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @author ZhaoZiQu
 * @version 2020/6/29
 */
@Data
public class UserPriceInfo {
    private String id;

    private String userId;

    private String type;

    private BigDecimal price;

    private String info;

}