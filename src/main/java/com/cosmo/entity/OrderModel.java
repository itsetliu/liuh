package com.cosmo.entity;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @author ZhaoZiQu
 * @version 2020/6/29
 */
@Data
public class OrderModel {
    private String id;

    private String orderId;

    private String userId;

    private Integer modelType;

    private String modelName;

    private String specWidth;

    private String specThickness;

    private String specLength;

    private String specSuttle;

    private String pipeWeight;

    private String pipeDia;

    private String cartonWeight;

    private Integer cartonType;

    private String cartonInfo;

    private Integer cartonPipeNumber;

    private Integer cartonNumber;

    private BigDecimal cartonPrice;

    private Integer labelType;

    private String labelInfo;

    private Integer trayType;

    private Integer trayNumber;

    private String trayModel;

    private Integer trayCapacity;

    private Integer rollNumber;

    private String rollRoughWeight;

    private String modelTotalRoughWeight;

    private String modelTotalSuttle;

    private BigDecimal modelUnitPrice;

    private BigDecimal modelTotalPrice;

    private BigDecimal orderModelFreightPrice;

    private BigDecimal orderModelFreight;

    private BigDecimal modelProcessCost;

    private BigDecimal modelRawPrice;

    private Integer modelRawPriceType;

    private Integer orderModelStatus;

    private String orderModelExpireTime;

    private String userLockId;

    private String memberId;

    private BigDecimal memberDiscount;

    private BigDecimal agoModelTotalPrice;

}