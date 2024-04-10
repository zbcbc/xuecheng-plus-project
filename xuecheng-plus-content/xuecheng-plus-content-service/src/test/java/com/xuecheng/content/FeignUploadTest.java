package com.xuecheng.content;

import com.xuecheng.content.feignclient.MediaServiceClient;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * ClassName: FeignUploadTest
 * Package: com.xuecheng.content
 * Description: 测试远程调用媒资服务进行文件上传
 *
 * @Author zbc
 * @Create 2024/3/30 14:29
 * @Version 1.0
 */
@SpringBootTest
public class FeignUploadTest {
    @Autowired
    MediaServiceClient mediaServiceClient;

    @Test
    public void test(){
        //将file转成multipartFile

    }
}
