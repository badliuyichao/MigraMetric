package com.migrametric.controller.ladder;

import com.migrametric.common.Result;
import com.migrametric.dto.ladder.UserCountLadderCreateDTO;
import com.migrametric.dto.ladder.UserCountLadderUpdateDTO;
import com.migrametric.service.ladder.UserCountLadderService;
import com.migrametric.vo.ladder.UserCountLadderVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 用户数阶梯控制器
 *
 * @author MigraMetric Team
 */
@Tag(name = "用户数阶梯管理", description = "用户数阶梯配置相关接口")
@RestController
@RequestMapping("/ladder/user-count")
@RequiredArgsConstructor
public class UserCountLadderController {

    private final UserCountLadderService ladderService;

    @Operation(summary = "获取所有阶梯")
    @GetMapping
    public Result<List<UserCountLadderVO>> listAll() {
        List<UserCountLadderVO> list = ladderService.listAll();
        return Result.success(list);
    }

    @Operation(summary = "获取阶梯详情")
    @GetMapping("/{id}")
    public Result<UserCountLadderVO> getById(
            @Parameter(description = "阶梯ID") @PathVariable Long id
    ) {
        UserCountLadderVO vo = ladderService.getById(id);
        return Result.success(vo);
    }

    @Operation(summary = "创建阶梯")
    @PostMapping
    public Result<Long> create(
            @Valid @RequestBody UserCountLadderCreateDTO createDTO
    ) {
        Long id = ladderService.create(createDTO);
        return Result.success(id);
    }

    @Operation(summary = "更新阶梯")
    @PutMapping("/{id}")
    public Result<Void> update(
            @Parameter(description = "阶梯ID") @PathVariable Long id,
            @Valid @RequestBody UserCountLadderUpdateDTO updateDTO
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

    @Operation(summary = "根据用户数匹配阶梯")
    @GetMapping("/match")
    public Result<UserCountLadderVO> matchByCount(
            @Parameter(description = "用户数") @RequestParam Integer count
    ) {
        UserCountLadderVO ladder = ladderService.matchByCount(count);
        return Result.success(ladder);
    }
}
