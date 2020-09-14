package com.cosmo.service;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cosmo.dao.ConfigMapper;
import com.cosmo.entity.Config;
import com.cosmo.util.*;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ConfigService {
    @Resource
    private ConfigMapper configMapper;

    /**
     * 通过code，type查询
     * @return
     */
    public List<Config> customerService(String code, String type){
        QueryWrapper<Config> configQueryWrapper = new QueryWrapper<>();
        configQueryWrapper.eq("code",code).eq("type",type);
        return configMapper.selectList(configQueryWrapper);
    }

    /**
     * 查询配置杂项
     * @return
     */
    public List<Config> customerConfig(){
        QueryWrapper<Config> configQueryWrapper = new QueryWrapper<>();
        configQueryWrapper.eq("type",1);
        return configMapper.selectList(configQueryWrapper);
    }

    /**
     * 通过code查询配置杂项
     * @return
     */
    public Config codeConfig(String code){
        QueryWrapper<Config> configQueryWrapper = new QueryWrapper<>();
        configQueryWrapper.eq("type",1).eq("code",code);
        List<Config> configs = configMapper.selectList(configQueryWrapper);
        if (configs.size()<=0) return null;
        return configs.get(0);
    }

    /**
     * 修改客服信息
     *      先执行查询客服
     *      删除原客服二维码
     *      修改客服信息
     * @param map
     * @param file
     * @return
     */
    public int updateService(Map<String,String> map, MultipartFile file){
        Config config = this.customerService("service","2").get(0);
        Map<String,String> map1 = JSON.parseObject(config.getValue(),Map.class);
        if ("1".equals(map.get("type"))){
            if (map1!=null) FileUtil.delFile(map1.get("img"));
            map.put("img",FileUtil.upload(file));
        }else {
            map.put("img",map1.get("img"));
        }
        config.setValue(JSON.toJSONString(map));
        return configMapper.updateById(config);
    }


    /**
     * 通过配置类型查询 （分页）
     * @param pageNum
     * @param type
     * @return
     */
    public PageInfo configsType(Integer pageNum, Integer type, String code){
        QueryWrapper<Config> configQueryWrapper = new QueryWrapper<>();
        configQueryWrapper.eq("type",type);
        if (code!=null) configQueryWrapper.eq("code",code);
        Page page = new Page(pageNum,10);
        IPage<Config> configs = configMapper.selectPage(page,configQueryWrapper);
        PageInfo pageInfo = new PageInfo(configs);
        return pageInfo;
    }

    /**
     * 根据config实体类id修改
     * @param config
     * @return
     */
    public Integer updateById(Config config){
        return configMapper.updateById(config);
    }

    /**
     * 根据map 中id数组，value数组修改
     * @param map
     * @return
     */
    public Integer updateByIds(Map<String,String> map){
        List<Integer> ids = JSON.parseArray(map.get("ids"),Integer.class);
        List<String> values = JSON.parseArray(map.get("values"),String.class);
        if (ids.size()!=values.size()) return 0;
        List<Map<String,Object>> configList = new ArrayList<>();
        for (int i=0;i<ids.size();i++){
            Map<String,Object> config = new HashMap<>();
            config.put("id",ids.get(i));
            config.put("value",values.get(i));
            configList.add(config);
        }
        return configMapper.updateByIds(configList);
    }

    /**
     * 通过type修改所有value
     * @param type
     * @param value
     * @return
     */
    public Integer updateTypeValue(String type, String value){
        Config config = new Config();
        config.setValue(value);
        QueryWrapper<Config> configQueryWrapper = new QueryWrapper<>();
        configQueryWrapper.eq("type",type);
        return configMapper.update(config,configQueryWrapper);
    }

}
