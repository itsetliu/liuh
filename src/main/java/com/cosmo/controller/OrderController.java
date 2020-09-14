package com.cosmo.controller;

import com.alibaba.druid.util.StringUtils;
import com.cosmo.entity.*;
import com.cosmo.service.OrderService;
import com.cosmo.util.*;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class OrderController {

    @Resource
    private OrderService orderService;

    @GetMapping("/app/projectExport")
    public ResponseEntity<?> export(HttpServletRequest request){
        String orderId = request.getParameter("orderId");
        if (StringUtils.isEmpty(orderId)) return new ResponseEntity<String>("{ \"code\" : \"404\", \"message\" : \"not found\" }", null, HttpStatus.NOT_FOUND);
        try {
            ResponseEntity<?> responseEntity = orderService.export(Integer.parseInt(orderId));
            return responseEntity;
        } catch (Exception e) {
            e.printStackTrace();
        }

        HttpHeaders headers = new HttpHeaders();
//        headers.setContentType(MediaType.APPLICATION_JSON_UTF8);
        headers.setContentType(MediaType.APPLICATION_JSON);
        return new ResponseEntity<String>("{ \"code\" : \"404\", \"message\" : \"not found\" }",
                headers, HttpStatus.NOT_FOUND);
    }

    /**
     * 新增商品到购物车
     * @param request
     * @return
     */
    @PostMapping("/app/order/addOrderModel")
    public CommonResult addOrderModel(HttpServletRequest request){
        Map<String,String> map = new HashMap<>();
        String userId = request.getParameter("userId");
        if (StringUtil.isEmpty(userId)) return new CommonResult(500,"userId 为空");
        String modelType = request.getParameter("modelType");
        if (StringUtil.isEmpty(modelType)) return new CommonResult(500,"modelType 为空");
        String modelName = request.getParameter("modelName");
        if (StringUtil.isEmpty(modelName)) return new CommonResult(500,"modelName 为空");
        String specWidth = request.getParameter("specWidth");
        if (StringUtil.isEmpty(specWidth)) return new CommonResult(500,"specWidth 为空");
        String specThickness = request.getParameter("specThickness");
        if (StringUtil.isEmpty(specThickness)) return new CommonResult(500,"specThickness 为空");
        String specLength = request.getParameter("specLength");
        if (StringUtil.isEmpty(specLength)) return new CommonResult(500,"specLength 为空");
        String specSuttle = request.getParameter("specSuttle");
        if (StringUtil.isEmpty(specSuttle)) return new CommonResult(500,"specSuttle 为空");
        String pipeWeight = request.getParameter("pipeWeight");
        if (StringUtil.isEmpty(pipeWeight)) return new CommonResult(500,"pipeWeight 为空");
        String pipeDia = request.getParameter("pipeDia");
        if (StringUtil.isEmpty(pipeDia)) return new CommonResult(500,"pipeDia 为空");
        String cartonWeight = request.getParameter("cartonWeight");
        if (StringUtil.isEmpty(cartonWeight)) return new CommonResult(500,"cartonWeight 为空");
        String cartonType = request.getParameter("cartonType");
        if (StringUtil.isEmpty(cartonType)) return new CommonResult(500,"cartonType 为空");
        if ("1".equals(cartonType)){
            String cartonInfo = request.getParameter("cartonInfo");
            if (StringUtil.isEmpty(cartonInfo)) return new CommonResult(500,"cartonInfo 为空");
            map.put("cartonInfo",cartonInfo);
        }
        String cartonPipeNumber = request.getParameter("cartonPipeNumber");
        if (StringUtil.isEmpty(cartonPipeNumber)) return new CommonResult(500,"cartonPipeNumber 为空");
        String cartonNumber = request.getParameter("cartonNumber");
        if (StringUtil.isEmpty(cartonNumber)) return new CommonResult(500,"cartonNumber 为空");
        String labelType = request.getParameter("labelType");
        if (StringUtil.isEmpty(labelType)) return new CommonResult(500,"labelType 为空");
        if ("1".equals(labelType)){
            String labelInfo = request.getParameter("labelInfo");
            if (StringUtil.isEmpty(labelInfo)) return new CommonResult(500,"labelInfo 为空");
            map.put("labelInfo",labelInfo);
        }
        String trayType = request.getParameter("trayType");
        if (StringUtil.isEmpty(trayType)) return new CommonResult(500,"trayType 为空");
        String trayNumber = request.getParameter("trayNumber");
        if (StringUtil.isEmpty(trayNumber)) return new CommonResult(500,"trayNumber 为空");
        String trayCapacity = request.getParameter("trayCapacity");
        if (StringUtil.isEmpty(trayCapacity)) return new CommonResult(500,"trayCapacity 为空");
        String rollNumber = request.getParameter("rollNumber");
        if (StringUtil.isEmpty(rollNumber)) return new CommonResult(500,"rollNumber 为空");
        String rollRoughWeight = request.getParameter("rollRoughWeight");
        if (StringUtil.isEmpty(rollRoughWeight)) return new CommonResult(500,"rollRoughWeight 为空");
        String modelUnitPrice = request.getParameter("modelUnitPrice");
        if (StringUtil.isEmpty(modelUnitPrice)) return new CommonResult(500,"modelUnitPrice 为空");
        String modelTotalPrice = request.getParameter("modelTotalPrice");
        if (StringUtil.isEmpty(modelTotalPrice)) return new CommonResult(500,"modelTotalPrice 为空");
        String modelProcessCost = request.getParameter("modelProcessCost");
        if (StringUtil.isEmpty(modelProcessCost)) return new CommonResult(500,"modelProcessCost 为空");
        String modelRawPrice = request.getParameter("modelRawPrice");
        if (StringUtil.isEmpty(modelRawPrice)) return new CommonResult(500,"modelRawPrice 为空");
        String modelRawPriceType = request.getParameter("modelRawPriceType");
        if (StringUtil.isEmpty(modelRawPriceType)) return new CommonResult(500,"modelRawPriceType 为空");
        if ("2".equals(modelRawPriceType)){
            String userLockId = request.getParameter("userLockId");
            if (StringUtil.isEmpty(userLockId)) return new CommonResult(500,"userLockId 为空");
            map.put("userLockId",userLockId);
        }
        String memberId = request.getParameter("memberId");
        if (StringUtil.isEmpty(memberId)) return new CommonResult(500,"memberId 为空");
        String memberDiscount = request.getParameter("memberDiscount");
        if (StringUtil.isEmpty(memberDiscount)) return new CommonResult(500,"memberDiscount 为空");
        map.put("userId",userId);map.put("modelType",modelType);map.put("modelName",modelName);
        map.put("specWidth",specWidth);map.put("specThickness",specThickness);map.put("specLength",specLength);
        map.put("specSuttle",specSuttle);map.put("pipeWeight",pipeWeight);map.put("pipeDia",pipeDia);
        map.put("cartonWeight",cartonWeight);map.put("cartonType",cartonType);
        map.put("cartonPipeNumber",cartonPipeNumber);map.put("cartonNumber",cartonNumber);map.put("labelType",labelType);
        map.put("trayType",trayType);map.put("trayNumber",trayNumber);
        map.put("trayCapacity",trayCapacity);map.put("rollNumber",rollNumber);map.put("rollRoughWeight",rollRoughWeight);
        map.put("modelUnitPrice",modelUnitPrice);map.put("modelTotalPrice",modelTotalPrice);map.put("modelProcessCost",modelProcessCost);
        map.put("modelRawPrice",modelRawPrice);map.put("modelRawPriceType",modelRawPriceType);
        map.put("memberId",memberId);map.put("memberDiscount",memberDiscount);
        Integer i = orderService.addOrderModel(map);
        if (i>0) return new CommonResult(200,"新增成功");
        return new CommonResult(201,"新增失败");
    }

    /**
     * 根据orderModelId删除购物车订单
     * @param request
     * @return
     */
    @PostMapping("/app/order/delOrderModel")
    public CommonResult delOrderModel(HttpServletRequest request){
        String orderModelId = request.getParameter("orderModelId");
        if (StringUtil.isEmpty(orderModelId)) return new CommonResult(500,"orderModelId 为空");
        Integer i = orderService.delOrderModel(Integer.parseInt(orderModelId));
        if (i>0) return new CommonResult(200,"删除成功");
        return new CommonResult(201,"删除失败");
    }

    /**
     * 根据用户id，orderModelStatus查询购物车
     * @param request
     * @return
     */
    @GetMapping("/app/order/orderModelList")
    public CommonResult orderModelList(HttpServletRequest request){
        String userId = request.getParameter("userId");
        if (StringUtil.isEmpty(userId)) return new CommonResult(500,"userId 为空");
        String modelRawPriceType = request.getParameter("modelRawPriceType");
        if (StringUtil.isEmpty(modelRawPriceType)) return new CommonResult(500,"modelRawPriceType 为空");
        Map<String,String> map = new HashMap<>();
        map.put("userId",userId);map.put("modelRawPriceType",modelRawPriceType);
        List<OrderModel> orderModelList = orderService.orderModelList(map);
        if (orderModelList.size()>0) return new CommonResult(200,"查询成功",orderModelList);
        return new CommonResult(201,"未查询到结果",null);
    }

    /**
     * 生成合同订单
     * @param request
     * @return
     */
    @PostMapping("/app/order/createOrderForm")
    public CommonResult createOrderForm(HttpServletRequest request){
        String userId = request.getParameter("userId");
        if (StringUtil.isEmpty(userId)) return new CommonResult(500,"userId 为空");
        String orderRemark = request.getParameter("orderRemark");
        if (StringUtil.isEmpty(orderRemark)) return new CommonResult(500,"orderRemark 为空");
        String rawPriceType = request.getParameter("rawPriceType");
        if (StringUtil.isEmpty(rawPriceType)) return new CommonResult(500,"rawPriceType 为空");
        String orderModelIds = request.getParameter("orderModelIds");
        if (StringUtil.isEmpty(orderModelIds)) return new CommonResult(500,"orderModelIds 为空");
        String orderAddressStatus = request.getParameter("orderAddressStatus");
        if (StringUtil.isEmpty(orderAddressStatus)) return new CommonResult(500,"orderAddressStatus 为空");
        Map<String,String> map = new HashMap<>();
        if ("0".equals(orderAddressStatus)||"1".equals(orderAddressStatus)){//单地址收货参数 或  自提
            String companyName = request.getParameter("companyName");
            if (StringUtil.isEmpty(companyName)) return new CommonResult(500,"companyName 为空");
            String userName = request.getParameter("userName");
            if (StringUtil.isEmpty(userName)) return new CommonResult(500,"userName 为空");
            String userPhone = request.getParameter("userPhone");
            if (StringUtil.isEmpty(userPhone)) return new CommonResult(500,"userPhone 为空");
            String userFax = request.getParameter("userFax");
            map.put("companyName",companyName);map.put("userName",userName);
            map.put("userPhone",userPhone);map.put("userFax",userFax);
            if ("0".equals(orderAddressStatus)){//单地址收货参数
                String userAddress = request.getParameter("userAddress");
                if (StringUtil.isEmpty(userAddress)) return new CommonResult(500,"userAddress 为空");
                String userDetailAddress = request.getParameter("userDetailAddress");
                if (StringUtil.isEmpty(userDetailAddress)) return new CommonResult(500,"userDetailAddress 为空");
                map.put("userAddress",userAddress);map.put("userDetailAddress",userDetailAddress);
            }
        }//else 地址待定
        map.put("userId",userId);map.put("orderRemark",orderRemark);map.put("rawPriceType",rawPriceType);
        map.put("orderModelIds",orderModelIds);map.put("orderAddressStatus",orderAddressStatus);
        Integer i = 0;
        if ("2".equals(rawPriceType)) {
            i = orderService.createOrderForm1(map);
            if (i==201) return new CommonResult(201,"所选购物车型号不是同次锁价数据");
            else if (i==202) return new CommonResult(201,"超过差价上限，请重新整理购物车");
            else if (i==203) return new CommonResult(201,"用户余额不足已补差价，请充值后再次操作");
            else if (i==204) return new CommonResult(201,"用户余额不足已补差价及支付运费，请充值后再次操作");
        }
        else {
            i = orderService.createOrderForm(map);
            if (i==201) return new CommonResult(201,"会员预存余额不足支付本次交易，请续费会员或调整购物车后生成订单");
        }
        if (i>0) return new CommonResult(200,"合并成功");
        return new CommonResult(201,"合并失败");
    }

    /**
     * 给待定订单新增地址
     * @param request
     * @return
     */
    @PostMapping("/app/order/addOrderAddress")
    public CommonResult addOrderAddress(HttpServletRequest request){
        Map<String,String> map = new HashMap<>();
        String userId = request.getParameter("userId");
        if (StringUtil.isEmpty(userId)) return new CommonResult(500,"userId 为空");
        String orderFormId = request.getParameter("orderFormId");
        if (StringUtil.isEmpty(orderFormId)) return new CommonResult(500,"orderFormId 为空");
        String orderModels = request.getParameter("orderModels");
        if (StringUtil.isEmpty(orderModels)) return new CommonResult(500,"orderModels 为空");
        String companyName = request.getParameter("companyName");
        if (StringUtil.isEmpty(companyName)) return new CommonResult(500,"companyName 为空");
        String userName = request.getParameter("userName");
        if (StringUtil.isEmpty(userName)) return new CommonResult(500,"userName 为空");
        String userPhone = request.getParameter("userPhone");
        if (StringUtil.isEmpty(userPhone)) return new CommonResult(500,"userPhone 为空");
        String userFax = request.getParameter("userFax");
        String orderAddressStatus = request.getParameter("orderAddressStatus");
        if (StringUtil.isEmpty(orderAddressStatus)) return new CommonResult(500,"orderAddressStatus 为空");
        if ("0".equals(orderAddressStatus)){
            String userAddress = request.getParameter("userAddress");
            if (StringUtil.isEmpty(userAddress)) return new CommonResult(500,"userAddress 为空");
            String userDetailAddress = request.getParameter("userDetailAddress");
            if (StringUtil.isEmpty(userDetailAddress)) return new CommonResult(500,"userDetailAddress 为空");
            map.put("userAddress",userAddress);map.put("userDetailAddress",userDetailAddress);
        }
        map.put("userId",userId);map.put("orderFormId",orderFormId);map.put("orderModels",orderModels);
        map.put("companyName",companyName);map.put("userName",userName);map.put("userPhone",userPhone);
        map.put("userFax",userFax);map.put("orderAddressStatus",orderAddressStatus);
        Integer i = orderService.addOrderAddress(map);
        if (i==501) return new CommonResult(201,"有型号卷数不足");
        else if (i==502) return new CommonResult(201,"余额不足支付本次运费");
        else if (i==503) return new CommonResult(201,"余额不足支付该订单的托盘差价");
        else if (i>0) return new CommonResult(200,"新增成功");
        return new CommonResult(200,"新增失败");
    }

    /**
     * 根据状态分页查询订单合同
     * @param request
     * @return
     */
    @GetMapping("/app/order/orderFormPageList")
    public CommonResult orderFormPageList(HttpServletRequest request){
        String pageNum = request.getParameter("pageNum");
        if (StringUtil.isEmpty(pageNum)) pageNum = "1";
        String orderStatus = request.getParameter("orderStatus");
        if (StringUtil.isEmpty(orderStatus)) return new CommonResult(500,"orderStatus 为空");
        String userId = request.getParameter("userId");
        if (StringUtil.isEmpty(userId)) return new CommonResult(500,"userId 为空");
        PageInfo pageInfo = orderService.orderFormPageList(Integer.parseInt(pageNum),Integer.parseInt(orderStatus),Integer.parseInt(userId));
        if (pageInfo.getList().size()>0) return new CommonResult(200,"查询成功",pageInfo);
        return new CommonResult(201,"未查询到结果",null);
    }

    /**
     * 根据状态查询订单合同
     * @param request
     * @return
     */
    @GetMapping("/app/order/orderFormList")
    public CommonResult orderFormList(HttpServletRequest request){
        String orderStatus = request.getParameter("orderStatus");
        if (StringUtil.isEmpty(orderStatus)) return new CommonResult(500,"orderStatus 为空");
        String userId = request.getParameter("userId");
        if (StringUtil.isEmpty(userId)) return new CommonResult(500,"userId 为空");
        List<OrderForm> orderFormList = orderService.orderFormList(Integer.parseInt(orderStatus),Integer.parseInt(userId));
        if (orderFormList.size()>0) return new CommonResult(200,"查询成功",orderFormList);
        return new CommonResult(201,"未查询到结果",null);
    }

    /**
     * 根据orderFormId查询orderForm详情
     * 加默认地址
     * @param request
     * @return
     */
    @GetMapping("/app/order/orderFormInfo")
    public CommonResult orderFormInfo(HttpServletRequest request){
        String orderFormId = request.getParameter("orderFormId");
        if (StringUtil.isEmpty(orderFormId)) return new CommonResult(500,"orderFormId 为空");
        OrderForm orderForm = orderService.orderFormInfo(Integer.parseInt(orderFormId));
        if (orderForm!=null) return new CommonResult(200,"查询成功",orderForm);
        return new CommonResult(201,"未查询到结果",null);
    }

    /**
     * 根据orderFormId查询orderForm详情
     * 加默认地址、加后增地址
     * @param request
     * @return
     */
    @GetMapping("/app/order/orderFormInfo1")
    public CommonResult orderFormInfo1(HttpServletRequest request){
        String orderFormId = request.getParameter("orderFormId");
        if (StringUtil.isEmpty(orderFormId)) return new CommonResult(500,"orderFormId 为空");
        Map<String,Object> orderForm = orderService.orderFormInfo1(Integer.parseInt(orderFormId));
        if (orderForm!=null) return new CommonResult(200,"查询成功",orderForm);
        return new CommonResult(201,"未查询到结果",null);
    }

    /**
     * 根据orderAddressId查询orderAddress详情
     * @param request
     * @return
     */
    @GetMapping("/app/order/orderAddressInfo")
    public CommonResult orderAddressInfo(HttpServletRequest request){
        String orderAddressId = request.getParameter("orderAddressId");
        if (StringUtil.isEmpty(orderAddressId)) return new CommonResult(500,"orderAddressId 为空");
        OrderAddress orderAddress = orderService.orderAddressInfo(Integer.parseInt(orderAddressId));
        if (orderAddress!=null) return new CommonResult(200,"查询成功",orderAddress);
        return new CommonResult(201,"未查询到结果",null);
    }

    /**
     * 修改合同订单的默认地址
     * @param request
     * @return
     */
    @PostMapping("/app/order/updateOrderAddress")
    public CommonResult updateOrderAddress(HttpServletRequest request){
        Map<String,String> map = new HashMap<>();
        String orderAddressId = request.getParameter("orderAddressId");
        if (StringUtil.isEmpty(orderAddressId)) return new CommonResult(500,"orderAddressId 为空");
        String companyName = request.getParameter("companyName");
        if (StringUtil.isEmpty(companyName)) return new CommonResult(500,"companyName 为空");
        String userName = request.getParameter("userName");
        if (StringUtil.isEmpty(userName)) return new CommonResult(500,"userName 为空");
        String userPhone = request.getParameter("userPhone");
        if (StringUtil.isEmpty(userPhone)) return new CommonResult(500,"userPhone 为空");
        String userFax = request.getParameter("userFax");
        String orderAddressStatus = request.getParameter("orderAddressStatus");
        if (StringUtil.isEmpty(orderAddressStatus)) return new CommonResult(500,"orderAddressStatus 为空");
        if ("0".equals(orderAddressStatus)) {
            String userAddress = request.getParameter("userAddress");
            if (StringUtil.isEmpty(userAddress)) return new CommonResult(500,"userAddress 为空");
            String userDetailAddress = request.getParameter("userDetailAddress");
            if (StringUtil.isEmpty(userDetailAddress)) return new CommonResult(500,"userDetailAddress 为空");
            map.put("userAddress",userAddress);
            map.put("userDetailAddress",userDetailAddress);
        }
        map.put("orderAddressId",orderAddressId);map.put("companyName",companyName);map.put("userName",userName);
        map.put("userPhone",userPhone);map.put("userFax",userFax);map.put("orderAddressStatus",orderAddressStatus);
        Integer i = orderService.updateOrderAddress(map);
        if (i>0) return new CommonResult(200,"修改成功");
        return new CommonResult(201,"修改失败");
    }

    /**
     * 给合同订单绑定返现红包
     * @param request
     * @return
     */
    @PostMapping("/app/order/bindCoupon")
    public CommonResult bindCoupon(HttpServletRequest request){
        String orderFormId = request.getParameter("orderFormId");
        if (StringUtil.isEmpty(orderFormId)) return new CommonResult(500,"orderFormId 为空");
        String couponId = request.getParameter("couponId");
        if (StringUtil.isEmpty(couponId)) return new CommonResult(500,"couponId 为空");
        Integer i = orderService.bindCoupon(Integer.parseInt(orderFormId),Long.valueOf(couponId));
        if (i==0) return new CommonResult(200,"绑定成功");
        else if (i==1) return new CommonResult(201,"该订单不存在");
        else if (i==2) return new CommonResult(201,"当前状态无法绑定返现红包");
        else if (i==3) return new CommonResult(201,"当前红包不存在");
        else return new CommonResult(201,"绑定成功");
    }

    /**
     * 后台
     * 根据状态、订单号
     * 分页查询订单合同
     * @param request
     * @return
     */
    @GetMapping("/order/orderFormPageList")
    public CommonResult orderFormPageListPc(HttpServletRequest request){
        String pageNum = request.getParameter("pageNum");
        if (StringUtil.isEmpty(pageNum)) pageNum = "1";
        String orderStatus = request.getParameter("orderStatus");
        if (StringUtil.isEmpty(orderStatus)) return new CommonResult(500,"orderStatus 为空");
        String orderNumber = request.getParameter("orderNumber");
        Map<String,String> map = new HashMap<>();
        map.put("orderStatus",orderStatus);map.put("orderNumber",orderNumber);
        PageInfo pageInfo = orderService.orderFormPageListPc(Integer.parseInt(pageNum),map);
        if (pageInfo.getList().size()>0) return new CommonResult(200,"查询成功",pageInfo);
        return new CommonResult(201,"未查询到结果",null);
    }

    /**
     * 后台
     * 根据合同订单Id查询合同订单下所有型号
     * @param request
     * @return
     */
    @GetMapping("/order/orderModelList1")
    public CommonResult orderModelList1(HttpServletRequest request){
        String orderId = request.getParameter("orderId");
        if (StringUtil.isEmpty(orderId)) return new CommonResult(500,"orderId 为空");
        List<OrderModel> orderModelList = orderService.orderModelList(Integer.parseInt(orderId));
        if (orderModelList.size()>0) return new CommonResult(200,"查询成功",orderModelList);
        return new CommonResult(201,"未查询到结果",null);
    }

    /**
     * 后台
     * 根据合同地址Id查询合同订单下所有型号
     * @param request
     * @return
     */
    @GetMapping("/order/orderModelList2")
    public CommonResult orderModelList2(HttpServletRequest request){
        String orderId = request.getParameter("orderId");
        if (StringUtil.isEmpty(orderId)) return new CommonResult(500,"orderId 为空");
        List<OrderModel> orderModelList = orderService.orderModelList1(Integer.parseInt(orderId),2);
        if (orderModelList.size()>0) return new CommonResult(200,"查询成功",orderModelList);
        return new CommonResult(201,"未查询到结果",null);
    }

    /**
     * 后台
     * 根据orderId（订单合同id）和合同是否默认状态查询订单地址列表
     * @param request
     * @return
     */
    @GetMapping("/order/orderAddressList")
    public CommonResult orderAddressList(HttpServletRequest request){
        String orderId = request.getParameter("orderId");
        if (StringUtil.isEmpty(orderId)) return new CommonResult(500,"orderId 为空");
        String orderAddressType = request.getParameter("orderAddressType");
        if (StringUtil.isEmpty(orderAddressType)) return new CommonResult(500,"orderAddressType 为空");
        List<OrderAddress> orderAddressList = orderService.orderAddressList(Integer.parseInt(orderId),Integer.parseInt(orderAddressType));
        if (orderAddressList.size()>0) return new CommonResult(200,"查询成功",orderAddressList);
        return new CommonResult(201,"未查询到结果",null);
    }

    /**
     * 后台
     * 通过订单id
     * 修改合同订单状态
     * @param request
     * @return
     */
    @PostMapping("/order/updateOrderFormStatus")
    public CommonResult updateOrderFormStatus(HttpServletRequest request){
        String orderFormId = request.getParameter("orderFormId");
        if (StringUtil.isEmpty(orderFormId)) return new CommonResult(500,"orderFormId 为空");
        String orderStatus = request.getParameter("orderStatus");
        if (StringUtil.isEmpty(orderStatus)) return new CommonResult(500,"orderStatus 为空");
        Map<String,String> map = new HashMap();
        map.put("orderFormId",orderFormId);
        map.put("orderStatus",orderStatus);
        Integer i = orderService.updateOrderForm(map);
        if (i>0) return new CommonResult(200,"修改成功");
        return new CommonResult(201,"修改失败");
    }

    /**
     * 后台
     * 根据orderAddressShopStatus查询OrderAddressList
     * 分页
     * @param request
     * @return
     */
    @GetMapping("/order/selectOrderAddressList")
    public CommonResult selectOrderAddressList(HttpServletRequest request){
        String pageNum = request.getParameter("pageNum");
        if (StringUtil.isEmpty(pageNum)) pageNum = "1";
        String orderAddressShopStatus = request.getParameter("orderAddressShopStatus");
        if (StringUtil.isEmpty(orderAddressShopStatus)) return new CommonResult(500,"orderAddressShopStatus 为空");
        String companNyame = request.getParameter("companNyame");
        String userName = request.getParameter("userName");
        String userPhone = request.getParameter("userPhone");
        String userAddress = request.getParameter("userAddress");
        String orderAddressStatus = request.getParameter("orderAddressStatus");
        Map<String,String> map = new HashMap<>();
        map.put("orderAddressShopStatus",orderAddressShopStatus);map.put("companNyame","%"+companNyame+"%");
        map.put("userName","%"+userName+"%");map.put("userPhone","%"+userPhone+"%");
        map.put("userAddress","%"+userAddress+"%");map.put("orderAddressStatus",orderAddressStatus);
        PageInfo pageInfo = orderService.selectOrderAddressList(Integer.parseInt(pageNum),map);
        if (pageInfo.getList().size()>0) return new CommonResult(200,"查询成功",pageInfo);
        return new CommonResult(201,"未查询到结果",null);
    }

    /**
     * 后台
     * 修改订单地址状态
     * @param request
     * @return
     */
    @PostMapping("/order/updateOrderAddressShopStatus")
    public CommonResult updateOrderAddressShopStatus(HttpServletRequest request){
        String orderAddressId = request.getParameter("orderAddressId");
        if (StringUtil.isEmpty(orderAddressId)) return new CommonResult(500,"orderAddressId 为空");
        String orderAddressShopStatus = request.getParameter("orderAddressShopStatus");
        if (StringUtil.isEmpty(orderAddressShopStatus)) return new CommonResult(500,"orderAddressShopStatus 为空");
        String orderAddressLogisticsNumber = request.getParameter("orderAddressLogisticsNumber");
        OrderAddress orderAddress = new OrderAddress();
        orderAddress.setId(Long.valueOf(orderAddressId));
        orderAddress.setOrderAddressShopStatus(Integer.parseInt(orderAddressShopStatus));
        if (StringUtil.isEmpty(orderAddressLogisticsNumber)) orderAddress.setOrderAddressLogisticsNumber(orderAddressLogisticsNumber);
        Integer i = orderService.updateOrderAddress(orderAddress);
        if (i>0) return new CommonResult(200,"修改成功");
        return new CommonResult(201,"修改失败");
    }

    /**
     * 后台
     * 修改订单地址
     * @param request
     * @return
     */
    @PostMapping("/order/updateOrderAddressPc")
    public CommonResult updateOrderAddressPc(HttpServletRequest request){
        Map<String,String> map = new HashMap<>();
        String orderAddressId = request.getParameter("orderAddressId");
        if (StringUtil.isEmpty(orderAddressId)) return new CommonResult(500,"orderAddressId 为空");
        String companyName = request.getParameter("companyName");
        if (StringUtil.isEmpty(companyName)) return new CommonResult(500,"companyName 为空");
        String userName = request.getParameter("userName");
        if (StringUtil.isEmpty(userName)) return new CommonResult(500,"userName 为空");
        String userPhone = request.getParameter("userPhone");
        if (StringUtil.isEmpty(userPhone)) return new CommonResult(500,"userPhone 为空");
        String userFax = request.getParameter("userFax");
        String orderAddressStatus = request.getParameter("orderAddressStatus");
        if (StringUtil.isEmpty(orderAddressStatus)) return new CommonResult(500,"orderAddressStatus 为空");
        if ("0".equals(orderAddressStatus)) {
            String userAddress = request.getParameter("userAddress");
            if (StringUtil.isEmpty(userAddress)) return new CommonResult(500,"userAddress 为空");
            String userDetailAddress = request.getParameter("userDetailAddress");
            if (StringUtil.isEmpty(userDetailAddress)) return new CommonResult(500,"userDetailAddress 为空");
            map.put("userAddress",userAddress);
            map.put("userDetailAddress",userDetailAddress);
        }
        map.put("orderAddressId",orderAddressId);map.put("companyName",companyName);map.put("userName",userName);
        map.put("userPhone",userPhone);map.put("userFax",userFax);map.put("orderAddressStatus",orderAddressStatus);
        Integer i = orderService.updateOrderAddressPc(map);
        if (i==501) return new CommonResult(500,"用户余额不足以运费差价的扣除");
        else if (i>0) return new CommonResult(200,"修改成功");
        return new CommonResult(201,"修改失败");
    }

}
