package com.cosmo.controller;

import com.cosmo.dao.CommentMapper;
import com.cosmo.entity.HomeCommodity;
import com.cosmo.entity.HomeProduct;
import com.cosmo.entity.HomeTitle;
import com.cosmo.service.HomeService;
import com.cosmo.util.CommonResult;
import com.cosmo.util.PageInfo;
import com.cosmo.util.StringUtil;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class HomeController {
    @Resource
    private HomeService homeService;

    /**
     * 新增首页广告
     * @param request
     * @param file
     * @return
     */
    @PostMapping("/home/addHomeAdvertising")
    public CommonResult addHomeAdvertising(HttpServletRequest request, MultipartFile file){
        String advertising = request.getParameter("advertising");
        if (StringUtil.isEmpty(advertising)) return new CommonResult(201, "advertising 为空");
        if (advertising.length()>21) return new CommonResult(201, "advertising 超过最长长度21");
        Integer i = homeService.addHomeAdvertising(advertising, file);
        if (i>0) return new CommonResult(200,"新增成功");
        return new CommonResult(201,"新增失败");
    }

    /**
     * 通过id删除首页广告
     * @param request
     * @return
     */
    @PostMapping("/home/delHomeAdvertising")
    public CommonResult delHomeAdvertising(HttpServletRequest request){
        String id = request.getParameter("id");
        if (StringUtil.isEmpty(id)) return new CommonResult(201, "id 为空");
        Integer i = homeService.delHomeAdvertising(id);
        if (i>0) return new CommonResult(200,"删除成功");
        return new CommonResult(201,"删除失败");
    }

    /**
     * 分页查询首页广告
     * @param pageNum
     * @return
     */
    @GetMapping("/home/selectHomeAdvertisingPage/{pageNum}")
    public CommonResult selectHomeAdvertisingPage(@PathVariable("pageNum") Long pageNum){
        PageInfo pageInfo = homeService.selectHomeAdvertisingPage(pageNum);
        if (pageInfo.getList().size()>0) return new CommonResult(200,"查询成功",pageInfo);
        return new CommonResult(201,"未查询到结果",null);
    }

    /**
     * 新增首页标题列表
     * @param request
     * @return
     */
    @PostMapping("/home/addHomeTitle")
    public CommonResult addHomeTitle(HttpServletRequest request){
        String title = request.getParameter("title");
        if (StringUtil.isEmpty(title)) return new CommonResult(201, "title 为空");
        int i = homeService.addHomeTitle(title);
        if (i>0) return new CommonResult(200,"新增成功");
        return new CommonResult(201,"新增失败");
    }

    /**
     * 删除首页标题列表
     * @param request
     * @return
     */
    @PostMapping("/home/delHomeTitle")
    public CommonResult delHomeTitle(HttpServletRequest request){
        String id = request.getParameter("id");
        if (StringUtil.isEmpty(id)) return new CommonResult(201, "id 为空");
        int i = homeService.delHomeTitle(id);
        if (i>0) return new CommonResult(200,"删除成功");
        return new CommonResult(201,"删除失败");
    }

    /**
     * 分页查询首页标题
     * 模糊查询标题
     * @param pageNum
     * @param request
     * @return
     */
    @GetMapping("/home/selectHomeTitlePage/{pageNum}")
    public CommonResult selectHomeTitlePage(@PathVariable("pageNum") Long pageNum, HttpServletRequest request){
        String title = request.getParameter("title");
        PageInfo pageInfo = homeService.selectHomeTitlePage(pageNum,title);
        if (pageInfo.getList().size()>0) return new CommonResult(200,"查询成功",pageInfo);
        return new CommonResult(201,"未查询到结果",null);
    }

    /**
     * 查询首页标题
     * @return
     */
    @GetMapping("/home/selectHomeTitle")
    public CommonResult selectHomeTitle(){
        List<HomeTitle> homeTitleList = homeService.selectHomeTitle();
        if (homeTitleList.size()>0) return new CommonResult(200,"查询成功",homeTitleList);
        return new CommonResult(201,"未查询到结果",null);
    }

    /**
     * 新增首页商品初始化spu和sku
     * @param request
     * @return
     */
    @PostMapping("/home/addHomeProduct")
    public CommonResult addHomeProduct(HttpServletRequest request){
        String images = request.getParameter("images");
        if (StringUtil.isEmpty(images)) return new CommonResult(201, "images 为空");
        String homeCommoditys = request.getParameter("homeCommoditys");
        if (StringUtil.isEmpty(homeCommoditys)) return new CommonResult(201, "homeCommoditys 为空");
        String name = request.getParameter("name");
        if (StringUtil.isEmpty(name)) return new CommonResult(201, "name 为空");
        String processed = request.getParameter("processed");
        if (StringUtil.isEmpty(processed)) return new CommonResult(201, "processed 为空");
        String homeTitleId = request.getParameter("homeTitleId");
        if (StringUtil.isEmpty(homeTitleId)) return new CommonResult(201, "homeTitleId 为空");
        String modelId = request.getParameter("modelId");
        if (StringUtil.isEmpty(modelId)) return new CommonResult(201, "modelId 为空");
        Map<String, String> map = new HashMap<>();
        map.put("images",images);
        map.put("homeCommoditys",homeCommoditys);
        map.put("name",name);
        map.put("processed",processed);
        map.put("homeTitleId",homeTitleId);
        map.put("modelId",modelId);
        Integer i = homeService.addHomeProduct(map);
        if (i==201) return new CommonResult(201,"商品图片为空，商品图片至少一张");
        else if (i==202) return new CommonResult(201,"sku为空，sku至少一条");
        else if (i==203) return new CommonResult(201,"新增失败");
        else if (i>0) return new CommonResult(200,"新增成功");
        return new CommonResult(201,"新增失败");
    }

    /**
     * 删除首页商品
     * @param request
     * @return
     */
    @PostMapping("/home/delHomeProduct")
    public CommonResult delHomeProduct(HttpServletRequest request){
        String homeProductId = request.getParameter("homeProductId");
        if (StringUtil.isEmpty(homeProductId)) return new CommonResult(201, "homeProductId 为空");
        int i = homeService.delHomeProduct(homeProductId);
        if (i>0) return new CommonResult(200,"删除成功");
        return new CommonResult(201,"删除失败");
    }

    /**
     * 分页查询spu列表
     * 查询条件：
     *      标题类型
     *      模糊商品名称
     * @param pageNum
     * @param request
     * @return
     */
    @GetMapping("/home/selectHomeProductPage/{pageNum}")
    public CommonResult selectHomeProductPage(@PathVariable("pageNum") Long pageNum, HttpServletRequest request){
        String homeTitleId = request.getParameter("homeTitleId");
        String name = request.getParameter("name");
        Map<String, String> map = new HashMap<>();
        map.put("homeTitleId",homeTitleId);
        map.put("name",name);
        PageInfo pageInfo = homeService.selectHomeProductPage(pageNum,map);
        if (pageInfo.getList().size()>0) return new CommonResult(200,"查询成功",pageInfo);
        return new CommonResult(201,"未查询到结果",null);
    }

    /**
     * 根据spuId查询spu、sku详情
     * @param homeProductId
     * @return
     */
    @GetMapping("/home/getHomeProductInfoById/{homeProductId}")
    public CommonResult getHomeProductInfoById(@PathVariable("homeProductId") String homeProductId){
        if (StringUtil.isEmpty(homeProductId)) return new CommonResult(201, "homeProductId 为空");
        HomeProduct homeProduct = homeService.getHomeProductInfoById(homeProductId);
        if (homeProduct!=null) return new CommonResult(200,"查询成功",homeProduct);
        return new CommonResult(201,"未查询到结果",null);
    }

    /**
     * 更新首页商品初始化spu和sku
     * @param request
     * @return
     */
    @PostMapping("/home/updateHomeProduct")
    public CommonResult updateHomeProduct(HttpServletRequest request){
        String homeProductId = request.getParameter("homeProductId");
        if (StringUtil.isEmpty(homeProductId)) return new CommonResult(201, "homeProductId 为空");
        String images = request.getParameter("images");
        if (StringUtil.isEmpty(images)) return new CommonResult(201, "images 为空");
        String removeFile = request.getParameter("removeFile");
        if (StringUtil.isEmpty(removeFile)) return new CommonResult(201, "removeFile 为空");
        String homeCommoditys = request.getParameter("homeCommoditys");
        if (StringUtil.isEmpty(homeCommoditys)) return new CommonResult(201, "homeCommoditys 为空");
        String name = request.getParameter("name");
        if (StringUtil.isEmpty(name)) return new CommonResult(201, "name 为空");
        String processed = request.getParameter("processed");
        if (StringUtil.isEmpty(processed)) return new CommonResult(201, "processed 为空");
        String homeTitleId = request.getParameter("homeTitleId");
        if (StringUtil.isEmpty(homeTitleId)) return new CommonResult(201, "homeTitleId 为空");
        String modelId = request.getParameter("modelId");
        if (StringUtil.isEmpty(modelId)) return new CommonResult(201, "modelId 为空");
        Map<String, String> map = new HashMap<>();
        map.put("homeProductId",homeProductId);
        map.put("images",images);
        map.put("removeFile",removeFile);
        map.put("homeCommoditys",homeCommoditys);
        map.put("name",name);
        map.put("processed",processed);
        map.put("homeTitleId",homeTitleId);
        map.put("modelId",modelId);
        Integer i = homeService.updateHomeProduct(map);
        if (i==201) return new CommonResult(201,"商品图片为空，商品图片至少一张");
        else if (i==202) return new CommonResult(201,"sku为空，sku至少一条");
        else if (i==203) return new CommonResult(201,"更新失败");
        else if (i>0) return new CommonResult(200,"更新成功");
        return new CommonResult(201,"更新失败");
    }

    /**
     * 新增首页商品配置
     * @param request
     * @return
     */
    /*@PostMapping("/home/addHomeCommodity")
    public CommonResult addHomeCommodity(HttpServletRequest request){
        String model = request.getParameter("model");
        if (StringUtil.isEmpty(model)) return new CommonResult(201, "model 为空");
        String volume = request.getParameter("volume");
        if (StringUtil.isEmpty(volume)) return new CommonResult(201, "volume 为空");
        String thickness = request.getParameter("thickness");
        if (StringUtil.isEmpty(thickness)) return new CommonResult(201, "thickness 为空");
        String width = request.getParameter("width");
        if (StringUtil.isEmpty(width)) return new CommonResult(201, "width 为空");
        String length = request.getParameter("length");
        if (StringUtil.isEmpty(length)) return new CommonResult(201, "length 为空");
        String tubeWeight = request.getParameter("tubeWeight");
        if (StringUtil.isEmpty(tubeWeight)) return new CommonResult(201, "tubeWeight 为空");
        String suttle = request.getParameter("suttle");
        if (StringUtil.isEmpty(suttle)) return new CommonResult(201, "suttle 为空");
        String roughWeight = request.getParameter("roughWeight");
        if (StringUtil.isEmpty(roughWeight)) return new CommonResult(201, "roughWeight 为空");
        String price = request.getParameter("price");
        if (StringUtil.isEmpty(price)) return new CommonResult(201, "price 为空");
        String pipe = request.getParameter("pipe");
        if (StringUtil.isEmpty(pipe)) return new CommonResult(201, "pipe 为空");
        String moeny = request.getParameter("moeny");
        if (StringUtil.isEmpty(moeny)) return new CommonResult(201, "moeny 为空");
        String labelType = request.getParameter("labelType");
        if (StringUtil.isEmpty(labelType)) return new CommonResult(201, "labelType 为空");
        String trayType = request.getParameter("trayType");
        if (StringUtil.isEmpty(trayType)) return new CommonResult(201, "trayType 为空");
        String traySum = request.getParameter("traySum");
        if (StringUtil.isEmpty(traySum)) return new CommonResult(201, "traySum 为空");
        String boxVolume = request.getParameter("boxVolume");
        if (StringUtil.isEmpty(boxVolume)) return new CommonResult(201, "boxVolume 为空");
        String boxType = request.getParameter("boxType");
        if (StringUtil.isEmpty(boxType)) return new CommonResult(201, "boxType 为空");
        String boxSumOrVolumeSum = request.getParameter("boxSumOrVolumeSum");
        if (StringUtil.isEmpty(boxSumOrVolumeSum)) return new CommonResult(201, "boxSumOrVolumeSum 为空");
        String homeProductId = request.getParameter("homeProductId");
        if (StringUtil.isEmpty(homeProductId)) return new CommonResult(201, "homeProductId 为空");
        HomeCommodity homeCommodity = new HomeCommodity();
        homeCommodity.setModel(model);
        homeCommodity.setVolume(volume);
        homeCommodity.setThickness(thickness);
        homeCommodity.setWidth(width);
        homeCommodity.setLength(length);
        homeCommodity.setTubeWeight(tubeWeight);
        homeCommodity.setSuttle(suttle);
        homeCommodity.setRoughWeight(roughWeight);
        homeCommodity.setPrice(price);
        homeCommodity.setPipe(pipe);
        homeCommodity.setMoeny(new BigDecimal(moeny));
        homeCommodity.setLabelType(labelType);
        homeCommodity.setTrayType(trayType);
        homeCommodity.setTraySum(traySum);
        homeCommodity.setBoxVolume(boxVolume);
        homeCommodity.setBoxType(boxType);
        homeCommodity.setBoxSumOrVolumeSum(boxSumOrVolumeSum);
        homeCommodity.setHomeProductId(homeProductId);
        int i = homeService.addHomeTitle(boxType);
        if (i>0) return new CommonResult(200,"新增成功");
        return new CommonResult(201,"新增失败");
    }*/

    /**
     * 删除单条sku
     * @param request
     * @return
     */
    /*@PostMapping("/home/delHomeCommodity")
    public CommonResult delHomeCommodity(HttpServletRequest request){
        String id = request.getParameter("id");
        if (StringUtil.isEmpty(id)) return new CommonResult(201, "id 为空");
        int i = homeService.delHomeCommodity(id);
        if (i>0) return new CommonResult(200,"删除成功");
        return new CommonResult(201,"删除失败");
    }*/

}
