package com.cosmo.entity.pack;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Menu {
    private Integer id;
    private String title;
    private String icon;
    private List<Sub> subs;
}
