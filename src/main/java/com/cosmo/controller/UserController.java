package com.cosmo.controller;

import com.alibaba.fastjson.JSON;
import com.cosmo.entity.*;
import com.cosmo.service.UserService;
import com.cosmo.util.CommonResult;
import com.cosmo.util.*;
import org.apache.commons.collections.map.HashedMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.Mapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class UserController {

    @Resource
    private UserService userService;

    /**
     * 后台
     */

    @PostMapping("/user/userList")
    public CommonResult userList(HttpServletRequest request){
        String status = request.getParameter("status");
        if (StringUtil.isEmpty(status)) return new CommonResult(500,"status 为空",null);
        Map<String,Object> map = new HashMap<>();
        map.put("pageNum",request.getParameter("pageNum"));
        map.put("wxName",request.getParameter("wxName"));
        map.put("name",request.getParameter("name"));
        map.put("phone",request.getParameter("phone"));
        map.put("identity",request.getParameter("identity"));
        map.put("serialNumber",request.getParameter("serialNumber"));
        map.put("status",status);
        PageInfo pageInfo = userService.userList(map);
        if (pageInfo.getList().size()>0) return new CommonResult(200,"查询成功",pageInfo);
        return new CommonResult(201,"查询失败",null);
    }

    /**
     * 根据id小程序用户
     * 删除用户收货地址
     * 删除用户开票信息
     * @param request
     * @return
     */
    @PostMapping("/user/delUserInfo")
    public CommonResult delUserInfo(HttpServletRequest request){
        String id = request.getParameter("id");
        if (StringUtil.isEmpty(id)) return new CommonResult(500,"id 为空",null);
        if (userService.delUserInfo(id)>0) return new CommonResult(200,"删除成功");
        return new CommonResult(201,"删除失败");
    }



    /**
     * 设为默认地址
     * @param request
     * @return
     */
    @PostMapping("/app/user/updateStatus")
    public CommonResult updateStatus(HttpServletRequest request){
        String id = request.getParameter("id");
        if (StringUtil.isEmpty(id)) return new CommonResult(500,"id 为空");
        String userId = request.getParameter("userId");
        if (StringUtil.isEmpty(userId)) return new CommonResult(500,"userId 为空");
        UserRess userRess = new UserRess();
        userRess.setId(id);
        userRess.setUserId(userId);
        if (userService.updateStatus(userRess)) return new CommonResult(200,"修改成功");
        return new CommonResult(500,"修改失败");
    }

    /**
     * 新增收货地址
     * @param request
     * @return
     */
    @PostMapping("/app/user/addRess")
    public CommonResult addRess(HttpServletRequest request){
        UserRess userRess = new UserRess();
        String userId = request.getParameter("userId");
        if (StringUtil.isEmpty(userId)) return new CommonResult(500,"userId 为空");
        String name = request.getParameter("name");
        if (StringUtil.isEmpty(name)) return new CommonResult(500,"name 为空");
        String phone = request.getParameter("phone");
        if (StringUtil.isEmpty(phone)) return new CommonResult(500,"phone 为空");
        String type = request.getParameter("type");
        if (StringUtil.isEmpty(type)) return new CommonResult(500,"type 为空");
        userRess.setType(Integer.parseInt(type));
        if ("1".equals(type)){
            String address = request.getParameter("address");
            if (StringUtil.isEmpty(address)) return new CommonResult(500,"address 为空");
            if (!userService.addressExist(address))return new CommonResult(500,"该地区暂未开放");
            String detailAddress = request.getParameter("detailAddress");
            if (StringUtil.isEmpty(detailAddress)) return new CommonResult(500,"detailAddress 为空");
            userRess.setAddress(address);
            userRess.setDetailAddress(detailAddress);
        }
        String postcode = request.getParameter("postcode");
//        if (StringUtil.isEmpty(postcode)) return new CommonResult(500,"postcode 为空");
        String status = request.getParameter("status");
        if (StringUtil.isEmpty(status)) return new CommonResult(500,"status 为空");
        String company = request.getParameter("company");
//        if (StringUtil.isEmpty(company)) return new CommonResult(500,"company 为空");
        String fax = request.getParameter("fax");
//        if (StringUtil.isEmpty(fax)) return new CommonResult(500,"fax 为空");
        userRess.setUserId(userId);
        userRess.setName(name);
        userRess.setPhone(phone);
        userRess.setPostcode(postcode);
        userRess.setStatus(Integer.parseInt(status));
        userRess.setCompany(company);
        userRess.setFax(fax);
        Map<String, Object> map = userService.addRess(userRess);
        if (Boolean.parseBoolean(map.get("boolean").toString())) return new CommonResult(200,map.get("msg").toString());
        return new CommonResult(500,map.get("msg").toString());
    }

    /**
     * 修改收货地址
     * @param request
     * @return
     */
    @PostMapping("/app/user/updateRess")
    public CommonResult updateRess(HttpServletRequest request){
        UserRess userRess = new UserRess();
        String id = request.getParameter("id");
        if (StringUtil.isEmpty(id)) return new CommonResult(500,"id 为空");
        String userId = request.getParameter("userId");
        if (StringUtil.isEmpty(userId)) return new CommonResult(500,"userId 为空");
        String name = request.getParameter("name");
        if (StringUtil.isEmpty(name)) return new CommonResult(500,"name 为空");
        String phone = request.getParameter("phone");
        if (StringUtil.isEmpty(phone)) return new CommonResult(500,"phone 为空");
        String type = request.getParameter("type");
        if (StringUtil.isEmpty(type)) return new CommonResult(500,"type 为空");
        userRess.setType(Integer.parseInt(type));
        if ("1".equals(type)){
            String address = request.getParameter("address");
            if (StringUtil.isEmpty(address)) return new CommonResult(500,"address 为空");
            if (!userService.addressExist(address))return new CommonResult(500,"该地区暂未开放");
            String detailAddress = request.getParameter("detailAddress");
            if (StringUtil.isEmpty(detailAddress)) return new CommonResult(500,"detailAddress 为空");
            userRess.setAddress(address);
            userRess.setDetailAddress(detailAddress);
        }
        String postcode = request.getParameter("postcode");
//        if (StringUtil.isEmpty(postcode)) return new CommonResult(500,"postcode 为空");
        String status = request.getParameter("status");
        if (StringUtil.isEmpty(status)) return new CommonResult(500,"status 为空");
        String company = request.getParameter("company");
//        if (StringUtil.isEmpty(company)) return new CommonResult(500,"company 为空");
        String fax = request.getParameter("fax");
//        if (StringUtil.isEmpty(fax)) return new CommonResult(500,"fax 为空");
        userRess.setId(id);
        userRess.setUserId(userId);
        userRess.setName(name);
        userRess.setPhone(phone);
        userRess.setPostcode(postcode);
        userRess.setStatus(Integer.parseInt(status));
        userRess.setCompany(company);
        userRess.setFax(fax);
        if (userService.updateRess(userRess)) return new CommonResult(200,"修改成功");
        return new CommonResult(500,"修改失败");
    }

    /**
     * 根据id删除
     * @param request
     * @return
     */
    @PostMapping("/app/user/delRess")
    public CommonResult delRess(HttpServletRequest request){
        String id = request.getParameter("id");
        if (StringUtil.isEmpty(id)) return new CommonResult(500,"id 为空");
        if (userService.delRess(id)) return new CommonResult(200,"删除成功");
        return new CommonResult(201,"删除失败");
    }

    /**
     * 根据用户id查询地址列表并升序type，及自提地址在最前
     * @param request
     * @return
     */
    @GetMapping("/app/user/userResses")
    public CommonResult userResses(HttpServletRequest request){
        String userId = request.getParameter("userId");
        if (StringUtil.isEmpty(userId)) return new CommonResult(500,"userId 为空");
        List<UserRess> userRessList = userService.userResses(userId);
        if (userRessList.size()>0) return new CommonResult(200,"查询成功",userRessList);
        return new CommonResult(201,"为查询到结果",null);
    }

    /**
     * 根据用户id、type查询地址列表
     * @param request
     * @return
     */
    @GetMapping("/app/user/userRessList")
    public CommonResult userRessList(HttpServletRequest request){
        String userId = request.getParameter("userId");
        if (StringUtil.isEmpty(userId)) return new CommonResult(500,"userId 为空");
        String type = request.getParameter("type");
        if (StringUtil.isEmpty(type)) return new CommonResult(500,"type 为空");
        List<UserRess> userRessList = userService.userRessList(userId,Integer.parseInt(type));
        if (userRessList.size()>0) return new CommonResult(200,"查询成功",userRessList);
        return new CommonResult(201,"为查询到结果",null);
    }

    /**
     * 根据用户id查询发票信息
     * @param request
     * @return
     */
    @GetMapping("/app/user/userInvoices")
    public CommonResult userInvoices(HttpServletRequest request){
        String userId = request.getParameter("userId");
        if (StringUtil.isEmpty(userId)) return new CommonResult(500,"userId 为空");
        List<UserInvoice> userInvoiceList = userService.userInvoices(userId);
        if (userInvoiceList.size()>0) return new CommonResult(200,"查询成功",userInvoiceList);
        return new CommonResult(201,"为查询到结果",null);
    }

    /**
     * 根据用户id和id修改默认
     * @param request
     * @return
     */
    @PostMapping("/app/user/updateInvoiceStatus")
    public CommonResult updateInvoiceStatus(HttpServletRequest request){
        String id = request.getParameter("id");
        if (StringUtil.isEmpty(id)) return new CommonResult(500,"id 为空");
        String userId = request.getParameter("userId");
        if (StringUtil.isEmpty(userId)) return new CommonResult(500,"userId 为空");
        UserInvoice userInvoice = new UserInvoice();
        userInvoice.setId(id);
        userInvoice.setUserId(userId);
        if (userService.updateInvoiceStatus(userInvoice)) return new CommonResult(200,"修改成功");
        return new CommonResult(500,"修改失败");
    }

    /**
     * 新增开票信息
     * @param request
     * @return
     */
    @PostMapping("/app/user/addInvoice")
    public CommonResult addInvoice(HttpServletRequest request){
        String userId = request.getParameter("userId");
        if (StringUtil.isEmpty(userId)) return new CommonResult(500,"userId 为空");
        String name = request.getParameter("name");
        if (StringUtil.isEmpty(name)) return new CommonResult(500,"name 为空");
        String phone = request.getParameter("phone");
        if (StringUtil.isEmpty(phone)) return new CommonResult(500,"phone 为空");
        String tax = request.getParameter("tax");
        if (StringUtil.isEmpty(tax)) return new CommonResult(500,"tax 为空");
        String fax = request.getParameter("fax");
//        if (StringUtil.isEmpty(fax)) return new CommonResult(500,"fax 为空");
        String company = request.getParameter("company");
//        if (StringUtil.isEmpty(company)) return new CommonResult(500,"company 为空");
        String address = request.getParameter("address");
        if (StringUtil.isEmpty(address)) return new CommonResult(500,"address 为空");
//        if (!userService.addressExist(address))return new CommonResult(500,"该地区暂未开放");
        String detailAddress = request.getParameter("detailAddress");
        if (StringUtil.isEmpty(detailAddress)) return new CommonResult(500,"detailAddress 为空");
        String openBankNum = request.getParameter("openBankNum");
        if (StringUtil.isEmpty(openBankNum)) return new CommonResult(500,"openBankNum 为空");
        String openBank = request.getParameter("openBank");
        if (StringUtil.isEmpty(openBank)) return new CommonResult(500,"openBank 为空");
        String status = request.getParameter("status");
        if (StringUtil.isEmpty(status)) return new CommonResult(500,"status 为空");
        Integer countInvoice = userService.countInvoice(userId);
        if (countInvoice>10) return new CommonResult(500,"开票信息超过10个");
        UserInvoice userInvoice = new UserInvoice();
        userInvoice.setUserId(userId);
        userInvoice.setName(name);
        userInvoice.setPhone(phone);
        userInvoice.setTax(tax);
        userInvoice.setFax(fax);
        userInvoice.setCompany(company);
        userInvoice.setAddress(address);
        userInvoice.setDetailAddress(detailAddress);
        userInvoice.setOpenBankNum(openBankNum);
        userInvoice.setOpenBank(openBank);
        userInvoice.setStatus(Integer.parseInt(status));
        Integer i = userService.addInvoice(userInvoice);
        if (i>0) return new CommonResult(200,"新增成功");
        return new CommonResult(201,"新增失败");
    }


    /**
     * 修改开票信息
     * @param request
     * @return
     */
    @PostMapping("/app/user/updateInvoice")
    public CommonResult updateInvoice(HttpServletRequest request){
        String id = request.getParameter("id");
        if (StringUtil.isEmpty(id)) return new CommonResult(500,"id 为空");
        String userId = request.getParameter("userId");
        if (StringUtil.isEmpty(userId)) return new CommonResult(500,"userId 为空");
        String name = request.getParameter("name");
        if (StringUtil.isEmpty(name)) return new CommonResult(500,"name 为空");
        String phone = request.getParameter("phone");
        if (StringUtil.isEmpty(phone)) return new CommonResult(500,"phone 为空");
        String tax = request.getParameter("tax");
        if (StringUtil.isEmpty(tax)) return new CommonResult(500,"tax 为空");
        String fax = request.getParameter("fax");
//        if (StringUtil.isEmpty(fax)) return new CommonResult(500,"fax 为空");
        String company = request.getParameter("company");
//        if (StringUtil.isEmpty(company)) return new CommonResult(500,"company 为空");
        String address = request.getParameter("address");
        if (StringUtil.isEmpty(address)) return new CommonResult(500,"address 为空");
//        if (!userService.addressExist(address))return new CommonResult(500,"该地区暂未开放");
        String detailAddress = request.getParameter("detailAddress");
        if (StringUtil.isEmpty(detailAddress)) return new CommonResult(500,"detailAddress 为空");
        String openBankNum = request.getParameter("openBankNum");
        if (StringUtil.isEmpty(openBankNum)) return new CommonResult(500,"openBankNum 为空");
        String openBank = request.getParameter("openBank");
        if (StringUtil.isEmpty(openBank)) return new CommonResult(500,"openBank 为空");
        String status = request.getParameter("status");
        if (StringUtil.isEmpty(status)) return new CommonResult(500,"status 为空");
        UserInvoice userInvoice = new UserInvoice();
        userInvoice.setId(id);
        userInvoice.setUserId(userId);
        userInvoice.setName(name);
        userInvoice.setPhone(phone);
        userInvoice.setTax(tax);
        userInvoice.setFax(fax);
        userInvoice.setCompany(company);
        userInvoice.setAddress(address);
        userInvoice.setDetailAddress(detailAddress);
        userInvoice.setOpenBankNum(openBankNum);
        userInvoice.setOpenBank(openBank);
        userInvoice.setStatus(Integer.parseInt(status));
        Integer i = userService.updateInvoice(userInvoice);
        if (i>0) return new CommonResult(200,"修改成功");
        return new CommonResult(201,"修改失败");
    }

    /**
     * 根据id删除开票信息
     * @param request
     * @return
     */
    @PostMapping("/app/user/delInvoice")
    public CommonResult delInvoice(HttpServletRequest request){
        String id = request.getParameter("id");
        if (StringUtil.isEmpty(id)) return new CommonResult(500,"id 为空");
        Integer i = userService.delInvoice(id);
        if (i>0) return new CommonResult(200,"删除成功");
        return new CommonResult(201,"删除失败");
    }

    /**
     * 获取首页红包 数据
     * @return
     */
    @GetMapping("/app/user/getHomeCoupon")
    public CommonResult getHomeCoupon(){
        return new CommonResult(200,"查询成功",userService.getHomeCoupon());
    }

    /**
     * 获取首页红包 数据
     * @return
     */
    @GetMapping("/user/getHomeCoupon")
    public CommonResult getHomeCoupon1(){
        return new CommonResult(200,"查询成功",userService.getHomeCoupon());
    }

    /**
     * 更新首页红包 数据
     * @param request
     * @return
     */
    /*@PostMapping("/user/setHomeCoupon")
    public CommonResult setHomeCoupon(HttpServletRequest request){
        String value = request.getParameter("value");
        if (StringUtil.isEmpty(value)) return new CommonResult(500,"value 为空");
        Integer i = userService.setHomeCoupon(value);
        if (i>0) return new CommonResult(200,"更新成功");
        else return new CommonResult(201,"更新失败");
    }*/

    /**
     * 新增首页红包 数据
     * @return
     */
    @PostMapping("/user/addHomeCoupon")
    public CommonResult addHomeCoupon(HttpServletRequest request){
        String name = request.getParameter("name");
        if (StringUtil.isEmpty(name)) return new CommonResult(500,"name 为空");
        String full = request.getParameter("full");
        if (StringUtil.isEmpty(full)) return new CommonResult(500,"full 为空");
        String subtract = request.getParameter("subtract");
        if (StringUtil.isEmpty(subtract)) return new CommonResult(500,"subtract 为空");
        String time = request.getParameter("time");
        if (StringUtil.isEmpty(time)) return new CommonResult(500,"time 为空");
        String type = request.getParameter("type");
        if (StringUtil.isEmpty(type)) return new CommonResult(500,"type 为空");
        Map<String, String> map = new HashMap<>();
        map.put("name",name);
        map.put("full",full);
        map.put("subtract",subtract);
        map.put("time",time);
        map.put("type",type);
        Integer i = userService.addHomeCoupon(map);
        if (i>0) return new CommonResult(200,"新增成功");
        else return new CommonResult(201,"新增失败");
    }

    /**
     * 删除首页红包 数据
     * @return
     */
    @PostMapping("/user/delHomeCoupon")
    public CommonResult delHomeCoupon(HttpServletRequest request){
        String couponHomeId = request.getParameter("couponHomeId");
        if (StringUtil.isEmpty(couponHomeId)) return new CommonResult(500,"couponHomeId 为空");
        Integer i = userService.delHomeCoupon(couponHomeId);
        if (i>0) return new CommonResult(200,"删除成功");
        else return new CommonResult(201,"删除失败");
    }

    /**
     * 首页红包领取
     * @return
     */
    @PostMapping("/app/user/neckHomeCoupon")
    public CommonResult neckHomeCoupon(HttpServletRequest request){
        String userId = request.getParameter("userId");
        if (StringUtil.isEmpty(userId)) return new CommonResult(500,"userId 为空");
        HashMap<String, String> map = new HashMap<>();
        map.put("userId",userId);
        Integer i = userService.neckHomeCoupon(map);
        if (i>0) return new CommonResult(200,"领取成功");
        else return new CommonResult(201,"领取失败");
    }

    /**
     * 分页查询 拥有我分享的红包的用户
     * @param request
     * @return
     */
    @GetMapping("/app/user/selectSonUserInfo")
    public CommonResult selectSonUserInfo(HttpServletRequest request){
        String pageNum = request.getParameter("pageNum");
        if (StringUtil.isEmpty(pageNum)) pageNum = "1";
        String userId = request.getParameter("userId");
        if (StringUtil.isEmpty(userId)) return new CommonResult(500,"userId 为空");
        Map map = new HashMap();
        map.put("pageNum",pageNum);
        map.put("userId",userId);
        PageInfo pageInfo = userService.selectSonUserInfo(map);
        if (pageInfo.getList().size()>0) return new CommonResult(200,"查询成功",pageInfo);
        return new CommonResult(201,"未查询到结果",null);
    }

    /**
     * 分页查询所有返现红包
     * status ： 0未使用，1使用后未付款，2使用后已付款，3已过期，4已返现
     * @param request
     * @return
     */
    @GetMapping("/app/user/selectSonCoupon")
    public CommonResult selectSonCoupon(HttpServletRequest request){
        String pageNum = request.getParameter("pageNum");
        if (StringUtil.isEmpty(pageNum)) pageNum = "1";
        String shareUserId = request.getParameter("shareUserId");//分享者用户id
        if (StringUtil.isEmpty(shareUserId)) return new CommonResult(500,"shareUserId 为空");
        String ownUserId = request.getParameter("ownUserId");//拥有者用户id
        if (StringUtil.isEmpty(ownUserId)) return new CommonResult(500,"ownUserId 为空");
        String status = request.getParameter("status");
        if (StringUtil.isEmpty(status)) return new CommonResult(500,"status 为空");
        Map map = new HashMap();
        map.put("pageNum",pageNum);
        map.put("shareUserId",shareUserId);
        map.put("ownUserId",ownUserId);
        map.put("status",status);
        PageInfo pageInfo = userService.selectSonCoupon(map);
        if (pageInfo.getList().size()>0) return new CommonResult(200,"查询成功",pageInfo);
        return new CommonResult(201,"未查询到结果",null);
    }

    /**
     * 查询拥有 分享的红包 各状态总数量和总返利
     * @param request
     * @return
     */
    @GetMapping("/app/user/selectSonCouponInfo")
    public CommonResult selectSonCouponInfo(HttpServletRequest request){
        String userId = request.getParameter("userId");
        if (StringUtil.isEmpty(userId)) return new CommonResult(500,"userId 为空");
        Map map = new HashMap();
        map.put("userId",userId);
        Map map1 = userService.selectSonCouponInfo(map);
        if (map1!=null) return new CommonResult(200,"查询成功",map1);
        return new CommonResult(201,"未查询到结果",null);
    }

    /**
     * 查询指定用户拥有 分享的红包 各状态总数量和总返利
     * @param request
     * @return
     */
    @GetMapping("/app/user/selectSonUserCouponInfo")
    public CommonResult selectSonUserCouponInfo(HttpServletRequest request){
        String shareUserId = request.getParameter("shareUserId");//分享者用户id
        if (StringUtil.isEmpty(shareUserId)) return new CommonResult(500,"shareUserId 为空");
        String ownUserId = request.getParameter("ownUserId");//拥有者用户id
        if (StringUtil.isEmpty(ownUserId)) return new CommonResult(500,"ownUserId 为空");
        Map map = new HashMap();
        map.put("shareUserId",shareUserId);
        map.put("ownUserId",ownUserId);
        Map map1 = userService.selectSonUserCouponInfo(map);
        if (map1!=null) return new CommonResult(200,"查询成功",map1);
        return new CommonResult(201,"未查询到结果",null);
    }

    /**
     * 分页查询所有返现红包
     * @param request
     * @return
     */
    @PostMapping("/user/selectCoupon")
    public CommonResult selectCoupon(HttpServletRequest request){
        String pageNum = request.getParameter("pageNum");
        if (StringUtil.isEmpty(pageNum)) pageNum = "1";
        String status = request.getParameter("status");
        if (StringUtil.isEmpty(status)) return new CommonResult(500,"status 为空");
        PageInfo pageInfo = userService.selectCoupon(Integer.parseInt(pageNum),Integer.parseInt(status));
        if (pageInfo.getList().size()>0) return new CommonResult(200,"查询成功",pageInfo);
        return new CommonResult(201,"未查询到结果",null);
    }

    /**
     * 分页查询该用户所有返现红包
     * @param request
     * @return
     */
    @PostMapping("/user/selectUserCoupon")
    public CommonResult selectUserCoupon(HttpServletRequest request){
        String pageNum = request.getParameter("pageNum");
        if (StringUtil.isEmpty(pageNum)) pageNum = "1";
        String userId = request.getParameter("userId");
        if (StringUtil.isEmpty(userId)) return new CommonResult(500,"userId 为空");
        String status = request.getParameter("status");
        if (StringUtil.isEmpty(status)) return new CommonResult(500,"status 为空");
        String type = request.getParameter("type");
        HashMap<String, String> map = new HashMap<>();
        map.put("pageNum",pageNum);
        map.put("userId",userId);
        map.put("status",status);
        map.put("type",type);
        PageInfo pageInfo = userService.selectUserCoupon(map);
        if (pageInfo.getList().size()>0) return new CommonResult(200,"查询成功",pageInfo);
        return new CommonResult(201,"未查询到结果",null);
    }

    /**
     * 指定用户新增返现红包
     * @param request
     * @return
     */
    @PostMapping("/user/addCoupon")
    public CommonResult addCoupon(HttpServletRequest request){
        String userId = request.getParameter("userId");
        if (StringUtil.isEmpty(userId)) return new CommonResult(500,"userId 为空");
        String name = request.getParameter("name");
        if (StringUtil.isEmpty(name)) return new CommonResult(500,"name 为空");
        String full = request.getParameter("full");
        if (StringUtil.isEmpty(full)) return new CommonResult(500,"full 为空");
        String subtract = request.getParameter("subtract");
        if (StringUtil.isEmpty(subtract)) return new CommonResult(500,"subtract 为空");
        String time = request.getParameter("time");
        if (StringUtil.isEmpty(time)) return new CommonResult(500,"time 为空");
        String type = request.getParameter("type");
        if (StringUtil.isEmpty(type)) return new CommonResult(500,"type 为空");
        String number = request.getParameter("number");
        if (StringUtil.isEmpty(number)) return new CommonResult(500,"number 为空");
        Map<String,String> map = new HashMap<>();
        map.put("userId",userId);
        map.put("name",name);
        map.put("full",full);
        map.put("subtract",subtract);
        map.put("time",time);
        map.put("type",type);
        map.put("number",number);
        int i = userService.addCoupon(map);
        if (i>0) return new CommonResult(200,"新增成功");
        return new CommonResult(201,"新增失败");
    }

    /**
     * 领取分享的红包
     * @param request
     * @return
     */
    @PostMapping("/app/user/getShare")
    public CommonResult getShare(HttpServletRequest request){
        String couponId = request.getParameter("couponId");//红包id
        if (StringUtil.isEmpty(couponId)) return new CommonResult(500,"couponId 为空");
        String agoUserId = request.getParameter("agoUserId");//现拥有者userId
        if (StringUtil.isEmpty(agoUserId)) return new CommonResult(500,"agoUserId 为空");
        String userId = request.getParameter("userId");//领取红包者userId
        if (StringUtil.isEmpty(userId)) return new CommonResult(500,"userId 为空");
        Map<String,String> map = new HashMap<>();
        map.put("couponId",couponId);
        map.put("agoUserId",agoUserId);
        map.put("userId",userId);
        int i = userService.getShare(map);
        if (i==0) return new CommonResult(200,"领取成功");
        else if (i==1) return new CommonResult(201,"该红包已被领取");
        else if (i==2) return new CommonResult(201,"该红包已过期");
        else if (i==3) return new CommonResult(201,"该红包已使用");
        else if (i==5) return new CommonResult(201,"不可领取自己的分享");
        else if (i==6) return new CommonResult(201,"该优惠卷不是返现卷，不可分享");
        else return new CommonResult(201,"领取失败");
    }

    /**
     * 根据status查询该用户的红包
     * 新增type参数（不传根据userId和status查，若传根据userId、type和status查）
     * @param request
     * @return
     */
    @GetMapping("/app/user/couponList")
    public CommonResult couponList(HttpServletRequest request){
        String status = request.getParameter("status");
        if (StringUtil.isEmpty(status)) return new CommonResult(500,"status 为空");
        String userId = request.getParameter("userId");
        if (StringUtil.isEmpty(userId)) return new CommonResult(500,"userId 为空");
        String type = request.getParameter("type");
        Map<String,String> map = new HashMap<>();
        map.put("status",status);
        map.put("userId",userId);
        map.put("type",type);
        List<Map<String,Object>> coupons = userService.couponList(map);
        if (coupons.size()>0) return new CommonResult(200,"查询成功",coupons);
        return new CommonResult(201,"未查询到结果",null);
    }

    /**
     * 通过用户id查询账户余额
     * @param request
     * @return
     */
    @GetMapping("/app/user/userPrice")
    public CommonResult userPrice(HttpServletRequest request){
        String userId = request.getParameter("userId");
        if (StringUtil.isEmpty(userId)) return new CommonResult(500,"userId 为空");
        return new CommonResult(200,"成功",userService.userPrice(userId));
    }

    /**
     * 通过用户id查询会员预存金额和待提现金额
     * @param request
     * @return
     */
    @GetMapping("/app/user/userMemberPrice")
    public CommonResult userMemberPrice(HttpServletRequest request){
        String userId = request.getParameter("userId");
        if (StringUtil.isEmpty(userId)) return new CommonResult(500,"userId 为空");
        return new CommonResult(200,"成功",userService.userMemberPrice(userId));
    }

    /**
     * 分页查询用户账户明细
     * @param request
     * @return
     */
    @GetMapping("/app/user/userPriceList")
    public CommonResult userPriceList(HttpServletRequest request){
        String pageNum = request.getParameter("pageNum");
        if (StringUtil.isEmpty(pageNum)) pageNum = "1";
        String userId = request.getParameter("userId");
        if (StringUtil.isEmpty(userId)) return new CommonResult(500,"userId 为空");
        PageInfo pageInfo = userService.userPriceList(Integer.parseInt(pageNum),userId);
        if (pageInfo.getList().size()>0)return new CommonResult(200,"查询成功",pageInfo);
        return new CommonResult(201,"未查询到结果",null);
    }

    /**
     * 分页查询用户会员预存金额和待提现金额明细
     * @param request
     * @return
     */
    @GetMapping("/app/user/userMemberPriceList")
    public CommonResult userMemberPriceList(HttpServletRequest request){
        String pageNum = request.getParameter("pageNum");
        if (StringUtil.isEmpty(pageNum)) pageNum = "1";
        String userId = request.getParameter("userId");
        if (StringUtil.isEmpty(userId)) return new CommonResult(500,"userId 为空");
        String type = request.getParameter("type");
        if (StringUtil.isEmpty(type)) return new CommonResult(500,"type 为空");
        PageInfo pageInfo = userService.userMemberPriceList(Integer.parseInt(pageNum),userId,type);
        if (pageInfo.getList().size()>0)return new CommonResult(200,"查询成功",pageInfo);
        return new CommonResult(201,"未查询到结果",null);
    }

    /**
     * 查询所有用户会员类别
     * @param request
     * @return
     */
    @GetMapping("/user/userMemberList")
    public CommonResult userMemberList(HttpServletRequest request){
        List<UserMember> userMemberList = userService.userMemberList();
        if (userMemberList.size()>0) return new CommonResult(200,"查询成功",userMemberList);
        return new CommonResult(201,"未查询到结果",null);
    }

    /**
     * 查询所有用户会员类别
     * @param request
     * @return
     */
    @GetMapping("/app/user/userMemberList")
    public CommonResult userMemberList1(HttpServletRequest request){
        List<UserMember> userMemberList = userService.userMemberList();
        if (userMemberList.size()>0) return new CommonResult(200,"查询成功",userMemberList);
        return new CommonResult(201,"未查询到结果",null);
    }

    /**
     * 修改单条用户会员类别
     * @param request
     * @return
     */
    @PostMapping("/user/updateUserMember")
    public CommonResult updateUserMember(HttpServletRequest request){
        String id = request.getParameter("id");
        if (StringUtil.isEmpty(id)) return new CommonResult(500,"id 为空");
        String name = request.getParameter("name");
        if (StringUtil.isEmpty(name)) return new CommonResult(500,"name 为空");
        String moneyMin = request.getParameter("moneyMin");
        if (StringUtil.isEmpty(moneyMin)) return new CommonResult(500,"moneyMin 为空");
        String moneyMax = request.getParameter("moneyMax");
        if (StringUtil.isEmpty(moneyMax)) return new CommonResult(500,"moneyMax 为空");
        String discounts = request.getParameter("discounts");
        if (StringUtil.isEmpty(discounts)) return new CommonResult(500,"discounts 为空");
        Map<String,Object> map = new HashMap<>();
        map.put("id",Integer.parseInt(id));map.put("name",name);
        map.put("moneyMin",moneyMin);map.put("moneyMax",moneyMax);
        map.put("discounts",discounts);
        int i = userService.updateUserMember(map);
        if (i>0) return new CommonResult(200,"修改成功");
        return new CommonResult(201,"修改失败");
    }

    /**
     * 修改多条用户会员类别
     * @param request
     * @return
     */
    @PostMapping("/user/updateUserMembers")
    public CommonResult updateUserMembers(HttpServletRequest request){
        String userMemberListJson = request.getParameter("userMemberListJson");
        if (StringUtil.isEmpty(userMemberListJson)) return new CommonResult(500,"userMemberListJson 为空");
        int i = userService.updateUserMembers(userMemberListJson);
        if (i>0) return new CommonResult(200,"修改成功");
        return new CommonResult(201,"修改失败");
    }

    /**
     * 通过会员类别id查询折扣
     * @param request
     * @return
     */
    @GetMapping("/user/selectUserMemberModel")
    public CommonResult selectUserMemberModel(HttpServletRequest request){
        String memberId = request.getParameter("memberId");
        if (StringUtil.isEmpty(memberId)) return new CommonResult(500,"memberId 为空");
        Map<String,String> map = new HashMap<>();
        map.put("memberId",memberId);
        List<Map<String,String>> userMemberModelList = userService.selectUserMemberModel(map);
        if (userMemberModelList.size()>0) return new CommonResult(200,"查询成功",userMemberModelList);
        return new CommonResult(201,"未查询到结果",null);
    }

    /**
     * 新增会员折扣
     * @param request
     * @return
     */
    @PostMapping("/user/addUserMemberModel")
    public CommonResult addUserMemberModel(HttpServletRequest request){
        String memberId = request.getParameter("memberId");
        if (StringUtil.isEmpty(memberId)) return new CommonResult(500,"memberId 为空");
        String modelId = request.getParameter("modelId");
        if (StringUtil.isEmpty(modelId)) return new CommonResult(500,"modelId 为空");
        String discount = request.getParameter("discount");
        if (StringUtil.isEmpty(discount)) return new CommonResult(500,"discount 为空");
        Map<String,String> map = new HashMap<>();
        map.put("memberId",memberId);map.put("modelId",modelId);map.put("discount",discount);
        Integer i = userService.addUserMemberModel(map);
        if (i==201) return new CommonResult(201,"该级别会员已有该型号折扣");
        else if (i>0) return new CommonResult(200,"新增成功");
        else return new CommonResult(201,"新增失败");
    }

    /**
     * 通过id删除会员折扣
     * @param request
     * @return
     */
    @PostMapping("/user/delUserMemberModel")
    public CommonResult delUserMemberModel(HttpServletRequest request){
        String userMemberModelId = request.getParameter("userMemberModelId");
        if (StringUtil.isEmpty(userMemberModelId)) return new CommonResult(500,"userMemberModelId 为空");
        int i = userService.delUserMemberModel(userMemberModelId);
        if (i>0) return new CommonResult(200,"删除成功");
        return new CommonResult(201,"删除失败");
    }

    /**
     * 通过用户id查询会员折扣
     * @param request
     * @return
     */
    @GetMapping("/app/user/userMemberModelList")
    public CommonResult userMemberModelList(HttpServletRequest request){
        String userId = request.getParameter("userId");
        if (StringUtil.isEmpty(userId)) return new CommonResult(500,"userId 为空");
        List<UserMemberModel> userMemberModelList = userService.userMemberModelList(userId);
        if (userMemberModelList==null) return new CommonResult(201,"当前用户不存在",null);
        else if (userMemberModelList.size()>0) return new CommonResult(200,"查询成功",userMemberModelList);
        else return new CommonResult(201,"未查询到结果",null);
    }

    /**
     * 升级用户为正式用户
     * @param request
     * @return
     */
    @PostMapping("/user/updateUserSerialNumber")
    public CommonResult updateUserSerialNumber(HttpServletRequest request){
        String userId = request.getParameter("userId");
        if (StringUtil.isEmpty(userId)) return new CommonResult(500,"userId 为空");
        String serialNumber = request.getParameter("serialNumber");
        if (StringUtil.isEmpty(serialNumber)) return new CommonResult(500,"serialNumber 为空");
        Map<String,String> map = new HashMap<>();
        map.put("userId",userId);map.put("serialNumber",serialNumber);
        Integer i = userService.updateUserSerialNumber(map);
        if (i==201) return new CommonResult(201,"该编号已存在");
        else if (i==202) return new CommonResult(201,"该用户不存在");
        else if (i==203) return new CommonResult(201,"该用户已是正式用户");
        else if (i>0) return new CommonResult(200,"修改成功");
        else return new CommonResult(201,"修改失败");
    }

    /**
     * 新增开通申请
     * @param request
     * @return
     */
    @PostMapping("/app/user/addUserMemberApply")
    public CommonResult addUserMemberApply(HttpServletRequest request){
        String userId = request.getParameter("userId");
        if (StringUtil.isEmpty(userId)) return new CommonResult(500,"userId 为空");
        String memberId = request.getParameter("memberId");
        if (StringUtil.isEmpty(memberId)) return new CommonResult(500,"memberId 为空");
        String name = request.getParameter("name");
        if (StringUtil.isEmpty(name)) return new CommonResult(500,"name 为空");
        String phone = request.getParameter("phone");
        if (StringUtil.isEmpty(phone)) return new CommonResult(500,"phone 为空");
        String type = request.getParameter("type");
        if (StringUtil.isEmpty(type)) return new CommonResult(500,"type 为空");//0:把原会员预存金额转为待提现金额 1:把原会员预存金额转为新会员预存金额
        Map<String,String> map = new HashMap<>();
        if ("0".equals(memberId)){
            String identity = request.getParameter("identity");
            if (StringUtil.isEmpty(identity)) return new CommonResult(500,"identity 为空");
            map.put("identity",identity);
        }
        map.put("userId",userId);map.put("memberId",memberId);
        map.put("name",name);map.put("phone",phone);map.put("type",type);
        Integer i = userService.addUserMemberApply(map);
        if (i==201) return new CommonResult(201,"该用户不存在");
        else if (i==202) return new CommonResult(201,"该用户已是正式用户，不可多次申请");
        else if (i==203) return new CommonResult(201,"该会员不存在");
        else if (i==204) return new CommonResult(201,"每个用户同时只可存在一条申请");
        else if (i==205) return new CommonResult(201,"已是当前申请的会员类型");
        else if (i==206) return new CommonResult(201,"会员预存金额超过会员预存金额转待提现金额最大金额");
        else if (i>0) return new CommonResult(200,"新增成功");
        else return new CommonResult(201,"新增失败");
    }

    /**
     * 分页查询申请列表
     * @param request
     * @return
     */
    @GetMapping("/user/selectUserMemberApply")
    public CommonResult selectUserMemberApply(HttpServletRequest request){
        String pageNum = request.getParameter("pageNum");
        if (StringUtil.isEmpty(pageNum)) pageNum = "1";
        String name = request.getParameter("name");
        String phone = request.getParameter("phone");
        String status = request.getParameter("status");
        if (StringUtil.isEmpty(status)) return new CommonResult(500,"status 为空");
        Map<String,String> map = new HashMap<>();
        map.put("pageNum",pageNum);map.put("name",name);map.put("phone",phone);map.put("status",status);
        PageInfo pageInfo = userService.selectUserMemberApply(map);
        if (pageInfo.getList().size()>0) return new CommonResult(200,"查询成功",pageInfo);
        return new CommonResult(201,"未查询到结果",null);
    }

    /**
     * 升级为正式用户
     * @param request
     * @return
     */
    @PostMapping("/user/updateUserStatus")
    public CommonResult updateUserStatus(HttpServletRequest request){
        String userMemberApplyId = request.getParameter("userMemberApplyId");
        if (StringUtil.isEmpty(userMemberApplyId)) return new CommonResult(500,"userMemberApplyId 为空");
        String userId = request.getParameter("userId");
        if (StringUtil.isEmpty(userId)) return new CommonResult(500,"userId 为空");
        String serialNumber = request.getParameter("serialNumber");
        if (StringUtil.isEmpty(serialNumber)) return new CommonResult(500,"serialNumber 为空");
        Map<String,String> map = new HashMap<>();
        map.put("userMemberApplyId",userMemberApplyId);map.put("userId",userId);map.put("serialNumber",serialNumber);
        Integer i = userService.updateUserStatus(map);
        if (i==204) return new CommonResult(201,"该申请不存在");
        else if (i==201) return new CommonResult(201,"该编号已存在");
        else if (i==202) return new CommonResult(201,"该用户不存在");
        else if (i==203) return new CommonResult(201,"该用户已是正式用户");
        else if (i>0) return new CommonResult(200,"修改成功");
        else return new CommonResult(201,"修改失败");
    }

    /**
     * 升级为会员
     * @param request
     * @return
     */
    @PostMapping("/user/updateUserMember1")
    public CommonResult updateUserMember1(HttpServletRequest request){
        Map<String,String> map = new HashMap<>();
        String userMemberApplyId = request.getParameter("userMemberApplyId");
        if (StringUtil.isEmpty(userMemberApplyId)) return new CommonResult(500,"userMemberApplyId 为空");
        String userId = request.getParameter("userId");
        if (StringUtil.isEmpty(userId)) return new CommonResult(500,"userId 为空");
        String memberId = request.getParameter("memberId");
        if (StringUtil.isEmpty(memberId)) return new CommonResult(500,"memberId 为空");
        String userStatus = request.getParameter("userStatus");
        if (StringUtil.isEmpty(userStatus)) return new CommonResult(500,"userStatus 为空");
        if ("0".equals(userStatus)) {
            String serialNumber = request.getParameter("serialNumber");
            if (StringUtil.isEmpty(serialNumber)) return new CommonResult(500,"serialNumber 为空");
            map.put("serialNumber",serialNumber);
        }
        String memberPrice = request.getParameter("memberPrice");
        if (StringUtil.isEmpty(memberPrice)) return new CommonResult(500,"memberPrice 为空");
        map.put("userMemberApplyId",userMemberApplyId);map.put("userId",userId);
        map.put("memberId",memberId);map.put("userStatus",userStatus);map.put("memberPrice",memberPrice);
        Integer i = userService.updateUserMember1(map);
        if (i==201) return new CommonResult(201,"该申请不存在");
        else if (i==202) return new CommonResult(201,"该用户不存在");
        else if (i==203) return new CommonResult(201,"该会员类型不存在");
        else if (i==204) return new CommonResult(201,"该编号已存在");
        else if (i==205) {
            UserMember userMember = userService.selectUserMemberById(memberId);
            return new CommonResult(201, "预存金额不足"+userMember.getName()+"最低预存金额:"+userMember.getMoneyMin());
        }
        else if (i>0) return new CommonResult(200,"修改成功");
        else return new CommonResult(201,"修改失败");
    }

    /**
     * 通过用户id查询
     * @param request
     * @return
     */
    @GetMapping("/user/selectUserInfoMap")
    public CommonResult selectUserInfoMap(HttpServletRequest request){
        String userId = request.getParameter("userId");
        if (StringUtil.isEmpty(userId)) return new CommonResult(500,"userId 为空");
        Map<String,String> map = userService.selectUserInfoMap(userId);
        if (map!=null) return new CommonResult(200,"查询成功",map);
        else return new CommonResult(201,"未查询到结果",null);
    }

    /**
     * 通过用户id查询用户
     * @param request
     * @return
     */
    @GetMapping("/user/userInfoById")
    public CommonResult userInfoById(HttpServletRequest request){
        String userId = request.getParameter("userId");
        if (StringUtil.isEmpty(userId)) return new CommonResult(500,"userId 为空");
        UserInfo userInfo = userService.userInfoById(userId);
        if (userInfo!=null) return new CommonResult(200,"查询成功",userInfo);
        else return new CommonResult(201,"未查询到结果",null);
    }

    /**
     * 通过用户id修改用户
     * @param request
     * @return
     */
    @PostMapping("/user/updateUserInfo")
    public CommonResult updateUserInfo(HttpServletRequest request){
        String userId = request.getParameter("userId");
        if (StringUtil.isEmpty(userId)) return new CommonResult(500,"userId 为空");
        String memberId = request.getParameter("memberId");
        if (StringUtil.isEmpty(memberId)) return new CommonResult(500,"memberId 为空");
        String price = request.getParameter("price");
        if (StringUtil.isEmpty(price)) return new CommonResult(500,"price 为空");
        String goldCoin = request.getParameter("goldCoin");
        if (StringUtil.isEmpty(goldCoin)) return new CommonResult(500,"goldCoin 为空");
        String serialNumber = request.getParameter("serialNumber");
        String status = request.getParameter("status");
        if (StringUtil.isEmpty(status)) return new CommonResult(500,"status 为空");
        String name = request.getParameter("name");
        if (StringUtil.isEmpty(name)) return new CommonResult(500,"name 为空");
        String phone = request.getParameter("phone");
        if (StringUtil.isEmpty(phone)) return new CommonResult(500,"phone 为空");
        String identity = request.getParameter("identity");
        if (StringUtil.isEmpty(identity)) return new CommonResult(500,"identity 为空");
        String memberPrice = request.getParameter("memberPrice");
        if (StringUtil.isEmpty(memberPrice)) return new CommonResult(500,"memberPrice 为空");
        String withdrawPrice = request.getParameter("withdrawPrice");
        if (StringUtil.isEmpty(withdrawPrice)) return new CommonResult(500,"withdrawPrice 为空");
        Map<String,String> map = new HashMap<>();
        map.put("userId",userId);map.put("memberId",memberId);map.put("price",price);
        map.put("goldCoin",goldCoin);map.put("serialNumber",serialNumber);
        map.put("status",status);map.put("memberPrice",memberPrice);
        map.put("name",name);map.put("phone",phone);map.put("identity",identity);
        map.put("withdrawPrice",withdrawPrice);
        Integer i = userService.updateUserInfo(map);
        if (i==201) return new CommonResult(201,"该用户不存在");
        else if (i==202) return new CommonResult(201,"该会员类型不存在");
        else if (i==203) return new CommonResult(201,"该编号已存在");
        else if (i>0) return new CommonResult(200,"修改成功");
        else return new CommonResult(201,"修改失败");
    }

    /**
     * 新增待提现金额提现申请
     * @param request
     * @return
     */
    @PostMapping("/app/user/addUserWithdrawPriceApply")
    public CommonResult addUserWithdrawPriceApply(HttpServletRequest request){
        String userId = request.getParameter("userId");
        if (StringUtil.isEmpty(userId)) return new CommonResult(500,"userId 为空");
        String withdrawPrice = request.getParameter("withdrawPrice");
        if (StringUtil.isEmpty(withdrawPrice)) return new CommonResult(500,"withdrawPrice 为空");
        String name = request.getParameter("name");
        if (StringUtil.isEmpty(name)) return new CommonResult(500,"name 为空");
        String phone = request.getParameter("phone");
        if (StringUtil.isEmpty(phone)) return new CommonResult(500,"phone 为空");
        String bankName = request.getParameter("bankName");
        if (StringUtil.isEmpty(bankName)) return new CommonResult(500,"bankName 为空");
        String bankNumder = request.getParameter("bankNumder");
        if (StringUtil.isEmpty(bankNumder)) return new CommonResult(500,"bankNumder 为空");
        String cardholder = request.getParameter("cardholder");
        if (StringUtil.isEmpty(cardholder)) return new CommonResult(500,"cardholder 为空");
        Map<String,String> map = new HashedMap();
        map.put("userId",userId);map.put("withdrawPrice",withdrawPrice);
        map.put("name",name);map.put("phone",phone);
        map.put("bankName",bankName);map.put("bankNumder",bankNumder);
        map.put("cardholder",cardholder);
        Integer i = userService.addUserWithdrawPriceApply(map);
        if (i==201) return new CommonResult(201,"该用户不存在");
        else if (i==202) return new CommonResult(201,"该用户待提现金额不足以本次申请扣除");
        else if (i>0) return new CommonResult(200,"申请成功");
        else return new CommonResult(200,"申请失败");
    }

    /**
     * 分页查询待提现金额提现申请
     * @param request
     * @return
     */
    @GetMapping("/user/userWithdrawPriceApplyLitsPage")
    public CommonResult userWithdrawPriceApplyLitsPage(HttpServletRequest request){
        String pageNum = request.getParameter("pageNum");
        if (StringUtil.isEmpty(pageNum)) pageNum = "1";
        String status = request.getParameter("status");
        if (StringUtil.isEmpty(status)) return new CommonResult(500,"status 为空",null);
        String name = request.getParameter("name");
        String phone = request.getParameter("phone");
        String bankName = request.getParameter("bankName");
        String bankNumder = request.getParameter("bankNumder");
        String cardholder = request.getParameter("cardholder");
        Map<String,String> map = new HashedMap();
        map.put("status",status);map.put("pageNum",pageNum);map.put("name",name);
        map.put("phone",phone);map.put("bankName",bankName);
        map.put("bankNumder",bankNumder);map.put("cardholder",cardholder);
        PageInfo pageInfo = userService.userWithdrawPriceApplyLitsPage(map);
        if (pageInfo.getList().size()>0) return new CommonResult(200,"查询成功",pageInfo);
        else return new CommonResult(201,"未查询到结果",null);
    }

    /**
     * 修改带提现金额申请状态
     * @param request
     * @return
     */
    @PostMapping("/user/userWithdrawPriceApplyStatus")
    public CommonResult userWithdrawPriceApplyStatus(HttpServletRequest request){
        String userWithdrawPriceApplyId = request.getParameter("userWithdrawPriceApplyId");
        if (StringUtil.isEmpty(userWithdrawPriceApplyId)) return new CommonResult(500,"userWithdrawPriceApplyId 为空",null);
        String status = request.getParameter("status");
        if (StringUtil.isEmpty(status)) return new CommonResult(500,"status 为空",null);
        Map<String,String> map = new HashedMap();
        map.put("userWithdrawPriceApplyId",userWithdrawPriceApplyId);
        map.put("status",status);
        Integer i = userService.userWithdrawPriceApplyStatus(map);
        if (i>0) return new CommonResult(200,"修改成功");
        else return new CommonResult(201,"修改失败");
    }


    /**
     * 根据锁价id查询锁价数据
     * @param request
     * @return
     */
    @GetMapping("/user/selectUserLockById")
    public CommonResult selectUserLockById(HttpServletRequest request){
        String userLockId = request.getParameter("userLockId");
        if (StringUtil.isEmpty(userLockId)) return new CommonResult(500,"userLockId 为空");
        UserLock userLock = userService.selectUserLockById(userLockId);
        if (userLock==null) return new CommonResult(201,"未查询到结果",null);
        else return new CommonResult(200,"查询成功",userLock);
    }

    /**
     * 新增采购方信息
     * @param request
     * @return
     */
    @PostMapping("/app/user/addUserPurchaser")
    public CommonResult addUserPurchaser(HttpServletRequest request){
        String userId = request.getParameter("userId");
        if (StringUtil.isEmpty(userId)) return new CommonResult(500,"userId 为空");
        String companyName = request.getParameter("companyName");
        if (StringUtil.isEmpty(companyName)) return new CommonResult(500,"companyName 为空");
        String userName = request.getParameter("userName");
        if (StringUtil.isEmpty(userName)) return new CommonResult(500,"userName 为空");
        String phone = request.getParameter("phone");
        if (StringUtil.isEmpty(phone)) return new CommonResult(500,"phone 为空");
        String status = request.getParameter("status");
        if (StringUtil.isEmpty(status)) return new CommonResult(500,"status 为空");
        HashMap<String, String> map = new HashMap<>();
        map.put("userId",userId);
        map.put("companyName",companyName);
        map.put("userName",userName);
        map.put("phone",phone);
        map.put("status",status);
        Integer i = userService.addUserPurchaser(map);
        if (i>0) return new CommonResult(200,"新增成功");
        else return new CommonResult(201,"新增失败");
    }

    /**
     * 修改采购方信息
     * @param request
     * @return
     */
    @PostMapping("/app/user/updateUserPurchaser")
    public CommonResult updateUserPurchaser(HttpServletRequest request){
        String userPurchaserId = request.getParameter("userPurchaserId");
        if (StringUtil.isEmpty(userPurchaserId)) return new CommonResult(500,"userPurchaserId 为空");
        Map<String, String> map = new HashMap<>();
        map.put("userPurchaserId",userPurchaserId);
        String companyName = request.getParameter("companyName");
        if (!StringUtil.isEmpty(companyName)) map.put("companyName",companyName);
        String userName = request.getParameter("userName");
        if (!StringUtil.isEmpty(userName)) map.put("userName",userName);
        String phone = request.getParameter("phone");
        if (!StringUtil.isEmpty(phone)) map.put("phone",phone);
        String status = request.getParameter("status");
        if (!StringUtil.isEmpty(status)) map.put("status",status);
        Integer i = userService.updateUserPurchaser(map);
        if (i>0) return new CommonResult(200,"修改成功");
        else return new CommonResult(201,"修改失败");
    }

    /**
     * 采购方信息设为默认
     * @param request
     * @return
     */
    @PostMapping("/app/user/updateUserPurchaserStatus")
    public CommonResult updateUserPurchaserStatus(HttpServletRequest request){
        String userPurchaserId = request.getParameter("userPurchaserId");
        if (StringUtil.isEmpty(userPurchaserId)) return new CommonResult(500,"userPurchaserId 为空");
        Map<String, String> map = new HashMap<>();
        map.put("userPurchaserId",userPurchaserId);
        map.put("status","0");
        Integer i = userService.updateUserPurchaser(map);
        if (i>0) return new CommonResult(200,"修改成功");
        else return new CommonResult(201,"修改失败");
    }

    /**
     * 删除采购方信息
     * @param request
     * @return
     */
    @PostMapping("/app/user/delUserPurchaser")
    public CommonResult delUserPurchaser(HttpServletRequest request){
        String userPurchaserId = request.getParameter("userPurchaserId");
        if (StringUtil.isEmpty(userPurchaserId)) return new CommonResult(500,"userPurchaserId 为空");
        Integer i = userService.delUserPurchaser(userPurchaserId);
        if (i>0) return new CommonResult(200,"删除成功");
        else return new CommonResult(201,"删除失败");
    }

    /**
     * 根据用户id查询所有采购方信息
     * @param request
     * @return
     */
    @GetMapping("/app/user/getUserPurchaserList")
    public CommonResult getUserPurchaserList(HttpServletRequest request){
        String userId = request.getParameter("userId");
        if (StringUtil.isEmpty(userId)) return new CommonResult(500,"userId 为空");
        Map<String, String> map = new HashMap<>();
        map.put("userId",userId);
        String companyName = request.getParameter("companyName");
        if (!StringUtil.isEmpty(companyName)) map.put("companyName",companyName);
        String userName = request.getParameter("userName");
        if (!StringUtil.isEmpty(userName)) map.put("userName",userName);
        String phone = request.getParameter("phone");
        if (!StringUtil.isEmpty(phone)) map.put("phone",phone);
        String status = request.getParameter("status");
        if (!StringUtil.isEmpty(status)) map.put("status",status);
        List<UserPurchaser> userPurchaserList = userService.getUserPurchaserList(map);
        if (userPurchaserList.size()>0) return new CommonResult(200,"查询成功",userPurchaserList);
        else return new CommonResult(201,"未查询到结果");
    }

    /**
     * 根据用户id分页查询采购方信息
     * @param request
     * @return
     */
    @GetMapping("/app/user/getUserPurchaserListPage")
    public CommonResult getUserPurchaserListPage(HttpServletRequest request){
        String pageNum = request.getParameter("pageNum");
        if (StringUtil.isEmpty(pageNum)) pageNum = "1";
        String userId = request.getParameter("userId");
        if (StringUtil.isEmpty(userId)) return new CommonResult(500,"userId 为空");
        Map<String, String> map = new HashMap<>();
        map.put("pageNum",pageNum);
        map.put("userId",userId);
        String companyName = request.getParameter("companyName");
        if (!StringUtil.isEmpty(companyName)) map.put("companyName",companyName);
        String userName = request.getParameter("userName");
        if (!StringUtil.isEmpty(userName)) map.put("userName",userName);
        String phone = request.getParameter("phone");
        if (!StringUtil.isEmpty(phone)) map.put("phone",phone);
        String status = request.getParameter("status");
        if (!StringUtil.isEmpty(status)) map.put("status",status);
        PageInfo pageInfo = userService.getUserPurchaserListPage(map);
        if (pageInfo.getList().size()>0) return new CommonResult(200,"查询成功",pageInfo);
        else return new CommonResult(201,"未查询到结果");
    }


}
