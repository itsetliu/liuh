package com.cosmo.entity;

import lombok.Data;

/**
 * @author ZhaoZiQu
 * @version 2020/6/29
 */
@Data
public class Comment {
    private String id;

    private String userId;

    private String articleId;

    private String parentId;

    private String parents;

    private String sons;

    private Integer praiseNumber;

    private String time;

    private String info;

}