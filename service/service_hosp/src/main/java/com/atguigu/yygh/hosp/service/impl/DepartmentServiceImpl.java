package com.atguigu.yygh.hosp.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.yygh.hosp.repository.DepartmentRepository;
import com.atguigu.yygh.hosp.service.DepartmentService;
import com.atguigu.yygh.model.hosp.Department;
import com.atguigu.yygh.model.hosp.Hospital;
import com.atguigu.yygh.vo.hosp.DepartmentQueryVo;
import com.atguigu.yygh.vo.hosp.DepartmentVo;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

// 该接口用于医院接口模拟管理系统
@Service
public class DepartmentServiceImpl implements DepartmentService {
    @Autowired
    private DepartmentRepository departmentRepository;

    //上传科室接口
    @Override
    public void saveDepart(Map<String, Object> paramMap) {
        String jsonString = JSONObject.toJSONString(paramMap);
        Department department = JSONObject.parseObject(jsonString, Department.class);
        // 先查询mongo中有无该数据
        Department departmentExist =  departmentRepository
                .getDepartmentByHoscodeAndDepcode(department.getHoscode(),department.getDepcode());
        if (departmentExist == null){
            department.setUpdateTime(new Date());
            department.setCreateTime(new Date());
            department.setIsDeleted(0);
            departmentRepository.save(department);
        }else{
            departmentExist.setUpdateTime(new Date());
            departmentExist.setIsDeleted(0);
            departmentRepository.save(departmentExist);
        }

    }

    //查询医院科室接口
    @Override
    public Page<Department> queryPageDepart(int page, int limit, DepartmentQueryVo departmentQueryVo) {
        //转为DepartMen对象
        Department department = new Department();
        BeanUtils.copyProperties(departmentQueryVo,department);
        department.setIsDeleted(0);
        // 设置分页和匹配
        Pageable pageable = PageRequest.of(page,limit);
        ExampleMatcher matcher = ExampleMatcher.matching()    //创建一个匹配器
                .withStringMatcher(ExampleMatcher.StringMatcher.CONTAINING)   //模糊查询
                .withIgnoreCase(true); // 忽略大小写
        Example<Department> example = Example.of(department,matcher);
        //查询
        Page<Department> all = departmentRepository.findAll(example, pageable);
        return all;
    }

    // 删除医院科室
    @Override
    public void deleteDepart(String hoscode, String depcode) {
        Department object = departmentRepository.getDepartmentByHoscodeAndDepcode(hoscode, depcode);
        if (object != null){
            departmentRepository.delete(object);
        }
    }

    // 查询医院的所有科室，包含其下的子科室
    @Override
    public List<DepartmentVo> findDeptTree(String hoscode) {
        Department department = new Department();
        department.setHoscode(hoscode);
        Example example = Example.of(department);
        List<Department> deptList = departmentRepository.findAll(example);

        // 加工成DepartmentVo
        List<DepartmentVo> listVo = new ArrayList<>();
        // 将查询到的集合根据bigCode进行分组
        Map<String, List<Department>> collectByGroup =
                deptList.stream().collect(Collectors.groupingBy(Department::getBigcode));
        // 对分组后的集合进行遍历
        for (Map.Entry<String,List<Department>> entry : collectByGroup.entrySet()){
            String bigcode = entry.getKey();
            List<Department> departments = entry.getValue();
            // 由于分组后集合里对象的bigcode和bigname都是一样的，所以将其封装成一个大科室对象
            DepartmentVo departmentVo = new DepartmentVo();
            departmentVo.setDepcode(bigcode);
            departmentVo.setDepname(departments.get(0).getBigname());

            //封装小科室列表
            List<DepartmentVo> children = new ArrayList<>();
            for (Department department1 : departments){
                DepartmentVo son = new DepartmentVo();
                son.setDepcode(department1.getDepcode());
                son.setDepname(department1.getDepname());
                children.add(son);
            }

            departmentVo.setChildren(children);
            listVo.add(departmentVo);
        }
        return listVo;
    }

    @Override
    public String getDepName(String hoscode, String depcode) {
        Department department = departmentRepository.getDepartmentByHoscodeAndDepcode(hoscode, depcode);
        if (department!=null){
            return department.getDepname();
        }
        return null;
    }

    @Override

    public Department getDepartment(String hoscode, String depcode) {
        Department department = departmentRepository.getDepartmentByHoscodeAndDepcode(hoscode, depcode);
        return department;
    }
}
