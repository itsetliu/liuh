package com.cosmo.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cosmo.entity.UserInfo;
import java.util.Map;

public interface UserInfoMapper extends BaseMapper<UserInfo> {

    /**
     * 通过openId查询用户
     * @param openId
     * @return
     */
    Map<String,Object> selectByOpenId(String openId);
}