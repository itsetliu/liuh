package com.cosmo.entity;

import lombok.Data;

/**
 * @author ZhaoZiQu
 * @version 2020/6/29
 */
@Data
public class UserInvoice {
    private String id;

    private String userId;

    private String name;

    private String phone;

    private String tax;

    private String fax;

    private String company;

    private String address;

    private String detailAddress;

    private String openBankNum;

    private String openBank;

    private String createTime;

    private Integer status;

}