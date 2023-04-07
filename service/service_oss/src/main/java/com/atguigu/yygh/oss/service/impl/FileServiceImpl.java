package com.atguigu.yygh.oss.service.impl;

import com.atguigu.yygh.oss.service.FileService;
import com.atguigu.yygh.oss.utils.ConstantOssPropertiesUtils;
import com.qcloud.cos.COSClient;
import com.qcloud.cos.ClientConfig;
import com.qcloud.cos.auth.BasicCOSCredentials;
import com.qcloud.cos.auth.COSCredentials;
import com.qcloud.cos.http.HttpProtocol;
import com.qcloud.cos.model.ObjectMetadata;
import com.qcloud.cos.model.PutObjectRequest;
import com.qcloud.cos.model.PutObjectResult;
import com.qcloud.cos.region.Region;
import org.joda.time.DateTime;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.UUID;

@Service
public class FileServiceImpl implements FileService {
    private COSClient getCosClient(){
        // 1 初始化用户身份信息（secretId, secretKey）。
        // SECRETID和SECRETKEY请登录访问管理控制台 https://console.cloud.tencent.com/cam/capi 进行查看和管理
        String secretId = ConstantOssPropertiesUtils.SECRECT_ID;
        String secretKey = ConstantOssPropertiesUtils.SECRECT_KEY;
        COSCredentials cred = new BasicCOSCredentials(secretId, secretKey);
        // 2 设置 bucket 的地域, COS 地域的简称请参照 https://cloud.tencent.com/document/product/436/6224
        // clientConfig 中包含了设置 region, https(默认 http), 超时, 代理等 set 方法, 使用可参见源码或者常见问题 Java SDK 部分。
        Region region = new Region(ConstantOssPropertiesUtils.COS_REGION);
        ClientConfig clientConfig = new ClientConfig(region);
        // 这里建议设置使用 https 协议
        // 从 5.6.54 版本开始，默认使用了 https
        clientConfig.setHttpProtocol(HttpProtocol.https);
        // 3 生成 cos 客户端。
        COSClient cosClient = new COSClient(cred, clientConfig);
        return cosClient;
    }


    // 上传文件到腾讯云后返回该存储文件的url
    @Override
    public String uploadFile(MultipartFile multipartFile) {
        //设置上传到的路径
        String uuid = UUID.randomUUID().toString().replace("-","");
        String dateString = new DateTime().toString("yyyy/MM/dd");
        String key = "yygh/"+ dateString + "/" + uuid+ multipartFile.getOriginalFilename();
        //生成cos客户端
        COSClient cosClient = getCosClient();
        String bucketName = ConstantOssPropertiesUtils.BUCKET;
        try {
            InputStream inputStream = multipartFile.getInputStream();
            ObjectMetadata objectMetadata = new ObjectMetadata();
            objectMetadata.setContentLength(multipartFile.getSize());
            PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName,key,inputStream,objectMetadata);
            cosClient.putObject(putObjectRequest);
        } catch (IOException e) {
            e.printStackTrace();
        }
        URL objectUrl = cosClient.getObjectUrl(bucketName, key);
        cosClient.shutdown();
        return objectUrl.toString();
    }
}
