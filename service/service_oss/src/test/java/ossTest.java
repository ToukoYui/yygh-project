import com.atguigu.yygh.oss.ServiceOssApplication;
import com.atguigu.yygh.oss.utils.ConstantOssPropertiesUtils;
import com.qcloud.cos.COSClient;
import com.qcloud.cos.ClientConfig;
import com.qcloud.cos.auth.BasicCOSCredentials;
import com.qcloud.cos.auth.COSCredentials;
import com.qcloud.cos.http.HttpProtocol;
import com.qcloud.cos.model.PutObjectRequest;
import com.qcloud.cos.model.PutObjectResult;
import com.qcloud.cos.region.Region;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.File;
import java.net.URL;

@SpringBootTest(classes = ServiceOssApplication.class)
@RunWith(SpringRunner.class)
public class ossTest {
    public COSClient getCosClient(){
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


    @Test
    public void save(){
        COSClient cosClient = getCosClient();
        // 指定要上传的文件
        String localFilePath = "C:\\Users\\不爱翻书的小脚\\OneDrive\\桌面\\微信图片_2021041323364.jpg";
        File localFile = new File(localFilePath);
        // 指定文件将要存放的存储桶
        String bucketName = ConstantOssPropertiesUtils.BUCKET;
        // 指定文件上传到 COS 上的路径，即对象键。例如对象键为folder/picture.jpg，则表示将文件 picture.jpg 上传到 folder 路径下
        String key = "folder/微信图片_2021041323364.jpg";
        PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName, key, localFile);
        PutObjectResult putObjectResult = cosClient.putObject(putObjectRequest);
        System.out.println("putObjectResult = " + putObjectResult);
    }

    @Test
    public void queryUrl(){
        COSClient cosClient = getCosClient();
        URL url =
                cosClient.getObjectUrl(ConstantOssPropertiesUtils.BUCKET, "微信图片_2021041323364.jpg");
        System.out.println("url = " + url);
        cosClient.shutdown();
    }
}
