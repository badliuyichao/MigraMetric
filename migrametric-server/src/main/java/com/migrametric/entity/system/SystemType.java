package com.migrametric.entity.system;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 系统类型实体类
 *
 * @author MigraMetric Team
 */
@Data
@TableName("sys_system_type")
public class SystemType implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 系统类型ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 系统名称
     */
    private String systemName;

    /**
     * 系统类别：1-源系统，2-目标系统
     */
    private Integer systemCategory;

    /**
     * 描述信息
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
     * 创建人
     */
    private String createBy;

    /**
     * 更新人
     */
    private String updateBy;

    /**
     * 备注
     */
    private String remark;
}
