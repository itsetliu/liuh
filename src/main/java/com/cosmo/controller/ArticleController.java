package com.cosmo.controller;

import com.cosmo.entity.*;
import com.cosmo.service.ArticleService;
import com.cosmo.util.*;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author ZhaoZiQu
 * @version 2020/6/29
 */
@RestController
public class ArticleController {

    @Resource
    private ArticleService articleService;

    /**
     * 新增文章分类
     * @param request
     * @return
     */
    @PostMapping("/article/addArticleClassify")
    public CommonResult addArticleClassify(HttpServletRequest request){
        String name = request.getParameter("name");
        if (StringUtil.isEmpty(name)) return new CommonResult(500,"name 为空");
        ArticleClassify articleClassify = new ArticleClassify();
        articleClassify.setName(name);
        Integer i = articleService.addArticleClassify(articleClassify);
        if (i>0) return new CommonResult(200,"新增成功");
        return new CommonResult(201,"新增失败");
    }

    /**
     * 删除文章分类
     * @param request
     * @return
     */
    @PostMapping("/article/delArticleClassify")
    public CommonResult delArticleClassify(HttpServletRequest request){
        String id = request.getParameter("id");
        if (StringUtil.isEmpty(id)) return new CommonResult(500,"id 为空");
        Integer i = articleService.delArticleClassify(Integer.parseInt(id));
        if (i>0) return new CommonResult(200,"删除成功");
        return new CommonResult(201,"删除失败");
    }

    /**
     * 查询所有文章分类
     * @param request
     * @return
     */
    @GetMapping("/article/articleClassifyList")
    public CommonResult articleClassifyListPc(HttpServletRequest request){
        String name = request.getParameter("name");
        List<ArticleClassify> articleClassifyList = articleService.articleClassifyListPc(name);
        if (articleClassifyList.size()>0) return new CommonResult(200,"查询成功",articleClassifyList);
        return new CommonResult(201,"未查询到结果",null);
    }

    /**
     * 查询所有文章分类
     * @param request
     * @return
     */
    @GetMapping("/app/article/articleClassifyList")
    public CommonResult articleClassifyList(HttpServletRequest request){
        List<ArticleClassify> articleClassifyList = articleService.articleClassifyList();
        if (articleClassifyList.size()>0) return new CommonResult(200,"查询成功",articleClassifyList);
        return new CommonResult(201,"未查询到结果",null);
    }

    /**
     * 新增文章
     * @param request
     * @return
     */
    @PostMapping("/app/article/addArticle")
    public CommonResult addArticle(HttpServletRequest request,MultipartFile video){
        Map<String,String> map = new HashMap<>();
        String userId = request.getParameter("userId");
        if (StringUtil.isEmpty(userId)) return new CommonResult(500,"userId 为空");
        String type = request.getParameter("type");
        if (StringUtil.isEmpty(type)) return new CommonResult(500,"type 为空");
        String title = request.getParameter("title");
        if (StringUtil.isEmpty(title)) return new CommonResult(500,"title 为空");
        String classifyId = request.getParameter("classifyId");
        if (StringUtil.isEmpty(classifyId)) return new CommonResult(500,"classifyIdclassifyId 为空");
        if ("1".equals(type)){//视频-文字
            String infoArray = request.getParameter("infoArray");
            if (StringUtil.isEmpty(infoArray)) return new CommonResult(500,"infoArray 为空");
            String videoImg = request.getParameter("videoImg");
            if (StringUtil.isEmpty(videoImg)) return new CommonResult(500,"videoImg 为空");
            map.put("infoArray",infoArray);map.put("videoImg",videoImg);
        }else if ("2".equals(type)){//纯文字
            String infoArray = request.getParameter("infoArray");
            if (StringUtil.isEmpty(infoArray)) return new CommonResult(500,"infoArray 为空");
            map.put("infoArray",infoArray);
        }else if ("3".equals(type)){//纯图片
            String imgArray = request.getParameter("imgArray");
            if (StringUtil.isEmpty(imgArray)) return new CommonResult(500,"imgArray 为空");
            map.put("imgArray",imgArray);
        }else if ("4".equals(type)||"5".equals(type)){//文字-图片 或 图片-文字
            String infoArray = request.getParameter("infoArray");
            if (StringUtil.isEmpty(infoArray)) return new CommonResult(500,"infoArray 为空");
            String imgArray = request.getParameter("imgArray");
            if (StringUtil.isEmpty(imgArray)) return new CommonResult(500,"imgArray 为空");
            map.put("infoArray",infoArray);map.put("imgArray",imgArray);
        }
        map.put("classifyId",classifyId);map.put("userId",userId);map.put("type",type);map.put("title",title);
        Integer i = articleService.addArticle(map,video);
        if (i==201) return new CommonResult(201,"视频文件流为空");
        else if (i>0) return new CommonResult(200,"新增成功");
        return new CommonResult(201,"新增失败");
    }

    /**
     * 删除文章
     * @param request
     * @return
     */
    @PostMapping("/app/article/delArticle")
    public CommonResult delArticle(HttpServletRequest request){
        String articleId = request.getParameter("articleId");
        if (StringUtil.isEmpty(articleId)) return new CommonResult(500,"articleId 为空");
        Integer i = articleService.delArticle(Long.valueOf(articleId));
        if (i>0) return new CommonResult(200,"删除成功");
        return new CommonResult(201,"删除失败");
    }

    /**
     * 查询文章列表，分页
     * @param request
     * @return
     *      返回值中 praiseExist：当前用户是否点赞文章
     *      返回值中 browseExist：当前用户是否浏览文章
     */
    @GetMapping("/app/article/articleList")
    public CommonResult articleList(HttpServletRequest request){
        String pageNum = request.getParameter("pageNum");
        if (StringUtil.isEmpty(pageNum)) pageNum = "1";
        String classifyId = request.getParameter("classifyId");
        if (StringUtil.isEmpty(classifyId)) return new CommonResult(500,"classifyId 为空");
        String userId = request.getParameter("userId");
        if (StringUtil.isEmpty(userId)) return new CommonResult(500,"userId 为空");
        Article article = new Article();
        article.setClassifyId(Long.valueOf(classifyId));
        PageInfo pageInfo = articleService.articleList(Integer.parseInt(pageNum),article,userId);
        if (pageInfo.getList().size()>0) return new CommonResult(200,"查询成功",pageInfo);
        return new CommonResult(201,"未查询到结果",null);
    }

    /**
     * 查询该用户发布文章列表，分页
     * @param request
     * @return
     *      返回值中 praiseExist：当前用户是否点赞文章
     *      返回值中 browseExist：当前用户是否浏览文章
     */
    @GetMapping("/app/article/articleUserList")
    public CommonResult articleUserList(HttpServletRequest request){
        String pageNum = request.getParameter("pageNum");
        if (StringUtil.isEmpty(pageNum)) pageNum = "1";
        String classifyId = request.getParameter("classifyId");
        if (StringUtil.isEmpty(classifyId)) return new CommonResult(500,"classifyId 为空");
        String userId = request.getParameter("userId");
        if (StringUtil.isEmpty(userId)) return new CommonResult(500,"userId 为空");
        Article article = new Article();
        article.setUserId(Long.valueOf(userId));
        article.setClassifyId(Long.valueOf(classifyId));
        PageInfo pageInfo = articleService.articleList(Integer.parseInt(pageNum),article,userId);
        if (pageInfo.getList().size()>0) return new CommonResult(200,"查询成功",pageInfo);
        return new CommonResult(201,"未查询到结果",null);
    }

    /**
     * 查询文章列表，分页
     * @param request
     * @return
     */
    @GetMapping("/article/articleList")
    public CommonResult articleListPc(HttpServletRequest request){
        String pageNum = request.getParameter("pageNum");
        if (StringUtil.isEmpty(pageNum)) pageNum = "1";
        String classifyId = request.getParameter("classifyId");
        if (StringUtil.isEmpty(classifyId)) return new CommonResult(500,"classifyId 为空");
        String title = request.getParameter("title");
        Article article = new Article();
        article.setClassifyId(Long.valueOf(classifyId));
        article.setTitle("%"+title+"%");
        PageInfo pageInfo = articleService.articleList(Integer.parseInt(pageNum),article,null);
        if (pageInfo.getList().size()>0) return new CommonResult(200,"查询成功",pageInfo);
        return new CommonResult(201,"未查询到结果",null);
    }

    /**
     * 新增评论
     * @param request
     * @return
     */
    @PostMapping("/app/article/addComment")
    public CommonResult addComment(HttpServletRequest request){
        String userId = request.getParameter("userId");
        if (StringUtil.isEmpty(userId)) return new CommonResult(500,"userId 为空");
        String articleId = request.getParameter("articleId");
        if (StringUtil.isEmpty(articleId)) return new CommonResult(500,"articleId 为空");
        String parentId = request.getParameter("parentId");
        if (StringUtil.isEmpty(parentId)) return new CommonResult(500,"parentId 为空");
        String info = request.getParameter("info");
        if (StringUtil.isEmpty(info)) return new CommonResult(500,"info 为空");
        Comment comment = new Comment();
        comment.setUserId(Long.valueOf(userId));
        comment.setArticleId(Long.valueOf(articleId));
        comment.setParentId(Long.valueOf(parentId));
        comment.setPraiseNumber(0);
        comment.setInfo(info);
        Integer i = articleService.addComment(comment);
        if (i>0) return new CommonResult(200,"评论成功");
        return new CommonResult(201,"评论失败");
    }

    /**
     * 删除评论
     * @param request
     * @return
     */
    @PostMapping("/app/article/delComment")
    public CommonResult delComment(HttpServletRequest request){
        String commentId = request.getParameter("commentId");
        if (StringUtil.isEmpty(commentId)) return new CommonResult(500,"commentId 为空");
        Integer i = articleService.delComment(Integer.parseInt(commentId));
        if (i>0) return new CommonResult(200,"删除成功");
        return new CommonResult(201,"删除失败");
    }

    /**
     * 点赞/浏览量
     * @param request
     * @return
     */
    @PostMapping("/app/article/addUserPraiseBrowse")
    public CommonResult addUserPraiseBrowse(HttpServletRequest request){
        String userId = request.getParameter("userId");
        if (StringUtil.isEmpty(userId)) return new CommonResult(500,"userId 为空");
        String type = request.getParameter("type");
        if (StringUtil.isEmpty(type)) return new CommonResult(500,"type 为空");
        String parentId = request.getParameter("parentId");
        if (StringUtil.isEmpty(parentId)) return new CommonResult(500,"parentId 为空");
        UserPraiseBrowse userPraiseBrowse = new UserPraiseBrowse();
        userPraiseBrowse.setUserId(Long.valueOf(userId));
        userPraiseBrowse.setType(Integer.parseInt(type));
        userPraiseBrowse.setParentId(Long.valueOf(parentId));
        Integer i = articleService.addUserPraiseBrowse(userPraiseBrowse);
        if (i==201) return new CommonResult(201,"已点赞/已浏览");
        else if (i==202) return new CommonResult(201,"type类型错误");
        else if (i==203) return new CommonResult(201,"没用该文章");
        else if (i==204) return new CommonResult(201,"没用该评论");
        else if (i>0) return new CommonResult(200,"点赞成功");
        return new CommonResult(201,"点赞失败");
    }

    /**
     * 取消点赞
     * @param request
     * @return
     */
    @PostMapping("/app/article/delUserPraiseBrowse")
    public CommonResult delUserPraiseBrowse(HttpServletRequest request){
        String userId = request.getParameter("userId");
        if (StringUtil.isEmpty(userId)) return new CommonResult(500,"userId 为空");
        String type = request.getParameter("type");
        if (StringUtil.isEmpty(type)) return new CommonResult(500,"type 为空");
        String parentId = request.getParameter("parentId");
        if (StringUtil.isEmpty(parentId)) return new CommonResult(500,"parentId 为空");
        Integer i = articleService.delUserPraiseBrowse(Integer.parseInt(userId),Integer.parseInt(type),Integer.parseInt(parentId));
        if (i==201) return new CommonResult(201,"无该点赞");
        else if (i==202) return new CommonResult(201,"该点赞不存在");
        else if (i>0) return new CommonResult(200,"取消点赞成功");
        return new CommonResult(201,"取消点赞失败");
    }

    /**
     * 查询评论，分页
     *      查询第一级时 parentId 传 0
     * @param request
     * @return
     *      返回值中 praiseExist：当前用户是否点赞评论
     *      返回值中 sonExist：是否有子级评论
     */
    @GetMapping("/app/article/commentList")
    public CommonResult commentList(HttpServletRequest request){
        String pageNum = request.getParameter("pageNum");
        if (StringUtil.isEmpty(pageNum)) pageNum = "1";
        String articleId = request.getParameter("articleId");
        if (StringUtil.isEmpty(articleId)) return new CommonResult(500,"articleId 为空");
        String parentId = request.getParameter("parentId");
        if (StringUtil.isEmpty(parentId)) return new CommonResult(500,"parentId 为空");
        String userId = request.getParameter("userId");
        if (StringUtil.isEmpty(userId)) return new CommonResult(500,"userId 为空");
        Map<String,String> map = new HashMap<>();
        map.put("articleId",articleId);map.put("parentId",parentId);map.put("userId",userId);
        PageInfo pageInfo = articleService.commentList(Integer.parseInt(pageNum),map);
        if (pageInfo.getList().size()>0) return new CommonResult(200,"查询成功",pageInfo);
        return new CommonResult(201,"未查询到结果",null);
    }

    /**
     * 根据文章id、用户id
     * 查询文章详情
     * @param request
     * @return
     *      browseExist: 当前用户是否浏览过
     *      praiseExist: 当前用户是否点赞
     */
    @GetMapping("/app/article/selectArticleMap")
    public CommonResult selectArticleMap(HttpServletRequest request){
        String articleId = request.getParameter("articleId");
        if (StringUtil.isEmpty(articleId)) return new CommonResult(500,"articleId 为空");
        String userId = request.getParameter("userId");
        if (StringUtil.isEmpty(userId)) return new CommonResult(500,"userId 为空");
        Map<String,Object> map = articleService.selectArticleMap(Long.valueOf(articleId),Long.valueOf(userId));
        if (map!=null) return new CommonResult(200,"查询成功",map);
        return new CommonResult(201,"未查询到结果",null);
    }
}