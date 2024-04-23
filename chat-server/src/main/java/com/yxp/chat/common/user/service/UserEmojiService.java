package com.yxp.chat.common.user.service;



import com.yxp.chat.common.common.domain.vo.resp.ApiResult;
import com.yxp.chat.common.common.domain.vo.resp.IdRespVO;
import com.yxp.chat.common.user.domain.vo.req.UserEmojiReq;
import com.yxp.chat.common.user.domain.vo.resp.UserEmojiResp;

import java.util.List;

/**
 * 用户表情包 Service
 */
public interface UserEmojiService {

    /**
     * 表情包列表
     *
     * @return 表情包列表
     **/
    List<UserEmojiResp> list(Long uid);

    /**
     * 新增表情包
     *
     * @param emojis 用户表情包
     * @param uid    用户ID
     * @return 表情包
     **/
    ApiResult<IdRespVO> insert(UserEmojiReq emojis, Long uid);

    /**
     * 删除表情包
     *
     * @param id
     * @param uid
     */
    void remove(Long id, Long uid);
}
