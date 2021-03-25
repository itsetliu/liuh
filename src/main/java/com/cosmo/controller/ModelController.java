package com.cosmo.controller;

import com.cosmo.dao.CalculatorModelRecommendMapper;
import com.cosmo.entity.CalculatorModelRecommend;
import com.cosmo.entity.Model;
import com.cosmo.entity.ModelCarton;
import com.cosmo.entity.ModelShow;
import com.cosmo.service.ModelService;
import com.cosmo.util.CommonResult;
import com.cosmo.util.*;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class ModelController {
    @Resource
    private ModelService modelService;

    /**
     * 查询所有型号
     * @param request
     * @return
     */
    @GetMapping("/model/selectModel")
    public CommonResult selectModel(HttpServletRequest request){
        String type = request.getParameter("type");
        List<Model> models = modelService.selectModel(type);
        if (models.size()>0) return new CommonResult(200,"查询成功",models);
        return new CommonResult(201,"未查询到结果",null);
    }

    /**
     * 查询所有型号（分页，模糊，类型）
     * @param request
     * @return
     */
    @PostMapping("/model/modelList")
    public CommonResult modelList(HttpServletRequest request){
        String pageNum = request.getParameter("pageNum");
        if (StringUtil.isEmpty(pageNum)) pageNum = "1";
        String name = request.getParameter("name");
        String type = request.getParameter("type");
        PageInfo pageInfo = modelService.modelList(Integer.parseInt(pageNum),name,type);
        if (pageInfo.getList().size()>0) return new CommonResult(200,"查询成功",pageInfo);
        return new CommonResult(201,"未查询到结果",null);
    }

    /**
     * 新增型号
     * @param request
     * @return
     */
    @PostMapping("/model/addModel")
    public CommonResult addModel(HttpServletRequest request){
        String name = request.getParameter("name");
        if (StringUtil.isEmpty(name)) return new CommonResult(500,"型号 为空");
        String typeName = request.getParameter("typeName");
        if (StringUtil.isEmpty(typeName)) return new CommonResult(500,"型号简称 为空");
//        String price = request.getParameter("price");
//        if (StringUtil.isEmpty(price)) return new CommonResult(500,"原料单价 为空");
        String type = request.getParameter("type");
        if (StringUtil.isEmpty(type)) return new CommonResult(500,"未选择膜类型");
        String modelThickness = request.getParameter("modelThickness");
        if (StringUtil.isEmpty(modelThickness)) return new CommonResult(500,"最少设置1组厚度及加工费");
//        String modelSuttles = request.getParameter("modelSuttles");
//        if (StringUtil.isEmpty(modelSuttles)) return new CommonResult(500,"最少设置1组净重");
        Map<String,String> map = new HashMap<>();
        map.put("name",name);
        map.put("typeName",typeName);
//        map.put("price",price);
        map.put("type",type);
        map.put("modelThickness",modelThickness);
//        map.put("modelSuttles",modelSuttles);
        Integer i = modelService.addModel(map);
        if (i>0) return new CommonResult(200,"新增成功");
        return new CommonResult(201,"新增失败");
    }

    /**
     * 查询修改参数
     * @param request
     * @return
     */
    @GetMapping("/model/updeteInfo")
    public CommonResult updateInfo(HttpServletRequest request){
        String id = request.getParameter("id");
        if (StringUtil.isEmpty(id)) return new CommonResult(500,"id 为空");
        Map<String,Object> map = modelService.updateInfo(id);
        if (map!=null) return new CommonResult(200,"查询成功",map);
        return new CommonResult(201,"未查询到结果",null);
    }

    /**
     * 修改型号
     * @param request
     * @return
     */
    @PostMapping("/model/updateModel")
    public CommonResult updateModel(HttpServletRequest request){
        String id = request.getParameter("id");
        if (StringUtil.isEmpty(id)) return new CommonResult(500,"id 为空");
        String name = request.getParameter("name");
        String typeName = request.getParameter("typeName");
//        String price = request.getParameter("price");
        String type = request.getParameter("type");
        String modelThickness = request.getParameter("modelThickness");
        if (StringUtil.isEmpty(modelThickness)) return new CommonResult(500,"最少设置1组厚度及加工费");
//        String modelSuttles = request.getParameter("modelSuttles");
//        if (StringUtil.isEmpty(modelSuttles)) return new CommonResult(500,"最少设置1组净重");
        Map<String,String> map = new HashMap<>();
        map.put("id",id);
        map.put("name",name);
        map.put("typeName",typeName);
//        map.put("price",price);
        map.put("type",type);
        map.put("modelThickness",modelThickness);
//        map.put("modelSuttles",modelSuttles);
        Integer i = modelService.updateModel(map);
        if (i>0) return new CommonResult(200,"修改成功");
        return new CommonResult(201,"修改失败");
    }

    /**
     * 根据id删除model
     * @param request
     * @return
     */
    @PostMapping("/model/delModel")
    public CommonResult delModel(HttpServletRequest request){
        String id = request.getParameter("id");
        if (StringUtil.isEmpty(id)) return new CommonResult(500,"id 为空");
        Integer i = modelService.delModel(id);
        if (i>0) return new CommonResult(200,"删除成功");
        return new CommonResult(201,"删除失败");
    }



    /**
     * 查询所有型号
     * 对应的原料价格
     * 对应的厚度加工费
     * @return
     */
    @GetMapping("/app/model/selectModel")
    public CommonResult selectModel(){
        Map<String,List<Model>> map = modelService.selectModel();
        if (map!=null) return new CommonResult(200,"查询成功",map);
        return new CommonResult(201,"未查询到结果",null);
    }

    /**
     * 查询首页
     * @return
     */
    @GetMapping("/app/model/home")
    public CommonResult home(){
//        long time = new Date().getTime();
        List<Map<String,Object>> mapList = modelService.home();
//        long time1 = new Date().getTime();
//        System.err.println(time1-time);
        if (mapList.size()>0) return new CommonResult(200,"查询成功",mapList);
        return new CommonResult(201,"未查询到结果",null);
    }

    /**
     * 根据型号id查询
     * @param request
     * @return
     */
    @PostMapping("/app/model/byId")
    public CommonResult byId(HttpServletRequest request){
        String id = request.getParameter("id");
        if (StringUtil.isEmpty(id)) return new CommonResult(500,"id 为空");
        Map<String,Object> map = modelService.byId(id);
        if (map!=null) return new CommonResult(200,"查询成功",map);
        return new CommonResult(201,"未查询到结果",null);
    }

    /**
     * 新增型号规格首页显示
     * @param request
     * @return
     */
    @PostMapping("/model/addModelShow")
    public CommonResult addModelShow(HttpServletRequest request){
        String modelId = request.getParameter("modelId");
        if (StringUtil.isEmpty(modelId)) return new CommonResult(500,"modelId 为空");
        String modelName = request.getParameter("modelName");
        if (StringUtil.isEmpty(modelName)) return new CommonResult(500,"modelName 为空");
        String type = request.getParameter("type");
        if (StringUtil.isEmpty(type)) return new CommonResult(500,"type 为空");
        String typeName = request.getParameter("typeName");
        if (StringUtil.isEmpty(typeName)) return new CommonResult(500,"typeName 为空");
        String suttle = request.getParameter("suttle");
        if (StringUtil.isEmpty(suttle)) return new CommonResult(500,"suttle 为空");
        String pipeWeight = request.getParameter("pipeWeight");
        if (StringUtil.isEmpty(pipeWeight)) return new CommonResult(500,"pipeWeight 为空");
        String width = request.getParameter("width");
        if (StringUtil.isEmpty(width)) return new CommonResult(500,"width 为空");
        String thickness = request.getParameter("thickness");
        if (StringUtil.isEmpty(thickness)) return new CommonResult(500,"thickness 为空");
        String scope = request.getParameter("scope");
        if (StringUtil.isEmpty(scope)) return new CommonResult(500,"scope 为空");
        String volume = request.getParameter("volume");
        if (StringUtil.isEmpty(volume)) return new CommonResult(500,"volume 为空");
        ModelShow modelShow = new ModelShow();
        modelShow.setModelId(modelId);
        BigDecimal suttle1 = new BigDecimal(suttle);
        if (suttle1.compareTo(new BigDecimal("10"))==-1) suttle1=suttle1.multiply(new BigDecimal("10"));
        if (Integer.parseInt(thickness)<10) thickness = "0"+thickness;
        String name = typeName+thickness+suttle1.toString();
        modelShow.setName(name);
        modelShow.setModelName(modelName);
        modelShow.setType(Integer.parseInt(type));
        modelShow.setTypeName(typeName);
        modelShow.setSuttle(suttle);
        modelShow.setPipeWeight(pipeWeight);
        modelShow.setWidth(width);
        modelShow.setThickness(thickness);
        modelShow.setScope(scope);
        modelShow.setVolume(Integer.parseInt(volume));
        int i = modelService.addModelShow(modelShow);
        if (i>0) return new CommonResult(200,"新增成功");
        return new CommonResult(201,"新增失败");
    }

    /**
     * 修改型号规格首页显示
     * @param request
     * @return
     */
    @PostMapping("/model/updateModelShow")
    public CommonResult updateModelShow(HttpServletRequest request){
        ModelShow modelShow = new ModelShow();
        String id = request.getParameter("id");
        if (StringUtil.isEmpty(id)) return new CommonResult(500,"id 为空");
        modelShow.setId(id);
        String scope = request.getParameter("scope");
        if (!StringUtil.isEmpty(scope)) modelShow.setScope(scope);
        String volume = request.getParameter("volume");
        if (!StringUtil.isEmpty(volume)) modelShow.setVolume(Integer.parseInt(volume));
        String pipeWeight = request.getParameter("pipeWeight");
        if (!StringUtil.isEmpty(pipeWeight)) modelShow.setPipeWeight(pipeWeight);
        int i = modelService.updateModelShow(modelShow);
        if (i>0) return new CommonResult(200,"修改成功");
        return new CommonResult(201,"修改失败");
    }

    /**
     * 根据id删除型号规格首页显示
     * @param request
     * @return
     */
    @PostMapping("/model/delModelShow")
    public CommonResult delModelShow(HttpServletRequest request){
        String id = request.getParameter("id");
        if (StringUtil.isEmpty(id)) return new CommonResult(500,"id 为空");
        int i = modelService.delModelShow(id);
        if (i>0) return new CommonResult(200,"删除成功");
        return new CommonResult(201,"删除失败");
    }

    /**
     * 分页查询小程序首页显示
     * @param request
     * @return
     */
    @PostMapping("/model/selectModelShow")
    public CommonResult selectModelShow(HttpServletRequest request){
        String pageNum = request.getParameter("pageNum");
        if (StringUtil.isEmpty(pageNum)) pageNum = "1";
        PageInfo pageInfo = modelService.selectModelShow(Integer.parseInt(pageNum));
        if (pageInfo.getList().size()>0) return new CommonResult(200,"查询成功",pageInfo);
        return new CommonResult(201,"查询失败",null);
    }

    /**
     * 修改所有scope
     * @param request
     * @return
     */
    @PostMapping("/model/updateModelShowScope")
    public CommonResult updateModelShowScope(HttpServletRequest request){
        String scope = request.getParameter("scope");
        if (StringUtil.isEmpty(scope)) return new CommonResult(500,"scope 为空");
        Integer i = modelService.updateModelShowScope(scope);
        if (i>0) return new CommonResult(200,"更新成功");
        return new CommonResult(201,"更新失败");
    }

    /**
     * 新建首页锁价
     * @param request
     *      lockPrice:锁价数据
     * @return
     */
    @PostMapping("/app/model/lockPrice")
    public CommonResult lockPrice(HttpServletRequest request){
        String userId = request.getParameter("userId");
        if (StringUtil.isEmpty(userId)) return new CommonResult(500,"userId 为空");
        String lockPrice = request.getParameter("lockPrice");
        if (StringUtil.isEmpty(lockPrice)) return new CommonResult(500,"lockPrice 为空");
        String margin = request.getParameter("margin");
        if (StringUtil.isEmpty(margin)) return new CommonResult(500,"margin 为空");
        String obligation = request.getParameter("obligation");
        if (StringUtil.isEmpty(obligation)) return new CommonResult(500,"obligation 为空");
        Map<String,String> map = new HashMap<>();
        map.put("userId",userId);map.put("lockPrice",lockPrice);
        map.put("margin",margin);map.put("obligation",obligation);
        String i = modelService.lockPrice(map);
        if ("201".equals(i)) return new CommonResult(201,"userId错误/不存在",null);
        else if (i!=null) return new CommonResult(200,"新增成功，锁价数据id为",i);
        else return new CommonResult(201,"新增失败",null);
    }

    /**
     * 通过userId，status状态查询锁价数据
     * @param request
     * @return
     */
    @GetMapping("/app/model/userLockListPage")
    public CommonResult userLockListPage(HttpServletRequest request){
        String pageNum = request.getParameter("pageNum");
        if (StringUtil.isEmpty(pageNum)) pageNum="1";
        String userId = request.getParameter("userId");
        if (StringUtil.isEmpty(userId)) return new CommonResult(500,"userId 为空");
        String status = request.getParameter("status");
        if (StringUtil.isEmpty(status)) return new CommonResult(500,"status 为空");
        if (!("0".equals(status)||"1".equals(status)||"2".equals(status)||"3".equals(status)||"4".equals(status))) return new CommonResult(500,"status 格式不正确");
        PageInfo pageInfo = modelService.userLockListPage(Integer.parseInt(pageNum),userId,Integer.parseInt(status));
        if (pageInfo.getList().size()>0) return new CommonResult(200,"查询成功",pageInfo);
        return new CommonResult(201,"未查询到结果",null);
    }

    /**
     * 通过锁价数据id修改balance余额,并修改为未完成状态
     * @param request
     * @return
     */
    @PostMapping("/model/updateUserLockBalance")
    public CommonResult updateUserLockBalance(HttpServletRequest request){
        String userLockId = request.getParameter("userLockId");
        if (StringUtil.isEmpty(userLockId)) return new CommonResult(500,"userLockId 为空");
        Integer i = modelService.updateUserLockBalance(userLockId);
        if (i>0) return new CommonResult(200,"成功");
        return new CommonResult(201,"失败");
    }

    /**
     * 通过每箱卷数查询纸箱单价
     * @param request
     * @return
     */
    @GetMapping("/app/model/selectModelCartonPipeNumber")
    public CommonResult selectModelCartonPipeNumber(HttpServletRequest request){
        List<ModelCarton> modelCartonList = modelService.selectModelCarton();
        if (modelCartonList.size()>0) return new CommonResult(200,"查询成功",modelCartonList);
        return new CommonResult(201,"未查询到结果",null);
    }

    /**
     * 分页查询纸箱单价
     * @return
     */
    @GetMapping("/model/selectModelCartonPipeNumberPage")
    public CommonResult selectModelCartonPipeNumberPage(HttpServletRequest request){
        String pageNum = request.getParameter("pageNum");
        if (StringUtil.isEmpty(pageNum)) pageNum="1";
        String length = request.getParameter("length");
        String width = request.getParameter("width");
        String height = request.getParameter("height");
        Map<String,String> map = new HashMap<>();
        map.put("pageNum",pageNum);map.put("length",length);
        map.put("width",width);map.put("height",height);
        PageInfo modelCartonList = modelService.selectModelCartonPage(map);
        if (modelCartonList.getList().size()>0) return new CommonResult(200,"查询成功",modelCartonList);
        return new CommonResult(201,"未查询到结果",null);
    }

    /**
     * 新增纸箱规格
     * @param request
     * @return
     */
    @PostMapping("/model/addModelCarton")
    public CommonResult addModelCarton(HttpServletRequest request){
        String length = request.getParameter("length");
        if (StringUtil.isEmpty(length)) return new CommonResult(500,"length 为空");
        String width = request.getParameter("width");
        if (StringUtil.isEmpty(width)) return new CommonResult(500,"width 为空");
        String height = request.getParameter("height");
        if (StringUtil.isEmpty(height)) return new CommonResult(500,"height 为空");
        String cartonPrice = request.getParameter("cartonPrice");
        if (StringUtil.isEmpty(cartonPrice)) return new CommonResult(500,"cartonPrice 为空");
        Map<String,String> map = new HashMap<>();
        map.put("length",length);map.put("width",width);
        map.put("height",height);map.put("cartonPrice",cartonPrice);
        Integer i = modelService.addModelCarton(map);
        if (i>0) return new CommonResult(200,"新增成功");
        return new CommonResult(201,"新增失败");
    }

    /**
     * 根据id删除纸箱规格
     * @param request
     * @return
     */
    @PostMapping("/model/delModelCarton")
    public CommonResult delModelCarton(HttpServletRequest request){
        String modelCartonId = request.getParameter("modelCartonId");
        if (StringUtil.isEmpty(modelCartonId)) return new CommonResult(500,"modelCartonId 为空");
        Integer i = modelService.delModelCarton(modelCartonId);
        if (i>0) return new CommonResult(200,"删除成功");
        return new CommonResult(201,"删除失败");
    }

    /**
     * 新增计算机型号推荐
     * @param request
     * @return
     */
    @PostMapping("/model/addCalculatorModelRecommend")
    public CommonResult addCalculatorModelRecommend(HttpServletRequest request){
//        String calculatorModelRecommendId = request.getParameter("calculatorModelRecommendId");
//        if (StringUtil.isEmpty(calculatorModelRecommendId)) return new CommonResult(500,"calculatorModelRecommendId 为空");
        String modelType = request.getParameter("modelType");
        if (StringUtil.isEmpty(modelType)) return new CommonResult(500,"modelType 为空");
        String machineType1 = request.getParameter("machineType1");
//        if (StringUtil.isEmpty(machineType1)) return new CommonResult(500,"machineType1 为空");
        String machineType2 = request.getParameter("machineType2");
//        if (StringUtil.isEmpty(machineType2)) return new CommonResult(500,"machineType2 为空");
        String stretchType = request.getParameter("stretchType");
//        if (StringUtil.isEmpty(stretchType)) return new CommonResult(500,"stretchType 为空");
        String stretchScope = request.getParameter("stretchScope");
//        if (StringUtil.isEmpty(stretchScope)) return new CommonResult(500,"stretchScope 为空");
        String packType = request.getParameter("packType");
        if (StringUtil.isEmpty(packType)) return new CommonResult(500,"packType 为空");
        String kgScope = request.getParameter("kgScope");
        if (StringUtil.isEmpty(kgScope)) return new CommonResult(500,"kgScope 为空");
        String modelRecommend = request.getParameter("modelRecommend");
        if (StringUtil.isEmpty(modelRecommend)) return new CommonResult(500,"modelRecommend 为空");
        CalculatorModelRecommend calculatorModelRecommend = new CalculatorModelRecommend();
//        calculatorModelRecommend.setId(calculatorModelRecommendId);
        calculatorModelRecommend.setModelType(Integer.parseInt(modelType));
        if (!StringUtil.isEmpty(machineType1)) calculatorModelRecommend.setMachineType1(Integer.parseInt(machineType1));
        if (!StringUtil.isEmpty(machineType2)) calculatorModelRecommend.setMachineType2(Integer.parseInt(machineType2));
        if (!StringUtil.isEmpty(stretchType)) calculatorModelRecommend.setStretchType(Integer.parseInt(stretchType));
        if (!StringUtil.isEmpty(stretchScope)) calculatorModelRecommend.setStretchScope(Integer.parseInt(stretchScope));
        calculatorModelRecommend.setPackType(Integer.parseInt(packType));
        calculatorModelRecommend.setKgScope(Integer.parseInt(kgScope));
        calculatorModelRecommend.setModelRecommend(modelRecommend);
        Integer i = modelService.addCalculatorModelRecommend(calculatorModelRecommend);
        if (i==201) return new CommonResult(201,"该配置型号已存在");
        else if (i>0) return new CommonResult(200,"新增成功");
        return new CommonResult(201,"新增失败");
    }

    /**
     * 删除计算机型号推荐
     * @param request
     * @return
     */
    @PostMapping("/model/delCalculatorModelRecommend")
    public CommonResult delCalculatorModelRecommend(HttpServletRequest request){
        String calculatorModelRecommendId = request.getParameter("calculatorModelRecommendId");
        if (StringUtil.isEmpty(calculatorModelRecommendId)) return new CommonResult(500,"calculatorModelRecommendId 为空");
        Integer i = modelService.delCalculatorModelRecommend(calculatorModelRecommendId);
        if (i>0) return new CommonResult(200,"删除成功");
        return new CommonResult(201,"删除失败");
    }

    /**
     * 分页查询计算器型号推荐
     * @param request
     * @return
     */
    @GetMapping("/model/selectCalculatorModelRecommendPage")
    public CommonResult selectCalculatorModelRecommendPage(HttpServletRequest request){
        String pageNum = request.getParameter("pageNum");
        if (StringUtil.isEmpty(pageNum)) return new CommonResult(500,"pageNum 为空");
        Map<String, String> map = new HashMap<>();
        map.put("pageNum",pageNum);
        PageInfo pageInfo = modelService.selectCalculatorModelRecommendPage(map);
        if (pageInfo.getList().size()>0) return new CommonResult(200,"查询成功",pageInfo);
        return new CommonResult(201,"未查询到结果",null);
    }

    /**
     * 指定配置新增/修改推荐型号
     * @param request
     * @return
     */
    @PostMapping("/model/addModelRecommend")
    public CommonResult addModelRecommend(HttpServletRequest request){
        String calculatorModelRecommendId = request.getParameter("calculatorModelRecommendId");
        if (StringUtil.isEmpty(calculatorModelRecommendId)) return new CommonResult(500,"calculatorModelRecommendId 为空");
        String modelRecommend = request.getParameter("modelRecommend");
        if (StringUtil.isEmpty(modelRecommend)) return new CommonResult(500,"modelRecommend 为空");
        Map map = new HashMap();
        map.put("calculatorModelRecommendId",calculatorModelRecommendId);
        map.put("modelRecommend",modelRecommend);
        Integer i = modelService.addModelRecommend(map);
        if (i>0) return new CommonResult(200,"更新成功");
        return new CommonResult(201,"更新失败");
    }

    /**
     * 通过配置查询推荐型号
     * @param request
     * @return
     */
    @GetMapping("/app/model/getCalculatorModelRecommend")
    public CommonResult getCalculatorModelRecommend(HttpServletRequest request){
//        String calculatorModelRecommendId = request.getParameter("calculatorModelRecommendId");
//        if (StringUtil.isEmpty(calculatorModelRecommendId)) return new CommonResult(500,"calculatorModelRecommendId 为空");
        String modelType = request.getParameter("modelType");
        if (StringUtil.isEmpty(modelType)) return new CommonResult(500,"modelType 为空");
        String machineType1 = request.getParameter("machineType1");
//        if (StringUtil.isEmpty(machineType1)) return new CommonResult(500,"machineType1 为空");
        String machineType2 = request.getParameter("machineType2");
//        if (StringUtil.isEmpty(machineType2)) return new CommonResult(500,"machineType2 为空");
        String stretchType = request.getParameter("stretchType");
//        if (StringUtil.isEmpty(stretchType)) return new CommonResult(500,"stretchType 为空");
        String stretchScope = request.getParameter("stretchScope");
//        if (StringUtil.isEmpty(stretchScope)) return new CommonResult(500,"stretchScope 为空");
        String packType = request.getParameter("packType");
        if (StringUtil.isEmpty(packType)) return new CommonResult(500,"packType 为空");
        String kgScope = request.getParameter("kgScope");
        if (StringUtil.isEmpty(kgScope)) return new CommonResult(500,"kgScope 为空");
//        String modelRecommend = request.getParameter("modelRecommend");
//        if (StringUtil.isEmpty(modelRecommend)) return new CommonResult(500,"modelRecommend 为空");
        CalculatorModelRecommend calculatorModelRecommend = new CalculatorModelRecommend();
//        calculatorModelRecommend.setId(calculatorModelRecommendId);
        calculatorModelRecommend.setModelType(Integer.parseInt(modelType));
        if (!StringUtil.isEmpty(machineType1)) calculatorModelRecommend.setMachineType1(Integer.parseInt(machineType1));
        if (!StringUtil.isEmpty(machineType2)) calculatorModelRecommend.setMachineType2(Integer.parseInt(machineType2));
        if (!StringUtil.isEmpty(stretchType)) calculatorModelRecommend.setStretchType(Integer.parseInt(stretchType));
        if (!StringUtil.isEmpty(stretchScope)) calculatorModelRecommend.setStretchScope(Integer.parseInt(stretchScope));
        calculatorModelRecommend.setPackType(Integer.parseInt(packType));
        calculatorModelRecommend.setKgScope(Integer.parseInt(kgScope));
//        calculatorModelRecommend.setModelRecommend(modelRecommend);
        CalculatorModelRecommend calculatorModelRecommend1 = modelService.getCalculatorModelRecommend(calculatorModelRecommend);
        if (calculatorModelRecommend1!=null) return new CommonResult(200,"查询成功",calculatorModelRecommend1);
        return new CommonResult(201,"该配置没有推荐型号",null);
    }

}
