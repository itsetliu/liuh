package com.cosmo.controller;

import com.alibaba.fastjson.JSON;
import com.cosmo.service.WxPayService;
import com.cosmo.util.CommonResult;
import com.cosmo.wx.PayUtil;
import com.cosmo.wx.PaymentPo;
import com.cosmo.util.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

@RestController
@Slf4j
public class WxPayController {

    @Value("${wechat.mchKey}")
    private String mchKey;

    @Resource
    private WxPayService wxPayService;

    /**
     * 用户登录 及 更新
     * @param request
     * @return
     */
    @PostMapping("/app/wx/login")
    public CommonResult wxLogin(HttpServletRequest request){
        String code = request.getParameter("code");
        if (StringUtil.isEmpty(code)) return new CommonResult(500,"code 为空");
        String encryptedData = request.getParameter("encryptedData");
        if (StringUtil.isEmpty(encryptedData)) return new CommonResult(500,"encryptedData 为空");
        String iv = request.getParameter("iv");
        if (StringUtil.isEmpty(iv)) return new CommonResult(500,"iv 为空");
        Map<String,String> map = wxPayService.login(code,encryptedData,iv);
        if ("200".equals(map.get("code"))) return new CommonResult(200,"成功",map.get("data"));
        return new CommonResult(500,"失败",null);
    }

    /**
     * 通过用户id 和 订单号 支付意向金
     * @param request
     * @return
     * @throws Exception
     */
    @PostMapping("/app/pay/wx")
    public CommonResult wxPay(HttpServletRequest request) throws Exception {
        String userId = request.getParameter("userId");
        if (StringUtil.isEmpty(userId)) return new CommonResult(500,"userId 为空");
        String orderId = request.getParameter("orderId");
        if (StringUtil.isEmpty(orderId)) return new CommonResult(500,"订单id 为空");
        String type = request.getParameter("type");
        if (StringUtil.isEmpty(type)) return new CommonResult(500,"支付类型 为空");
        Map<String, String> map = wxPayService.wxPay(Integer.parseInt(userId), Integer.parseInt(orderId),Integer.parseInt(type));
        if ("0".equals(type)) return new CommonResult(200,"唤起",map);
        return new CommonResult(200,"唤起",map.get("msg"));
    }

    /**
     * 根据用户id 充值指定数值的用户余额
     * @param request
     * @return
     * @throws Exception
     */
    @PostMapping("/app/pay/balance")
    public CommonResult balance(HttpServletRequest request) throws Exception {
        String userId = request.getParameter("userId");
        if (StringUtil.isEmpty(userId)) return new CommonResult(500,"userId 为空");
        String price = request.getParameter("price");
        if (StringUtil.isEmpty(price)) return new CommonResult(500,"price 为空");
        Map<String, String> map = wxPayService.balance(Integer.parseInt(userId), price);
        return new CommonResult(200,"唤起",map);
    }

    /**
     * 根据锁价数据id查询支付保证金
     * @param request
     *      type:0微信 1余额
     * @return
     * @throws Exception
     */
    @PostMapping("/app/pay/payLockGuaranteeGold")
    public CommonResult payLockGuaranteeGold(HttpServletRequest request) throws Exception {
        String userLockId = request.getParameter("userLockId");
        if (StringUtil.isEmpty(userLockId)) return new CommonResult(500,"userLockId 为空");
        String type = request.getParameter("type");
        if (StringUtil.isEmpty(type)) return new CommonResult(500,"支付类型 为空");
        Map<String, String> map = wxPayService.payLockGuaranteeGold(Integer.parseInt(userLockId),Integer.parseInt(type));
        if (map.get("code")!=null){
            if ("201".equals(map.get("code"))) return new CommonResult(201,"当前锁价数据id不存在",null);
            else if ("202".equals(map.get("code"))) return new CommonResult(201,"当前锁价数据id不是未支付保证金状态",null);
        }
        if("0".equals(type)) return new CommonResult(200,"唤起",map);
        return new CommonResult(200,"唤起",map.get("msg"));
    }

    /**
     * 支付回调
     * @param request
     * @param response
     * @throws InterruptedException
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    @RequestMapping(value="/app/pay/notify")
    public synchronized void notify(HttpServletRequest request, HttpServletResponse response) throws InterruptedException{

        String orderId = null;
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader((ServletInputStream)request.getInputStream()));
            String line = null;
            StringBuilder sb = new StringBuilder();
            while((line = br.readLine()) != null){
                sb.append(line);
            }
            br.close();
            //sb为微信返回的xml
            String notityXml = sb.toString();
            log.info("接收到的报文：" + notityXml);

            Map map = PayUtil.doXMLParse(notityXml);
            String resXml = "";
            String returnCode = (String) map.get("return_code");
            if("SUCCESS".equals(returnCode)){
                //验证签名是否正确
                Map<String, String> validParams = PayUtil.paraFilter(map);  //回调验签时需要去除sign和空值参数
                String validStr = PayUtil.createLinkString(validParams);//把数组所有元素，按照“参数=参数值”的模式用“&”字符拼接成字符串
                String sign = PayUtil.sign(validStr, mchKey, "utf-8").toUpperCase();//拼装生成服务器端验证的签名
                //根据微信官网的介绍，此处不仅对回调的参数进行验签，还需要对返回的金额与系统订单的金额进行比对等
                if(sign.equals(map.get("sign"))){
                    /**此处添加自己的业务逻辑代码start**/
                    //TODO 微信支付回调
                    wxPayService.updateStatue(map.get("out_trade_no").toString(),map.get("openid").toString(),map.get("total_fee").toString());
                    /**此处添加自己的业务逻辑代码end**/
                }
                resXml =
                        "<xml>"
                                + "<return_code><![CDATA[SUCCESS]]></return_code>"
                                + "<return_msg><![CDATA[OK]]></return_msg>"
                                + "</xml> ";
            }
            log.info("微信支付回调数据结束");


            BufferedOutputStream out = new BufferedOutputStream(
                    response.getOutputStream());
            out.write(resXml.getBytes());
            out.flush();
            out.close();
            return;
        } catch (Exception e) {
            log.error("出错啦",orderId,e);
        }
    }

    /**
     * 微信提现
     * @return
     */
    @PostMapping("/app/WXTX")
    public CommonResult WXTX(HttpServletRequest request){
        String userId = request.getParameter("userId");
        if (StringUtil.isEmpty(userId)) return new CommonResult(500,"userId 为空");
        String price = request.getParameter("price");
        if (StringUtil.isEmpty(price)) return new CommonResult(500,"price 为空");
        Map<String,String> map = new HashMap<>();
        map.put("userId",userId);map.put("price",price);
        return wxPayService.WXTX(map);
    }
}
