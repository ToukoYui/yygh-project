package com.atguigu.yygh.user.controller.api;

import com.atguigu.yygh.common.result.Result;
import com.atguigu.yygh.common.utils.AuthContextHolder;
import com.atguigu.yygh.model.user.Patient;
import com.atguigu.yygh.user.service.PatientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cglib.core.Local;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/user/patient")
public class PatientApiController {
    @Autowired
    private PatientService patientService;

    /**
     * @Author: Touko
     * @Date: 2022/10/28 10:50
     * @Description: 获取就诊人
     **/
    @GetMapping("auth/findAll")
    public Result findAllPatientByUserId(HttpServletRequest request){
        Long userId = AuthContextHolder.getUserId(request);
        System.out.println("userId = " + userId);
        List<Patient> patientList = patientService.getPatientList(userId);
        return Result.ok(patientList);
    }


    //添加就诊人
    @PostMapping("auth/save")
    public Result savePatient(@RequestBody Patient patient, HttpServletRequest request) {
        //获取当前登录用户id
        Long userId = AuthContextHolder.getUserId(request);
        patient.setUserId(userId);
        patient.setCreateTime(LocalDateTime.now());
        patient.setUpdateTime(LocalDateTime.now());
        patientService.save(patient);
        return Result.ok();
    }
    //根据id获取就诊人信息
    @GetMapping("auth/get/{id}")
    public Result getPatient(@PathVariable Long id) {
        Patient patient = patientService.getPatientId(id);
        return Result.ok(patient);
    }
    //修改就诊人
    @PostMapping("auth/update")
    public Result updatePatient(@RequestBody Patient patient) {
        patient.setUpdateTime(LocalDateTime.now());
        patientService.updateById(patient);
        return Result.ok();
    }
    //删除就诊人
    @DeleteMapping("auth/remove/{id}")
    public Result removePatient(@PathVariable Long id) {
        patientService.removeById(id);
        return Result.ok();
    }


    //根据就诊人id获取就诊人信息、
    @GetMapping("inner/get/{id}")
    public Patient getPatientById(@PathVariable Long id){
        Patient patient = patientService.getPatientId(id);
        return patient;
    }
}
