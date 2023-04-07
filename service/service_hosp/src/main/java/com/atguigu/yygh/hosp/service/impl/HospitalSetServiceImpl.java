package com.atguigu.yygh.hosp.service.impl;

import com.atguigu.yygh.common.exception.HospitalException;
import com.atguigu.yygh.common.result.ResultCodeEnum;
import com.atguigu.yygh.hosp.mapper.HospitalSetMapper;
import com.atguigu.yygh.hosp.service.HospitalSetService;
import com.atguigu.yygh.model.hosp.HospitalSet;
import com.atguigu.yygh.vo.order.SignInfoVo;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

// 医院接口模拟管理系统部分
@Service
public class HospitalSetServiceImpl extends ServiceImpl<HospitalSetMapper, HospitalSet> implements HospitalSetService {

    /**
     * @Author: Touko 
     * @Date: 2022/10/9 21:03
     * @Description: 通过传递过来的医院的编码，查询出医院设置对象的签名
     **/
    @Override
    public String getSignKey(String hoscode) {
        LambdaQueryWrapper<HospitalSet> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(HospitalSet::getHoscode,hoscode);
        HospitalSet hospitalSet = baseMapper.selectOne(wrapper);
        return hospitalSet.getSignKey();
    }

    //获取医院签名信息
    @Override
    public SignInfoVo getSignInfoVo(String hoscode) {
        LambdaQueryWrapper<HospitalSet> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(HospitalSet::getHoscode,hoscode);
        HospitalSet hospitalSet = baseMapper.selectOne(wrapper);
        if(null == hospitalSet) {
            throw new HospitalException(ResultCodeEnum.HOSPITAL_OPEN);
        }
        SignInfoVo signInfoVo = new SignInfoVo();
        signInfoVo.setApiUrl(hospitalSet.getApiUrl());
        signInfoVo.setSignKey(hospitalSet.getSignKey());
        return signInfoVo;
    }

}
