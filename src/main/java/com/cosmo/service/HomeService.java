package com.cosmo.service;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cosmo.dao.*;
import com.cosmo.entity.HomeAdvertising;
import com.cosmo.entity.HomeCommodity;
import com.cosmo.entity.HomeProduct;
import com.cosmo.entity.HomeTitle;
import com.cosmo.util.FileUtil;
import com.cosmo.util.PageInfo;
import com.cosmo.util.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * @Author: Mr.liu
 * @Data : 2021/3/22 10:03
 */
@Service
public class HomeService {
    @Autowired(required=false)
    private HomeMapper  homeMapper;

    @Autowired(required=false)
    private HomeTitleMapper homeTitleMapper;

    @Autowired(required=false)
    private HomeProductMapper homeCommodity;

    @Autowired(required=false)
    private HomeCommodityMapper  homeCommodityMapper;

    @Resource
    private HomeAdvertisingMapper homeAdvertisingMapper;

    @Resource
    private HomeProductMapper homeProductMapper;


    /*首页图片轮播广告*/
         public Object HomepagePictures(){
             QueryWrapper wrapper=new QueryWrapper();
             wrapper.eq("1", 1);
             Object obj=homeMapper.selectList(wrapper);
             return obj;
         }

        /*首页图片轮播广告*/
        public Object Homeheadline(){
            QueryWrapper wrapper=new QueryWrapper();
            wrapper.eq("1", 1);
            Object obj=homeTitleMapper.selectList(wrapper);
            return obj;
        }

        /*首页商品*/
        public Page<Map<String, Object>> Homecommodity(String titleId,String pageSum){
            QueryWrapper wrapper=new QueryWrapper();
            wrapper.eq("homeTitleId", titleId);
            Page<Map<String, Object>> page=new Page<Map<String, Object>>(Long.parseLong(pageSum),10);
            Page pages=homeCommodity.selectMapsPage(page,wrapper);
            return pages;
        }

        /*商品详情*/
        public Object Homedatails(String homeProductId,int type ){
            QueryWrapper wrapper=new QueryWrapper();
            wrapper.eq("homeProductId", homeProductId);
            wrapper.eq("typeOfMerchandise",type);
            wrapper.orderBy(true,true,"model");
            Object obj=homeCommodityMapper.selectList(wrapper);
            return obj;
        }

    /**
     * 新增首页广告
     * @param advertising
     * @param file
     * @return
     */
    public Integer addHomeAdvertising(String advertising, MultipartFile file){
        HomeAdvertising homeAdvertising = new HomeAdvertising();
        homeAdvertising.setImage(FileUtil.upload(file));
        homeAdvertising.setAdvertising(advertising);
        return homeAdvertisingMapper.insert(homeAdvertising);
    }

    /**
     * 通过id删除首页广告
     * @param id
     * @return
     */
    public Integer delHomeAdvertising(String id){
        HomeAdvertising homeAdvertising = homeAdvertisingMapper.selectById(id);
        if (!FileUtil.delFile(homeAdvertising.getImage())) return 0;
        return homeAdvertisingMapper.deleteById(id);
    }

    /**
     * 分页查询首页广告
     * @param pageNum
     * @return
     */
    public PageInfo selectHomeAdvertisingPage(Long pageNum){
        Page page = new Page(pageNum,10);
        return new PageInfo(homeAdvertisingMapper.selectPage(page,null));
    }

    /**
     * 新增首页标题列表
     * @param title
     * @return
     */
    public Integer addHomeTitle(String title){
        HomeTitle homeTitle = new HomeTitle();
        homeTitle.setTitle(title);
        return homeTitleMapper.insert(homeTitle);
    }

    /**
     * 删除首页标题列表
     * @param id
     * @return
     *  201 该标题下还有商品，不可删除
     */
    public Integer delHomeTitle(String id){
        QueryWrapper<HomeProduct> homeProductQueryWrapper = new QueryWrapper<>();
        homeProductQueryWrapper.eq("homeTitleId",id);
        Integer count = homeProductMapper.selectCount(homeProductQueryWrapper);
        if (count>0) return 201; // 该标题下还有商品，不可删除
        return homeTitleMapper.deleteById(id);
    }

    /**
     * 分页查询首页标题
     * 模糊查询标题
     * @param pageNum
     * @return
     */
    public PageInfo selectHomeTitlePage(Long pageNum, String title){
        Page page = new Page(pageNum,10);
        QueryWrapper<HomeTitle> homeTitleQueryWrapper = new QueryWrapper<>();
        if (!StringUtil.isEmpty(title)) homeTitleQueryWrapper.like("title",title);
        return new PageInfo(homeTitleMapper.selectPage(page,homeTitleQueryWrapper));
    }

    /**
     * 查询全部首页标题
     * @return
     */
    public List<HomeTitle> selectHomeTitle(){
        return homeTitleMapper.selectList(null);
    }

    /**
     * 新增首页商品初始化spu和sku
     * @param map
     * @return
     *  201 商品图片为空，商品图片至少一张
     *  202 sku为空，sku至少一条
     *  203 新增失败
     */
    @Transactional(value="txManager1")
    public Integer addHomeProduct(Map<String, String> map){
        List<String> images = JSON.parseArray(map.get("images"), String.class);
        List<String> imageList = new ArrayList<>();
        if (images.size()>0){
            for (int i = 0; images.size()>i; i++){
                String imgName = FileUtil.upload(FileUtil.base64ToMultipart(images.get(i)));
                imageList.add(imgName);
            }
        }else return 201; // 商品图片为空，商品图片至少一张
        // 初始化 spu最小金额和最大金额
        BigDecimal min = new BigDecimal("0");
        BigDecimal max = new BigDecimal("0");
        List<String> homeCommoditys = JSON.parseArray(map.get("homeCommoditys"), String.class);
        List<HomeCommodity> homeCommodityList = new ArrayList<>();
        if (homeCommoditys.size()>0){
            for (int i = 0; homeCommoditys.size()>i; i++){
                HomeCommodity homeCommodity = JSON.parseObject(homeCommoditys.get(i), HomeCommodity.class);
                if (i==0){
                    min = min.add(homeCommodity.getMoeny());
                    max = max.add(homeCommodity.getMoeny());
                }else {
                    if (min.compareTo(homeCommodity.getMoeny())>0){
                        // 新型号规格小于最小值则覆盖
                        min = homeCommodity.getMoeny();
                    }
                    if (max.compareTo(homeCommodity.getMoeny())<0){
                        // 新型号规格大于最大值则覆盖
                        max = homeCommodity.getMoeny();
                    }
                }
                homeCommodityList.add(homeCommodity);
            }
        }else return 202; // sku为空，sku至少一条
        HomeProduct homeProduct = new HomeProduct();
        homeProduct.setImage(JSON.toJSONString(imageList));
        homeProduct.setName(map.get("name"));
        homeProduct.setPrice(min.toString()+"~"+max.toString());
        homeProduct.setProcessed(Long.parseLong(map.get("processed")));
        homeProduct.setHomeTitleId(map.get("homeTitleId"));
        homeProduct.setModelId(map.get("modelId"));
        // 新增spu
        int insert = homeProductMapper.insert(homeProduct);
        if (insert<=0) return 203; // 新增失败
        for (int i = 0; homeCommodityList.size()>i; i++){
            homeCommodityList.get(i).setHomeProductId(homeProduct.getId());
            int insert1 = homeCommodityMapper.insert(homeCommodityList.get(i));
            if (insert1<=0){
                // 手动报错，触发事务回滚
                throw new RuntimeException();
            }
        }
        return insert;
    }

    /**
     * 更新首页商品初始化spu和sku
     * @param map
     * @return
     *  201 商品图片为空，商品图片至少一张
     *  202 sku为空，sku至少一条
     *  203 更新失败
     */
    @Transactional(value="txManager1")
    public Integer updateHomeProduct(Map<String, String> map){
        HomeProduct homeProductOld = homeProductMapper.selectById(map.get("homeProductId"));
        List<String> images = JSON.parseArray(map.get("images"), String.class);
        List<String> imageList = JSON.parseArray(homeProductOld.getImage(),String.class);
        if (images.size()>0||JSON.parseArray(homeProductOld.getImage(),String.class).size()!=JSON.parseArray(map.get("removeFile"),String.class).size()){
            List<String> removeFile = JSON.parseArray(map.get("removeFile"), String.class);
            if (removeFile.size()>0){
                // 删除要删除的图片
                for (int i = 0; removeFile.size()>i; i++){
                    //使用迭代器删除列表中的元素
                    Iterator<String> iterator=imageList.iterator();
                    while(iterator.hasNext()){
                        String img=iterator.next();
                        if(img.equals(removeFile.get(i))){
                            FileUtil.delFile(img);
                            iterator.remove();
                        }
                    }
                }
            }
            if (images.size()>0){
                for (int i = 0; images.size()>i; i++){
                    String imgName = FileUtil.upload(FileUtil.base64ToMultipart(images.get(i)));
                    imageList.add(imgName);
                }
            }
        }else return 201; // 商品图片为空，商品图片至少一张

        // 初始化 spu最小金额和最大金额
        BigDecimal min = new BigDecimal("0");
        BigDecimal max = new BigDecimal("0");
        List<String> homeCommoditys = JSON.parseArray(map.get("homeCommoditys"), String.class);
        List<HomeCommodity> homeCommodityList = new ArrayList<>();
        if (homeCommoditys.size()>0){
            for (int i = 0; homeCommoditys.size()>i; i++){
                HomeCommodity homeCommodity = JSON.parseObject(homeCommoditys.get(i), HomeCommodity.class);
                if (i==0){
                    min = min.add(homeCommodity.getMoeny());
                    max = max.add(homeCommodity.getMoeny());
                }else {
                    if (min.compareTo(homeCommodity.getMoeny())>0){
                        // 新型号规格小于最小值则覆盖
                        min = homeCommodity.getMoeny();
                    }
                    if (max.compareTo(homeCommodity.getMoeny())<0){
                        // 新型号规格大于最大值则覆盖
                        max = homeCommodity.getMoeny();
                    }
                }
                homeCommodityList.add(homeCommodity);
            }
        }else return 202; // sku为空，sku至少一条
        HomeProduct homeProduct = new HomeProduct();
        homeProduct.setId(map.get("homeProductId"));
        homeProduct.setImage(JSON.toJSONString(imageList));
        homeProduct.setName(map.get("name"));
        homeProduct.setPrice(min.toString()+"~"+max.toString());
        homeProduct.setProcessed(Long.parseLong(map.get("processed")));
        homeProduct.setHomeTitleId(map.get("homeTitleId"));
        homeProduct.setModelId(map.get("modelId"));
        // 更新spu
        int insert = homeProductMapper.updateById(homeProduct);
        if (insert<=0) return 203; // 更新失败
        // 删除原先配置
        int insert2 = homeCommodityMapper.delete(new QueryWrapper<HomeCommodity>().eq("homeProductId", homeProduct.getId()));
        // 新增新配置
        for (int i = 0; homeCommodityList.size()>i; i++){
            homeCommodityList.get(i).setHomeProductId(homeProduct.getId());
            int insert1 = homeCommodityMapper.insert(homeCommodityList.get(i));
            if (insert1<=0){
                // 手动报错，触发事务回滚
                throw new RuntimeException();
            }
        }
        return insert;
    }

    /**
     * 删除首页商品初始化spu和sku
     * @param homeProductId
     * @return
     *  201 删除失败
     */
    @Transactional(value="txManager1")
    public Integer delHomeProduct(String homeProductId){
        HomeProduct homeProduct = homeProductMapper.selectById(homeProductId);
        List<String> imgs = JSON.parseArray(homeProduct.getImage(), String.class);
        imgs.forEach(img->{
            FileUtil.delFile(img);
        });
        int i = homeProductMapper.deleteById(homeProductId);
        if (i<=0) return 201;
        this.homeCommodityMapper.delete(new QueryWrapper<HomeCommodity>().eq("homeProductId",homeProductId));
        return i;
    }

    /**
     * 分页查询spu列表
     * 查询条件：
     *      标题类型
     *      模糊商品名称
     * @param pageNum
     * @param map
     * @return
     */
    public PageInfo selectHomeProductPage(Long pageNum, Map<String,String> map){
        Page page = new Page(pageNum,10);
        QueryWrapper<HomeProduct> homeProductQueryWrapper = new QueryWrapper<>();
        if (!StringUtil.isEmpty(map.get("homeTitleId"))) homeProductQueryWrapper.eq("homeTitleId",map.get("homeTitleId"));
        if (!StringUtil.isEmpty(map.get("name"))) homeProductQueryWrapper.like("name",map.get("name"));
        return new PageInfo(homeProductMapper.selectPage(page,homeProductQueryWrapper));
    }

    /**
     * 根据spuId查询spu、sku详情
     * @param homeProductId
     * @return
     */
    public HomeProduct getHomeProductInfoById(String homeProductId){
        HomeProduct homeProduct = homeProductMapper.selectById(homeProductId);
        if (homeProduct==null) return null;
        homeProduct.setHomeCommoditys(homeCommodityMapper.selectList(new QueryWrapper<HomeCommodity>().eq("homeProductId",homeProductId)));
        return homeProduct;
    }

    /**
     * 新增单条sku
     * @param homeCommodity
     * @return
     */
    /*@Transactional(value="txManager1")
    public Integer addHomeCommodity(HomeCommodity homeCommodity){
        HomeProduct homeProduct = homeProductMapper.selectById(homeCommodity.getHomeProductId());
        String[] price = homeProduct.getPrice().split("~");
        // 初始化 spu最小金额和最大金额
        BigDecimal min = new BigDecimal(price[0]);
        BigDecimal max = new BigDecimal(price[1]);
        if (min.compareTo(homeCommodity.getMoeny())>0){
            // 新型号规格小于最小值则覆盖
            min = homeCommodity.getMoeny();
        }
        if (max.compareTo(homeCommodity.getMoeny())<0){
            // 新型号规格大于最大值则覆盖
            max = homeCommodity.getMoeny();
        }
        homeProduct.setPrice(min.toString()+"~"+max.toString());
        homeProductMapper.updateById(homeProduct);
        return homeCommodityMapper.insert(homeCommodity);
    }*/

    /**
     * 删除单条sku
     * @param id
     * @return
     */
    /*public Integer delHomeCommodity(String id){
        HomeCommodity delHomeCommodity = homeCommodityMapper.selectById(id);
        HomeProduct homeProduct = homeProductMapper.selectById(delHomeCommodity.getHomeProductId());
        List<HomeCommodity> homeCommodityList = homeCommodityMapper.selectList(new QueryWrapper<HomeCommodity>().eq("homeProductId", homeProduct.getId()));
        //处理 homeCommodityList -》 去除集合中要删除的sku
        homeCommodityList = homeCommodityList.stream().filter(homeCommodity -> !homeCommodity.getId().equals(id)).collect(Collectors.toList());
        // 初始化 spu最小金额和最大金额
        BigDecimal min = new BigDecimal("0");
        BigDecimal max = new BigDecimal("0");
        for (int i = 0; homeCommodityList.size()>i; i++){
            if (i==0){
                min = min.add(homeCommodityList.get(i).getMoeny());
                max = max.add(homeCommodityList.get(i).getMoeny());
            }else {
                if (min.compareTo(homeCommodityList.get(i).getMoeny())>0){
                    // 新型号规格小于最小值则覆盖
                    min = homeCommodityList.get(i).getMoeny();
                }
                if (max.compareTo(homeCommodityList.get(i).getMoeny())<0){
                    // 新型号规格大于最大值则覆盖
                    max = homeCommodityList.get(i).getMoeny();
                }
            }
        }
        homeProduct.setPrice(min.toString()+"~"+max.toString());
        homeProductMapper.updateById(homeProduct);
        return homeCommodityMapper.deleteById(id);
    }*/
}
