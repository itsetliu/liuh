package com.cosmo.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

import java.util.List;
/**
 * @author ZhaoZiQu
 * @version 2020/6/29
 */
@Data
public class HatProvince {
    private Integer id;

    private String provinceid;

    private String province;

    @TableField(exist = false)
    private List<HatCity> hatCityList;

   }