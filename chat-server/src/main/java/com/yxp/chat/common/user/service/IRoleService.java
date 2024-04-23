package com.yxp.chat.common.user.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yxp.chat.common.user.domain.enums.RoleEnum;

/**
 * 角色表 服务类
 */
public interface IRoleService{
    /**
     * 是否有某个权限，临时做法
     *
     * @return
     */
    boolean hasPower(Long uid, RoleEnum roleEnum);
}
