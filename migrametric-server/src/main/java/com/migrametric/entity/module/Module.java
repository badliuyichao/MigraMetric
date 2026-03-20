package com.migrametric.entity.module;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 模块实体类
 *
 * @author MigraMetric Team
 */
@Data
@TableName("sys_module")
public class Module implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 模块ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 模块名称
     */
    private String moduleName;

    /**
     * 所属系统ID
     */
    private Long systemId;

    /**
     * 模块分类
     */
    private String category;

    /**
     * 基础工作量（人天）
     */
    private BigDecimal baseWorkload;

    /**
     * 默认加权系数
     */
    private BigDecimal defaultWeight;

    /**
     * 模块描述
     */
    private String description;

    /**
     * 状态：0-禁用，1-启用
     */
    private Integer status;

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
