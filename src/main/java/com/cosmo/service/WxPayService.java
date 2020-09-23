package com.cosmo.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.cosmo.dao.*;
import com.cosmo.entity.*;
import com.cosmo.util.*;
import com.cosmo.wx.AesCbcUtil;
import com.cosmo.wx.HttpUtils;
import com.cosmo.wx.PayUtil;
import com.cosmo.wx.PaymentPo;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class WxPayService {

    @Value("${wechat.appid}")
    private String appid;
    @Value("${wechat.mchId}")
    private String mchId;
    @Value("${wechat.mchKey}")
    private String mchKey;
    @Value("${wechat.notifyUrl}")
    private String notifyUrl;
    @Value("${wechat.appSecret}")
    private String appSecret;

    private String tradeType = "JSAPI";
    private String payUrl = "https://api.mch.weixin.qq.com/pay/unifiedorder";
    private String payIndividualUrl = "https://api.mch.weixin.qq.com/mmpaymkttransfers/promotion/transfers";


    public final static String getPageOpenidUrl = "https://api.weixin.qq.com/sns/jscode2session";



    private SimpleDateFormat ft = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    @Resource
    private PayWxMapper payWxMapper;
    @Resource
    private UserInfoMapper userInfoMapper;
    @Resource
    private UserMemberMapper userMemberMapper;
    @Resource
    private OrderFormMapper orderFormMapper;
    @Resource
    private UserPriceInfoMapper userPriceInfoMapper;
    @Resource
    private UserLockMapper userLockMapper;
    @Resource
    private ConfigMapper configMapper;
    @Resource
    private RedisUtil redisUtil;



    /**
     * 新增微信支付记录表
     * @param payWx
     * @return
     */
    public Integer addPayWx(PayWx payWx){
        return payWxMapper.insert(payWx);
    }

    /**
     * 微信获取用户信息
     * @param code
     * @param encryptedData
     * @param iv
     * @return
     */
    public Map<String, String> login(String code, String encryptedData, String iv){
        Map<String, String> map = this.getOpenId(code);
        Map<String, String> map1 = new HashMap<>();
        try {
            String result1 = AesCbcUtil.decrypt(encryptedData, map.get("sessionKey"), iv, "UTF-8");
            if (null != result1 && result1.length() > 0) {
                JSONObject userInfoJSON = JSONObject.parseObject(result1);
                String openId = userInfoJSON.getString("openId");
                String nickName = userInfoJSON.getString("nickName");
                Map<String,Object> userInfo1 = userInfoMapper.selectByOpenId(openId);
                UserInfo userInfo2 = new UserInfo();
                Integer i = null;
                Map<String,String> map2 = new HashMap<>();
                if (userInfo1!=null){
                    QueryWrapper<UserInfo> userInfoQueryWrapper = new QueryWrapper<>();
                    userInfoQueryWrapper.eq("open_id",openId);
                    userInfo2.setLoginCount(Integer.parseInt(userInfo1.get("login_count").toString())+1);
                    userInfo2.setLastLoginTime(ft.format(new Date()));
                    userInfo2.setWxName(nickName);
                    i = userInfoMapper.update(userInfo2,userInfoQueryWrapper);
                }else {
                    userInfo2.setLastLoginTime(ft.format(new Date()));
                    userInfo2.setLoginCount(1);
                    userInfo2.setCreateTime(ft.format(new Date()));
                    userInfo2.setWxName(nickName);
                    userInfo2.setOpenId(openId);
                    userInfo2.setPrice(new BigDecimal(0));
                    userInfo2.setGoldCoin(0);
                    Random random = new Random();
                    String result="";
                    for (int l=0;l<6;l++){result+=random.nextInt(10);}
                    userInfo2.setReferralCode(result);
                    i = userInfoMapper.insert(userInfo2);
                    userInfo1 = userInfoMapper.selectByOpenId(openId);
                }
                map2.put("status",String.valueOf(userInfo1.get("status")));
                Integer memberId = Integer.parseInt(userInfo1.get("member_id").toString());
                if (memberId==0) map2.put("memberName","无会员");
                else map2.put("memberName",String.valueOf(userMemberMapper.selectById(memberId).getName()));
                map2.put("userId", userInfo1.get("id").toString());
                map1.put("code", "200");
                map1.put("msg", "解密成功");
                map1.put("data", JSON.toJSONString(map2));
            } else {
                map.put("status", "500");
                map.put("msg", "解密失败");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return map1;
    }


    /**
     * 获取 openId 和 sessionKey
     * @param code
     * @return
     * @throws Exception
     */
    public Map<String,String> getOpenId(String code) {
        try{

            //请求参数
            String params = "appid=" + appid + "&secret=" + appSecret + "&js_code=" + code + "&grant_type=authorization_code";
            // 发送请求
            String urlNameString = getPageOpenidUrl + "?" + params;
            Connection conn = Jsoup.connect(urlNameString);
            conn.method(Connection.Method.GET).execute();
            JSONObject jsonObject = JSONObject.parseObject(conn.response().body());
            String wxOpenId = String.valueOf(jsonObject.getString("openid"));
            String wxSessionKey = String.valueOf(jsonObject.getString("session_key"));
            if(wxOpenId==null || wxSessionKey ==null){
                return null;
            }
            Map<String, String> map = new HashMap<>();
            map.put("openId",wxOpenId);
            map.put("sessionKey",wxSessionKey);
            return map;
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }


    @SuppressWarnings("rawtypes")
    private Map<String, String> goPay(PaymentPo paymentPo) throws Exception {
        //商品名称
        //String body = "测试商品名称";
        //金额元=paymentPo.getTotal_fee()*100
        String total_fee = String.valueOf(new BigDecimal(paymentPo.getTotal_fee()).multiply(new BigDecimal(100)).intValue());
        //组装参数，用户生成统一下单接口的签名
        Map<String, String> packageParams = new HashMap<String, String>();
        packageParams.put("appid", appid);
        packageParams.put("mch_id", mchId);
        packageParams.put("nonce_str", paymentPo.getNonce_str());
        packageParams.put("body", paymentPo.getBody());
        packageParams.put("out_trade_no", paymentPo.getOut_trade_no());//商户订单号
        packageParams.put("total_fee", total_fee);//支付金额，这边需要转成字符串类型，否则后面的签名会失败
        packageParams.put("notify_url", notifyUrl);//支付成功后的回调地址
        packageParams.put("trade_type", tradeType);//支付方式
        packageParams.put("openid", paymentPo.getOpenid());

        String prestr = PayUtil.createLinkString(packageParams); // 把数组所有元素，按照“参数=参数值”的模式用“&”字符拼接成字符串

        //MD5运算生成签名，这里是第一次签名，用于调用统一下单接口
        String mysign = PayUtil.sign(prestr, mchKey, "utf-8").toUpperCase();

        //拼接统一下单接口使用的xml数据，要将上一步生成的签名一起拼接进去
        String xml = "<xml>" + "<appid>" + appid + "</appid>"
                + "<body><![CDATA[" + paymentPo.getBody() + "]]></body>"
                + "<mch_id>" + mchId + "</mch_id>"
                + "<nonce_str>" + paymentPo.getNonce_str() + "</nonce_str>"
                + "<notify_url>" + paymentPo.getNotify_url() + "</notify_url>"
                + "<openid>" + paymentPo.getOpenid() + "</openid>"
                + "<out_trade_no>" + paymentPo.getOut_trade_no() + "</out_trade_no>"
                /* + "<spbill_create_ip>" + paymentPo.getSpbill_create_ip() + "</spbill_create_ip>" */
                + "<total_fee>" + total_fee + "</total_fee>"
                + "<trade_type>" + tradeType + "</trade_type>"
                + "<sign>" + mysign + "</sign>"
                + "</xml>";

        log.info("调试模式_统一下单接口 请求XML数据：" + xml);

        //调用统一下单接口，并接受返回的结果
        String res = PayUtil.httpRequest(payUrl, "POST", xml);

        log.info("调试模式_统一下单接口 返回XML数据：" + res);

        // 将解析结果存储在HashMap中
        Map map = PayUtil.doXMLParse(res);

        String return_code = (String) map.get("return_code");//返回状态码

        Map<String, String> result = new HashMap<String, String>();//返回给小程序端需要的参数
        String prepay_id = null;
        if(return_code=="SUCCESS"||return_code.equals(return_code)){

            prepay_id = (String) map.get("prepay_id");
            result.put("nonce_str", (String) map.get("nonce_str"));
            result.put("package", "prepay_id="+(String) map.get("prepay_id"));
            result.put("appid", (String) map.get("appid"));
            result.put("sign", (String) map.get("sign"));
            result.put("trade_type", (String) map.get("trade_type"));
            result.put("mch_id", (String) map.get("mch_id"));
            Long timeStamp = System.currentTimeMillis() / 1000;
            result.put("timeStamp", timeStamp + "");//这边要将返回的时间戳转化成字符串，不然小程序端调用wx.requestPayment方法会报签名错误
            //拼接签名需要的参数
            String stringSignTemp = "appId=" + appid + "&nonceStr=" + (String) map.get("nonce_str") + "&package=prepay_id=" + prepay_id+ "&signType=MD5&timeStamp=" + timeStamp;
            //再次签名，这个签名用于小程序端调用wx.requesetPayment方法
            String paySign = PayUtil.sign(stringSignTemp, mchKey, "utf-8").toUpperCase();
            result.put("paySign", paySign);
        }
        return result;
    }

   /**
     * 通过用户id 和 订单号 统一下单
     * @param userId
     * @param orderId
     * @return
     * @throws Exception
     */
   @Transactional(value="txManager1")
    public Map<String, String> wxPay(String userId, String orderId, Integer type) throws Exception {
        UserInfo userInfo = userInfoMapper.selectById(userId);
        OrderForm order = orderFormMapper.selectById(orderId);
        QueryWrapper<Config> configQueryWrapper = new QueryWrapper<>();
        configQueryWrapper.eq("code","cashDeposit");
        String cashDeposit = configMapper.selectList(configQueryWrapper).get(0).getValue();
        if (type==0){//微信支付
            PaymentPo paymentPo = new PaymentPo();
            paymentPo.setBody("支付意向金");
            paymentPo.setNonce_str(PayUtil.create_nonce_str());
            paymentPo.setNotify_url(notifyUrl);
            paymentPo.setOut_trade_no(order.getOrderNumber());
            paymentPo.setTotal_fee(cashDeposit);
            paymentPo.setOpenid(userInfo.getOpenId());
            QueryWrapper<PayWx> payWxQueryWrapper = new QueryWrapper<>();
            payWxQueryWrapper.eq("order_no",paymentPo.getOut_trade_no());
            List<PayWx> payWxes = payWxMapper.selectList(payWxQueryWrapper);
            if (payWxes.size()<=0){
                PayWx payWx = new PayWx();
                payWx.setBody(paymentPo.getBody());
    //            payWx.setNonceStr(paymentPo.getNonce_str());
                payWx.setOrderNo(paymentPo.getOut_trade_no());
                payWx.setTotalFee(paymentPo.getTotal_fee());
                payWx.setOpenId(paymentPo.getOpenid());
                payWx.setStatus(0);
                payWx.setType(0);
                this.addPayWx(payWx);
            }
            order.setCashDepositPrice(new BigDecimal(cashDeposit));
            this.orderFormMapper.updateById(order);
            return this.goPay(paymentPo);
        }else {//用户余额支付
            Map<String,String> map = new HashMap<>();
            if(userInfo.getPrice().compareTo(new BigDecimal(cashDeposit)) == -1) { map.put("msg","用户余额不足已支付意向金"); return map; }
            userInfo.setPrice(userInfo.getPrice().subtract(new BigDecimal(cashDeposit)));
            this.userInfoMapper.updateById(userInfo);
            UserPriceInfo userPriceInfo = new UserPriceInfo();
            userPriceInfo.setUserId(userInfo.getId());
            userPriceInfo.setType("-");
            userPriceInfo.setPrice(new BigDecimal(cashDeposit));
            userPriceInfo.setInfo("支付订单意向金");
            Integer i = userPriceInfoMapper.insert(userPriceInfo);
            if (i<=0) { map.put("msg","支付失败"); return map; }
            redisUtil.delete("orderCode"+order.getOrderNumber());
            order.setOrderStatus(1);
            order.setCashDepositType(1);
            order.setCashDepositPrice(new BigDecimal(cashDeposit));
            i = orderFormMapper.updateById(order);
            QueryWrapper<Config> configQueryWrapper1 = new QueryWrapper<>();
            configQueryWrapper1.eq("code","orderTime");
            Integer orderTime = Integer.parseInt(configMapper.selectList(configQueryWrapper1).get(0).getValue());
            redisUtil.set("orderCode,"+order.getOrderNumber(),"orderCode,"+order.getOrderNumber());
            redisUtil.expire("orderCode,"+order.getOrderNumber(),orderTime, TimeUnit.SECONDS);
            if (i<=0) { map.put("msg","支付失败"); return map; }
            map.put("msg","支付成功");
            return map;
        }

    }


    /**
     * 支付回调
     *       修改订单状态为1（待付款）
     *       完成余额充值
     *       修改锁价状态为2（待付款）
     * @param orderNumber
     * @return
     */
    public Integer updateStatue(String orderNumber,String openid,String totalFee){
        PayWx payWx = new PayWx();
        payWx.setStatus(1);
        QueryWrapper<PayWx> payWxQueryWrapper = new QueryWrapper<>();
        payWxQueryWrapper.eq("status",0);
        this.payWxMapper.update(payWx,payWxQueryWrapper);
        if ("COSMO".equals(orderNumber.substring(0,5))){
            redisUtil.delete("orderCode"+orderNumber);
            QueryWrapper<OrderForm> orderFormQueryWrapper = new QueryWrapper<>();
            orderFormQueryWrapper.eq("order_number",orderNumber);
            OrderForm order = orderFormMapper.selectList(orderFormQueryWrapper).get(0);
            order.setOrderStatus(1);
            order.setCashDepositType(1);
            QueryWrapper<Config> configQueryWrapper = new QueryWrapper<>();
            configQueryWrapper.eq("code","orderTime");
            Integer orderTime = Integer.parseInt(configMapper.selectList(configQueryWrapper).get(0).getValue());
            redisUtil.delete("intentionGold,"+order.getOrderNumber());
            redisUtil.set("orderCode,"+order.getOrderNumber(),"orderCode,"+order.getOrderNumber());
            redisUtil.expire("orderCode,"+order.getId(),orderTime, TimeUnit.SECONDS);
            return orderFormMapper.updateById(order);
        }else if ("VC".equals(orderNumber.substring(0,2))){
            QueryWrapper<UserInfo> userInfoQueryWrapper = new QueryWrapper<>();
            userInfoQueryWrapper.eq("open_id",openid);
            UserInfo userInfo = userInfoMapper.selectList(userInfoQueryWrapper).get(0);
            BigDecimal price = new BigDecimal(totalFee).divide(new BigDecimal(100));
            UserPriceInfo userPriceInfo = new UserPriceInfo();
            userPriceInfo.setUserId(userInfo.getId());
            userPriceInfo.setType("+");
            userPriceInfo.setPrice(price);
            userPriceInfo.setInfo("运费差价退还");
            this.userPriceInfoMapper.insert(userPriceInfo);
            userInfo.setPrice(userInfo.getPrice().add(price));
            return userInfoMapper.updateById(userInfo);
        }else if ("lockPrice".equals(orderNumber.substring(0,9))){
            QueryWrapper<UserLock> userLockQueryWrapper = new QueryWrapper<>();
            userLockQueryWrapper.eq("number",orderNumber);
            UserLock userLock = userLockMapper.selectList(userLockQueryWrapper).get(0);
            QueryWrapper<Config> configQueryWrapper = new QueryWrapper<>();
            configQueryWrapper.eq("code","lockTime");
            Integer lockTime = Integer.parseInt(configMapper.selectList(configQueryWrapper).get(0).getValue());
            redisUtil.delete("lockPrice1,"+userLock.getId());
            redisUtil.set("lockPrice2,"+userLock.getId(),"lockPrice2,"+userLock.getId());
            redisUtil.expire("lockPrice2,"+userLock.getId(),lockTime, TimeUnit.SECONDS);
            userLock.setStatus(2);
            return userLockMapper.updateById(userLock);
        }
        return 0;
    }

    /**
     * 根据用户id 充值指定数值的用户余额
     * @param userId
     * @param price
     * @return
     * @throws Exception
     */
    public Map<String, String> balance(String userId, String price) throws Exception {
        UserInfo userInfo = userInfoMapper.selectById(userId);
        PaymentPo paymentPo = new PaymentPo();
        paymentPo.setBody("充值用户余额");
        paymentPo.setNonce_str(PayUtil.create_nonce_str());
        paymentPo.setNotify_url(notifyUrl);
        paymentPo.setOut_trade_no("VC"+new Date().getTime());
        paymentPo.setTotal_fee(price);
        paymentPo.setOpenid(userInfo.getOpenId());
        QueryWrapper<PayWx> payWxQueryWrapper = new QueryWrapper<>();
        payWxQueryWrapper.eq("order_no",paymentPo.getOut_trade_no());
        List<PayWx> payWxes = payWxMapper.selectList(payWxQueryWrapper);
        if (payWxes.size()<=0){
            PayWx payWx = new PayWx();
            payWx.setBody(paymentPo.getBody());
//            payWx.setNonceStr(paymentPo.getNonce_str());
            payWx.setOrderNo(paymentPo.getOut_trade_no());
            payWx.setTotalFee(paymentPo.getTotal_fee());
            payWx.setOpenId(paymentPo.getOpenid());
            payWx.setStatus(0);
            payWx.setType(3);
            this.addPayWx(payWx);
        }
        return this.goPay(paymentPo);
    }

    /**
     * 根据锁价数据id查询支付保证金
     * @param userLockId
     * @return
     *  code:201:当前锁价数据id不存在
     *       202:当前锁价数据id不是未支付保证金状态
     * @throws Exception
     */
    public Map<String, String> payLockGuaranteeGold(String userLockId, Integer type) throws Exception {
        Map<String,String> map = new HashMap<>();
        UserLock userLock = userLockMapper.selectById(userLockId);
        if (userLock==null) { map.put("code","201"); return map; }
        if (userLock.getStatus()!=1) { map.put("code","202"); return map; }
        UserInfo userInfo = userInfoMapper.selectById(userLock.getUserId());
        QueryWrapper<Config> configQueryWrapper = new QueryWrapper<>();
        configQueryWrapper.eq("code","cashDeposit");
        String price = configMapper.selectList(configQueryWrapper).get(0).getValue();
        if (type==0){//微信支付
            PaymentPo paymentPo = new PaymentPo();
            paymentPo.setBody("支付锁价意向金");
            paymentPo.setNonce_str(PayUtil.create_nonce_str());
            paymentPo.setNotify_url(notifyUrl);
            paymentPo.setOut_trade_no(userLock.getNumber());
            paymentPo.setTotal_fee(price);
            paymentPo.setOpenid(userInfo.getOpenId());
            QueryWrapper<PayWx> payWxQueryWrapper = new QueryWrapper<>();
            payWxQueryWrapper.eq("order_no",paymentPo.getOut_trade_no());
            List<PayWx> payWxes = payWxMapper.selectList(payWxQueryWrapper);
            if (payWxes.size()<=0){
                PayWx payWx = new PayWx();
                payWx.setBody(paymentPo.getBody());
//            payWx.setNonceStr(paymentPo.getNonce_str());
                payWx.setOrderNo(paymentPo.getOut_trade_no());
                payWx.setTotalFee(paymentPo.getTotal_fee());
                payWx.setOpenId(paymentPo.getOpenid());
                payWx.setStatus(0);
                payWx.setType(0);
                this.addPayWx(payWx);
            }
            return this.goPay(paymentPo);
        }else {//用户余额支付
            if(userInfo.getPrice().compareTo(new BigDecimal(price)) == -1) map.put("msg","用户余额不足已支付意向金");
            userInfo.setPrice(userInfo.getPrice().subtract(new BigDecimal(price)));
            this.userInfoMapper.updateById(userInfo);
            UserPriceInfo userPriceInfo = new UserPriceInfo();
            userPriceInfo.setUserId(userInfo.getId());
            userPriceInfo.setType("-");
            userPriceInfo.setPrice(new BigDecimal(price));
            userPriceInfo.setInfo("支付锁价意向金");
            Integer i = userPriceInfoMapper.insert(userPriceInfo);
            if (i<=0) { map.put("msg","支付失败"); return map; }
            QueryWrapper<Config> configQueryWrapper1 = new QueryWrapper<>();
            configQueryWrapper1.eq("code","lockTime");
            Integer lockTime = Integer.parseInt(configMapper.selectList(configQueryWrapper1).get(0).getValue());
            redisUtil.delete("lockPrice1,"+userLock.getId());
            redisUtil.set("lockPrice2,"+userLock.getId(),"lockPrice2,"+userLock.getId());
            redisUtil.expire("lockPrice2,"+userLock.getId(),lockTime, TimeUnit.SECONDS);
            userLock.setStatus(2);
            i = userLockMapper.updateById(userLock);
            if (i<=0) { map.put("msg","支付失败"); return map; }
            map.put("msg","支付成功");
            return map;
        }
    }


    /**
     * 调用微信商户转账给个人零钱
     * 参数：paymentPo.getNonce_str()       随机码
     *      paymentPo.getOut_trade_no()    商户订单号（唯一）
     *      paymentPo.getOpenid()          接受企业转账的用户的openid（关联上面微信公众号的）
     *      paymentPo.getTotal_fee()       企业付款金额（元）
     *      paymentPo.getBody()            企业转账描述
     *      paymentPo.getType()            类型 0支付保证金 1退还保证金 2红包返现
     * @param paymentPo
     * @return
     */
    public CommonResult paymentIndividual(PaymentPo paymentPo){
        //金额元=paymentPo.getTotal_fee()*100
        String total_fee = String.valueOf(new BigDecimal(paymentPo.getTotal_fee()).multiply(new BigDecimal(100)).intValue());
        Map map = new HashMap();
        map.put("mch_appid",appid);//appid（微信公众号）
        map.put("mchid",mchId);//商户号
        map.put("nonce_str", paymentPo.getNonce_str());//随机码
        map.put("partner_trade_no",paymentPo.getOut_trade_no());//商户订单号（唯一）
        map.put("openid",paymentPo.getOpenid());//接受企业转账的用户的openid（关联上面微信公众号的）
        map.put("amount",total_fee);//企业付款金额，单位为分
        map.put("desc",paymentPo.getBody());//企业转账描述
        //是否强制校验姓名，如果选了FORCE_CHECK，那就得传一个re_user_name字段，value为被转账用户真实姓名（非实名用户转账会失败）
        map.put("check_name","NO_CHECK");//FORCE_CHECK强制校验姓名  NO_CHECK不校验姓名

        String prestr = PayUtil.createLinkString(map); // 把数组所有元素，按照“参数=参数值”的模式用“&”字符拼接成字符串
        //MD5运算生成签名，这里是第一次签名，用于调用统一下单接口
        String mysign = PayUtil.sign(prestr, mchKey, "utf-8").toUpperCase();
        //拼接统一下单接口使用的xml数据，要将上一步生成的签名一起拼接进去
        String xml = "<xml>" + "<mch_appid>" + appid + "</mch_appid>"
                + "<mchid>" + mchId + "</mchid>"
                + "<nonce_str>" + paymentPo.getNonce_str() + "</nonce_str>"
                + "<sign>" + mysign + "</sign>"
                + "<partner_trade_no>" + paymentPo.getOut_trade_no() + "</partner_trade_no>"
                + "<openid>" + paymentPo.getOpenid() + "</openid>"
                +"<check_name>NO_CHECK</check_name>"
                + "<amount>" + total_fee + "</amount>"
                + "<desc>"+paymentPo.getBody()+"</desc>"
                + "</xml>";
        log.info("向微信付款的消息："+xml);
        //调用企业转账零钱，并接受返回的结果
        CloseableHttpClient httpClient = null;
        HttpPost httpPost = new HttpPost(payIndividualUrl);
        String body = null;
        CloseableHttpResponse response = null;
        try{
            httpClient = HttpClients.custom().setDefaultRequestConfig(HttpUtils.REQUEST_CONFIG).setSslcontext(HttpUtils.wx_ssl_context).build();
            httpPost.setEntity(new StringEntity(xml, "UTF-8"));
            response = httpClient.execute(httpPost);
            body = EntityUtils.toString(response.getEntity(), "UTF-8");
            log.info("微信付款返回的消息："+body);
            if (!StringUtil.isEmpty(body)){

                Map<String,String> resultMap = PayUtil.doXMLParse(body.trim());//xml转map

                if (resultMap.get("return_code").equals("SUCCESS") ){
                    if (resultMap.get("result_code").equals("SUCCESS")){
                        //处理自己的业务逻辑
                        PayWx payWx = new PayWx();
                        payWx.setBody(paymentPo.getBody());
                        payWx.setNonceStr(paymentPo.getNonce_str());
                        payWx.setOrderNo(paymentPo.getOut_trade_no());
                        payWx.setTotalFee(paymentPo.getTotal_fee());
                        payWx.setOpenId(paymentPo.getOpenid());
                        payWx.setStatus(0);
                        payWx.setType(paymentPo.getType());
                        this.addPayWx(payWx);
                        Map<String,Object> userInfo = userInfoMapper.selectByOpenId(paymentPo.getOpenid());
                        UserPriceInfo userPriceInfo = new UserPriceInfo();
                        userPriceInfo.setInfo(paymentPo.getBody());
                        userPriceInfo.setUserId(userInfo.get("id").toString());
                        userPriceInfo.setPrice(new BigDecimal(paymentPo.getTotal_fee()));
                        userPriceInfo.setType("-");
                        this.userPriceInfoMapper.insert(userPriceInfo);
                    }else if (resultMap.get("result_code").equals("FAIL")) {
                        log.error("业务结果未明确，具体信息如下：");
                        log.error("错误代码{}，错误描述{}",resultMap.get("err_code"),resultMap.get("err_code_des"));
                        return new CommonResult(201,"转账失败，失败原因："+resultMap.get("err_code_des"));
                    }
                }else {
                    log.error("连接微信转账接口通信失败，信息："+map.get("return_msg"));
                    return new CommonResult(201,"连接微信转账接口通信失败，信息："+map.get("return_msg"));
                }
            }else {
                return new CommonResult(201,"转账失败，微信返回的消息为空");
            }
        }catch (Exception e){
            log.error("转账异常，异常消息如下："+e.getMessage());
            return new CommonResult(201,"转账异常，信息如下："+e.getMessage());
        }finally {
            if (response != null) {
                try {
                    response.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (httpClient != null) {
                try {
                    httpClient.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return new CommonResult(200,"转账成功");
    }

    /**
     * 微信提现
     * @return
     */
    public CommonResult WXTX(Map<String,String> map){
        UserInfo userInfo = userInfoMapper.selectById(map.get("userId"));
        if(new BigDecimal(map.get("price")).compareTo(userInfo.getPrice())==1) return new CommonResult(201,"提现金额超出用户余额");
        userInfo.setPrice(userInfo.getPrice().subtract(new BigDecimal(map.get("price"))));
        this.userInfoMapper.updateById(userInfo);
        PaymentPo paymentPo = new PaymentPo();
        paymentPo.setNonce_str(PayUtil.create_nonce_str());
        paymentPo.setOpenid(userInfo.getOpenId());
        paymentPo.setOut_trade_no("TX"+new Date().getTime());
//        paymentPo.setOut_trade_no("10000098201411111234567890");
        paymentPo.setTotal_fee(map.get("price"));
        paymentPo.setBody("账户提现");
        paymentPo.setType(1);
        return this.paymentIndividual(paymentPo);
    }

}
