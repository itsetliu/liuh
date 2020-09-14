package com.cosmo.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cosmo.entity.ModelThickness;
import java.util.List;
import java.util.Map;


public interface ModelThicknessMapper extends BaseMapper<ModelThickness> {

    /**
     * 批量新增厚度加工费
     * @param mapList
     * @return
     */
    Integer addModelThickness(List<Map<String, String>> mapList);
}