package com.yxp.chat.common.user.dao;

import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.yxp.chat.common.common.domain.enums.NormalOrNoEnum;
import com.yxp.chat.common.common.domain.enums.YesOrNoEnum;
import com.yxp.chat.common.common.domain.vo.req.CursorPageBaseReq;
import com.yxp.chat.common.common.domain.vo.resp.CursorPageBaseResp;
import com.yxp.chat.common.common.utils.CursorUtils;
import com.yxp.chat.common.user.domain.entity.User;
import com.yxp.chat.common.user.domain.enums.ChatActiveStatusEnum;
import com.yxp.chat.common.user.mapper.UserMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 用户表 服务实现类
 */
@Service
public class UserDao extends ServiceImpl<UserMapper, User>{

    public User getByOpenId(String openId) {
        return lambdaQuery()
                .eq(User::getOpenId, openId)
                .one();
    }

    public User getByName(String name) {
        return lambdaQuery()
                .eq(User::getName, name)
                .one();
    }

    public void modifyName(Long uid, String name) {
        lambdaUpdate()
                .eq(User::getId, uid)
                .set(User::getName, name)
                .update();
    }

    public void wearingBadge(Long uid, Long badgeId) {
        lambdaUpdate()
                .eq(User::getId, uid)
                .set(User::getItemId, badgeId)
                .update();
    }

    public void inValidUid(Long uid) {
        lambdaUpdate()
                .eq(User::getId, uid)
                .set(User::getStatus, YesOrNoEnum.YES.getStatus())
                .update();
    }

    public List<User> getFriendList(List<Long> uids) {
        return lambdaQuery()
                .in(User::getId, uids)
                .select(User::getId, User::getActiveStatus, User::getName, User::getAvatar)
                .list();
    }

    public CursorPageBaseResp<User> getCursorPage(List<Long> memberUidList, CursorPageBaseReq request, ChatActiveStatusEnum online) {
        return CursorUtils.getCursorPageByMysql(this, request, wrapper -> {
            wrapper.eq(User::getActiveStatus, online.getStatus());//筛选上线或者离线的
            wrapper.in(CollectionUtils.isNotEmpty(memberUidList), User::getId, memberUidList);//普通群对uid列表做限制
        }, User::getLastOptTime);
    }

    public List<User> getMemberList() {
        return lambdaQuery()
                .eq(User::getStatus, NormalOrNoEnum.NORMAL.getStatus())
                .orderByDesc(User::getLastOptTime)//最近活跃的1000个人，可以用lastOptTime字段，但是该字段没索引，updateTime可平替
                .last("limit 1000")//毕竟是大群聊，人数需要做个限制
                .select(User::getId, User::getName, User::getAvatar)
                .list();

    }
    public Integer getOnlineCount(List<Long> memberUidList) {
        return lambdaQuery()
                .eq(User::getActiveStatus, ChatActiveStatusEnum.ONLINE.getStatus())
                .in(CollectionUtil.isNotEmpty(memberUidList), User::getId, memberUidList)
                .count();
    }
}
