package com.migrametric.service.evaluation;

import com.migrametric.dto.evaluation.EvaluationCreateDTO;
import com.migrametric.dto.evaluation.EvaluationUpdateDTO;
import com.migrametric.vo.evaluation.EvaluationVO;

/**
 * 评估服务接口
 *
 * @author MigraMetric Team
 */
public interface EvaluationService {

    /**
     * 根据项目ID获取评估详情
     *
     * @param projectId 项目ID
     * @return 评估VO
     */
    EvaluationVO getByProjectId(Long projectId);

    /**
     * 统计项目已选模块数量
     *
     * @param projectId 项目ID
     * @return 模块数量
     */
    int countSelectedModules(Long projectId);

    /**
     * 创建评估记录
     *
     * @param dto 创建DTO
     * @return 评估ID
     */
    Long createEvaluation(EvaluationCreateDTO dto);

    /**
     * 保存或更新评估指标
     *
     * @param projectId 项目ID
     * @param dto      指标DTO
     */
    void saveIndicators(Long projectId, EvaluationUpdateDTO dto);

    /**
     * 判断评估是否存在
     *
     * @param projectId 项目ID
     * @return 是否存在
     */
    boolean existsByProjectId(Long projectId);

    /**
     * 完成评估
     *
     * @param projectId 项目ID
     */
    void completeEvaluation(Long projectId);
}
