package com.migrametric.service.evaluation;

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
}
