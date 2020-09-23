package com.cosmo.controller;

import com.cosmo.dao.ConfigMapper;
import com.cosmo.entity.Config;
import com.cosmo.service.ConfigService;
import com.cosmo.util.CommonResult;
import com.cosmo.util.*;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class ConfigController {
    @Resource
    private ConfigService configService;

    /**
     * 查询客服
     * @return
     */
    @GetMapping("/app/config/customerService")
    public CommonResult customerService(){
        List<Config> configs = configService.customerService("service","2");
        if (configs.size()>0) return new CommonResult(200,"查询成功",configs.get(0).getValue());
        return new CommonResult(201,"查询失败",null);
    }

    /**
     * 通过code，type查询
     * @param request
     * @return
     */
    @GetMapping("/config/configValue")
    public CommonResult configValue(HttpServletRequest request){
        String code = request.getParameter("code");
        if (StringUtil.isEmpty(code)) return new CommonResult(500,"code 为空",null);
        String type = request.getParameter("type");
        if (StringUtil.isEmpty(type)) return new CommonResult(500,"type 为空",null);
        List<Config> configs = configService.customerService(code,type);
        if (configs.size()>0) return new CommonResult(200,"查询成功",configs.get(0));
        return new CommonResult(201,"查询失败",null);
    }

    /**
     * 查询分切加工费
     * app 小程序用
     * @param request
     * @return
     */
    @GetMapping("/app/config/configValue")
    public CommonResult configValue1(HttpServletRequest request){
        List<Config> configs = configService.customerService("slittingProcessCost","4");
        if (configs.size()>0) return new CommonResult(200,"查询成功",configs.get(0));
        return new CommonResult(201,"查询失败",null);
    }

    /**
     * 查询配置杂项
     * @return
     */
    @GetMapping("/app/config/customerConfig")
    public CommonResult customerConfig(){
        List<Config> configs = configService.customerConfig();
        if (configs.size()>0) return new CommonResult(200,"查询成功",configs);
        return new CommonResult(201,"查询失败",null);
    }

    /**
     * 通过code查询配置杂项
     * @return
     */
    @GetMapping("/app/config/codeConfig")
    public CommonResult codeConfig(HttpServletRequest request){
        String code = request.getParameter("code");
        Config config = configService.codeConfig(code);
        if (config!=null) return new CommonResult(200,"查询成功",config);
        return new CommonResult(201,"未查询到结果",null);
    }

    /**
     * 修改客服
     * @param request
     * @param file
     * @return
     */
    @PostMapping("/config/updateService")
    public CommonResult updateService(HttpServletRequest request, MultipartFile file){
        String name = request.getParameter("name");
        if (StringUtil.isEmpty(name)) return new CommonResult(500,"name 为空",null);
        String wx = request.getParameter("wx");
        if (StringUtil.isEmpty(wx)) return new CommonResult(500,"wx 为空",null);
        String qq = request.getParameter("qq");
        if (StringUtil.isEmpty(qq)) return new CommonResult(500,"qq 为空",null);
        String email = request.getParameter("email");
        if (StringUtil.isEmpty(email)) return new CommonResult(500,"email 为空",null);
        String type = request.getParameter("type");
        if (StringUtil.isEmpty(type)) return new CommonResult(500,"type 为空",null);
        if ("1".equals(type))if (file.isEmpty()) return new CommonResult(500,"图片 为空");
        Map<String,String> map = new HashMap<>();
        map.put("name",name);
        map.put("wx",wx);
        map.put("qq",qq);
        map.put("email",email);
        map.put("type",type);
        if (configService.updateService(map,file)>0) return new CommonResult(200,"更新成功");
        return new CommonResult(201,"更新失败");
    }

    /**
     * 通过配置类型查询 （分页）
     * @param request
     * @return
     */
    @PostMapping("/config/configsType")
    public CommonResult configsType(HttpServletRequest request){
        String pageNum = request.getParameter("pageNum");//页码
        if (StringUtil.isEmpty(pageNum)) pageNum = "1";
        String type = request.getParameter("type");//配置参数类型 0原料价格，1杂项
        if (StringUtil.isEmpty(type)) return new CommonResult(500,"type 为空",null);
        String code = null;
        if ("0".equals(type)) {
            code = request.getParameter("code");
            if (StringUtil.isEmpty(code)) return new CommonResult(500,"code 为空",null);
        }
        PageInfo pageInfo = configService.configsType(Integer.parseInt(pageNum),Integer.parseInt(type),code);
        if (pageInfo.getList().size()>0) return new CommonResult(200,"查询成功",pageInfo);
        return new CommonResult(201,"未查询到结果",null);
    }

    /**
     * 根据id，value 更新单行数据
     * @param request
     * @return
     */
    @PostMapping("/config/updateById")
    public CommonResult updateById(HttpServletRequest request){
        String id = request.getParameter("id");
        if (StringUtil.isEmpty(id)) new CommonResult(500,"id 为空");
        String value = request.getParameter("value");
        if (StringUtil.isEmpty(value)) new CommonResult(500,"value 为空");
        Config config = new Config();
        config.setId(id);
        config.setValue(value);
        Integer i = configService.updateById(config);
        if (i>0) return new CommonResult(200,"更新成功成功");
        return new CommonResult(201,"更新失败");
    }

    /**
     * 根据id数组，value数组修改
     * @param request
     * @return
     */
    @PostMapping("/config/updateByIds")
    public CommonResult updateByIds(HttpServletRequest request){
        String ids = request.getParameter("ids");
        if (StringUtil.isEmpty(ids)) new CommonResult(500,"ids 为空");
        String values = request.getParameter("values");
        if (StringUtil.isEmpty(values)) new CommonResult(500,"values 为空");
        Map<String,String> map = new HashMap<>();
        map.put("ids",ids);
        map.put("values",values);
        Integer i = configService.updateByIds(map);
        if (i>0) return new CommonResult(200,"更新成功");
        return new CommonResult(201,"更新失败");
    }

    /**
     * 通过type修改所有value
     * @param request
     * @return
     */
    @PostMapping("/config/updateTypeValue")
    public CommonResult updateTypeValue(HttpServletRequest request){
        String type = request.getParameter("type");
        if (StringUtil.isEmpty(type)) new CommonResult(500,"type 为空");
        String value = request.getParameter("value");
        if (StringUtil.isEmpty(value)) new CommonResult(500,"value 为空");
        Integer i = configService.updateTypeValue(type,value);
        if (i>0) return new CommonResult(200,"更新成功");
        return new CommonResult(201,"更新失败");
    }
}
