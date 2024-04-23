package com.yxp.chat.common.user.domain.vo.resp.ws;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Description:
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class WSLoginUrl {
    private String loginUrl;
}
