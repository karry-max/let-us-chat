package com.yxp.chat.common.user.dao;

import com.yxp.chat.common.user.domain.entity.UserRole;
import com.yxp.chat.common.user.mapper.UserRoleMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 用户角色关系表 服务实现类
 */
@Service
public class UserRoleDao extends ServiceImpl<UserRoleMapper, UserRole>{

    public List<UserRole> listByUid(Long uid) {
        return lambdaQuery()
                .eq(UserRole::getUid, uid)
                .list();
    }
}
