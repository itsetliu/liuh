package com.cosmo.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class HomeCommodity {
    private String id;
    private String model;
    private String volume;
    private String thickness;
    private String width;
    private String length;
    // 指定数据库字段名
    @TableField(value = "tubeWeight")
    private String tubeWeight;
    private String suttle;
    // 指定数据库字段名
    @TableField(value = "roughWeight")
    private String roughWeight;
    private String price;
    private String pipe;
    private BigDecimal moeny;
    // 指定数据库字段名
    @TableField(value = "labelType")
    private String labelType;
    // 指定数据库字段名
    @TableField(value = "trayType")
    private String trayType;
    // 指定数据库字段名
    @TableField(value = "traySum")
    private String traySum;
    // 指定数据库字段名
    @TableField(value = "boxVolume")
    private String boxVolume;
    // 指定数据库字段名
    @TableField(value = "boxNumber")
    private String boxNumber;
    // 指定数据库字段名
    @TableField(value = "boxType")
    private String boxType;
    // 指定数据库字段名
    @TableField(value = "boxSumOrVolumeSum")
    private String boxSumOrVolumeSum;
    // 指定数据库字段名
    @TableField(value = "homeProductId")
    private String homeProductId;
    // 指定数据库字段名
    @TableField(value = "totalWeight")
    private String totalWeight;
    // 指定数据库字段名
    @TableField(value = "cartonWeight")
    private String cartonWeight;
    // 指定数据库字段名
    @TableField(value = "modelType")
    private String modelType;
    // 指定数据库字段名
    @TableField(value = "modelProcessCost")
    private String modelProcessCost;
    // 指定数据库字段名
    @TableField(value = "modelRawPrice")
    private String modelRawPrice;
    // 指定数据库字段名
    @TableField(value = "modelRawPriceType")
    private String modelRawPriceType;
    // 指定数据库字段名
    @TableField(value = "cartonPrice")
    private String cartonPrice;
}
