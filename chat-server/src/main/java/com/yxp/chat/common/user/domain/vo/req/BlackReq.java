package com.yxp.chat.common.user.domain.vo.req;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;


/**
 * Description: 拉黑目标
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BlackReq {

    @NotNull
    @ApiModelProperty("拉黑目标uid")
    private Long uid;

}
