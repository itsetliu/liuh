package com.cosmo.entity;

import lombok.Data;

/**
 * @author ZhaoZiQu
 * @version 2020/6/29
 */
@Data
public class UserRess {
    private Long id;

    private Long userId;

    private String name;

    private String phone;

    private String address;

    private String detailAddress;

    private String createTime;

    private String postcode;

    private Integer status;

    private String company;

    private String fax;

    private Integer type;

}