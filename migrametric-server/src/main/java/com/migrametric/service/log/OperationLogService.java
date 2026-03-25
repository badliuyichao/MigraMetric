package com.migrametric.service.log;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.migrametric.dto.log.OperationLogQueryDTO;
import com.migrametric.entity.log.OperationLog;
import com.migrametric.vo.log.OperationLogVO;

/**
 * 操作日志服务接口
 *
 * @author MigraMetric Team
 */
public interface OperationLogService {

    /**
     * 记录操作日志
     *
     * @param operationLog 日志实体
     */
    void saveLog(OperationLog operationLog);

    /**
     * 分页查询操作日志
     *
     * @param queryDTO 查询条件
     * @return 分页结果
     */
    IPage<OperationLogVO> queryPage(OperationLogQueryDTO queryDTO);

    /**
     * 获取日志详情
     *
     * @param id 日志ID
     * @return 日志详情
     */
    OperationLogVO getById(Long id);

    /**
     * 清空所有操作日志
     */
    void clearAll();

    /**
     * 删除指定日志
     *
     * @param id 日志ID
     */
    void deleteById(Long id);
}
