package com.cosmo.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

import java.util.List;

@Data
public class HomeProduct {
    private String id;
    private String image;
    private String name;
    private String price;
    private Long processed;
    // 指定数据库字段名
    @TableField(value = "homeTitleId")
    private String homeTitleId;
    // 指定数据库字段名
    @TableField(value = "modelId")
    private String modelId;

    //非数据库字段
    @TableField(exist = false)
    private List<HomeCommodity> homeCommoditys;
}
