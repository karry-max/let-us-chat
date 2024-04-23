package com.yxp.chat.common.user.service.impl;

import com.yxp.chat.common.user.service.cache.UserCache;
import com.yxp.chat.common.user.domain.enums.RoleEnum;
import com.yxp.chat.common.user.service.IRoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Set;

/**
 * Description: 角色管理
 */
@Service
public class RoleServiceImpl implements IRoleService {

    @Autowired
    private UserCache userCache;

    /**
     * 是否有某个权限
     * @param uid
     * @param roleEnum
     * @return
     */
    @Override
    public boolean hasPower(Long uid, RoleEnum roleEnum) {//超级管理员无敌的好吧，后期做成权限=》资源模式
        //1.获取用户的所有权限
        Set<Long> roleSet = userCache.getRoleSet(uid);
        //2.判断用户是否有这个权限 或者是否是超级管理员
        return isAdmin(roleSet) || roleSet.contains(roleEnum.getId());
    }

    private boolean isAdmin(Set<Long> roleSet) {
        return roleSet.contains(RoleEnum.ADMIN.getId());
    }

}
