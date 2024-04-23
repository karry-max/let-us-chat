package com.yxp.chat.common.user.service.adapter;

import cn.hutool.core.bean.BeanUtil;
import com.yxp.chat.common.common.domain.enums.YesOrNoEnum;
import com.yxp.chat.common.user.domain.entity.ItemConfig;
import com.yxp.chat.common.user.domain.entity.User;
import com.yxp.chat.common.user.domain.entity.UserBackpack;
import com.yxp.chat.common.user.domain.vo.resp.BadgeResp;
import com.yxp.chat.common.user.domain.vo.resp.UserInfoResp;
import me.chanjar.weixin.common.bean.WxOAuth2UserInfo;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class UserAdapter {
    public static User buildUserSave(String openId) {
        return User.builder()
                .openId(openId)
                .build();
    }

    public static User buildAuthorizeUser(Long uid, WxOAuth2UserInfo userInfo) {
        return User.builder()
                .id(uid)
                .name(userInfo.getNickname())
                .avatar(userInfo.getHeadImgUrl())
                .build();
    }

    public static UserInfoResp buildUserInfo(User userInfo, Integer countByValidItemId) {
        UserInfoResp userInfoResp = new UserInfoResp();
        BeanUtil.copyProperties(userInfo, userInfoResp);
        userInfoResp.setModifyNameChance(countByValidItemId);
        return userInfoResp;
    }


    public static List<BadgeResp> buildBadgeResp(List<ItemConfig> itemConfigs, List<UserBackpack> backpacks, User user) {
        Set<Long> obtainItemSet = backpacks.stream().map(UserBackpack::getItemId).collect(Collectors.toSet());

        return itemConfigs.stream().map(a -> {
            BadgeResp resp = new BadgeResp();
            BeanUtil.copyProperties(a, resp);
            //是否拥有
            resp.setObtain(obtainItemSet.contains(a.getId()) ? YesOrNoEnum.YES.getStatus() : YesOrNoEnum.NO.getStatus());
            //是否佩戴
            resp.setWearing(Objects.equals(a.getId(), user.getItemId()) ? YesOrNoEnum.YES.getStatus() : YesOrNoEnum.NO.getStatus());
            return resp;
            //按照佩戴和拥有排序
        }).sorted(Comparator.comparing(BadgeResp::getWearing, Comparator.reverseOrder())
            .thenComparing(BadgeResp::getObtain, Comparator.reverseOrder()))
                .collect(Collectors.toList());

    }
}
