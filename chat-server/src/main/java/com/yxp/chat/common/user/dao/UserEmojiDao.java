package com.yxp.chat.common.user.dao;


import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yxp.chat.common.user.domain.entity.UserEmoji;
import com.yxp.chat.common.user.mapper.UserEmojiMapper;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 用户表情包 服务实现类
 */
@Service
public class UserEmojiDao extends ServiceImpl<UserEmojiMapper, UserEmoji> {

    public List<UserEmoji> listByUid(Long uid) {
        return lambdaQuery().eq(UserEmoji::getUid, uid).list();
    }

    public int countByUid(Long uid) {
        return lambdaQuery().eq(UserEmoji::getUid, uid).count();
    }
}
