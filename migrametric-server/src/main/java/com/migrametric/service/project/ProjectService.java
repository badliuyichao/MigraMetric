package com.migrametric.service.project;

import com.migrametric.common.PageResult;
import com.migrametric.dto.project.ProjectCreateDTO;
import com.migrametric.dto.project.ProjectQueryDTO;
import com.migrametric.vo.project.ProjectDetailVO;
import com.migrametric.vo.project.ProjectVO;

/**
 * 项目服务接口
 *
 * @author MigraMetric Team
 */
public interface ProjectService {

    /**
     * 创建项目
     *
     * @param createDTO 创建信息
     * @return 项目ID
     */
    Long create(ProjectCreateDTO createDTO);

    /**
     * 分页查询项目列表
     *
     * @param queryDTO 查询条件
     * @return 分页结果
     */
    PageResult<ProjectVO> queryPage(ProjectQueryDTO queryDTO);

    /**
     * 根据ID获取项目详情
     *
     * @param id 项目ID
     * @return 项目详情
     */
    ProjectVO getById(Long id);

    /**
     * 获取项目详情（包含评估概况）
     *
     * @param id 项目ID
     * @return 项目详情VO
     */
    ProjectDetailVO getProjectDetail(Long id);
}
