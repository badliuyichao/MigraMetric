package com.migrametric.dto.config;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 报表配置更新DTO
 *
 * @author MigraMetric Team
 */
@Data
@Schema(description = "报表配置更新DTO")
public class ReportConfigUpdateDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 配置ID
     */
    @NotBlank(message = "配置ID不能为空")
    @Schema(description = "配置ID")
    private Long id;

    /**
     * 配置键
     */
    @NotBlank(message = "配置键不能为空")
    @Size(max = 100, message = "配置键长度不能超过100个字符")
    @Schema(description = "配置键")
    private String configKey;

    /**
     * 配置值
     */
    @NotBlank(message = "配置值不能为空")
    @Size(max = 500, message = "配置值长度不能超过500个字符")
    @Schema(description = "配置值")
    private String configValue;
}
