package com.cosmo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.math.BigDecimal;

/**
 * @author ZhaoZiQu
 * @version 2020/9/21
 */
@Data
public class ModelCarton {
    private String id;
    private Integer length;
    private Integer width;
    private Integer height;
    private BigDecimal cartonPrice;
}
