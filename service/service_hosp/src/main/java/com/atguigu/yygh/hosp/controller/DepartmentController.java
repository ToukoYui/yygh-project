package com.atguigu.yygh.hosp.controller;

import com.atguigu.yygh.common.result.Result;
import com.atguigu.yygh.hosp.service.DepartmentService;
import com.atguigu.yygh.vo.hosp.DepartmentVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
//@CrossOrigin
@RestController
@RequestMapping("/admin/hosp/department")
@Api(tags = "部门操作接口")
public class DepartmentController {
    @Autowired
    private DepartmentService departmentService;

    /**
     * @Author: Touko 
     * @Date: 2022/10/15 20:54
     * @Description: 查询医院的所有科室，包含其下的子科室
     **/
    @ApiOperation(value = "查询医院的所有科室")
    @GetMapping("/getDeptList/{hoscode}")
    public Result getDeptList(@PathVariable String hoscode){
       List<DepartmentVo> deptTree = departmentService.findDeptTree(hoscode);
       return Result.ok(deptTree);
    }
}
