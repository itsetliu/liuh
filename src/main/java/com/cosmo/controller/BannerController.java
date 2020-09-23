package com.cosmo.controller;

import com.cosmo.entity.Banner;
import com.cosmo.service.BannerService;
import com.cosmo.util.*;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
public class BannerController {

    @Resource
    private BannerService bannerService;

    /**
     * 查询所有轮播图
     * @return
     */
    @PostMapping("/banner/banners")
    public CommonResult banners(){
        List<Banner> bannerList = bannerService.Banners();
        if (bannerList.size()>0) return new CommonResult(200,"查询成功",bannerList);
        return new CommonResult(201,"未查询到结果",null);
    }

    /**
     * 新增轮播图
     * @param file
     * @return
     */
    @PostMapping("/banner/addBanner")
    public CommonResult addBanner(MultipartFile file){
        if (file==null) return new CommonResult(500,"图片 为空");
        if (bannerService.addBanner(file)>0) return new CommonResult(200,"新增成功");
        return new CommonResult(201,"新增失败");
    }

    /**
     * 根据id删除轮播图
     * @param request
     * @return
     */
    @PostMapping("/banner/delBanner")
    public CommonResult delBanner(HttpServletRequest request){
        String id = request.getParameter("id");
        if (StringUtil.isEmpty(id)) return new CommonResult(500,"id 为空");
        if (bannerService.delBanner(id)>0) return new CommonResult(200,"删除成功");
        return new CommonResult(201,"删除失败");
    }


    /**
     * 小程序
     */

    /**
     * 查询所有轮播图
     * @return
     */
    @PostMapping("/app/banner/banners")
    public CommonResult app_banners(){
        List<Banner> bannerList = bannerService.Banners();
        if (bannerList.size()>0) return new CommonResult(200,"查询成功",bannerList);
        return new CommonResult(201,"未查询到结果",null);
    }
}
