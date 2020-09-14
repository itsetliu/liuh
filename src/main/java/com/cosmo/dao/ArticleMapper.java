package com.cosmo.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cosmo.entity.Article;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Param;

/**
 * @author ZhaoZiQu
 * @version 2020/6/29
 */
public interface ArticleMapper extends BaseMapper<Article> {
    int addArticle(Article article);
    Article selectArticle(Long articleId);
    Map<String,Object> selectArticleMap(Long articleId);
    int delArticle(Long articleId);
    IPage<Map<String,Object>> articleList(Page<Map<String,Object>> page, Article article);
    int updateArticle(Article article);
    IPage<Map<String,Object>> commentList(Page<Map<String,Object>> page, Map<String,String> map);
}