package com.migrametric.mapper.ladder;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.migrametric.entity.ladder.UserCountLadder;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户数阶梯Mapper接口
 *
 * @author MigraMetric Team
 */
@Mapper
public interface UserCountLadderMapper extends BaseMapper<UserCountLadder> {

    /**
     * 获取最大的排序顺序
     *
     * @return 最大排序顺序
     */
    Integer selectMaxSortOrder();

    /**
     * 根据排序顺序查询
     *
     * @param sortOrder 排序顺序
     * @return 阶梯信息
     */
    UserCountLadder selectBySortOrder(Integer sortOrder);
}
