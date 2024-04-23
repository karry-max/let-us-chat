package com.yxp.chat.common.user.service.impl;


import com.yxp.chat.common.common.utils.AssertUtil;
import com.yxp.chat.common.user.service.OssService;
import com.yxp.chat.common.user.domain.enums.OssSceneEnum;
import com.yxp.chat.common.user.domain.vo.req.oss.UploadUrlReq;
import com.yxp.chat.oss.MinIOTemplate;
import com.yxp.chat.oss.domain.OssReq;
import com.yxp.chat.oss.domain.OssResp;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class OssServiceImpl implements OssService {
    @Autowired
    private MinIOTemplate minIOTemplate;

    @Override
    public OssResp getUploadUrl(Long uid, UploadUrlReq req) {
        OssSceneEnum sceneEnum = OssSceneEnum.of(req.getScene());
        AssertUtil.isNotEmpty(sceneEnum, "场景有误");
        OssReq ossReq = OssReq.builder()
                .fileName(req.getFileName())
                .filePath(sceneEnum.getPath())
                .uid(uid)
                .build();
        return minIOTemplate.getPreSignedObjectUrl(ossReq);
    }
}
