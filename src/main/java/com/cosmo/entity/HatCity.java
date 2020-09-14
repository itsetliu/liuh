package com.cosmo.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

/**
 * @author ZhaoZiQu
 * @version 2020/6/29
 */
@Data
public class HatCity {
    private Integer id;

    private String cityid;

    private String city;

    private String father;

    @TableField(exist = false)
    private String freights;

}