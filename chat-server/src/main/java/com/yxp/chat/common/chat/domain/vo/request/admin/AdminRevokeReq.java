package com.yxp.chat.common.chat.domain.vo.request.admin;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;

/**
 * Description 撤销管理员请求信息
 */
@Data
public class AdminRevokeReq {
    @NotNull
    @ApiModelProperty("房间号")
    private Long roomId;

    @NotNull
    @Size(min = 1, max = 3)
    @ApiModelProperty("需要撤销管理的列表")
    private List<Long> uidList;
}
