package com.migrametric.dto.system;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 系统类型创建DTO
 *
 * @author MigraMetric Team
 */
@Data
@Schema(description = "系统类型创建DTO")
public class SystemTypeCreateDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 系统名称
     */
    @NotBlank(message = "系统名称不能为空")
    @Size(max = 100, message = "系统名称长度不能超过100个字符")
    @Schema(description = "系统名称")
    private String systemName;

    /**
     * 系统类别：1-源系统，2-目标系统
     */
    @NotNull(message = "系统类别不能为空")
    @Schema(description = "系统类别：1-源系统，2-目标系统")
    private Integer systemCategory;

    /**
     * 描述信息
     */
    @Size(max = 500, message = "描述长度不能超过500个字符")
    @Schema(description = "描述信息")
    private String description;

    /**
     * 备注
     */
    @Size(max = 200, message = "备注长度不能超过200个字符")
    @Schema(description = "备注")
    private String remark;
}
