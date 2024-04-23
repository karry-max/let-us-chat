package com.yxp.chat.common.user.service.impl;


import com.yxp.chat.common.common.annotation.RedissonLock;
import com.yxp.chat.common.common.domain.vo.resp.ApiResult;
import com.yxp.chat.common.common.domain.vo.resp.IdRespVO;
import com.yxp.chat.common.common.utils.AssertUtil;
import com.yxp.chat.common.user.dao.UserEmojiDao;
import com.yxp.chat.common.user.domain.entity.UserEmoji;
import com.yxp.chat.common.user.domain.vo.req.UserEmojiReq;
import com.yxp.chat.common.user.domain.vo.resp.UserEmojiResp;
import com.yxp.chat.common.user.service.UserEmojiService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户表情包 ServiceImpl
 */
@Service
@Slf4j
public class UserEmojiServiceImpl implements UserEmojiService {

    @Autowired
    private UserEmojiDao userEmojiDao;

    @Override
    public List<UserEmojiResp> list(Long uid) {
        return userEmojiDao.listByUid(uid).
                stream()
                .map(a -> UserEmojiResp.builder()
                        .id(a.getId())
                        .expressionUrl(a.getExpressionUrl())
                        .build())
                .collect(Collectors.toList());
    }

    /**
     * 新增表情包
     *
     * @param uid 用户ID
     * @return 表情包
     **/
    @Override
    @RedissonLock(key = "#uid")
    public ApiResult<IdRespVO> insert(UserEmojiReq req, Long uid) {
        //校验表情数量是否超过30
        int count = userEmojiDao.countByUid(uid);
        AssertUtil.isFalse(count > 30, "最多只能添加30个表情哦~~");
        //校验表情是否存在
        Integer existsCount = userEmojiDao.lambdaQuery()
                .eq(UserEmoji::getExpressionUrl, req.getExpressionUrl())
                .eq(UserEmoji::getUid, uid)
                .count();
        AssertUtil.isFalse(existsCount > 0, "当前表情已存在哦~~");
        UserEmoji insert = UserEmoji.builder().uid(uid).expressionUrl(req.getExpressionUrl()).build();
        userEmojiDao.save(insert);
        return ApiResult.success(IdRespVO.id(insert.getId()));
    }

    @Override
    public void remove(Long id, Long uid) {
        UserEmoji userEmoji = userEmojiDao.getById(id);
        AssertUtil.isNotEmpty(userEmoji, "表情不能为空");
        AssertUtil.equal(userEmoji.getUid(), uid, "小黑子，别人表情不是你能删的");
        userEmojiDao.removeById(id);
    }
}
