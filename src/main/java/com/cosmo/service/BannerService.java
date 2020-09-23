package com.cosmo.service;

import com.cosmo.dao.BannerMapper;
import com.cosmo.entity.Banner;
import com.cosmo.util.FileUtil;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.util.List;

@Service
public class BannerService {

    @Resource
    private BannerMapper bannerMapper;

    /**
     * 查询所有轮播图
     * @return
     */
    public List<Banner> Banners(){
        return bannerMapper.selectList(null);
    }

    /**
     * 新增轮播图
     * @param file
     * @return
     */
    public int addBanner(MultipartFile file){
        Banner banner = new Banner();
        banner.setImgUrl(FileUtil.upload(file));
        return bannerMapper.insert(banner);
    }

    /**
     * 删除轮播图
     * @param id
     * @return
     */
    public int delBanner(String id){
        Banner banner = bannerMapper.selectById(id);
        if (!FileUtil.delFile(banner.getImgUrl())) return 0;
        return bannerMapper.deleteById(id);
    }

}
