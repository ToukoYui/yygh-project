package com.atguigu.yygh.hosp.service;

import com.atguigu.yygh.model.hosp.Hospital;
import com.atguigu.yygh.vo.hosp.HospitalQueryVo;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Map;

public interface HospitalService {
    void save(Map<String, Object> paramMap);

    Hospital getHospByHoscode(String hoscode);

    Page<Hospital> selectHosPage(int page, int limit, HospitalQueryVo hospitalQueryVo);

    void updateHospStatus(String id, Integer status);

    Map<String,Object>  showHospDetail(String id);

    String getHosName(String hoscode);

    List<Hospital> findHospByHosname(String hosname);

    Map<String, Object> item(String hoscode);
}
