package com.cosmo.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cosmo.dao.CouponMapper;
import com.cosmo.entity.Coupon;
import org.springframework.stereotype.Service;

@Service
public class CouponService extends ServiceImpl<CouponMapper, Coupon> {
}
