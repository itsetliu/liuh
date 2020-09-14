package com.cosmo.entity;

import lombok.Data;

/**
 * @author ZhaoZiQu
 * @version 2020/6/29
 */
@Data
public class Article {
    private Long id;
    private Long userId;
    private Integer browseNumber;
    private Integer praiseNumber;
    private String time;
    private Integer type;
    private String title;
    private String infoArray;
    private String imgArray;
    private String video;
    private Long classifyId;
}