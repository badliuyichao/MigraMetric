package com.migrametric.service.ladder;

import com.migrametric.dto.ladder.DataVolumeLadderCreateDTO;
import com.migrametric.dto.ladder.DataVolumeLadderUpdateDTO;
import com.migrametric.vo.ladder.DataVolumeLadderVO;

import java.math.BigDecimal;
import java.util.List;

/**
 * 数据量阶梯服务接口
 *
 * @author MigraMetric Team
 */
public interface DataVolumeLadderService {

    /**
     * 获取所有阶梯（按排序顺序）
     *
     * @return 阶梯列表
     */
    List<DataVolumeLadderVO> listAll();

    /**
     * 根据ID获取阶梯详情
     *
     * @param id 阶梯ID
     * @return 阶梯详情
     */
    DataVolumeLadderVO getById(Long id);

    /**
     * 创建阶梯
     *
     * @param createDTO 创建信息
     * @return 阶梯ID
     */
    Long create(DataVolumeLadderCreateDTO createDTO);

    /**
     * 更新阶梯
     *
     * @param updateDTO 更新信息
     */
    void update(DataVolumeLadderUpdateDTO updateDTO);

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
     * 根据数据量匹配阶梯
     *
     * @param volume 数据量（万条）
     * @return 匹配的阶梯，如果没有匹配返回null
     */
    DataVolumeLadderVO matchByVolume(BigDecimal volume);
}
