package com.yxp.chat.common.user.domain.vo.req;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


/**
 * Description: 表情包反参
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserEmojiReq {
    /**
     * 表情地址
     */
    @ApiModelProperty(value = "新增的表情url")
    private String expressionUrl;

}
