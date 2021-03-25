package com.cosmo;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;

@SpringBootTest
class CosmoApplicationTests {


    /**
     * 单价计算公式
     */
    @Test
    void formula1() {
        // （原料价格+加工费）*（订单卷数*净重）+（订单卷数/装箱卷数*纸箱价格+（管重*订单卷数*纸管单价））
        BigDecimal yuanliao = new BigDecimal("8.2");//原料价格
        BigDecimal jiagongfei = new BigDecimal("1.2");//加工费
        BigDecimal juanshu = new BigDecimal("300");//订单卷数
        BigDecimal jingzhong = new BigDecimal("15");//净重
        BigDecimal zhuangxiang = new BigDecimal("1");//装箱卷数
        BigDecimal zhixiang = new BigDecimal("2.56");//纸箱价格
        BigDecimal guanzhong = new BigDecimal("0.8");//管重
        BigDecimal zhiguan = new BigDecimal("4.5");//纸管单价

        //（原料价格+加工费）*（订单卷数*净重）
        BigDecimal mo = yuanliao.add(jiagongfei).multiply(juanshu.multiply(jingzhong));
        System.err.println("膜："+mo);
        //订单卷数/装箱卷数*纸箱价格
        BigDecimal xiang = juanshu.divide(zhuangxiang,0,BigDecimal.ROUND_UP).multiply(zhixiang);
        System.err.println("箱："+xiang);
        //（管重*订单卷数*纸管单价）
        BigDecimal guan = guanzhong.multiply(juanshu).multiply(zhiguan);
        System.err.println("管："+guan);
//        BigDecimal zong = mo.add(xiang).add(guan);
        BigDecimal zong = mo.add(guan);
        System.err.println("总金额："+zong);
        BigDecimal zongjingzhong = juanshu.multiply(jingzhong);
        System.err.println("单价 元/kg："+zong.divide(zongjingzhong,2,BigDecimal.ROUND_DOWN));
    }

    /**
     * 单价计算公式(分切)
     */
    @Test
    void formula2() {
        // （原料价格+加工费+分切加工费）*（订单卷数*净重）+（订单卷数/装箱卷数*纸箱价格+（分切管重*订单卷数*纸管单价））
//        BigDecimal bili = new BigDecimal("300").divide(new BigDecimal("500"), 2, BigDecimal.ROUND_DOWN);//分切比例
        BigDecimal bizhun = new BigDecimal("500");//标准宽度
        BigDecimal fenqie = new BigDecimal("100");//分切宽度
        BigDecimal fenqiejiagongfei = new BigDecimal("0.8");//分切加工费
        BigDecimal yuanliao = new BigDecimal("8.2");//原料价格
        BigDecimal jiagongfei = new BigDecimal("1.2");//加工费
        BigDecimal juanshu = new BigDecimal("2000");//订单卷数
        BigDecimal jingzhong = new BigDecimal("1");//净重
        BigDecimal zhuangxiang = new BigDecimal("20");//装箱卷数
        BigDecimal zhixiang = new BigDecimal("3.4");//纸箱价格
        BigDecimal guanzhong = new BigDecimal("0.5");//管重
        BigDecimal zhiguan = new BigDecimal("4.5");//纸管单价

        //（净重/标准宽度*分切宽度）= 标准净重
//        BigDecimal biaozhunjingzhong = jingzhong.divide(bizhun, 2, BigDecimal.ROUND_DOWN).multiply(fenqie);
        //（原料价格+加工费+分切加工费）*（订单卷数*净重）
        BigDecimal mo = yuanliao.add(jiagongfei).add(fenqiejiagongfei).multiply(juanshu.multiply(jingzhong));
        System.err.println("膜："+mo);
        //订单卷数/装箱卷数*纸箱价格
        BigDecimal xiang = juanshu.divide(zhuangxiang,0,BigDecimal.ROUND_UP).multiply(zhixiang);
        System.err.println("箱："+xiang);
        //（管重/标准宽度*分切宽度）= 分切管重
        guanzhong = guanzhong.divide(bizhun, 2, BigDecimal.ROUND_DOWN).multiply(fenqie);
        //（分切管重*订单卷数*纸管单价）
        BigDecimal guan = guanzhong.multiply(juanshu).multiply(zhiguan);
        System.err.println("管："+guan);
//        BigDecimal zong = mo.add(xiang).add(guan);
        BigDecimal zong = mo.add(guan);
        System.err.println("总金额："+zong);
        BigDecimal zongjingzhong = juanshu.multiply(jingzhong);
        System.err.println("单价 元/kg："+zong.divide(zongjingzhong,2,BigDecimal.ROUND_DOWN));
    }

    public static void main(String[] args) {
        // （原料价格+加工费+分切加工费）*（订单卷数*净重）+（订单卷数/装箱卷数*纸箱价格+（分切管重*订单卷数*纸管单价））
//        BigDecimal bili = new BigDecimal("300").divide(new BigDecimal("500"), 2, BigDecimal.ROUND_DOWN);//分切比例
        BigDecimal bizhun = new BigDecimal("500");//标准宽度
        BigDecimal fenqie = new BigDecimal("100");//分切宽度
        BigDecimal fenqiejiagongfei = new BigDecimal("0.8");//分切加工费
        BigDecimal yuanliao = new BigDecimal("8.2");//原料价格
        BigDecimal jiagongfei = new BigDecimal("1.2");//加工费
        BigDecimal juanshu = new BigDecimal("2000");//订单卷数
        BigDecimal jingzhong = new BigDecimal("1");//净重
        BigDecimal zhuangxiang = new BigDecimal("20");//装箱卷数
        BigDecimal zhixiang = new BigDecimal("3.4");//纸箱价格
        BigDecimal guanzhong = new BigDecimal("0.3");//管重
        BigDecimal zhiguan = new BigDecimal("4.5");//纸管单价
        BigDecimal zhekuo = new BigDecimal("0.85");//会员折扣

//        （净重/标准宽度*分切宽度）= 标准净重
//        BigDecimal biaozhunjingzhong = jingzhong.divide(bizhun, 2, BigDecimal.ROUND_DOWN).multiply(fenqie);
        //（原料价格+加工费+分切加工费）*（订单卷数*净重）
        BigDecimal mo = yuanliao.add(jiagongfei).add(fenqiejiagongfei).multiply(juanshu.multiply(jingzhong));
        System.err.println("膜："+mo);
        //订单卷数/装箱卷数*纸箱价格
        BigDecimal xiang = juanshu.divide(zhuangxiang,0,BigDecimal.ROUND_UP).multiply(zhixiang);
        System.err.println("箱："+xiang);
        //（管重*（分切宽度/标准宽度））= 分切管重
        guanzhong = guanzhong.multiply(fenqie.divide(bizhun, 2, BigDecimal.ROUND_DOWN));
        //（分切管重*订单卷数*纸管单价）
        BigDecimal guan = guanzhong.multiply(juanshu).multiply(zhiguan);
        System.err.println("管："+guan);
        BigDecimal zong = mo.add(xiang).add(guan);
//        BigDecimal zong = mo.add(guan);
        System.err.println("总金额："+zong);
        BigDecimal zongjingzhong = juanshu.multiply(jingzhong);
        System.err.println("单价 元/kg："+zong.divide(zongjingzhong,2,BigDecimal.ROUND_DOWN));
        System.err.println("会员单价 元/kg："+zong.divide(zongjingzhong,2,BigDecimal.ROUND_DOWN).subtract(zhekuo));
    }

}
