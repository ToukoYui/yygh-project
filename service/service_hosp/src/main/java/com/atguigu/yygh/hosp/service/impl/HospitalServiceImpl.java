package com.atguigu.yygh.hosp.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.yygh.cmn.client.DictFeignClient;
import com.atguigu.yygh.hosp.repository.HospitalRepository;
import com.atguigu.yygh.hosp.service.HospitalService;
import com.atguigu.yygh.model.hosp.Hospital;
import com.atguigu.yygh.vo.hosp.HospitalQueryVo;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

// 医院接口模拟管理系统部分
@Service
public class HospitalServiceImpl implements HospitalService {
    @Autowired
    private HospitalRepository hospitalRepository;

    @Autowired
    private DictFeignClient dictFeignClient;
    //
    @Override
    public void save(Map<String, Object> paramMap) {
        //1.将参数map转成json后再转成Hospital对象
        String jsonString = JSONObject.toJSONString(paramMap);
        Hospital object = JSONObject.parseObject(jsonString, Hospital.class);
        //2.判断是否存在数据
        String hoscode = object.getHoscode();
        Hospital isHospital = hospitalRepository.getHospitalByHoscode(hoscode);
        //3.1 存在则更新
        if (isHospital != null){
            object.setStatus(isHospital.getStatus());
            object.setCreateTime(isHospital.getCreateTime());
            object.setUpdateTime(new Date());
            object.setIsDeleted(0);
            hospitalRepository.save(object);
        }else{//3.2 不存在则添加
            object.setStatus(0);
            object.setCreateTime(new Date());
            object.setUpdateTime(new Date());
            object.setIsDeleted(0);
            hospitalRepository.save(object);
        }
    }

    @Override
    public Hospital getHospByHoscode(String hoscode) {
        Hospital hospital = hospitalRepository.getHospitalByHoscode(hoscode);
        return hospital;
    }

    // 医院列表条件查询
    @Override
    public Page<Hospital> selectHosPage(int page, int limit, HospitalQueryVo hospitalQueryVo) {
        Hospital hospital = new Hospital();
        BeanUtils.copyProperties(hospitalQueryVo,hospital);

        Pageable pageable = PageRequest.of(page-1,limit);
        ExampleMatcher matcher = ExampleMatcher
                .matching()
                .withStringMatcher(ExampleMatcher.StringMatcher.CONTAINING)
                .withIgnoreCase(true);
        Example example = Example.of(hospital,matcher);
        Page<Hospital> pages = hospitalRepository.findAll(example, pageable);
        /* 获取了医院基本信息外还需要医院的等级信息，
            该信息存于dict表中，查询服务在servie_cmn需要通过Nacos和Feign查询
         */
        pages.getContent().stream().forEach((item)->{
            this.setHospitalHosType(item);
        });
        return pages;
    }


    private Hospital setHospitalHosType(Hospital hospital){
        String hostypeString = dictFeignClient.getName("Hostype",hospital.getHostype());
        String provinceString = dictFeignClient.getName(hospital.getProvinceCode());
        String cityString = dictFeignClient.getName(hospital.getCityCode());
        String districtString = dictFeignClient.getName(hospital.getDistrictCode());
        hospital.getParam().put("fullAddress",provinceString+cityString+districtString);
        hospital.getParam().put("hostypeString",hostypeString);
        return hospital;
    }

    // 更新医院状态
    @Override
    public void updateHospStatus(String id, Integer status) {
        Hospital hospital = hospitalRepository.findById(id).get();
        hospital.setCreateTime(new Date());
        hospital.setStatus(status);
        hospitalRepository.save(hospital);
    }

    // 查询医院详情信息,得到对象后给对象添加更详细的内容
    Map<String,Object> getHospById(String id){
        Hospital hospital = this.setHospitalHosType(hospitalRepository.findById(id).get());
        Map<String,Object> resultMap = new HashMap<>();
        resultMap.put("hospital",hospital);
        // 单独处理更直观
        resultMap.put("bookingRule",hospital.getBookingRule());
        // 不需要重复返回，所以置为空
        hospital.setBookingRule(null);
        return resultMap;
    }
    // 展示医院详情信息
    @Override
    public Map<String,Object>  showHospDetail(String id) {
        Map<String,Object> resultMap = this.getHospById(id);
        return resultMap;
    }

    // 获取医院名称
    @Override
    public String getHosName(String hoscode) {
        Hospital hoscode1 = hospitalRepository.getHospitalByHoscode(hoscode);
        return hoscode1.getHosname();
    }

    // 根据医院名称模糊查询医院列表
    @Override
    public List<Hospital> findHospByHosname(String hosname) {
        List<Hospital> list = hospitalRepository.findHospitalByHosnameLike(hosname);
        return list;
    }


    // 根据hoscode查询医院的详情
    @Override
    public Map<String, Object> item(String hoscode) {
        Map<String, Object> result = new HashMap<>();
        //医院详情
        Hospital hospital = this.setHospitalHosType(this.getHospByHoscode(hoscode));
        result.put("hospital", hospital);
        //预约规则
        result.put("bookingRule", hospital.getBookingRule());
        //不需要重复返回
        hospital.setBookingRule(null);
        return result;
    }
}
