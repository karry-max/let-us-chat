package com.yxp;


import com.yxp.chat.common.ChatApplication;
import com.yxp.chat.oss.MinIOTemplate;
import com.yxp.chat.oss.domain.OssReq;
import com.yxp.chat.oss.domain.OssResp;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@SpringBootTest(classes = {ChatApplication.class})
@RunWith(SpringRunner.class)
public class OssTest {
    @Autowired
    private MinIOTemplate minIOTemplate;


    @Test
    public void getUploadUrl() {
        OssReq ossReq = OssReq.builder()
                .fileName("test.jpg")
                .filePath("test")
                .autoPath(false)
                .build();
        OssResp preSignedObjectUrl = minIOTemplate.getPreSignedObjectUrl(ossReq);
        System.out.println(preSignedObjectUrl);
    }
}
