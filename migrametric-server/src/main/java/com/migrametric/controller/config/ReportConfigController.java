package com.migrametric.controller.config;

import com.migrametric.common.Result;
import com.migrametric.dto.config.ReportConfigUpdateDTO;
import com.migrametric.service.config.ReportConfigService;
import com.migrametric.vo.config.ReportConfigVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * 报表配置控制器
 *
 * @author MigraMetric Team
 */
@Tag(name = "报表配置管理", description = "报表系统参数配置相关接口")
@RestController
@RequestMapping("/config/report")
@RequiredArgsConstructor
public class ReportConfigController {

    private final ReportConfigService configService;

    @Operation(summary = "获取所有配置")
    @GetMapping
    public Result<List<ReportConfigVO>> listAll() {
        List<ReportConfigVO> list = configService.listAll();
        return Result.success(list);
    }

    @Operation(summary = "根据配置键获取配置值")
    @GetMapping("/value")
    public Result<String> getValue(
            @Parameter(description = "配置键") @RequestParam String configKey
    ) {
        String value = configService.getValueByKey(configKey);
        return Result.success(value);
    }

    @Operation(summary = "根据配置键获取配置值（数值）")
    @GetMapping("/value/number")
    public Result<BigDecimal> getValueAsNumber(
            @Parameter(description = "配置键") @RequestParam String configKey,
            @Parameter(description = "默认值") @RequestParam(required = false) BigDecimal defaultValue
    ) {
        BigDecimal value = configService.getValueAsBigDecimal(configKey, defaultValue);
        return Result.success(value);
    }

    @Operation(summary = "更新配置")
    @PutMapping
    public Result<Void> update(
            @Valid @RequestBody ReportConfigUpdateDTO updateDTO
    ) {
        configService.update(updateDTO);
        return Result.success();
    }
}
