package com.cosmo.service;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cosmo.dao.InvoiceMapper;
import com.cosmo.dao.OrderFormMapper;
import com.cosmo.entity.Invoice;
import com.cosmo.entity.OrderForm;
import com.cosmo.util.PageInfo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class InvoiceService {

    @Resource
    private InvoiceMapper invoiceMapper;
    @Resource
    private OrderFormMapper orderFormMapper;

    SimpleDateFormat ft = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    /**
     * 根据用户id查询未开票订单
     * @param userId
     * @return
     */
    public List<OrderForm> orderList(String userId){
        QueryWrapper<OrderForm> orderFormQueryWrapper = new QueryWrapper<>();
        orderFormQueryWrapper.eq("user_id",userId).eq("invoice_type",0);
        List<Integer> in = new ArrayList<Integer>();
        in.add(2);in.add(3);
        orderFormQueryWrapper.in("order_status",2,3);
        return orderFormMapper.selectList(orderFormQueryWrapper);
    }

    /**
     * 新增开票
     * @param map
     * @return
     */
    @Transactional(value="txManager1")
    public Integer addInvoice(Map<String,String> map){
        Invoice invoice = new Invoice();
        invoice.setUserId(map.get("userId"));
        invoice.setName(map.get("name"));
        invoice.setPhone(map.get("phone"));
        invoice.setTax(map.get("tax"));
        invoice.setFax(map.get("fax"));
        invoice.setCompany(map.get("company"));
        invoice.setAddress(map.get("address"));
        invoice.setDetailAddress(map.get("detailAddress"));
        invoice.setOpenBank(map.get("openBank"));
        invoice.setOpenBankNum(map.get("openBankNum"));
        invoice.setCreateTime(ft.format(new Date()));
        invoice.setType(0);
        BigDecimal price = new BigDecimal("0");
        List<String> orderPriceList = JSON.parseArray(map.get("orderPriceList"),String.class);
        for(String orderPrice : orderPriceList){
            price = price.add(new BigDecimal(orderPrice));
        }
        invoice.setPrice(price);
        Integer i = invoiceMapper.insert(invoice);
        if (i<=0) return i;
        List<Integer> orderIdList = JSON.parseArray(map.get("orderIdList"),Integer.class);
        Map<String,Object> map1 = new HashMap<>();
        map1.put("orderIdList",orderIdList);
        map1.put("invoiceId",invoice.getId());
        map1.put("invoiceType",1);
        return orderFormMapper.updateInvoiceId(map1);
    }

    /**
     * 根据用户id
     * 发票状态
     * 查询发票
     * @param pageNum
     * @param userId
     * @param type
     * @return
     */
    public PageInfo invoiceList(Integer pageNum, String userId, String type){
        QueryWrapper<Invoice> invoiceQueryWrapper = new QueryWrapper<>();
        invoiceQueryWrapper.eq("user_id",userId).eq("type",type);
        Page page = new Page(pageNum,10);
        IPage<Invoice> invoices = invoiceMapper.selectPage(page,invoiceQueryWrapper);
        for (int i = 0;i<invoices.getRecords().size();i++){
            Invoice invoice = invoices.getRecords().get(i);
            QueryWrapper<OrderForm> orderFormQueryWrapper = new QueryWrapper<>();
            orderFormQueryWrapper.eq("invoice_id",invoice.getId());
            List<OrderForm> orders = orderFormMapper.selectList(orderFormQueryWrapper);
            invoice.setOrderFormList(orders);
            invoices.getRecords().set(i,invoice);
        }
        PageInfo pageInfo = new PageInfo(invoices);
        return pageInfo;
    }

    /**
     * 根据发票状态
     * 和税号模糊查询
     * 查询发票
     * @param pageNum
     * @param tax
     * @param type
     * @return
     */
    public PageInfo invoiceList1(Integer pageNum, String tax, String type){
        QueryWrapper<Invoice> invoiceQueryWrapper = new QueryWrapper<>();
        invoiceQueryWrapper.eq("type",type).like("tax",tax);
        Page page = new Page(pageNum,10);
        IPage<Invoice> invoices = invoiceMapper.selectPage(page,invoiceQueryWrapper);
        for (int i = 0;i<invoices.getRecords().size();i++){
            Invoice invoice = invoices.getRecords().get(i);
            QueryWrapper<OrderForm> orderFormQueryWrapper = new QueryWrapper<>();
            orderFormQueryWrapper.eq("invoice_id",invoice.getId());
            List<OrderForm> orders = orderFormMapper.selectList(orderFormQueryWrapper);
            invoice.setOrderFormList(orders);
            invoices.getRecords().set(i,invoice);
        }
        PageInfo pageInfo = new PageInfo(invoices);
        return pageInfo;
    }

    /**
     * 修改开票
     * @param invoice
     * @return
     */
    public Integer updateInvoice(Invoice invoice){
        return invoiceMapper.updateById(invoice);
    }



}
