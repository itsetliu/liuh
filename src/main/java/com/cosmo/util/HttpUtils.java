package com.cosmo.util;

import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.Socket;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class HttpUtils {
    //GET//
    /** * get请求 * * @param url 目标地址 * @param param 参数 * @return String 响应信息 */
    public static String sendGet(String url, String param) {

        String result = "";
        BufferedReader in = null;
        try {

            String urlNameString = url + "?" + param;
            URL realUrl = new URL(urlNameString);
            // 打开和URL之间的连接
            URLConnection connection = realUrl.openConnection();
            // 设置通用的请求属性
            connection.setRequestProperty("accept", "*/*");
            connection.setRequestProperty("connection", "Keep-Alive");
            connection.setRequestProperty("user-agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
            // 建立连接
            connection.connect();
            // 定义 BufferedReader读取URL的响应
            in = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8));
            String line;
            while ((line = in.readLine()) != null) {

                result += line;
            }
        } catch (Exception e) {

            log.error("GET请求异常{}" , e.getMessage());
        } finally {

            try {

                if (in != null) {

                    in.close();
                }
            } catch (Exception e2) {

                e2.getMessage();
            }
        }
        return result;
    }
////////////////////////////////POST///////////////////////////
    /** * 向指定 URL 发送POST方法的请求 * * @param url 发送请求的 URL * @param param 请求参数，json * @return 响应结果 */
    public static String sendPost(String url, String param) {

        OutputStreamWriter out = null;
        BufferedReader in = null;
        String result = "";

        //创建连接
        try {

            URL httpUrl = null; //HTTP URL类 用这个类来创建连接
            //创建URL
            httpUrl = new URL(url);
            //建立连接
            HttpURLConnection conn = (HttpURLConnection) httpUrl.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("connection", "keep-alive");
            conn.setUseCaches(false);//缓存关闭
            conn.setInstanceFollowRedirects(true);
            conn.setDoOutput(true);
            conn.setDoInput(true);
            conn.connect();
            //POST请求
            out = new OutputStreamWriter(conn.getOutputStream());
            out.write(param);
            out.flush();
            //读取响应
            in = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
            String line;
            while ((line = in.readLine()) != null) {

                result += line;
            }
            in.close();
            // 断开连接
            conn.disconnect();
        } catch (Exception e) {

            log.error("发送 POST 请求出现异常！{}" , e.getMessage());
        }
        //使用finally块来关闭输出流、输入流
        finally {

            try {

                if (out != null) {

                    out.close();
                }
                if (in != null) {

                    in.close();
                }
            } catch (IOException ex) {

                ex.getMessage();
            }
        }

        return result;
    }
///PUT/
    /** * put请求 * * @param url 目标地址 * @param map 参数 * @return String响应结果 */
    public static String sendPut(String url, Map map) {

        String encode = "utf-8";
        CloseableHttpClient closeableHttpClient = HttpClients.createDefault();
        HttpPut httpput = new HttpPut(url);
        httpput.setHeader("Accept", "*/*");
        httpput.setHeader("Accept-Encoding", "gzip, deflate");
        httpput.setHeader("Cache-Control", "no-cache");
        httpput.setHeader("Connection", "keep-alive");
        httpput.setHeader("Content-Type", "application/json;charset=UTF-8");
        //请求参数处理
        StringEntity stringEntity = null;
        try {
            stringEntity = new StringEntity(JSON.toJSONString(map), encode);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        httpput.setEntity(stringEntity);
        String content = null;
        CloseableHttpResponse httpResponse = null;
        try {

            //响应信息
            httpResponse = closeableHttpClient.execute(httpput);
            HttpEntity entity = httpResponse.getEntity();
            content = EntityUtils.toString(entity, encode);
        } catch (Exception e) {

            e.printStackTrace();
        } finally {

            try {

                httpResponse.close();
            } catch (IOException e) {

                e.getMessage();
            }
        }
        try {

            closeableHttpClient.close();  //关闭连接、释放资源
        } catch (IOException e) {

            e.getMessage();
        }
        return content;
    }
/////////////////////////////socket////////////////////////
    /** * socket发送数据 * * @param text 数据 * @param host 地址 * @param port 端口 */
    public static void forwardSocket(String text, String host, Integer port) {

        Socket socket = null;
        OutputStream outputStream = null;
        DataOutputStream dataOutputStream = null;
        try {

            socket = new Socket(host, port);
            outputStream = socket.getOutputStream();
            //把输出流封装在DataOutputStream中
            dataOutputStream = new DataOutputStream(outputStream);
            //使用writeUTF发送字符串
            dataOutputStream.writeUTF(text);
            dataOutputStream.close();
            socket.close();
        } catch (IOException e) {

            log.error("socket发送数据-失败{}", e.getMessage());
        } finally {

            try {

                if (dataOutputStream != null) {

                    dataOutputStream.close();
                }
                if (socket != null) {

                    socket.close();
                }
            } catch (Exception ex) {

                log.error("error to socket close :{}", ex.getMessage());
            }
        }
    }
}
