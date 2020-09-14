package com.cosmo.entity;

import lombok.Data;

/**
 * @author ZhaoZiQu
 * @version 2020/6/29
 */
@Data
public class Comment {
    private Long id;

    private Long userId;

    private Long articleId;

    private Long parentId;

    private String parents;

    private String sons;

    private Integer praiseNumber;

    private String time;

    private String info;

}