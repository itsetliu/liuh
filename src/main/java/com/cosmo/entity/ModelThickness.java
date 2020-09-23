package com.cosmo.entity;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @author ZhaoZiQu
 * @version 2020/6/29
 */
@Data
public class ModelThickness {
    private String id;

    private String modelId;

    private String thickness;

    private BigDecimal processCost;

    private Integer status;
}