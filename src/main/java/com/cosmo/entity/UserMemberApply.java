package com.cosmo.entity;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @author ZhaoZiQu
 * @version 2020/6/29
 */
@Data
public class UserMemberApply {
    private Long id;

    private Long userId;

    private Long memberId;

    private String name;

    private String phone;

    private Integer status;

    private BigDecimal price;

}