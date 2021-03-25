<!DOCTYPE html [
        <!ENTITY nbsp "&#160;">
]>
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
    <title>PDF下载</title>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
    <style mce_bogus="1" type="text/css">
        * {
            margin: 0;
            padding: 0;
        }

        body {
            font-family: SimSun;
            font-size: 1rem;
        }

        @page {
            size: 397mm 210mm
            /*size: A4;*/
        }

        .head1 {
            width: 100%;
            margin: 0 auto;
            margin-top: 0;
            overflow: hidden;
        }

        .head2 {
            width: 100%;
            margin: 0 auto;
            overflow: hidden;
        }

        .head3 {
            width: 100%;
            margin: 0 auto;
            overflow: hidden;
        }
        .head4 {
            width: 100%;
            margin: 0 auto;
            overflow: hidden;
        }
        .head5 {
            width: 100%;
            margin: 0 auto;
            overflow: hidden;
        }

        .float_left_50 {
            float: left;
            width: 49.7%;
            border: 2px solid black;
        }
        .float_left_33 {
            float: left;
            width: 32%;
        }

        .float_left_16 {
            float: left;
            width: 15%;
        }
        .float_left_17 {
            float: left;
            width: 20%;
        }

        .zhengFang {
            width: 90%;
            height: 60%;
            border: 2px solid black;
            margin-left: 0.875rem;
            float: right;
        }

        .margin_top {
            margin-top: 0.9375rem;
        }

        .text_align {
            text-align: center;
        }

        .xiaoShouHeTong {
            margin-top: 5%;
            font-size: 1.875rem;
        }

        .nameAdd {
            font-size: 1rem;
            margin-top: 1%;
        }

        .addTime {
            margin-left: 0.625rem;
        }

        .table {
            width: 100%;
        }
        /*.gongZhang{
            height: 5rem;width: 5rem;
            position: relative;
            left: 100rem;
            bottom: 100rem;
        }*/
    </style>
</head>
<body>
<div class="head1">
<#--    <div class="float_left_33"><img src="file:///E:/word/cosmo/target/classes/logo.png"></img></div>-->
   <div class="float_left_33"><img src="file:///root/img/cosmo/hetong/logo.png"></img></div>
    <div class="float_left_33 text_align xiaoShouHeTong">购销合同</div>
    <div class="float_left_33 text_align nameAdd"><b>昆山科世茂包装材料有限公司<br></br>江苏昆山千灯石浦利都路298号</b></div>
</div>
<div class="head2">
    <div class="float_left_33 margin_top">
        <p>供方：昆山科世茂包装材料有限公司</p>
        <p>需方：<#if orderDemander??>${orderDemander}</#if></p>
        <p>第一条 名称、数量、金额及交（提）货时间：</p>
    </div>
    <div class="float_left_33 margin_top">
        <p class="addTime">签订地址：江苏-昆山</p>
        <p class="addTime">签订时间：<#if nowTime??>${nowTime}</#if></p>
    </div>
    <div class="float_left_17 margin_top">
        <p>合同编号：<#if orderNumber??>${orderNumber}</#if></p>
    </div>
    <div class="float_left_16">
        <div class="zhengFang">&nbsp;<br></br>&nbsp;<br></br></div>
    </div>
</div>
<div class="head3">
    <table class="table text_align" border="2px" cellspacing="0"  cellpadding="">
        <tr>
            <td rowspan="2">序号</td>
            <td colspan="7">产品信息</td>
            <td colspan="10">包装信息</td>
            <td colspan="6">价格信息</td>
        </tr>
        <tr>
            <td>型号</td>
            <td>厚度<br></br>(um)</td>
            <td>宽度<br></br>(mm)</td>
            <td>长度<br></br>(m)</td>
            <td>净重<br></br>(kg)</td>
            <td>毛重<br></br>(kg)</td>
            <td>总净重<br></br>(kg)</td>
            <td>管重<br></br>(kg)</td>
            <td>管径<br></br>(mm)</td>
            <td>装箱<br></br>卷数</td>
            <td>纸箱<br></br>规格</td>
            <td>箱重(kg)</td>
            <td>标签</td>
            <td>托盘<br></br>数(个)</td>
            <td>托盘重<br></br>(kg)</td>
            <td>托盘型号</td>
            <td>箱数<br></br>/托盘</td>
            <td>数量<br></br>(卷)</td>
            <td>总毛重<br></br>(kg)</td>
            <td>运费<br></br>单价<br></br>(元/kg)</td>
            <td>运费<br></br>(元)</td>
            <td>单价<br></br>(元/kg)</td>
            <td>金额<br></br>(元)</td>
        </tr>
        <#list orderModelList as orderModel>
            <tr>
                 <td>${orderModel_index+1}</td>
                 <td><#if orderModel.modelName??>${orderModel.modelName}</#if></td>
                 <td><#if orderModel.specThickness??>${orderModel.specThickness}</#if></td>
                 <td><#if orderModel.specWidth??>${orderModel.specWidth}</#if></td>
                 <td><#if orderModel.specLength??>${orderModel.specLength}</#if></td>
                 <td><#if orderModel.specSuttle??>${orderModel.specSuttle}</#if></td>
                 <td><#if orderModel.rollRoughWeight??>${orderModel.rollRoughWeight}</#if></td>
                 <td><#if orderModel.modelTotalSuttle??>${orderModel.modelTotalSuttle}</#if></td>
                 <td><#if orderModel.pipeWeight??>${orderModel.pipeWeight}</#if></td>
                 <td><#if orderModel.pipeDia??>${orderModel.pipeDia}</#if></td>
                 <td><#if orderModel.cartonPipeNumber??>${orderModel.cartonPipeNumber}</#if></td>
                 <td><#if orderModel.cartonType??>${orderModel.cartonType}</#if></td>
                 <td><#if orderModel.cartonWeight??>${orderModel.cartonWeight}</#if></td>
                 <td><#if orderModel.labelType??><#if (orderModel.labelType==0)>中性<#else><#if (orderModel.labelType==1)>定制<#else>无标签</#if></#if></#if></td>
                 <td><#if orderModel.trayNumber??>${orderModel.trayNumber}</#if></td>
                 <td><#if orderModel.trayNumber??>${orderModel.trayNumber*20}</#if></td>
                 <td><#if orderModel.trayModel??>${orderModel.trayModel}</#if></td>
                 <td><#if orderModel.trayCapacity??>${orderModel.trayCapacity}</#if></td>
                 <td><#if orderModel.rollNumber??>${orderModel.rollNumber}</#if></td>
                 <td><#if orderModel.modelTotalRoughWeight??>${orderModel.modelTotalRoughWeight}</#if></td>
                 <td><#if freightPrice??>${freightPrice}</#if></td>
                 <td><#if orderModel.orderModelFreight??>${orderModel.orderModelFreight}</#if></td>
                 <td><#if orderModel.modelUnitPrice??>${orderModel.modelUnitPrice-orderModel.memberDiscount}</#if></td>
                 <td><#if orderModel.modelTotalPrice??>${orderModel.modelTotalPrice}</#if></td>
            </tr>
        </#list>
        <tr>
            <td>&nbsp;</td>
            <td>&nbsp;</td>
            <td>&nbsp;</td>
            <td>&nbsp;</td>
            <td>&nbsp;</td>
            <td>&nbsp;</td>
            <td>&nbsp;</td>
            <td>&nbsp;</td>
            <td>&nbsp;</td>
            <td>&nbsp;</td>
            <td>&nbsp;</td>
            <td>&nbsp;</td>
            <td>&nbsp;</td>
            <td>&nbsp;</td>
            <td>&nbsp;</td>
            <td>&nbsp;</td>
            <td>&nbsp;</td>
            <td>&nbsp;</td>
            <td>&nbsp;</td>
            <td>&nbsp;</td>
            <td>&nbsp;</td>
            <td>&nbsp;</td>
            <td>&nbsp;</td>
            <td>&nbsp;</td>
        </tr>
        <tr>
            <td colspan="2">合计</td>
            <td colspan="2">含增值税13%</td>
            <td colspan="6">备注：</td>
            <td colspan="4"><#if orderPrice??>订单金额：${orderPrice}</#if></td>
            <td colspan="10">${coupon}</td>
        </tr>
    </table>
</div>
<div class="head4">
    <p>第二条 交货时间：<#if orderTimeDelivery??>${orderTimeDelivery}</#if>发货 </p>
    <p>第三条 质量标准：按本公司样品质量标准或按国家标准交货。</p>
    <p>第四条 供方对质量负责的条件及期限：自需方收到货物起7天内提出质量异议，双方协商解决。</p>
    <p>第六条 本合同解除的条件：双方协商一致同意。  </p>
    <p>第七条 违约责任：按《经济合同法》有关规定执行； </p>
    <p>第八条 合同争议的解决方式：供需双方履行本合同过程中发生争议的，应协商解决，协商不成的，在供方住所地法院诉讼解决。 </p>
    <p>第九条 本合同自双方签订之日起 生效。 </p>
    <p>第十条 其他约定事项：本合同一式两份，传真件或扫描件同样有效。</p>
</div>
<div style="margin-left:25%;margin-top:0;margin-bottom:5%;width:160px;height:160px;position: absolute; left: 0; bottom: 0;">
<#--    <img src="file:///E:/word/cosmo/target/classes/gongzhang.png" class="gongZhang" ></img>-->
    <img src="file:///root/img/cosmo/hetong/gongzhang.png" class="gongZhang" ></img>
</div>
<div class="head5">
    <div style="z-index:1;" class="float_left_50">
        <p class="text_align">供方</p>
        <p>供方（章）：昆山科世茂包装材料有限公司</p>
        <p>地址：江苏省昆山市千灯镇石浦利都路298号</p>
        <p>委托代理人：叶建伟</p>
        <p>电话：13867493672</p>
        <p>传真：0512-57277131</p>
        <p>账户：37400103004229141</p>
        <p>开户行：上海银行昆山支行</p>
    </div>
    <div style="z-index:1;border-left: 0px;" class="float_left_50">
        <p class="text_align">收货方</p>
        <p>收货方：<#if company??>${company}</#if></p>
        <p>地址：<#if userAddress??>${userAddress}</#if></p>
        <p>委托代理人：<#if userName??>${userName}</#if></p>
        <p>电话：<#if userPhone??>${userPhone}</#if></p>
        <p>传真：<#if fax??>${fax}</#if></p>
        <p>&nbsp;</p>
        <p>&nbsp;</p>
    </div>

</div>
<br></br>
<br></br>
<div class="head5">
    <table class="table" border="2px" cellspacing="0"  cellpadding="">
        <tr>
            <td>业务部</td>
            <td>生产部</td>
            <td>仓储部</td>
            <td>财务部</td>
            <td>物流部</td>
            <td>票据部</td>
        </tr>
        <tr>
            <td>&nbsp;</td>
            <td></td>
            <td></td>
            <td></td>
            <td></td>
            <td></td>
        </tr>
    </table>
</div>


</body>
</html>