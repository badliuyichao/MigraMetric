package com.migrametric.controller.ladder;

import com.migrametric.common.Result;
import com.migrametric.service.ladder.LadderMatchingService;
import com.migrametric.vo.ladder.LadderMatchResultVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

/**
 * 阶梯匹配控制器
 * <p>
 * 提供统一的阶梯匹配接口，供评估向导前端调用。
 * </p>
 *
 * @author MigraMetric Team
 */
@Tag(name = "阶梯匹配", description = "阶梯匹配接口，供评估向导使用")
@RestController
@RequestMapping("/api/ladder")
@RequiredArgsConstructor
public class LadderMatchingController {

    private final LadderMatchingService ladderMatchingService;

    /**
     * 根据数据量匹配阶梯
     */
    @Operation(summary = "数据量阶梯匹配", description = "根据数据量自动匹配阶梯，返回阶梯信息和系数")
    @GetMapping("/data-volume/match")
    public Result<LadderMatchResultVO> matchDataVolume(
            @Parameter(description = "数据量（万条）")
            @RequestParam(required = false) BigDecimal volume) {
        LadderMatchResultVO result = ladderMatchingService.matchDataVolume(volume);
        return Result.success(result);
    }

    /**
     * 根据用户数匹配阶梯
     */
    @Operation(summary = "用户数阶梯匹配", description = "根据用户数自动匹配阶梯，返回阶梯信息和系数")
    @GetMapping("/user-count/match")
    public Result<LadderMatchResultVO> matchUserCount(
            @Parameter(description = "用户数量")
            @RequestParam(required = false) Integer count) {
        LadderMatchResultVO result = ladderMatchingService.matchUserCount(count);
        return Result.success(result);
    }

    /**
     * 获取数据量阶梯系数
     */
    @Operation(summary = "获取数据量阶梯系数", description = "根据数据量获取匹配的阶梯系数")
    @GetMapping("/data-volume/weight")
    public Result<BigDecimal> getDataVolumeWeight(
            @Parameter(description = "数据量（万条）")
            @RequestParam(required = false) BigDecimal volume) {
        BigDecimal weight = ladderMatchingService.getDataVolumeWeight(volume);
        return Result.success(weight);
    }

    /**
     * 获取用户数阶梯系数
     */
    @Operation(summary = "获取用户数阶梯系数", description = "根据用户数获取匹配的阶梯系数")
    @GetMapping("/user-count/weight")
    public Result<BigDecimal> getUserCountWeight(
            @Parameter(description = "用户数量")
            @RequestParam(required = false) Integer count) {
        BigDecimal weight = ladderMatchingService.getUserCountWeight(count);
        return Result.success(weight);
    }
}
