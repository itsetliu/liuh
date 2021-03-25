package com.cosmo.entity;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @author ZhaoZiQu
 * @version 2020/6/29
 */
@Data
public class UserMember {
    private String id;

    private String name;

    private BigDecimal moneyMin;

    private BigDecimal moneyMax;

    private String discounts;


}