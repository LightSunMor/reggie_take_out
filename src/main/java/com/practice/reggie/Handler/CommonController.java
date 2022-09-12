package com.practice.reggie.Handler;

import com.practice.reggie.model.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.UUID;

/*
* 文件上传
*  文件上传时，对页面的from表单有如下要求：1.method=”post“ 2.enctype="multipart/form-data" 采用multipart格式上传 3.type="file" 使用input的file控件上传
* 但这个上传，前端有相应的组件
* */
/*
* 文件下载（对于客户端而言是下载）
*  页面上发出请求，服务端再将文件通过输出写到客户端，说明白点，就是显示文件到服务端
* */

@RestController
@Slf4j
@RequestMapping("/common")
public class CommonController {

    //！！动态改变文件转存位置
    @Value("${reggie.img_path}")
    private String imgLoad_path;

    /**
     * 文件上传接口，返回转存后文件名
     * @param file
     * @return
     */
    @PostMapping("/upload") //这个请求路径是form表单action发出
    R<String> upload(MultipartFile file) //此处的参数名称要和前端的input的name值保持一致，就像拿到请求参数一样
    {
        //file是一个临时文件（在电脑上暂存一会儿），如果不进行转存，就会消失
        log.info("上传的文件对象"+file.toString());
        //1.上传文件的原始文件名,且为了防止文件重复，覆盖原文件，使用随机生成器生成文件名
        String originalFilename = file.getOriginalFilename();
        //使用UUID生成随机文件名
        String filename = UUID.randomUUID().toString();
        filename=filename+originalFilename.substring(originalFilename.lastIndexOf(".")); //subString方法 顾头不顾尾
        //2.因为imgLoad_path是动态获取的，所以该路径上的包是否存在，无法得知，需要进行一个判断
        File dir=new File(imgLoad_path);
        //判断是否存在
        if (!dir.exists())
        {
            dir.mkdirs();//mkdirs 加s 可以创建多级目录
        }
        try {
            //3.将临时文件file转存到的位置，转存为永久存在
            //建议位置改成灵活的，使用配置文件来改变
            file.transferTo(new File(imgLoad_path+"\\"+filename));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return R.success(filename);  //说到底，所有的返回客户端方式都通过response进行作用的
    }

    /**
     * 文件下载接口（img图片请求接口）
     * @param name
     * @param response
     */
    @GetMapping("/download")
    void download(String name, HttpServletResponse response)
    {
        try {
            //1.输入流，通过输入流读取文件内容
            FileInputStream fis=new FileInputStream(new File(imgLoad_path+"\\"+name));
            //2.输出流，将文件写回浏览器
            ServletOutputStream outputStream = response.getOutputStream();
            //设置相应数据格式（易遗忘）
            response.setContentType("image/jpeg");
            //开始读写
            int len=0;
            byte[] bytes=new byte[1024];
            while ((len=fis.read(bytes))!=-1)
            {
                outputStream.write(bytes,0,len);
                outputStream.flush();
            }
            fis.close();
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ;
    }

}
