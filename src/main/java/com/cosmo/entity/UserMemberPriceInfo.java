package com.cosmo.entity;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @author ZhaoZiQu
 * @version 2020/6/29
 */
@Data
public class UserMemberPriceInfo {
    private Long id;

    private Long userId;

    private Integer type;

    private BigDecimal price;

    private String info;

    private Date time;

}