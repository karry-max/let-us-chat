package com.yxp.chat.common.chat.dao;


import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yxp.chat.common.chat.domain.entity.WxMsg;
import com.yxp.chat.common.chat.mapper.WxMsgMapper;
import org.springframework.stereotype.Service;

/**
 * 微信消息表 服务实现类
 */
@Service
public class WxMsgDao extends ServiceImpl<WxMsgMapper, WxMsg> {

}
