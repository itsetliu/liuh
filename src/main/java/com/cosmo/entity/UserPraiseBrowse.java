package com.cosmo.entity;

import lombok.Data;

/**
 * @author ZhaoZiQu
 * @version 2020/6/29
 */
@Data
public class UserPraiseBrowse {
    private String id;

    private String userId;

    private Integer type;

    private String parentId;

    private String articleId;


}