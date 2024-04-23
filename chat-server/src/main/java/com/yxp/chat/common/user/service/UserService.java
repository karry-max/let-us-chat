package com.yxp.chat.common.user.service;

import com.yxp.chat.common.user.domain.dto.ItemInfoDTO;
import com.yxp.chat.common.user.domain.dto.SummeryInfoDTO;
import com.yxp.chat.common.user.domain.entity.User;
import com.baomidou.mybatisplus.extension.service.IService;
import com.yxp.chat.common.user.domain.vo.req.*;
import com.yxp.chat.common.user.domain.vo.resp.BadgeResp;
import com.yxp.chat.common.user.domain.vo.resp.UserInfoResp;

import java.util.List;

/**
 * 用户表 服务类
 */
public interface UserService {

    Long register(User insert);

    /**
     * 用户详情
     * @param uid
     * @return
     */
    UserInfoResp getUserInfo(Long uid);

    /**
     * 修改用户名
     * @param uid
     * @param req
     */
    void modifyName(Long uid, ModifyNameReq req);

    /**
     * 查询可选徽章
     * @param uid
     * @return
     */
    List<BadgeResp> badges(Long uid);

    /**
     * 佩戴徽章
     * @param uid
     * @param req
     */
    void wearingBadge(Long uid, WearingBadgeReq req);

    /**
     * 拉黑用户
     * @param req
     */
    void black(BlackReq req);

    /**
     * 获取用户汇总信息
     *
     * @param req
     * @return
     */
    List<SummeryInfoDTO> getSummeryUserInfo(SummeryInfoReq req);

    List<ItemInfoDTO> getItemInfo(ItemInfoReq req);
}
