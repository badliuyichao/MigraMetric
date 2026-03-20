package com.migrametric.entity.project;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 项目实体
 *
 * @author MigraMetric Team
 */
@Data
@TableName("proj_project")
public class Project implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 项目ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 项目名称
     */
    private String projectName;

    /**
     * 客户名称
     */
    private String customerName;

    /**
     * 源系统ID
     */
    private Long sourceSystemId;

    /**
     * 目标系统ID
     */
    private Long targetSystemId;

    /**
     * 项目负责人
     */
    private String projectLeader;

    /**
     * 联系方式
     */
    private String contact;

    /**
     * 项目描述
     */
    private String description;

    /**
     * 评估日期
     */
    private LocalDate evaluationDate;

    /**
     * 项目状态：DRAFT-草稿，IN_PROGRESS-进行中，COMPLETED-已完成，ARCHIVED-已归档
     */
    private String status;

    /**
     * 创建用户ID
     */
    private Long userId;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 创建者
     */
    private String createBy;

    /**
     * 更新者
     */
    private String updateBy;
}
