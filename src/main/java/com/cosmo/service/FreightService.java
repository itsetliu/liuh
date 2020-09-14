package com.cosmo.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.cosmo.dao.FreightMapper;
import com.cosmo.dao.HatAreaMapper;
import com.cosmo.dao.HatCityMapper;
import com.cosmo.dao.HatProvinceMapper;
import com.cosmo.entity.*;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
public class FreightService {

    @Resource
    private FreightMapper freightMapper;
    @Resource//省
    private HatProvinceMapper hatProvinceMapper;
    @Resource//市
    private HatCityMapper hatCityMapper;
    @Resource//区
    private HatAreaMapper hatAreaMapper;

    /**
     * 更新区间运费单价
     * 没有时新增
     * @param freight
     * @return
     */
    public Integer updateFreight(Freight freight){
        QueryWrapper<Freight> freightQueryWrapper = new QueryWrapper<>();
        freightQueryWrapper.eq("hatID",freight.getHatid());
        List<Freight> freights = freightMapper.selectList(freightQueryWrapper);
        if (freights.size()>0){
            freight.setId(freights.get(0).getId());
            return freightMapper.updateById(freight);
        }
        return freightMapper.insert(freight);
    }

    /**
     * 查询所有省市
     * 市的所有运费区间
     * @return
     */
    public List<HatProvince> provinceCityList(){
        List<HatProvince> provinceList = hatProvinceMapper.selectList(null);
        provinceList.forEach(province->{
            province.setHatCityList(hatCityMapper.hatCity(province.getProvinceid()));
        });
        return provinceList;
    }


}
