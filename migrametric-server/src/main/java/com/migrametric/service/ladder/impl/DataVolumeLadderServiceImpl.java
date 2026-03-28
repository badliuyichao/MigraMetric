package com.migrametric.service.ladder.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.migrametric.common.BusinessException;
import com.migrametric.common.ResultCode;
import com.migrametric.dto.ladder.DataVolumeLadderCreateDTO;
import com.migrametric.dto.ladder.DataVolumeLadderUpdateDTO;
import com.migrametric.entity.ladder.DataVolumeLadder;
import com.migrametric.mapper.ladder.DataVolumeLadderMapper;
import com.migrametric.service.ladder.DataVolumeLadderService;
import com.migrametric.vo.ladder.DataVolumeLadderVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import com.migrametric.context.UserContext;

/**
 * 数据量阶梯服务实现类
 *
 * @author MigraMetric Team
 */
@Service
@RequiredArgsConstructor
public class DataVolumeLadderServiceImpl implements DataVolumeLadderService {

    private final DataVolumeLadderMapper ladderMapper;

    @Override
    public List<DataVolumeLadderVO> listAll() {
        LambdaQueryWrapper<DataVolumeLadder> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByAsc(DataVolumeLadder::getSortOrder);

        List<DataVolumeLadder> list = ladderMapper.selectList(wrapper);

        return list.stream()
                .map(this::convertToVO)
                .toList();
    }

    @Override
    public DataVolumeLadderVO getById(Long id) {
        DataVolumeLadder ladder = ladderMapper.selectById(id);
        if (ladder == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "数据量阶梯不存在");
        }
        return convertToVO(ladder);
    }

    @Override
    @Transactional
    public Long create(DataVolumeLadderCreateDTO createDTO) {
        // 验证数据量范围
        validateVolumeRange(createDTO.getMinVolume(), createDTO.getMaxVolume());

        // 如果没有指定排序顺序，自动设置为最大+1
        Integer sortOrder = createDTO.getSortOrder();
        if (sortOrder == null) {
            Integer maxOrder = ladderMapper.selectMaxSortOrder();
            sortOrder = (maxOrder == null) ? 1 : maxOrder + 1;
        }

        // 创建实体
        DataVolumeLadder ladder = new DataVolumeLadder();
        ladder.setLadderName(createDTO.getLadderName());
        ladder.setMinVolume(createDTO.getMinVolume());
        ladder.setMaxVolume(createDTO.getMaxVolume());
        ladder.setWeight(createDTO.getWeight());
        ladder.setSortOrder(sortOrder);
        ladder.setCreateTime(LocalDateTime.now());
        ladder.setCreateBy(UserContext.getCurrentUsername());

        ladderMapper.insert(ladder);

        return ladder.getId();
    }

    @Override
    @Transactional
    public void update(DataVolumeLadderUpdateDTO updateDTO) {
        DataVolumeLadder ladder = ladderMapper.selectById(updateDTO.getId());
        if (ladder == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "数据量阶梯不存在");
        }

        // 验证数据量范围
        validateVolumeRange(updateDTO.getMinVolume(), updateDTO.getMaxVolume());

        // 更新实体
        ladder.setLadderName(updateDTO.getLadderName());
        ladder.setMinVolume(updateDTO.getMinVolume());
        ladder.setMaxVolume(updateDTO.getMaxVolume());
        ladder.setWeight(updateDTO.getWeight());
        ladder.setSortOrder(updateDTO.getSortOrder());
        ladder.setUpdateTime(LocalDateTime.now());
        ladder.setUpdateBy(UserContext.getCurrentUsername());

        ladderMapper.updateById(ladder);
    }

    @Override
    public void delete(Long id) {
        DataVolumeLadder ladder = ladderMapper.selectById(id);
        if (ladder == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "数据量阶梯不存在");
        }

        ladderMapper.deleteById(id);
    }

    @Override
    @Transactional
    public void moveUp(Long id) {
        DataVolumeLadder ladder = ladderMapper.selectById(id);
        if (ladder == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "数据量阶梯不存在");
        }

        Integer currentOrder = ladder.getSortOrder();
        if (currentOrder <= 1) {
            throw new BusinessException(ResultCode.PARAM_INVALID, "已经是第一个，无法上移");
        }

        // 查找上一个阶梯
        DataVolumeLadder prevLadder = ladderMapper.selectBySortOrder(currentOrder - 1);
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
        DataVolumeLadder ladder = ladderMapper.selectById(id);
        if (ladder == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "数据量阶梯不存在");
        }

        Integer currentOrder = ladder.getSortOrder();
        Integer maxOrder = ladderMapper.selectMaxSortOrder();

        if (currentOrder >= maxOrder) {
            throw new BusinessException(ResultCode.PARAM_INVALID, "已经是最后一个，无法下移");
        }

        // 查找下一个阶梯
        DataVolumeLadder nextLadder = ladderMapper.selectBySortOrder(currentOrder + 1);
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
    public DataVolumeLadderVO matchByVolume(BigDecimal volume) {
        if (volume == null) {
            return null;
        }

        List<DataVolumeLadderVO> allLadders = listAll();

        return allLadders.stream()
                .filter(ladder -> {
                    boolean aboveMin = volume.compareTo(ladder.getMinVolume()) >= 0;
                    boolean belowMax = ladder.getMaxVolume() == null ||
                                       volume.compareTo(ladder.getMaxVolume()) < 0;
                    return aboveMin && belowMax;
                })
                .findFirst()
                .orElse(null);
    }

    /**
     * 验证数据量范围
     */
    private void validateVolumeRange(BigDecimal minVolume, BigDecimal maxVolume) {
        if (maxVolume != null && minVolume.compareTo(maxVolume) >= 0) {
            throw new BusinessException(ResultCode.PARAM_INVALID, "数据量下限必须小于上限");
        }
    }

    /**
     * 转换为VO
     */
    private DataVolumeLadderVO convertToVO(DataVolumeLadder ladder) {
        DataVolumeLadderVO vo = new DataVolumeLadderVO();
        vo.setId(ladder.getId());
        vo.setLadderName(ladder.getLadderName());
        vo.setMinVolume(ladder.getMinVolume());
        vo.setMaxVolume(ladder.getMaxVolume());
        vo.setVolumeRangeText(buildVolumeRangeText(ladder.getMinVolume(), ladder.getMaxVolume()));
        vo.setWeight(ladder.getWeight());
        vo.setSortOrder(ladder.getSortOrder());
        vo.setCreateTime(ladder.getCreateTime());
        vo.setCreateBy(ladder.getCreateBy());
        return vo;
    }

    /**
     * 构建数据量范围文本
     */
    private String buildVolumeRangeText(BigDecimal minVolume, BigDecimal maxVolume) {
        if (minVolume == null) {
            return "";
        }
        String minText = minVolume.toPlainString();
        if (maxVolume == null) {
            return minText + "万条以上";
        }
        return minText + "万-" + maxVolume.toPlainString() + "万条";
    }
}
