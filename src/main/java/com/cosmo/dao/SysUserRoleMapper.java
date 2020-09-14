package com.cosmo.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cosmo.entity.SysUserRole;
import java.util.Map;


public interface SysUserRoleMapper extends BaseMapper<SysUserRole> {

    /**
     * 通过用户id和身份id集合新增中间表关联
     * @param map
     * @return
     */
    int insertSysUserRoles(Map<String, Object> map);
}