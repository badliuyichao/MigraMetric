package com.migrametric.entity.project;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 项目状态历史实体（REQ-§3.2.4）
 *
 * @author MigraMetric Team
 */
@Data
@TableName("proj_status_history")
public class ProjectStatusHistory implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long projectId;
    private String fromStatus;
    private String toStatus;
    private String event;
    private String operator;
    private String reason;
    private LocalDateTime changeTime;
    private Integer manualEdit;
}
