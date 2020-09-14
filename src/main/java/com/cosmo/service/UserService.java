package com.cosmo.service;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cosmo.dao.*;
import com.cosmo.entity.*;
import com.cosmo.util.*;
import org.springframework.cglib.beans.BeanMap;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
public class UserService {
    @Resource
    private UserInfoMapper userInfoMapper;
    @Resource
    private UserRessMapper userRessMapper;
    @Resource
    private UserInvoiceMapper userInvoiceMapper;
    @Resource
    private UserPriceInfoMapper userPriceInfoMapper;
    @Resource
    private UserMemberMapper userMemberMapper;
    @Resource
    private UserMemberApplyMapper userMemberApplyMapper;
    @Resource
    private UserMemberModelMapper userMemberModelMapper;
    @Resource
    private HatCityMapper hatCityMapper;
    @Resource
    private CouponMapper couponMapper;
    @Resource
    private ConfigMapper configMapper;
    @Resource
    private RedisUtil redisUtil;


    SimpleDateFormat ft = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    /**
     * 查询所有小程序用户 （分页，模糊）
     * @param map
     * @return
     */
    public PageInfo userList(Map<String,Object> map){
        String pageNum = map.get("pageNum").toString();
        if (StringUtil.isEmpty(pageNum)) pageNum = "1";
        QueryWrapper<UserInfo> userInfoQueryWrapper = new QueryWrapper<>();
        userInfoQueryWrapper.eq("status",map.get("status"));
        String wxName = map.get("wxName").toString();
        if (!StringUtil.isEmpty(wxName)) userInfoQueryWrapper.like("wx_name",wxName);
        String serialNumber = map.get("serialNumber").toString();
        if (!StringUtil.isEmpty(serialNumber)) userInfoQueryWrapper.likeRight("serial_number",serialNumber+"-");
        Page page = new Page(Integer.parseInt(pageNum),10);
        IPage<UserInfo> sysUserList = userInfoMapper.selectPage(page,userInfoQueryWrapper);
        PageInfo pageInfo = new PageInfo(sysUserList);
        return pageInfo;
    }

    /**
     * 根据id删除小程序用户
     * 删除用户收货地址
     * 删除用户开票信息
     * @param id
     * @return
     */
    @Transactional(value="txManager1")
    public Integer delUserInfo(Integer id){
        Integer i = userInfoMapper.deleteById(id);
        QueryWrapper<UserRess> userRessQueryWrapper = new QueryWrapper<>();
        userRessQueryWrapper.eq("user_id",id);
        this.userRessMapper.delete(userRessQueryWrapper);
        QueryWrapper<UserInvoice> userInvoiceQueryWrapper = new QueryWrapper<>();
        userInvoiceQueryWrapper.eq("user_id",id);
        this.userInvoiceMapper.delete(userInvoiceQueryWrapper);
        return i;
    }



    /**
     * 通过id设置地址默认
     * 取消其他默认
     * @param userRess
     * @return
     */
    @Transactional(value="txManager1")
    public boolean updateStatus(UserRess userRess){
        QueryWrapper<UserRess> userRessQueryWrapper = new QueryWrapper<>();
        userRessQueryWrapper.eq("user_id",userRess.getUserId());
        UserRess userAddress1 = new UserRess();
        userAddress1.setStatus(1);
        userRessMapper.update(userAddress1,userRessQueryWrapper);
        QueryWrapper<UserRess> userRessQueryWrapper1 = new QueryWrapper<>();
        userRessQueryWrapper1.eq("id",userRess.getId());
        userAddress1.setStatus(0);
        int i = userRessMapper.update(userAddress1,userRessQueryWrapper1);
        if (i>0)return true;
        return false;
    }

    /**
     * 通过地址查询该地址有无运费
     * @param address
     * @return
     */
    public boolean addressExist (String address){
        String[] addresss = address.split(" ");
        Map<String,String> cityIdMap = new HashMap<>();
        cityIdMap.put("province",addresss[0]);
        cityIdMap.put("city",addresss[1]);
        String cityId = hatCityMapper.cityId(cityIdMap);
        if (cityId!=null&&cityId!="") return true;
        return false;
    }

    /**
     * 新增收货地址
     * @param userRess
     * @return
     */
    @Transactional(value="txManager1")
    public Map<String, Object> addRess(UserRess userRess){
        Map<String, Object> map = new HashMap<>();
        if (userRess.getType()==0){
            if (this.countRess(userRess.getUserId(),userRess.getType())>=1) {
                map.put("boolean",false);
                map.put("msg","已存在自提信息");
                return map;
            }
        }else if (userRess.getType()==1) {
            if (this.countRess(userRess.getUserId(),userRess.getType())>=10) {
                map.put("boolean",false);
                map.put("msg","地址总量超过10个");
                return map;
            }
        }
        userRess.setCreateTime(ft.format(new Date()));
        Integer i = userRessMapper.insert(userRess);
        if (userRess.getStatus()==0){
            boolean updateStatus = this.updateStatus(userRess);
        }

        if (i>0&&i!=null){
            map.put("boolean",true);
            map.put("msg","新增成功");
            return map;
        }
        map.put("boolean",false);
        map.put("msg","新增失败");
        return map;
    }

    /**
     * 根据用户id查询收货地址总数
     * @param userId
     * @return
     */
    public int countRess(long userId,int type){
        QueryWrapper<UserRess> userRessQueryWrapper = new QueryWrapper<>();
        userRessQueryWrapper.eq("user_id",userId).eq("type",type);
        return userRessMapper.selectCount(userRessQueryWrapper);
    }

    /**
     * 修改收货地址
     * @param userRess
     * @return
     */
    public boolean updateRess(UserRess userRess){
        Integer i = null;
        if (userRess.getStatus()==0){
            i = userRessMapper.updateById(userRess);
            boolean updateStatus = this.updateStatus(userRess);
        }else {
            i = userRessMapper.updateById(userRess);
        }

        if (i>0&&i!=null) return true;
        return false;
    }

    /**
     * 删除收货地址
     * @param id
     * @return
     */
    public boolean delRess(Integer id){
        Integer i = userRessMapper.deleteById(id);
        if (i>0) return true;
        return false;
    }

    /**
     * 根据用户id查询地址列表并升序type，及自提地址在最前
     * @param userId
     * @return
     */
    public List<UserRess> userResses(Integer userId){
        QueryWrapper<UserRess> userRessQueryWrapper = new QueryWrapper<>();
        userRessQueryWrapper.eq("user_id",userId).orderByDesc("type");
        List<UserRess> userRessList = userRessMapper.selectList(userRessQueryWrapper);
        return userRessList;
    }

    /**
     * 根据用户id、type查询地址列表
     * @param userId
     * @return
     */
    public List<UserRess> userRessList(Integer userId,Integer type){
        QueryWrapper<UserRess> userRessQueryWrapper = new QueryWrapper<>();
        userRessQueryWrapper.eq("user_id",userId).eq("type",type);
        List<UserRess> userRessList = userRessMapper.selectList(userRessQueryWrapper);
        return userRessList;
    }

    /**
     * 根据用户id查询发票信息
     * @param userId
     * @return
     */
    public List<UserInvoice> userInvoices(Integer userId){
        QueryWrapper<UserInvoice> userInvoiceQueryWrapper = new QueryWrapper<>();
        userInvoiceQueryWrapper.eq("user_id",userId);
        List<UserInvoice> userInvoiceList = userInvoiceMapper.selectList(userInvoiceQueryWrapper);
        return userInvoiceList;
    }

    /**
     * 通过id设置开票信息默认
     * 取消其他默认
     * @param userInvoice
     * @return
     */
    @Transactional(value="txManager1")
    public boolean updateInvoiceStatus(UserInvoice userInvoice){
        QueryWrapper<UserInvoice> userInvoiceQueryWrapper = new QueryWrapper<>();
        userInvoiceQueryWrapper.eq("user_id",userInvoice.getUserId());
        UserInvoice userInvoice1 = new UserInvoice();
        userInvoice1.setStatus(1);
        userInvoiceMapper.update(userInvoice1,userInvoiceQueryWrapper);
        userInvoice1.setId(userInvoice.getId());
        userInvoice1.setStatus(0);
        int i = userInvoiceMapper.updateById(userInvoice1);
        if (i>0)return true;
        return false;
    }

    /**
     * 根据用户id查询开票信息总数
     * @param userId
     * @return
     */
    public int countInvoice(int userId){
        QueryWrapper<UserInvoice> userInvoiceQueryWrapper = new QueryWrapper<>();
        userInvoiceQueryWrapper.eq("user_id",userId);
        return userInvoiceMapper.selectCount(userInvoiceQueryWrapper);
    }

    /**
     * 新增开票信息
     * @param userInvoice
     * @return
     */
    @Transactional(value="txManager1")
    public Integer addInvoice(UserInvoice userInvoice){
        userInvoice.setCreateTime(ft.format(new Date()));
        Integer i = userInvoiceMapper.insert(userInvoice);
        if (userInvoice.getStatus()==0){
            boolean updateInvoiceStatus = this.updateInvoiceStatus(userInvoice);
        }
        return i;
    }

    /**
     * 修改开票信息
     * @param userInvoice
     * @return
     */
    @Transactional(value="txManager1")
    public Integer updateInvoice(UserInvoice userInvoice){
        Integer i = userInvoiceMapper.updateById(userInvoice);
        if (userInvoice.getStatus()==0){
            boolean updateInvoiceStatus = this.updateInvoiceStatus(userInvoice);
        }
        return i;
    }

    /**
     * 根据id删除开票信息
     * @param id
     * @return
     */
    public Integer delInvoice(Integer id){
        return userInvoiceMapper.deleteById(id);
    }

    /**
     * 分页查询所有返现红包
     * @param pageNum
     * @param status
     * @return
     */
    public PageInfo selectCoupon(Integer pageNum,Integer status){
        QueryWrapper<Coupon> couponQueryWrapper = new QueryWrapper<>();
        couponQueryWrapper.eq("status",status);
        Page page = new Page(pageNum,10);
        IPage<Coupon> coupons = couponMapper.selectPage(page,couponQueryWrapper);
        PageInfo pageInfo = new PageInfo(coupons);
        for (int i = 0;i<pageInfo.getList().size();i++){
            Coupon coupon = (Coupon)pageInfo.getList().get(i);
            coupon.setUser(userInfoMapper.selectById(coupon.getUserId()));
            if (!"[]".equals(coupon.getAgoUserId())){
                List<Integer> userIdList = JSON.parseArray(coupon.getAgoUserId(),Integer.class);
                QueryWrapper<UserInfo> userInfoQueryWrapper = new QueryWrapper<>();
                userInfoQueryWrapper.in("id",userIdList);
                coupon.setAgoUser(userInfoMapper.selectList(userInfoQueryWrapper));
            }
        }
        return pageInfo;
    }

    /**
     * 分页查询该用户所有返现红包
     * @param pageNum
     * @param status
     * @return
     */
    public PageInfo selectUserCoupon(Integer pageNum, Integer userId, Integer status){
        QueryWrapper<Coupon> couponQueryWrapper = new QueryWrapper<>();
        couponQueryWrapper.eq("user_id",userId).eq("status",status);
        Page page = new Page(pageNum,10);
        IPage<Coupon> coupons = couponMapper.selectPage(page,couponQueryWrapper);
        PageInfo pageInfo = new PageInfo(coupons);
        for (int i = 0;i<pageInfo.getList().size();i++){
            Coupon coupon = (Coupon)pageInfo.getList().get(i);
            if (!"[]".equals(coupon.getAgoUserId())){
                List<Integer> userIdList = JSON.parseArray(coupon.getAgoUserId(),Integer.class);
                QueryWrapper<UserInfo> userInfoQueryWrapper = new QueryWrapper<>();
                userInfoQueryWrapper.in("id",userIdList);
                coupon.setAgoUser(userInfoMapper.selectList(userInfoQueryWrapper));
            }
        }
        return pageInfo;
    }

    /**
     * 指定用户新增返现红包
     * @param map
     * @return
     */
    public Integer addCoupon(Map<String,String> map){
        Integer index = 0;
        for (int i=0;i<Integer.parseInt(map.get("number"));i++){
            Coupon coupon = new Coupon();
            coupon.setUserId(Long.valueOf(map.get("userId")));
            coupon.setAgoUserId("[]");
            coupon.setName(map.get("name"));
            coupon.setFull(new BigDecimal(map.get("full")));
            coupon.setSubtract(new BigDecimal(map.get("subtract")));
            coupon.setStatus(0);
            coupon.setTime(map.get("time"));
            try {
                index = index + couponMapper.insert(coupon);
                //获取过期时间与当前时间的秒差
                int second = (int)((ft.parse(coupon.getTime()).getTime() - new Date().getTime()) / 1000);
                //把红包id存到redis中
                redisUtil.set("couponId,"+coupon.getId(),"couponId,"+coupon.getId());
                redisUtil.expire("couponId,"+coupon.getId(),second, TimeUnit.SECONDS);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        return index;
    }

    /**
     * 领取分享的红包
     * @param map
     * @return 0领取成功，
     *         1该红包已被领取，
     *         2该红包已过期，
     *         3该红包已使用，
     *         4领取失败
     *         5不可领取自己的分享
     */
    public Integer getShare(Map<String,String> map){
        Coupon coupon = couponMapper.selectById(Integer.parseInt(map.get("couponId")));
        if (coupon.getUserId()!=Integer.parseInt(map.get("agoUserId"))) return 1;
        try {
            Date time = ft.parse(coupon.getTime());
            int compareTo = new Date().compareTo(time);
            if (compareTo==-1) return 2;
        } catch (ParseException e) {
            e.printStackTrace();
        }
        if (coupon.getStatus()!=0) return 3;
        if (Integer.parseInt(map.get("agoUserId"))==Integer.parseInt(map.get("userId"))) return 5;
        List<Integer> agoUserId = JSON.parseArray(coupon.getAgoUserId(),Integer.class);
        agoUserId.add(Integer.parseInt(map.get("agoUserId")));
        coupon.setAgoUserId(JSON.toJSONString(agoUserId));
        coupon.setUserId(Long.valueOf(map.get("userId")));
        int i = couponMapper.updateById(coupon);
        if (i>0) return 0;
        return 4;
    }

    /**
     * 根据sataus查询该用户的红包
     * @param map
     * @return
     */
    public List<Map<String,Object>> couponList(Map<String,String> map){
        QueryWrapper<Coupon> couponQueryWrapper = new QueryWrapper<>();
        couponQueryWrapper.eq("status",map.get("status")).eq("user_id",map.get("userId"));
        List<Coupon> coupons = couponMapper.selectList(couponQueryWrapper);
        List<Map<String,Object>> mapList = new ArrayList<>();
        coupons.forEach(coupon -> {
            Map<String,Object> couponMap = new HashMap<>();
            couponMap.put("id",coupon.getId());
            couponMap.put("userId",coupon.getUserId());
            couponMap.put("agoUserId",coupon.getAgoUserId());
            couponMap.put("name",coupon.getName());
            couponMap.put("full",coupon.getFull());
            couponMap.put("subtract",coupon.getSubtract());
            couponMap.put("status",coupon.getStatus());
            couponMap.put("time",coupon.getTime());
            //share 该红包为0时可分享，为1时不可分享
            try {
                Date time = ft.parse(coupon.getTime());
                int compareTo = new Date().compareTo(time);
                if (compareTo==-1) {
                    couponMap.put("share",1);
                    mapList.add(couponMap);
                    return;
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
            if (coupon.getStatus()!=0) {
                couponMap.put("share",1);
                mapList.add(couponMap);
                return;
            }
            couponMap.put("share",0);
            mapList.add(couponMap);
            return;
        });
        return mapList;
    }

    /**
     * 通过用户id查询账户余额
     * @param userId
     * @return
     */
    public String userPrice(Integer userId){
        UserInfo userInfo = userInfoMapper.selectById(userId);
        return userInfo.getPrice().toString();
    }

    /**
     * 分页查询用户账户明细
     * @param pageNum
     * @return
     */
    public PageInfo userPriceList(Integer pageNum,Integer userId){
        QueryWrapper<UserPriceInfo> userPriceInfoQueryWrapper = new QueryWrapper<>();
        userPriceInfoQueryWrapper.eq("user_id",userId).orderByDesc("id");
        Page page = new Page(pageNum,10);
        IPage<UserPriceInfo> userPriceInfoList = userPriceInfoMapper.selectPage(page,userPriceInfoQueryWrapper);
        PageInfo pageInfo = new PageInfo(userPriceInfoList);
        return pageInfo;
    }

    /**
     * 查询所有用户会员类别
     * @return
     */
    public List<UserMember> userMemberList(){
        List<UserMember> userMembers = userMemberMapper.selectList(null);
        return userMembers;
    }

    /**
     * 修改单条用户会员类别
     * @param map
     * @return
     */
    public Integer updateUserMember(Map<String,Object> map){
        UserMember userMember = new UserMember();
        userMember.setId(Long.valueOf(map.get("id").toString()));
        userMember.setName((String)map.get("name"));
        if (map.get("moneyMin")==null||"null".equals(map.get("moneyMin"))){ userMember.setMoneyMin(null); }
        else { userMember.setMoneyMin(new BigDecimal(String.valueOf(map.get("moneyMin")))); }
        if (map.get("moneyMax")==null||"null".equals(map.get("moneyMax"))){userMember.setMoneyMax(null);}
        else {userMember.setMoneyMax(new BigDecimal(String.valueOf(map.get("moneyMax"))));}
        return userMemberMapper.updateById(userMember);
    }

    /**
     * 修改多条用户会员类别
     * @param userMemberListJson
     * @return
     */
    @Transactional(value="txManager1")
    public Integer updateUserMembers(String userMemberListJson){
        List<String> userMemberJsonList = JSON.parseArray(userMemberListJson,String.class);
        int l = 0;
        for (int i=0;i<userMemberJsonList.size();i++){
            Map<String,Object> map = JSON.parseObject(userMemberJsonList.get(i),Map.class);
            l = this.updateUserMember(map);
            if (l==0) break;
        }
        return l;
    }

    /**
     * 通过会员类别id查询折扣
     * @return
     */
    public List<Map<String,String>> selectUserMemberModel(Map<String,String> map){
        return userMemberModelMapper.selectUserMember(map);
    }

    /**
     * 新增会员折扣
     * @param map
     * @return
     *  201: 该级别会员已有该型号折扣
     */
    public Integer addUserMemberModel(Map<String,String> map){
        QueryWrapper<UserMemberModel> userMemberModelQueryWrapper = new QueryWrapper<>();
        userMemberModelQueryWrapper.eq("member_id",map.get("memberId"))
                .eq("model_id",map.get("modelId"));
        List<UserMemberModel> userMemberModelList = userMemberModelMapper.selectList(userMemberModelQueryWrapper);
        if (userMemberModelList.size()>0) return 201; //该级别会员已有该型号折扣
        UserMemberModel userMemberModel = new UserMemberModel();
        userMemberModel.setMemberId(Long.valueOf(map.get("memberId")));
        userMemberModel.setModelId(Long.valueOf(map.get("modelId")));
        userMemberModel.setDiscount(new BigDecimal(map.get("discount")));
        return userMemberModelMapper.insert(userMemberModel);
    }

    /**
     * 通过id删除会员折扣
     * @param userMemberModelId
     * @return
     */
    public Integer delUserMemberModel(String userMemberModelId){
        return userMemberModelMapper.deleteById(Integer.parseInt(userMemberModelId));
    }

    /**
     * 通过用户id查询会员折扣
     * @param userId
     * @return
     *  null: 当前用户不存在
     */
    public List<UserMemberModel> userMemberModelList(String userId){
        UserInfo userInfo = userInfoMapper.selectById(Integer.parseInt(userId));
        if (userInfo==null) return null;
        QueryWrapper<UserMemberModel> userMemberModelQueryWrapper = new QueryWrapper<>();
        userMemberModelQueryWrapper.eq("member_id",userInfo.getMemberId());
        return userMemberModelMapper.selectList(userMemberModelQueryWrapper);
    }

    /**
     * 升级用户为正式用户
     * @param map
     * @return
     *  201: 该编号已存在
     *  202: 该用户不存在
     *  203: 该用户已是正式用户
     */
    public Integer updateUserSerialNumber(Map<String,String> map){
        UserInfo userInfo = userInfoMapper.selectById(Integer.parseInt(map.get("userId")));
        if (userInfo.getStatus()==1) return 203; //该用户已是正式用户
        QueryWrapper<UserInfo> userInfoQueryWrapper = new QueryWrapper<>();
        userInfoQueryWrapper.eq("serial_number",map.get("serialNumber"));
        List<UserInfo> userInfos = userInfoMapper.selectList(userInfoQueryWrapper);
        if (userInfos.size()>0) return 201; //该编号已存在
        if (userInfo==null) return 202; //该用户不存在
        userInfo.setStatus(1);
        userInfo.setSerialNumber(map.get("serialNumber"));
        return userInfoMapper.updateById(userInfo);
    }

    /**
     * 新增开通申请
     * @param map
     * @return
     *  201: 该用户不存在
     *  202: 该用户已是正式用户，不可多次申请
     *  203: 该会员不存在
     *  204: 每个用户同时只可存在一条申请
     *  205: 已是当前申请的会员类型
     *  206: 会员预存金额超过会员预存金额转待提现金额最大金额
     */
    public Integer addUserMemberApply(Map<String,String> map){
        Long userId = Long.valueOf(map.get("userId"));
        Long memberId = Long.valueOf(map.get("memberId"));
        UserInfo userInfo = userInfoMapper.selectById(userId);
        if (userInfo == null) return 201;//该用户不存在
        if (userInfo.getStatus()==1&&memberId.longValue()==0) return 202;//该用户已是正式用户，不可多次申请
        UserMember userMember = userMemberMapper.selectById(memberId);
        if (userMember == null) return 203;//该会员不存在
        QueryWrapper<UserMemberApply> userMemberApplyQueryWrapper = new QueryWrapper<>();
        userMemberApplyQueryWrapper.eq("user_id",userId).eq("status","0");
        List<UserMemberApply> userMemberApplies = userMemberApplyMapper.selectList(userMemberApplyQueryWrapper);
        if (userMemberApplies.size()>0) return 204;//每个用户同时只可存在一条申请
        UserMemberApply userMemberApply = new UserMemberApply();
        if (userInfo.getMemberId()!=0){
            //判断要申请的会员类型是否和原会员类型相同
            if (userInfo.getMemberId().longValue()==memberId.longValue()){
                return 205;//已是当前申请的会员类型
            }else {
                userMemberApply.setStatus(0);
                //0:把原会员预存金额转为待提现金额 1:把原会员预存金额转为新会员预存金额
                if ("0".equals(map.get("type"))){
                    userMemberApply.setPrice(new BigDecimal(0));
                    QueryWrapper<Config> configQueryWrapper = new QueryWrapper<>();
                    configQueryWrapper.eq("code","withdrawPrice");
                    BigDecimal withdrawPrice = new BigDecimal(configMapper.selectList(configQueryWrapper).get(0).getValue());
                    if (withdrawPrice.compareTo(userInfo.getMemberPrice())==-1){
                        return 206;//会员预存金额超过会员预存金额转待提现金额最大金额
                    }
                }else if ("1".equals(map.get("type"))){
                    userMemberApply.setPrice(userInfo.getMemberPrice());
                    userInfo.setMemberPrice(new BigDecimal(0));
                    this.userInfoMapper.updateById(userInfo);
                }
            }
        }
        userMemberApply.setUserId(userId);
        userMemberApply.setMemberId(memberId);
        userMemberApply.setName(map.get("name"));
        userMemberApply.setPhone(map.get("phone"));
        return userMemberApplyMapper.insert(userMemberApply);
    }

    /**
     * 分页查询申请列表
     * @param map
     * @return
     */
    public PageInfo selectUserMemberApply(Map<String,String> map){
        QueryWrapper<UserMemberApply> userMemberApplyQueryWrapper = new QueryWrapper<>();
        userMemberApplyQueryWrapper.eq("status",map.get("status"));
        if (StringUtil.isEmpty(map.get("name"))) userMemberApplyQueryWrapper.like("name",map.get("name"));
        if (StringUtil.isEmpty(map.get("phone"))) userMemberApplyQueryWrapper.like("phone",map.get("phone"));
        Page page = new Page(Integer.parseInt(map.get("pageNum")),10);
        IPage<UserMemberApply> userMemberApplies = userMemberApplyMapper.selectPage(page,userMemberApplyQueryWrapper);
        PageInfo pageInfo = new PageInfo(userMemberApplies);
        List<Map<String,String>> mapList = new ArrayList<>();
        for (UserMemberApply userMemberApply : (List<UserMemberApply>)pageInfo.getList()){
            Map<String,String> userMemberApplyMap = JSON.parseObject(JSON.toJSONString(userMemberApply),Map.class);
            UserInfo userInfo = userInfoMapper.selectById(userMemberApply.getUserId());
            userMemberApplyMap.put("wxName",userInfo.getWxName());
            if (userMemberApply.getMemberId()==0) {userMemberApplyMap.put("memberName","正式用户");}
            else {
                UserMember userMember = userMemberMapper.selectById(userMemberApply.getMemberId());
                userMemberApplyMap.put("memberName",userMember.getName());
            }
            mapList.add(userMemberApplyMap);
        }
        pageInfo.setList(mapList);
        return pageInfo;
    }

    /**
     * 通过id删除申请
     * @param id
     * @return
     */
    public Integer delUserMemberApply(Integer id){
        return userMemberApplyMapper.deleteById(id);
    }

    /**
     * 升级为正式用户
     * @param map
     * @return
     *  201: 该编号已存在
     *  202: 该用户不存在
     *  203: 该用户已是正式用户
     *  204: 该申请不存在
     */
    @Transactional(value="txManager1")
    public Integer updateUserStatus(Map<String,String> map){
        UserMemberApply userMemberApply = userMemberApplyMapper.selectById(Integer.parseInt(map.get("userMemberApplyId")));
        if (userMemberApply==null) return 204;//该申请不存在
        Integer i = this.updateUserSerialNumber(map);
        if (!(i>0&&i<201)) return i;
        userMemberApply.setStatus(1);
        return userMemberApplyMapper.updateById(userMemberApply);
    }

    /**
     * 升级为会员
     * @param map
     * @return
     *  201: 该申请不存在
     *  202: 该用户不存在
     *  203: 该会员类型不存在
     *  204: 该编号已存在
     *  205: 会员预存金额不足该会员类型最小限额
     */
    @Transactional(value="txManager1")
    public Integer updateUserMember1(Map<String,String> map){
        UserMemberApply userMemberApply = userMemberApplyMapper.selectById(Integer.parseInt(map.get("userMemberApplyId")));
        if (userMemberApply==null) return 201;//该申请不存在
        UserInfo userInfo = userInfoMapper.selectById(Integer.parseInt(map.get("userId")));
        if (userInfo==null) return 202;//该用户不存在
        UserMember userMember = userMemberMapper.selectById(Integer.parseInt(map.get("memberId")));
        if (userMember==null) return 203;//该会员类型不存在
        if ("0".equals(map.get("userStatus"))) {
            QueryWrapper<UserInfo> userInfoQueryWrapper = new QueryWrapper<>();
            userInfoQueryWrapper.eq("serial_number",map.get("serialNumber"));
            List<UserInfo> userInfos = userInfoMapper.selectList(userInfoQueryWrapper);
            if (userInfos.size()>0) return 204; //该编号已存在
            userInfo.setStatus(1);
            userInfo.setSerialNumber(map.get("serialNumber"));
        }
        userInfo.setMemberId(userMember.getId());
        BigDecimal memberPrice = new BigDecimal(map.get("memberPrice"));
        if (userMember.getMoneyMin().compareTo(memberPrice.add(userMemberApply.getPrice()))==1) return 205; //会员预存金额不足该会员类型最小限额
        userInfo.setMemberPrice(memberPrice.add(userMemberApply.getPrice()));
        Integer i = userInfoMapper.updateById(userInfo);
        if (i<=0) return i;
        userMemberApply.setStatus(1);
        return userMemberApplyMapper.updateById(userMemberApply);
    }

    /**
     * 通过id查询会员类别
     * @param memberId
     * @return
     */
    public UserMember selectUserMemberById(String memberId){
        return userMemberMapper.selectById(Integer.parseInt(memberId));
    }

    /**
     * 通过用户id查询
     * @param userId
     * @return
     */
    public Map<String,String> selectUserInfoMap(String userId){
        UserInfo userInfo = userInfoMapper.selectById(Integer.parseInt(userId));
        Map<String,String> userInfoMap = JSON.parseObject(JSON.toJSONString(userInfo),Map.class);
        if (userInfo.getStatus()==0) userInfoMap.put("userStatus","体验用户");
        else if (userInfo.getStatus()==1) userInfoMap.put("userStatus","正式用户");
        if (userInfo.getMemberId()==0) userInfoMap.put("memberName","非会员");
        else {
            UserMember userMember = userMemberMapper.selectById(userInfo.getMemberId());
            userInfoMap.put("memberName",userMember.getName());
        }
        return userInfoMap;
    }

    /**
     * 通过用户id查询用户
     * @param userId
     * @return
     */
    public UserInfo userInfoById(String userId){
        return userInfoMapper.selectById(Integer.parseInt(userId));
    }

    /**
     * 通过用户id修改用户
     * @param userInfoMap
     * @return
     *  201: 该用户不存在
     *  202: 该会员类型不存在
     *  203: 该编号已存在
     */
    public Integer updateUserInfo(Map<String,String> userInfoMap){
        UserInfo userInfo = userInfoMapper.selectById(Integer.parseInt(userInfoMap.get("userId")));
        if (userInfo==null) return 201;//该用户不存在
        UserMember userMember = userMemberMapper.selectById(Integer.parseInt(userInfoMap.get("memberId")));
        if (userMember==null) return 202;//该会员类型不存在
        userInfo.setMemberId(userMember.getId());
        userInfo.setPrice(new BigDecimal(userInfoMap.get("price")));
        userInfo.setGoldCoin(Integer.parseInt(userInfoMap.get("goldCoin")));
        QueryWrapper<UserInfo> userInfoQueryWrapper = new QueryWrapper<>();
        userInfoQueryWrapper.eq("serial_number",userInfoMap.get("serialNumber"));
        List<UserInfo> userInfos = userInfoMapper.selectList(userInfoQueryWrapper);
        if ("".equals(userInfoMap.get("serialNumber"))||userInfoMap.get("serialNumber")==null){
            userInfo.setSerialNumber(null);
        }else if (userInfo.getSerialNumber()!=null
                &&userInfo.getSerialNumber().equals(userInfoMap.get("serialNumber"))
                &&userInfos.size()>1) return 203;//该编号已存在
        else if (userInfo.getSerialNumber()!=null
                &&!userInfo.getSerialNumber().equals(userInfoMap.get("serialNumber"))
                &&userInfos.size()>0) return 203;//该编号已存在
        else userInfo.setSerialNumber(userInfoMap.get("serialNumber"));
        userInfo.setStatus(Integer.parseInt(userInfoMap.get("status")));
        userInfo.setMemberPrice(new BigDecimal(userInfoMap.get("memberPrice")));
        return userInfoMapper.updateById(userInfo);
    }
}
