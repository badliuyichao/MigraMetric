package com.migrametric.service.ladder.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.migrametric.common.BusinessException;
import com.migrametric.common.ResultCode;
import com.migrametric.dto.ladder.UserCountLadderCreateDTO;
import com.migrametric.dto.ladder.UserCountLadderUpdateDTO;
import com.migrametric.entity.ladder.UserCountLadder;
import com.migrametric.mapper.ladder.UserCountLadderMapper;
import com.migrametric.service.ladder.UserCountLadderService;
import com.migrametric.vo.ladder.UserCountLadderVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import com.migrametric.context.UserContext;

/**
 * 用户数阶梯服务实现类
 *
 * @author MigraMetric Team
 */
@Service
@RequiredArgsConstructor
public class UserCountLadderServiceImpl implements UserCountLadderService {

    private final UserCountLadderMapper ladderMapper;

    @Override
    public List<UserCountLadderVO> listAll() {
        LambdaQueryWrapper<UserCountLadder> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByAsc(UserCountLadder::getSortOrder);

        List<UserCountLadder> list = ladderMapper.selectList(wrapper);

        return list.stream()
                .map(this::convertToVO)
                .toList();
    }

    @Override
    public UserCountLadderVO getById(Long id) {
        UserCountLadder ladder = ladderMapper.selectById(id);
        if (ladder == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "用户数阶梯不存在");
        }
        return convertToVO(ladder);
    }

    @Override
    @Transactional
    public Long create(UserCountLadderCreateDTO createDTO) {
        // 验证用户数范围
        validateCountRange(createDTO.getMinCount(), createDTO.getMaxCount());

        // 如果没有指定排序顺序，自动设置为最大+1
        Integer sortOrder = createDTO.getSortOrder();
        if (sortOrder == null) {
            Integer maxOrder = ladderMapper.selectMaxSortOrder();
            sortOrder = (maxOrder == null) ? 1 : maxOrder + 1;
        }

        // 创建实体
        UserCountLadder ladder = new UserCountLadder();
        ladder.setLadderName(createDTO.getLadderName());
        ladder.setMinCount(createDTO.getMinCount());
        ladder.setMaxCount(createDTO.getMaxCount());
        ladder.setWeight(createDTO.getWeight());
        ladder.setSortOrder(sortOrder);
        ladder.setCreateTime(LocalDateTime.now());
        ladder.setCreateBy(UserContext.getCurrentUsername());

        ladderMapper.insert(ladder);

        return ladder.getId();
    }

    @Override
    @Transactional
    public void update(UserCountLadderUpdateDTO updateDTO) {
        UserCountLadder ladder = ladderMapper.selectById(updateDTO.getId());
        if (ladder == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "用户数阶梯不存在");
        }

        // 验证用户数范围
        validateCountRange(updateDTO.getMinCount(), updateDTO.getMaxCount());

        // 更新实体
        ladder.setLadderName(updateDTO.getLadderName());
        ladder.setMinCount(updateDTO.getMinCount());
        ladder.setMaxCount(updateDTO.getMaxCount());
        ladder.setWeight(updateDTO.getWeight());
        ladder.setSortOrder(updateDTO.getSortOrder());
        ladder.setUpdateTime(LocalDateTime.now());
        ladder.setUpdateBy(UserContext.getCurrentUsername());

        ladderMapper.updateById(ladder);
    }

    @Override
    public void delete(Long id) {
        UserCountLadder ladder = ladderMapper.selectById(id);
        if (ladder == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "用户数阶梯不存在");
        }

        ladderMapper.deleteById(id);
    }

    @Override
    @Transactional
    public void moveUp(Long id) {
        UserCountLadder ladder = ladderMapper.selectById(id);
        if (ladder == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "用户数阶梯不存在");
        }

        Integer currentOrder = ladder.getSortOrder();
        if (currentOrder <= 1) {
            throw new BusinessException(ResultCode.PARAM_INVALID, "已经是第一个，无法上移");
        }

        // 查找上一个阶梯
        UserCountLadder prevLadder = ladderMapper.selectBySortOrder(currentOrder - 1);
        if (prevLadder == null) {
            throw new BusinessException(ResultCode.PARAM_INVALID, "上移失败");
        }

        // 交换排序顺序
        ladder.setSortOrder(currentOrder - 1);
        ladder.setUpdateTime(LocalDateTime.now());
        ladder.setUpdateBy(UserContext.getCurrentUsername());
        ladderMapper.updateById(ladder);

        prevLadder.setSortOrder(currentOrder);
        prevLadder.setUpdateTime(LocalDateTime.now());
        prevLadder.setUpdateBy(UserContext.getCurrentUsername());
        ladderMapper.updateById(prevLadder);
    }

    @Override
    @Transactional
    public void moveDown(Long id) {
        UserCountLadder ladder = ladderMapper.selectById(id);
        if (ladder == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "用户数阶梯不存在");
        }

        Integer currentOrder = ladder.getSortOrder();
        Integer maxOrder = ladderMapper.selectMaxSortOrder();

        if (currentOrder >= maxOrder) {
            throw new BusinessException(ResultCode.PARAM_INVALID, "已经是最后一个，无法下移");
        }

        // 查找下一个阶梯
        UserCountLadder nextLadder = ladderMapper.selectBySortOrder(currentOrder + 1);
        if (nextLadder == null) {
            throw new BusinessException(ResultCode.PARAM_INVALID, "下移失败");
        }

        // 交换排序顺序
        ladder.setSortOrder(currentOrder + 1);
        ladder.setUpdateTime(LocalDateTime.now());
        ladder.setUpdateBy(UserContext.getCurrentUsername());
        ladderMapper.updateById(ladder);

        nextLadder.setSortOrder(currentOrder);
        nextLadder.setUpdateTime(LocalDateTime.now());
        nextLadder.setUpdateBy(UserContext.getCurrentUsername());
        ladderMapper.updateById(nextLadder);
    }

    @Override
    public UserCountLadderVO matchByCount(Integer count) {
        if (count == null) {
            return null;
        }

        List<UserCountLadderVO> allLadders = listAll();

        return allLadders.stream()
                .filter(ladder -> {
                    boolean aboveMin = count >= ladder.getMinCount();
                    boolean belowMax = ladder.getMaxCount() == null ||
                                       count < ladder.getMaxCount();
                    return aboveMin && belowMax;
                })
                .findFirst()
                .orElse(null);
    }

    /**
     * 验证用户数范围
     */
    private void validateCountRange(Integer minCount, Integer maxCount) {
        if (maxCount != null && minCount >= maxCount) {
            throw new BusinessException(ResultCode.PARAM_INVALID, "用户数下限必须小于上限");
        }
    }

    /**
     * 转换为VO
     */
    private UserCountLadderVO convertToVO(UserCountLadder ladder) {
        UserCountLadderVO vo = new UserCountLadderVO();
        vo.setId(ladder.getId());
        vo.setLadderName(ladder.getLadderName());
        vo.setMinCount(ladder.getMinCount());
        vo.setMaxCount(ladder.getMaxCount());
        vo.setCountRangeText(buildCountRangeText(ladder.getMinCount(), ladder.getMaxCount()));
        vo.setWeight(ladder.getWeight());
        vo.setSortOrder(ladder.getSortOrder());
        vo.setCreateTime(ladder.getCreateTime());
        vo.setCreateBy(ladder.getCreateBy());
        return vo;
    }

    /**
     * 构建用户数范围文本
     */
    private String buildCountRangeText(Integer minCount, Integer maxCount) {
        if (minCount == null) {
            return "";
        }
        if (maxCount == null) {
            return minCount + "人以上";
        }
        return minCount + "人-" + maxCount + "人";
    }
}
