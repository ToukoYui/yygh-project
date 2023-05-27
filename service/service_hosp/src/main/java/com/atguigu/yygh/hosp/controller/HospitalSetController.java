package com.atguigu.yygh.hosp.controller;

import com.atguigu.yygh.common.exception.HospitalException;
import com.atguigu.yygh.common.result.Result;
import com.atguigu.yygh.common.util.MD5;
import com.atguigu.yygh.hosp.service.HospitalSetService;
import com.atguigu.yygh.model.hosp.Hospital;
import com.atguigu.yygh.model.hosp.HospitalSet;
import com.atguigu.yygh.vo.hosp.HospitalSetQueryVo;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Random;
/**
 * @Author: ToukoYui
 * @Date: 2023/5/1 20:01
 * @Description: 内部统筹的医院设置系统
 **/
@Api(tags = "医院设置管理")
@RestController
@RequestMapping("/admin/hosp/hospitalSet")
//@CrossOrigin //解决跨域问题
public class HospitalSetController {
    @Autowired
    private HospitalSetService hospitalSetService;

    /**
     * @Author: Touko
     * @Date: 2022/10/2 18:11
     * @Description: 获取全部医院设置信息
     **/
    @ApiOperation("获取全部医院设置信息")
    @GetMapping("/findAll")
    public Result findAllHospSet() {
        List<HospitalSet> list = hospitalSetService.list();
        return Result.ok(list);
    }

    /**
     * @Author: Touko
     * @Date: 2022/10/2 18:54
     * @Description: 根据id逻辑删除数据
     **/
    @ApiOperation("根据id逻辑删除数据")
    @DeleteMapping("{id}")
    public Result deleteById(@PathVariable Long id) {
        boolean flag = hospitalSetService.removeById(id);
        if (!flag) {
            return Result.fail();
        }
        return Result.ok();
    }

    /**
     * @Author: Touko
     * @Date: 2022/10/3 11:27
     * @Description: 分页&条件查询
     **/
    @ApiOperation("分页&条件查询")
    @PostMapping("findPageHospSet/{current}/{limit}")
    public Result getByPage(@PathVariable long current,
                            @PathVariable long limit,
                            @RequestBody(required = false) HospitalSetQueryVo hospitalSetQueryVo) {
        Page<HospitalSet> page = new Page<>(current, limit);
        LambdaQueryWrapper<HospitalSet> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.isNotBlank(hospitalSetQueryVo.getHosname()), HospitalSet::getHosname, hospitalSetQueryVo.getHosname());
        wrapper.eq(StringUtils.isNotBlank(hospitalSetQueryVo.getHoscode()), HospitalSet::getHoscode, hospitalSetQueryVo.getHoscode());
        Page<HospitalSet> hospitalSetPage = hospitalSetService.page(page, wrapper);
        return Result.ok(hospitalSetPage);
    }


    /**
     * @Author: Touko
     * @Date: 2022/10/3 12:13
     * @Description: 添加医院设置
     **/
    @ApiOperation("添加医院设置")
    @PostMapping("/saveHospitalSet")
    public Result insertHospSet(@RequestBody HospitalSet hospitalSet) {
        hospitalSet.setStatus(1);
        Random random = new Random();
        String signKey = MD5.encrypt(System.currentTimeMillis() + "" + random.nextInt(1000));
        hospitalSet.setSignKey(signKey);
        boolean save = hospitalSetService.save(hospitalSet);
        if (!save) {
            return Result.fail();
        }
        return Result.ok();
    }

    /**
     * @Author: Touko
     * @Date: 2022/10/3 12:14
     * @Description: 根据id获取医院设置
     **/
    @ApiOperation("根据id获取医院设置")
    @GetMapping("getHospSet/{id}")
    public Result getById(@PathVariable Long id) {
        try {
            boolean a = true;
        } catch (Exception e) {
            throw new HospitalException("失败", 201);
        }
        HospitalSet hospitalSet = hospitalSetService.getById(id);
        return Result.ok(hospitalSet);
    }

    /**
     * @Author: Touko
     * @Date: 2022/10/3 12:16
     * @Description: 修改医院设置
     **/
    @ApiOperation("修改医院设置")
    @PostMapping("updateHospitalSet")
    public Result updateByHosp(@RequestBody HospitalSet hospitalSet) {
        boolean flag = hospitalSetService.updateById(hospitalSet);
        if (!flag) {
            return Result.fail();
        }
        return Result.ok();
    }

    /**
     * @Author: Touko
     * @Date: 2022/10/3 14:42
     * @Description: 批量删除医院设置
     **/
    @ApiOperation("批量删除医院设置")
    @DeleteMapping("batchRemove")
    public Result batchRemove(@RequestBody List<Long> ids) {
        boolean flag = hospitalSetService.removeByIds(ids);
        if (!flag) {
            return Result.fail();
        }
        return Result.ok();
    }

    /**
     * @Author: Touko
     * @Date: 2022/10/3 14:42
     * @Description: 医院的开启与禁用
     **/
    @ApiOperation("医院的锁定和解锁")
    @PutMapping("/lockHospitalSet/{id}/{status}")
    public Result HospLockSet(@PathVariable Long id, @PathVariable int status) {
        HospitalSet hospitalSet = hospitalSetService.getById(id);
        hospitalSet.setStatus(status);
        boolean flag = hospitalSetService.updateById(hospitalSet);
        if (!flag) {
            return Result.fail();
        }
        return Result.ok();
    }


    //9 发送签名秘钥
    @PutMapping("sendKey/{id}")
    public Result lockHospitalSet(@PathVariable Long id) {
        HospitalSet hospitalSet = hospitalSetService.getById(id);
        String signKey = hospitalSet.getSignKey();
        String hoscode = hospitalSet.getHoscode();
        //TODO 发送短信
        return Result.ok();
    }
}
