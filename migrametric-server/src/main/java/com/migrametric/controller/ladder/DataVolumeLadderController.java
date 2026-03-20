package com.migrametric.controller.ladder;

import com.migrametric.common.Result;
import com.migrametric.dto.ladder.DataVolumeLadderCreateDTO;
import com.migrametric.dto.ladder.DataVolumeLadderUpdateDTO;
import com.migrametric.service.ladder.DataVolumeLadderService;
import com.migrametric.vo.ladder.DataVolumeLadderVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * 数据量阶梯控制器
 *
 * @author MigraMetric Team
 */
@Tag(name = "数据量阶梯管理", description = "数据量阶梯配置相关接口")
@RestController
@RequestMapping("/ladder/data-volume")
@RequiredArgsConstructor
public class DataVolumeLadderController {

    private final DataVolumeLadderService ladderService;

    @Operation(summary = "获取所有阶梯")
    @GetMapping
    public Result<List<DataVolumeLadderVO>> listAll() {
        List<DataVolumeLadderVO> list = ladderService.listAll();
        return Result.success(list);
    }

    @Operation(summary = "获取阶梯详情")
    @GetMapping("/{id}")
    public Result<DataVolumeLadderVO> getById(
            @Parameter(description = "阶梯ID") @PathVariable Long id
    ) {
        DataVolumeLadderVO vo = ladderService.getById(id);
        return Result.success(vo);
    }

    @Operation(summary = "创建阶梯")
    @PostMapping
    public Result<Long> create(
            @Valid @RequestBody DataVolumeLadderCreateDTO createDTO
    ) {
        Long id = ladderService.create(createDTO);
        return Result.success(id);
    }

    @Operation(summary = "更新阶梯")
    @PutMapping("/{id}")
    public Result<Void> update(
            @Parameter(description = "阶梯ID") @PathVariable Long id,
            @Valid @RequestBody DataVolumeLadderUpdateDTO updateDTO
    ) {
        updateDTO.setId(id);
        ladderService.update(updateDTO);
        return Result.success();
    }

    @Operation(summary = "删除阶梯")
    @DeleteMapping("/{id}")
    public Result<Void> delete(
            @Parameter(description = "阶梯ID") @PathVariable Long id
    ) {
        ladderService.delete(id);
        return Result.success();
    }

    @Operation(summary = "上移动阶梯")
    @PatchMapping("/{id}/move-up")
    public Result<Void> moveUp(
            @Parameter(description = "阶梯ID") @PathVariable Long id
    ) {
        ladderService.moveUp(id);
        return Result.success();
    }

    @Operation(summary = "下移动阶梯")
    @PatchMapping("/{id}/move-down")
    public Result<Void> moveDown(
            @Parameter(description = "阶梯ID") @PathVariable Long id
    ) {
        ladderService.moveDown(id);
        return Result.success();
    }

    @Operation(summary = "根据数据量匹配阶梯")
    @GetMapping("/match")
    public Result<DataVolumeLadderVO> matchByVolume(
            @Parameter(description = "数据量（万条）") @RequestParam BigDecimal volume
    ) {
        DataVolumeLadderVO ladder = ladderService.matchByVolume(volume);
        return Result.success(ladder);
    }
}
