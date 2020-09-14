package com.cosmo.entity;


import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

import java.util.List;

/**
 * @author ZhaoZiQu
 * @version 2020/6/29
 */
@Data
public class SysMenu {
    private Long id;

    private String title;

    private String url;

    private Long pid;

    private Integer status;

    private String icon;

    private Integer createId;

    private String createTime;

    @TableField(exist = false)
    private List<SysMenu> menuList;


}