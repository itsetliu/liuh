package com.cosmo.util;

import org.springframework.web.multipart.MultipartFile;
import sun.misc.BASE64Decoder;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

public class FileUtil {
    /**
     * 文件上传路径
     *///TODO 本地  服务器
//    private static final String filePath = "F:/img/cosmo/";
    private static final String filePath = "/root/img/cosmo/";

    /**
     * 单文件上传
     * @param file
     * @return
     */
    public static String upload(MultipartFile file){

        //file.isEmpty(); 判断图片是否为空
        //file.getSize(); 图片大小进行判断

        // 获取文件名
        String fileName = file.getOriginalFilename();

        // 获取文件的后缀名,比如图片的jpeg,png
        String suffixName = fileName.substring(fileName.lastIndexOf("."));

        // 文件上传后的路径
        fileName = UUID.randomUUID() + suffixName;

        File dest = new File(filePath + fileName);

        try {
            file.transferTo(dest);

            return fileName;
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return  null;

    }

    /**
     * 删除文件
     * @param fileName
     * @return
     */
    public static boolean delFile(String fileName){
        File file = new File(filePath+fileName);
        if (file.exists()){//文件是否存在
            file.delete();//删除文件
            return true;
        }
        return false;
    }

    /**
     * base64转MultipartFile
     * @param base64
     * @return
     */
    public static MultipartFile base64ToMultipart(String base64) {
        try {
            String[] baseStrs = base64.split(",");

            BASE64Decoder decoder = new BASE64Decoder();
            byte[] b = new byte[0];
            b = decoder.decodeBuffer(baseStrs[1]);

            for(int i = 0; i < b.length; ++i) {
                if (b[i] < 0) {
                    b[i] += 256;
                }
            }

            return new BASE64DecodedMultipartFile(b, baseStrs[0]);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
