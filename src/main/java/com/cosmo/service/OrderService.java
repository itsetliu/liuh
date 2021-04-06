package com.cosmo.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cosmo.dao.*;
import com.cosmo.entity.*;
import com.cosmo.excel.OrderExcelBO;
import com.cosmo.pdf.PDFExportConfig;
import com.cosmo.pdf.PDFUtil;
import com.cosmo.util.FileUtil;
import com.cosmo.util.PageInfo;
import com.cosmo.util.RedisUtil;
import com.cosmo.util.StringUtil;
import org.springframework.beans.BeanUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class OrderService {

    @Resource
    private WxPayService wxPayService;

    @Resource
    private UserInfoMapper userInfoMapper;
    @Resource
    private UserLockMapper userLockMapper;
    @Resource
    private OrderFormMapper orderFormMapper;
    @Resource
    private OrderAddressMapper orderAddressMapper;
    @Resource
    private OrderModelMapper orderModelMapper;
    @Resource
    private OrderParentMapper orderParentMapper;
    @Resource
    private CouponMapper couponMapper;
    @Resource
    private ConfigMapper configMapper;
    @Resource
    private UserPriceInfoMapper userPriceInfoMapper;
    @Resource
    private UserMemberPriceInfoMapper userMemberPriceInfoMapper;
    @Resource
    private UserMemberModelMapper userMemberModelMapper;
    @Resource
    private UserRemindMapper userRemindMapper;
    @Resource
    private UserPurchaserMapper userPurchaserMapper;
    @Resource
    private HatProvinceMapper hatProvinceMapper;
    @Resource
    private HatCityMapper hatCityMapper;
    @Resource
    private FreightMapper freightMapper;
    @Resource
    private RedisUtil redisUtil;
    @Resource
    private PDFExportConfig pdfExportConfig;

    /*boxWeigth：纸箱重，trayWeigth：托盘重，orderTime：订单失效时间，trayWeigth：购物车失效时间
    BigDecimal boxWeigth = new BigDecimal(this.getConfigValue("boxWeigth"));
    BigDecimal trayWeigth = new BigDecimal(this.getConfigValue("trayWeigth"));
    Integer orderTime = Integer.parseInt(this.getConfigValue("orderTime"));
    Integer shopTrolleyTime = Integer.parseInt(this.getConfigValue("shopTrolleyTime"));
    */

    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd");

    /**
     * 根据订单id查询excel数据
     * @param orderId
     * @return
     */
    public List<OrderExcelBO> orderExcel(String orderId){
        Config config = null;
        List<Config> configs = configMapper.selectList(new QueryWrapper<Config>().eq("code", "trayWeigth").eq("type", 1));
        if (configs.size()>0){
            config = configs.get(0);
        }
        OrderForm orderForm = orderFormMapper.selectById(orderId);
        List<OrderModel> orderModelList = orderModelMapper.selectList(
                new QueryWrapper<OrderModel>().eq("order_id", orderId).eq("order_model_status", 1));
        final int[] i = {1};
        Config finalConfig = config;
        List<OrderExcelBO> collect = new ArrayList<>();
        for (int l = 0; l<orderModelList.size(); l++){
            OrderModel orderModel = orderModelList.get(l);
            OrderExcelBO orderExcelBO = new OrderExcelBO();
            orderExcelBO.setOrderIndex(String.valueOf(i[0]));
            orderExcelBO.setTime(sdf1.format(orderForm.getOrderTimeDelivery()));
            orderExcelBO.setDirection("");
            OrderAddress address = null;
            List<OrderAddress> orderAddresses = orderAddressMapper.selectList(
                    new QueryWrapper<OrderAddress>().eq("order_id", orderId).eq("order_address_type", 0));
            if (orderAddresses.size()>0){
                address = orderAddresses.get(0);
            }
            orderExcelBO.setUserNumber(address.getCompanyName());
            orderExcelBO.setOrderNumber(orderForm.getOrderNumber().substring(5));
            orderExcelBO.setPermission("同意");
            orderExcelBO.setModelIndex(String.valueOf(i[0]));
            orderExcelBO.setModelName(orderModel.getModelName());
            orderExcelBO.setThickness(orderModel.getSpecThickness());
            orderExcelBO.setWidth(orderModel.getSpecWidth());
            orderExcelBO.setLength(orderModel.getSpecLength());
            orderExcelBO.setSuttle(orderModel.getSpecSuttle());
            orderExcelBO.setRoughWeight(orderModel.getRollRoughWeight());
            orderExcelBO.setTotalSuttle(orderModel.getModelTotalSuttle());
            orderExcelBO.setPipeWeight(orderModel.getPipeWeight());
            orderExcelBO.setPipeDia(orderModel.getPipeDia());
            orderExcelBO.setCartonPipeNumber(String.valueOf(orderModel.getCartonPipeNumber()));
            String cartonType = "中性";
            if (orderModel.getCartonType()==0) cartonType = "中性";
            else if (orderModel.getCartonType()==1) cartonType = "定制";
            else if (orderModel.getCartonType()==2) cartonType = "无纸箱";
            orderExcelBO.setCartonType(cartonType);
//            orderExcelBO.setCartonWeight(orderModel.getCartonWeight());
            orderExcelBO.setCartonWeight("0.5");
            String labelType = "中性";
            if (orderModel.getLabelType()==0) labelType = "中性";
            else if (orderModel.getLabelType()==1) labelType = "定制";
            else if (orderModel.getLabelType()==2) labelType = "无标签";
            orderExcelBO.setLabelType(labelType);
            orderExcelBO.setTrayNumber(String.valueOf(orderModel.getTrayNumber()));
            orderExcelBO.setTrayWeight(finalConfig.getValue());
            orderExcelBO.setTrayModel(orderModel.getTrayModel());
//            orderExcelBO.setTrayCapacity(String.valueOf(orderModel.getTrayCapacity()));
            orderExcelBO.setTrayCapacity("标准");
            orderExcelBO.setRollNumber(String.valueOf(orderModel.getRollNumber()));
            orderExcelBO.setTotalRoughWeight(orderModel.getModelTotalRoughWeight());
            orderExcelBO.setFreightPrice("");
            orderExcelBO.setFreight("");
            orderExcelBO.setUnitPrice("");
            orderExcelBO.setTotalPrice("");
            orderExcelBO.setRemark("");
            orderExcelBO.setStatus("未完成");
            orderExcelBO.setStatus1("");
            orderExcelBO.setModelNumber("");
            orderExcelBO.setModelWeight("");
            orderExcelBO.setRecord("");
            orderExcelBO.setPayMethod("");
            orderExcelBO.setAccountingDate("");
            orderExcelBO.setAudit("");
            orderExcelBO.setInvoice("");
            orderExcelBO.setInvoiceFMSNumber("");
            collect.add(orderExcelBO);
            collect.add(new OrderExcelBO());
        }
        /*List<OrderExcelBO> collect = orderModelList.stream().map(orderModel -> {
            OrderExcelBO orderExcelBO = new OrderExcelBO();
            orderExcelBO.setOrderIndex(String.valueOf(i[0]));
            orderExcelBO.setTime(sdf1.format(orderForm.getOrderTimeDelivery()));
            orderExcelBO.setDirection("");
            OrderAddress address = null;
            List<OrderAddress> orderAddresses = orderAddressMapper.selectList(
                    new QueryWrapper<OrderAddress>().eq("order_id", orderId).eq("order_address_type", 0));
            if (orderAddresses.size()>0){
                address = orderAddresses.get(0);
            }
            orderExcelBO.setUserNumber(address.getCompanyName());
            orderExcelBO.setOrderNumber(orderForm.getOrderNumber().substring(5));
            orderExcelBO.setPermission("同意");
            orderExcelBO.setModelIndex(String.valueOf(i[0]));
            orderExcelBO.setModelName(orderModel.getModelName());
            orderExcelBO.setThickness(orderModel.getSpecThickness());
            orderExcelBO.setWidth(orderModel.getSpecWidth());
            orderExcelBO.setLength(orderModel.getSpecLength());
            orderExcelBO.setSuttle(orderModel.getSpecSuttle());
            orderExcelBO.setRoughWeight(orderModel.getRollRoughWeight());
            orderExcelBO.setTotalSuttle(orderModel.getModelTotalSuttle());
            orderExcelBO.setPipeWeight(orderModel.getPipeWeight());
            orderExcelBO.setPipeDia(orderModel.getPipeDia());
            orderExcelBO.setCartonPipeNumber(String.valueOf(orderModel.getCartonPipeNumber()));
            String cartonType = "中性";
            if (orderModel.getCartonType()==0) cartonType = "中性";
            else if (orderModel.getCartonType()==1) cartonType = "定制";
            else if (orderModel.getCartonType()==2) cartonType = "无纸箱";
            orderExcelBO.setCartonType(cartonType);
//            orderExcelBO.setCartonWeight(orderModel.getCartonWeight());
            orderExcelBO.setCartonWeight("0.5");
            String labelType = "中性";
            if (orderModel.getCartonType()==0) labelType = "中性";
            else if (orderModel.getCartonType()==1) labelType = "定制";
            orderExcelBO.setLabelType(labelType);
            orderExcelBO.setTrayNumber(String.valueOf(orderModel.getTrayNumber()));
            orderExcelBO.setTrayWeight(finalConfig.getValue());
            orderExcelBO.setTrayModel(orderModel.getTrayModel());
//            orderExcelBO.setTrayCapacity(String.valueOf(orderModel.getTrayCapacity()));
            orderExcelBO.setTrayCapacity("标准");
            orderExcelBO.setRollNumber(String.valueOf(orderModel.getRollNumber()));
            orderExcelBO.setTotalRoughWeight(orderModel.getModelTotalRoughWeight());
            orderExcelBO.setFreightPrice("");
            orderExcelBO.setFreight("");
            orderExcelBO.setUnitPrice("");
            orderExcelBO.setTotalPrice("");
            orderExcelBO.setRemark("");
            orderExcelBO.setStatus("未完成");
            orderExcelBO.setStatus1("");
            orderExcelBO.setModelNumber("");
            orderExcelBO.setModelWeight("");
            orderExcelBO.setRecord("");
            orderExcelBO.setPayMethod("");
            orderExcelBO.setAccountingDate("");
            orderExcelBO.setAudit("");
            orderExcelBO.setInvoice("");
            orderExcelBO.setInvoiceFMSNumber("");
            i[0]++;
            return orderExcelBO;
        }).collect(Collectors.toList());*/
        return collect;
    }

    /**
     * PDF 文件导出
     *
     * @return
     */
    public ResponseEntity<?> export(String orderId) {
        OrderForm orderForm = this.orderFormMapper.selectById(orderId);
        QueryWrapper<OrderModel> orderModelQueryWrapper = new QueryWrapper<>();
        orderModelQueryWrapper.eq("order_id",orderId).eq("order_model_status",1);
        QueryWrapper<OrderAddress> orderAddressQueryWrapper = new QueryWrapper<>();
        orderAddressQueryWrapper.eq("order_id",orderId).eq("order_address_type",0);
        OrderAddress orderAddress = this.orderAddressMapper.selectList(orderAddressQueryWrapper).get(0);
        List<OrderModel> orderModels = this.orderModelMapper.selectList(orderModelQueryWrapper);
        if (orderAddress.getOrderAddressStatus()==0){
            List<OrderModel> collect = orderModels.stream().map(orderModel -> {
                orderModel.setOrderModelFreight(new BigDecimal(orderModel.getModelTotalRoughWeight()).multiply(orderAddress.getOrderAddressFreightPrice()));
                return orderModel;
            }).collect(Collectors.toList());
            orderForm.setOrderModelList(collect);
        }else {
            orderForm.setOrderModelList(orderModels);
        }
        HttpHeaders headers = new HttpHeaders();
        SimpleDateFormat ft = new SimpleDateFormat("yyyy年MM月dd日");
        String coupon = "";
        if (orderForm.getOrderCouponId()!=null){
            Coupon coupon1 = couponMapper.selectById(orderForm.getOrderCouponId());
            orderForm.setOrderCouponInfo(coupon1);
            coupon = "已使用"+(coupon1.getType()==0?"满减卷:":"返现卷:")
                    +coupon1.getName()
                    +(coupon1.getType()==0?",优惠后价格为:"+orderForm.getOrderPrice().subtract(coupon1.getSubtract()):"");
        }else {
            coupon = "未使用优惠卷";
        }
        /**
         * 数据导出(PDF 格式)
         */
        Map<String, Object> dataMap = new HashMap<>(16);
        dataMap.put("couponId",orderForm.getOrderCouponId());
        dataMap.put("coupon",coupon);
        dataMap.put("nowTime",ft.format(orderForm.getOrderTimeCreate()));
        dataMap.put("orderTimeDelivery",ft.format(orderForm.getOrderTimeDelivery()));
        dataMap.put("orderDemander",orderForm.getOrderDemander());
        dataMap.put("company",orderAddress.getCompanyName());
        dataMap.put("purchaserCompany",orderAddress.getCompanyName());
        dataMap.put("orderNumber",orderForm.getOrderNumber());
        if (orderAddress.getOrderAddressStatus()==0){
            dataMap.put("userAddress",orderAddress.getUserAddress()+orderAddress.getUserDetailAddress());
        }else if (orderAddress.getOrderAddressStatus()==1){
            dataMap.put("userAddress","自提");
        }else if (orderAddress.getOrderAddressStatus()==2){
            dataMap.put("userAddress","待定");
        }
        dataMap.put("freightPrice",orderAddress.getOrderAddressFreightPrice());
        dataMap.put("freight",orderAddress.getOrderAddressFreight());
        dataMap.put("userName",orderAddress.getUserName());
        dataMap.put("userPhone",orderAddress.getUserPhone());
        dataMap.put("fax",orderAddress.getUserFax());
//        dataMap.put("orderPrice",orderAddress.getOrderAddressPrice().add(orderAddress.getOrderAddressFreight()).toString());
        dataMap.put("orderPrice",orderForm.getOrderPrice());
        dataMap.put("orderModelList",orderForm.getOrderModelList());
        //TODO 本地  服务器
//        String htmlStr = PDFUtil.freemarkerRender(dataMap, pdfExportConfig.getEmployeeKpiFtl());//本地
        String htmlStr = PDFUtil.freemarkerRender1(dataMap, pdfExportConfig.getEmployeeKpiFtlName(), pdfExportConfig.getEmployeeKpiFtlUrl());//服务器
//        byte[] pdfBytes = PDFUtil.createPDF(htmlStr, pdfExportConfig.getFontSimsun());
        byte[] pdfBytes = PDFUtil.createPDF1(htmlStr, pdfExportConfig.getFontSimsunUrl());
        if (pdfBytes != null && pdfBytes.length > 0) {
            String fileName = System.currentTimeMillis() + (int) (Math.random() * 90000 + 10000) + ".pdf";
            headers.setContentDispositionFormData("attachment", fileName);
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            return new ResponseEntity<byte[]>(pdfBytes, headers, HttpStatus.OK);
        }
//        headers.setContentType(MediaType.APPLICATION_JSON_UTF8);
        headers.setContentType(MediaType.APPLICATION_JSON);
        return new ResponseEntity<String>("{ \"code\" : \"404\", \"message\" : \"not found\" }",
                headers, HttpStatus.NOT_FOUND);
    }


    /**
     * 通过code获取value
     * 这里指获取纸箱托盘重量使用
     * @param code
     * @return
     */
    private String getConfigValue(String code){
        QueryWrapper<Config> configQueryWrapper = new QueryWrapper<>();
        configQueryWrapper.eq("code",code);
        return configMapper.selectList(configQueryWrapper).get(0).getValue();
    }

    /**
     * date天数加number
     * @param date
     * @param number
     * @return
     */
    public static Date getNextsDay(Date date,Integer number) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.DAY_OF_MONTH, +number);//传入的时间加number天
        date = calendar.getTime();
        return date;
    }

    /**
     * 修改型号规格后确定
     * @param map
     * @return
     */
    /*public Integer updateOrderFormPrice(Map<String, String> map){
        OrderForm orderForm = orderFormMapper.selectById(map.get("orderId"));
        UserInfo userInfo = userInfoMapper.selectById(orderForm.getUserId());
//        if ()
        return 1;
    }
*/

    /**
     * 根据订单id 修改订单已读状态
     * @param orderId
     * @return
     */
    public Integer lookOrder(String orderId,Integer ifLook){
        QueryWrapper<OrderForm> orderFormQueryWrapper = new QueryWrapper<>();
        orderFormQueryWrapper.eq("id",orderId);
        OrderForm orderForm = new OrderForm();
        orderForm.setIfLook(ifLook);
        return orderFormMapper.update(orderForm,orderFormQueryWrapper);
    }

    /**
     * 后台修改订单型号规格
     * @param map
     * @return
     */
    @Transactional(value="txManager1")
    public Integer updateOrderModel(Map<String,String> map){
        // 前台数据
        OrderModel orderModelNew = JSON.parseObject(map.get("updateOrderModerForm"), OrderModel.class);
        orderModelNew.setAgoModelTotalPrice(new BigDecimal(map.get("agoModelTotalPrice")));
        // 前台型号原始数据
        OrderModel orderModelAgo = orderModelMapper.selectById(orderModelNew.getId());
        // 合同
        OrderForm orderForm = orderFormMapper.selectById(orderModelNew.getOrderId());
        // 默认地址
        List<OrderAddress> orderAddresses = orderAddressMapper.selectList(new QueryWrapper<OrderAddress>().eq("order_id", orderForm.getId()).eq("order_address_type", 0));
        OrderAddress orderAddress = orderAddresses.get(0);
        // 默认地址原始数据
        BigDecimal orderAddressRoughWeightAgo = new BigDecimal(orderAddress.getOrderAddressRoughWeight());
        BigDecimal orderAddressPriceAgo = orderAddress.getOrderAddressPrice();
        BigDecimal orderAddressFreightAgo = orderAddress.getOrderAddressFreight();

        // 修改地址数据
//        orderAddress.setAgoOrderAddressFreight(orderAddress.getOrderAddressFreight());
//        orderAddress.setAgoOrderAddressPrice(orderAddress.getOrderAddressPrice());
        BigDecimal orderAddressRoughWeight = new BigDecimal(orderAddress.getOrderAddressRoughWeight())
                .subtract(new BigDecimal(orderModelAgo.getModelTotalRoughWeight())).add(new BigDecimal(orderModelNew.getModelTotalRoughWeight()));
        orderAddress.setOrderAddressRoughWeight(orderAddressRoughWeight.toString());
        orderAddress.setOrderAddressPrice(orderAddress.getOrderAddressPrice()
                .subtract(orderModelAgo.getModelTotalPrice())
                .add(orderModelNew.getModelTotalPrice()));
        orderAddress.setOrderAddressFreight(orderAddress.getOrderAddressFreightPrice().multiply(orderAddressRoughWeight));
        // 修改合同
//        orderForm.setAgoOrderFreight(orderForm.getOrderFreight());
//        orderForm.setAgoProductPrice(orderForm.getProductPrice());
//        orderForm.setAgoOrderPrice(orderForm.getOrderPrice());
        orderForm.setOrderFreight(orderForm.getOrderFreight().subtract(orderAddressFreightAgo).add(orderAddress.getOrderAddressFreight()));
        orderForm.setProductPrice(orderForm.getProductPrice().subtract(orderAddressPriceAgo).add(orderAddress.getOrderAddressPrice()));
        orderForm.setOrderPrice(orderForm.getProductPrice().add(orderForm.getOrderFreight()));
        BigDecimal orderWeight = new BigDecimal(orderForm.getOrderWeight()).subtract(orderAddressRoughWeightAgo).add(new BigDecimal(orderAddress.getOrderAddressRoughWeight()));
        orderForm.setOrderWeight(orderWeight.toString());

        QueryWrapper<OrderModel> orderModelQueryWrapper = new QueryWrapper<>();
        orderModelQueryWrapper.eq("order_id", orderAddress.getId()).eq("order_model_status", 2);
        orderModelQueryWrapper.eq("model_type",orderModelAgo.getModelType());
        orderModelQueryWrapper.eq("model_name",orderModelAgo.getModelName());
        orderModelQueryWrapper.eq("spec_thickness",orderModelAgo.getSpecThickness());
        orderModelQueryWrapper.eq("pipe_weight",orderModelAgo.getPipeWeight());
        orderModelQueryWrapper.eq("roll_number",orderModelAgo.getRollNumber());
        orderModelQueryWrapper.eq("model_total_price",orderModelAgo.getModelTotalPrice());
        List<OrderModel> orderModels = orderModelMapper.selectList(orderModelQueryWrapper);
        OrderModel orderModelAddress = orderModels.get(0);
        orderModelAddress.setModelType(orderModelNew.getModelType());
        orderModelAddress.setModelName(orderModelNew.getModelName());
        orderModelAddress.setSpecLength(orderModelNew.getSpecLength());
        orderModelAddress.setSpecSuttle(orderModelNew.getSpecSuttle());
        orderModelAddress.setSpecThickness(orderModelNew.getSpecThickness());
        orderModelAddress.setSpecWidth(orderModelNew.getSpecWidth());
        orderModelAddress.setPipeDia(orderModelNew.getPipeDia());
        orderModelAddress.setPipeWeight(orderModelNew.getPipeWeight());
        orderModelAddress.setCartonWeight(orderModelNew.getCartonWeight());
        orderModelAddress.setCartonType(orderModelNew.getCartonType());
        orderModelAddress.setCartonPipeNumber(orderModelNew.getCartonPipeNumber());
        orderModelAddress.setCartonNumber(orderModelNew.getCartonNumber());
        orderModelAddress.setCartonPrice(orderModelNew.getCartonPrice());
        orderModelAddress.setLabelType(orderModelNew.getLabelType());
        orderModelAddress.setTrayType(orderModelNew.getTrayType());
        orderModelAddress.setTrayNumber(orderModelNew.getTrayNumber());
        orderModelAddress.setTrayModel(orderModelNew.getTrayModel());
        orderModelAddress.setTrayCapacity(orderModelNew.getTrayCapacity());
        orderModelAddress.setRollNumber(orderModelNew.getRollNumber());
        orderModelAddress.setRollRoughWeight(orderModelNew.getRollRoughWeight());
        orderModelAddress.setModelTotalRoughWeight(orderModelNew.getModelTotalRoughWeight());
        orderModelAddress.setModelTotalSuttle(orderModelNew.getModelTotalSuttle());
        orderModelAddress.setModelUnitPrice(orderModelNew.getModelUnitPrice());
        orderModelAddress.setModelTotalPrice(orderModelNew.getModelTotalPrice());
        orderModelAddress.setModelProcessCost(orderModelNew.getModelProcessCost());
        orderModelAddress.setModelRawPrice(orderModelNew.getModelRawPrice());
        orderModelAddress.setModelRawPriceType(orderModelNew.getModelRawPriceType());
        orderModelAddress.setUserLockId(orderModelNew.getUserLockId());
        orderModelAddress.setMemberId(orderModelNew.getMemberId());
        orderModelAddress.setMemberDiscount(orderModelNew.getMemberDiscount());
        orderModelAddress.setAgoModelTotalPrice(orderModelNew.getAgoModelTotalPrice());

        // orderModelNew orderForm orderAddress orderModelAddress
        int i = this.orderModelMapper.updateById(orderModelAddress);
        int i1 = this.orderModelMapper.updateById(orderModelNew);
        int i2 = this.orderAddressMapper.updateById(orderAddress);
        return this.orderFormMapper.updateById(orderForm);
    }

    /**
     * 新增商品到购物车
     * @param map
     * @return
     */
    public String addOrderModel(Map<String,String> map){
        BigDecimal boxWeigth = new BigDecimal(this.getConfigValue("boxWeigth"));
        BigDecimal trayWeigth = new BigDecimal(this.getConfigValue("trayWeigth"));
        OrderModel orderModel = new OrderModel();
        orderModel.setUserId(map.get("userId"));
        orderModel.setModelType(Integer.parseInt(map.get("modelType")));
        orderModel.setModelName(map.get("modelName"));
        orderModel.setSpecWidth(map.get("specWidth"));
        orderModel.setSpecThickness(map.get("specThickness"));
        orderModel.setSpecLength(map.get("specLength"));
        orderModel.setSpecSuttle(map.get("specSuttle"));
        orderModel.setPipeWeight(map.get("pipeWeight"));
        orderModel.setPipeDia(map.get("pipeDia"));
        orderModel.setCartonWeight(map.get("cartonWeight"));
        orderModel.setCartonType(Integer.parseInt(map.get("cartonType")));
        if (Integer.parseInt(map.get("cartonType"))==1){
            List<String> cartonInfoList = JSON.parseArray(map.get("cartonInfo"),String.class);
            List<String> cartonInfos = new ArrayList<>();
            for (String cartonInfo : cartonInfoList) {
                cartonInfos.add(FileUtil.upload(FileUtil.base64ToMultipart(cartonInfo)));
            }
            orderModel.setCartonInfo(JSON.toJSONString(cartonInfos));
        }
        orderModel.setCartonPipeNumber(Integer.parseInt(map.get("cartonPipeNumber")));
        orderModel.setCartonNumber(Integer.parseInt(map.get("cartonNumber")));
        orderModel.setCartonPrice(new BigDecimal(map.get("cartonPrice")));
        orderModel.setLabelType(Integer.parseInt(map.get("labelType")));
        if (Integer.parseInt(map.get("labelType"))==1)orderModel.setLabelInfo(FileUtil.upload(FileUtil.base64ToMultipart(map.get("labelInfo"))));
        orderModel.setTrayType(Integer.parseInt(map.get("trayType")));
        orderModel.setTrayNumber(Integer.parseInt(map.get("trayNumber")));
        orderModel.setTrayModel("标准");
        orderModel.setTrayCapacity(Integer.parseInt(map.get("trayCapacity")));
        orderModel.setRollNumber(Integer.parseInt(map.get("rollNumber")));
        orderModel.setRollRoughWeight(map.get("rollRoughWeight"));
        BigDecimal modelTotalRoughWeight = new BigDecimal(map.get("rollNumber")).multiply(new BigDecimal(map.get("rollRoughWeight")))
                .add(new BigDecimal(map.get("cartonWeight")).multiply(new BigDecimal(map.get("cartonNumber"))))
                .add(trayWeigth.multiply(new BigDecimal(map.get("trayNumber"))));
        orderModel.setModelTotalRoughWeight(modelTotalRoughWeight.toString());
        BigDecimal modelTotalSuttle = new BigDecimal(map.get("rollNumber")).multiply(new BigDecimal(map.get("specSuttle")));
        orderModel.setModelTotalSuttle(modelTotalSuttle.toString());
        orderModel.setModelUnitPrice(new BigDecimal(map.get("modelUnitPrice")));
        orderModel.setModelTotalPrice(new BigDecimal(map.get("modelTotalPrice")));
        orderModel.setModelProcessCost(new BigDecimal(map.get("modelProcessCost")));
        orderModel.setModelRawPrice(new BigDecimal(map.get("modelRawPrice")));
        orderModel.setModelRawPriceType(Integer.parseInt(map.get("modelRawPriceType")));
        orderModel.setMemberId(map.get("memberId"));
        orderModel.setMemberDiscount(new BigDecimal(map.get("memberDiscount")));
        if ("2".equals(map.get("modelRawPriceType"))) orderModel.setUserLockId(map.get("userLockId"));
        orderModel.setOrderModelStatus(0);
        Date nowDate = new Date();
        orderModel.setOrderModelExpireTime(sdf.format(nowDate));
//        Integer shopTrolleyTime = Integer.parseInt(this.getConfigValue("shopTrolleyTime"));
//        Integer shopTrolleyTime = 0;
//        nowDate.setTime(nowDate.getTime()+shopTrolleyTime);
//        Date nowSpecificDate = null;
//        try {
//            nowSpecificDate = sdf.parse(sdf1.format(new Date())+" 08:59:59");
//        } catch (ParseException e) {
//            e.printStackTrace();
//        }
//        if ((new Date()).compareTo(nowSpecificDate)==-1){//当前时间小于当天 08:59:59
//            shopTrolleyTime = (int)((nowSpecificDate.getTime() - (new Date()).getTime())/1000);
//        }else {//当前时间大于当天 08:59:59
//            Calendar calendar = Calendar.getInstance();
//            calendar.setTime(nowSpecificDate);
//            calendar.add(Calendar.DAY_OF_MONTH, +1);//传入的时间加1天
//            Date tomorrowSpecificDate = calendar.getTime();
//            shopTrolleyTime = (int)((tomorrowSpecificDate.getTime() - (new Date()).getTime())/1000);
//        }
//        //把购物车编号存到redis中
//        redisUtil.set("shopTrolley,"+orderModel.getId(),"shopTrolley,"+orderModel.getId());
//        redisUtil.expire("shopTrolley,"+orderModel.getId(),shopTrolleyTime, TimeUnit.SECONDS);
        orderModel.setAgoModelTotalPrice(orderModel.getModelTotalPrice());
        Integer i = orderModelMapper.insert(orderModel);
        return orderModel.getId();
    }

    /**
     * 根据orderModelId删除购物车订单
     * @param orderModelId
     * @return
     */
    public Integer delOrderModel(String orderModelId){
        OrderModel orderModel = orderModelMapper.selectById(orderModelId);
        if (orderModel.getLabelType()==1)FileUtil.delFile(orderModel.getLabelInfo());
        if (orderModel.getCartonType()==1)FileUtil.delFile(orderModel.getCartonInfo());
        return orderModelMapper.deleteById(orderModelId);
    }

    /**
     * 根据用户id，orderModelStatus查询购物车
     * @param map
     * @return
     */
    public List<OrderModel> orderModelList(Map<String,String> map){
        QueryWrapper<OrderModel> orderModelQueryWrapper = new QueryWrapper<>();
        orderModelQueryWrapper.eq("user_id",map.get("userId"))
                .eq("model_raw_price_type",map.get("modelRawPriceType"))
                .eq("order_model_status",0);
        return orderModelMapper.selectList(orderModelQueryWrapper);
    }

    /**
     * 截取原料中的现售/预售天数
     * @return
     */
    public String getDay(String dayName){
        dayName = dayName.substring(2);
        int strLength = dayName.length();
        return dayName.substring(0,strLength-1);
    }

    /**
     * 生成合同订单
     * @param map
     * @return
     *  201:会员预存余额不足支付本次交易，请续费会员或调整购物车后生成订单
     *  202:用户没有默认采购方信息
     *  203:该地址暂无运费信息，无法生成订单
     */
    @Transactional(value="txManager1")
    public Map createOrderForm(Map<String,String> map){
        Map rel=new HashMap();
        UserInfo userInfo = userInfoMapper.selectById(map.get("userId"));
        Integer i = 0;
        List<UserPurchaser> userPurchaserList = userPurchaserMapper.selectList(new QueryWrapper<UserPurchaser>().eq("user_id", map.get("userId")).eq("status", 0));
        if(userPurchaserList.size()<=0){
            rel.put("data","202");
            return rel;//用户没有默认采购方信息
        }
        UserPurchaser userPurchaser = userPurchaserList.get(0);
        if (userPurchaser==null){rel.put("data","202"); return rel;}//用户没有默认采购方信息
        //初步新建合同订单获取orderFormId
        OrderForm orderForm = new OrderForm();
        orderForm.setUserId(userInfo.getId());
        orderForm.setOrderDemander(userPurchaser.getCompanyName()+" "+userPurchaser.getUserName()+" "+userPurchaser.getUserPhone());
        SimpleDateFormat sdf2 = new SimpleDateFormat("yyyyMMdd");
        String create = sdf1.format(new Date())+" 00:00:00";
        String end = sdf1.format(this.getNextsDay(new Date(),1))+" 00:00:00";
        Map<String,String> time = new HashMap<>();
        time.put("create",create);
        time.put("end",end);
//        int count = orderFormMapper.count(time)+1;
        List<OrderForm> orderForms = orderFormMapper.orderFormList(time);
        int count = 1;
        if (orderForms.size()>0) count = orderForms.get(0).getTodayNumber()+1;
        String orderNumber = "COSMO"+sdf2.format(new Date())+"A"+String.format("%3d", count).replace(" ", "0");
        orderForm.setTodayNumber(count);
        orderForm.setOrderNumber(orderNumber);
        orderForm.setInvoiceType(0);
        orderForm.setOrderRemark(map.get("orderRemark"));
        orderForm.setOrderStatus(0);
        orderForm.setOrderTimeCreate(new Date());
        orderForm.setCashDepositType(0);
        i = orderFormMapper.insert(orderForm);
        if (i==0){rel.put("data",i); return rel;}
        List<Config> configList = configMapper.selectList(new QueryWrapper<Config>().eq("code", "LLDPEPrice").eq("type", 0));
        Config config = configList.get(0);
        List<Map<String, String>> list = JSON.parseObject(config.getValue(), ArrayList.class);
        String dayName1 = list.get(0).get("name");
        String dayName2 = list.get(1).get("name");
        Integer day = Integer.parseInt(getDay(dayName2));
        if ("0".equals(map.get("rawPriceType"))) day = Integer.parseInt(getDay(dayName1));
        orderForm.setOrderTimeDelivery(this.getNextsDay(new Date(),day));
        //初步新建默认订单地址获取orderAddressId
        Integer orderAddressStatus = Integer.parseInt(map.get("orderAddressStatus"));
        OrderAddress orderAddress = new OrderAddress();
        orderAddress.setOrderId(orderForm.getId());
        orderAddress.setOrderAddressStatus(orderAddressStatus);
        orderAddress.setOrderAddressType(0);
        orderAddress.setOrderAddressShopStatus(0);
        if (orderAddressStatus==0||orderAddressStatus==1){//单地址收货参数 或 自提
            orderAddress.setCompanyName(map.get("companyName"));
            orderAddress.setUserName(map.get("userName"));
            orderAddress.setUserPhone(map.get("userPhone"));
            orderAddress.setUserFax(map.get("userFax"));
            if (orderAddressStatus==0){//单地址收货参数
                orderAddress.setUserAddress(map.get("userAddress"));
                orderAddress.setUserDetailAddress(map.get("userDetailAddress"));
            }
        }//else 地址待定
        i = orderAddressMapper.insert(orderAddress);
        if (i==0) {rel.put("data",i); return rel;}
        List<String> orderModelIds = JSON.parseArray(map.get("orderModelIds"),String.class);
        QueryWrapper<OrderModel> orderModelQueryWrapper = new QueryWrapper<>();
        orderModelQueryWrapper.in("id",orderModelIds);
        List<OrderModel> orderModelList = orderModelMapper.selectList(orderModelQueryWrapper);
        BigDecimal[] orderAddressRoughWeight = {new BigDecimal(0)};
        BigDecimal[] modelTotalPrice = {new BigDecimal(0)};
        int[] trayNumber = {0};
        orderModelList.forEach(orderModels -> {
            if (orderModels.getMemberId()!=userInfo.getMemberId()){
                orderModels.setMemberId(userInfo.getMemberId());
                //判断用户是否有会员
                if ("0".equals(userInfo.getMemberId())){
                    orderModels.setMemberDiscount(new BigDecimal(0));
                }else {
                    Map<String,String> map1 = new HashMap<>();
                    map1.put("memberId",userInfo.getMemberId().toString());map1.put("modelName",orderModels.getModelName());
                    UserMemberModel userMemberModel1 = userMemberModelMapper.selectUserMemberMap(map1);
                    BigDecimal discount = userMemberModel1.getDiscount();
                    orderModels.setMemberDiscount(discount);
                }
                BigDecimal modelTotalPrice1 = orderModels.getModelUnitPrice().subtract(orderModels.getMemberDiscount()).multiply(new BigDecimal(orderModels.getModelTotalSuttle()));
                if (orderModels.getTrayType()==0){
                    modelTotalPrice1 = modelTotalPrice1.add(new BigDecimal(orderModels.getTrayNumber()).multiply(new BigDecimal(this.getConfigValue("trayPrice"))));
                }
                orderModels.setModelTotalPrice(modelTotalPrice1);
                this.orderModelMapper.updateById(orderModels);
            }
            orderAddressRoughWeight[0]=orderAddressRoughWeight[0].add(new BigDecimal(orderModels.getModelTotalRoughWeight()));
            modelTotalPrice[0]=modelTotalPrice[0].add(orderModels.getModelTotalPrice());
            trayNumber[0]=trayNumber[0]+orderModels.getTrayNumber();
        });
        if (orderAddressStatus==0){//单地址收货状态
            //给订单地址新增 运费单价、运费、产品总毛重、产品总金额
            String[] address = orderAddress.getUserAddress().split(" ");
            Map<String,String> cityIdMap = new HashMap<>();
            cityIdMap.put("province",address[0]);
            cityIdMap.put("city",address[1]);
            String cityId = hatCityMapper.cityId(cityIdMap);
            String freights = freightMapper.value(cityId);
            List<String> freightList = JSONObject.parseArray(freights,String.class);
            if (freightList.size()<=0||freightList==null){
                rel.put("data","203");
                return rel;//该地址暂无运费信息，无法生成订单
            }
            String freightPrice = null;
            for (int l=0;l<freightList.size();l++){
                Map<String,Object> valueMap = JSONObject.parseObject(freightList.get(l));
                String[] names = valueMap.get("name").toString().split("-");
                if (orderAddressRoughWeight[0].compareTo(new BigDecimal(names[0]))==1){//大于
                    if (names.length==2){
                        if (orderAddressRoughWeight[0].compareTo(new BigDecimal(names[1]))<0){//小于等于
                            freightPrice=valueMap.get("value").toString();
                            break;
                        }
                    }else {
                        freightPrice=valueMap.get("value").toString();
                        break;
                    }
                }
            }
            orderAddress.setOrderAddressFreightPrice(new BigDecimal(freightPrice));
            orderAddress.setOrderAddressFreight(orderAddress.getOrderAddressFreightPrice().multiply(orderAddressRoughWeight[0]));
            orderAddress.setOrderAddressRoughWeight(orderAddressRoughWeight[0].toString());
            orderAddress.setOrderAddressPrice(modelTotalPrice[0]);
            orderAddress.setOrderAddressTrayNumber(trayNumber[0]);
            //给订单合同新增 运费、产品总金额、订单总金额、产品总毛重、订单托盘总数
            orderForm.setOrderFreight(orderAddress.getOrderAddressFreight());
            orderForm.setProductPrice(orderAddress.getOrderAddressPrice());
            orderForm.setOrderPrice(orderForm.getProductPrice().add(orderForm.getOrderFreight()));
            orderForm.setOrderWeight(orderAddress.getOrderAddressRoughWeight());
            orderForm.setOrderTotalTrayNumber(trayNumber[0]);
        }else if (orderAddressStatus==1||orderAddressStatus==2) {//自提状态 或 地址待定状态
            //给订单地址新增 运费单价、运费、产品总毛重、产品总金额
            orderAddress.setOrderAddressFreightPrice(new BigDecimal(0));
            orderAddress.setOrderAddressFreight(new BigDecimal(0));
            orderAddress.setOrderAddressRoughWeight(orderAddressRoughWeight[0].toString());
            orderAddress.setOrderAddressPrice(modelTotalPrice[0]);
            orderForm.setOrderTotalTrayNumber(trayNumber[0]);
            //给订单合同新增 运费、产品总金额、订单总金额、产品总毛重、订单托盘总数
            orderForm.setOrderFreight(new BigDecimal(0));
            orderForm.setProductPrice(orderAddress.getOrderAddressPrice());
            orderForm.setOrderPrice(orderForm.getProductPrice().add(orderForm.getOrderFreight()));
            orderForm.setOrderWeight(orderAddress.getOrderAddressRoughWeight());
            orderForm.setOrderTotalTrayNumber(trayNumber[0]);
        }
        if (!"0".equals(userInfo.getMemberId())){
            if (userInfo.getMemberPrice().compareTo(orderForm.getOrderPrice())==-1){
                this.orderFormMapper.deleteById(orderForm.getId());
                this.orderAddressMapper.deleteById(orderAddress.getId());
                rel.put("data","201");
                return rel;//会员预存余额不足支付本次交易，请续费会员或调整购物车后生成订单
            }
            userInfo.setMemberPrice(userInfo.getMemberPrice().subtract(orderForm.getOrderPrice()));
            this.userInfoMapper.updateById(userInfo);
            orderForm.setOrderStatus(2);
        }else {
            //把订单编号存到redis中
            Integer orderTime = Integer.parseInt(this.getConfigValue("intentionGoldTime"));
            redisUtil.set("intentionGold,"+orderForm.getOrderNumber(),"intentionGold,"+orderForm.getOrderNumber());
            redisUtil.expire("intentionGold,"+orderForm.getOrderNumber(),orderTime, TimeUnit.SECONDS);
        }
        //把购物车订单转换为总订单子属
        OrderModel orderModel = new OrderModel();
        orderModel.setOrderModelStatus(1);
        orderModel.setOrderId(orderForm.getId());
        i = orderModelMapper.update(orderModel,orderModelQueryWrapper);
        //
        int[] ii = {0};
        orderModelList.forEach(orderModels -> {
            orderModels.setId(null);
            orderModels.setOrderId(null);
            orderModels.setOrderId(orderAddress.getId());
            orderModels.setOrderModelStatus(2);
            ii[0] = ii[0] + orderModelMapper.insert(orderModels);
        });
        orderModelIds.forEach(orderModelId->{
            //删除购物车失效缓存
            redisUtil.delete("shopTrolley,"+orderModelId);
        });
        if (ii[0]<=0){rel.put("data",ii[0]); return rel;}
        if (i==0) {rel.put("data",i); return rel;}
        orderAddress.setAgoOrderAddressPrice(orderAddress.getOrderAddressPrice());
        orderAddress.setAgoOrderAddressFreight(orderAddress.getOrderAddressFreight());
        orderAddress.setAgoOrderAddressFreightPrice(orderAddress.getOrderAddressFreightPrice());
        i = orderAddressMapper.updateById(orderAddress);
        if (i==0) {rel.put("data",i); return rel;}
        orderForm.setAgoProductPrice(orderForm.getProductPrice());
        orderForm.setAgoOrderPrice(orderForm.getOrderPrice());
        orderForm.setAgoOrderFreight(orderForm.getOrderFreight());
        i = orderFormMapper.updateById(orderForm);
        if (i==0) {rel.put("data",i); return rel;};
        rel.put("data",i);
        rel.put("id",orderForm.getId().toString());
        return rel;
    }

    /**
     * 锁价子订单生成合同订单
     * @param map
     * @return
     *      201:所选购物车型号不是同次锁价数据
     *      202:超过差价上限，请重新整理购物车
     *      203:用户余额不足已补差价，请充值后再次操作
     *      204:用户余额不足已补差价及支付运费，请充值后再次操作
     *      205:待付款的锁价订单同时只可存在一个
     *      206:用户没有默认采购方信息
     *      207:该地址暂无运费信息，无法生成订单
     */
    @Transactional(value="txManager1")
    public Integer createOrderForm1(Map<String,String> map){
        UserInfo userInfo = userInfoMapper.selectById(map.get("userId"));
        List<Map<String,String>> orderFormMap = orderFormMapper.orderFormMap(userInfo.getId());
        if (orderFormMap.size()>0) return 205;//待付款的锁价订单同时只可存在一个
        List<String> orderModelIds = JSON.parseArray(map.get("orderModelIds"),String.class);
        QueryWrapper<OrderModel> orderModelQueryWrapper = new QueryWrapper<>();
        orderModelQueryWrapper.in("id",orderModelIds);
        List<OrderModel> orderModelList = orderModelMapper.selectList(orderModelQueryWrapper);
        BigDecimal[] orderAddressRoughWeight = {new BigDecimal(0)};
        BigDecimal[] modelTotalPrice = {new BigDecimal(0)};
        int[] trayNumber = {0};
        String userLockId = orderModelList.get(0).getUserLockId();
        boolean[] userLockIdSame = {false};
        orderModelList.forEach(orderModels -> {
            if (!userLockId.equals(orderModels.getUserLockId())) {
                userLockIdSame[0] = true;
                return;
            }
            orderAddressRoughWeight[0]=orderAddressRoughWeight[0].add(new BigDecimal(orderModels.getModelTotalRoughWeight()));
            modelTotalPrice[0]=modelTotalPrice[0].add(orderModels.getModelTotalPrice());
            trayNumber[0]=trayNumber[0]+orderModels.getTrayNumber();
        });
        if (userLockIdSame[0]) return 201;//所选购物车型号不是同次锁价数据
        UserLock userLock = userLockMapper.selectById(userLockId);
        BigDecimal lockPriceUpperLimit = new BigDecimal(this.getConfigValue("lockPriceUpperLimit"));
        BigDecimal upperLimit = new BigDecimal(0);//
        if (modelTotalPrice[0].compareTo(userLock.getMargin().add(userLock.getObligation()))==1) {
            if (modelTotalPrice[0].compareTo(userLock.getMargin().add(userLock.getObligation()).add(lockPriceUpperLimit))==1) {
                return 202;//超过差价上限，请重新整理购物车
            }else {
                upperLimit = userLock.getMargin().add(userLock.getObligation()).add(lockPriceUpperLimit).subtract(modelTotalPrice[0]);
            }
        }
        if (upperLimit.compareTo(new BigDecimal(0))==1)if (upperLimit.compareTo(userInfo.getPrice())==1) return 203;//用户余额不足已补差价，请充值后再次操作
        Integer orderAddressStatus = Integer.parseInt(map.get("orderAddressStatus"));
        String freightPrice = null;//运费单价
        if (orderAddressStatus==0){//单地址收货状态
            String userAddress = map.get("userAddress");
            //获取单地址订单 运费单价、运费
            String[] address = userAddress.split(" ");
            Map<String,String> cityIdMap = new HashMap<>();
            cityIdMap.put("province",address[0]);
            cityIdMap.put("city",address[1]);
            String cityId = hatCityMapper.cityId(cityIdMap);
            String freights = freightMapper.value(cityId);
            List<String> freightList = JSONObject.parseArray(freights,String.class);
            if (freightList.size()<=0||freightList==null){
                return 207;//该地址暂无运费信息，无法生成订单
            }
            //获取对应运费单价
            for (int l=0;l<freightList.size();l++){
                Map<String,Object> valueMap = JSONObject.parseObject(freightList.get(l));
                String[] names = valueMap.get("name").toString().split("-");
                if (orderAddressRoughWeight[0].compareTo(new BigDecimal(names[0]))==1){//大于
                    if (names.length==2){
                        if (orderAddressRoughWeight[0].compareTo(new BigDecimal(names[1]))==-1){//小于
                            freightPrice=valueMap.get("value").toString();
                            break;
                        }
                    }else {
                        freightPrice=valueMap.get("value").toString();
                        break;
                    }
                }
            }
            BigDecimal freight = new BigDecimal(freightPrice).multiply(orderAddressRoughWeight[0]);//运费
            if (userInfo.getPrice().compareTo(freight.add(upperLimit))==-1) return 204;//用户余额不足已补差价及支付运费，请充值后再次操作
        }else if (orderAddressStatus==1||orderAddressStatus==2) {//自提状态 或 地址待定状态
        }
        List<UserPurchaser> userPurchaserList = userPurchaserMapper.selectList(new QueryWrapper<UserPurchaser>().eq("user_id", map.get("userId")).eq("status", 0));
        if(userPurchaserList.size()<=0){
            return 202;//用户没有默认采购方信息
        }
        UserPurchaser userPurchaser = userPurchaserList.get(0);
        if (userPurchaser==null) return 206;//用户没有默认采购方信息
        //初步新建合同订单获取orderFormId
        OrderForm orderForm = new OrderForm();
        orderForm.setUserId(map.get("userId"));
        orderForm.setOrderDemander(userPurchaser.getCompanyName()+" "+userPurchaser.getUserName()+" "+userPurchaser.getUserPhone());
        SimpleDateFormat sdf2 = new SimpleDateFormat("yyyyMMdd");
        String create = sdf1.format(new Date())+" 00:00:00";
        String end = sdf1.format(this.getNextsDay(new Date(),1))+" 00:00:00";
        Map<String,String> time = new HashMap<>();
        time.put("create",create);
        time.put("end",end);
//        int count = orderFormMapper.count(time)+1;
        List<OrderForm> orderForms = orderFormMapper.orderFormList(time);
        int count = 1;
        if (orderForms.size()>0) count = orderForms.get(0).getTodayNumber()+1;
        String orderNumber = "COSMO"+sdf2.format(new Date())+"A"+String.format("%3d", count).replace(" ",  "0");
        orderForm.setOrderNumber(orderNumber);
        orderForm.setInvoiceType(0);
        orderForm.setOrderRemark(map.get("orderRemark"));
        orderForm.setOrderStatus(1);
        orderForm.setOrderTimeCreate(new Date());
        orderForm.setCashDepositType(0);
        List<Config> configList = configMapper.selectList(new QueryWrapper<Config>().eq("code", "LLDPEPrice").eq("type", 0));
        Config config = configList.get(0);
        List<Map<String, String>> list = JSON.parseObject(config.getValue(), ArrayList.class);
        String dayName1 = list.get(0).get("name");
        String dayName2 = list.get(1).get("name");
        Integer day = Integer.parseInt(getDay(dayName2));
        orderForm.setOrderTimeDelivery(this.getNextsDay(new Date(),day));
        Integer i = 0;
        i = orderFormMapper.insert(orderForm);
        if (i==0) return i;
        //初步新建默认订单地址获取orderAddressId
        OrderAddress orderAddress = new OrderAddress();
        orderAddress.setOrderId(orderForm.getId());
        orderAddress.setOrderAddressStatus(orderAddressStatus);
        orderAddress.setOrderAddressType(0);
        orderAddress.setOrderAddressShopStatus(0);
        orderAddress.setCompanyName(map.get("companyName"));
        orderAddress.setUserName(map.get("userName"));
        orderAddress.setUserPhone(map.get("userPhone"));
        orderAddress.setUserFax(map.get("userFax"));
        if (orderAddressStatus==0){//单地址收货参数
            orderAddress.setUserAddress(map.get("userAddress"));
            orderAddress.setUserDetailAddress(map.get("userDetailAddress"));
        }
        i = orderAddressMapper.insert(orderAddress);
        if (i==0) return i;
        orderModelIds.forEach(orderModelId->{//删除购物车失效缓存
            redisUtil.delete("shopTrolley,"+orderModelId);
        });
        OrderModel orderModel = new OrderModel();
        orderModel.setOrderModelStatus(1);
        orderModel.setOrderId(orderForm.getId());
        //把购物车订单转换为总订单子属
        i = orderModelMapper.update(orderModel,orderModelQueryWrapper);
        if (i==0) return i;
        int[] ii = {0};
        orderModelList.forEach(orderModels -> {
            orderModels.setId(null);
            orderModels.setOrderId(null);
            orderModels.setOrderId(orderAddress.getId());
            orderModels.setOrderModelStatus(2);
            ii[0] = ii[0] + orderModelMapper.insert(orderModels);
        });
        if (ii[0]<=0) return ii[0];
        if (orderAddressStatus==0){//单地址收货状态
            orderAddress.setOrderAddressFreightPrice(new BigDecimal(freightPrice));
            orderAddress.setOrderAddressFreight(orderAddress.getOrderAddressFreightPrice().multiply(orderAddressRoughWeight[0]));
            orderAddress.setOrderAddressRoughWeight(orderAddressRoughWeight[0].toString());
            orderAddress.setOrderAddressPrice(modelTotalPrice[0]);
            orderAddress.setOrderAddressTrayNumber(trayNumber[0]);
            orderAddress.setAgoOrderAddressPrice(orderAddress.getOrderAddressPrice());
            orderAddress.setAgoOrderAddressFreight(orderAddress.getOrderAddressFreight());
            orderAddress.setAgoOrderAddressFreightPrice(orderAddress.getOrderAddressFreightPrice());
            i = orderAddressMapper.updateById(orderAddress);
            if (i==0) return i;
            //给订单合同新增 运费、产品总金额、订单总金额、产品总毛重、订单托盘总数
            orderForm.setOrderFreight(orderAddress.getOrderAddressFreight());
            orderForm.setProductPrice(orderAddress.getOrderAddressPrice());
            orderForm.setOrderPrice(orderForm.getProductPrice().add(orderForm.getOrderFreight()));
            orderForm.setOrderWeight(orderAddress.getOrderAddressRoughWeight());
            orderForm.setOrderTotalTrayNumber(trayNumber[0]);
            orderForm.setAgoProductPrice(orderForm.getProductPrice());
            orderForm.setAgoOrderPrice(orderForm.getOrderPrice());
            orderForm.setAgoOrderFreight(orderForm.getOrderFreight());
            i = orderFormMapper.updateById(orderForm);
            if (i==0) return i;
        }else if (orderAddressStatus==1||orderAddressStatus==2) {//自提状态 或 地址待定状态
            //给订单地址新增 运费单价、运费、产品总毛重、产品总金额
            orderAddress.setOrderAddressFreightPrice(new BigDecimal(0));
            orderAddress.setOrderAddressFreight(new BigDecimal(0));
            orderAddress.setOrderAddressRoughWeight(orderAddressRoughWeight[0].toString());
            orderAddress.setOrderAddressPrice(modelTotalPrice[0]);
            orderForm.setOrderTotalTrayNumber(trayNumber[0]);
            orderAddress.setAgoOrderAddressPrice(orderAddress.getOrderAddressPrice());
            orderAddress.setAgoOrderAddressFreight(orderAddress.getOrderAddressFreight());
            orderAddress.setAgoOrderAddressFreightPrice(orderAddress.getOrderAddressFreightPrice());
            i = orderAddressMapper.updateById(orderAddress);
            if (i==0) return i;
            //给订单合同新增 运费、产品总金额、订单总金额、产品总毛重、订单托盘总数
            orderForm.setOrderFreight(new BigDecimal(0));
            orderForm.setProductPrice(orderAddress.getOrderAddressPrice());
            orderForm.setOrderPrice(orderForm.getProductPrice().add(orderForm.getOrderFreight()));
            orderForm.setOrderWeight(orderAddress.getOrderAddressRoughWeight());
            orderForm.setOrderTotalTrayNumber(trayNumber[0]);
            orderForm.setAgoProductPrice(orderForm.getProductPrice());
            orderForm.setAgoOrderPrice(orderForm.getOrderPrice());
            orderForm.setAgoOrderFreight(orderForm.getOrderFreight());
            i = orderFormMapper.updateById(orderForm);
            if (i==0) return i;
        }
        if (userLock.getObligation().compareTo(new BigDecimal("0"))>=0){//锁价订单待付款金额小于等于0时
            orderForm.setOrderStatus(2);
            orderForm.setAgoProductPrice(orderForm.getProductPrice());
            orderForm.setAgoOrderPrice(orderForm.getOrderPrice());
            orderForm.setAgoOrderFreight(orderForm.getOrderFreight());
            this.orderFormMapper.updateById(orderForm);
            if (upperLimit.compareTo(new BigDecimal(0))==0){
                userLock.setMargin(userLock.getMargin().subtract(modelTotalPrice[0]));
            }else{
                userLock.setMargin(new BigDecimal(0));
                userLock.setStatus(3);
                userInfo.setPrice(userInfo.getPrice().subtract(upperLimit));
            }
            this.userLockMapper.updateById(userLock);
            this.userInfoMapper.updateById(userInfo);
        }else {
//            Integer orderTime = 0;
//            Date nowSpecificDate = null;
//            try {
//                nowSpecificDate = sdf.parse(sdf1.format(new Date())+" 08:59:59");
//            } catch (ParseException e) {
//                e.printStackTrace();
//            }
//            if ((new Date()).compareTo(nowSpecificDate)==-1){//当前时间小于当天 08:59:59
//                orderTime = (int)((nowSpecificDate.getTime() - (new Date()).getTime())/1000);
//            }else {//当前时间大于当天 08:59:59
//                Calendar calendar = Calendar.getInstance();
//                calendar.setTime(nowSpecificDate);
//                calendar.add(Calendar.DAY_OF_MONTH, +1);//传入的时间加1天
//                Date tomorrowSpecificDate = calendar.getTime();
//                orderTime = (int)((tomorrowSpecificDate.getTime() - (new Date()).getTime())/1000);
//            }
//            redisUtil.set("orderCode,"+orderForm.getOrderNumber(),"orderCode,"+orderForm.getOrderNumber());
//            redisUtil.expire("orderCode,"+orderForm.getOrderNumber(),orderTime, TimeUnit.SECONDS);
        }
        //扣除订单金额
//        if (upperLimit.compareTo(new BigDecimal(0))==0){
//            userLock.setBalance(userLock.getBalance().subtract(modelTotalPrice[0]));
//        }else {
//            userLock.setBalance(new BigDecimal(0));
//            userInfo.setPrice(userInfo.getPrice().subtract(upperLimit));
//        }
//        if (userLock.getBalance().compareTo(new BigDecimal(0))==0) userLock.setStatus(4);
//        this.userLockMapper.updateById(userLock);
//        this.userInfoMapper.updateById(userInfo);
        return i;
    }

    /**
     * 给待定订单新增地址
     * @param map
     * @return
     */
    @Transactional(value="txManager1")
    public Integer addOrderAddress(Map<String,String> map){
//        BigDecimal boxWeigth = new BigDecimal(this.getConfigValue("boxWeigth"));
        BigDecimal trayWeigth = new BigDecimal(this.getConfigValue("trayWeigth"));
        BigDecimal trayPrice = new BigDecimal(this.getConfigValue("trayPrice"));
        UserInfo userInfo = userInfoMapper.selectById(map.get("userId"));
        OrderForm orderForm = orderFormMapper.selectById(map.get("orderFormId"));
        List<OrderModel> orderModelList1 = new ArrayList<>();//原先剩余的各型号数
        List<OrderModel> orderModelList2 = new ArrayList<>();//分配出来的各型号数
        Integer orderAddressTrayNumber1 = 0;
        Integer orderAddressTrayNumber2 = 0;
        BigDecimal addressTotalRoughWeight1 = new BigDecimal(0);//原先地址总毛重
        BigDecimal addressTotalRoughWeight2 = new BigDecimal(0);//当前地址总毛重
        BigDecimal addressTotalPrice1 = new BigDecimal(0);//原先地址商品总价
        BigDecimal addressTotalPrice2 = new BigDecimal(0);//当前地址商品总价
        BigDecimal totalPrice = new BigDecimal(0);//需要扣除的总金额
        String info = "";
        List<String> stringList = JSON.parseArray(map.get("orderModels"),String.class);
        for(int i=0;i<stringList.size();i++){
            Map<String,Object> stringMap = JSON.parseObject(stringList.get(i),Map.class);
            OrderModel orderModel1 = new OrderModel();
            OrderModel orderModel2 = orderModelMapper.selectById(stringMap.get("orderModelId").toString());
            BeanUtils.copyProperties(orderModel2, orderModel1);//把orderModel2的值赋值给orderModel1
            if (orderModel1.getRollNumber()<Integer.parseInt(stringMap.get("number").toString())) return 501;//有型号卷数不足
            orderModel1.setRollNumber(orderModel1.getRollNumber()-Integer.parseInt(stringMap.get("number").toString()));
            orderModel2.setRollNumber(Integer.parseInt(stringMap.get("number").toString()));
            if (orderModel2.getCartonType()!=2){
                Integer cartonNumber1 = orderModel1.getRollNumber()/orderModel1.getCartonPipeNumber();
                Integer cartonNumber2 = orderModel2.getRollNumber()/orderModel2.getCartonPipeNumber();
                if (orderModel2.getRollNumber()%orderModel2.getCartonPipeNumber()>0){
                    cartonNumber1=cartonNumber1+1;
                    cartonNumber2=cartonNumber2+1;
                }
                orderModel1.setCartonNumber(cartonNumber1);
                orderModel2.setCartonNumber(cartonNumber2);
            }
            if (orderModel2.getTrayType()==0){
                Integer trayNumber1 = orderModel1.getCartonNumber()/orderModel1.getTrayCapacity();
                Integer trayNumber2 = orderModel2.getCartonNumber()/orderModel2.getTrayCapacity();
                if (orderModel2.getCartonNumber()%orderModel2.getTrayCapacity()>0){
                    trayNumber1=trayNumber1+1;
                    trayNumber2=trayNumber2+1;
                }
                orderModel1.setTrayNumber(trayNumber1);
                orderModel2.setTrayNumber(trayNumber2);
                orderAddressTrayNumber2=orderAddressTrayNumber2+trayNumber2;
            }
            BigDecimal modelTotalRoughWeight1 = new BigDecimal(orderModel1.getRollNumber()).multiply(new BigDecimal(orderModel1.getRollRoughWeight()))
                    .add(new BigDecimal(orderModel1.getCartonWeight()).multiply(new BigDecimal(orderModel1.getCartonNumber())))
                    .add(trayWeigth.multiply(new BigDecimal(orderModel1.getTrayNumber())));
            orderModel1.setModelTotalRoughWeight(modelTotalRoughWeight1.toString());
            orderModel1.setModelTotalSuttle((new BigDecimal(orderModel1.getSpecSuttle()).multiply(new BigDecimal(orderModel1.getRollNumber()))).toString());
            orderModel1.setModelTotalPrice(new BigDecimal(orderModel1.getModelTotalSuttle()).multiply(orderModel1.getModelUnitPrice())
                    .add(trayPrice.multiply(new BigDecimal(orderModel1.getTrayNumber()))));
            orderModelList1.add(orderModel1);
            BigDecimal modelTotalRoughWeight2 = new BigDecimal(orderModel2.getRollNumber()).multiply(new BigDecimal(orderModel2.getRollRoughWeight()))
                    .add(new BigDecimal(orderModel1.getCartonWeight()).multiply(new BigDecimal(orderModel2.getCartonNumber())))
                    .add(trayWeigth.multiply(new BigDecimal(orderModel2.getTrayNumber())));
            orderModel2.setModelTotalRoughWeight(modelTotalRoughWeight2.toString());
            orderModel2.setModelTotalSuttle((new BigDecimal(orderModel2.getSpecSuttle()).multiply(new BigDecimal(orderModel2.getRollNumber()))).toString());
            orderModel2.setModelTotalPrice(new BigDecimal(orderModel2.getModelTotalSuttle()).multiply(orderModel2.getModelUnitPrice())
                    .add(trayPrice.multiply(new BigDecimal(orderModel2.getTrayNumber()))));
            orderModel2.setId(null);
            orderModel2.setOrderId(null);
            orderModelList2.add(orderModel2);
            addressTotalRoughWeight2 = addressTotalRoughWeight2.add(modelTotalRoughWeight2);
            addressTotalPrice2 = addressTotalPrice2.add(orderModel2.getModelTotalPrice());
        }
        QueryWrapper<OrderAddress> orderAddressQueryWrapper = new QueryWrapper<>();
        orderAddressQueryWrapper.eq("order_id",orderForm.getId()).eq("order_address_type",0);
        OrderAddress orderAddress1 = orderAddressMapper.selectList(orderAddressQueryWrapper).get(0);
        OrderAddress orderAddress2 = new OrderAddress();
        orderAddress2.setOrderId(orderForm.getId());
        orderAddress2.setCompanyName(map.get("companyName"));
        orderAddress2.setUserName(map.get("userName"));
        orderAddress2.setUserPhone(map.get("userPhone"));
        orderAddress2.setUserFax(map.get("userFax"));
        orderAddress2.setOrderAddressType(1);
        orderAddress2.setOrderAddressShopStatus(0);
        orderAddress2.setOrderAddressRoughWeight(addressTotalRoughWeight2.toString());
        orderAddress2.setOrderAddressPrice(addressTotalPrice2);
        orderAddress2.setOrderAddressTrayNumber(orderAddressTrayNumber2);
        orderAddress2.setOrderAddressStatus(Integer.parseInt(map.get("orderAddressStatus")));
        if (orderAddress2.getOrderAddressStatus()==0){//新增地址状态为但单地址发货时执行
            orderAddress2.setUserAddress(map.get("userAddress"));
            orderAddress2.setUserDetailAddress(map.get("userDetailAddress"));
            String[] address = orderAddress2.getUserAddress().split(" ");
            Map<String,String> cityIdMap = new HashMap<>();
            cityIdMap.put("province",address[0]);
            cityIdMap.put("city",address[1]);
            String cityId = hatCityMapper.cityId(cityIdMap);
            String freights = freightMapper.value(cityId);
            List<String> freightList = JSONObject.parseArray(freights,String.class);
            String freightPrice = null;
            for (int l=0;l<freightList.size();l++){
                Map<String,Object> valueMap = JSONObject.parseObject(freightList.get(l));
                String[] names = valueMap.get("name").toString().split("-");
                if (addressTotalRoughWeight2.compareTo(new BigDecimal(names[0]))==1){//大于
                    if (names.length==2){
                        if (addressTotalRoughWeight2.compareTo(new BigDecimal(names[1]))==-1){//小于
                            freightPrice=valueMap.get("value").toString();
                            break;
                        }
                    }else {
                        freightPrice=valueMap.get("value").toString();
                        break;
                    }
                }
            }
            orderAddress2.setOrderAddressFreightPrice(new BigDecimal(freightPrice));
            orderAddress2.setOrderAddressFreight(orderAddress2.getOrderAddressFreightPrice().multiply(addressTotalRoughWeight2));
            totalPrice=totalPrice.add(orderAddress2.getOrderAddressFreight());
            if ("0".equals(userInfo.getMemberId())){
                if (userInfo.getPrice().compareTo(orderAddress2.getOrderAddressFreight())==-1) return 502;//余额不足支付本次运费
            }else {
                if (userInfo.getMemberPrice().compareTo(orderAddress2.getOrderAddressFreight())==-1) return 503;//会员余额不足支付本次运费
            }
            info = "扣除新地址运费 ";
        }
        QueryWrapper<OrderModel> orderModelQueryWrapper = new QueryWrapper<>();
        orderModelQueryWrapper.eq("order_id",orderAddress1.getId());
        List<OrderModel> orderModelList3 = orderModelMapper.selectList(orderModelQueryWrapper);
        boolean allNull = true;
        for (OrderModel orderModel3:orderModelList3){
            if (orderModel3.getRollNumber()!=0) {
                allNull=false;
                break;
            }
        }
        if (allNull){//判断订单下所有型号是否全部用完
            QueryWrapper<OrderAddress> orderAddressQueryWrapper1 = new QueryWrapper<>();
            orderAddressQueryWrapper1.eq("order_id",orderForm.getId()).eq("order_address_type",1);
            List<OrderAddress> orderAddressList = orderAddressMapper.selectList(orderAddressQueryWrapper1);
            Integer trayNumber[] = {0};
            orderAddressList.forEach(orderAddress3 -> {
                trayNumber[0]=trayNumber[0]+orderAddress3.getOrderAddressTrayNumber();
            });
            if ((trayNumber[0]+orderAddressTrayNumber2)>orderForm.getOrderTotalTrayNumber()){//判断所用托盘是否超过原托盘总数
                BigDecimal price = orderAddress2.getOrderAddressFreight().add(new BigDecimal((trayNumber[0]+orderAddressTrayNumber2)-orderForm.getOrderTotalTrayNumber()).multiply(trayPrice));
                totalPrice = totalPrice.add(price);
                if ("0".equals(userInfo.getMemberId())){
                    if (userInfo.getPrice().compareTo(totalPrice)==-1) return 504;//余额不足支付该订单的托盘差价
                }else {
                    if (userInfo.getPrice().compareTo(totalPrice)==-1) return 505;//会员余额不足支付该订单的托盘差价
                }
                info = info+"扣除托盘差价";
            }
        }
        if ("0".equals(userInfo.getMemberId())){
            UserPriceInfo userPriceInfo = new UserPriceInfo();
            userPriceInfo.setUserId(userInfo.getId());
            userPriceInfo.setType("-");
            userPriceInfo.setPrice(totalPrice);
            userPriceInfo.setInfo(info);
            this.userPriceInfoMapper.insert(userPriceInfo);
            userInfo.setPrice(userInfo.getPrice().subtract(totalPrice));
        }else {
            UserMemberPriceInfo userMemberPriceInfo = new UserMemberPriceInfo();
            userMemberPriceInfo.setUserId(userInfo.getId());
            userMemberPriceInfo.setType(1);
            userMemberPriceInfo.setPrice(totalPrice);
            userMemberPriceInfo.setInfo("待定订单："+orderForm.getOrderNumber()+"-"+info);
            userMemberPriceInfo.setTime(new Date());
            this.userMemberPriceInfoMapper.insert(userMemberPriceInfo);
            userInfo.setMemberPrice(userInfo.getMemberPrice().subtract(totalPrice));
        }
        this.userInfoMapper.updateById(userInfo);
        this.orderAddressMapper.insert(orderAddress2);
        orderModelList1.forEach(orderModel1 -> {
            this.orderModelMapper.updateById(orderModel1);
        });
        orderModelList2.forEach(orderModel2 -> {
            orderModel2.setOrderId(orderAddress2.getId());
            this.orderModelMapper.insert(orderModel2);
        });
        for (int i=0;i<orderModelList3.size();i++){
            orderAddressTrayNumber1=orderAddressTrayNumber1+orderModelList3.get(i).getTrayNumber();
            addressTotalRoughWeight1 = addressTotalRoughWeight1.add(new BigDecimal(orderModelList3.get(i).getModelTotalRoughWeight()));
            addressTotalPrice1 = addressTotalPrice1.add(orderModelList3.get(i).getModelTotalPrice());
        }
        orderAddress1.setOrderAddressRoughWeight(addressTotalRoughWeight1.toString());
        orderAddress1.setOrderAddressPrice(addressTotalPrice1);
        orderAddress1.setOrderAddressTrayNumber(orderAddressTrayNumber1);
        this.orderAddressMapper.updateById(orderAddress1);
        return 1;
    }

    /**
     * 根据状态分页查询订单合同
     * @param pageNum
     * @param orderStatus
     * @return
     */
    public PageInfo orderFormPageList(Integer pageNum,Integer orderStatus,String userId){
        QueryWrapper<OrderForm> orderFormQueryWrapper = new QueryWrapper<>();
        orderFormQueryWrapper.eq("order_status",orderStatus)
                .eq("user_id",userId).orderByDesc("order_time_create");
        Page page = new Page(pageNum,10);
        IPage<OrderForm> orderFormList = orderFormMapper.selectPage(page,orderFormQueryWrapper);
        PageInfo pageInfo = new PageInfo(orderFormList);
        for (OrderForm orderForm : (List<OrderForm>)pageInfo.getList()){
            List<OrderAddress> orderAddressList = new ArrayList<>();
            QueryWrapper<OrderAddress> orderAddressQueryWrapper0 = new QueryWrapper<>();
            orderAddressQueryWrapper0.eq("order_id",orderForm.getId()).eq("order_address_type",0);
            List<OrderAddress> orderAddressList0 = orderAddressMapper.selectList(orderAddressQueryWrapper0);
            QueryWrapper<OrderAddress> orderAddressQueryWrapper1 = new QueryWrapper<>();
            orderAddressQueryWrapper1.eq("order_id",orderForm.getId()).eq("order_address_type",1);
            List<OrderAddress> orderAddressList1 = orderAddressMapper.selectList(orderAddressQueryWrapper1);
            QueryWrapper<OrderModel> orderModelQueryWrapper = new QueryWrapper<>();
            orderModelQueryWrapper.eq("order_id",orderAddressList0.get(0).getId()).eq("order_model_status",2).ne("roll_number",0);
            List<OrderModel> orderModelList = orderModelMapper.selectList(orderModelQueryWrapper);
            orderForm.setOrderDefaultStatus(orderAddressList0.get(0).getOrderAddressStatus());
            if (orderModelList.size()>0) orderAddressList.addAll(orderAddressList0);
            orderAddressList.addAll(orderAddressList1);
            orderForm.setOrderAddressList(orderAddressList);
        }
        return pageInfo;
    }

    /**
     * 根据状态查询订单合同
     * @param orderStatus
     * @return
     */
    public List<OrderForm> orderFormList(Integer orderStatus,String userId){
        QueryWrapper<OrderForm> orderFormQueryWrapper = new QueryWrapper<>();
        orderFormQueryWrapper.eq("order_status",orderStatus)
                .eq("user_id",userId).orderByDesc("order_time_create");
        List<OrderForm> orderFormList = orderFormMapper.selectList(orderFormQueryWrapper);
        for (OrderForm orderForm : orderFormList){
            QueryWrapper<OrderAddress> orderAddressQueryWrapper = new QueryWrapper<>();
            orderAddressQueryWrapper.eq("order_id",orderForm.getId());
            List<OrderAddress> orderAddressList = orderAddressMapper.selectList(orderAddressQueryWrapper);
            orderForm.setOrderAddressList(orderAddressList);
        }
        return orderFormList;
    }

    /**
     * 根据orderFormId查询orderForm详情加默认地址
     * @param orderFormId
     * @return
     */
    public OrderForm orderFormInfo(String orderFormId){
        OrderForm orderForm = orderFormMapper.selectById(orderFormId);
        QueryWrapper<OrderAddress> orderAddressQueryWrapper = new QueryWrapper<>();
        orderAddressQueryWrapper.eq("order_id",orderForm.getId()).eq("order_address_type",0);
        List<OrderAddress> orderAddressList = orderAddressMapper.selectList(orderAddressQueryWrapper);
        orderForm.setOrderAddressList(orderAddressList);
        return orderForm;
    }

    /**
     * 根据orderFormId查询orderForm详情加默认地址加后增地址
     * @param orderFormId
     * @return
     */
    public Map<String,Object> orderFormInfo1(String orderFormId){
        OrderForm orderForm = orderFormMapper.selectById(orderFormId);
        QueryWrapper<OrderAddress> orderAddressQueryWrapper = new QueryWrapper<>();
        orderAddressQueryWrapper.eq("order_id",orderForm.getId()).eq("order_address_type",0);
        List<OrderAddress> orderAddressList0 = orderAddressMapper.selectList(orderAddressQueryWrapper);
        QueryWrapper<OrderAddress> orderAddressQueryWrapper1 = new QueryWrapper<>();
        orderAddressQueryWrapper1.eq("order_id",orderForm.getId()).eq("order_address_type",1);
        List<OrderAddress> orderAddressList1 = orderAddressMapper.selectList(orderAddressQueryWrapper1);
        Map<String,Object> map = new HashMap<>();
        map.put("id",orderForm.getId());map.put("userId",orderForm.getUserId());map.put("orderNumber",orderForm.getOrderNumber());
        map.put("orderFreight",orderForm.getOrderFreight());map.put("productPrice",orderForm.getProductPrice());map.put("orderPrice",orderForm.getOrderPrice());
        map.put("orderWeight",orderForm.getOrderWeight());map.put("invoiceId",orderForm.getInvoiceId());map.put("invoiceType",orderForm.getInvoiceType());
        map.put("orderRemark",orderForm.getOrderRemark());map.put("orderStatus",orderForm.getOrderStatus());map.put("orderTimeCreate",orderForm.getOrderTimeCreate());
        map.put("orderTimeEnd",orderForm.getOrderTimeEnd());map.put("orderTimeDelivery",orderForm.getOrderTimeDelivery());map.put("orderTotalTrayNumber",orderForm.getOrderTotalTrayNumber());
        map.put("orderAddressList0",orderAddressList0);map.put("orderAddressList1",orderAddressList1);
        return map;
    }

    /**
     * 根据orderAddressId查询orderAddress详情
     * @param orderAddressId
     * @return
     */
    public OrderAddress orderAddressInfo(String orderAddressId){
        OrderAddress orderAddress = orderAddressMapper.selectById(orderAddressId);
        QueryWrapper<OrderModel> orderModelQueryWrapper = new QueryWrapper<>();
        orderModelQueryWrapper.eq("order_id",orderAddressId).eq("order_model_status",2).ne("roll_number",0);
        List<OrderModel> orderModelList = orderModelMapper.selectList(orderModelQueryWrapper);
        if (orderModelList.size()>0)orderAddress.setOrderModelList(orderModelList);
        else orderAddress.setOrderModelList(new ArrayList<>());
        return orderAddress;
    }

    /**
     * 修改合同订单的默认地址
     * @param map
     * @return
     */
    @Transactional(value="txManager1")
    public Integer updateOrderAddress(Map<String,String> map){
        OrderAddress orderAddress = orderAddressMapper.selectById(map.get("orderAddressId"));
        OrderForm orderForm = orderFormMapper.selectById(orderAddress.getOrderId());
        orderAddress.setCompanyName(map.get("companyName"));
        orderAddress.setUserName(map.get("userName"));
        orderAddress.setUserPhone(map.get("userPhone"));
        orderAddress.setUserFax(map.get("userFax"));
        orderAddress.setOrderAddressStatus(Integer.parseInt(map.get("orderAddressStatus")));
        if (orderAddress.getOrderAddressStatus()==0) {//修改为单地址状态
            orderAddress.setUserAddress(map.get("userAddress"));
            orderAddress.setUserDetailAddress(map.get("userDetailAddress"));
            BigDecimal addressTotalRoughWeight = new BigDecimal(orderAddress.getOrderAddressRoughWeight());
            String[] address = orderAddress.getUserAddress().split(" ");
            Map<String,String> cityIdMap = new HashMap<>();
            cityIdMap.put("province",address[0]);
            cityIdMap.put("city",address[1]);
            String cityId = hatCityMapper.cityId(cityIdMap);
            String freights = freightMapper.value(cityId);
            List<String> freightList = JSONObject.parseArray(freights,String.class);
            String freightPrice = null;
            for (int l=0;l<freightList.size();l++){
                Map<String,Object> valueMap = JSONObject.parseObject(freightList.get(l));
                String[] names = valueMap.get("name").toString().split("-");
                if (addressTotalRoughWeight.compareTo(new BigDecimal(names[0]))==1){//大于
                    if (names.length==2){
                        if (addressTotalRoughWeight.compareTo(new BigDecimal(names[1]))==-1){//小于
                            freightPrice=valueMap.get("value").toString();
                            break;
                        }
                    }else {
                        freightPrice=valueMap.get("value").toString();
                        break;
                    }
                }
            }
            orderAddress.setOrderAddressFreightPrice(new BigDecimal(freightPrice));
            orderAddress.setOrderAddressFreight(orderAddress.getOrderAddressFreightPrice().multiply(addressTotalRoughWeight));
            orderForm.setOrderFreight(orderAddress.getOrderAddressFreight());
            orderForm.setOrderPrice(orderForm.getProductPrice().add(orderForm.getOrderFreight()));
        }else if (orderAddress.getOrderAddressStatus()==1||orderAddress.getOrderAddressStatus()==2) {//修改为自提或待定
            orderAddress.setUserAddress(null);
            orderAddress.setUserDetailAddress(null);
            orderAddress.setOrderAddressFreightPrice(null);
            orderAddress.setOrderAddressFreight(null);
            orderForm.setOrderFreight(orderAddress.getOrderAddressFreight());
            orderForm.setOrderPrice(orderForm.getProductPrice());
        }
        Integer i = orderAddressMapper.updateById(orderAddress);
        if (i<0) return i;
        i = orderFormMapper.updateById(orderForm);
        return i;
    }

    /**
     * 给合同订单绑定返现红包
     *      0 绑定成功
     *      1 该订单不存在
     *      2 当前状态无法绑定返现红包
     *      3 当前优惠卷不存在
     *      4 优惠卷已使用
     *      5 该订单不满足优惠卷使用条件
     * @param orderFormId
     * @param couponId
     * @return
     */
    @Transactional(value="txManager1")
    public Integer bindCoupon(String orderFormId,String couponId){
        OrderForm orderForm = orderFormMapper.selectById(orderFormId);
        if (orderForm==null) return 1;
        if (orderForm.getOrderStatus()!=1) return 2;
        Coupon coupon = couponMapper.selectById(couponId);
        if (coupon==null) return 3;
        if (coupon.getStatus()==1) return 4;
        if (orderForm.getOrderPrice().compareTo(coupon.getFull())==-1) return 5;
        orderForm.setOrderCouponId(couponId);

        orderFormMapper.updateById(orderForm);
        coupon.setStatus(1);
        coupon.setEmployPrice(orderForm.getOrderPrice());
        coupon.setEmployTime(sdf.format(new Date()));
        coupon.setEmployStatus(0);
        couponMapper.updateById(coupon);
        return 0;
    }

    /**
     * 后台
     * 根据状态、订单号
     * 分页查询订单合同
     * @param pageNum
     * @param map
     * @return
     */
    public PageInfo orderFormPageListPc(Integer pageNum,Map<String,String> map){
        QueryWrapper<OrderForm> orderFormQueryWrapper = new QueryWrapper<>();
        orderFormQueryWrapper.eq("order_status",map.get("orderStatus")).orderByDesc("order_time_create").orderByAsc("order_time_delivery");
        if (!StringUtil.isEmpty(map.get("userId")))orderFormQueryWrapper.eq("user_id",map.get("userId"));
        if (!StringUtil.isEmpty(map.get("orderNumber"))) orderFormQueryWrapper.like("order_number",map.get("orderNumber"));
        Page page = new Page(pageNum,10);
        IPage<OrderForm> orderFormList = orderFormMapper.selectPage(page,orderFormQueryWrapper);
        PageInfo pageInfo = new PageInfo(orderFormList);
        List<OrderForm> orderForms = new ArrayList<>();
        pageInfo.getList().forEach(orderForm->{
            OrderForm orderForm1 = (OrderForm) orderForm;
            QueryWrapper<OrderModel> orderModelQueryWrapper = new QueryWrapper<>();
            orderModelQueryWrapper.eq("order_id",orderForm1.getId()).eq("model_raw_price_type",2).eq("order_model_status",1);
            List<OrderModel> orderModelList = orderModelMapper.selectList(orderModelQueryWrapper);
            if (orderModelList.size()>0) {
                UserLock userLock = userLockMapper.selectById(orderModelList.get(0).getUserLockId());
                orderForm1.setUserLockId(userLock.getId());
                orderForm1.setUserLockOrderPrice(userLock.getObligation());
            } else {
                orderForm1.setUserLockId("0");
                orderForm1.setUserLockOrderPrice(new BigDecimal("0"));
            }
            if (((OrderForm) orderForm).getOrderCouponId()!=null){
                orderForm1.setOrderCouponInfo(couponMapper.selectById(((OrderForm) orderForm).getOrderCouponId()));
                if (orderForm1.getOrderCouponInfo().getType()==0){
                    orderForm1.setOrderPriceCoupon(orderForm1.getOrderPrice().subtract(orderForm1.getOrderCouponInfo().getSubtract()));
                }else {
                    orderForm1.setOrderPriceCoupon(orderForm1.getOrderPrice());
                }
            }else {
                orderForm1.setOrderCouponId("0");
            }
            OrderAddress orderAddress = null;
            List<OrderAddress> orderAddresses = orderAddressMapper.selectList(new QueryWrapper<OrderAddress>()
                    .eq("order_id", orderForm1.getId())
                    .eq("order_address_type", 0));
            if (orderAddresses.size()>0){
                orderAddress = orderAddresses.get(0);
            }

            List<OrderAddress> orderAddressList = new ArrayList<>();
            QueryWrapper<OrderAddress> orderAddressQueryWrapper0 = new QueryWrapper<>();
            orderAddressQueryWrapper0.eq("order_id",orderForm1.getId()).eq("order_address_type",0);
            List<OrderAddress> orderAddressList0 = orderAddressMapper.selectList(orderAddressQueryWrapper0);
            QueryWrapper<OrderAddress> orderAddressQueryWrapper1 = new QueryWrapper<>();
            orderAddressQueryWrapper1.eq("order_id",orderForm1.getId()).eq("order_address_type",1);
            List<OrderAddress> orderAddressList1 = orderAddressMapper.selectList(orderAddressQueryWrapper1);
            QueryWrapper<OrderModel> orderModelQueryWrapper1 = new QueryWrapper<>();
            orderModelQueryWrapper1.eq("order_id",orderAddressList0.get(0).getId()).eq("order_model_status",2).ne("roll_number",0);
            List<OrderModel> orderModelList1 = orderModelMapper.selectList(orderModelQueryWrapper1);
            orderForm1.setOrderDefaultStatus(orderAddressList0.get(0).getOrderAddressStatus());
            if (orderModelList1.size()>0) orderAddressList.addAll(orderAddressList0);
            orderAddressList.addAll(orderAddressList1);
            orderForm1.setOrderAddressList(orderAddressList);

            UserInfo userInfo = userInfoMapper.selectById(orderForm1.getUserId());
            orderForm1.setCompanyName(orderAddress.getCompanyName());
            if (userInfo!=null){
                orderForm1.setWxName(userInfo.getWxName());
                orderForm1.setName(userInfo.getName());
            }
            orderForms.add(orderForm1);
        });
        pageInfo.setList(orderForms);
        return pageInfo;
    }

    /**
     * 后台
     * 根据orderId（上级id）和orderModelStatus查询订单子属型号列表
     * @param orderId
     * @return
     */
    public List<OrderModel> orderModelList(String orderId){
        QueryWrapper<OrderAddress> orderAddressQueryWrapper = new QueryWrapper<>();
        orderAddressQueryWrapper.eq("order_id",orderId).eq("order_address_type",0);
        OrderAddress orderAddress = orderAddressMapper.selectList(orderAddressQueryWrapper).get(0);
        QueryWrapper<OrderModel> orderModelQueryWrapper = new QueryWrapper<>();
        if (orderAddress.getOrderAddressStatus()==0||orderAddress.getOrderAddressStatus()==1){
            orderModelQueryWrapper.eq("order_id",orderAddress.getId()).eq("order_model_status",2);
        }else {
//            orderModelQueryWrapper.eq("order_id",orderAddress.getId()).eq("order_model_status",1);
            orderModelQueryWrapper.eq("order_id",orderId).eq("order_model_status",1);
        }
        return orderModelMapper.selectList(orderModelQueryWrapper);
    }

    /**
     * 后台
     * 根据orderId（上级id）和orderModelStatus查询订单子属型号列表
     * @param orderId
     * @return
     */
    public List<OrderModel> orderModelList1(String orderId){
        QueryWrapper<OrderModel> orderModelQueryWrapper = new QueryWrapper<>();
        orderModelQueryWrapper.eq("order_id",orderId).eq("order_model_status","1");
        return orderModelMapper.selectList(orderModelQueryWrapper);
    }

    /**
     * 后台
     * 根据orderId（上级id）和orderModelStatus查询订单子属型号列表
     * @param orderId
     * @param orderModelStatus
     * @return
     */
    public List<OrderModel> orderModelList1(String orderId,Integer orderModelStatus){
        QueryWrapper<OrderModel> orderModelQueryWrapper = new QueryWrapper<>();
        orderModelQueryWrapper.eq("order_id",orderId).eq("order_model_status",orderModelStatus);
        return orderModelMapper.selectList(orderModelQueryWrapper);
    }

    /**
     * 后台
     * 根据orderId（订单合同id）和合同是否默认状态查询订单地址列表
     * @param orderId
     * @return
     */
    public List<OrderAddress> orderAddressList(String orderId,Integer orderAddressType ){
        QueryWrapper<OrderAddress> orderAddressQueryWrapper = new QueryWrapper<>();
        orderAddressQueryWrapper.eq("order_id",orderId).eq("order_address_type",orderAddressType);
        return orderAddressMapper.selectList(orderAddressQueryWrapper);
    }

    /**
     * 根据id修改orderForm
     * @param map
     * @return
     */
    @Transactional(value="txManager1")
    public Integer updateOrderForm(Map<String,String> map){
        String orderFormId = map.get("orderFormId");
        Integer orderStatus = Integer.parseInt(map.get("orderStatus"));
        OrderForm orderForm = orderFormMapper.selectById(orderFormId);
        //判断是否存在redis缓存，存在及删除
        if (redisUtil.hasKey("intentionGold,"+orderForm.getOrderNumber())) redisUtil.delete("intentionGold,"+orderForm.getOrderNumber());
//        if (redisUtil.hasKey("orderCode,"+orderForm.getOrderNumber())) redisUtil.delete("orderCode,"+orderForm.getOrderNumber());
        // 0待付保证金，1待付款，2未完成，3已完成，4已取消
        if(orderStatus.equals(0)){
            Integer orderTime = Integer.parseInt(this.getConfigValue("intentionGoldTime"));
            redisUtil.set("intentionGold,"+orderForm.getOrderNumber(),"intentionGold,"+orderForm.getOrderNumber());
            redisUtil.expire("intentionGold,"+orderForm.getOrderNumber(),orderTime, TimeUnit.SECONDS);
//        }else if(orderStatus.equals(1)){
////            Integer orderTime = Integer.parseInt(this.getConfigValue("orderTime"));
//            Integer orderTime = 0;
//            Date nowSpecificDate = null;
//            try {
//                nowSpecificDate = sdf.parse(sdf1.format(new Date())+" 08:59:59");
//            } catch (ParseException e) {
//                e.printStackTrace();
//            }
//            if ((new Date()).compareTo(nowSpecificDate)==-1){//当前时间小于当天 08:59:59
//                orderTime = (int)((nowSpecificDate.getTime() - (new Date()).getTime())/1000);
//            }else {//当前时间大于当天 08:59:59
//                Calendar calendar = Calendar.getInstance();
//                calendar.setTime(nowSpecificDate);
//                calendar.add(Calendar.DAY_OF_MONTH, +1);//传入的时间加1天
//                Date tomorrowSpecificDate = calendar.getTime();
//                orderTime = (int)((tomorrowSpecificDate.getTime() - (new Date()).getTime())/1000);
//            }
//            redisUtil.set("orderCode,"+orderForm.getOrderNumber(),"orderCode,"+orderForm.getOrderNumber());
//            redisUtil.expire("orderCode,"+orderForm.getOrderNumber(),orderTime, TimeUnit.SECONDS);
        }else if(orderStatus.equals(2)){
            QueryWrapper<OrderModel> orderModelQueryWrapper = new QueryWrapper<>();
            orderModelQueryWrapper.eq("order_id",orderForm.getId()).eq("order_model_status",1);
            OrderModel orderModel1 = null;
            List<OrderModel> orderModels1 = orderModelMapper.selectList(orderModelQueryWrapper);
            if (orderModels1.size()>0){
                orderModel1 = orderModels1.get(0);
            }
            if (orderModel1.getModelRawPriceType()==2){//合同订单为锁价时
                UserLock userLock = userLockMapper.selectById(orderModel1.getUserLockId());
                if (userLock.getObligation().compareTo(new BigDecimal("0"))<=0){//锁价剩余待付款小于等于0时，及锁价剩余待付款没有剩余
                    userLock.setMargin(userLock.getMargin().subtract(orderForm.getOrderPrice()));
                    if (userLock.getMargin().compareTo(new BigDecimal("0"))==0) userLock.setStatus(3);//锁价保证金用完
                }else {//锁价剩余待付款大于0时，及锁价剩余待付款有剩余
                    if (userLock.getObligation().compareTo(orderForm.getOrderPrice())==1){//锁价剩余待付款大于订单金额
                        userLock.setObligation(userLock.getObligation().subtract(orderForm.getOrderPrice()));
                    }else if (userLock.getObligation().compareTo(orderForm.getOrderPrice())==0){//锁价剩余待付款等于订单金额
                        userLock.setObligation(new BigDecimal("0"));
                    }else if (userLock.getObligation().compareTo(orderForm.getOrderPrice())==-1){//锁价剩余待付款小于订单金额
                        userLock.setObligation(new BigDecimal("0"));
                        userLock.setMargin(userLock.getMargin().subtract(orderForm.getOrderPrice().subtract(userLock.getObligation())));
                    }
                }
                this.userLockMapper.updateById(userLock);
            }else {//合同订单不为锁价时
                UserInfo userInfo = userInfoMapper.selectById(orderForm.getUserId());
                String[] serialNumbers = userInfo.getSerialNumber().split("-");
                QueryWrapper<UserInfo> userInfoQueryWrapper = new QueryWrapper<>();
                userInfoQueryWrapper.eq("member_id",1).likeRight("serial_number",serialNumbers[0]);
                List<UserInfo> userInfoList = userInfoMapper.selectList(userInfoQueryWrapper);
                //此用户所在区域是否有黑钻会员
                if (userInfoList.size()>0){
                    UserInfo userInfo1 = userInfoList.get(0);
                    //判断该订单用户是否是黑钻会员
                    if (userInfo.getMemberId()!=userInfo1.getMemberId()){
                        Map<String,String> map1 = new HashMap<>();
                        BigDecimal orderPrice = new BigDecimal(0);
                        BigDecimal orderPriceSon = new BigDecimal(0);
                        QueryWrapper<OrderModel> orderModelQueryWrapper1 = new QueryWrapper<>();
                        orderModelQueryWrapper1.eq("order_id",orderFormId).eq("order_model_status",1);
                        List<OrderModel> orderModels = orderModelMapper.selectList(orderModelQueryWrapper1);
                        for (OrderModel orderModel:orderModels){
                            //获取 黑钻会员 与当前用户会员折扣 当前型号价差
                            map1.put("memberId","1");map1.put("modelName",orderModel.getModelName());
                            UserMemberModel userMemberModel1 = userMemberModelMapper.selectUserMemberMap(map1);
                            BigDecimal discount = userMemberModel1.getDiscount();
                            BigDecimal price = new BigDecimal(orderModel.getModelTotalSuttle()).multiply(orderModel.getModelUnitPrice().subtract(orderModel.getMemberDiscount()));
                            BigDecimal price1 = new BigDecimal(orderModel.getModelTotalSuttle()).multiply(orderModel.getModelUnitPrice().subtract(discount));
                            //判断有无托盘
                            if (orderModel.getTrayType()==0){
                                price = price.add(new BigDecimal(orderModel.getTrayNumber()).multiply(new BigDecimal(this.getConfigValue("trayPrice"))));
                                price1 = price1.add(new BigDecimal(orderModel.getTrayNumber()).multiply(new BigDecimal(this.getConfigValue("trayPrice"))));
                            }
                            orderPrice = orderPrice.add(price1);
                            orderPriceSon= orderPriceSon.add(price);
                        }
                        //判断黑钻会员剩余会员预存金额是否足与当前订单扣除
                        QueryWrapper<OrderParent> orderParentQueryWrapper = new QueryWrapper<>();
                        orderParentQueryWrapper.eq("order_id",orderFormId);
                        List<OrderParent> orderParentList = orderParentMapper.selectList(orderParentQueryWrapper);
                        if (orderParentList.size()>0){}else {
                            if (userInfo1.getMemberPrice().compareTo(orderPrice)==1){
                                userInfo1.setMemberPrice(userInfo1.getMemberPrice().subtract(orderPrice));
                                this.userInfoMapper.updateById(userInfo1);
                                //新增黑钻会员差价订单
                                OrderParent orderParent = new OrderParent();
                                orderParent.setOrderId(orderFormId);
                                orderParent.setUserId(userInfo1.getId());
                                orderParent.setOrderPrice(orderPrice);
                                orderParent.setOrderPriceSon(orderPriceSon);
                                orderParent.setStatus(0);
                                orderParent.setOrderTimeCreate(new Date());
                                this.orderParentMapper.insert(orderParent);
                                UserMemberPriceInfo userMemberPriceInfo = new UserMemberPriceInfo();
                                userMemberPriceInfo.setInfo("支付下线订单:"+orderForm.getOrderNumber());
                                userMemberPriceInfo.setPrice(orderPrice);
                                userMemberPriceInfo.setTime(new Date());
                                userMemberPriceInfo.setType(1);
                                userMemberPriceInfo.setUserId(userInfo.getId());
                                this.userMemberPriceInfoMapper.insert(userMemberPriceInfo);
                            }else {
                                //不足扣除
                                UserRemind userRemind = new UserRemind();
                                userRemind.setOrderId(orderFormId);
                                userRemind.setStatus(0);
                                userRemind.setUserId(userInfo1.getId());
                                userRemind.setTime(new Date());
                                userRemind.setInfo("因您的会员预存金额不足，有一个订单未进行差价代理");
                                this.userRemindMapper.insert(userRemind);
                            }
                        }
                    }
                }
            }
            //判断该订单是否支付保证金且返回保证金到用户余额
            if(orderForm.getCashDepositType()==1){
                orderForm.setCashDepositType(2);
                UserPriceInfo userPriceInfo = new UserPriceInfo();
                userPriceInfo.setType("+");
                userPriceInfo.setPrice(orderForm.getCashDepositPrice());
                userPriceInfo.setUserId(orderForm.getUserId());
                userPriceInfo.setInfo("订单完成保证金返还");
                this.userPriceInfoMapper.insert(userPriceInfo);
                UserInfo userInfo = userInfoMapper.selectById(orderForm.getUserId());
                userInfo.setPrice(userInfo.getPrice().add(orderForm.getCashDepositPrice()));
                this.userInfoMapper.updateById(userInfo);
            }
            //判断是否使用优惠卷 使用后修改优惠卷使用后状态为 1：使用后已付款
            if (orderForm.getOrderCouponId()!=null){
                Coupon coupon = couponMapper.selectById(orderForm.getOrderCouponId());
                coupon.setEmployStatus(1);
                this.couponMapper.updateById(coupon);
            }
            //发送订阅提醒
//            wxPayService.subscription(userInfoMapper.selectById(orderForm.getUserId()).getOpenId(),orderForm.getOrderNumber(),"订单状态更正为已付款");
        }else if(orderStatus.equals(3)){
            QueryWrapper<OrderParent> orderParentQueryWrapper = new QueryWrapper<>();
            orderParentQueryWrapper.eq("order_id",orderFormId);
            List<OrderParent> orderParentList = orderParentMapper.selectList(orderParentQueryWrapper);
            if (orderParentList.size()>0){//判断当前订单是否拥有上线订单
                OrderParent orderParent = orderParentList.get(0);
                UserInfo userInfo = userInfoMapper.selectById(orderParent.getUserId());
                userInfo.setWithdrawPrice(userInfo.getWithdrawPrice().add(orderParent.getOrderPriceSon()));
                orderParent.setStatus(1);
                this.orderParentMapper.updateById(orderParent);
                this.userInfoMapper.updateById(userInfo);
            }
            //判断该订单是否有返现红包 且返现到初始拥有者和使用者余额
            if (orderForm.getOrderCouponId()!=null){
                Coupon coupon = couponMapper.selectById(orderForm.getOrderCouponId());
                //判断使用的优惠卷是否为返现卷 若是执行返现
                if (coupon.getType()==1){
                    List<String> userIds = JSON.parseArray(coupon.getAgoUserId(), String.class);
                    // 使用者
                    UserInfo userInfo1 = userInfoMapper.selectById(orderForm.getUserId());
                    userInfo1.setPrice(userInfo1.getPrice().add(coupon.getSubtract()));
                    userInfoMapper.updateById(userInfo1);
                    UserPriceInfo userPriceInfo1 = new UserPriceInfo();
                    userPriceInfo1.setUserId(userInfo1.getId());
                    userPriceInfo1.setType("+");
                    userPriceInfo1.setPrice(coupon.getSubtract());
                    userPriceInfo1.setInfo("返现红包返现");
                    userPriceInfoMapper.insert(userPriceInfo1);
                    // 判断使用者与初始拥有者是否为同一人，若为同一人则不返现初始拥有者
                    if (!orderForm.getUserId().equals(userIds.get(0))){
                        // 初始拥有者
                        UserInfo userInfo = userInfoMapper.selectById(userIds.get(0));
                        userInfo.setPrice(userInfo.getPrice().add(coupon.getSubtract()));
                        userInfoMapper.updateById(userInfo);
                        UserPriceInfo userPriceInfo2 = new UserPriceInfo();
                        userPriceInfo2.setUserId(userInfo.getId());
                        userPriceInfo2.setType("+");
                        userPriceInfo2.setPrice(coupon.getSubtract());
                        userPriceInfo2.setInfo("返现红包返现");
                        userPriceInfoMapper.insert(userPriceInfo2);
                    }
                }
                //使用后修改优惠卷使用后状态为 2：使用后已返现
                coupon.setEmployStatus(2);
                this.couponMapper.updateById(coupon);
            }
            // 修改该地址订单下的所有地址为已收货
            OrderAddress orderAddress = new OrderAddress();
            orderAddress.setOrderAddressType(2);
            orderAddressMapper.update(orderAddress,new QueryWrapper<OrderAddress>().eq("order_id",orderForm.getId()));
        }
//        this.lookOrder(orderFormId,0);
        orderForm.setIfLook(0);
        orderForm.setOrderStatus(orderStatus);
        return orderFormMapper.updateById(orderForm);
    }

    /**
     * 根据orderAddressShopStatus查询OrderAddress
     * @param pageNum
     * @param map
     * @return
     */
    public PageInfo selectOrderAddressList(Integer pageNum,Map<String,String> map){
        Page page = new Page(pageNum,10);
        IPage<OrderAddress> orderAddressList = orderAddressMapper.selectOrderAddressList(page,map);
        PageInfo pageInfo = new PageInfo(orderAddressList);
        return pageInfo;
    }

    /**
     * 根据id修改orderAddress
     * @param orderAddress
     * @return
     */
    public Integer updateOrderAddress(OrderAddress orderAddress){
        return orderAddressMapper.updateById(orderAddress);
    }

    /**
     * 后台
     * 修改订单地址
     * @param map
     * @return
     */
    @Transactional(value="txManager1")
    public Integer updateOrderAddressPc(Map<String,String> map){
        OrderAddress orderAddress = orderAddressMapper.selectById(map.get("orderAddressId"));
        OrderForm orderForm = orderFormMapper.selectById(orderAddress.getOrderId());
        orderAddress.setCompanyName(map.get("companyName"));
        orderAddress.setUserName(map.get("userName"));
        orderAddress.setUserPhone(map.get("userPhone"));
        orderAddress.setUserFax(map.get("userFax"));
        orderAddress.setOrderAddressStatus(Integer.parseInt(map.get("orderAddressStatus")));
        BigDecimal freight1 = orderAddress.getOrderAddressFreight();
        BigDecimal freight2 = new BigDecimal(0);
        if (orderAddress.getOrderAddressStatus()==0) {//修改为单地址状态
            orderAddress.setUserAddress(map.get("userAddress"));
            orderAddress.setUserDetailAddress(map.get("userDetailAddress"));
            BigDecimal addressTotalRoughWeight = new BigDecimal(orderAddress.getOrderAddressRoughWeight());
            String[] address = orderAddress.getUserAddress().split(" ");
            Map<String,String> cityIdMap = new HashMap<>();
            cityIdMap.put("province",address[0]);
            cityIdMap.put("city",address[1]);
            String cityId = hatCityMapper.cityId(cityIdMap);
            String freights = freightMapper.value(cityId);
            List<String> freightList = JSONObject.parseArray(freights,String.class);
            String freightPrice = null;
            for (int l=0;l<freightList.size();l++){
                Map<String,Object> valueMap = JSONObject.parseObject(freightList.get(l));
                String[] names = valueMap.get("name").toString().split("-");
                if (addressTotalRoughWeight.compareTo(new BigDecimal(names[0]))==1){//大于
                    if (names.length==2){
                        if (addressTotalRoughWeight.compareTo(new BigDecimal(names[1]))==-1){//小于
                            freightPrice=valueMap.get("value").toString();
                            break;
                        }
                    }else {
                        freightPrice=valueMap.get("value").toString();
                        break;
                    }
                }
            }
            orderAddress.setOrderAddressFreightPrice(new BigDecimal(freightPrice));
            orderAddress.setOrderAddressFreight(orderAddress.getOrderAddressFreightPrice().multiply(addressTotalRoughWeight));
            orderForm.setOrderFreight(orderAddress.getOrderAddressFreight());
            orderForm.setOrderPrice(orderForm.getProductPrice().add(orderForm.getOrderFreight()));
            freight2=freight2.add(orderAddress.getOrderAddressFreight());
        }else if (orderAddress.getOrderAddressStatus()==1) {//修改为自提
            orderAddress.setUserAddress(null);
            orderAddress.setUserDetailAddress(null);
            orderAddress.setOrderAddressFreightPrice(null);
            orderAddress.setOrderAddressFreight(null);
            orderForm.setOrderFreight(orderAddress.getOrderAddressFreight());
            orderForm.setOrderPrice(orderForm.getProductPrice());
            freight2=new BigDecimal(0);
        }
        UserInfo userInfo = userInfoMapper.selectById(orderForm.getUserId());
        UserPriceInfo userPriceInfo = new UserPriceInfo();
        freight2 = freight2.setScale(2, BigDecimal.ROUND_HALF_UP);
        if (freight1.compareTo(freight2)==-1){//freight1小于freight2
            if (userInfo.getPrice().compareTo(freight2)==-1) return 501;
            BigDecimal freight3 = freight2.subtract(freight1);
            userPriceInfo.setUserId(userInfo.getId());
            userPriceInfo.setType("-");
            userPriceInfo.setPrice(freight3);
            userPriceInfo.setInfo("补运费差价");
            this.userPriceInfoMapper.insert(userPriceInfo);
            userInfo.setPrice(userInfo.getPrice().subtract(freight3));
            this.userInfoMapper.updateById(userInfo);
        }else if(freight1.compareTo(freight2)==0){//freight1等于freight2
        }else if(freight1.compareTo(freight2)==1){//freight1大于freight2
            BigDecimal freight3 = freight1.subtract(freight2);
            userPriceInfo.setUserId(userInfo.getId());
            userPriceInfo.setType("+");
            userPriceInfo.setPrice(freight3);
            userPriceInfo.setInfo("运费差价退还");
            this.userPriceInfoMapper.insert(userPriceInfo);
            userInfo.setPrice(userInfo.getPrice().add(freight3));
            this.userInfoMapper.updateById(userInfo);
        }
        Integer i = orderAddressMapper.updateById(orderAddress);
        if (i<0) return i;
        i = orderFormMapper.updateById(orderForm);
        return i;
    }

    /**
     * 分页查询下线订单
     * @param map
     * @return
     */
    public PageInfo orderParentListPage(Map<String,String> map){
        QueryWrapper<OrderParent> orderParentQueryWrapper = new QueryWrapper<>();
        orderParentQueryWrapper.eq("user_id",map.get("userId")).eq("status",map.get("status"));
        Page page = new Page(Integer.parseInt(map.get("pageNum")),10);
        IPage<Map<String,String>> orderParenMapList = orderParentMapper.selectMapsPage(page,orderParentQueryWrapper);
        for (Map<String,String> orderParenMap:orderParenMapList.getRecords()){
            OrderForm orderForm = orderFormMapper.selectById(orderParenMap.get("order_id"));
            orderParenMap.put("order_number",orderForm.getOrderNumber());
        }
        PageInfo pageInfo = new PageInfo(orderParenMapList);
        return pageInfo;
    }

    /**
     * 通过用户id查询下线订单收益
     * @param userId
     * @return
     */
    public Map<String,Object> orderParentEarnings(String userId){
        QueryWrapper<OrderParent> orderParentQueryWrapper0 = new QueryWrapper<>();
        orderParentQueryWrapper0.eq("user_id",userId).eq("status",0);
        List<OrderParent> orderParentList0 = orderParentMapper.selectList(orderParentQueryWrapper0);
        BigDecimal earnings0 = new BigDecimal("0.00");
        for (OrderParent orderParent:orderParentList0){
            BigDecimal earnings = orderParent.getOrderPriceSon().subtract(orderParent.getOrderPrice());
            earnings0 = earnings0.add(earnings);
        }
        QueryWrapper<OrderParent> orderParentQueryWrapper1 = new QueryWrapper<>();
        orderParentQueryWrapper1.eq("user_id",userId).eq("status",1);
        List<OrderParent> orderParentList1 = orderParentMapper.selectList(orderParentQueryWrapper1);
        BigDecimal earnings1 = new BigDecimal("0.00");
        for (OrderParent orderParent:orderParentList1){
            BigDecimal earnings = orderParent.getOrderPriceSon().subtract(orderParent.getOrderPrice());
            earnings1 = earnings1.add(earnings);
        }
        Map<String,Object> map = new HashMap<>();
        map.put("ongoing",earnings0);map.put("complete",earnings1);
        return map;
    }

    /**
     * 通过合同订单id查询所有型号
     * @param orderId
     * @return
     */
    public OrderForm orderFormById(String orderId){
        OrderForm orderForm = orderFormMapper.selectById(orderId);
        if (orderForm==null) return null;
        QueryWrapper<OrderModel> orderModelQueryWrapper = new QueryWrapper<>();
        orderModelQueryWrapper.eq("order_model_status",1).eq("order_id",orderId);
        orderForm.setOrderModelList(orderModelMapper.selectList(orderModelQueryWrapper));
        return orderForm;
    }
}
