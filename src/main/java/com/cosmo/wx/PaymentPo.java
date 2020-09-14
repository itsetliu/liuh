package com.cosmo.wx;

import lombok.Data;

import java.io.Serializable;

@Data
public class PaymentPo  implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 1712467669291115101L;
    private String appid;//小程序ID
    private String mch_id;//商户号
    private String nonce_str;//随机字符串
    private String sign;//签名
    private String body;//商品描述
    private String detail;//商品详情
    private String out_trade_no;//商户订单号
    private String total_fee;//总金额
    private String notify_url;//通知地址
    private String trade_type;//交易类型
    private String limit_pay;//指定支付方式
    private String openid;//用户标识
    private Integer type;//类型 0支付保证金 1退还保证金 2红包返现
}
