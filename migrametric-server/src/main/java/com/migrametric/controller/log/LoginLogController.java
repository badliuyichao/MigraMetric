package com.migrametric.controller.log;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.migrametric.common.Result;
import com.migrametric.dto.log.LoginLogQueryDTO;
import com.migrametric.service.log.LoginLogService;
import com.migrametric.vo.log.LoginLogVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 登录日志Controller
 *
 * @author MigraMetric Team
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/logs/login")
@Tag(name = "登录日志", description = "登录日志相关接口")
public class LoginLogController {

    private final LoginLogService loginLogService;

    /**
     * 分页查询登录日志
     */
    @GetMapping("/page")
    @Operation(summary = "分页查询登录日志", description = "支持多条件查询登录日志")
    public Result<IPage<LoginLogVO>> queryPage(
            @Parameter(description = "登录用户名")
            @RequestParam(required = false) String username,
            @Parameter(description = "登录状态：success-成功，fail-失败")
            @RequestParam(required = false) String status,
            @Parameter(description = "IP地址")
            @RequestParam(required = false) String ipAddress,
            @Parameter(description = "开始时间")
            @RequestParam(required = false) String startTime,
            @Parameter(description = "结束时间")
            @RequestParam(required = false) String endTime,
            @Parameter(description = "页码")
            @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页数量")
            @RequestParam(defaultValue = "10") Integer pageSize) {

        log.info("分页查询登录日志请求, username={}, status={}", username, status);

        LoginLogQueryDTO queryDTO = new LoginLogQueryDTO();
        queryDTO.setUsername(username);
        queryDTO.setStatus(status);
        queryDTO.setIpAddress(ipAddress);
        queryDTO.setStartTime(startTime);
        queryDTO.setEndTime(endTime);
        queryDTO.setPageNum(pageNum);
        queryDTO.setPageSize(pageSize);

        IPage<LoginLogVO> page = loginLogService.queryPage(queryDTO);
        return Result.success(page);
    }

    /**
     * 获取登录日志详情
     */
    @GetMapping("/{id}")
    @Operation(summary = "获取登录日志详情", description = "根据ID获取登录日志详情")
    public Result<LoginLogVO> getById(
            @Parameter(description = "日志ID", required = true)
            @PathVariable Long id) {

        log.info("获取登录日志详情, id={}", id);
        LoginLogVO vo = loginLogService.getById(id);
        return Result.success(vo);
    }

    /**
     * 获取用户最后登录信息
     */
    @GetMapping("/last/{username}")
    @Operation(summary = "获取用户最后登录信息", description = "获取用户最后登录日志")
    public Result<LoginLogVO> getLastLogin(
            @Parameter(description = "用户名", required = true)
            @PathVariable String username) {

        log.info("获取用户最后登录信息, username={}", username);
        LoginLogVO vo = loginLogService.getLastLogin(username);
        return Result.success(vo);
    }

    /**
     * 清空所有登录日志
     */
    @DeleteMapping("/clear")
    @Operation(summary = "清空所有登录日志", description = "清空所有登录日志")
    public Result<Void> clearAll() {
        log.info("清空所有登录日志请求");
        loginLogService.clearAll();
        return Result.success();
    }

    /**
     * 删除指定登录日志
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除指定登录日志", description = "根据ID删除登录日志")
    public Result<Void> deleteById(
            @Parameter(description = "日志ID", required = true)
            @PathVariable Long id) {

        log.info("删除登录日志, id={}", id);
        loginLogService.deleteById(id);
        return Result.success();
    }
}
