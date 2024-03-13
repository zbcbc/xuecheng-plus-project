package com.xuecheng.media;

import org.apache.commons.codec.digest.DigestUtils;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * ClassName: bigFileTest
 * Package: com.xuecheng.media
 * Description: 测试分块 合并 上传
 *
 * @Author zbc
 * @Create 2024/3/13 10:13
 * @Version 1.0
 */
public class bigFileTest {
    //测试文件分块方法
    @Test
    public void testChunk() throws IOException {
        //源文件
        File sourceFile = new File("D:\\test\\fileTest.mp4");

        String chunkPath = "D:\\test\\chunk\\";
        //分块文件夹
        File chunkFolder = new File(chunkPath);
        if(!chunkFolder.exists()){
            chunkFolder.mkdirs();
        }

        //分块大小
        long chunkSize = 1024 * 1024 * 1;
        //分块数量
        long chunkNum =(long) Math.ceil(sourceFile.length() * 1.0 / chunkSize);

        //使用RandomAccessFile 这个流可以读也可以写
        RandomAccessFile raf_r = new RandomAccessFile(sourceFile, "r");
        byte[] bytes = new byte[1024];

        //分块
        for (int i = 0; i < chunkNum; i++){

            File file = new File(chunkPath + i); //分块文件
            if(file.exists()){
                file.delete();
            }
            boolean newFile = file.createNewFile();

            if(newFile){
                RandomAccessFile raf_rw = new RandomAccessFile(file, "rw");
                int len = -1;
                while((len = raf_r.read(bytes)) != -1){
                    raf_rw.write(bytes, 0, len);
                    if(file.length() >= chunkSize){ //分块文件的大小达到了chunksize 不再读取
                        break;
                    }
                }
                //关闭流 继续for循环下次分块
                raf_rw.close();
            }
        }
    }

    @Test
    //测试合并方法
    public void testMerge() throws IOException {
        //源文件 用来合并之后比较MD5
        File sourceFile = new File("D:\\test\\fileTest.mp4");
        //块文件目录
        File chunkFolder = new File("D:\\test\\chunk\\");
        //合并后的文件
        File mergeFile = new File("D:\\test\\fileTest_2.mp4");

        //取出所有分块文件
        File[] chunkFiles = chunkFolder.listFiles();
        //排序
        List<File> chunkFileList = Arrays.asList(chunkFiles);
        Collections.sort(chunkFileList, new Comparator<File>() {
            @Override
            public int compare(File o1, File o2) {
                return Integer.parseInt(o1.getName()) -Integer.parseInt(o2.getName()); //升序排列
            }
        });

        //遍历分块文件，向合并文件写
        RandomAccessFile raf_rw = new RandomAccessFile(mergeFile, "rw");
        byte[] bytes = new byte[1024]; // 缓冲区
        for (File file : chunkFileList) {
            RandomAccessFile raf_r = new RandomAccessFile(file, "r");
            int len = -1;
            while((len = raf_r.read(bytes)) != -1){
                raf_rw.write(bytes, 0, len);
            }
            raf_r.close();

        }
        raf_rw.close();

        //合并完成 校验
        FileInputStream stream = new FileInputStream(sourceFile);
        FileInputStream stream1 = new FileInputStream(mergeFile);
        String md5source = DigestUtils.md5Hex(stream);
        String md5merge = DigestUtils.md5Hex(stream1);
        if(md5merge.equals(md5source)){
            System.out.println("合并成功");
        }

    }

}
