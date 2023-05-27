package com.atguigu.yygh.hosp.controller.api;

import com.atguigu.yygh.common.exception.HospitalException;
import com.atguigu.yygh.common.helper.HttpRequestHelper;
import com.atguigu.yygh.common.result.Result;
import com.atguigu.yygh.common.result.ResultCodeEnum;
import com.atguigu.yygh.common.util.MD5;
import com.atguigu.yygh.hosp.service.DepartmentService;
import com.atguigu.yygh.hosp.service.HospitalService;
import com.atguigu.yygh.hosp.service.HospitalSetService;
import com.atguigu.yygh.hosp.service.ScheduleService;
import com.atguigu.yygh.model.hosp.Department;
import com.atguigu.yygh.model.hosp.Hospital;
import com.atguigu.yygh.model.hosp.Schedule;
import com.atguigu.yygh.vo.hosp.DepartmentQueryVo;
import com.atguigu.yygh.vo.hosp.ScheduleQueryVo;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/hosp")
@Api(tags = "管理员后台操作接口")
public class ApiController {
    @Autowired
    private HospitalService hospitalService;
    @Autowired
    private DepartmentService departmentService;
    @Autowired
    private HospitalSetService hospitalSetService;
    @Autowired
    private ScheduleService scheduleService;

    // 判断前端传来的数据签名和mysql数据库中的签名是否一致
    private Map<String, Object> checkSignByMD5(HttpServletRequest request){
        Map<String, String[]> parameterMap = request.getParameterMap();
        Map<String, Object> stringObjectMap = HttpRequestHelper.switchMap(parameterMap);
        // 1.获取前端传递过来的加密签名
        String hospSign = (String) stringObjectMap.get("sign");
        // 2.1 通过传递过来的医院的编码，从数据库中查询出医院设置对象的签名
        String hoscode = (String) stringObjectMap.get("hoscode");
        String signKey = hospitalSetService.getSignKey(hoscode);
        // 2.2 加密签名
        String encrypt = MD5.encrypt(signKey);
        // 3.判断是否一致
        if(!encrypt.equals(hospSign)){
            throw new HospitalException(ResultCodeEnum.SIGN_ERROR);
        }
        return stringObjectMap;
    }

    /**
     * @Author: Touko
     * @Date: 2022/10/10 23:33
     * @Description: 删除排班
     **/
    @PostMapping("/schedule/remove")
    public Result deleteSchedule(HttpServletRequest request){
        Map<String, String[]> parameterMap = request.getParameterMap();
        Map<String, Object> paramMap = HttpRequestHelper.switchMap(parameterMap);
        // TODO 签名校验
        scheduleService.deleteSchedule((String)paramMap.get("hoscode"),(String)paramMap.get("hosScheduleId"));
        return Result.ok();
    }

    /**
     * @Author: Touko
     * @Date: 2022/10/10 19:16
     * @Description: 查询医院科室
     **/
    @PostMapping("/schedule/list")
    public Result querySchedule(HttpServletRequest request){
        Map<String, String[]> requestMap = request.getParameterMap();
        Map<String, Object> paramMap = HttpRequestHelper.switchMap(requestMap);
        int page = StringUtils.isBlank((String)paramMap.get("page"))? 1:Integer.parseInt((String) paramMap.get("page"));
        int limit = StringUtils.isBlank((String)paramMap.get("limit"))? 1:Integer.parseInt((String) paramMap.get("limit"));

        ScheduleQueryVo scheduleQueryVo = new ScheduleQueryVo();
        scheduleQueryVo.setHoscode((String) paramMap.get("hoscode"));
        scheduleQueryVo.setDepcode((String) paramMap.get("depcode"));


        Page<Schedule> pageModel = scheduleService.queryPageSchedule(page,limit,scheduleQueryVo);
        return Result.ok(pageModel);
    }

    /**
     * @Author: Touko 
     * @Date: 2022/10/10 23:05
     * @Description: 上传排班接口
     **/
    @ApiOperation(value = "上传排班接口")
    @PostMapping("/saveSchedule")
    public Result saveSchedule(HttpServletRequest request){
        Map<String, String[]> requestMap = request.getParameterMap();
        Map<String, Object> paramMap = HttpRequestHelper.switchMap(requestMap);
        // TODO 签名校验

        System.out.println("上传排班！！！");
        scheduleService.saveSchedule(paramMap);
        return Result.ok();
    }
    
    
    /**
     * @Author: Touko
     * @Date: 2022/10/10 20:24
     * @Description: 删除医院科室
     **/
    @ApiOperation(value = "删除医院科室")
    @PostMapping("/department/remove")
    public Result deleteDepart(HttpServletRequest request){
        Map<String, String[]> requestMap = request.getParameterMap();
        Map<String, Object> paramMap = HttpRequestHelper.switchMap(requestMap);
        String depcode = (String) paramMap.get("depcode");
        String hoscode = (String) paramMap.get("hoscode");
        departmentService.deleteDepart(hoscode,depcode);
        return Result.ok();
    }


    /**
     * @Author: Touko 
     * @Date: 2022/10/10 19:16
     * @Description: 查询医院科室
     **/
    @PostMapping("/department/list")
    public Result queryDepart(HttpServletRequest request){
        Map<String, String[]> requestMap = request.getParameterMap();
        Map<String, Object> paramMap = HttpRequestHelper.switchMap(requestMap);
        int page = StringUtils.isBlank((String)paramMap.get("page"))? 1:Integer.parseInt((String) paramMap.get("page"));
        int limit = StringUtils.isBlank((String)paramMap.get("limit"))? 1:Integer.parseInt((String) paramMap.get("limit"));

        DepartmentQueryVo departmentQueryVo = new DepartmentQueryVo();
        departmentQueryVo.setHoscode((String) paramMap.get("hoscode"));

        Page<Department> pageModel = departmentService.queryPageDepart(page,limit,departmentQueryVo);
        return Result.ok(pageModel);
    }
    
    
    /**
     * @Author: Touko
     * @Date: 2022/10/10 16:21
     * @Description: 新增医院的科室
     **/
    @ApiOperation(value = "新增科室")
    @PostMapping("/saveDepartment")
    public Result saveDepart(HttpServletRequest request){
        Map<String, Object> paramMap = checkSignByMD5(request);
        departmentService.saveDepart(paramMap);
        return Result.ok();
    }

    /**
     * @Author: Touko 
     * @Date: 2022/10/10 15:42
     * @Description: 查询医院
     **/
    @ApiOperation(value = "查询医院")
    @PostMapping("/hospital/show")
    public Result getHospital(HttpServletRequest request){
        Map<String, Object> paramMap = checkSignByMD5(request);
        String hoscode = (String) paramMap.get("hoscode");
        Hospital hospital = hospitalService.getHospByHoscode(hoscode);
        return Result.ok(hospital);
    }

    /**
     * @Author: Touko 
     * @Date: 2022/10/9 16:24
     * @Description: 上传医院接口
     **/
    @ApiOperation(value = "上传医院")
    @PostMapping("saveHospital")
    public Result saveHospital(HttpServletRequest request){
        Map<String, Object> paramMap = checkSignByMD5(request);
        String hoscode = (String) paramMap.get("hoscode");
        //4.图片传输过程中“+”转换为了“ ”，因此我们要转换回来
        String logoData = (String)paramMap.get("logoData");
        logoData = logoData.replaceAll(" ","+");
        paramMap.put("logoData",logoData);
        hospitalService.save(paramMap);
        return Result.ok();
    }
}
