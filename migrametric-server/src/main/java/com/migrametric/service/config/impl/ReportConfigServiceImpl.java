package com.migrametric.service.config.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.migrametric.common.BusinessException;
import com.migrametric.common.ResultCode;
import com.migrametric.dto.config.ReportConfigUpdateDTO;
import com.migrametric.entity.config.ReportConfig;
import com.migrametric.mapper.config.ReportConfigMapper;
import com.migrametric.service.config.ReportConfigService;
import com.migrametric.vo.config.ReportConfigVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 报表配置服务实现类
 *
 * @author MigraMetric Team
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ReportConfigServiceImpl implements ReportConfigService {

    private final ReportConfigMapper configMapper;

    /**
     * 状态常量
     */
    private static final int STATUS_DISABLED = 0;   // 禁用
    private static final int STATUS_ENABLED = 1;   // 启用

    @Override
    public List<ReportConfigVO> listAll() {
        LambdaQueryWrapper<ReportConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByAsc(ReportConfig::getConfigKey);

        List<ReportConfig> list = configMapper.selectList(wrapper);

        return list.stream()
                .map(this::convertToVO)
                .toList();
    }

    @Override
    public String getValueByKey(String configKey) {
        if (!StringUtils.hasText(configKey)) {
            return null;
        }
        ReportConfig config = configMapper.selectByConfigKey(configKey);
        if (config == null || config.getStatus() != STATUS_ENABLED) {
            return null;
        }
        return config.getConfigValue();
    }

    @Override
    public BigDecimal getValueAsBigDecimal(String configKey, BigDecimal defaultValue) {
        String value = getValueByKey(configKey);
        if (!StringUtils.hasText(value)) {
            return defaultValue;
        }
        try {
            return new BigDecimal(value);
        } catch (NumberFormatException e) {
            log.warn("配置值转换失败: configKey={}, value={}", configKey, value);
            return defaultValue;
        }
    }

    @Override
    @Transactional
    public void update(ReportConfigUpdateDTO updateDTO) {
        ReportConfig config = configMapper.selectByConfigKey(updateDTO.getConfigKey());
        if (config == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "配置项不存在");
        }

        // 更新配置值
        config.setConfigValue(updateDTO.getConfigValue());
        config.setUpdateTime(LocalDateTime.now());
        config.setUpdateBy("admin"); // TODO: 从上下文获取

        configMapper.updateById(config);
    }

    /**
     * 转换为VO
     */
    private ReportConfigVO convertToVO(ReportConfig config) {
        ReportConfigVO vo = new ReportConfigVO();
        vo.setId(config.getId());
        vo.setConfigKey(config.getConfigKey());
        vo.setConfigValue(config.getConfigValue());
        vo.setConfigName(config.getConfigName());
        vo.setDescription(config.getDescription());
        vo.setStatus(config.getStatus());
        vo.setStatusText(getStatusText(config.getStatus()));
        vo.setCreateTime(config.getCreateTime());
        vo.setCreateBy(config.getCreateBy());
        return vo;
    }

    /**
     * 获取状态文本
     */
    private String getStatusText(Integer status) {
        if (status == null) {
            return "";
        }
        return switch (status) {
            case STATUS_ENABLED -> "启用";
            case STATUS_DISABLED -> "禁用";
            default -> "未知";
        };
    }
}
