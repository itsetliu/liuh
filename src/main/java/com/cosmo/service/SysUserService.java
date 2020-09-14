package com.cosmo.service;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cosmo.dao.*;
import com.cosmo.entity.*;
import com.cosmo.entity.pack.Menu;
import com.cosmo.util.*;
import org.apache.shiro.crypto.hash.SimpleHash;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class SysUserService {

    @Resource
    private SysUserMapper sysUserMapper;
    @Resource
    private SysRoleMapper sysRoleMapper;
    @Resource
    private SysMenuMapper sysMenuMapper;
    @Resource
    private SysRoleMenuMapper sysRoleMenuMapper;
    @Resource
    private SysUserRoleMapper sysUserRoleMapper;


    /**
     * md5加密
     * @param password
     * @param salt
     * @param number
     * @return
     */
    public String md5(String password,String salt,Integer number){
        SimpleHash simpleHash = new SimpleHash("MD5",password,salt,number);
        return simpleHash.toString();
    }




    /**
     * 根据用户账号查询
     * @param userName
     * @return
     */
    public SysUser findByUsername(String userName){
        return sysUserMapper.findByUsername(userName);
    }

    /**
     * 根据用户账号更新信息
     * @param userName
     */
    public void updateLogin(String userName){
        int i = sysUserMapper.updateLogin(userName);
    }

    /**
     * 根据用户id查询
     * @param userId
     * @return
     */
//    public SysUser findByIdRole(Integer userId){
//        return sysUserMapper.findByIdRole(userId);
//    }

    /**
     * 根据用户id查询
     * @param userId
     * @return
     */
    public SysUser userById(long userId){
        return sysUserMapper.selectById(userId);
    }


    /**
     * 通过用户id查询身份
     * @param userId
     * @return
     */
    public List<SysRole> roleList(long userId){
        return sysUserMapper.roleList(userId);
    }

    /**
     * 通过用户id查询权限
     * @param userId
     * @return
     */
    public List<Menu> menuList(long userId){
        return sysUserMapper.menuList(userId);
    }

    /**
     * 查询所有用户（分页，模糊）
     * @param map
     * @return
     */
    public PageInfo sysUsers(Map<String, Object> map){
        String pageNum = map.get("pageNum").toString();
        if (StringUtil.isEmpty(pageNum)) map.put("pageNum",1);
        QueryWrapper<SysUser> sysUserQueryWrapper = new QueryWrapper<>();
        String nickName = map.get("nickName").toString();
        if (!StringUtil.isEmpty(nickName)) sysUserQueryWrapper.like("nick_name",nickName);
        Page page = new Page(Integer.parseInt(pageNum),10);
        IPage<SysUser> sysUserList = sysUserMapper.selectPage(page,sysUserQueryWrapper);
        PageInfo pageInfo = new PageInfo(sysUserList);
        return pageInfo;
    }

    /**
     * 查询所有身份（分页，模糊）
     * @param map
     * @return
     */
    public PageInfo sysRoles(Map<String, Object> map){
        String pageNum = map.get("pageNum").toString();
        if (StringUtil.isEmpty(pageNum)) map.put("pageNum",1);
        QueryWrapper<SysRole> sysRoleQueryWrapper = new QueryWrapper<>();
        String name = map.get("name").toString();
        if (!StringUtil.isEmpty(name)) sysRoleQueryWrapper.like("name",name);
        Page page = new Page(Integer.parseInt(pageNum),10);
        IPage<SysRole> sysUserList = sysRoleMapper.selectPage(page,sysRoleQueryWrapper);
        PageInfo pageInfo = new PageInfo(sysUserList);
        return pageInfo;
    }

    /**
     * 通过pid查询所有权限
     * @return
     */
    public List<SysMenu> sysMenus(Long pid){
        return sysMenuMapper.sysMenuList(pid);
    }

    /**
     * 根据父id查询
     * @return
     */
    public List<SysMenu> sysMenuList(Integer pid){
        QueryWrapper<SysMenu> sysMenuQueryWrapper = new QueryWrapper<>();
        sysMenuQueryWrapper.eq("pid",pid);
        return sysMenuMapper.selectList(sysMenuQueryWrapper);
    }

    /**
     * 根据id修改权限
     * @param sysMenu
     * @return
     */
    public int updateSysMenu(SysMenu sysMenu){
        return sysMenuMapper.updateById(sysMenu);
    }

    /**
     * 根据身份id查询权限
     * @param roleId
     * @return
     */
    public List<SysMenu> sysRoleMenuList(Long roleId){
        return sysMenuMapper.sysRoleMenuList(roleId);
    }

    /**
     * 通过身份id查询身份
     * @param roleId
     * @return
     */
    public SysRole sysRoleById(Long roleId){
        SysRole sysRole = sysRoleMapper.selectById(roleId);
        if (sysRole!=null) sysRole.setMenuList(this.sysRoleMenuList(roleId));
        return sysRole;
    }

    /**
     * 查询所有子权限
     * @return
     */
    public List<SysMenu> sysMenuListSon1(){
        return sysMenuMapper.sysMenuListSon();
    }

    /**
     * 查询所有子权限
     * 查询该身份子权限
     * 查询该身份没有子权限
     * @param roleId
     * @return
     */
    public Map<String, Object> sysMenuListSon(Long roleId){
        Map<String,Object> map = new HashMap<>();
        List<SysMenu> sysMenuList1 = this.sysMenuListSon1();
        List<SysMenu> sysMenuList2 = sysMenuMapper.sysMenuListRoleSon(roleId);
        map.put("sysMenuList2",sysMenuList2);
        map.put("sysMenuList3",this.sysMenuListSon1());
        for(int i=sysMenuList1.size()-1;i>=0;i--){
            for(int l=sysMenuList2.size()-1;l>=0;l--){
                if (sysMenuList1.get(i).getId()==sysMenuList2.get(l).getId()){
                    sysMenuList1.remove(i);
                    break;
                }
            }
        }
        map.put("sysMenuList1",sysMenuList1);
        return map;
    }

    /**
     * 通过身份id删除与权限中间表关联
     * @param roleId
     * @return
     */
    public int delSysRoleMenus(long roleId){
        QueryWrapper<SysRoleMenu> sysRoleMenuQueryWrapper = new QueryWrapper<>();
        sysRoleMenuQueryWrapper.eq("role_id",roleId);
        return sysRoleMenuMapper.delete(sysRoleMenuQueryWrapper);
    }

    /**
     * 通过身份id和权限id集合新增中间表关联
     * @param roleId
     * @param menuIds
     * @return
     */
    public int addSysRoleMenus(long roleId,List<Integer> menuIds){
        if (menuIds.size()==0) return 0;
        Map<String, Object> map = new HashMap<>();
        map.put("roleId",roleId);
        map.put("menuIds",menuIds);
        return sysRoleMenuMapper.insertSysRoleMenus(map);
    }

    /**
     * 通过身份信息和与权限中间表信息修改
     * @return
     */
    @Transactional(value="txManager1")
    public int updateSysRole(Map<String, String> map){
        long id = Long.valueOf(map.get("id"));
        String name = map.get("name");
        String remark = map.get("remark");
        int status = Integer.parseInt(map.get("status"));
        String menuIds1 = map.get("menuIds");
        SysRole sysRole = new SysRole();
        sysRole.setId(id);
        sysRole.setName(name);
        sysRole.setRemark(remark);
        sysRole.setStatus(status);
        int updateRole = sysRoleMapper.updateById(sysRole);
        if (updateRole<=0) return updateRole;
        List<Integer> menuIds = new ArrayList<>();
        List<String> menuIds2 = JSON.parseArray(menuIds1,String.class);
        Map<String,String> map1 = new HashMap<>();
        for (String menuId:menuIds2){
            String[] s=menuId.split("-");
            map1.put(s[0],s[0]);
            map1.put(s[1],s[1]);
        }
        for (Map.Entry<String, String> entry : map1.entrySet()) {
            //Map.entry<Integer,String> 映射项（键-值对）  有几个方法：用上面的名字entry
            //entry.getKey() ;entry.getValue(); entry.setValue();
            //map.entrySet()  返回此映射中包含的映射关系的 Set视图。
            //System.out.println("key= " + entry.getKey() + " and value= " + entry.getValue());
            menuIds.add(Integer.parseInt(entry.getValue()));
        }
        this.delSysRoleMenus(id);
        this.addSysRoleMenus(id,menuIds);
        return updateRole;
    }

    /**
     * 通过身份信息和与权限中间表信息新增
     * @return
     */
    @Transactional(value="txManager1")
    public int addSysRole(Map<String, String> map){
        long userId = Integer.parseInt(map.get("userId"));
        String name = map.get("name");
        String remark = map.get("remark");
        int status = Integer.parseInt(map.get("status"));
        String menuIds1 = map.get("menuIds");
        SysRole sysRole = new SysRole();
        sysRole.setCreatedId(userId);
        sysRole.setName(name);
        sysRole.setRemark(remark);
        sysRole.setStatus(status);
        SimpleDateFormat ft = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        sysRole.setCreatedTime(ft.format(new Date()));
        int addRole = sysRoleMapper.insert(sysRole);
        if (addRole<=0) return addRole;
        List<Integer> menuIds = new ArrayList<>();
        List<String> menuIds2 = JSON.parseArray(menuIds1,String.class);
        Map<String,String> map1 = new HashMap<>();
        for (String menuId:menuIds2){
            String[] s=menuId.split("-");
            map1.put(s[0],s[0]);
            map1.put(s[1],s[1]);
        }
        for (Map.Entry<String, String> entry : map1.entrySet()) {
            //Map.entry<Integer,String> 映射项（键-值对）  有几个方法：用上面的名字entry
            //entry.getKey() ;entry.getValue(); entry.setValue();
            //map.entrySet()  返回此映射中包含的映射关系的 Set视图。
            //System.out.println("key= " + entry.getKey() + " and value= " + entry.getValue());
            menuIds.add(Integer.parseInt(entry.getValue()));
        }
        this.addSysRoleMenus(sysRole.getId(),menuIds);
        return addRole;
    }

    /**
     * 通过用户id删除身份关联
     * 或
     * 通过身份id删除用户关联
     * @param userId
     * @param roleId
     * @return
     */
    public int delUserRoles(Long userId,Integer roleId,Integer type){
        QueryWrapper<SysUserRole> sysUserRoleQueryWrapper = new QueryWrapper<>();
        if (type==0){//type==0  通过用户id删除身份关联
            sysUserRoleQueryWrapper.eq("user_id",userId);
        }else if (type==1){//type==1  通过身份id删除用户关联
            sysUserRoleQueryWrapper.eq("role_id",roleId);
        }else {
            return -1;
        }
        return sysUserRoleMapper.delete(sysUserRoleQueryWrapper);
    }

    /**
     * 通过身份id删除身份及权限关联及用户关联
     * @param roleId
     * @return
     */
    public int delRoleById(Integer roleId){
        int delRole = sysRoleMapper.deleteById(roleId);
        if (delRole<=0) return delRole;
        this.delUserRoles(null,roleId,1);
        this.delSysRoleMenus(roleId);
        return delRole;
    }

    /**
     * 查询所有身份
     * @return
     */
    public List<SysRole> SysRoleList(){
        return sysRoleMapper.selectList(null);
    }

    /**
     * 通过用户id和身份id集合新增中间表关联
     * @param userId
     * @param roleIds
     * @return
     */
    public int addSysUserRoles(long userId,List<Integer> roleIds){
        if (roleIds.size()==0) return 0;
        Map<String, Object> map = new HashMap<>();
        map.put("userId",userId);
        map.put("roleIds",roleIds);
        return sysUserRoleMapper.insertSysUserRoles(map);
    }

    /**
     * 通过用户名查询
     * @param userName
     * @return
     */
    public List<SysUser> selectByUserName(String userName){
        QueryWrapper<SysUser> sysUserQueryWrapper = new QueryWrapper<>();
        sysUserQueryWrapper.eq("user_name",userName);
        return sysUserMapper.selectList(sysUserQueryWrapper);
    }

    /**
     * 通过手机号查询
     * @param phone
     * @return
     */
    public List<SysUser> selectByPhone(String phone){
        QueryWrapper<SysUser> sysUserQueryWrapper = new QueryWrapper<>();
        sysUserQueryWrapper.eq("phone",phone);
        return sysUserMapper.selectList(sysUserQueryWrapper);
    }

    /**
     * 通过邮箱查询
     * @param email
     * @return
     */
    public List<SysUser> selectByEmail(String email){
        QueryWrapper<SysUser> sysUserQueryWrapper = new QueryWrapper<>();
        sysUserQueryWrapper.eq("email",email);
        return sysUserMapper.selectList(sysUserQueryWrapper);
    }

    /**
     * 通过用户信息和与身份中间表信息新增
     * @return
     */
    @Transactional(value="txManager1")
    public int addSysUsers(Map<String, String> map){
        long userId = Integer.parseInt(map.get("userId"));
        String nickName = map.get("nickName");
        String userName = map.get("userName");
        String password = map.get("password");
        String phone = map.get("phone");
        String email = map.get("email");
        int status = Integer.parseInt(map.get("status"));
        String roleIds = map.get("roleIds");
        SysUser sysUser = new SysUser();
        sysUser.setCreateId(userId);
        sysUser.setNickName(nickName);
        sysUser.setUserName(userName);
        sysUser.setPassword(this.md5(password,sysUser.getUserName(),2));
        sysUser.setPhone(phone);
        sysUser.setEmail(email);
        sysUser.setStatus(status);
        sysUser.setLoginCount(0);
        SimpleDateFormat ft = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        sysUser.setCreateTime(ft.format(new Date()));
        int addUser = sysUserMapper.insert(sysUser);
        if (addUser<=0) return addUser;
        List<Integer> roleIds2 = JSON.parseArray(roleIds,Integer.class);
        this.addSysUserRoles(sysUser.getId(),roleIds2);
        return addUser;
    }

    /**
     * 通过用户信息和与身份中间表信息修改
     * @return
     */
    @Transactional(value="txManager1")
    public int updateSysUsers(Map<String, String> map){
        long id = Integer.parseInt(map.get("id"));
        String nickName = map.get("nickName");
        String phone = map.get("phone");
        String email = map.get("email");
        int status = Integer.parseInt(map.get("status"));
        String roleIds = map.get("roleIds");
        SysUser sysUser = new SysUser();
        sysUser.setId(id);
        sysUser.setNickName(nickName);
        sysUser.setPhone(phone);
        sysUser.setEmail(email);
        sysUser.setStatus(status);
        int updateUser = sysUserMapper.updateById(sysUser);
        if (updateUser<=0) return updateUser;
        List<Integer> roleIds2 = JSON.parseArray(roleIds,Integer.class);
        this.delUserRoles(id,null,0);
        this.addSysUserRoles(sysUser.getId(),roleIds2);
        return updateUser;
    }

    /**
     * 通过用户id修改密码
     * @return
     */
    public int updatePassword(Map<String, String> map){
        long id = Integer.parseInt(map.get("id"));
        String password = map.get("password");
        SysUser sysUser = new SysUser();
        sysUser.setId(id);
        sysUser.setPassword(this.md5(password,sysUser.getUserName(),2));
        int updateUser = sysUserMapper.updateById(sysUser);
        if (updateUser<=0) return updateUser;
        return updateUser;
    }

    /**
     * 通过用户id删除及身份关联表
     * @return
     */
    @Transactional(value="txManager1")
    public int delUserById(long id){
        int delUser = sysUserMapper.deleteById(id);
        if (delUser<=0) return delUser;
        this.delUserRoles(id,null,0);
        return delUser;
    }
}
