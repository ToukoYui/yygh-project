package com.atguigu.yygh.hosp.repository;

import com.atguigu.yygh.model.hosp.Hospital;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

//数据层
@Repository
public interface HospitalRepository extends MongoRepository<Hospital,String> {
    //内部已经的方法，只需要方法的命名符合规则就可以起作用
    Hospital getHospitalByHoscode(String hoscode);

    // 根据医院名称模糊查询医院列表
    List<Hospital> findHospitalByHosnameLike(String hosname);
}
