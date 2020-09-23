package com.cosmo.service;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cosmo.dao.*;
import com.cosmo.entity.*;
import com.cosmo.util.*;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author ZhaoZiQu
 * @version 2020/6/29
 */
@Service
public class ArticleService {

    @Resource
    private UserInfoMapper userInfoMapper;
    @Resource
    private ArticleMapper articleMapper;
    @Resource
    private ArticleClassifyMapper articleClassifyMapper;
    @Resource
    private CommentMapper commentMapper;
    @Resource
    private UserPraiseBrowseMapper userPraiseBrowseMapper;
    @Resource
    private ConfigMapper configMapper;

    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    /**
     * 新增文章分类
     * @param articleClassify
     * @return
     */
    public Integer addArticleClassify(ArticleClassify articleClassify){
        return articleClassifyMapper.insert(articleClassify);
    }

    /**
     * 删除文章分类
     * @param articleClassifyId
     * @return
     */
    public Integer delArticleClassify(String articleClassifyId){
        return articleClassifyMapper.deleteById(articleClassifyId);
    }

    /**
     * 查询所有文章分类
     * @return
     */
    public List<ArticleClassify> articleClassifyListPc(String name){
        QueryWrapper<ArticleClassify> articleClassifyQueryWrapper = new QueryWrapper<>();
        articleClassifyQueryWrapper.like("name",name);
        return articleClassifyMapper.selectList(articleClassifyQueryWrapper);
    }

    /**
     * 查询所有文章分类
     * @return
     */
    public List<ArticleClassify> articleClassifyList(){
        return articleClassifyMapper.selectList(null);
    }

    /**
     * 新增文章
     * @param map
     * @return
     *      201:视频文件流为空
     */
    public Integer addArticle(Map<String,String> map, MultipartFile video){
        Article article = new Article();
        article.setUserId(map.get("userId"));
        article.setBrowseNumber(0);
        article.setPraiseNumber(0);
        article.setType(Integer.parseInt(map.get("type")));
        article.setTime(sdf.format(new Date()));
        article.setTitle(map.get("title"));
        article.setClassifyId(map.get("classifyId"));
        if (article.getType()==1){//视频-文字
            if (video==null) return 201;
            article.setVideo(FileUtil.upload(video));
            article.setInfoArray(map.get("infoArray"));
            String imgName = FileUtil.upload(FileUtil.base64ToMultipart(map.get("videoImg")));
            List<String> imgArray1 = new ArrayList<>();
            imgArray1.add(imgName);
            article.setImgArray(JSON.toJSONString(imgArray1));
        }else if (article.getType()==2){//纯文字
            article.setInfoArray(map.get("infoArray"));
        }else if (article.getType()==3){//纯图片
            List<String> imgArray = JSON.parseArray(map.get("imgArray"),String.class);
            List<String> imgArray1 = new ArrayList<>();
            imgArray.forEach(img->{
                String imgName = FileUtil.upload(FileUtil.base64ToMultipart(img));
                imgArray1.add(imgName);
            });
            article.setImgArray(JSON.toJSONString(imgArray1));
        }else if (article.getType()==4||article.getType()==5){//文字-图片 或 图片-文字
            List<String> imgArray = JSON.parseArray(map.get("imgArray"),String.class);
            List<String> imgArray1 = new ArrayList<>();
            imgArray.forEach(img->{
                String imgName = FileUtil.upload(FileUtil.base64ToMultipart(img));
                imgArray1.add(imgName);
            });
            article.setImgArray(JSON.toJSONString(imgArray1));
            article.setInfoArray(map.get("infoArray"));
        }
        return articleMapper.addArticle(article);
    }

    /**
     * 删除文章
     * @param articleId
     * @return
     */
    public Integer delArticle(String articleId){
        Article article = articleMapper.selectArticle(articleId);
        if (article.getType()==1){
            FileUtil.delFile(article.getVideo());
        }else if (article.getType()==3||article.getType()==4||article.getType()==5){
            List<String> imgArray = JSON.parseArray(article.getImgArray(),String.class);
             if (imgArray.size()>0) imgArray.forEach(img->FileUtil.delFile(img));
        }
        Integer i = articleMapper.delArticle(articleId);
        if (i<=0) return i;
        QueryWrapper<UserPraiseBrowse> userPraiseBrowseQueryWrapper = new QueryWrapper<>();
        userPraiseBrowseQueryWrapper.eq("article_id",articleId);
        this.userPraiseBrowseMapper.delete(userPraiseBrowseQueryWrapper);//删除所有的文章点赞、文章浏览、评论点赞
        QueryWrapper<Comment> commentQueryWrapper = new QueryWrapper<>();
        commentQueryWrapper.eq("article_id",articleId);
        this.commentMapper.delete(commentQueryWrapper);//删除所有文章评论
        return i;
    }

    /**
     * 查询文章列表，分页
     * @param pageNum
     * @param article
     * @return
     */
    public PageInfo articleList(Integer pageNum,Article article,String userId){
        Page<Map<String,Object>> page = new Page<>(pageNum,10);
        IPage<Map<String,Object>> articles = articleMapper.articleList(page,article);
        if (userId!=null){
            for (Map<String,Object> articleMap:articles.getRecords()){
                QueryWrapper<UserPraiseBrowse> userPraiseBrowseQueryWrapper = new QueryWrapper<>();
                Map<String,String> userPraiseBrowseMap = new HashMap<>();
                userPraiseBrowseMap.put("article_id",String.valueOf(articleMap.get("id")));
                userPraiseBrowseMap.put("parent_id",String.valueOf(articleMap.get("id")));
                userPraiseBrowseMap.put("user_id",userId);
                userPraiseBrowseMap.put("type","1");
                userPraiseBrowseQueryWrapper.allEq(userPraiseBrowseMap,false);
                List<UserPraiseBrowse> userPraiseBrowseList = userPraiseBrowseMapper.selectList(userPraiseBrowseQueryWrapper);
                if (userPraiseBrowseList.size()<=0) articleMap.put("praiseExist","false");
                else articleMap.put("praiseExist","true");
                if (article.getUserId()==null){
                    QueryWrapper<UserPraiseBrowse> userPraiseBrowseQueryWrapper1 = new QueryWrapper<>();
                    userPraiseBrowseMap.put("article_id",String.valueOf(articleMap.get("id")));
                    userPraiseBrowseMap.put("parent_id",String.valueOf(articleMap.get("id")));
                    userPraiseBrowseMap.put("user_id",userId);
                    userPraiseBrowseMap.put("type","3");
                    userPraiseBrowseQueryWrapper1.allEq(userPraiseBrowseMap,false);
                    List<UserPraiseBrowse> userPraiseBrowseList1 = userPraiseBrowseMapper.selectList(userPraiseBrowseQueryWrapper1);
                    if (userPraiseBrowseList1.size()<=0) articleMap.put("browseExist","false");
                    else articleMap.put("browseExist","true");
                }else articleMap.put("browseExist","true");
            }
        }
        PageInfo pageInfo = new PageInfo(articles);
        return pageInfo;
    }

    /**
     * 新增评论
     * @param comment
     * @return
     */
    public Integer addComment(Comment comment){
        comment.setTime(sdf.format(new Date()));
        Comment comment1 = commentMapper.selectById(comment.getParentId());
        if (comment1==null) {
            List<Integer> parents = new ArrayList<>();
            parents.add(0);
            comment.setParents(JSON.toJSONString(parents));
        }else {
            List<String> parents = JSON.parseArray(comment1.getParents(),String.class);
            parents.add(comment1.getId());
            comment.setParents(JSON.toJSONString(parents));
        }
        List<String> sons = new ArrayList<>();
        comment.setSons(JSON.toJSONString(sons));
        Integer l = commentMapper.insert(comment);
        if (l<=0) return l;
        List<String> parentIds = JSON.parseArray(comment.getParents(),String.class);
        for (int i=1;i<parentIds.size();i++){
            Comment comment2 = commentMapper.selectById(parentIds.get(i));
            List<String> sons1 = JSON.parseArray(comment2.getSons(),String.class);
            sons1.add(comment.getId());
            comment2.setSons(JSON.toJSONString(sons1));
            this.commentMapper.updateById(comment2);
        }
        return l;
    }

    /**
     * 删除评论
     * @param commentId
     * @return
     */
    public Integer delComment(String commentId){
        Comment comment = commentMapper.selectById(commentId);
        Integer i = commentMapper.deleteById(commentId);
        if (i<=0) return i;
        List<String> sons = JSON.parseArray(comment.getSons(),String.class);
        if (sons.size()>0) sons.forEach(son->{
            this.commentMapper.deleteById(son);
            QueryWrapper<UserPraiseBrowse> userPraiseBrowseQueryWrapper = new QueryWrapper<>();
            userPraiseBrowseQueryWrapper.eq("type","2").eq("parent_id",son);
            this.userPraiseBrowseMapper.delete(userPraiseBrowseQueryWrapper);
        });
        return i;
    }

    /**
     * 点赞/浏览量
     * 201: 已点赞/已浏览
     * 202: type类型错误
     * 203: 没用该文章
     * 202: 没用该评论
     * @param userPraiseBrowse
     * @return
     */
    public Integer addUserPraiseBrowse(UserPraiseBrowse userPraiseBrowse){
        QueryWrapper<UserPraiseBrowse> userPraiseBrowseQueryWrapper = new QueryWrapper<>();
        Map<String,Object> userPraiseBrowseMap = new HashMap<>();
        userPraiseBrowseMap.put("user_id",userPraiseBrowse.getUserId());
        userPraiseBrowseMap.put("type",userPraiseBrowse.getType());
        userPraiseBrowseMap.put("parent_id",userPraiseBrowse.getParentId());
        userPraiseBrowseQueryWrapper.allEq(userPraiseBrowseMap,false);
        List<UserPraiseBrowse> userPraiseBrowseList = userPraiseBrowseMapper.selectList(userPraiseBrowseQueryWrapper);
        if (userPraiseBrowseList.size()>0) return 201;
        if (userPraiseBrowse.getType()==1){//文章点赞
            Article article = articleMapper.selectArticle(userPraiseBrowse.getParentId());
            if (article==null) return 203;
            article.setPraiseNumber(article.getPraiseNumber()+1);
            this.articleMapper.updateArticle(article);
            userPraiseBrowse.setArticleId(userPraiseBrowse.getParentId());
        }else if (userPraiseBrowse.getType()==2){//评论点赞
            Comment comment = commentMapper.selectById(userPraiseBrowse.getParentId());
            if (comment==null) return 204;
            comment.setPraiseNumber(comment.getPraiseNumber()+1);
            this.commentMapper.updateById(comment);
            userPraiseBrowse.setArticleId(comment.getArticleId());
        }else if (userPraiseBrowse.getType()==3){//文章浏览
            Article article = articleMapper.selectArticle(userPraiseBrowse.getParentId());
            if (article==null) return 203;
            article.setBrowseNumber(article.getBrowseNumber()+1);
            this.articleMapper.updateArticle(article);
            userPraiseBrowse.setArticleId(userPraiseBrowse.getParentId());
            //浏览量增加时增加佣金金币
            QueryWrapper<Config> configQueryWrapper = new QueryWrapper<>();
            configQueryWrapper.eq("code","browseGold");
            Integer browseGold = Integer.parseInt(configMapper.selectList(configQueryWrapper).get(0).getValue());//获取浏览量金币
            UserInfo userInfo = userInfoMapper.selectById(article.getUserId());
            userInfo.setGoldCoin(userInfo.getGoldCoin()+browseGold);
            this.userInfoMapper.updateById(userInfo);
        }else return 202;
        return userPraiseBrowseMapper.insert(userPraiseBrowse);
    }

    /**
     * 取消点赞
     * 201: 无该点赞
     * 202: 该点赞不存在
     * @param userId
     * @param type
     * @param parentId
     * @return
     */
    public Integer delUserPraiseBrowse(String userId,String type,String parentId){
        QueryWrapper<UserPraiseBrowse> userPraiseBrowseQueryWrapper = new QueryWrapper<>();
        Map<String,Object> userPraiseBrowseMap = new HashMap<>();
        userPraiseBrowseMap.put("user_id",userId);
        userPraiseBrowseMap.put("type",type);
        userPraiseBrowseMap.put("parent_id",parentId);
        userPraiseBrowseQueryWrapper.allEq(userPraiseBrowseMap,false);
        List<UserPraiseBrowse> userPraiseBrowseList = userPraiseBrowseMapper.selectList(userPraiseBrowseQueryWrapper);
        if(userPraiseBrowseList.size()<=0) return 202;
        UserPraiseBrowse userPraiseBrowse = userPraiseBrowseList.get(0);
        if (userPraiseBrowse.getType()==1){//取消文章点赞
            Article article = articleMapper.selectArticle(userPraiseBrowse.getParentId());
            article.setPraiseNumber(article.getPraiseNumber()-1);
            this.articleMapper.updateArticle(article);
        }else if (userPraiseBrowse.getType()==2){//取消评论点赞
            Comment comment = commentMapper.selectById(userPraiseBrowse.getParentId());
            comment.setPraiseNumber(comment.getPraiseNumber()-1);
            this.commentMapper.updateById(comment);
        }else if (userPraiseBrowse.getType()==3){//取消文章浏览
            return 201;
        }
        return userPraiseBrowseMapper.deleteById(userPraiseBrowse.getId());
    }

    /**
     * 查询评论，分页
     * @param map
     * @return
     */
    public PageInfo commentList(Integer pageNum, Map<String,String> map){
        Page page = new Page(pageNum,5);
        IPage<Map<String,Object>> comments = articleMapper.commentList(page,map);
        for (Map<String,Object> comment:comments.getRecords()){
            QueryWrapper<UserPraiseBrowse> userPraiseBrowseQueryWrapper = new QueryWrapper<>();
            Map<String,Object> userPraiseBrowseMap = new HashMap<>();
            userPraiseBrowseMap.put("article_id",comment.get("article_id"));
            userPraiseBrowseMap.put("parent_id",comment.get("id"));
            userPraiseBrowseMap.put("user_id",map.get("userId"));
            userPraiseBrowseMap.put("type",2);
            userPraiseBrowseQueryWrapper.allEq(userPraiseBrowseMap,false);
            List<UserPraiseBrowse> userPraiseBrowseList = userPraiseBrowseMapper.selectList(userPraiseBrowseQueryWrapper);
            if (userPraiseBrowseList.size()<=0) comment.put("praiseExist","false");
            else comment.put("praiseExist","true");
            if ("[]".equals(comment.get("sons"))) comment.put("sonExist","false");
            else comment.put("sonExist","true");
        }
        PageInfo pageInfo = new PageInfo(comments);
        return pageInfo;
    }

    /**
     * 根据文章id、用户id
     * 查询文章详情
     * @param articleId
     * @return
     *      browseExist: 当前用户是否浏览过
     *      praiseExist: 当前用户是否点赞
     */
    public Map<String,Object> selectArticleMap(String articleId,String userId){
        Map<String,Object> map = articleMapper.selectArticleMap(articleId);
        if (map==null) return null;
        QueryWrapper<UserPraiseBrowse> userPraiseBrowseQueryWrapper = new QueryWrapper<>();
        Map<String,Object> userPraiseBrowseMap = new HashMap<>();
        userPraiseBrowseMap.put("article_id",map.get("id"));
        userPraiseBrowseMap.put("parent_id",map.get("id"));
        userPraiseBrowseMap.put("user_id",userId);
        userPraiseBrowseMap.put("type",1);
        userPraiseBrowseQueryWrapper.allEq(userPraiseBrowseMap,false);
        List<UserPraiseBrowse> userPraiseBrowseList = userPraiseBrowseMapper.selectList(userPraiseBrowseQueryWrapper);
        if (userPraiseBrowseList.size()<=0) map.put("praiseExist","false");
        else map.put("praiseExist","true");
        if (!userId.equals(map.get("userId"))){
            QueryWrapper<UserPraiseBrowse> userPraiseBrowseQueryWrapper1 = new QueryWrapper<>();
            userPraiseBrowseMap.put("article_id",map.get("id"));
            userPraiseBrowseMap.put("parent_id",map.get("id"));
            userPraiseBrowseMap.put("user_id",userId);
            userPraiseBrowseMap.put("type",3);
            userPraiseBrowseQueryWrapper1.allEq(userPraiseBrowseMap,false);
            List<UserPraiseBrowse> userPraiseBrowseList1 = userPraiseBrowseMapper.selectList(userPraiseBrowseQueryWrapper1);
            if (userPraiseBrowseList1.size()<=0) map.put("browseExist","false");
            else map.put("browseExist","true");
        }else map.put("browseExist","true");
        return map;
    }

}
