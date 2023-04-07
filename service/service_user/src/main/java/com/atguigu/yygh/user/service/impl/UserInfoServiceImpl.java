package com.atguigu.yygh.user.service.impl;

import com.atguigu.yygh.common.exception.HospitalException;
import com.atguigu.yygh.common.helper.JwtHelper;
import com.atguigu.yygh.common.result.ResultCodeEnum;
import com.atguigu.yygh.enums.AuthStatusEnum;
import com.atguigu.yygh.model.user.Patient;
import com.atguigu.yygh.model.user.UserInfo;
import com.atguigu.yygh.user.mapper.UserInfoMapper;
import com.atguigu.yygh.user.service.PatientService;
import com.atguigu.yygh.user.service.UserInfoService;
import com.atguigu.yygh.vo.user.LoginVo;
import com.atguigu.yygh.vo.user.UserAuthVo;
import com.atguigu.yygh.vo.user.UserInfoQueryVo;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.print.DocFlavor;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class UserInfoServiceImpl extends ServiceImpl<UserInfoMapper, UserInfo> implements UserInfoService {
    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private PatientService patientService;
    // 验证用户输入的手机号和验证码
    @Override
    public Map<String, Object> queryUserInfo(LoginVo loginVo) {
        // 判断手机号和验证码是否为空
        String phone = loginVo.getPhone();
        String code = loginVo.getCode();
        if (StringUtils.isEmpty(phone) || StringUtils.isEmpty(code)) {
            throw new HospitalException(ResultCodeEnum.PARAM_ERROR);
        }

        // 检验用户传来的验证码是否正确
        String redisCode = stringRedisTemplate.opsForValue().get(phone);
        if (!redisCode.equals(code)) {
            throw new HospitalException(ResultCodeEnum.CODE_ERROR);
        }

        // 查询数据库时先查是否已经有微信登录的用户在数据库中
        //绑定手机号码
        UserInfo userInfo = null;
        if (!StringUtils.isEmpty(loginVo.getOpenid())) {
            userInfo = this.getByOpenid(loginVo.getOpenid());
            if (null != userInfo) {
                userInfo.setPhone(loginVo.getPhone());
                this.updateById(userInfo);
            } else {
                throw new HospitalException(ResultCodeEnum.DATA_ERROR);
            }
        }

        // 开始查询数据库的用户信息
        if (userInfo == null){
            LambdaQueryWrapper<UserInfo> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(UserInfo::getPhone, phone);
             userInfo = this.getOne(wrapper);
        }
        // 如果该手机号对应的用户不存在则注册,
        if (userInfo == null) {
            userInfo = new UserInfo();
            userInfo.setPhone(phone);
            userInfo.setStatus(1);
            userInfo.setName("");
            userInfo.setCreateTime(LocalDateTime.now());
            userInfo.setUpdateTime(LocalDateTime.now());
            this.save(userInfo);
        }
        // 用户手机号存在则进行登录(先检查用户有无被禁用)
        if (userInfo.getStatus() == 0) {
            throw new HospitalException(ResultCodeEnum.LOGIN_DISABLED_ERROR);
        }

        // TODO 记录登录


        // 如果用户的姓名为空，则用昵称当作姓名
        String name = userInfo.getName();
        if (StringUtils.isEmpty(name)) {
            name = userInfo.getNickName();
        }
        // 如果昵称也是空，则用手机号当姓名
        if (StringUtils.isEmpty(name)) {
            name = userInfo.getPhone();
        }
        Map<String, Object> resultMap = new HashMap<>();
        // 生成token
        String token = JwtHelper.createToken(userInfo.getId(), name);
        resultMap.put("token", token);
        resultMap.put("name", name);
        return resultMap;
    }

    @Override
    public UserInfo getByOpenid(String openid) {
        return this.getOne(new QueryWrapper<UserInfo>().eq("openid", openid));
    }

    // //用户认证接口
    @Override
    public void userAuth(Long userId, UserAuthVo userAuthVo) {
        UserInfo userInfo = baseMapper.selectById(userId);
        //认证人姓名
        userInfo.setName(userAuthVo.getName());
        //其他认证信息
        userInfo.setCertificatesType(userAuthVo.getCertificatesType());
        userInfo.setCertificatesNo(userAuthVo.getCertificatesNo());
        userInfo.setCertificatesUrl(userAuthVo.getCertificatesUrl());
        userInfo.setAuthStatus(AuthStatusEnum.AUTH_RUN.getStatus());
        this.baseMapper.updateById(userInfo);
    }

    //分页查询用户列表
    @Override
    public IPage<UserInfo> selectPage(Page<UserInfo> pageParam, UserInfoQueryVo userInfoQueryVo) {
        LambdaQueryWrapper<UserInfo> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.isNotEmpty(userInfoQueryVo.getKeyword()), UserInfo::getName,userInfoQueryVo.getKeyword())
                .like(userInfoQueryVo.getStatus()!=null, UserInfo::getStatus,userInfoQueryVo.getStatus())
                .like(userInfoQueryVo.getAuthStatus()!=null, UserInfo::getAuthStatus,userInfoQueryVo.getAuthStatus())
                .ge(StringUtils.isNotEmpty(userInfoQueryVo.getCreateTimeBegin()), UserInfo::getCreateTime,userInfoQueryVo.getCreateTimeBegin())
                .lt(StringUtils.isNotEmpty(userInfoQueryVo.getCreateTimeEnd()), UserInfo::getCreateTime,userInfoQueryVo.getCreateTimeEnd());
        Page<UserInfo> userInfoPage = baseMapper.selectPage(pageParam, wrapper);
        pageParam.getRecords().stream().forEach(item -> {
            this.packageUserInfo(item);
        });
        return pageParam;

    }

    private void packageUserInfo(UserInfo userInfo) {
        //处理认证状态编码
        userInfo.getParam().put("authStatusString",AuthStatusEnum.getStatusNameByStatus(userInfo.getAuthStatus()));
        //处理用户状态 0  1
        String statusString = userInfo.getStatus().intValue()==0 ?"锁定" : "正常";
        userInfo.getParam().put("statusString",statusString);
        return;
    }


    @Override
    public void lock(Long userId, Integer status) {
        if(status.intValue() == 0 || status.intValue() == 1) {
            UserInfo userInfo = this.getById(userId);
            userInfo.setStatus(status);
            this.updateById(userInfo);
        }
    }

    //显示用户详情
    @Override
    public Map<String, Object> show(Long userId) {
        Map<String,Object> map = new HashMap<>();
        //根据userid查询用户信息
        UserInfo userInfo = baseMapper.selectById(userId);
        this.packageUserInfo(userInfo);
        map.put("userInfo",userInfo);
        //根据userid查询就诊人信息
        List<Patient> patientList = patientService.getPatientList(userId);
        map.put("patientList",patientList);
        return map;
    }

    //认证审批  2通过  -1不通过
    @Override
    public void approval(Long userId, Integer authStatus) {
        if(authStatus.intValue()==2 || authStatus.intValue()==-1) {
            UserInfo userInfo = baseMapper.selectById(userId);
            userInfo.setAuthStatus(authStatus);
            baseMapper.updateById(userInfo);
        }
    }



}
