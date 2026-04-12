package com.migrametric.entity.ladder;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 用户数阶梯实体类
 *
 * @author MigraMetric Team
 */
@Data
@TableName("sys_user_count_ladder")
public class UserCountLadder implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 阶梯ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 阶梯名称
     */
    private String ladderName;

    /**
     * 用户数下限
     */
    private Integer minCount;

    /**
     * 用户数上限，NULL表示无上限
     */
    private Integer maxCount;

    /**
     * 对应工作量系数
     */
    private BigDecimal weight;

    /**
     * 排序顺序
     */
    private Integer sortOrder;

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
