package com.cosmo.entity;

import lombok.Data;

@Data
public class CalculatorModelRecommend {
    private String id;
    private Integer modelType;
    private Integer machineType1;
    private Integer machineType2;
    private Integer stretchType;
    private Integer stretchScope;
    private Integer packType;
    private Integer kgScope;
    private String modelRecommend;
}
