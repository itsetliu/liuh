package com.cosmo.entity;

import lombok.Data;

import java.util.Date;

/**
 * @author ZhaoZiQu
 * @version 2020/9/12
 */
@Data
public class UserRemind {
    private Long id;

    private Long orderId;

    private Long userId;

    private String info;

    private Integer status;

    private Date time;
}
