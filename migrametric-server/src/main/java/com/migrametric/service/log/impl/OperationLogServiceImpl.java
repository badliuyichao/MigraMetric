package com.migrametric.service.log.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.migrametric.dto.log.OperationLogQueryDTO;
import com.migrametric.entity.log.OperationLog;
import com.migrametric.mapper.log.OperationLogMapper;
import com.migrametric.service.log.OperationLogService;
import com.migrametric.vo.log.OperationLogVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 操作日志服务实现类
 *
 * @author MigraMetric Team
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OperationLogServiceImpl implements OperationLogService {

    private final OperationLogMapper operationLogMapper;

    @Override
    public void saveLog(OperationLog operationLog) {
        log.debug("保存操作日志: 模块={}, 操作={}, 用户={}",
                operationLog.getModule(), operationLog.getOperationType(), operationLog.getUsername());
        operationLogMapper.insert(operationLog);
    }

    @Override
    public IPage<OperationLogVO> queryPage(OperationLogQueryDTO queryDTO) {
        Page<OperationLog> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());

        LambdaQueryWrapper<OperationLog> wrapper = new LambdaQueryWrapper<>();

        // 动态添加查询条件
        if (StringUtils.hasText(queryDTO.getModule())) {
            wrapper.eq(OperationLog::getModule, queryDTO.getModule());
        }
        if (StringUtils.hasText(queryDTO.getOperationType())) {
            wrapper.eq(OperationLog::getOperationType, queryDTO.getOperationType());
        }
        if (StringUtils.hasText(queryDTO.getUsername())) {
            wrapper.like(OperationLog::getUsername, queryDTO.getUsername());
        }
        if (queryDTO.getStatus() != null) {
            wrapper.eq(OperationLog::getStatus, queryDTO.getStatus());
        }
        if (StringUtils.hasText(queryDTO.getStartTime())) {
            wrapper.ge(OperationLog::getCreateTime,
                    LocalDateTime.parse(queryDTO.getStartTime() + " 00:00:00",
                            java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        }
        if (StringUtils.hasText(queryDTO.getEndTime())) {
            wrapper.le(OperationLog::getCreateTime,
                    LocalDateTime.parse(queryDTO.getEndTime() + " 23:59:59",
                            java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        }
        wrapper.orderByDesc(OperationLog::getCreateTime);

        IPage<OperationLog> resultPage = operationLogMapper.selectPage(page, wrapper);

        // 转换为VO
        Page<OperationLogVO> voPage = new Page<>(resultPage.getCurrent(), resultPage.getSize(), resultPage.getTotal());
        List<OperationLogVO> voList = resultPage.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
        voPage.setRecords(voList);

        return voPage;
    }

    @Override
    public OperationLogVO getById(Long id) {
        OperationLog operationLog = operationLogMapper.selectById(id);
        return operationLog != null ? convertToVO(operationLog) : null;
    }

    @Override
    public void clearAll() {
        operationLogMapper.delete(null);
        log.info("清空所有操作日志");
    }

    @Override
    public void deleteById(Long id) {
        operationLogMapper.deleteById(id);
        log.info("删除操作日志, id={}", id);
    }

    /**
     * 转换为VO
     */
    private OperationLogVO convertToVO(OperationLog operationLog) {
        OperationLogVO vo = new OperationLogVO();
        BeanUtils.copyProperties(operationLog, vo);
        return vo;
    }
}
