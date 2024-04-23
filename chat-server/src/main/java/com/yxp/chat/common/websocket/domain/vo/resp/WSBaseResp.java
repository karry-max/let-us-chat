package com.yxp.chat.common.websocket.domain.vo.resp;

import com.yxp.chat.common.websocket.domain.enums.WSRespTypeEnum;
import lombok.Data;

@Data
public class WSBaseResp<T> {
    /**
     * @see WSRespTypeEnum
     */
    private Integer type;
    private T data;
}
