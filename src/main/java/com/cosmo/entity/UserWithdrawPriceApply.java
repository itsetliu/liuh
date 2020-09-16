package com.cosmo.entity;

import lombok.Data;

/**
 * @author ZhaoZiQu
 * @version 2020/9/16
 */
@Data
public class UserWithdrawPriceApply {
    private Long id;
    private Long userId;
    private String name;
    private String phone;
    private String bankName;
    private String bankNumber;
    private String cardholder;
    private Integer status;
}
