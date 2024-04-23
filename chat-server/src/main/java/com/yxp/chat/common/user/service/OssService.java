package com.yxp.chat.common.user.service;


import com.yxp.chat.common.user.domain.vo.req.oss.UploadUrlReq;
import com.yxp.chat.oss.domain.OssResp;

/**
 * oss 服务类
 */
public interface OssService {

    /**
     * 获取临时的上传链接
     */
    OssResp getUploadUrl(Long uid, UploadUrlReq req);
}
