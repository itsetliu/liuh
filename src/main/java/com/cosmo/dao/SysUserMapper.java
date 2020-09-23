package com.cosmo.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cosmo.entity.SysRole;
import com.cosmo.entity.SysUser;
import java.util.List;

import com.cosmo.entity.pack.Menu;

public interface SysUserMapper extends BaseMapper<SysUser> {

    /**
     * 根据用户账号查询
     * @param userName
     * @return
     */
    SysUser findByUsername(String userName);

    /**
     * 根据用户id查询身份
     * @param id
     * @return
     */
//    SysUser findByIdRole(Integer id);

    /**
     * 通过用户id查询身份
     * @param userId
     * @return
     */
    List<SysRole> roleList(String userId);

    /**
     * 通过用户id查询权限
     * @param userId
     * @return
     */
    List<Menu> menuList(String userId);

    /**
     * 通过用户账号更新用户登录信息
     * @param userName
     * @return
     */
    int updateLogin(String userName);
}