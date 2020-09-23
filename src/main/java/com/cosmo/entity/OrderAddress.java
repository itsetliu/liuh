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
public class OrderAddress {
    private String id;

    private String orderId;

    private String companyName;

    private String userName;

    private String userPhone;

    private String userFax;

    private String userAddress;

    private String userDetailAddress;

    private Integer orderAddressStatus;

    private Integer orderAddressType;

    private BigDecimal orderAddressFreightPrice;

    private BigDecimal orderAddressFreight;

    private String orderAddressRoughWeight;

    private BigDecimal orderAddressPrice;

    private Integer orderAddressTrayNumber;

    private Integer orderAddressShopStatus;

    private String orderAddressLogisticsNumber;

    @TableField(exist = false)
    private List<OrderModel> orderModelList;

}