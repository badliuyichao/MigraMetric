package com.migrametric.service.ladder;

import com.migrametric.vo.ladder.LadderMatchResultVO;

import java.math.BigDecimal;

/**
 * 阶梯匹配服务接口
 * <p>
 * 提供统一的数据量和用户数阶梯匹配服务，供评估向导使用。
 * </p>
 *
 * @author MigraMetric Team
 */
public interface LadderMatchingService {

    /**
     * 根据数据量匹配阶梯
     * <p>
     * 匹配规则：minVolume <= 数据量 < maxVolume
     * 如果 maxVolume 为 null，表示无上限
     * </p>
     *
     * @param volume 数据量（万条）
     * @return 匹配的阶梯结果，如果未匹配返回null
     */
    LadderMatchResultVO matchDataVolume(BigDecimal volume);

    /**
     * 根据用户数匹配阶梯
     * <p>
     * 匹配规则：minCount <= 用户数 < maxCount
     * 如果 maxCount 为 null，表示无上限
     * </p>
     *
     * @param count 用户数量
     * @return 匹配的阶梯结果，如果未匹配返回null
     */
    LadderMatchResultVO matchUserCount(Integer count);

    /**
     * 获取数据量阶梯系数
     * <p>
     * 如果匹配到阶梯返回系数，否则返回null
     * </p>
     *
     * @param volume 数据量（万条）
     * @return 阶梯系数，如果未匹配返回null
     */
    BigDecimal getDataVolumeWeight(BigDecimal volume);

    /**
     * 获取用户数阶梯系数
     * <p>
     * 如果匹配到阶梯返回系数，否则返回null
     * </p>
     *
     * @param count 用户数量
     * @return 阶梯系数，如果未匹配返回null
     */
    BigDecimal getUserCountWeight(Integer count);
}
