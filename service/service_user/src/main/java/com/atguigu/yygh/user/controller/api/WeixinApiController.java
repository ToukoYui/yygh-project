package com.atguigu.yygh.user.controller.api;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.yygh.common.helper.JwtHelper;
import com.atguigu.yygh.common.result.Result;
import com.atguigu.yygh.common.util.HttpUtil;
import com.atguigu.yygh.model.user.UserInfo;
import com.atguigu.yygh.user.service.UserInfoService;
import com.atguigu.yygh.user.utils.ConstantWXPropertiesUtils;
import com.atguigu.yygh.user.utils.HttpClientUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Controller
@RequestMapping("/api/ucenter/wx")
public class WeixinApiController {
    @Autowired
    private UserInfoService userInfoService;


    /**
     * 获取微信登录参数
     */
    @GetMapping("/getLoginParam")
    @ResponseBody
    public Result genQrConnect(HttpSession session) throws UnsupportedEncodingException {
        String redirectUri = URLEncoder.encode(ConstantWXPropertiesUtils.WX_OPEN_REDIRECT_URL, "UTF-8");
        Map<String, Object> map = new HashMap<>();
        map.put("appid", ConstantWXPropertiesUtils.WX_OPEN_APP_ID);
        map.put("redirectUri", redirectUri);
        map.put("scope", "snsapi_login");
        map.put("state", System.currentTimeMillis()+"");//System.currentTimeMillis()+""
        return Result.ok(map);
    }

    /**
     * @Author: Touko
     * @Date: 2022/10/24 17:51
     * @Description: 微信回调
     **/
    @GetMapping("/callback")
    public String callback(String code,String status) throws Exception {
        // 1.拿着微信id，密钥和微信服务器发送过来的code，请求微信固定地址，得到两个值：access_token和open_id
        StringBuffer baseAccessTokenUrl = new StringBuffer()
                .append("https://api.weixin.qq.com/sns/oauth2/access_token")
                .append("?appid=%s")
                .append("&secret=%s")
                .append("&code=%s")
                .append("&grant_type=authorization_code");

        String accessTokenUrl = String.format(baseAccessTokenUrl.toString(),
                ConstantWXPropertiesUtils.WX_OPEN_APP_ID,
                ConstantWXPropertiesUtils.WX_OPEN_APP_SECRET,
                code);

        String accessTokenInfo = "";

        //使用httpclient请求地址,获取access_token和open_id
        accessTokenInfo = HttpClientUtils.get(accessTokenUrl);

        JSONObject jsonObject = JSONObject.parseObject(accessTokenInfo);
        String accessToken = jsonObject.getString("access_token");
        String openId = jsonObject.getString("openid");
        log.info("access_token:{}",accessToken);
        log.info("open_id:{}",openId);


        // 判断数据库中是否已经存在该用户，根据openId查询
        UserInfo userInfo = userInfoService.getByOpenid(openId);
        if (userInfo == null) {
            // 2.使用access_token和open_id请求另一个微信api，得到扫描二维码的用户信息
            String baseUserInfoUrl = "https://api.weixin.qq.com/sns/userinfo" +
                    "?access_token=%s" +
                    "&openid=%s";
            String userInfoUrl = String.format(baseUserInfoUrl, accessToken, openId);
            log.info(userInfoUrl);

            String result = HttpClientUtils.get(userInfoUrl);
            JSONObject jsonObject1 = JSONObject.parseObject(result);
            String nickname = jsonObject1.getString("nickname");
            String headimgurl = jsonObject1.getString("headimgurl");
            log.info("nickname:{}",nickname);
            log.info("headimgurl:{}",headimgurl);

            //生成对象，添加进userInfo数据库
            userInfo = new UserInfo();
            userInfo.setOpenid(openId);
            userInfo.setNickName(nickname);
            userInfo.setStatus(1);
            userInfo.setCreateTime(LocalDateTime.now());
            userInfo.setUpdateTime(LocalDateTime.now());
            userInfoService.save(userInfo);
        }

        //封装map集合，目的是为了返回用户信息给前端
        Map<String, Object> map = new HashMap<>();
        String name = userInfo.getName();
        if(StringUtils.isEmpty(name)) {
            name = userInfo.getNickName();
        }
        if(StringUtils.isEmpty(name)) {
            name = userInfo.getPhone();
        }
        map.put("name", name);
        if(StringUtils.isEmpty(userInfo.getPhone())) {
            map.put("openid", userInfo.getOpenid());
        } else {
            map.put("openid", "");
        }
        String token = JwtHelper.createToken(userInfo.getId(), name);
        map.put("token", token);

        return "redirect:" + ConstantWXPropertiesUtils.YYGH_BASE_URL +
                "/weixin/callback?token="+map.get("token")+"&openid="+
                map.get("openid")+"&name="+URLEncoder.encode((String)map.get("name"));
    }

}
