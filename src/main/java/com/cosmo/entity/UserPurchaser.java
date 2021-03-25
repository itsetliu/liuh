package com.cosmo.entity;

import lombok.Data;

@Data
public class UserPurchaser {
    private String id;
    private String userId;
    private String companyName;
    private String userName;
    private String userPhone;
    private Integer status;
}
