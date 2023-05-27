package com.atguigu.yygh.cmn.controller;

import com.atguigu.yygh.cmn.service.DictService;
import com.atguigu.yygh.common.result.Result;
import com.atguigu.yygh.model.cmn.Dict;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@Api(tags = "数据字典接口")
@RestController
@RequestMapping("/admin/cmn/dict")
//@CrossOrigin
public class DictController {
    @Autowired
    private DictService dictService;
    /**
     * @Author: Touko
     * @Date: 2022/10/7 11:39
     * @Description: 根据数据id查询子数据列表
     **/
    @ApiOperation(value = "根据数据id查询子数据列表")
    @GetMapping("findChildData/{id}")
    public Result findChildData(@ApiParam(value = "用于测试") @PathVariable Long id) {
        List<Dict> list = dictService.findChildData(id);
        return Result.ok(list);
    }

    @ApiOperation("数据导出Excel")
    @GetMapping("/exportData")
    public void exportDict(HttpServletResponse response) {
        dictService.exportDictData(response);
    }

    @ApiOperation(value = "从Excel导入数据")
    @PostMapping("importData")
    public Result importData(MultipartFile file) {
        dictService.importDictData(file);
        return Result.ok();
    }

    /**
     * @Author: Touko
     * @Date: 2022/10/13 17:13
     * @Description: 在字典表中根据dictCode和Value获取name
     **/
    // 医院等级的value可能不唯一，所以还需要dictCode
    @ApiOperation(value = "在字典表中根据dictCode和Value获取name")
    @GetMapping("getName/{dictCode}/{value}")
    public String getName(@PathVariable String dictCode,@PathVariable String value){
        String dictName = dictService.getDictName(dictCode,value);
        return dictName;
    }

    /**
     * @Author: Touko
     * @Date: 2022/10/13 17:16
     * @Description: 在字典表中根据Value获取name
     **/
    @ApiOperation(value = "在字典表中根据Value获取name")
    @GetMapping("getName/{value}")
    public String getName(@PathVariable String value){
        String dictName = dictService.getDictName("",value);
        return dictName;
    }

    @ApiOperation(value = "根据dictCode获取下级节点")
    @GetMapping("/findByDictCode/{dictCode}")
    public Result getByDictCode(@PathVariable String dictCode){
        List<Dict> dictList = dictService.getByDictCode(dictCode);
        return Result.ok(dictList);
    }
}
