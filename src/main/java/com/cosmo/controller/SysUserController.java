package com.cosmo.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.cosmo.entity.SysMenu;
import com.cosmo.entity.SysRole;
import com.cosmo.entity.SysUser;
import com.cosmo.service.SysUserService;
import com.cosmo.util.CommonResult;
import com.cosmo.util.*;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.IncorrectCredentialsException;
import org.apache.shiro.authc.LockedAccountException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.subject.Subject;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class SysUserController {
    
    @Resource
    private SysUserService sysUserService;

    @GetMapping("/logout")
    public String logout() {
        return "logout";
    }

    /**
     * 登录方法
     * @param param
     * @return
     */
    @PostMapping("/ajaxLogin")
    public String ajaxLogin(@RequestBody Map<String, String> param) {
        String userName = param.get("userName");
        String password = param.get("password");
        JSONObject jsonObject = new JSONObject();
        Subject subject = SecurityUtils.getSubject();
        UsernamePasswordToken token = new UsernamePasswordToken(userName, password);
        try {
            subject.login(token);
            jsonObject.put("token", subject.getSession().getId());
            jsonObject.put("code",200);
            jsonObject.put("msg", "登录成功");
            SysUser sysUser = sysUserService.findByUsername(userName);
            sysUserService.updateLogin(userName);
            jsonObject.put("userInfo",sysUserService.userById(sysUser.getId()));
            jsonObject.put("roleList",sysUserService.roleList(sysUser.getId()));
            jsonObject.put("menuList",sysUserService.menuList(sysUser.getId()));
        } catch (IncorrectCredentialsException e) {
            jsonObject.put("msg", "密码错误");
        } catch (LockedAccountException e) {
            jsonObject.put("msg", "登录失败，该用户已被冻结");
        } catch (AuthenticationException e) {
            jsonObject.put("msg", "该用户不存在");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return jsonObject.toString();
    }

    /**
     * 未登录，shiro应重定向到登录界面，此处返回未登录状态信息由前端控制跳转页面
     * @return
     */
    @GetMapping("/unauth")
    public Object unauth() {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("code", 1000000);
        map.put("msg", "未登录");
        return map;
    }

    /**
     * 查询所有用户（分页，模糊）
     * @param request
     * @return
     */
    @PostMapping(value = "/sys/users",produces = "application/json;charset=UTF-8")
    public CommonResult sysUsers(HttpServletRequest request){
        Map<String,Object> map = new HashMap<>();
        map.put("pageNum",request.getParameter("pageNum"));
        map.put("nickName",request.getParameter("nickName"));
        PageInfo pageInfo = sysUserService.sysUsers(map);
        if (pageInfo.getList().size()>0) return new CommonResult(200,"查询成功",pageInfo);
        return new CommonResult(500,"查询失败",null);
    }

    /**
     * 查询所有身份（分页，模糊）
     * @param request
     * @return
     */
    @PostMapping(value = "/sys/roles",produces = "application/json;charset=UTF-8")
    public CommonResult sysRoles(HttpServletRequest request){
        Map<String,Object> map = new HashMap<>();
        map.put("pageNum",request.getParameter("pageNum"));
        map.put("name",request.getParameter("name"));
        PageInfo pageInfo = sysUserService.sysRoles(map);
        if (pageInfo.getList().size()>0) return new CommonResult(200,"查询成功",pageInfo);
        return new CommonResult(500,"查询失败",null);
    }

    /**
     * 查询所有权限
     * @return
     */
    @PostMapping(value = "/sys/menus",produces = "application/json;charset=UTF-8")
    public CommonResult sysMenus(){
        List<SysMenu> sysMenuList = sysUserService.sysMenus("0");
        if (sysMenuList.size()>0) return new CommonResult(200,"查询成功",sysMenuList);
        return new CommonResult(500,"查询失败",null);
    }

    /**
     * 根据父id查询
     * @param request
     * @return
     */
    @PostMapping(value = "/sys/menuList")
    public CommonResult sysMenuList(HttpServletRequest request){
        String pid = request.getParameter("pid");
        if (StringUtil.isEmpty(pid)) return new CommonResult(500,"pid 为空",null);
        List<SysMenu> sysMenuList = sysUserService.sysMenuList(pid);
        if (sysMenuList.size()>0) return new CommonResult(200,"查询成功",sysMenuList);
        return new CommonResult(201,"未查询到结果",null);
    }

    /**
     * 根据id修改权限状态
     * @param request
     * @return
     */
    @PostMapping("/sys/updateMenuStatus")
    public CommonResult updateSysMenuStatus(HttpServletRequest request){
        String id = request.getParameter("id");
        if (StringUtil.isEmpty(id)) return new CommonResult(500,"id 为空",null);
        String status = request.getParameter("status");
        if (StringUtil.isEmpty(status)) return new CommonResult(500,"status 为空",null);
        SysMenu sysMenu = new SysMenu();
        sysMenu.setId(id);
        sysMenu.setStatus(Integer.parseInt(status));
        int updateSysMenu = sysUserService.updateSysMenu(sysMenu);
        if (updateSysMenu>0) return new CommonResult(200,"更变成功");
        return new CommonResult(201,"更变失败");
    }

    /**
     * 通过身份id查询身份
     * @param request
     * @return
     */
    @PostMapping("/sys/sysRoleById")
    public CommonResult sysRoleById(HttpServletRequest request){
        String roleId = request.getParameter("roleId");
        if (StringUtil.isEmpty(roleId)) return new CommonResult(500,"roleId 为空",null);
        SysRole sysRole = sysUserService.sysRoleById(roleId);
        if (sysRole!=null) return new CommonResult(200,"查询成功",sysRole);
        return new CommonResult(201,"未查询到结果",null);
    }

    /**
     * 查询所有子权限
     * @return
     */
    @PostMapping("/sys/sysMenuListSon1")
    public CommonResult sysMenuListSon1(){
        return new CommonResult(200,"查询成功",sysUserService.sysMenuListSon1());
    }

    /**
     * 查询所有子权限
     * 查询该身份子权限
     * 查询该身份没有子权限
     * @param request
     * @return
     */
    @PostMapping("/sys/sysMenuListSon")
    public CommonResult sysMenuListSon(HttpServletRequest request){
        String roleId = request.getParameter("roleId");
        if (StringUtil.isEmpty(roleId)) return new CommonResult(500,"roleId 为空",null);
        Map<String, Object> map = sysUserService.sysMenuListSon(roleId);
        if (map!=null) return new CommonResult(200,"查询成功",map);
        return new CommonResult(201,"未查询到结果",null);
    }

    /**
     * 通过身份信息和与权限中间表信息修改
     * @param request
     * @return
     */
    @PostMapping("/sys/updateSysRole")
    public CommonResult updateSysRole(HttpServletRequest request){
        String id = request.getParameter("id");
        if (StringUtil.isEmpty(id)) return new CommonResult(500,"id 为空");
        String name = request.getParameter("name");
        if (StringUtil.isEmpty(name)) return new CommonResult(500,"name 为空");
        String remark = request.getParameter("remark");
        if (StringUtil.isEmpty(remark)) return new CommonResult(500,"remark 为空");
        String status = request.getParameter("status");
        if (StringUtil.isEmpty(status)) return new CommonResult(500,"status 为空");
        String menuIds = request.getParameter("menuIds");
        if (StringUtil.isEmpty(menuIds)) return new CommonResult(500,"menuIds 为空");
        Map<String,String> map = new HashMap<>();
        map.put("id",id);
        map.put("name",name);
        map.put("remark",remark);
        map.put("status",status);
        map.put("menuIds",menuIds);
        int i = sysUserService.updateSysRole(map);
        if (map!=null) return new CommonResult(200,"修改成功");
        return new CommonResult(201,"修改失败");
    }

    /**
     * 通过身份信息和与权限中间表信息新增
     * @param request
     * @return
     */
    @PostMapping("/sys/addSysRole")
    public CommonResult addSysRole(HttpServletRequest request){
        String userId = request.getParameter("userId");
        if (StringUtil.isEmpty(userId)) return new CommonResult(500,"userId 为空");
        String name = request.getParameter("name");
        if (StringUtil.isEmpty(name)) return new CommonResult(500,"name 为空");
        String remark = request.getParameter("remark");
        if (StringUtil.isEmpty(remark)) return new CommonResult(500,"remark 为空");
        String status = request.getParameter("status");
        if (StringUtil.isEmpty(status)) return new CommonResult(500,"status 为空");
        String menuIds = request.getParameter("menuIds");
        if (StringUtil.isEmpty(menuIds)) return new CommonResult(500,"menuIds 为空");
        Map<String,String> map = new HashMap<>();
        map.put("userId",userId);
        map.put("name",name);
        map.put("remark",remark);
        map.put("status",status);
        map.put("menuIds",menuIds);
        int i = sysUserService.addSysRole(map);
        if (i>0) return new CommonResult(200,"新增成功");
        return new CommonResult(201,"新增失败");
    }

    /**
     * 通过身份id删除身份及权限关联及用户关联
     * @param request
     * @return
     */
    @PostMapping("/sys/delRoleById")
    public CommonResult delRoleById(HttpServletRequest request){
        String id = request.getParameter("id");
        if (StringUtil.isEmpty(id)) return new CommonResult(500,"id 为空");
        int i = sysUserService.delRoleById(id);
        if (i>0) return new CommonResult(200,"删除成功");
        return new CommonResult(201,"删除失败");
    }

    /**
     * 查询所有身份
     * @return
     */
    @PostMapping("/sys/SysRoleList")
    public CommonResult SysRoleList(){
        List<SysRole> roles = sysUserService.SysRoleList();
        if (roles.size()>0) return new CommonResult(200,"查询成功",roles);
        return new CommonResult(201,"未查询到结果",null);
    }

    /**
     * 通过用户id查询身份
     * @return
     */
    @PostMapping("/sys/SysRoleList1")
    public CommonResult SysRoleList1(HttpServletRequest request){
        String id = request.getParameter("id");
        if (StringUtil.isEmpty(id)) return new CommonResult(500,"id 为空");
        List<SysRole> roles = sysUserService.roleList(id);
        if (roles.size()>0) return new CommonResult(200,"查询成功",roles);
        return new CommonResult(201,"未查询到结果",null);
    }

    /**
     * 通过用户信息和与身份中间表信息新增
     * @param request
     * @return
     */
    @PostMapping("/sys/addSysUsers")
    public CommonResult addSysUsers(HttpServletRequest request){
        String userId = request.getParameter("userId");
        if (StringUtil.isEmpty(userId)) return new CommonResult(500,"userId 为空");
        String nickName = request.getParameter("nickName");
        if (StringUtil.isEmpty(nickName)) return new CommonResult(500,"nickName 为空");
        String userName = request.getParameter("userName");
        if (StringUtil.isEmpty(userName)) return new CommonResult(500,"userName 为空");
        if (sysUserService.selectByUserName(userName).size()>0) return new CommonResult(500,"该账号已注册");
        String password = request.getParameter("password");
        if (StringUtil.isEmpty(password)) return new CommonResult(500,"password 为空");
        String phone = request.getParameter("phone");
        if (StringUtil.isEmpty(phone)) return new CommonResult(500,"phone 为空");
        if (sysUserService.selectByPhone(phone).size()>0) return new CommonResult(500,"该手机号已注册");
        String email = request.getParameter("email");
        if (StringUtil.isEmpty(email)) return new CommonResult(500,"email 为空");
        if (sysUserService.selectByEmail(email).size()>0) return new CommonResult(500,"该邮箱已注册");
        String status = request.getParameter("status");
        if (StringUtil.isEmpty(status)) return new CommonResult(500,"status 为空");
        String roleIds = request.getParameter("roleIds");
        if (StringUtil.isEmpty(roleIds)) return new CommonResult(500,"roleIds 为空");
        Map<String, String> map = new HashMap<>();
        map.put("userId",userId);
        map.put("nickName",nickName);
        map.put("userName",userName);
        map.put("password",password);
        map.put("phone",phone);
        map.put("email",email);
        map.put("status",status);
        map.put("roleIds",roleIds);
        int i = sysUserService.addSysUsers(map);
        if (i>0) return new CommonResult(200,"新增成功");
        return new CommonResult(201,"新增失败");
    }

    /**
     * 通过用户信息和与身份中间表信息修改
     * @param request
     * @return
     */
    @PostMapping("/sys/updateSysUsers")
    public CommonResult updateSysUsers(HttpServletRequest request){
        String id = request.getParameter("id");
        if (StringUtil.isEmpty(id)) return new CommonResult(500,"id 为空");
        SysUser sysUser = sysUserService.userById(id);
        String nickName = request.getParameter("nickName");
        if (StringUtil.isEmpty(nickName)) return new CommonResult(500,"nickName 为空");
        String phone = request.getParameter("phone");
        if (StringUtil.isEmpty(phone)) return new CommonResult(500,"phone 为空");
        if (sysUserService.selectByPhone(phone).size()>1||!phone.equals(sysUser.getPhone())) return new CommonResult(500,"该手机号已注册");
        String email = request.getParameter("email");
        if (StringUtil.isEmpty(email)) return new CommonResult(500,"email 为空");
        if (sysUserService.selectByEmail(email).size()>1||!email.equals(sysUser.getEmail())) return new CommonResult(500,"该邮箱已注册");
        String status = request.getParameter("status");
        if (StringUtil.isEmpty(status)) return new CommonResult(500,"status 为空");
        String roleIds = request.getParameter("roleIds");
        if (StringUtil.isEmpty(roleIds)) return new CommonResult(500,"roleIds 为空");
        Map<String, String> map = new HashMap<>();
        map.put("id",id);
        map.put("nickName",nickName);
        map.put("phone",phone);
        map.put("email",email);
        map.put("status",status);
        map.put("roleIds",roleIds);
        int i = sysUserService.updateSysUsers(map);
        if (i>0) return new CommonResult(200,"修改成功");
        return new CommonResult(201,"修改失败");
    }

    /**
     * 通过用户id修改密码
     * @param request
     * @return
     */
    @PostMapping("/sys/updatePassword")
    public CommonResult updatePassword(HttpServletRequest request){
        String id = request.getParameter("id");
        if (StringUtil.isEmpty(id)) return new CommonResult(500,"id 为空");
        String password = request.getParameter("password");
        if (StringUtil.isEmpty(password)) return new CommonResult(500,"password 为空");
        Map<String, String> map = new HashMap<>();
        map.put("id",id);
        map.put("password",password);
        int i = sysUserService.updatePassword(map);
        if (i>0) return new CommonResult(200,"修改成功");
        return new CommonResult(201,"修改失败");
    }

    /**
     * 通过用户id删除及身份关联表
     * @param request
     * @return
     */
    @PostMapping("/sys/delUserById")
    public CommonResult delUserById(HttpServletRequest request){
        String id = request.getParameter("id");
        int i = sysUserService.delUserById(id);
        if (i>0) return new CommonResult(200,"删除成功");
        return new CommonResult(201,"删除失败");
    }

}
