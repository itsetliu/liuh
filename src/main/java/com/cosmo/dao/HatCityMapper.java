package com.cosmo.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cosmo.entity.HatCity;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Param;

public interface HatCityMapper extends BaseMapper<HatCity> {
    /**
     * 通过父id查询
     * @param father
     * @return
     */
    List<HatCity> hatCity(String father);

    /**
     * 通过省市地名查询市唯一id
     * @return
     */
    String cityId(Map<String,String> map);

}