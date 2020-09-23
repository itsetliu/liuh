package com.cosmo.entity;


import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author ZhaoZiQu
 * @version 2020/6/29
 */
@Data
public class SysUser implements Serializable {
    private String id;

    private String userName;

    private String nickName;

    private String phone;

    private String email;

    private String password;

    private Integer status;

    private Integer loginCount;

    private String createId;

    private String createTime;

    private String lastLoginTime;

    @TableField(exist = false)
    private List<SysRole> roleList;

    @TableField(exist = false)
    private List<SysMenu> menuList;

}