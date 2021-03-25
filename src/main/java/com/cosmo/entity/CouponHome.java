package com.cosmo.entity;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class CouponHome {
    private String id;

    private String name;

    private String full;

    private String subtract;

    private String time;

    private String type;
}
