package com.yxp.chat.common.chat.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yxp.chat.common.chat.domain.entity.Contact;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

/**
 * 会话列表 Mapper 接口
 */
public interface ContactMapper extends BaseMapper<Contact> {

    void refreshOrCreateActiveTime(@Param("roomId") Long roomId, @Param("memberUidList") List<Long> memberUidList, @Param("msgId") Long msgId, @Param("activeTime") Date activeTime);
}
