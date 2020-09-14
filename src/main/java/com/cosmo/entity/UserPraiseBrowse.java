package com.cosmo.entity;

import lombok.Data;

/**
 * @author ZhaoZiQu
 * @version 2020/6/29
 */
@Data
public class UserPraiseBrowse {
    private Long id;

    private Long userId;

    private Integer type;

    private Long parentId;

    private Long articleId;


}