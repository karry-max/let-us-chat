package com.yxp.chat.common.chat.dao;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yxp.chat.common.chat.domain.entity.RoomGroup;
import com.yxp.chat.common.chat.mapper.RoomGroupMapper;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 群聊房间表 服务实现类
 */
@Service
public class RoomGroupDao extends ServiceImpl<RoomGroupMapper, RoomGroup> {

    public List<RoomGroup> listByRoomIds(List<Long> roomIds) {
        return lambdaQuery()
                .in(RoomGroup::getRoomId, roomIds)
                .list();
    }

    public RoomGroup getByRoomId(Long roomId) {
        return lambdaQuery()
                .eq(RoomGroup::getRoomId, roomId)
                .one();
    }
}
