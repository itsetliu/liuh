package com.cosmo.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cosmo.entity.SysMenu;
import java.util.List;

public interface SysMenuMapper extends BaseMapper<SysMenu> {

    /**
     * 根据pid查询权限及子下权限
     * @return
     */
    List<SysMenu> sysMenuList(Long pid);

    /**
     * 根据身份id查询权限及子下权限
     * @return
     */
    List<SysMenu> sysRoleMenuList(Long roleId);

    /**
     * 根据身份id查询子下权限
     * @param roleId
     * @return
     */
    List<SysMenu> sysMenuListRoleSon(Long roleId);

    /**
     * 查询子下权限
     * @return
     */
    List<SysMenu> sysMenuListSon();
}