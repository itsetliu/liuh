package com.cosmo.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

import java.util.List;

/**
 * @author ZhaoZiQu
 * @version 2020/6/29
 */
@Data
public class Model {
    private String id;

    private Integer type;

    private String name;

    private String typeName;

    private String configId;

    private Integer status;

    private String suttle;

    private String pipeWeight;

    private String width;

    private String scope;

    private Integer volume;

    @TableField(exist = false)
    private String price;

    @TableField(exist = false)
    private List<ModelThickness> modelThicknessList;

    @TableField(exist = false)
    private List<ModelSuttle> modelSuttleList;


}