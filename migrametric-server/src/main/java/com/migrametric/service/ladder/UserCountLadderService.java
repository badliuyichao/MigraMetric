package com.migrametric.service.ladder;

import com.migrametric.dto.ladder.UserCountLadderCreateDTO;
import com.migrametric.dto.ladder.UserCountLadderUpdateDTO;
import com.migrametric.vo.ladder.UserCountLadderVO;

import java.util.List;

/**
 * 用户数阶梯服务接口
 *
 * @author MigraMetric Team
 */
public interface UserCountLadderService {

    /**
     * 获取所有阶梯（按排序顺序）
     *
     * @return 阶梯列表
     */
    List<UserCountLadderVO> listAll();

    /**
     * 根据ID获取阶梯详情
     *
     * @param id 阶梯ID
     * @return 阶梯详情
     */
    UserCountLadderVO getById(Long id);

    /**
     * 创建阶梯
     *
     * @param createDTO 创建信息
     * @return 阶梯ID
     */
    Long create(UserCountLadderCreateDTO createDTO);

    /**
     * 更新阶梯
     *
     * @param updateDTO 更新信息
     */
    void update(UserCountLadderUpdateDTO updateDTO);

    /**
     * 删除阶梯
     *
     * @param id 阶梯ID
     */
    void delete(Long id);

    /**
     * 上移动阶梯
     *
     * @param id 阶梯ID
     */
    void moveUp(Long id);

    /**
     * 下移动阶梯
     *
     * @param id 阶梯ID
     */
    void moveDown(Long id);

    /**
     * 根据用户数匹配阶梯
     *
     * @param count 用户数
     * @return 匹配的阶梯，如果没有匹配返回null
     */
    UserCountLadderVO matchByCount(Integer count);
}
