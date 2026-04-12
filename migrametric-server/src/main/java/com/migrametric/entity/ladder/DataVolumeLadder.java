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
 * 数据量阶梯实体类
 *
 * @author MigraMetric Team
 */
@Data
@TableName("sys_data_volume_ladder")
public class DataVolumeLadder implements Serializable {

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
     * 数据量下限（万条）
     */
    private BigDecimal minVolume;

    /**
     * 数据量上限（万条），NULL表示无上限
     */
    private BigDecimal maxVolume;

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
