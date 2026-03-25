package com.migrametric.dto.evaluation;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 评估创建DTO
 *
 * @author MigraMetric Team
 */
@Data
@Schema(description = "评估创建DTO")
public class EvaluationCreateDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 项目ID
     */
    @NotNull(message = "项目ID不能为空")
    @Schema(description = "项目ID")
    private Long projectId;
}
