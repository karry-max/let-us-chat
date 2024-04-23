package com.yxp.chat.common.websocket.domain.vo.req;

import com.yxp.chat.common.websocket.domain.enums.WSReqTypeEnum;
import lombok.Data;

@Data
public class WSBaseReq {
    /**
     * @see WSReqTypeEnum
     */
    private Integer type;
    private String data;
}
