package com.cosmo.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author ZhaoZiQu
 * @version 2020/6/29
 */
@Data
public class Coupon {
    private String id;

    private String userId;

    private String agoUserId;

    private String name;

    private BigDecimal full;

    private BigDecimal subtract;

    private Integer status;

    private String time;

    private Integer type;

    private String employTime;

    private BigDecimal employPrice;

    private Integer employStatus;

    @TableField(exist = false)
    private UserInfo user;

    @TableField(exist = false)
    private List<UserInfo> agoUser;


}