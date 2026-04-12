package com.migrametric.common;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 分页结果类
 *
 * @param <T> 数据类型
 * @author MigraMetric Team
 */
@Data
@NoArgsConstructor
public class PageResult<T> implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 数据列表
     */
    private List<T> records;

    /**
     * 总记录数
     */
    private Long total;

    /**
     * 当前页码
     */
    private Integer pageNum;

    /**
     * 每页大小
     */
    private Integer pageSize;

    /**
     * 总页数
     */
    private Integer totalPages;

    /**
     * 是否有上一页
     */
    private Boolean hasPrevious;

    /**
     * 是否有下一页
     */
    private Boolean hasNext;

    /**
     * 当前页的起始索引
     */
    private Integer fromIndex;

    /**
     * 当前页的结束索引
     */
    private Integer toIndex;

    /**
     * 构造函数
     *
     * @param records 数据列表
     * @param total   总记录数
     * @param pageNum 当前页码
     * @param pageSize 每页大小
     */
    public PageResult(List<T> records, Long total, Integer pageNum, Integer pageSize) {
        this.records = records;
        this.total = total;
        this.pageNum = pageNum;
        this.pageSize = pageSize;
        this.totalPages = calculateTotalPages(total, pageSize);
        this.hasPrevious = pageNum > 1;
        this.hasNext = pageNum < totalPages;
        this.fromIndex = (pageNum - 1) * pageSize + 1;
        this.toIndex = Math.min(pageNum * pageSize, total.intValue());
    }

    /**
     * 计算总页数
     */
    private Integer calculateTotalPages(Long total, Integer pageSize) {
        if (total == null || total == 0 || pageSize == null || pageSize == 0) {
            return 0;
        }
        return (int) Math.ceil((double) total / pageSize);
    }

    /**
     * 创建空的分页结果
     */
    public static <T> PageResult<T> empty(Integer pageNum, Integer pageSize) {
        return new PageResult<>(List.of(), 0L, pageNum, pageSize);
    }
}
