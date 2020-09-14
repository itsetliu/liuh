package com.cosmo.entity;

import lombok.Data;

/**
 * @author ZhaoZiQu
 * @version 2020/6/29
 */
@Data
public class Config {
    private Long id;

    private String code;

    private String name;

    private String value;

    private Integer type;


}