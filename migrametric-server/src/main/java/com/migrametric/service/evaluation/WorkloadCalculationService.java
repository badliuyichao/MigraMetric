package com.migrametric.service.evaluation;

import com.migrametric.vo.evaluation.ModuleWorkloadVO;
import com.migrametric.vo.evaluation.WorkloadResultVO;

import java.math.BigDecimal;
import java.util.List;

/**
 * 工作量计算服务接口
 * <p>
 * 提供工作量计算的核心业务逻辑，包括：
 * - 核心迁移工作量计算
 * - 报表工作量计算
 * - 客开工作量计算
 * - 总工作量计算
 * - 工作量明细生成
 * </p>
 *
 * @author MigraMetric Team
 */
public interface WorkloadCalculationService {

    /**
     * 计算工作量
     * <p>
     * 根据项目ID和评估指标计算总工作量
     * </p>
     *
     * @param projectId 项目ID
     * @return 工作量计算结果
     */
    WorkloadResultVO calculateWorkload(Long projectId);

    /**
     * 计算模块工作量明细
     * <p>
     * 根据模块列表和数据量/用户数系数计算每个模块的工作量
     * </p>
     *
     * @param modules           模块列表（已选中的模块）
     * @param dataVolumeWeight  数据量系数
     * @param userCountWeight   用户数系数
     * @return 模块工作量明细列表
     */
    List<ModuleWorkloadVO> calculateModuleWorkloads(List<ModuleWorkloadVO> modules,
                                                     BigDecimal dataVolumeWeight,
                                                     BigDecimal userCountWeight);

    /**
     * 计算核心迁移工作量
     * <p>
     * 核心迁移工作量 = Σ(基础人天 × 加权系数 × 数据量系数 × 用户数系数)
     * </p>
     *
     * @param moduleWorkloads 模块工作量列表
     * @return 核心迁移工作量
     */
    BigDecimal calculateCoreWorkload(List<ModuleWorkloadVO> moduleWorkloads);

    /**
     * 计算报表工作量
     * <p>
     * 报表工作量 = 报表数量 × 报表系数
     * </p>
     *
     * @param reportCount 报表数量
     * @return 报表工作量
     */
    BigDecimal calculateReportWorkload(Integer reportCount);

    /**
     * 计算客开工作量
     * <p>
     * 客开工作量 = 客开人天（手动输入）
     * </p>
     *
     * @param hasCustomDev      是否有客开
     * @param customDevWorkload 客开人天
     * @return 客开工作量
     */
    BigDecimal calculateCustomDevWorkload(Boolean hasCustomDev, BigDecimal customDevWorkload);

    /**
     * 计算总工作量
     * <p>
     * 总工作量 = 核心迁移工作量 + 报表工作量 + 客开工作量
     * </p>
     *
     * @param coreWorkload    核心迁移工作量
     * @param reportWorkload  报表工作量
     * @param customDevWorkload 客开工作量
     * @return 总工作量
     */
    BigDecimal calculateTotalWorkload(BigDecimal coreWorkload,
                                      BigDecimal reportWorkload,
                                      BigDecimal customDevWorkload);
}
