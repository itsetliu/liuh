package com.cosmo.pdf;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * @Description: PDF 文件导出配置文件
 * @Author: junqiang.lu
 * @Date: 2019/1/8
 */
@Configuration
@Data
public class PDFExportConfig {

    /**
     * 宋体字体文件相对路径
     */
    @Value("${pdfExport.fontSimsun}")
    private String fontSimsun;

    /**
     * 宋体字体文件相对路径
     */
    @Value("${pdfExport.fontSimsunUrl}")
    private String fontSimsunUrl;

    /**
     * 导出模板文件相对路径
     */
    @Value("${pdfExport.employeeKpiFtl}")
    private String employeeKpiFtl;

    /**
     * 导出模板文件名称
     */
    @Value("${pdfExport.employeeKpiFtlName}")
    private String employeeKpiFtlName;

    /**
     * 导出模板文件路径
     */
    @Value("${pdfExport.employeeKpiFtlUrl}")
    private String employeeKpiFtlUrl;

}
