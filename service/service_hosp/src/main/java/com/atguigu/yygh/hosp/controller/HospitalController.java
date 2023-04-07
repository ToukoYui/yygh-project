package com.atguigu.yygh.hosp.controller;

import com.atguigu.yygh.common.result.Result;
import com.atguigu.yygh.hosp.service.HospitalService;
import com.atguigu.yygh.hosp.service.ScheduleService;
import com.atguigu.yygh.model.hosp.Hospital;
import com.atguigu.yygh.vo.hosp.HospitalQueryVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

// 医院信息存储在mongodb中
@RestController
//@CrossOrigin
@RequestMapping("/admin/hosp/hospital")
public class HospitalController {
    @Autowired
    private HospitalService hospitalService;
    @Autowired
    private ScheduleService scheduleService;

    // 查询所有医院列表
    @GetMapping("/list/{page}/{limit}")
    public Result listHosp(@PathVariable int page, @PathVariable int limit, HospitalQueryVo hospitalQueryVo){
        Page<Hospital> pageModel = hospitalService.selectHosPage(page,limit,hospitalQueryVo);
        return Result.ok(pageModel);
    }

    /**
     * @Author: Touko
     * @Date: 2022/10/15 16:32
     * @Description: 更新医院状态
     **/
    @ApiOperation(value = "更新医院上线状态")
    @PutMapping("/updateHospStatus")
    public Result updateHospStatus(@RequestParam("id") String id,@RequestParam("status") Integer status){
        hospitalService.updateHospStatus(id,status);
        return Result.ok();
    }

    /**
     * @Author: Touko
     * @Date: 2022/10/15 17:27
     * @Description: 查询医院详情信息
     **/
    @ApiOperation(value = "展示医院详情信息")
    @GetMapping("/showHospDetail/{id}")
    public Result showHospDetail(@PathVariable String id){
        Map<String,Object> resultMap = hospitalService.showHospDetail(id);
        return Result.ok(resultMap);
    }



}
