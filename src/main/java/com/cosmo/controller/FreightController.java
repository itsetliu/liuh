package com.cosmo.controller;

import com.cosmo.entity.Freight;
import com.cosmo.entity.HatProvince;
import com.cosmo.service.FreightService;
import com.cosmo.util.CommonResult;
import com.cosmo.util.*;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
public class FreightController {

    @Resource
    private FreightService freightService;

    /**
     * 更新区间运费单价
     * 没有时新增
     * @param request
     * @return
     */
    @PostMapping("/freight/updateFreight")
    public CommonResult updateFreight(HttpServletRequest request){
        String hatid = request.getParameter("hatid");
        if (StringUtil.isEmpty(hatid)) return new CommonResult(500,"hatid 为空");
        String value = request.getParameter("value");
        if (StringUtil.isEmpty(value)) return new CommonResult(500,"value 为空");
        Freight freight = new Freight();
        freight.setHatid(hatid);
        freight.setValue(value);
        Integer i = freightService.updateFreight(freight);
        if (i>0) return new CommonResult(200,"修改成功");
        return new CommonResult(201,"修改失败");
    }

    /**
     * 查询所有省市
     * 市的所有运费区间
     * @return
     */
    @GetMapping("/freight/provinceCityList")
    public CommonResult provinceCityList(){
        List<HatProvince> hatProvinceList = freightService.provinceCityList();
        if (hatProvinceList.size()>0) return new CommonResult(200,"查询成功",hatProvinceList);
        return new CommonResult(201,"未查询到结果",null);
    }

}
