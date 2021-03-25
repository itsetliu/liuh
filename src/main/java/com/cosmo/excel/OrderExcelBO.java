package com.cosmo.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ContentFontStyle;
import com.alibaba.excel.annotation.write.style.ContentLoopMerge;
import com.alibaba.excel.annotation.write.style.ContentStyle;
import com.alibaba.excel.metadata.BaseRowModel;
import lombok.Data;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.VerticalAlignment;

@Data
// 内容字体设置成20
@ContentFontStyle(fontHeightInPoints = 11)
public class OrderExcelBO extends BaseRowModel {

    @ContentStyle(horizontalAlignment = HorizontalAlignment.CENTER,verticalAlignment = VerticalAlignment.CENTER)
    @ContentLoopMerge(eachRow = 2)
    @ExcelProperty(value = {"","订单序号"}, index = 0)
    private String orderIndex;
    @ContentStyle(horizontalAlignment = HorizontalAlignment.CENTER,verticalAlignment = VerticalAlignment.CENTER)
    @ContentLoopMerge(eachRow = 2)
    @ExcelProperty(value = {"","计划到期日期"}, index = 1)
    private String time;
    @ContentStyle(horizontalAlignment = HorizontalAlignment.CENTER,verticalAlignment = VerticalAlignment.CENTER)
    @ContentLoopMerge(eachRow = 2)
    @ExcelProperty(value = {"","物流方向"}, index = 2)
    private String direction;// 不显示
    @ContentStyle(horizontalAlignment = HorizontalAlignment.CENTER,verticalAlignment = VerticalAlignment.CENTER)
    @ContentLoopMerge(eachRow = 2)
    @ExcelProperty(value = {"","客户编号"}, index = 3)
    private String userNumber;
    @ContentStyle(horizontalAlignment = HorizontalAlignment.CENTER,verticalAlignment = VerticalAlignment.CENTER)
    @ContentLoopMerge(eachRow = 2)
    @ExcelProperty(value = {"","合同号"}, index = 4)
    private String orderNumber;
    @ContentStyle(horizontalAlignment = HorizontalAlignment.CENTER,verticalAlignment = VerticalAlignment.CENTER,
            // 字符串的内容的背景设置成绿色
            fillPatternType = FillPatternType.SOLID_FOREGROUND, fillForegroundColor = 57)
    @ContentLoopMerge(eachRow = 2)
    @ExcelProperty(value = {"","排产许可"}, index = 5)
    private String permission;
    @ContentStyle(horizontalAlignment = HorizontalAlignment.CENTER,verticalAlignment = VerticalAlignment.CENTER)
    @ContentLoopMerge(eachRow = 2)
    @ExcelProperty(value = {"","序号"}, index = 6)
    private String modelIndex;

    @ContentStyle(horizontalAlignment = HorizontalAlignment.CENTER,verticalAlignment = VerticalAlignment.CENTER)
    @ContentLoopMerge(eachRow = 2)
    @ExcelProperty(value = {"产品信息","型号"}, index = 7)
    private String modelName;
    @ContentStyle(horizontalAlignment = HorizontalAlignment.CENTER,verticalAlignment = VerticalAlignment.CENTER)
    @ContentLoopMerge(eachRow = 2)
    @ExcelProperty(value = {"产品信息","厚度(um)"}, index =8)
    private String thickness;
    @ContentStyle(horizontalAlignment = HorizontalAlignment.CENTER,verticalAlignment = VerticalAlignment.CENTER)
    @ContentLoopMerge(eachRow = 2)
    @ExcelProperty(value = {"产品信息","宽度(mm)"}, index = 9)
    private String width;
    @ContentStyle(horizontalAlignment = HorizontalAlignment.CENTER,verticalAlignment = VerticalAlignment.CENTER)
    @ContentLoopMerge(eachRow = 2)
    @ExcelProperty(value = {"产品信息","长度(m)"}, index = 10)
    private String length;
    @ContentStyle(horizontalAlignment = HorizontalAlignment.CENTER,verticalAlignment = VerticalAlignment.CENTER,
            fillPatternType = FillPatternType.SOLID_FOREGROUND, fillForegroundColor = 44)
    @ContentLoopMerge(eachRow = 2)
    @ExcelProperty(value = {"产品信息","净重(KG)"}, index = 11)
    private String suttle;
    @ContentStyle(horizontalAlignment = HorizontalAlignment.CENTER,verticalAlignment = VerticalAlignment.CENTER)
    @ContentLoopMerge(eachRow = 2)
    @ExcelProperty(value = {"产品信息","毛重(KG)"}, index = 12)
    private String roughWeight;
    @ContentStyle(horizontalAlignment = HorizontalAlignment.CENTER,verticalAlignment = VerticalAlignment.CENTER)
    @ContentLoopMerge(eachRow = 2)
    @ExcelProperty(value = {"产品信息","总净重(KG)"}, index = 13)
    private String totalSuttle;

    @ContentStyle(horizontalAlignment = HorizontalAlignment.CENTER,verticalAlignment = VerticalAlignment.CENTER,
            // 字符串的内容的背景设置成浅蓝色
            fillPatternType = FillPatternType.SOLID_FOREGROUND, fillForegroundColor = 44)
    @ContentLoopMerge(eachRow = 2)
    @ExcelProperty(value = {"包装信息","管重(KG)"}, index = 14)
    private String pipeWeight;
    @ContentStyle(horizontalAlignment = HorizontalAlignment.CENTER,verticalAlignment = VerticalAlignment.CENTER)
    @ContentLoopMerge(eachRow = 2)
    @ExcelProperty(value = {"包装信息","管径(mm)"}, index = 15)
    private String pipeDia;
    @ContentStyle(horizontalAlignment = HorizontalAlignment.CENTER,verticalAlignment = VerticalAlignment.CENTER,
            // 字符串的内容的背景设置成浅蓝色
            fillPatternType = FillPatternType.SOLID_FOREGROUND, fillForegroundColor = 44)
    @ContentLoopMerge(eachRow = 2)
    @ExcelProperty(value = {"包装信息","装箱卷数"}, index = 16)
    private String cartonPipeNumber;
    @ContentStyle(horizontalAlignment = HorizontalAlignment.CENTER,verticalAlignment = VerticalAlignment.CENTER)
    @ContentLoopMerge(eachRow = 2)
    @ExcelProperty(value = {"包装信息","纸箱规格"}, index = 17)
    private String cartonType;
    @ContentStyle(horizontalAlignment = HorizontalAlignment.CENTER,verticalAlignment = VerticalAlignment.CENTER)
    @ContentLoopMerge(eachRow = 2)
    @ExcelProperty(value = {"包装信息","箱重(KG)"}, index = 18)
    private String cartonWeight;
    @ContentStyle(horizontalAlignment = HorizontalAlignment.CENTER,verticalAlignment = VerticalAlignment.CENTER)
    @ContentLoopMerge(eachRow = 2)
    @ExcelProperty(value = {"包装信息","标签(KG)"}, index = 19)
    private String labelType;
    @ContentStyle(horizontalAlignment = HorizontalAlignment.CENTER,verticalAlignment = VerticalAlignment.CENTER)
    @ContentLoopMerge(eachRow = 2)
    @ExcelProperty(value = {"包装信息","托盘数(个)"}, index = 20)
    private String trayNumber;
    @ContentStyle(horizontalAlignment = HorizontalAlignment.CENTER,verticalAlignment = VerticalAlignment.CENTER)
    @ContentLoopMerge(eachRow = 2)
    @ExcelProperty(value = {"包装信息","托盘重(KG)"}, index = 21)
    private String trayWeight;
    @ContentStyle(horizontalAlignment = HorizontalAlignment.CENTER,verticalAlignment = VerticalAlignment.CENTER)
    @ContentLoopMerge(eachRow = 2)
    @ExcelProperty(value = {"包装信息","托盘型号"}, index = 22)
    private String trayModel;
    @ContentStyle(horizontalAlignment = HorizontalAlignment.CENTER,verticalAlignment = VerticalAlignment.CENTER)
    @ContentLoopMerge(eachRow = 2)
    @ExcelProperty(value = {"包装信息","箱数/托盘"}, index = 23)
    private String trayCapacity;

    @ContentStyle(horizontalAlignment = HorizontalAlignment.CENTER,verticalAlignment = VerticalAlignment.CENTER,
            // 字符串的内容的背景设置成浅蓝色
            fillPatternType = FillPatternType.SOLID_FOREGROUND, fillForegroundColor = 44)

    @ContentLoopMerge(eachRow = 2)
    @ExcelProperty(value = {"价格信息","数量(卷)"}, index = 24)
    private String rollNumber;
    @ContentStyle(horizontalAlignment = HorizontalAlignment.CENTER,verticalAlignment = VerticalAlignment.CENTER)
    @ContentLoopMerge(eachRow = 2)
    @ExcelProperty(value = {"价格信息","总毛重(KG)"}, index = 25)
    private String totalRoughWeight;
    @ContentStyle(horizontalAlignment = HorizontalAlignment.CENTER,verticalAlignment = VerticalAlignment.CENTER)
    @ContentLoopMerge(eachRow = 2)
    @ExcelProperty(value = {"价格信息","运费单价(元/KG)"}, index = 26)
    private String freightPrice;// 不显示
    @ContentStyle(horizontalAlignment = HorizontalAlignment.CENTER,verticalAlignment = VerticalAlignment.CENTER)
    @ContentLoopMerge(eachRow = 2)
    @ExcelProperty(value = {"价格信息","运费(元)"}, index = 27)
    private String freight;// 不显示
    @ContentStyle(horizontalAlignment = HorizontalAlignment.CENTER,verticalAlignment = VerticalAlignment.CENTER,
            // 字符串的内容的背景设置成浅灰色
            fillPatternType = FillPatternType.SOLID_FOREGROUND, fillForegroundColor = 22)

    @ContentLoopMerge(eachRow = 2)
    @ExcelProperty(value = {"价格信息","单价(元/KG)"}, index = 28)
    private String unitPrice;// 不显示
    @ContentStyle(horizontalAlignment = HorizontalAlignment.CENTER,verticalAlignment = VerticalAlignment.CENTER)
    @ContentLoopMerge(eachRow = 2)
    @ExcelProperty(value = {"价格信息","金额(元)"}, index = 29)
    private String totalPrice;// 不显示

    @ContentStyle(horizontalAlignment = HorizontalAlignment.CENTER,verticalAlignment = VerticalAlignment.CENTER)
    @ContentLoopMerge(eachRow = 2)
    @ExcelProperty(value = {"生产信息","备注"}, index = 30)
    private String remark;// 不显示
    @ContentStyle(horizontalAlignment = HorizontalAlignment.CENTER,verticalAlignment = VerticalAlignment.CENTER,
            // 字符串的内容的背景设置成黄色
            fillPatternType = FillPatternType.SOLID_FOREGROUND, fillForegroundColor = 13)
    @ContentLoopMerge(eachRow = 2)
    @ExcelProperty(value = {"生产信息","完成状态"}, index = 31)
    private String status;
    @ContentStyle(horizontalAlignment = HorizontalAlignment.CENTER,verticalAlignment = VerticalAlignment.CENTER)
    @ExcelProperty(value = {"生产信息","完成状态"}, index = 32)
    private String status1;
    @ContentStyle(horizontalAlignment = HorizontalAlignment.CENTER,verticalAlignment = VerticalAlignment.CENTER)
    @ExcelProperty(value = {"生产信息","数量(卷)"}, index = 33)
    private String ModelNumber;
    @ContentStyle(horizontalAlignment = HorizontalAlignment.CENTER,verticalAlignment = VerticalAlignment.CENTER)
    @ExcelProperty(value = {"生产信息","重量(KG)"}, index = 34)
    private String ModelWeight;
    @ContentStyle(horizontalAlignment = HorizontalAlignment.CENTER,verticalAlignment = VerticalAlignment.CENTER)
    @ContentLoopMerge(eachRow = 2)
    @ExcelProperty(value = {"生产信息","记录"}, index = 35)
    private String record;

    @ContentStyle(horizontalAlignment = HorizontalAlignment.CENTER,verticalAlignment = VerticalAlignment.CENTER)
    @ContentLoopMerge(eachRow = 2)
    @ExcelProperty(value = {"财务信息","付款方式"}, index = 36)
    private String payMethod;
    @ContentStyle(horizontalAlignment = HorizontalAlignment.CENTER,verticalAlignment = VerticalAlignment.CENTER)
    @ContentLoopMerge(eachRow = 2)
    @ExcelProperty(value = {"财务信息","实际到账时间"}, index = 37)
    private String accountingDate;
    @ContentStyle(horizontalAlignment = HorizontalAlignment.CENTER,verticalAlignment = VerticalAlignment.CENTER)
    @ContentLoopMerge(eachRow = 2)
    @ExcelProperty(value = {"财务信息","审核"}, index = 38)
    private String audit;
    @ContentStyle(horizontalAlignment = HorizontalAlignment.CENTER,verticalAlignment = VerticalAlignment.CENTER)
    @ContentLoopMerge(eachRow = 2)
    @ExcelProperty(value = {"财务信息","发票号"}, index = 39)
    private String invoice;
    @ContentStyle(horizontalAlignment = HorizontalAlignment.CENTER,verticalAlignment = VerticalAlignment.CENTER)
    @ContentLoopMerge(eachRow = 2)
    @ExcelProperty(value = {"财务信息","发票快递信息"}, index = 40)
    private String invoiceFMSNumber;
}
