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
public class Invoice {
    private Long id;

    private Long userId;

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

    private Integer type;

    private String trackingNumber;

    private BigDecimal price;

    @TableField(exist = false)
    private List<OrderForm> orderFormList;


}