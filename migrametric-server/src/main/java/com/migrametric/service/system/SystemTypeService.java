package com.migrametric.service.system;

import com.migrametric.common.PageResult;
import com.migrametric.dto.system.SystemTypeCreateDTO;
import com.migrametric.dto.system.SystemTypeQueryDTO;
import com.migrametric.dto.system.SystemTypeUpdateDTO;
import com.migrametric.vo.system.SystemTypeVO;

/**
 * 系统类型服务接口
 *
 * @author MigraMetric Team
 */
public interface SystemTypeService {

    /**
     * 分页查询系统类型
     *
     * @param queryDTO 查询条件
     * @return 分页结果
     */
    PageResult<SystemTypeVO> queryPage(SystemTypeQueryDTO queryDTO);

    /**
     * 根据ID查询系统类型
     *
     * @param id 系统类型ID
     * @return 系统类型VO
     */
    SystemTypeVO getById(Long id);

    /**
     * 创建系统类型
     *
     * @param createDTO 创建DTO
     * @return 系统类型ID
     */
    Long create(SystemTypeCreateDTO createDTO);

    /**
     * 更新系统类型
     *
     * @param updateDTO 更新DTO
     */
    void update(SystemTypeUpdateDTO updateDTO);

    /**
     * 删除系统类型
     *
     * @param id 系统类型ID
     */
    void delete(Long id);

    /**
     * 启用系统类型
     *
     * @param id 系统类型ID
     */
    void enable(Long id);

    /**
     * 禁用系统类型
     *
     * @param id 系统类型ID
     */
    void disable(Long id);

    /**
     * 获取所有启用的系统类型
     *
     * @param category 系统类别：1-源系统，2-目标系统
     * @return 系统类型列表
     */
    java.util.List<SystemTypeVO> listEnabled(Integer category);
}
