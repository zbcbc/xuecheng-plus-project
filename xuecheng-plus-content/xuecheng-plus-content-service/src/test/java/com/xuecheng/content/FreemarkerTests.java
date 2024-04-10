package com.xuecheng.content;

import com.xuecheng.content.model.dto.CoursePreviewDto;
import com.xuecheng.content.service.CoursePublishService;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

/**
 * ClassName: FreemarkerTests
 * Package: com.xuecheng.content
 * Description: 测试freemarker页面静态化方法
 *
 * @Author zbc
 * @Create 2024/3/28 16:13
 * @Version 1.0
 */
@SpringBootTest
public class FreemarkerTests {
    @Autowired
    CoursePublishService coursePublishService;


    @Test
    public void testFreemarker() throws IOException, TemplateException {
        Configuration configuration = new Configuration(Configuration.getVersion());
        //拿到classpath的路径
        String classpath = this.getClass().getResource("/").getPath();
        //指定模版的路径
        configuration.setDirectoryForTemplateLoading(new File(classpath + "/templates"));
        //指定编码
        configuration.setDefaultEncoding("utf-8");
        //得到模版
        Template template = configuration.getTemplate("course_template.ftl");

        //准备数据
        CoursePreviewDto coursePreviewInfo = coursePublishService.getCoursePreviewInfo(1L);
        //创建一个map 将模版中数据的前缀放进去
        HashMap<String, Object> map = new HashMap<>();
        map.put("model", coursePreviewInfo);

        String html = FreeMarkerTemplateUtils.processTemplateIntoString(template, map);
        System.out.println(html);
        //使用流将html写入文件
        //输入流
        InputStream inputStream = IOUtils.toInputStream(html, "utf-8");
        //输出流
        FileOutputStream fileOutputStream = new FileOutputStream("E:\\myCode\\xuechengOnline-project\\upload\\120.html");
        IOUtils.copy(inputStream, fileOutputStream);
    }
}
