package com.cosmo.controller;

import com.cosmo.entity.Invoice;
import com.cosmo.entity.OrderForm;
import com.cosmo.service.InvoiceService;
import com.cosmo.util.CommonResult;
import com.cosmo.util.*;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class InvoiceController {

    @Resource
    private InvoiceService invoiceService;

    /**
     * 根据用户id查询未开票订单
     * @param request
     * @return
     */
    @GetMapping("/app/invoice/orderList")
    public CommonResult orderList(HttpServletRequest request){
        String userId = request.getParameter("userId");
        if (StringUtils.isEmpty(userId)) return new CommonResult(500,"userId 为空");
        List<OrderForm> orders = invoiceService.orderList(Integer.parseInt(userId));
        if (orders!=null&&orders.size()>0) return new CommonResult(200,"查询成功",orders);
        return new CommonResult(201,"未查询到结果",null);
    }

    /**
     * 新增开票
     * @param request
     * @return
     */
    @PostMapping("/app/incoice/addInvoice")
    public CommonResult addInvoice(HttpServletRequest request){
        String userId = request.getParameter("userId");
        if (StringUtils.isEmpty(userId)) return new CommonResult(500,"userId 为空");
        String name = request.getParameter("name");
        if (StringUtils.isEmpty(name)) return new CommonResult(500,"name 为空");
        String phone = request.getParameter("phone");
        if (StringUtils.isEmpty(phone)) return new CommonResult(500,"phone 为空");
        String tax = request.getParameter("tax");
        if (StringUtils.isEmpty(tax)) return new CommonResult(500,"tax 为空");
        String fax = request.getParameter("fax");
//        if (StringUtils.isEmpty(fax)) return new CommonResult(500,"fax 为空");
        String company = request.getParameter("company");
//        if (StringUtils.isEmpty(company)) return new CommonResult(500,"company 为空");
        String address = request.getParameter("address");
        if (StringUtils.isEmpty(address)) return new CommonResult(500,"address 为空");
        String detailAddress = request.getParameter("detailAddress");
        if (StringUtils.isEmpty(detailAddress)) return new CommonResult(500,"detailAddress 为空");
        String openBank = request.getParameter("openBank");
        if (StringUtils.isEmpty(openBank)) return new CommonResult(500,"openBank 为空");
        String openBankNum = request.getParameter("openBankNum");
        if (StringUtils.isEmpty(openBankNum)) return new CommonResult(500,"openBankNum 为空");
        String orderIdList = request.getParameter("orderIdList");
        if (StringUtils.isEmpty(orderIdList)) return new CommonResult(500,"orderIdList 为空");
        String orderPriceList = request.getParameter("orderPriceList");
        if (StringUtils.isEmpty(orderPriceList)) return new CommonResult(500,"orderPriceList 为空");

        Map<String,String> map = new HashMap<>();
        map.put("userId",userId);map.put("name",name);map.put("phone",phone);
        map.put("tax",tax);map.put("fax",fax);map.put("company",company);
        map.put("address",address);map.put("detailAddress",detailAddress);map.put("openBank",openBank);
        map.put("openBankNum",openBankNum);map.put("orderIdList",orderIdList);map.put("orderPriceList",orderPriceList);
        Integer i = invoiceService.addInvoice(map);
        if (i>0) return new CommonResult(200,"新增成功");
        return new CommonResult(201,"新增失败");
    }

    /**
     * 根据用户id查询发票
     * @param request
     * @return
     */
    @GetMapping("/app/invoice/invoiceList")
    public CommonResult invoiceList(HttpServletRequest request){
        String pageNum = request.getParameter("pageNum");
        if (StringUtils.isEmpty(pageNum)) pageNum = "1";
        String userId = request.getParameter("userId");
        if (StringUtils.isEmpty(userId)) return new CommonResult(500,"userId 为空");
        String type = request.getParameter("type");
        if (StringUtils.isEmpty(type)) return new CommonResult(500,"type 为空");
        PageInfo pageInfo = invoiceService.invoiceList(Integer.parseInt(pageNum),userId,type);
        if (pageInfo.getList().size()>0) return new CommonResult(200,"查询成功",pageInfo);
        return new CommonResult(201,"未查询到结果",null);
    }


    /**
     * 根据发票状态
     * 和税号模糊查询
     * 查询发票
     * @param request
     * @return
     */
    @PostMapping("/invoice/invoiceList")
    public CommonResult invoiceList1(HttpServletRequest request){
        String pageNum = request.getParameter("pageNum");
        if (StringUtils.isEmpty(pageNum)) pageNum = "1";
        String tax = request.getParameter("tax");
        String type = request.getParameter("type");
        if (StringUtils.isEmpty(type)) return new CommonResult(500,"type 为空");
        PageInfo pageInfo = invoiceService.invoiceList1(Integer.parseInt(pageNum),tax,type);
        if (pageInfo.getList().size()>0) return new CommonResult(200,"查询成功",pageInfo);
        return new CommonResult(201,"未查询到结果",null);
    }

    /**
     * 修改开票
     * @param request
     * @return
     */
    @PostMapping("/invoice/updateInvoice")
    public CommonResult updateInvoice(HttpServletRequest request){
        String id = request.getParameter("id");
        if (StringUtils.isEmpty(id)) return new CommonResult(500,"id 为空");
        String type = request.getParameter("type");
        if (StringUtils.isEmpty(type)) return new CommonResult(500,"type 为空");
        String trackingNumber = request.getParameter("trackingNumber");
        if ("2".equals(type)) if (StringUtils.isEmpty(trackingNumber)||"null".equals(trackingNumber)) return new CommonResult(201,"更变为'已开票'状态时快递单号不可为空");
        String name = request.getParameter("name");
        if (StringUtils.isEmpty(name)) return new CommonResult(500,"name 为空");
        String phone = request.getParameter("phone");
        if (StringUtils.isEmpty(phone)) return new CommonResult(500,"phone 为空");
        String tax = request.getParameter("tax");
        if (StringUtils.isEmpty(tax)) return new CommonResult(500,"tax 为空");
        String fax = request.getParameter("fax");
//        if (StringUtils.isEmpty(fax)) return new CommonResult(500,"fax 为空");
        String company = request.getParameter("company");
//        if (StringUtils.isEmpty(company)) return new CommonResult(500,"company 为空");
        String address = request.getParameter("address");
        if (StringUtils.isEmpty(address)) return new CommonResult(500,"address 为空");
        String detailAddress = request.getParameter("detailAddress");
        if (StringUtils.isEmpty(detailAddress)) return new CommonResult(500,"detailAddress 为空");
        String openBank = request.getParameter("openBank");
        if (StringUtils.isEmpty(openBank)) return new CommonResult(500,"openBank 为空");
        String openBankNum = request.getParameter("openBankNum");
        if (StringUtils.isEmpty(openBankNum)) return new CommonResult(500,"openBankNum 为空");
        Invoice invoice = new Invoice();
        invoice.setId(Long.valueOf(id));
        invoice.setType(Integer.parseInt(type));
        if ("2".equals(type)) invoice.setTrackingNumber(trackingNumber);
        invoice.setName(name);
        invoice.setPhone(phone);
        invoice.setTax(tax);
        invoice.setFax(fax);
        invoice.setCompany(company);
        invoice.setAddress(address);
        invoice.setDetailAddress(detailAddress);
        invoice.setOpenBank(openBank);
        invoice.setOpenBankNum(openBankNum);
        Integer i = invoiceService.updateInvoice(invoice);
        if (i>0) return new CommonResult(200,"修改成功");
        return new CommonResult(201,"修改失败");
    }
}
