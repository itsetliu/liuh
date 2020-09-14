package com.cosmo.entity;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @author ZhaoZiQu
 * @version 2020/6/29
 */
@Data
public class UserMemberModel {
    private Long id;

    private Long memberId;

    private Long modelId;

    private BigDecimal discount;


}