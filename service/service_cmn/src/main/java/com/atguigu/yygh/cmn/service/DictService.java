package com.atguigu.yygh.cmn.service;

import com.atguigu.yygh.model.cmn.Dict;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.util.List;


public interface DictService extends IService<Dict> {
    List<Dict> findChildData(Long id);

    boolean isChildren(Long id);

    void exportDictData(HttpServletResponse response);

    void importDictData(MultipartFile multipartFile);

    String getDictName(String dictCode, String value);

    List<Dict> getByDictCode(String dictCode);
}
