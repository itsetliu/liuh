package com.cosmo.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cosmo.entity.SysRoleMenu;
import java.util.Map;


public interface SysRoleMenuMapper extends BaseMapper<SysRoleMenu> {

    /**
     * 通过身份id和权限id集合新增中间表关联
     * @param map
     * @return
     */
    int insertSysRoleMenus(Map<String, Object> map);
}