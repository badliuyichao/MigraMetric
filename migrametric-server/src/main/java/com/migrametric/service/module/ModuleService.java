package com.migrametric.service.module;

import com.migrametric.common.PageResult;
import com.migrametric.dto.module.ModuleCreateDTO;
import com.migrametric.dto.module.ModuleQueryDTO;
import com.migrametric.dto.module.ModuleUpdateDTO;
import com.migrametric.vo.module.ModuleVO;

import java.util.List;

/**
 * 模块服务接口
 *
 * @author MigraMetric Team
 */
public interface ModuleService {

    /**
     * 分页查询模块
     *
     * @param queryDTO 查询条件
     * @return 分页结果
     */
    PageResult<ModuleVO> queryPage(ModuleQueryDTO queryDTO);

    /**
     * 根据ID获取模块详情
     *
     * @param id 模块ID
     * @return 模块详情
     */
    ModuleVO getById(Long id);

    /**
     * 创建模块
     *
     * @param createDTO 创建信息
     * @return 模块ID
     */
    Long create(ModuleCreateDTO createDTO);

    /**
     * 更新模块
     *
     * @param updateDTO 更新信息
     */
    void update(ModuleUpdateDTO updateDTO);

    /**
     * 删除模块
     *
     * @param id 模块ID
     */
    void delete(Long id);

    /**
     * 启用模块
     *
     * @param id 模块ID
     */
    void enable(Long id);

    /**
     * 禁用模块
     *
     * @param id 模块ID
     */
    void disable(Long id);

    /**
     * 获取所有启用的模块
     *
     * @param systemId 所属系统ID（可选）
     * @return 模块列表
     */
    List<ModuleVO> listEnabled(Long systemId);

    /**
     * 获取所有模块分类
     *
     * @param systemId 所属系统ID（可选）
     * @return 分类列表
     */
    List<String> listCategories(Long systemId);
}
