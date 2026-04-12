package com.migrametric.mapper.ladder;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.migrametric.entity.ladder.DataVolumeLadder;
import org.apache.ibatis.annotations.Mapper;

/**
 * 数据量阶梯Mapper接口
 *
 * @author MigraMetric Team
 */
@Mapper
public interface DataVolumeLadderMapper extends BaseMapper<DataVolumeLadder> {

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
    DataVolumeLadder selectBySortOrder(Integer sortOrder);
}
