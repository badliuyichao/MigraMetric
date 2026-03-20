package com.migrametric.service.config;

import com.migrametric.dto.config.ReportConfigUpdateDTO;
import com.migrametric.vo.config.ReportConfigVO;

import java.math.BigDecimal;
import java.util.List;

/**
 * 报表配置服务接口
 *
 * @author MigraMetric Team
 */
public interface ReportConfigService {

    /**
     * 获取所有配置
     *
     * @return 配置列表
     */
    List<ReportConfigVO> listAll();

    /**
     * 根据配置键获取配置值
     *
     * @param configKey 配置键
     * @return 配置值
     */
    String getValueByKey(String configKey);

    /**
     * 根据配置键获取配置值（转换为BigDecimal）
     *
     * @param configKey 配置键
     * @param defaultValue 默认值
     * @return 配置值
     */
    BigDecimal getValueAsBigDecimal(String configKey, BigDecimal defaultValue);

    /**
     * 更新配置
     *
     * @param updateDTO 更新信息
     */
    void update(ReportConfigUpdateDTO updateDTO);
}
