package com.atguigu.gulimall.thirdparty;

import com.aliyun.oss.OSSClient;
import com.atguigu.gulimall.thirdparty.component.SmsComponent;
import com.atguigu.gulimall.thirdparty.util.HttpUtils;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.HashMap;
import java.util.Map;

@SpringBootTest
class GulimallThirdPartyApplicationTests {

    @Autowired
    private OSSClient ossClient;

    @Autowired
    private SmsComponent smsComponent;

    @Test
    void contextLoads() {
    }

    @Test
    public void testUpload(){
    }

    @Test
    public void sendSmsComponentTest(){
        smsComponent.sendSmsCode("13883270441", "123777");
    }

    @Test
    public void sendSmsTest(){
        String host = "https://smsmsgs.market.alicloudapi.com";
        String path = "/sms/";
        String method = "GET";
        String appcode = "128a0abcd6f940d6aa84933197a1ca3b";
        Map<String, String> headers = new HashMap<String, String>();
        //�����header�еĸ�ʽ(�м���Ӣ�Ŀո�)ΪAuthorization:APPCODE 83359fd73fe94948385f570e3c139105
        headers.put("Authorization", "APPCODE " + appcode);
        Map<String, String> querys = new HashMap<String, String>();
        querys.put("code", "123321");
        querys.put("phone", "13883270441");
        querys.put("skin", "1");
        querys.put("sign", "175622");
        //JDK 1.8ʾ�����������������أ�  http://code.fegine.com/Tools.zip

        try {
            /**
             * ��Ҫ��ʾ����:
             * HttpUtils���
             * https://github.com/aliyun/api-gateway-demo-sign-java/blob/master/src/main/java/com/aliyun/api/gateway/demo/util/HttpUtils.java
             * ����ֱ�����أ�
             * http://code.fegine.com/HttpUtils.zip
             * ����
             *
             * ��Ӧ�����������
             * https://github.com/aliyun/api-gateway-demo-sign-java/blob/master/pom.xml
             * ���jar������pom��ֱ�����أ�
             * http://code.fegine.com/aliyun-jar.zip
             */
            HttpResponse response = HttpUtils.doGet(host, path, method, headers, querys);
            //System.out.println(response.toString());�粻���json, ������д��룬��ӡ����ͷ��״̬�롣
            //״̬��: 200 ������400 URL��Ч��401 appCode���� 403 �������ꣻ 500 API���ܴ���
            //��ȡresponse��body
            System.out.println(EntityUtils.toString(response.getEntity()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
