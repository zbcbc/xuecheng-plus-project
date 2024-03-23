package com.xuecheng.content.api;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;

/**
 * ClassName: FreemarkerController
 * Package: com.xuecheng.content.api
 * Description: freemarker入门程序
 *
 * @Author zbc
 * @Create 2024/3/23 13:33
 * @Version 1.0
 */
@Controller
public class FreemarkerController {
    @GetMapping("/testfreemarker")
    public ModelAndView test(){
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.addObject("name", "小明");
        modelAndView.setViewName("test"); //根据视图名称加.ftl找到模版
        return modelAndView;
    }
}
