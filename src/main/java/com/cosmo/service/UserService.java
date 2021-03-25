package com.cosmo.service;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cosmo.dao.*;
import com.cosmo.entity.*;
import com.cosmo.util.*;
import org.apache.commons.collections.map.HashedMap;
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
import java.util.stream.Collectors;

@Service
public class UserService {

    @Resource
    private CouponService couponService;


    @Resource
    private UserInfoMapper userInfoMapper;
    @Resource
    private UserRessMapper userRessMapper;
    @Resource
    private UserInvoiceMapper userInvoiceMapper;
    @Resource
    private UserPriceInfoMapper userPriceInfoMapper;
    @Resource
    private UserMemberPriceInfoMapper userMemberPriceInfoMapper;
    @Resource
    private UserMemberMapper userMemberMapper;
    @Resource
    private UserLockMapper userLockMapper;
    @Resource
    private UserMemberApplyMapper userMemberApplyMapper;
    @Resource
    private UserMemberModelMapper userMemberModelMapper;
    @Resource
    private UserWithdrawPriceApplyMapper userWithdrawPriceApplyMapper;
    @Resource
    private UserPurchaserMapper userPurchaserMapper;
    @Resource
    private HatCityMapper hatCityMapper;
    @Resource
    private FreightMapper freightMapper;
    @Resource
    private CouponMapper couponMapper;
    @Resource
    private CouponHomeMapper couponHomeMapper;
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
        String name = map.get("name").toString();
        if (!StringUtil.isEmpty(name)) userInfoQueryWrapper.like("name",name);
        String phone = map.get("phone").toString();
        if (!StringUtil.isEmpty(phone)) userInfoQueryWrapper.like("phone",phone);
        String identity = map.get("identity").toString();
        if (!StringUtil.isEmpty(identity)) userInfoQueryWrapper.like("identity",identity);
        String serialNumber = map.get("serialNumber").toString();
        if (!StringUtil.isEmpty(serialNumber)) userInfoQueryWrapper.likeRight("serial_number",serialNumber+"-");
        userInfoQueryWrapper.orderByDesc("last_login_time");
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
    public Integer delUserInfo(String id){
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
        QueryWrapper<Freight> freightQueryWrapper = new QueryWrapper<>();
        freightQueryWrapper.eq("hatID",cityId);
        Integer integer = freightMapper.selectCount(freightQueryWrapper);
        if (integer>0) return true;
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
    public int countRess(String userId,int type){
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
    public boolean delRess(String id){
        Integer i = userRessMapper.deleteById(id);
        if (i>0) return true;
        return false;
    }

    /**
     * 根据用户id查询地址列表并升序type，及自提地址在最前
     * @param userId
     * @return
     */
    public List<UserRess> userResses(String userId){
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
    public List<UserRess> userRessList(String userId,Integer type){
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
    public List<UserInvoice> userInvoices(String userId){
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
    public int countInvoice(String userId){
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
    public Integer delInvoice(String id){
        return userInvoiceMapper.deleteById(id);
    }

    /**
     * 获取首页红包 数据
     * @return
     */
    public List<CouponHome> getHomeCoupon(){
        List<CouponHome> couponHomeList = couponHomeMapper.selectList(null);
        /*Config config = configMapper.selectOne(new QueryWrapper<Config>().eq("type", 5).eq("code", "homeCoupon"));
        Map map = JSON.parseObject(config.getValue(), Map.class);*/
        return couponHomeList;
    }

    /**
     * 更新首页红包 数据
     * @param value
     * @return
     */
    /*public Integer setHomeCoupon(String value){
        Config config1 = configMapper.selectOne(new QueryWrapper<Config>().eq("type", 5).eq("code", "homeCoupon"));
        if (config1==null){//不存在及新增
            Config config = new Config();
            config.setCode("homeCoupon");
            config.setType(5);
            config.setValue(value);
            config.setName("首页红包");
            return configMapper.insert(config);
        }else {//存在及更新
            Config config = new Config();
            config.setValue(value);
            return configMapper.update(config,new QueryWrapper<Config>().eq("type", 5).eq("code", "homeCoupon"));
        }
    }*/

    /**
     * 根据首页红包id删除首页红包
     * @return
     */
    public Integer addHomeCoupon(){
        return configMapper.delete(new QueryWrapper<Config>().eq("type", 5).eq("code", "homeCoupon"));
    }

    /**
     * 新增首页红包
     * @param map
     * @return
     */
    public Integer addHomeCoupon(Map<String, String> map){
        CouponHome couponHome = new CouponHome();
        couponHome.setName(map.get("name"));
        couponHome.setFull(map.get("full"));
        couponHome.setSubtract(map.get("subtract"));
        couponHome.setTime(map.get("time"));
        couponHome.setType(map.get("type"));
        return couponHomeMapper.insert(couponHome);
    }

    /**
     * 根据首页红包id删除首页红包
     * @return
     */
    public Integer delHomeCoupon(String couponHomeId){
//        return configMapper.delete(new QueryWrapper<Config>().eq("type", 5).eq("code", "homeCoupon"));
        return couponHomeMapper.deleteById(couponHomeId);
    }

    /**
     * 首页红包领取
     * @param map
     * @return
     */
    @Transactional(value="txManager1")
    public Integer neckHomeCoupon(Map<String, String> map){
        int[] i = {0};
        List<CouponHome> couponHomeList = this.getHomeCoupon();
        couponHomeList.forEach(couponHome -> {
            Map<String, String> map1 = new HashMap<>();
            map1.put("userId",map.get("userId"));
            map1.put("name",couponHome.getName());
            map1.put("full",couponHome.getFull());
            map1.put("subtract",couponHome.getSubtract());
            map1.put("status","0");
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date now = new Date();
            long time=Integer.parseInt(couponHome.getTime())*60*1000;//homeCoupon.get("time")分钟
            Date afterDate=new Date(now.getTime()+time);//30分钟后的时间
//        Date beforeDate=new Date(now.getTime()-time);//30分钟前的时间
//        System.out.println(sdf.format(afterDate));
//        System.out.println(sdf.format(beforeDate));
            map1.put("time",sdf.format(afterDate));
            map1.put("type",couponHome.getType());
            map1.put("number","1");
            this.addCoupon(map1);
            i[0]++;
        });
        return i[0];
    }

    /**
     * 分页查询 拥有我分享的红包的用户
     * @param map
     * @return
     */
    public PageInfo selectSonUserInfo(Map<String, String> map){
        QueryWrapper<Coupon> couponQueryWrapper = new QueryWrapper<>();
        couponQueryWrapper.eq("type",1).ne("user_id",map.get("userId"));
        // [初始者用户id,           前拥有者用户id ]
        // ["1366193605630300162",""           ]
        couponQueryWrapper.like("ago_user_id","[\""+map.get("userId")+"\",");
        List<Coupon> couponList = couponMapper.selectList(couponQueryWrapper);
        List<String> collect = couponList.stream().map(coupon -> {
            return coupon.getUserId();
        }).collect(Collectors.toList());
        // 创建一个新的list集合，用于存储去重后的元素
        List<String> userIds = new ArrayList();
        // 遍历list集合
        for (int i = 0; i < collect.size(); i++) {
            // 判断userIds集合中是否包含collect中的元素
            if (!userIds.contains(collect.get(i))) {
                // 将未包含的元素添加进userIds集合中
                userIds.add(collect.get(i));
            }
        }
        QueryWrapper<UserInfo> userInfoQueryWrapper = new QueryWrapper<>();
        userInfoQueryWrapper.select("id","wx_name");
        if (userIds.size()>0){
            userInfoQueryWrapper.in("id",userIds);
        }else {
            userInfoQueryWrapper.eq("open_id","");
        }
        Page page = new Page(Integer.parseInt(map.get("pageNum")),10);
        IPage<UserInfo> userInfoIPage = userInfoMapper.selectPage(page, userInfoQueryWrapper);
        PageInfo pageInfo = new PageInfo(userInfoIPage);
        return pageInfo;
    }

    /**
     * 分页查询 分享的红包
     * status ： 0未使用，1使用后未付款，2使用后已付款，3已过期
     * @param map
     * @return
     */
    public PageInfo selectSonCoupon(Map<String, String> map){
        QueryWrapper<Coupon> couponQueryWrapper = new QueryWrapper<>();
        // 返现卷 且user_id 不等于分享者id
        couponQueryWrapper.eq("type",1).ne("user_id",map.get("shareUserId"));
        //user_id 等于拥有者id
        couponQueryWrapper.eq("user_id",map.get("ownUserId"));
        // [初始者用户id,           前拥有者用户id ]
        // ["1366193605630300162",""           ]
        couponQueryWrapper.like("ago_user_id","[\""+map.get("shareUserId")+"\",");
        if ("0".equals(map.get("status"))){
            couponQueryWrapper.eq("status",0);
        }else if ("1".equals(map.get("status"))){
            couponQueryWrapper.eq("status",1).eq("employ_status",0);
        }else if ("2".equals(map.get("status"))){
            couponQueryWrapper.eq("status",1).eq("employ_status",1);
        }else if ("3".equals(map.get("status"))){
            couponQueryWrapper.eq("status",2);
        }else if ("4".equals(map.get("status"))){
            couponQueryWrapper.eq("status",1).eq("employ_status",2);
        }
        Page page = new Page(Integer.parseInt(map.get("pageNum")),10);
        IPage<Coupon> coupons = couponMapper.selectPage(page,couponQueryWrapper);
        PageInfo pageInfo = new PageInfo(coupons);
        /*List<Coupon> couponList = pageInfo.getList();
        List<Coupon> collect = couponList.stream().map(coupon -> {
            coupon.setUserName(userInfoMapper.selectById(coupon.getUserId()).getWxName());
            return coupon;
        }).collect(Collectors.toList());
        pageInfo.setList(collect);*/
        return pageInfo;
    }

    /**
     * 查询拥有 分享的红包 各状态总数量和总返利
     * @param map
     * @return
     */
    public Map<String,Object> selectSonCouponInfo(Map<String, String> map){
        // 返现的总金额
        QueryWrapper<Coupon> couponQueryWrapper = new QueryWrapper<>();
        couponQueryWrapper.eq("type",1).ne("user_id",map.get("userId"));
        // [初始者用户id,           前拥有者用户id ]
        // ["1366193605630300162",""           ]
        couponQueryWrapper.like("ago_user_id","[\""+map.get("userId")+"\",");
        couponQueryWrapper.eq("status",1).eq("employ_status",2);
        couponQueryWrapper.select("sum(subtract) as total");
        Map<String, Object> mapTotal = couponService.getMap(couponQueryWrapper);


        // 分享红包 未使用
        QueryWrapper<Coupon> couponQueryWrapper1 = new QueryWrapper<>();
        couponQueryWrapper1.eq("type",1).ne("user_id",map.get("userId"));
        // [初始者用户id,           前拥有者用户id ]
        // ["1366193605630300162",""           ]
        couponQueryWrapper1.like("ago_user_id","[\""+map.get("userId")+"\",");
        couponQueryWrapper1.eq("status",0);
        couponQueryWrapper1.select("count(id) as nummber");
        Map<String, Object> map1 = couponService.getMap(couponQueryWrapper1);

        // 分享红包 使用未付款
        QueryWrapper<Coupon> couponQueryWrapper2 = new QueryWrapper<>();
        couponQueryWrapper2.eq("type",1).ne("user_id",map.get("userId"));
        // [初始者用户id,           前拥有者用户id ]
        // ["1366193605630300162",""           ]
        couponQueryWrapper2.like("ago_user_id","[\""+map.get("userId")+"\",");
        couponQueryWrapper2.eq("status",1).eq("employ_status",0);
        couponQueryWrapper2.select("count(id) as nummber");
        Map<String, Object> map2 = couponService.getMap(couponQueryWrapper2);

        // 分享红包 使用已付款
        QueryWrapper<Coupon> couponQueryWrapper3 = new QueryWrapper<>();
        couponQueryWrapper3.eq("type",1).ne("user_id",map.get("userId"));
        // [初始者用户id,           前拥有者用户id ]
        // ["1366193605630300162",""           ]
        couponQueryWrapper3.like("ago_user_id","[\""+map.get("userId")+"\",");
        couponQueryWrapper3.eq("status",1).eq("employ_status",1);
        couponQueryWrapper3.select("count(subtract) as nummber");
        Map<String, Object> map3 = couponService.getMap(couponQueryWrapper3);

        // 分享红包 已过期
        QueryWrapper<Coupon> couponQueryWrapper4 = new QueryWrapper<>();
        couponQueryWrapper4.eq("type",1).ne("user_id",map.get("userId"));
        // [初始者用户id,           前拥有者用户id ]
        // ["1366193605630300162",""           ]
        couponQueryWrapper4.like("ago_user_id","[\""+map.get("userId")+"\",");
        couponQueryWrapper4.eq("status",2);
        couponQueryWrapper4.select("count(id) as nummber");
        Map<String, Object> map4 = couponService.getMap(couponQueryWrapper4);

        // 分享红包 已返现
        QueryWrapper<Coupon> couponQueryWrapper5 = new QueryWrapper<>();
        couponQueryWrapper5.eq("type",1).ne("user_id",map.get("userId"));
        // [初始者用户id,           前拥有者用户id ]
        // ["1366193605630300162",""           ]
        couponQueryWrapper5.like("ago_user_id","[\""+map.get("userId")+"\",");
        couponQueryWrapper5.eq("status",1).eq("employ_status",2);
        couponQueryWrapper5.select("count(id) as nummber");
        Map<String, Object> map5 = couponService.getMap(couponQueryWrapper5);

        if (mapTotal==null){
            mapTotal = new HashMap<>();
            mapTotal.put("total",0);
        }
        if (map1!=null) mapTotal.put("number1",map1.get("nummber"));
        else mapTotal.put("number1",0);
        if (map2!=null) mapTotal.put("number2",map2.get("nummber"));
        else mapTotal.put("number2",0);
        if (map3!=null) mapTotal.put("number3",map3.get("nummber"));
        else mapTotal.put("number3",0);
        if (map4!=null) mapTotal.put("number4",map4.get("nummber"));
        else mapTotal.put("number4",0);
        if (map5!=null) mapTotal.put("number5",map5.get("nummber"));
        else mapTotal.put("number5",0);
        return mapTotal;
    }

    /**
     * 查询指定用户拥有 分享的红包 各状态总数量和总返利
     * @param map
     * @return
     */
    public Map<String,Object> selectSonUserCouponInfo(Map<String, String> map){
        // 返现的总金额
        QueryWrapper<Coupon> couponQueryWrapper = new QueryWrapper<>();
        couponQueryWrapper.eq("type",1).ne("user_id",map.get("shareUserId")).eq("user_id",map.get("ownUserId"));
        // [初始者用户id,           前拥有者用户id ]
        // ["1366193605630300162",""           ]
        couponQueryWrapper.like("ago_user_id","[\""+map.get("shareUserId")+"\",");
        couponQueryWrapper.eq("status",1).eq("employ_status",2);
        couponQueryWrapper.select("sum(subtract) as total");
        Map<String, Object> mapTotal = couponService.getMap(couponQueryWrapper);


        // 分享红包 未使用
        QueryWrapper<Coupon> couponQueryWrapper1 = new QueryWrapper<>();
        couponQueryWrapper1.eq("type",1).ne("user_id",map.get("shareUserId")).eq("user_id",map.get("ownUserId"));
        // [初始者用户id,           前拥有者用户id ]
        // ["1366193605630300162",""           ]
        couponQueryWrapper1.like("ago_user_id","[\""+map.get("shareUserId")+"\",");
        couponQueryWrapper1.eq("status",0);
        couponQueryWrapper1.select("count(id) as nummber");
        Map<String, Object> map1 = couponService.getMap(couponQueryWrapper1);

        // 分享红包 使用未付款
        QueryWrapper<Coupon> couponQueryWrapper2 = new QueryWrapper<>();
        couponQueryWrapper2.eq("type",1).ne("user_id",map.get("shareUserId")).eq("user_id",map.get("ownUserId"));
        // [初始者用户id,           前拥有者用户id ]
        // ["1366193605630300162",""           ]
        couponQueryWrapper2.like("ago_user_id","[\""+map.get("shareUserId")+"\",");
        couponQueryWrapper2.eq("status",1).eq("employ_status",0);
        couponQueryWrapper2.select("count(id) as nummber");
        Map<String, Object> map2 = couponService.getMap(couponQueryWrapper2);

        // 分享红包 使用已付款
        QueryWrapper<Coupon> couponQueryWrapper3 = new QueryWrapper<>();
        couponQueryWrapper3.eq("type",1).ne("user_id",map.get("shareUserId")).eq("user_id",map.get("ownUserId"));
        // [初始者用户id,           前拥有者用户id ]
        // ["1366193605630300162",""           ]
        couponQueryWrapper3.like("ago_user_id","[\""+map.get("shareUserId")+"\",");
        couponQueryWrapper3.eq("status",1).eq("employ_status",1);
        couponQueryWrapper3.select("count(subtract) as nummber");
        Map<String, Object> map3 = couponService.getMap(couponQueryWrapper3);

        // 分享红包 已过期
        QueryWrapper<Coupon> couponQueryWrapper4 = new QueryWrapper<>();
        couponQueryWrapper4.eq("type",1).ne("user_id",map.get("shareUserId")).eq("user_id",map.get("ownUserId"));
        // [初始者用户id,           前拥有者用户id ]
        // ["1366193605630300162",""           ]
        couponQueryWrapper4.like("ago_user_id","[\""+map.get("shareUserId")+"\",");
        couponQueryWrapper4.eq("status",2);
        couponQueryWrapper4.select("count(id) as nummber");
        Map<String, Object> map4 = couponService.getMap(couponQueryWrapper4);

        // 分享红包 已返现
        QueryWrapper<Coupon> couponQueryWrapper5 = new QueryWrapper<>();
        couponQueryWrapper5.eq("type",1).ne("user_id",map.get("shareUserId")).eq("user_id",map.get("ownUserId"));
        // [初始者用户id,           前拥有者用户id ]
        // ["1366193605630300162",""           ]
        couponQueryWrapper5.like("ago_user_id","[\""+map.get("shareUserId")+"\",");
        couponQueryWrapper5.eq("status",1).eq("employ_status",2);
        couponQueryWrapper5.select("count(id) as nummber");
        Map<String, Object> map5 = couponService.getMap(couponQueryWrapper5);

        if (mapTotal==null){
            mapTotal = new HashMap<>();
            mapTotal.put("total",0);
        }
        if (map1!=null) mapTotal.put("number1",map1.get("nummber"));
        else mapTotal.put("number1",0);
        if (map2!=null) mapTotal.put("number2",map2.get("nummber"));
        else mapTotal.put("number2",0);
        if (map3!=null) mapTotal.put("number3",map3.get("nummber"));
        else mapTotal.put("number3",0);
        if (map4!=null) mapTotal.put("number4",map4.get("nummber"));
        else mapTotal.put("number4",0);
        if (map5!=null) mapTotal.put("number5",map5.get("nummber"));
        else mapTotal.put("number5",0);
        return mapTotal;
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
                List<String> userIdList = JSON.parseArray(coupon.getAgoUserId(),String.class);
                QueryWrapper<UserInfo> userInfoQueryWrapper = new QueryWrapper<>();
                userInfoQueryWrapper.in("id",userIdList);
                coupon.setAgoUser(userInfoMapper.selectList(userInfoQueryWrapper));
            }
        }
        return pageInfo;
    }

    /**
     * 分页查询该用户所有返现红包
     * @param map
     * @return
     */
    public PageInfo selectUserCoupon(Map<String, String> map){
        QueryWrapper<Coupon> couponQueryWrapper = new QueryWrapper<>();
        couponQueryWrapper.eq("user_id",map.get("userId")).eq("status",map.get("status"));
        if (!StringUtil.isEmpty(map.get("type"))){
            couponQueryWrapper.eq("type",map.get("type"));
        }
        Page page = new Page(Integer.parseInt(map.get("pageNum")),10);
        IPage<Coupon> coupons = couponMapper.selectPage(page,couponQueryWrapper);
        PageInfo pageInfo = new PageInfo(coupons);
        for (int i = 0;i<pageInfo.getList().size();i++){
            Coupon coupon = (Coupon)pageInfo.getList().get(i);
            if (!"[]".equals(coupon.getAgoUserId())){
                List<String> userIdList = JSON.parseArray(coupon.getAgoUserId(),String.class);
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
            coupon.setUserId(map.get("userId"));
            coupon.setName(map.get("name"));
            coupon.setFull(new BigDecimal(map.get("full")));
            coupon.setSubtract(new BigDecimal(map.get("subtract")));
            coupon.setStatus(0);
            coupon.setTime(map.get("time"));
            coupon.setType(Integer.parseInt(map.get("type")));
            if (coupon.getType()==0){
                //新增的是满减卷
                //[](存空数组json字符串)
                String[] userIds = {};
                coupon.setAgoUserId(JSON.toJSONString(userIds));
            }else {
                //新增的是返现卷
                //[初始者用户id,前拥有者用户id](存数组json字符串)
                String[] userIds = {map.get("userId"),""};
                coupon.setAgoUserId(JSON.toJSONString(userIds));
            }

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
     *         6该优惠卷不是返现卷，不可分享
     */
    public Integer getShare(Map<String,String> map){
        Coupon coupon = couponMapper.selectById(map.get("couponId"));
        if (!coupon.getUserId().equals(map.get("agoUserId"))) return 1;
        if (coupon.getType()!=1) return 6;
        try {
            Date time = ft.parse(coupon.getTime());
            int compareTo = new Date().compareTo(time);
            if (compareTo>0) return 2;
        } catch (ParseException e) {
            e.printStackTrace();
        }
        if (coupon.getStatus()!=0) return 3;
        if (map.get("agoUserId").equals(map.get("userId"))) return 5;
        List<String> agoUserId = JSON.parseArray(coupon.getAgoUserId(),String.class);

        //[初始者用户id,前拥有者用户id](存数组json字符串)
        String[] userIds = {agoUserId.get(0),map.get("agoUserId")};
        coupon.setAgoUserId(JSON.toJSONString(userIds));
        coupon.setUserId(map.get("userId"));
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
        if (!StringUtil.isEmpty(map.get("type"))){
            couponQueryWrapper.eq("type",map.get("type"));
        }
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
        });
        return mapList;
    }

    /**
     * 通过用户id查询账户余额
     * @param userId
     * @return
     */
    public String userPrice(String userId){
        UserInfo userInfo = userInfoMapper.selectById(userId);
        return userInfo.getPrice().toString();
    }

    /**
     * 通过用户id查询会员预存金额和待提现金额
     * @param userId
     * @return
     */
    public Map<String,String> userMemberPrice(String userId){
        UserInfo userInfo = userInfoMapper.selectById(userId);
        Map<String,String> map = new HashedMap();
        map.put("memberPrice",userInfo.getMemberPrice().toString());
        map.put("withdrawPrice",userInfo.getWithdrawPrice().toString());
        return map;
    }

    /**
     * 分页查询用户账户明细
     * @param pageNum
     * @return
     */
    public PageInfo userPriceList(Integer pageNum,String userId){
        QueryWrapper<UserPriceInfo> userPriceInfoQueryWrapper = new QueryWrapper<>();
        userPriceInfoQueryWrapper.eq("user_id",userId).orderByDesc("time");
        Page page = new Page(pageNum,10);
        IPage<UserPriceInfo> userPriceInfoList = userPriceInfoMapper.selectPage(page,userPriceInfoQueryWrapper);
        PageInfo pageInfo = new PageInfo(userPriceInfoList);
        return pageInfo;
    }

    /**
     * 分页查询用户会员预存金额和待提现金额明细
     * @param pageNum
     * @return
     */
    public PageInfo userMemberPriceList(Integer pageNum,String userId,String type){
        QueryWrapper<UserMemberPriceInfo> userMemberPriceInfoQueryWrapper = new QueryWrapper<>();
        userMemberPriceInfoQueryWrapper.eq("user_id",userId).eq("type",type).orderByDesc("time");
        Page page = new Page(pageNum,10);
        IPage<UserMemberPriceInfo> userMemberPriceInfoList = userMemberPriceInfoMapper.selectPage(page,userMemberPriceInfoQueryWrapper);
        PageInfo pageInfo = new PageInfo(userMemberPriceInfoList);
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
        userMember.setId(map.get("id").toString());
        userMember.setName((String)map.get("name"));
        userMember.setDiscounts((String)map.get("discounts"));
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
        userMemberModel.setMemberId(map.get("memberId"));
        userMemberModel.setModelId(map.get("modelId"));
        userMemberModel.setDiscount(new BigDecimal(map.get("discount")));
        return userMemberModelMapper.insert(userMemberModel);
    }

    /**
     * 通过id删除会员折扣
     * @param userMemberModelId
     * @return
     */
    public Integer delUserMemberModel(String userMemberModelId){
        return userMemberModelMapper.deleteById(userMemberModelId);
    }

    /**
     * 通过用户id查询会员折扣
     * @param userId
     * @return
     *  null: 当前用户不存在
     */
    public List<UserMemberModel> userMemberModelList(String userId){
        UserInfo userInfo = userInfoMapper.selectById(userId);
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
        UserInfo userInfo = userInfoMapper.selectById(map.get("userId"));
        if (userInfo.getStatus()==1) return 203; //该用户已是正式用户
        QueryWrapper<UserInfo> userInfoQueryWrapper = new QueryWrapper<>();
        userInfoQueryWrapper.eq("serial_number",map.get("serialNumber"));
        List<UserInfo> userInfos = userInfoMapper.selectList(userInfoQueryWrapper);
        if (userInfos.size()>0) return 201; //该编号已存在
        if (userInfo==null) return 202; //该用户不存在
        userInfo.setStatus(1);
        userInfo.setSerialNumber(map.get("serialNumber"));
        userInfo.setName(map.get("name"));
        userInfo.setPhone(map.get("phone"));
        userInfo.setIdentity(map.get("identity"));
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
    @Transactional(value="txManager1")
    public Integer addUserMemberApply(Map<String,String> map){
        String userId = map.get("userId");
        String memberId = map.get("memberId");
        UserInfo userInfo = userInfoMapper.selectById(userId);
        if (userInfo == null) return 201;//该用户不存在
        if (userInfo.getStatus()==1&&"0".equals(memberId)) return 202;//该用户已是正式用户，不可多次申请
        UserMember userMember = null;
        if (!"0".equals(memberId)){
            userMember = userMemberMapper.selectById(memberId);
            if (userMember == null) return 203;//该会员不存在
        }
        QueryWrapper<UserMemberApply> userMemberApplyQueryWrapper = new QueryWrapper<>();
        userMemberApplyQueryWrapper.eq("user_id",userId).eq("status","0");
        List<UserMemberApply> userMemberApplies = userMemberApplyMapper.selectList(userMemberApplyQueryWrapper);
        if (userMemberApplies.size()>0) return 204;//每个用户同时只可存在一条申请
        UserMemberApply userMemberApply = new UserMemberApply();
        if (!"0".equals(userInfo.getMemberId())){
            //判断要申请的会员类型是否和原会员类型相同
            if (userInfo.getMemberId().equals(memberId)){
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
        // 判断是否开通正式用户
        if ("0".equals(memberId)){
//            userInfo.setName(map.get("name"));
//            userInfo.setPhone(map.get("phone"));
//            userInfo.setIdentity(map.get("identity"));
//            userInfoMapper.updateById(userInfo);
            userMemberApply.setIdentity(map.get("identity"));
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
        userMemberApplyQueryWrapper.orderByDesc("id");
        Page page = new Page(Integer.parseInt(map.get("pageNum")),10);
        IPage<UserMemberApply> userMemberApplies = userMemberApplyMapper.selectPage(page,userMemberApplyQueryWrapper);
        PageInfo pageInfo = new PageInfo(userMemberApplies);
        List<Map<String,String>> mapList = new ArrayList<>();
        for (UserMemberApply userMemberApply : (List<UserMemberApply>)pageInfo.getList()){
            Map<String,String> userMemberApplyMap = JSON.parseObject(JSON.toJSONString(userMemberApply),Map.class);
            UserInfo userInfo = userInfoMapper.selectById(userMemberApply.getUserId());
            userMemberApplyMap.put("wxName",userInfo.getWxName());
            if ("0".equals(userMemberApply.getMemberId())) {userMemberApplyMap.put("memberName","正式用户");}
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
    public Integer delUserMemberApply(String id){
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
        UserMemberApply userMemberApply = userMemberApplyMapper.selectById(map.get("userMemberApplyId"));
        if (userMemberApply==null) return 204;//该申请不存在
        map.put("name",userMemberApply.getName());
        map.put("phone",userMemberApply.getPhone());
        map.put("identity",userMemberApply.getIdentity());
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
        UserMemberApply userMemberApply = userMemberApplyMapper.selectById(map.get("userMemberApplyId"));
        if (userMemberApply==null) return 201;//该申请不存在
        UserInfo userInfo = userInfoMapper.selectById(map.get("userId"));
        if (userInfo==null) return 202;//该用户不存在
        UserMember userMember = userMemberMapper.selectById(map.get("memberId"));
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
        UserMemberPriceInfo userMemberPriceInfo = new UserMemberPriceInfo();
        userMemberPriceInfo.setInfo("开通"+userMember.getName());
        userMemberPriceInfo.setPrice(memberPrice);
        userMemberPriceInfo.setTime(new Date());
        userMemberPriceInfo.setType(0);
        userMemberPriceInfo.setUserId(userInfo.getId());
        this.userMemberPriceInfoMapper.insert(userMemberPriceInfo);
        userMemberApply.setStatus(1);
        return userMemberApplyMapper.updateById(userMemberApply);
    }

    /**
     * 通过id查询会员类别
     * @param memberId
     * @return
     */
    public UserMember selectUserMemberById(String memberId){
        return userMemberMapper.selectById(memberId);
    }

    /**
     * 通过用户id查询
     * @param userId
     * @return
     */
    public Map<String,String> selectUserInfoMap(String userId){
        UserInfo userInfo = userInfoMapper.selectById(userId);
        Map<String,String> userInfoMap = JSON.parseObject(JSON.toJSONString(userInfo),Map.class);
        if (userInfo.getStatus()==0) userInfoMap.put("userStatus","体验用户");
        else if (userInfo.getStatus()==1) userInfoMap.put("userStatus","正式用户");
        if ("0".equals(userInfo.getMemberId())) userInfoMap.put("memberName","非会员");
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
        return userInfoMapper.selectById(userId);
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
        UserInfo userInfo = userInfoMapper.selectById(userInfoMap.get("userId"));
        if (userInfo==null) return 201;//该用户不存在
        if (!"0".equals(userInfoMap.get("memberId"))){
            UserMember userMember = userMemberMapper.selectById(userInfoMap.get("memberId"));
            if (userMember==null) return 202;//该会员类型不存在
            userInfo.setMemberId(userMember.getId());
        }else userInfo.setMemberId("0");
        userInfo.setName(userInfoMap.get("name"));
        userInfo.setPhone(userInfoMap.get("phone"));
        userInfo.setIdentity(userInfoMap.get("identity"));
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
        userInfo.setWithdrawPrice(new BigDecimal(userInfoMap.get("withdrawPrice")));
        return userInfoMapper.updateById(userInfo);
    }

    /**
     * 新增待提现金额提现申请
     * @param map
     * @return
     *  201: 该用户不存在
     *  202: 该用户待提现金额不足以本次申请扣除
     */
    public Integer addUserWithdrawPriceApply(Map<String,String> map){
        UserInfo userInfo = userInfoMapper.selectById(map.get("userId"));
        if (userInfo==null) return 201;//该用户不存在
        BigDecimal withdrawPrice = new BigDecimal(map.get("withdrawPrice"));
        if (userInfo.getWithdrawPrice().compareTo(withdrawPrice)==-1) return 202;//该用户待提现金额不足以本次申请扣除
        userInfo.setWithdrawPrice(userInfo.getWithdrawPrice().subtract(withdrawPrice));
        this.userInfoMapper.updateById(userInfo);
        UserMemberPriceInfo userMemberPriceInfo = new UserMemberPriceInfo();
        userMemberPriceInfo.setInfo("待提现金额提现申请");
        userMemberPriceInfo.setPrice(withdrawPrice);
        userMemberPriceInfo.setTime(new Date());
        userMemberPriceInfo.setType(2);
        userMemberPriceInfo.setUserId(userInfo.getId());
        this.userMemberPriceInfoMapper.insert(userMemberPriceInfo);
        UserWithdrawPriceApply userWithdrawPriceApply = new UserWithdrawPriceApply();
        userWithdrawPriceApply.setUserId(userInfo.getId());
        userWithdrawPriceApply.setName(map.get("name"));
        userWithdrawPriceApply.setPhone(map.get("phone"));
        userWithdrawPriceApply.setBankName(map.get("bankName"));
        userWithdrawPriceApply.setBankNumber(map.get("bankNumder"));
        userWithdrawPriceApply.setCardholder(map.get("cardholder"));
        userWithdrawPriceApply.setStatus(0);
        userWithdrawPriceApply.setTime(new Date());
        userWithdrawPriceApply.setWithdrawPrice(withdrawPrice);
        return userWithdrawPriceApplyMapper.insert(userWithdrawPriceApply);
    }

    /**
     * 分页查询待提现金额提现申请
     * @param map
     * @return
     */
    public PageInfo userWithdrawPriceApplyLitsPage(Map<String,String> map){
        QueryWrapper<UserWithdrawPriceApply> userWithdrawPriceApplyQueryWrapper = new QueryWrapper<>();
        userWithdrawPriceApplyQueryWrapper.eq("status",map.get("status"));
        if (!StringUtil.isEmpty(map.get("name"))) userWithdrawPriceApplyQueryWrapper.like("name",map.get("name"));
        if (!StringUtil.isEmpty(map.get("phone"))) userWithdrawPriceApplyQueryWrapper.like("phone",map.get("phone"));
        if (!StringUtil.isEmpty(map.get("bankName"))) userWithdrawPriceApplyQueryWrapper.like("bank_name",map.get("bankName"));
        if (!StringUtil.isEmpty(map.get("bankNumder"))) userWithdrawPriceApplyQueryWrapper.like("bank_number",map.get("bankNumder"));
        if (!StringUtil.isEmpty(map.get("cardholder"))) userWithdrawPriceApplyQueryWrapper.like("cardholder",map.get("cardholder"));;
        Page page = new Page(Integer.parseInt(map.get("pageNum")),10);
        IPage userWithdrawPriceApplyList = userWithdrawPriceApplyMapper.selectMapsPage(page,userWithdrawPriceApplyQueryWrapper);
        PageInfo pageInfo = new PageInfo(userWithdrawPriceApplyList);
        for (HashMap<String,Object> userWithdrawPriceApplyMap:(List<HashMap<String,Object>>)pageInfo.getList()){
            UserInfo userInfo = userInfoMapper.selectById(userWithdrawPriceApplyMap.get("user_id").toString());
            userWithdrawPriceApplyMap.put("user_name",userInfo.getWxName());
        }
        return pageInfo;
    }

    /**
     * 修改带提现金额申请状态
     * @param map
     * @return
     */
    public Integer userWithdrawPriceApplyStatus(Map<String,String> map){
        UserWithdrawPriceApply userWithdrawPriceApply = userWithdrawPriceApplyMapper.selectById(map.get("userWithdrawPriceApplyId"));
        userWithdrawPriceApply.setStatus(Integer.parseInt(map.get("status")));
        return userWithdrawPriceApplyMapper.updateById(userWithdrawPriceApply);
    }

    /**
     * 根据锁价id查询锁价数据
     * @param userLockId
     * @return
     */
    public UserLock selectUserLockById(String userLockId) {
        return userLockMapper.selectById(userLockId);
    }


    /**
     * 新增采购方信息
     * @param map
     * @return
     */
    public Integer addUserPurchaser(Map<String, String> map){
        UserPurchaser userPurchaser = new UserPurchaser();
        userPurchaser.setUserId(map.get("userId"));
        userPurchaser.setCompanyName(map.get("companyName"));
        userPurchaser.setUserName(map.get("userName"));
        userPurchaser.setUserPhone(map.get("phone"));
        userPurchaser.setStatus(Integer.parseInt(map.get("status")));
        if (userPurchaser.getStatus()==0){
            UserPurchaser userPurchaser1 = new UserPurchaser();
            userPurchaser1.setStatus(1);
            userPurchaserMapper.update(userPurchaser1,new QueryWrapper<UserPurchaser>().eq("user_id",userPurchaser.getUserId()));
        }
        return userPurchaserMapper.insert(userPurchaser);
    }

    /**
     * 修改采购方信息
     * @param map
     * @return
     */
    public Integer updateUserPurchaser(Map<String, String> map){
        UserPurchaser userPurchaser = userPurchaserMapper.selectById(map.get("userPurchaserId"));
        if (!StringUtil.isEmpty(map.get("companyName"))) userPurchaser.setCompanyName(map.get("companyName"));
        if (!StringUtil.isEmpty(map.get("userName"))) userPurchaser.setUserName(map.get("userName"));
        if (!StringUtil.isEmpty(map.get("phone"))) userPurchaser.setUserPhone(map.get("phone"));
        if (!StringUtil.isEmpty(map.get("status"))) {
            userPurchaser.setStatus(Integer.parseInt(map.get("status")));
            if (userPurchaser.getStatus()==0){
                UserPurchaser userPurchaser1 = new UserPurchaser();
                userPurchaser1.setStatus(1);
                userPurchaserMapper.update(userPurchaser1,new QueryWrapper<UserPurchaser>().eq("user_id",userPurchaser.getUserId()));
            }
        }
        return userPurchaserMapper.updateById(userPurchaser);
    }

    /**
     * 根据id删除采购方信息
     * @param userPurchaserId
     * @return
     */
    public Integer delUserPurchaser(String userPurchaserId){
        UserPurchaser userPurchaser = userPurchaserMapper.selectById(userPurchaserId);
        int i = userPurchaserMapper.deleteById(userPurchaserId);
        if (userPurchaser.getStatus()==0){
            List<UserPurchaser> userPurchaserList = userPurchaserMapper.selectList(new QueryWrapper<UserPurchaser>().eq("user_id", userPurchaser.getUserId()));
            if (userPurchaserList.size()>0){
                UserPurchaser userPurchaser1 = userPurchaserList.get(0);
                if (userPurchaser1!=null) {
                    userPurchaser1.setStatus(0);
                    userPurchaserMapper.updateById(userPurchaser1);
                }
            }
        }
        return i;
    }

    /**
     * 根据用户id查询所有采购方信息
     * @param map
     * @return
     */
    public List<UserPurchaser> getUserPurchaserList(Map<String, String> map){
        QueryWrapper<UserPurchaser> userPurchaserQueryWrapper = new QueryWrapper<>();
        userPurchaserQueryWrapper.eq("user_id",map.get("userId"));
        if (!StringUtil.isEmpty(map.get("companyName"))) userPurchaserQueryWrapper.eq("company_name",map.get("companyName"));
        if (!StringUtil.isEmpty(map.get("userName"))) userPurchaserQueryWrapper.eq("user_name",map.get("userName"));
        if (!StringUtil.isEmpty(map.get("phone"))) userPurchaserQueryWrapper.eq("phone",map.get("phone"));
        if (!StringUtil.isEmpty(map.get("status"))) userPurchaserQueryWrapper.eq("status",map.get("status"));
        return userPurchaserMapper.selectList(userPurchaserQueryWrapper);
    }

    /**
     * 根据用户id分页查询采购方信息
     * @param map
     * @return
     */
    public PageInfo getUserPurchaserListPage(Map<String, String> map){
        QueryWrapper<UserPurchaser> userPurchaserQueryWrapper = new QueryWrapper<>();
        userPurchaserQueryWrapper.eq("user_id",map.get("userId"));
        if (!StringUtil.isEmpty(map.get("companyName"))) userPurchaserQueryWrapper.eq("company_name",map.get("companyName"));
        if (!StringUtil.isEmpty(map.get("userName"))) userPurchaserQueryWrapper.eq("user_name",map.get("userName"));
        if (!StringUtil.isEmpty(map.get("phone"))) userPurchaserQueryWrapper.eq("phone",map.get("phone"));
        if (!StringUtil.isEmpty(map.get("status"))) userPurchaserQueryWrapper.eq("status",map.get("status"));
        Page page = new Page(Integer.parseInt(map.get("pageNum")),10);
        IPage userWithdrawPriceApplyList = userPurchaserMapper.selectPage(page,userPurchaserQueryWrapper);
        PageInfo pageInfo = new PageInfo(userWithdrawPriceApplyList);
        return pageInfo;
    }

}
