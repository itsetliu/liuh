package com.cosmo.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cosmo.dao.*;
import com.cosmo.entity.*;
import com.cosmo.pdf.PDFExportConfig;
import com.cosmo.pdf.PDFUtil;
import com.cosmo.util.*;
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
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

@Service
public class OrderService {

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

    /**
     * PDF 文件导出
     *
     * @return
     */
    public ResponseEntity<?> export(Integer orderId) {
        OrderForm orderForm = this.orderFormMapper.selectById(orderId);
        QueryWrapper<OrderModel> orderModelQueryWrapper = new QueryWrapper<>();
        orderModelQueryWrapper.eq("order_id",orderId).eq("order_model_status",1);
        orderForm.setOrderModelList(this.orderModelMapper.selectList(orderModelQueryWrapper));
        QueryWrapper<OrderAddress> orderAddressQueryWrapper = new QueryWrapper<>();
        orderAddressQueryWrapper.eq("order_id",orderId).eq("order_address_type",0);
        OrderAddress orderAddress = this.orderAddressMapper.selectList(orderAddressQueryWrapper).get(0);
        HttpHeaders headers = new HttpHeaders();
        SimpleDateFormat ft = new SimpleDateFormat("yyyy年MM月dd日");
        /**
         * 数据导出(PDF 格式)
         */
        Map<String, Object> dataMap = new HashMap<>(16);
        dataMap.put("nowTime",ft.format(orderForm.getOrderTimeCreate()));
        dataMap.put("orderTimeDelivery",ft.format(orderForm.getOrderTimeDelivery()));
        dataMap.put("company",orderAddress.getCompanyName());
        dataMap.put("orderNumber",orderForm.getOrderNumber());
        if (orderAddress.getOrderAddressStatus()==0){
            dataMap.put("userAddress",orderAddress.getUserAddress()+orderAddress.getUserDetailAddress());
        }else if (orderAddress.getOrderAddressStatus()==1){
            dataMap.put("userAddress","自提");
        }else if (orderAddress.getOrderAddressStatus()==2){
            dataMap.put("userAddress","待定");
        }
        dataMap.put("userName",orderAddress.getUserName());
        dataMap.put("userPhone",orderAddress.getUserPhone());
        dataMap.put("fax",orderAddress.getUserFax());
        dataMap.put("orderPrice",orderAddress.getOrderAddressPrice().add(orderAddress.getOrderAddressFreight()).toString());
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
     * 新增商品到购物车
     * @param map
     * @return
     */
    public Integer addOrderModel(Map<String,String> map){
        BigDecimal boxWeigth = new BigDecimal(this.getConfigValue("boxWeigth"));
        BigDecimal trayWeigth = new BigDecimal(this.getConfigValue("trayWeigth"));
        Integer shopTrolleyTime = Integer.parseInt(this.getConfigValue("shopTrolleyTime"));
        OrderModel orderModel = new OrderModel();
        orderModel.setUserId(Long.valueOf(map.get("userId")));
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
        if (Integer.parseInt(map.get("cartonType"))==1)orderModel.setCartonInfo(FileUtil.upload(FileUtil.base64ToMultipart(map.get("cartonInfo"))));
        orderModel.setCartonPipeNumber(Integer.parseInt(map.get("cartonPipeNumber")));
        orderModel.setCartonNumber(Integer.parseInt(map.get("cartonNumber")));
        orderModel.setLabelType(Integer.parseInt(map.get("labelType")));
        if (Integer.parseInt(map.get("labelType"))==1)orderModel.setLabelInfo(FileUtil.upload(FileUtil.base64ToMultipart(map.get("labelInfo"))));
        orderModel.setTrayType(Integer.parseInt(map.get("trayType")));
        orderModel.setTrayNumber(Integer.parseInt(map.get("trayNumber")));
        orderModel.setTrayModel("标准");
        orderModel.setTrayCapacity(Integer.parseInt(map.get("trayCapacity")));
        orderModel.setRollNumber(Integer.parseInt(map.get("rollNumber")));
        orderModel.setRollRoughWeight(map.get("rollRoughWeight"));
        BigDecimal modelTotalRoughWeight = new BigDecimal(map.get("rollNumber")).multiply(new BigDecimal(map.get("rollRoughWeight")))
                .add(boxWeigth.multiply(new BigDecimal(map.get("cartonNumber"))))
                .add(trayWeigth.multiply(new BigDecimal(map.get("trayNumber"))));
        orderModel.setModelTotalRoughWeight(modelTotalRoughWeight.toString());
        BigDecimal modelTotalSuttle = new BigDecimal(map.get("rollNumber")).multiply(new BigDecimal(map.get("specSuttle")));
        orderModel.setModelTotalSuttle(modelTotalSuttle.toString());
        orderModel.setModelUnitPrice(new BigDecimal(map.get("modelUnitPrice")));
        orderModel.setModelTotalPrice(new BigDecimal(map.get("modelTotalPrice")));
        orderModel.setModelProcessCost(new BigDecimal(map.get("modelProcessCost")));
        orderModel.setModelRawPrice(new BigDecimal(map.get("modelRawPrice")));
        orderModel.setModelRawPriceType(Integer.parseInt(map.get("modelRawPriceType")));
        orderModel.setMemberId(Long.valueOf(map.get("memberId")));
        orderModel.setMemberDiscount(new BigDecimal(map.get("memberDiscount")));
        if ("2".equals(map.get("modelRawPriceType"))) orderModel.setUserLockId(Long.valueOf(map.get("userLockId")));
        orderModel.setOrderModelStatus(0);
        Date nowDate = new Date();
        nowDate.setTime(nowDate.getTime()+shopTrolleyTime);
        orderModel.setOrderModelExpireTime(sdf.format(nowDate));
        Integer i = orderModelMapper.insert(orderModel);
        //把购物车编号存到redis中
        redisUtil.set("shopTrolley,"+orderModel.getId(),"shopTrolley,"+orderModel.getId());
        redisUtil.expire("shopTrolley,"+orderModel.getId(),shopTrolleyTime, TimeUnit.SECONDS);
        return i;
    }

    /**
     * 根据orderModelId删除购物车订单
     * @param orderModelId
     * @return
     */
    public Integer delOrderModel(Integer orderModelId){
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
     * 生成合同订单
     * @param map
     * @return
     *  201:会员预存余额不足支付本次交易，请续费会员或调整购物车后生成订单
     */
    @Transactional(value="txManager1")
    public Integer createOrderForm(Map<String,String> map){
        UserInfo userInfo = userInfoMapper.selectById(Long.valueOf(map.get("userId")));
        Integer i = 0;

        //初步新建合同订单获取orderFormId
        OrderForm orderForm = new OrderForm();
        orderForm.setUserId(userInfo.getId());
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd");
        String create = sdf1.format(new Date())+" 00:00:00";
        String end = sdf1.format(this.getNextsDay(new Date(),1))+" 00:00:00";
        Map<String,String> time = new HashMap<>();
        time.put("create",create);
        time.put("end",end);
        int count = orderFormMapper.count(time)+1;
        String orderNumber = "COSMO"+sdf.format(new Date())+"A"+String.format("%3d", count).replace(" ", "0");
        orderForm.setOrderNumber(orderNumber);
        orderForm.setInvoiceType(0);
        orderForm.setOrderRemark(map.get("orderRemark"));
        orderForm.setOrderStatus(0);
        orderForm.setOrderTimeCreate(new Date());
        i = orderFormMapper.insert(orderForm);
        if (i==0) return i;
        Integer day = 14;
        if ("0".equals(map.get("rawPriceType"))) day = 7;
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
        if (i==0) return i;
        List<Integer> orderModelIds = JSON.parseArray(map.get("orderModelIds"),Integer.class);
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
                if (userInfo.getMemberId()==0){
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
            String freightPrice = null;
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
        if (userInfo.getMemberId()!=0){
            if (userInfo.getMemberPrice().compareTo(orderForm.getOrderPrice())==-1){
                this.orderFormMapper.deleteById(orderForm.getId());
                this.orderAddressMapper.deleteById(orderAddress.getId());
                return 201;//会员预存余额不足支付本次交易，请续费会员或调整购物车后生成订单
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
            //删除购物车失效缓存
            redisUtil.delete("shopTrolley,"+orderModels.getId());
        });
        if (ii[0]<=0) return ii[0];
        if (i==0) return i;
        i = orderAddressMapper.updateById(orderAddress);
        if (i==0) return i;
        i = orderFormMapper.updateById(orderForm);
        if (i==0) return i;
        return i;
    }

    /**
     * 锁价子订单生成合同订单
     * @param map
     * @return
     *      201:所选购物车型号不是同次锁价数据
     *      202:超过差价上限，请重新整理购物车
     *      203:用户余额不足已补差价，请充值后再次操作
     *      204:用户余额不足已补差价及支付运费，请充值后再次操作
     */
    @Transactional(value="txManager1")
    public Integer createOrderForm1(Map<String,String> map){
        UserInfo userInfo = userInfoMapper.selectById(Integer.parseInt(map.get("userId")));
        List<Integer> orderModelIds = JSON.parseArray(map.get("orderModelIds"),Integer.class);
        QueryWrapper<OrderModel> orderModelQueryWrapper = new QueryWrapper<>();
        orderModelQueryWrapper.in("id",orderModelIds);
        List<OrderModel> orderModelList = orderModelMapper.selectList(orderModelQueryWrapper);
        BigDecimal[] orderAddressRoughWeight = {new BigDecimal(0)};
        BigDecimal[] modelTotalPrice = {new BigDecimal(0)};
        int[] trayNumber = {0};
        Long userLockId = orderModelList.get(0).getUserLockId();
        boolean[] userLockIdSame = {false};
        orderModelList.forEach(orderModels -> {
            if (userLockId!=orderModels.getUserLockId()) {
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
        BigDecimal upperLimit = new BigDecimal(0);
        if (modelTotalPrice[0].compareTo(userLock.getBalance())==1) {
            if (modelTotalPrice[0].compareTo(userLock.getBalance().add(lockPriceUpperLimit))==1) {
                return 202;//超过差价上限，请重新整理购物车
            }else {
                upperLimit = userLock.getBalance().subtract(modelTotalPrice[0]);
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
        //初步新建合同订单获取orderFormId
        OrderForm orderForm = new OrderForm();
        orderForm.setUserId(Long.valueOf(map.get("userId")));
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd");
        String create = sdf1.format(new Date())+" 00:00:00";
        String end = sdf1.format(this.getNextsDay(new Date(),1))+" 00:00:00";
        Map<String,String> time = new HashMap<>();
        time.put("create",create);
        time.put("end",end);
        int count = orderFormMapper.count(time)+1;
        String orderNumber = "COSMO"+sdf.format(new Date())+"A"+String.format("%3d", count).replace(" ", "0");
        orderForm.setOrderNumber(orderNumber);
        orderForm.setInvoiceType(0);
        orderForm.setOrderRemark(map.get("orderRemark"));
        orderForm.setOrderStatus(0);
        orderForm.setOrderTimeCreate(new Date());
        Integer day = 14;
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
            i = orderAddressMapper.updateById(orderAddress);
            if (i==0) return i;
            //给订单合同新增 运费、产品总金额、订单总金额、产品总毛重、订单托盘总数
            orderForm.setOrderFreight(orderAddress.getOrderAddressFreight());
            orderForm.setProductPrice(orderAddress.getOrderAddressPrice());
            orderForm.setOrderPrice(orderForm.getProductPrice().add(orderForm.getOrderFreight()));
            orderForm.setOrderWeight(orderAddress.getOrderAddressRoughWeight());
            orderForm.setOrderTotalTrayNumber(trayNumber[0]);
            i = orderFormMapper.updateById(orderForm);
            if (i==0) return i;
        }else if (orderAddressStatus==1||orderAddressStatus==2) {//自提状态 或 地址待定状态
            //给订单地址新增 运费单价、运费、产品总毛重、产品总金额
            orderAddress.setOrderAddressFreightPrice(new BigDecimal(0));
            orderAddress.setOrderAddressFreight(new BigDecimal(0));
            orderAddress.setOrderAddressRoughWeight(orderAddressRoughWeight[0].toString());
            orderAddress.setOrderAddressPrice(modelTotalPrice[0]);
            orderForm.setOrderTotalTrayNumber(trayNumber[0]);
            i = orderAddressMapper.updateById(orderAddress);
            if (i==0) return i;
            //给订单合同新增 运费、产品总金额、订单总金额、产品总毛重、订单托盘总数
            orderForm.setOrderFreight(new BigDecimal(0));
            orderForm.setProductPrice(orderAddress.getOrderAddressPrice());
            orderForm.setOrderPrice(orderForm.getProductPrice().add(orderForm.getOrderFreight()));
            orderForm.setOrderWeight(orderAddress.getOrderAddressRoughWeight());
            orderForm.setOrderTotalTrayNumber(trayNumber[0]);
            i = orderFormMapper.updateById(orderForm);
            if (i==0) return i;
        }
        //扣除订单金额
        if (upperLimit.compareTo(new BigDecimal(0))==0){
            userLock.setBalance(userLock.getBalance().subtract(modelTotalPrice[0]));
        }else {
            userLock.setBalance(new BigDecimal(0));
            userInfo.setPrice(userInfo.getPrice().subtract(upperLimit));
        }
        if (userLock.getBalance().compareTo(new BigDecimal(0))==0) userLock.setStatus(4);
        this.userLockMapper.updateById(userLock);
        this.userInfoMapper.updateById(userInfo);
        return i;
    }

    /**
     * 给待定订单新增地址
     * @param map
     * @return
     */
    @Transactional(value="txManager1")
    public Integer addOrderAddress(Map<String,String> map){
        BigDecimal boxWeigth = new BigDecimal(this.getConfigValue("boxWeigth"));
        BigDecimal trayWeigth = new BigDecimal(this.getConfigValue("trayWeigth"));
        BigDecimal trayPrice = new BigDecimal(this.getConfigValue("trayPrice"));
        UserInfo userInfo = userInfoMapper.selectById(Integer.parseInt(map.get("userId")));
        OrderForm orderForm = orderFormMapper.selectById(Integer.parseInt(map.get("orderFormId")));
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
            OrderModel orderModel2 = orderModelMapper.selectById((Integer) stringMap.get("orderModelId"));
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
                    .add(boxWeigth.multiply(new BigDecimal(orderModel1.getCartonNumber())))
                    .add(trayWeigth.multiply(new BigDecimal(orderModel1.getTrayNumber())));
            orderModel1.setModelTotalRoughWeight(modelTotalRoughWeight1.toString());
            orderModel1.setModelTotalSuttle((new BigDecimal(orderModel1.getSpecSuttle()).multiply(new BigDecimal(orderModel1.getRollNumber()))).toString());
            orderModel1.setModelTotalPrice(new BigDecimal(orderModel1.getModelTotalSuttle()).multiply(orderModel1.getModelUnitPrice())
                    .add(trayPrice.multiply(new BigDecimal(orderModel1.getTrayNumber()))));
            orderModelList1.add(orderModel1);
            BigDecimal modelTotalRoughWeight2 = new BigDecimal(orderModel2.getRollNumber()).multiply(new BigDecimal(orderModel2.getRollRoughWeight()))
                    .add(boxWeigth.multiply(new BigDecimal(orderModel2.getCartonNumber())))
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
            if (userInfo.getPrice().compareTo(orderAddress2.getOrderAddressFreight())==-1) return 502;//余额不足支付本次运费
            totalPrice=totalPrice.add(orderAddress2.getOrderAddressFreight());
            info = "扣除新地址运费 ";
        }
        QueryWrapper<OrderModel> orderModelQueryWrapper = new QueryWrapper<>();
        orderModelQueryWrapper.eq("order_id",orderAddress1.getId());
        List<OrderModel> orderModelList3 = orderModelMapper.selectList(orderModelQueryWrapper);
        boolean allNull[] = {false};
        orderModelList3.forEach(orderModel3 -> {
            if (orderModel3.getRollNumber()==0) allNull[0]=true;
        });
        if (allNull[0]){//判断订单下所有型号是否全部用完
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
                if (userInfo.getPrice().compareTo(totalPrice)==-1) return 503;//余额不足支付该订单的托盘差价
                info = info+"扣除托盘差价";
            }
        }
        UserPriceInfo userPriceInfo = new UserPriceInfo();
        userPriceInfo.setUserId(userInfo.getId());
        userPriceInfo.setType("-");
        userPriceInfo.setPrice(totalPrice);
        userPriceInfo.setInfo(info);
        this.userPriceInfoMapper.insert(userPriceInfo);
        userInfo.setPrice(userInfo.getPrice().subtract(totalPrice));
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
    public PageInfo orderFormPageList(Integer pageNum,Integer orderStatus,Integer userId){
        QueryWrapper<OrderForm> orderFormQueryWrapper = new QueryWrapper<>();
        orderFormQueryWrapper.eq("order_status",orderStatus)
                .eq("user_id",userId).orderByDesc("id");
        Page page = new Page(pageNum,10);
        IPage<OrderForm> orderFormList = orderFormMapper.selectPage(page,orderFormQueryWrapper);
        PageInfo pageInfo = new PageInfo(orderFormList);
        for (OrderForm orderForm : (List<OrderForm>)pageInfo.getList()){
            QueryWrapper<OrderAddress> orderAddressQueryWrapper = new QueryWrapper<>();
            orderAddressQueryWrapper.eq("order_id",orderForm.getId());
            List<OrderAddress> orderAddressList = orderAddressMapper.selectList(orderAddressQueryWrapper);
            orderForm.setOrderAddressList(orderAddressList);
        }
        return pageInfo;
    }

    /**
     * 根据状态查询订单合同
     * @param orderStatus
     * @return
     */
    public List<OrderForm> orderFormList(Integer orderStatus,Integer userId){
        QueryWrapper<OrderForm> orderFormQueryWrapper = new QueryWrapper<>();
        orderFormQueryWrapper.eq("order_status",orderStatus)
                .eq("user_id",userId).orderByDesc("id");
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
    public OrderForm orderFormInfo(Integer orderFormId){
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
    public Map<String,Object> orderFormInfo1(Integer orderFormId){
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
    public OrderAddress orderAddressInfo(Integer orderAddressId){
        OrderAddress orderAddress = orderAddressMapper.selectById(orderAddressId);
        QueryWrapper<OrderModel> orderModelQueryWrapper = new QueryWrapper<>();
        orderModelQueryWrapper.eq("order_id",orderAddressId).eq("order_model_status",2);
        List<OrderModel> orderModelList = orderModelMapper.selectList(orderModelQueryWrapper);
        orderAddress.setOrderModelList(orderModelList);
        return orderAddress;
    }

    /**
     * 修改合同订单的默认地址
     * @param map
     * @return
     */
    @Transactional(value="txManager1")
    public Integer updateOrderAddress(Map<String,String> map){
        OrderAddress orderAddress = orderAddressMapper.selectById(Integer.parseInt(map.get("orderAddressId")));
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
     *      3 当前红包不存在
     *      4 绑定失败
     * @param orderFormId
     * @param couponId
     * @return
     */
    public Integer bindCoupon(Integer orderFormId,Long couponId){
        OrderForm orderForm = orderFormMapper.selectById(orderFormId);
        if (orderForm==null) return 1;
        if (orderForm.getOrderStatus()!=1) return 2;
        Coupon coupon = couponMapper.selectById(couponId);
        if (coupon==null) return 2;
        orderForm.setOrderCouponId(couponId);
        return orderFormMapper.updateById(orderForm);
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
        orderFormQueryWrapper.eq("order_status",map.get("orderStatus"));
        if (!"".equals(map.get("orderNumber"))||map.get("orderNumber")!=null) orderFormQueryWrapper.like("order_number",map.get("orderNumber"));
        Page page = new Page(pageNum,10);
        IPage<OrderForm> orderFormList = orderFormMapper.selectPage(page,orderFormQueryWrapper);
        PageInfo pageInfo = new PageInfo(orderFormList);
        return pageInfo;
    }

    /**
     * 后台
     * 根据orderId（上级id）和orderModelStatus查询订单子属型号列表
     * @param orderId
     * @return
     */
    public List<OrderModel> orderModelList(Integer orderId){
        QueryWrapper<OrderAddress> orderAddressQueryWrapper = new QueryWrapper<>();
        orderAddressQueryWrapper.eq("order_id",orderId).eq("order_address_type",0);
        OrderAddress orderAddress = orderAddressMapper.selectList(orderAddressQueryWrapper).get(0);
        QueryWrapper<OrderModel> orderModelQueryWrapper = new QueryWrapper<>();
        if (orderAddress.getOrderAddressStatus()==0||orderAddress.getOrderAddressStatus()==1){
            orderModelQueryWrapper.eq("order_id",orderAddress.getId()).eq("order_model_status",2);
        }else {
            orderModelQueryWrapper.eq("order_id",orderAddress.getId()).eq("order_model_status",1);
        }
        return orderModelMapper.selectList(orderModelQueryWrapper);
    }

    /**
     * 后台
     * 根据orderId（上级id）和orderModelStatus查询订单子属型号列表
     * @param orderId
     * @param orderModelStatus
     * @return
     */
    public List<OrderModel> orderModelList1(Integer orderId,Integer orderModelStatus){
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
    public List<OrderAddress> orderAddressList(Integer orderId,Integer orderAddressType ){
        QueryWrapper<OrderAddress> orderAddressQueryWrapper = new QueryWrapper<>();
        orderAddressQueryWrapper.eq("order_id",orderId).eq("order_address_type",orderAddressType);
        return orderAddressMapper.selectList(orderAddressQueryWrapper);
    }

    /**
     * 根据id修改orderForm
     * @param map
     * @return
     */
    public Integer updateOrderForm(Map<String,String> map){
        Long orderFormId = Long.valueOf(map.get("orderFormId"));
        Integer orderStatus = Integer.parseInt(map.get("orderStatus"));
        OrderForm orderForm = orderFormMapper.selectById(orderFormId);
        //判断是否存在redis缓存，存在及删除
        if (redisUtil.hasKey("intentionGold,"+orderForm.getOrderNumber())) redisUtil.delete("intentionGold,"+orderForm.getOrderNumber());
        if (redisUtil.hasKey("orderCode,"+orderForm.getOrderNumber())) redisUtil.delete("orderCode,"+orderForm.getOrderNumber());
        // 0待付保证金，1待付款，2未完成，3已完成，4已取消
        if(orderStatus.equals(0)){
            Integer orderTime = Integer.parseInt(this.getConfigValue("intentionGoldTime"));
            redisUtil.set("intentionGold,"+orderForm.getOrderNumber(),"intentionGold,"+orderForm.getOrderNumber());
            redisUtil.expire("intentionGold,"+orderForm.getOrderNumber(),orderTime, TimeUnit.SECONDS);
        }else if(orderStatus.equals(1)){
            Integer orderTime = Integer.parseInt(this.getConfigValue("orderTime"));
            redisUtil.set("orderCode,"+orderForm.getOrderNumber(),"orderCode,"+orderForm.getOrderNumber());
            redisUtil.expire("orderCode,"+orderForm.getOrderNumber(),orderTime, TimeUnit.SECONDS);
        }else if(orderStatus.equals(2)){
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
                    QueryWrapper<OrderModel> orderModelQueryWrapper = new QueryWrapper<>();
                    orderModelQueryWrapper.eq("order_id",orderFormId).eq("order_model_status",1);
                    List<OrderModel> orderModels = orderModelMapper.selectList(orderModelQueryWrapper);
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
        }
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
        OrderAddress orderAddress = orderAddressMapper.selectById(Integer.parseInt(map.get("orderAddressId")));
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
}