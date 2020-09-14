package com.cosmo.util;

import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.Data;

import java.util.List;

/**
 * @author ZhaoZiQu
 * @version 2020/9/9
 */
@Data
public class PageInfo <T> {
    private long pageNum;
    private long pageSize;
    private long total;
    private long pages;
    private List<T> list;

    public PageInfo(IPage iPage){
        pageNum = iPage.getCurrent();
        pageSize = iPage.getSize();
        total = iPage.getTotal();
        pages = iPage.getPages();
        list = iPage.getRecords();
    }
}
