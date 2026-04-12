package com.migrametric.controller.log;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.migrametric.common.Result;
import com.migrametric.dto.log.OperationLogQueryDTO;
import com.migrametric.service.log.OperationLogService;
import com.migrametric.vo.log.OperationLogVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 操作日志Controller
 *
 * @author MigraMetric Team
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/logs/operation")
@Tag(name = "操作日志", description = "操作日志相关接口")
public class OperationLogController {

    private final OperationLogService operationLogService;

    /**
     * 分页查询操作日志
     */
    @GetMapping("/page")
    @Operation(summary = "分页查询操作日志", description = "支持多条件查询操作日志")
    public Result<IPage<OperationLogVO>> queryPage(
            @Parameter(description = "操作模块")
            @RequestParam(required = false) String module,
            @Parameter(description = "操作类型")
            @RequestParam(required = false) String operationType,
            @Parameter(description = "操作用户名")
            @RequestParam(required = false) String username,
            @Parameter(description = "操作状态：0-失败，1-成功")
            @RequestParam(required = false) Integer status,
            @Parameter(description = "开始时间")
            @RequestParam(required = false) String startTime,
            @Parameter(description = "结束时间")
            @RequestParam(required = false) String endTime,
            @Parameter(description = "页码")
            @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页数量")
            @RequestParam(defaultValue = "10") Integer pageSize) {

        log.info("分页查询操作日志请求, module={}, operationType={}, username={}", module, operationType, username);

        OperationLogQueryDTO queryDTO = new OperationLogQueryDTO();
        queryDTO.setModule(module);
        queryDTO.setOperationType(operationType);
        queryDTO.setUsername(username);
        queryDTO.setStatus(status);
        queryDTO.setStartTime(startTime);
        queryDTO.setEndTime(endTime);
        queryDTO.setPageNum(pageNum);
        queryDTO.setPageSize(pageSize);

        IPage<OperationLogVO> page = operationLogService.queryPage(queryDTO);
        return Result.success(page);
    }

    /**
     * 获取操作日志详情
     */
    @GetMapping("/{id}")
    @Operation(summary = "获取操作日志详情", description = "根据ID获取操作日志详情")
    public Result<OperationLogVO> getById(
            @Parameter(description = "日志ID", required = true)
            @PathVariable Long id) {

        log.info("获取操作日志详情, id={}", id);
        OperationLogVO vo = operationLogService.getById(id);
        return Result.success(vo);
    }

    /**
     * 清空所有操作日志
     */
    @DeleteMapping("/clear")
    @Operation(summary = "清空所有操作日志", description = "清空所有操作日志")
    public Result<Void> clearAll() {
        log.info("清空所有操作日志请求");
        operationLogService.clearAll();
        return Result.success();
    }

    /**
     * 删除指定操作日志
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除指定操作日志", description = "根据ID删除操作日志")
    public Result<Void> deleteById(
            @Parameter(description = "日志ID", required = true)
            @PathVariable Long id) {

        log.info("删除操作日志, id={}", id);
        operationLogService.deleteById(id);
        return Result.success();
    }
}
