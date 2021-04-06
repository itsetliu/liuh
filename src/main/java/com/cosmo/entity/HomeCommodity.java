package com.cosmo.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

import java.math.BigDecimal;

/**
 * @Author: Mr.liu
 * @Data : 2021/3/23 11:06
 */

@Data
public class HomeCommodity {

        private String id;

        private String model;

        private String thickness;

        private String width;

        private String length;

        @TableField("tubeWeight")
        private String tubeWeight;

        private String suttle;

        @TableField("roughWeight")
        private String roughWeight;

        private String price;

        private String pipe;

        private BigDecimal moeny;

        @TableField("boxVolume")
        private String boxVolume;


        @TableField("homeProductId")
        private String homeProductId;


        @TableField("cartonWeight")
        private String cartonWeight; //箱重

        @TableField("modelType")
        private String modelType;//型号类型0手用膜，1机用膜

        @TableField("modelProcessCost")
        private String modelProcessCost;//加工费

        @TableField("modelRawPrice")
        private String modelRawPrice;//原料价格

        @TableField("modelRawPriceType")
        private String modelRawPriceType;//0现售，1预售

        @TableField("cartonPrice")
        private String cartonPrice;//纸箱单价

        @TableField("typeOfMerchandise")
        private String typeOfMerchandise;

        @TableField("variety")
        private String variety;

        @TableField("offUniwafer")
        private String offUniwafer;

        @TableField("packingQuantity")
        private String packingQuantity;

        @TableField("commodityImage")
        private String commodityImage;
}

