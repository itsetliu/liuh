package com.cosmo.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cosmo.entity.UserMemberModel;
import java.util.List;
import java.util.Map;


public interface UserMemberModelMapper extends BaseMapper<UserMemberModel> {

    List<Map<String,String>> selectUserMember(Map<String,String> map);

    UserMemberModel selectUserMemberMap(Map<String,String> map);
}