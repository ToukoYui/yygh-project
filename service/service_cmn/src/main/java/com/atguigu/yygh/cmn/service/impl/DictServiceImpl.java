package com.atguigu.yygh.cmn.service.impl;

import com.alibaba.excel.EasyExcel;
import com.atguigu.yygh.cmn.listener.DictDataListener;
import com.atguigu.yygh.cmn.mapper.DictMapper;
import com.atguigu.yygh.cmn.service.DictService;
import com.atguigu.yygh.model.cmn.Dict;
import com.atguigu.yygh.vo.cmn.DictEeVo;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DictServiceImpl extends ServiceImpl<DictMapper, Dict> implements DictService {
    @Override
    @Cacheable(value = "dict", keyGenerator = "keyGenerator")
    //根据数据id查询子数据列表
    public List<Dict> findChildData(Long id) {
        LambdaQueryWrapper<Dict> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Dict::getParentId, id);
        List<Dict> list = this.list(wrapper);
        list.stream().map(dict -> {
            boolean isChildren = isChildren(dict.getId());
            dict.setHasChildren(isChildren);
            return dict;
        }).collect(Collectors.toList());
        return list;
    }

    @Override
    public boolean isChildren(Long id) {
        LambdaQueryWrapper<Dict> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Dict::getParentId, id);
        //获取符合条件的数据数目
        Integer count = baseMapper.selectCount(wrapper);
        return count > 0;
    }

    //导出字典接口
    @Override
    public void exportDictData(HttpServletResponse response) {
        try {
            response.setContentType("application/vnd.ms-excel");
            response.setCharacterEncoding("utf-8");
            // 这里URLEncoder.encode可以防止中文乱码 当然和easyexcel没有关系
            String fileName = URLEncoder.encode("数据字典", "UTF-8");
            response.setHeader("Content-disposition", "attachment;filename=" + fileName + ".xlsx");

            List<Dict> list = this.baseMapper.selectList(null);
            List<DictEeVo> voList = list.stream().map(dict -> {
                DictEeVo dictEeVo = new DictEeVo();
                BeanUtils.copyProperties(dict, dictEeVo);
                return dictEeVo;
            }).collect(Collectors.toList());

            EasyExcel.write(response.getOutputStream(), DictEeVo.class)
                    .sheet("dict").doWrite(voList);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //从Excel读取数据字典导入
    @Override
    @CacheEvict(value = "dict", allEntries = true)
    public void importDictData(MultipartFile multipartFile) {
        try {
            EasyExcel.read(multipartFile.getInputStream(), DictEeVo.class,
                    new DictDataListener(baseMapper)).sheet().doRead();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //在字典表中根据dictCode和Value获取字典名字name
    @Override
    public String getDictName(String dictCode, String value) {
        System.out.println(value);
        LambdaQueryWrapper<Dict> wrapper = new LambdaQueryWrapper<>();
        // 如果dictCode为空，说明该字典对象的value时唯一的，否则不唯一，需要根据dictCode查询其子对象
        if (StringUtils.isBlank(dictCode)){
            wrapper.eq(Dict::getValue,value);
            Dict dict = baseMapper.selectOne(wrapper);
            return dict.getName();
        }else{
            //通过dictCode获取该对象的id
            wrapper.eq(Dict::getDictCode,dictCode);
            Dict dict = baseMapper.selectOne(wrapper);
            Long id = dict.getId();
            // 通过id和value查询该对象的子对象
            Dict sonDict = baseMapper.selectOne(
                    new LambdaQueryWrapper<Dict>().eq(Dict::getParentId,id).eq(Dict::getValue,value)
            );
            return sonDict.getName();
        }
    }

    // 根据dictCode获取下级节点
    @Override
    public List<Dict> getByDictCode(String dictCode) {
        // 1.先根据dict_code得到父对象
        LambdaQueryWrapper<Dict> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Dict::getDictCode,dictCode);
        Dict dict = baseMapper.selectOne(wrapper);
        // 2.通过父对象的id作为parent_id搜索出其下的所有子对象
        Long id = dict.getId();
        List<Dict> childData = findChildData(id);
        return childData;
    }
}
