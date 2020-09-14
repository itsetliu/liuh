package com.cosmo.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

import java.util.List;

/**
 * @author ZhaoZiQu
 * @version 2020/6/29
 */
@Data
public class SysRole {
    private Long id;

    private String name;

    private Integer status;

    private String remark;

    private Long createdId;

    private String createdTime;

    @TableField(exist = false)
    private List<SysMenu> menuList;


}