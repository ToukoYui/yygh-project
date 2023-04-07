package com.atguigu.yygh.oss.controller;

import com.atguigu.yygh.common.result.Result;
import com.atguigu.yygh.oss.service.FileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("api/oss/file")
public class FileApiController {
    @Autowired
    private FileService fileService;

    /**
     * @Author: Touko
     * @Date: 2022/10/25 14:25
     * @Description: 上传文件到腾讯云
     **/
    @PostMapping("fileUpload")
    public Result uploadFile(MultipartFile file){ //此处的参数名必须是file，因为前端ele-ui上传的文件字段名默认值是file
        String url = fileService.uploadFile(file);
        return Result.ok(url);
    }
}
