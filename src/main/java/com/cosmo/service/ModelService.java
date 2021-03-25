package com.cosmo.service;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cosmo.dao.*;
import com.cosmo.entity.*;
import com.cosmo.util.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
public class ModelService {

    @Resource
    private ModelMapper modelMapper;
    @Resource
    private ModelShowMapper modelShowMapper;
    @Resource
    private ModelThicknessMapper modelThicknessMapper;
    @Resource
    private ModelSuttleMapper modelSuttleMapper;
    @Resource
    private ModelCartonMapper modelCartonMapper;
    @Resource
    private ConfigMapper configMapper;
    @Resource
    private UserInfoMapper userInfoMapper;
    @Resource
    private UserLockMapper userLockMapper;
    @Resource
    private RedisUtil redisUtil;
    @Resource
    private CalculatorModelRecommendMapper calculatorModelRecommendMapper;

    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd");

    /**
     * 查询所有型号
     * @param type
     * @return
     */
    public List<Model> selectModel(String type){
        QueryWrapper<Model> modelQueryWrapper = new QueryWrapper<>();
        if (!StringUtil.isEmpty(type)) modelQueryWrapper.eq("type",type);
        return modelMapper.selectList(modelQueryWrapper);
    }

    /**
     * 查询所有型号（分页，模糊，类型）
     * @param pageNum
     * @param name
     * @param type
     * @return
     */
    public PageInfo modelList(Integer pageNum,String name,String type){
        QueryWrapper<Model> modelQueryWrapper = new QueryWrapper<>();
        if (!StringUtil.isEmpty(name)) modelQueryWrapper.like("name",name);
        if (!StringUtil.isEmpty(type)) modelQueryWrapper.eq("type",type);
        Page page = new Page(pageNum,10);
        IPage<Model> modelList = modelMapper.selectPage(page,modelQueryWrapper);
        for (Model model : modelList.getRecords()){
            QueryWrapper<ModelThickness> modelThicknessQueryWrapper = new QueryWrapper<>();
            modelThicknessQueryWrapper.eq("model_id",model.getId()).eq("status",0);
            model.setModelThicknessList(modelThicknessMapper.selectList(modelThicknessQueryWrapper));
        }
        PageInfo pageInfo = new PageInfo(modelList);
        return pageInfo;
    }

    /**
     * 通过modelId删除modelThickness
     * @param modelId
     * @return
     */
    public Integer delModelThickness(String modelId){
        QueryWrapper<ModelThickness> modelThicknessQueryWrapper = new QueryWrapper<>();
        modelThicknessQueryWrapper.eq("model_id",modelId);
        return modelThicknessMapper.delete(modelThicknessQueryWrapper);
    }

    /**
     * 批量新增厚度对应的加工费
     * @param modelId
     * @param modelThicknesss
     * @return
     */
    @Transactional(value="txManager1")
    public Integer addModelThickness(String modelId,String modelThicknesss){
        List<String> modelThickness = JSON.parseArray(modelThicknesss,String.class);
        Integer i = 0;
        for (String modelThicknes : modelThickness){
            String[] s=modelThicknes.split("-");
            ModelThickness modelThickness1 = new ModelThickness();
            modelThickness1.setModelId(modelId);
            modelThickness1.setThickness(s[0]);
            modelThickness1.setProcessCost(new BigDecimal(s[1]));
            i = i + modelThicknessMapper.insert(modelThickness1);
        }
        return i;
    }

    /**
     * 通过modelId删除modelSuttles
     * @param modelId
     * @return
     */
    public Integer delModelSuttles(String modelId){
        QueryWrapper<ModelSuttle> modelSuttleQueryWrapper = new QueryWrapper<>();
        modelSuttleQueryWrapper.eq("model_id",modelId);
        return modelSuttleMapper.delete(modelSuttleQueryWrapper);
    }

    /**
     * 批量新增净重
     * @param modelId
     * @param modelSuttles
     * @return
     */
    @Transactional(value="txManager1")
    public Integer addModelSuttles(String modelId,String modelSuttles){
        List<String> modelThickness = JSON.parseArray(modelSuttles,String.class);
        Integer i = 0;
        for (String modelSuttle : modelThickness){
            ModelSuttle modelSuttle1 = new ModelSuttle();
            modelSuttle1.setModelId(modelId);
            modelSuttle1.setSuttle(modelSuttle);
            i = i + modelSuttleMapper.insert(modelSuttle1);
        }
        return i;
    }

    /**
     * 新增型号
     *  新增型号
     *  新增型号对应原料
     *  批量新增厚度对应的加工费
     * @param map
     * @return
     */
    @Transactional(value="txManager1")
    public Integer addModel(Map<String,String> map){
        QueryWrapper<Config> configQueryWrapper = new QueryWrapper<>();
        configQueryWrapper.eq("code","LLDPEPrice").eq("type",0);
        List<Config> configs = configMapper.selectList(configQueryWrapper);
        Config config = new Config();
        config.setCode("LLDPE");
        config.setName(map.get("name"));
        config.setValue(configs.get(0).getValue());
        config.setType(0);
        Integer i = configMapper.insert(config);
        if (i<=0) return i;
        Model model = new Model();
        model.setName(map.get("name"));
        model.setTypeName(map.get("typeName"));
        model.setType(Integer.parseInt(map.get("type")));
        model.setWidth(map.get("width"));
        model.setConfigId(config.getId());
        i = modelMapper.insert(model);
        if (i<=0) return i;
        return this.addModelThickness(model.getId().toString(),map.get("modelThickness"));
//        i = this.addModelThickness(model.getId().toString(),map.get("modelThickness"));
//        if (i<=0) return i;
//        return this.addModelSuttles(model.getId().toString(),map.get("modelSuttles"));
    }

    /**
     * 根据modelId查询modelThickness集合
     * @param modelId
     * @return
     */
    public List<ModelThickness> modelThicknessList(String modelId){
        QueryWrapper<ModelThickness> modelThicknessQueryWrapper = new QueryWrapper<>();
        modelThicknessQueryWrapper.eq("model_id",modelId);
        return modelThicknessMapper.selectList(modelThicknessQueryWrapper);
    }
    /**
     * 根据modelId查询modelSuttles集合
     * @param modelId
     * @return
     */
    public List<ModelSuttle> modelSuttleList(String modelId){
        QueryWrapper<ModelSuttle> modelSuttleQueryWrapper = new QueryWrapper<>();
        modelSuttleQueryWrapper.eq("model_id",modelId);
        return modelSuttleMapper.selectList(modelSuttleQueryWrapper);
    }

    /**
     * 查询修改参数
     * @param modelId
     * @return
     */
    @Transactional(value="txManager1")
    public Map<String,Object> updateInfo(String modelId){
        Map<String,Object> map = new HashMap<>();
        map.put("modelThicknessList",this.modelThicknessList(modelId));
        map.put("modelSuttleList",this.modelSuttleList(modelId));
        Model model = modelMapper.selectById(modelId);
        map.put("config",configMapper.selectById(model.getConfigId()));
        return map;
    }

    /**
     * 修改型号
     *  修改型号
     *  修改型号对应原料
     *  通过modelId删除modelThickness
     *  批量新增厚度对应的加工费
     * @param map
     * @return
     */
    @Transactional(value="txManager1")
    public Integer updateModel(Map<String,String> map){
        Model model = modelMapper.selectById(map.get("id"));
        if (!StringUtil.isEmpty(map.get("name"))) model.setName(map.get("name"));
        if (!StringUtil.isEmpty(map.get("typeName"))) model.setTypeName(map.get("typeName"));
        if (!StringUtil.isEmpty(map.get("type"))) model.setType(Integer.parseInt(map.get("type")));
        Integer i = modelMapper.updateById(model);
        if (i<=0) return i;
        Config config = configMapper.selectById(model.getConfigId());
        if (!StringUtil.isEmpty(map.get("name"))) config.setName(map.get("name"));
//        if (!StringUtil.isEmpty(map.get("price"))) config.setValue(map.get("price"));
        i = configMapper.updateById(config);
        if (i<=0) return i;
        this.delModelThickness(model.getId());
        return this.addModelThickness(model.getId().toString(),map.get("modelThickness"));
//        i = this.addModelThickness(model.getId().toString(),map.get("modelThickness"));
//        if (i<=0) return i;
//        this.delModelSuttles(model.getId());
//        return this.addModelSuttles(model.getId().toString(),map.get("modelSuttles"));
    }

    /**
     * 通过modelId删除所有相关信息
     * @param modelId
     * @return
     */
    @Transactional(value="txManager1")
    public Integer delModel(String modelId){
        Model model = modelMapper.selectById(modelId);
        Integer i = modelMapper.deleteById(modelId);
        if (i<=0) return i;
        i = configMapper.deleteById(model.getConfigId());
        if (i<=0) return i;
        this.delModelThickness(modelId);
        this.delModelSuttles(modelId);
        return i;
    }


    /**
     * 查询所有型号
     * 对应的原料价格
     * 对应的厚度加工费
     * @return
     */
    public Map<String,List<Model>> selectModel(){
        Map<String,List<Model>> map = new HashMap<>();
        map.put("manual",modelMapper.selectModel("0"));
        map.put("machine",modelMapper.selectModel("1"));
        return map;
    }


    /**
     * 计算价格
     * @param map
     * @return
     */
    public BigDecimal calculate(Map<String,String> map){
        //(LLDPE单价<原料> + 加工费) * 总净量 + (纸箱重量 * 订单卷数 / 装箱卷数 * 7<纸箱单价/kg>)
        // + (管重 * 订单卷数 * 4.5<纸管单价>) + 托盘单价
        //订单卷数
        BigDecimal juanShu = new BigDecimal(1000).divide(new BigDecimal(map.get("suttle")),2, RoundingMode.HALF_UP);
        //膜
        BigDecimal mo = (new BigDecimal(map.get("LLDPE")).add(new BigDecimal(map.get("processCost")))).multiply(new BigDecimal(1000));
        //纸箱 (默认每箱一卷)
        BigDecimal zhiXiang = new BigDecimal(map.get("boxWeigth")).multiply(juanShu).multiply(new BigDecimal(map.get("boxPrice")));
        //纸管
        BigDecimal zhiGuan = new BigDecimal(map.get("pipeWeight")).multiply(juanShu).multiply(new BigDecimal(map.get("tubePrice")));
        //托盘
        BigDecimal tuoPan = new BigDecimal(map.get("trayPrice"));
        return mo.add(zhiXiang).add(zhiGuan).add(tuoPan);
    }

    /**
     * 查询首页
     * @return
     */
    public List<Map<String,Object>> home(){
        List<ModelShow> modelShowList = modelShowMapper.selectList(null);
        List<Map<String,Object>> mapList = new ArrayList<>();
        for (int i = 0;i<modelShowList.size();i++){
            ModelShow modelShow = modelShowList.get(i);
            Map<String,Object> map = new HashMap<>();
            map.put("modelId",modelShow.getModelId());
            map.put("name",modelShow.getName());
            map.put("modelName",modelShow.getModelName());
            map.put("type",modelShow.getType());
            map.put("typeName",modelShow.getTypeName());
            map.put("suttle",modelShow.getSuttle());
            map.put("pipeWeight",modelShow.getPipeWeight());
            map.put("width",modelShow.getWidth());
            map.put("thickness",Integer.parseInt(modelShow.getThickness()));
            map.put("scope",modelShow.getScope());
            map.put("volume",modelShow.getVolume());
            //求现售预售
//            Map<String,String> map1 = new HashMap<>();
//            map1.put("pipeWeight",modelShow.getPipeWeight());
//            map1.put("suttle",modelShow.getSuttle());
            QueryWrapper<ModelThickness> modelThicknessQueryWrapper = new QueryWrapper<>();
            modelThicknessQueryWrapper.eq("model_id",modelShow.getModelId()).eq("thickness",Integer.parseInt(modelShow.getThickness()));
            List<ModelThickness> modelThicknesses = modelThicknessMapper.selectList(modelThicknessQueryWrapper);
//            map1.put("processCost",modelThicknesses.get(0).getProcessCost().toString());
//            QueryWrapper<Config> configQueryWrapper1 = new QueryWrapper<>();
//            configQueryWrapper1.eq("code","boxWeigth");
//            List<Config> boxWeigth = configMapper.selectList(configQueryWrapper1);
//            map1.put("boxWeigth",boxWeigth.get(0).getValue());
//            QueryWrapper<Config> configQueryWrapper2 = new QueryWrapper<>();
//            configQueryWrapper2.eq("code","boxPrice");
////            List<Config> boxPrice = configMapper.selectList(configQueryWrapper2);
////            map1.put("boxPrice",boxPrice.get(0).getValue());
//            map1.put("boxPrice","7");
//            QueryWrapper<Config> configQueryWrapper3 = new QueryWrapper<>();
//            configQueryWrapper3.eq("code","tubePrice");
//            List<Config> tubePrice = configMapper.selectList(configQueryWrapper3);
//            map1.put("tubePrice",tubePrice.get(0).getValue());
//            QueryWrapper<Config> configQueryWrapper4 = new QueryWrapper<>();
//            configQueryWrapper4.eq("code","trayPrice");
//            List<Config> trayPrice = configMapper.selectList(configQueryWrapper4);
//            map1.put("trayPrice",trayPrice.get(0).getValue());
            Model model = modelMapper.selectById(modelShow.getModelId());
            Config LLDPE = configMapper.selectById(model.getConfigId());
            List<String> LLDPEList = JSON.parseArray(LLDPE.getValue(),String.class);

//            map1.put("LLDPE",(String) JSON.parseObject(LLDPEList.get(0), HashMap.class).get("value"));
//            map.put("nowLLDPE",map1.get("LLDPE"));
            map.put("nowLLDPE",(String) JSON.parseObject(LLDPEList.get(0), HashMap.class).get("value"));
//            map.put("now",this.calculate(map1));
//            map1.put("LLDPE",(String) JSON.parseObject(LLDPEList.get(1), HashMap.class).get("value"));
//            map.put("predictLLDPE",map1.get("LLDPE"));
            map.put("predictLLDPE",(String) JSON.parseObject(LLDPEList.get(1), HashMap.class).get("value"));
//            map.put("predict",this.calculate(map1));
//            map.put("processCost",map1.get("processCost"));
            map.put("processCost",modelThicknesses.get(0).getProcessCost().toString());
            mapList.add(map);
        }
        return mapList;
    }

    /**
     * 根据型号id查询
     * @param modelId
     * @return
     */
    public Map<String,Object> byId(String modelId){
        Map<String,Object> map = new HashMap<>();
        Model model = modelMapper.selectById(modelId);
        map.put("id",model.getId());
        map.put("type",model.getType());
        map.put("name",model.getName());
        map.put("status",model.getStatus());
        map.put("suttle",model.getSuttle());
        map.put("pipeWeight",model.getPipeWeight());
        map.put("width",model.getWidth());
        map.put("scope",model.getScope());
        map.put("volume",model.getVolume());
        Config config = configMapper.selectById(model.getConfigId());
        map.put("LLDPE",config.getValue());
        QueryWrapper<ModelThickness> modelThicknessQueryWrapper = new QueryWrapper<>();
        modelThicknessQueryWrapper.eq("model_id",modelId);
        map.put("modelThicknessList",modelThicknessMapper.selectList(modelThicknessQueryWrapper));
        return map;
    }

    /**
     * 新增型号规格首页显示
     * @param modelShow
     * @return
     */
    public Integer addModelShow(ModelShow modelShow){
        return modelShowMapper.insert(modelShow);
    }

    /**
     * 修改型号规格首页显示
     * @param modelShow
     * @return
     */
    public Integer updateModelShow(ModelShow modelShow){
        return modelShowMapper.updateById(modelShow);
    }

    /**
     * 根据id删除型号规格首页显示
     * @param modelShowId
     * @return
     */
    public Integer delModelShow(String modelShowId){
        return modelShowMapper.deleteById(modelShowId);
    }

    /**
     * 分页查询小程序首页显示
     * @param pageNum
     * @return
     */
    public PageInfo selectModelShow(Integer pageNum){
        Page page = new Page(pageNum,10);
        IPage<ModelShow> modelShowList = modelShowMapper.selectPage(page,null);
        PageInfo pageInfo = new PageInfo(modelShowList);
        return pageInfo;
    }

    /**
     * 修改所有scope
     * @param scope
     * @return
     */
    public Integer updateModelShowScope(String scope){
        ModelShow modelShow = new ModelShow();
        modelShow.setScope(scope);
        return modelShowMapper.update(modelShow,null);
    }

    /**
     * 通过code获取value
     * 这里指获取纸箱托盘重量使用
     * @param code
     * @return
     */
    private String getConfigValue(String code){
        QueryWrapper<Config> configQueryWrapper = new QueryWrapper<>();
        configQueryWrapper.eq("code",code);
        return configMapper.selectList(configQueryWrapper).get(0).getValue();
    }

    /**
     * 新建首页锁价
     * @param map
     * @return
     *      201:userId错误/不存在
     */
    public String lockPrice(Map<String,String> map){
        UserInfo userInfo = userInfoMapper.selectById(map.get("userId"));
        if (userInfo==null) return "201";
        List<String> nameList = new ArrayList<>();
        List<Map<String,Object>> mapList = new ArrayList<>();
        UserLock userLock = new UserLock();
        String lockPrice = map.get("lockPrice");
        List<String> lockPriceList = JSON.parseArray(lockPrice,String.class);
        lockPriceList.forEach(lockPriceMap -> {
            Map<String,Object> map1 = JSON.parseObject(lockPriceMap,Map.class);
            if (nameList.size()>0){
                boolean[] is = {true};
                nameList.forEach(name -> {if (name.equals(String.valueOf(map1.get("modelName")))){ is[0]=false; return; }});
                if (is[0]){
                    nameList.add((String) map1.get("modelName"));
                    mapList.add(map1);
                }
            }else {
                nameList.add((String) map1.get("modelName"));
                mapList.add(map1);
            }
        });
        userLock.setLockPrice(JSON.toJSONString(mapList));
        userLock.setUserId(map.get("userId"));
        userLock.setNumber("lockPrice"+new Date().getTime());
        userLock.setStatus(1);
        userLock.setTimeCreate(sdf.format(new Date()));
        userLock.setMargin(new BigDecimal(map.get("margin")));
        userLock.setObligation(new BigDecimal(map.get("obligation")));
        userLock.setPriceMargin(new BigDecimal(map.get("margin")));
        userLock.setPrice(new BigDecimal(map.get("margin")).add(new BigDecimal(map.get("obligation"))));
        Integer i = userLockMapper.insert(userLock);
        if(i<=0) return null;
        //把锁价数据编号存到redis中
//        Integer lockGuaranteeGoldTime = Integer.parseInt(this.getConfigValue("lockGuaranteeGoldTime"));
        Integer lockGuaranteeGoldTime = 0;
        Date nowSpecificDate = null;
        try {
            nowSpecificDate = sdf.parse(sdf1.format(new Date())+" 08:59:59");
        } catch (ParseException e) {
            e.printStackTrace();
        }
//        if ((new Date()).compareTo(nowSpecificDate)==-1){//当前时间小于当天 08:59:59
//            lockGuaranteeGoldTime = (int)((nowSpecificDate.getTime() - (new Date()).getTime())/1000);
//        }else {//当前时间大于当天 08:59:59
//            Calendar calendar = Calendar.getInstance();
//            calendar.setTime(nowSpecificDate);
//            calendar.add(Calendar.DAY_OF_MONTH, +1);//传入的时间加1天
//            Date tomorrowSpecificDate = calendar.getTime();
//            lockGuaranteeGoldTime = (int)((tomorrowSpecificDate.getTime() - (new Date()).getTime())/1000);
//        }
//        redisUtil.set("lockPrice1,"+userLock.getId(),"lockPrice1,"+userLock.getId());
//        redisUtil.expire("lockPrice1,"+userLock.getId(),lockGuaranteeGoldTime, TimeUnit.SECONDS);
        return userLock.getId();
    }

    /**
     * 通过userId，status状态查询锁价数据
     * @param pageNum
     * @param userId
     * @param status
     * @return
     */
    public PageInfo userLockListPage(Integer pageNum, String userId, Integer status){
        QueryWrapper<UserLock> userLockQueryWrapper = new QueryWrapper<>();
        userLockQueryWrapper.eq("user_id",userId).eq("status",status);
        Page page = new Page(pageNum,10);
        IPage<UserLock> userLockList = userLockMapper.selectPage(page,userLockQueryWrapper);
        PageInfo pageInfo = new PageInfo(userLockList);
        return pageInfo;
    }

    /**
     * 通过锁价数据id修改balance余额,并修改为未完成状态
     * @param userLockId
     * @return
     */
    public Integer updateUserLockBalance(String userLockId){
        UserLock userLock = userLockMapper.selectById(userLockId);
        userLock.setStatus(2);
        return userLockMapper.updateById(userLock);
    }

    /**
     * 查询纸箱单价
     * @return
     */
    public List<ModelCarton> selectModelCarton(){
        QueryWrapper<ModelCarton> modelCartonQueryWrapper = new QueryWrapper<>();
        modelCartonQueryWrapper.orderBy(true,true,"width","height");
        return modelCartonMapper.selectList(modelCartonQueryWrapper);
    }

    /**
     * 查询纸箱单价
     * @param map
     * @return
     */
    public PageInfo selectModelCartonPage(Map<String,String> map){
        Page page = new Page(Long.valueOf(map.get("pageNum")),10);
        QueryWrapper<ModelCarton> modelCartonQueryWrapper = new QueryWrapper<>();
        if (!StringUtil.isEmpty(map.get("length"))) modelCartonQueryWrapper.like("length",map.get("length"));
        if (!StringUtil.isEmpty(map.get("width"))) modelCartonQueryWrapper.like("width",map.get("width"));
        if (!StringUtil.isEmpty(map.get("height"))) modelCartonQueryWrapper.like("height",map.get("height"));
        return new PageInfo(modelCartonMapper.selectPage(page,modelCartonQueryWrapper));
    }

    /**
     * 新增纸箱规格
     * @param map
     * @return
     */
    public Integer addModelCarton(Map<String,String> map){
        ModelCarton modelCarton = new ModelCarton();
        modelCarton.setLength(Integer.parseInt(map.get("length")));
        modelCarton.setWidth(Integer.parseInt(map.get("width")));
        modelCarton.setHeight(Integer.parseInt(map.get("height")));
        modelCarton.setCartonPrice(new BigDecimal(map.get("cartonPrice")));
        return modelCartonMapper.insert(modelCarton);
    }

    /**
     * 根据id删除纸箱规格
     * @param modelCartonId
     * @return
     */
    public Integer delModelCarton(String modelCartonId){
        return modelCartonMapper.deleteById(modelCartonId);
    }

    /**
     * 新增计算机型号推荐
     * @param calculatorModelRecommend
     * @return
     *  201: 该配置已存在
     */
    public Integer addCalculatorModelRecommend(CalculatorModelRecommend calculatorModelRecommend){
        QueryWrapper<CalculatorModelRecommend> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("model_type",calculatorModelRecommend.getModelType());
        if (calculatorModelRecommend.getMachineType1()!=null) queryWrapper.eq("machine_type1",calculatorModelRecommend.getMachineType1());
        else queryWrapper.isNull("machine_type1");
        if (calculatorModelRecommend.getMachineType2()!=null) queryWrapper.eq("machine_type2",calculatorModelRecommend.getMachineType2());
        else queryWrapper.isNull("machine_type2");
        if (calculatorModelRecommend.getStretchType()!=null) queryWrapper.eq("stretch_type",calculatorModelRecommend.getStretchType());
        else queryWrapper.isNull("stretch_type");
        if (calculatorModelRecommend.getStretchScope()!=null) queryWrapper.eq("stretch_scope",calculatorModelRecommend.getStretchScope());
        else queryWrapper.isNull("stretch_scope");
        queryWrapper.eq("pack_type",calculatorModelRecommend.getPackType());
        queryWrapper.eq("kg_scope",calculatorModelRecommend.getKgScope());
        List<CalculatorModelRecommend> calculatorModelRecommendList = calculatorModelRecommendMapper.selectList(queryWrapper);
        if (calculatorModelRecommendList.size()>0) return 201;
        return calculatorModelRecommendMapper.insert(calculatorModelRecommend);
    }

    /**
     * 删除计算器型号推荐
     * @param calculatorModelRecommendId
     * @return
     */
    public Integer delCalculatorModelRecommend(String calculatorModelRecommendId){
        return calculatorModelRecommendMapper.deleteById(calculatorModelRecommendId);
    }

    /**
     * 分页查询计算器型号推荐
     * @param map
     * @return
     */
    public PageInfo selectCalculatorModelRecommendPage(Map<String, String> map){
        Page page = new Page(Long.valueOf(map.get("pageNum")),10);
        return new PageInfo(calculatorModelRecommendMapper.selectPage(page,null));
    }

    /**
     * 指定配置新增/修改推荐型号
     * @param map
     * @return
     */
    public Integer addModelRecommend(Map<String, String> map){
        QueryWrapper<CalculatorModelRecommend> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("id", map.get("calculatorModelRecommendId"));
        CalculatorModelRecommend calculatorModelRecommend = new CalculatorModelRecommend();
        calculatorModelRecommend.setModelRecommend(map.get("modelRecommend"));
        return calculatorModelRecommendMapper.update(calculatorModelRecommend,queryWrapper);
    }

    /**
     * 通过配置查询推荐型号
     * @param calculatorModelRecommend
     * @return
     */
    public CalculatorModelRecommend getCalculatorModelRecommend(CalculatorModelRecommend calculatorModelRecommend){
        QueryWrapper<CalculatorModelRecommend> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("model_type",calculatorModelRecommend.getModelType());
        if (calculatorModelRecommend.getMachineType1()!=null) queryWrapper.eq("machine_type1",calculatorModelRecommend.getMachineType1());
        else queryWrapper.isNull("machine_type1");
        if (calculatorModelRecommend.getMachineType2()!=null) queryWrapper.eq("machine_type2",calculatorModelRecommend.getMachineType2());
        else queryWrapper.isNull("machine_type2");
        if (calculatorModelRecommend.getStretchType()!=null) queryWrapper.eq("stretch_type",calculatorModelRecommend.getStretchType());
        else queryWrapper.isNull("stretch_type");
        if (calculatorModelRecommend.getStretchScope()!=null) queryWrapper.eq("stretch_scope",calculatorModelRecommend.getStretchScope());
        else queryWrapper.isNull("stretch_scope");
        queryWrapper.eq("pack_type",calculatorModelRecommend.getPackType());
        queryWrapper.eq("kg_scope",calculatorModelRecommend.getKgScope());
        List<CalculatorModelRecommend> calculatorModelRecommendList = calculatorModelRecommendMapper.selectList(queryWrapper);
        if (calculatorModelRecommendList.size()>0) return calculatorModelRecommendList.get(0);
        return null;
    }

}
