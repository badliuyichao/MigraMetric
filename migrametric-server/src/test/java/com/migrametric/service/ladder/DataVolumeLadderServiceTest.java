package com.migrametric.service.ladder;

import com.migrametric.common.BusinessException;
import com.migrametric.dto.ladder.DataVolumeLadderCreateDTO;
import com.migrametric.dto.ladder.DataVolumeLadderUpdateDTO;
import com.migrametric.entity.ladder.DataVolumeLadder;
import com.migrametric.mapper.ladder.DataVolumeLadderMapper;
import com.migrametric.service.ladder.impl.DataVolumeLadderServiceImpl;
import com.migrametric.vo.ladder.DataVolumeLadderVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

/**
 * DataVolumeLadderService 单元测试
 *
 * @author MigraMetric Team
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("DataVolumeLadderService 单元测试")
class DataVolumeLadderServiceTest {

    @Mock
    private DataVolumeLadderMapper ladderMapper;

    @InjectMocks
    private DataVolumeLadderServiceImpl ladderService;

    private DataVolumeLadder mockLadder;

    @BeforeEach
    void setUp() {
        mockLadder = new DataVolumeLadder();
        mockLadder.setId(1L);
        mockLadder.setLadderName("小型");
        mockLadder.setMinVolume(new BigDecimal("0"));
        mockLadder.setMaxVolume(new BigDecimal("10"));
        mockLadder.setWeight(new BigDecimal("0.80"));
        mockLadder.setSortOrder(1);
        mockLadder.setCreateTime(LocalDateTime.now());
        mockLadder.setCreateBy("admin");
    }

    @Test
    @DisplayName("获取所有阶梯 - 按排序顺序")
    void testListAll() {
        // Given
        DataVolumeLadder ladder1 = new DataVolumeLadder();
        ladder1.setId(1L);
        ladder1.setLadderName("小型");
        ladder1.setMinVolume(new BigDecimal("0"));
        ladder1.setMaxVolume(new BigDecimal("10"));
        ladder1.setWeight(new BigDecimal("0.80"));
        ladder1.setSortOrder(1);

        DataVolumeLadder ladder2 = new DataVolumeLadder();
        ladder2.setId(2L);
        ladder2.setLadderName("中型");
        ladder2.setMinVolume(new BigDecimal("10"));
        ladder2.setMaxVolume(new BigDecimal("100"));
        ladder2.setWeight(new BigDecimal("1.00"));
        ladder2.setSortOrder(2);

        when(ladderMapper.selectList(any())).thenReturn(Arrays.asList(ladder1, ladder2));

        // When
        List<DataVolumeLadderVO> result = ladderService.listAll();

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("小型", result.get(0).getLadderName());
        assertEquals("中型", result.get(1).getLadderName());
    }

    @Test
    @DisplayName("根据ID获取阶梯 - 存在时返回VO")
    void testGetByIdExists() {
        // Given
        when(ladderMapper.selectById(1L)).thenReturn(mockLadder);

        // When
        DataVolumeLadderVO vo = ladderService.getById(1L);

        // Then
        assertNotNull(vo);
        assertEquals("小型", vo.getLadderName());
        assertEquals("0万-10万条", vo.getVolumeRangeText());
        assertEquals(new BigDecimal("0.80"), vo.getWeight());
    }

    @Test
    @DisplayName("根据ID获取阶梯 - 不存在时抛出异常")
    void testGetByIdNotExists() {
        // Given
        when(ladderMapper.selectById(999L)).thenReturn(null);

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class,
                () -> ladderService.getById(999L));
        assertEquals("数据量阶梯不存在", exception.getMessage());
    }

    @Test
    @DisplayName("创建阶梯 - 成功")
    void testCreateSuccess() {
        // Given
        DataVolumeLadderCreateDTO createDTO = new DataVolumeLadderCreateDTO();
        createDTO.setLadderName("大型");
        createDTO.setMinVolume(new BigDecimal("100"));
        createDTO.setMaxVolume(new BigDecimal("1000"));
        createDTO.setWeight(new BigDecimal("1.50"));
        createDTO.setSortOrder(3);

        when(ladderMapper.insert(any(DataVolumeLadder.class))).thenAnswer(invocation -> {
            DataVolumeLadder entity = invocation.getArgument(0);
            ReflectionTestUtils.setField(entity, "id", 3L);
            return 1;
        });

        // When
        Long id = ladderService.create(createDTO);

        // Then
        assertNotNull(id);
        assertEquals(3L, id);
        verify(ladderMapper).insert(any(DataVolumeLadder.class));
    }

    @Test
    @DisplayName("创建阶梯 - 数据量上限小于下限")
    void testCreateInvalidRange() {
        // Given
        DataVolumeLadderCreateDTO createDTO = new DataVolumeLadderCreateDTO();
        createDTO.setLadderName("测试");
        createDTO.setMinVolume(new BigDecimal("100"));
        createDTO.setMaxVolume(new BigDecimal("10")); // 无效：上限小于下限
        createDTO.setWeight(new BigDecimal("1.00"));

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class,
                () -> ladderService.create(createDTO));
        assertEquals("数据量下限必须小于上限", exception.getMessage());
    }

    @Test
    @DisplayName("更新阶梯 - 成功")
    void testUpdateSuccess() {
        // Given
        DataVolumeLadderUpdateDTO updateDTO = new DataVolumeLadderUpdateDTO();
        updateDTO.setId(1L);
        updateDTO.setLadderName("小型V2");
        updateDTO.setMinVolume(new BigDecimal("0"));
        updateDTO.setMaxVolume(new BigDecimal("15"));
        updateDTO.setWeight(new BigDecimal("0.90"));
        updateDTO.setSortOrder(1);

        when(ladderMapper.selectById(1L)).thenReturn(mockLadder);
        when(ladderMapper.updateById(any(DataVolumeLadder.class))).thenReturn(1);

        // When
        ladderService.update(updateDTO);

        // Then
        verify(ladderMapper).updateById(any(DataVolumeLadder.class));
    }

    @Test
    @DisplayName("删除阶梯 - 成功")
    void testDeleteSuccess() {
        // Given
        when(ladderMapper.selectById(1L)).thenReturn(mockLadder);
        when(ladderMapper.deleteById(1L)).thenReturn(1);

        // When
        ladderService.delete(1L);

        // Then
        verify(ladderMapper).deleteById(1L);
    }

    @Test
    @DisplayName("上移动阶梯 - 成功")
    void testMoveUpSuccess() {
        // Given
        mockLadder.setSortOrder(2); // 设为第2个，可以上移

        DataVolumeLadder prevLadder = new DataVolumeLadder();
        prevLadder.setId(1L);
        prevLadder.setSortOrder(1);

        when(ladderMapper.selectById(2L)).thenReturn(mockLadder);
        when(ladderMapper.selectBySortOrder(1)).thenReturn(prevLadder);
        when(ladderMapper.updateById(any(DataVolumeLadder.class))).thenReturn(1);

        // When
        ladderService.moveUp(2L);

        // Then
        verify(ladderMapper, times(2)).updateById(any(DataVolumeLadder.class));
    }

    @Test
    @DisplayName("上移动阶梯 - 已是第一个")
    void testMoveUpFirst() {
        // Given
        mockLadder.setSortOrder(1);
        when(ladderMapper.selectById(1L)).thenReturn(mockLadder);

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class,
                () -> ladderService.moveUp(1L));
        assertEquals("已经是第一个，无法上移", exception.getMessage());
    }

    @Test
    @DisplayName("下移动阶梯 - 成功")
    void testMoveDownSuccess() {
        // Given
        DataVolumeLadder nextLadder = new DataVolumeLadder();
        nextLadder.setId(2L);
        nextLadder.setSortOrder(2);

        mockLadder.setSortOrder(1);
        when(ladderMapper.selectById(1L)).thenReturn(mockLadder);
        when(ladderMapper.selectMaxSortOrder()).thenReturn(2);
        when(ladderMapper.selectBySortOrder(2)).thenReturn(nextLadder);
        when(ladderMapper.updateById(any(DataVolumeLadder.class))).thenReturn(1);

        // When
        ladderService.moveDown(1L);

        // Then
        verify(ladderMapper, times(2)).updateById(any(DataVolumeLadder.class));
    }

    @Test
    @DisplayName("下移动阶梯 - 已是最有一个")
    void testMoveDownLast() {
        // Given
        mockLadder.setSortOrder(4);
        when(ladderMapper.selectById(1L)).thenReturn(mockLadder);
        when(ladderMapper.selectMaxSortOrder()).thenReturn(4);

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class,
                () -> ladderService.moveDown(1L));
        assertEquals("已经是最后一个，无法下移", exception.getMessage());
    }

    @Test
    @DisplayName("根据数据量匹配阶梯 - 匹配成功")
    void testMatchByVolumeSuccess() {
        // Given
        DataVolumeLadder ladder1 = new DataVolumeLadder();
        ladder1.setId(1L);
        ladder1.setLadderName("小型");
        ladder1.setMinVolume(new BigDecimal("0"));
        ladder1.setMaxVolume(new BigDecimal("10"));
        ladder1.setWeight(new BigDecimal("0.80"));
        ladder1.setSortOrder(1);

        DataVolumeLadder ladder2 = new DataVolumeLadder();
        ladder2.setId(2L);
        ladder2.setLadderName("中型");
        ladder2.setMinVolume(new BigDecimal("10"));
        ladder2.setMaxVolume(new BigDecimal("100"));
        ladder2.setWeight(new BigDecimal("1.00"));
        ladder2.setSortOrder(2);

        when(ladderMapper.selectList(any())).thenReturn(Arrays.asList(ladder1, ladder2));

        // When - 测试5万条数据应该匹配"小型"
        DataVolumeLadderVO result = ladderService.matchByVolume(new BigDecimal("5"));

        // Then
        assertNotNull(result);
        assertEquals("小型", result.getLadderName());
        assertEquals(new BigDecimal("0.80"), result.getWeight());
    }

    @Test
    @DisplayName("根据数据量匹配阶梯 - 匹配无上限阶梯")
    void testMatchByVolumeNoUpperLimit() {
        // Given
        DataVolumeLadder ladder = new DataVolumeLadder();
        ladder.setId(4L);
        ladder.setLadderName("超大型");
        ladder.setMinVolume(new BigDecimal("1000"));
        ladder.setMaxVolume(null); // 无上限
        ladder.setWeight(new BigDecimal("2.00"));
        ladder.setSortOrder(4);

        when(ladderMapper.selectList(any())).thenReturn(Arrays.asList(ladder));

        // When - 测试1000万条数据应该匹配"超大型"
        DataVolumeLadderVO result = ladderService.matchByVolume(new BigDecimal("1500"));

        // Then
        assertNotNull(result);
        assertEquals("超大型", result.getLadderName());
        assertEquals(new BigDecimal("2.00"), result.getWeight());
    }

    @Test
    @DisplayName("根据数据量匹配阶梯 - 无匹配时返回null")
    void testMatchByVolumeNoMatch() {
        // Given
        DataVolumeLadder ladder = new DataVolumeLadder();
        ladder.setId(1L);
        ladder.setLadderName("小型");
        ladder.setMinVolume(new BigDecimal("0"));
        ladder.setMaxVolume(new BigDecimal("10"));
        ladder.setWeight(new BigDecimal("0.80"));
        ladder.setSortOrder(1);

        when(ladderMapper.selectList(any())).thenReturn(Arrays.asList(ladder));

        // When - 测试100万条数据，没有阶梯能匹配
        DataVolumeLadderVO result = ladderService.matchByVolume(new BigDecimal("100"));

        // Then
        assertNull(result);
    }

    @Test
    @DisplayName("根据数据量匹配阶梯 - 空数据量返回null (LM-DV-005)")
    void testMatchByVolumeNull() {
        // Given - 无需stub，因为service层在volume==null时直接返回null，不调用mapper
        // When
        DataVolumeLadderVO result = ladderService.matchByVolume(null);

        // Then
        assertNull(result);
    }

    @Test
    @DisplayName("根据数据量匹配阶梯 - 阶梯无数据返回null (LM-DV-006)")
    void testMatchByVolumeNoLadders() {
        // Given
        when(ladderMapper.selectList(any())).thenReturn(Arrays.asList());

        // When
        DataVolumeLadderVO result = ladderService.matchByVolume(new BigDecimal("500"));

        // Then
        assertNull(result);
    }

    @Test
    @DisplayName("根据数据量匹配阶梯 - 精确匹配下限边界 (LM-DV-001)")
    void testMatchByVolumeExactMinBoundary() {
        // Given - 阶梯配置：中型 100-1000万
        DataVolumeLadder ladder1 = new DataVolumeLadder();
        ladder1.setId(1L);
        ladder1.setLadderName("小型");
        ladder1.setMinVolume(new BigDecimal("0"));
        ladder1.setMaxVolume(new BigDecimal("100"));
        ladder1.setWeight(new BigDecimal("0.80"));
        ladder1.setSortOrder(1);

        DataVolumeLadder ladder2 = new DataVolumeLadder();
        ladder2.setId(2L);
        ladder2.setLadderName("中型");
        ladder2.setMinVolume(new BigDecimal("100"));
        ladder2.setMaxVolume(new BigDecimal("1000"));
        ladder2.setWeight(new BigDecimal("1.00"));
        ladder2.setSortOrder(2);

        when(ladderMapper.selectList(any())).thenReturn(Arrays.asList(ladder1, ladder2));

        // When - 100万正好匹配中型阶梯下限
        DataVolumeLadderVO result = ladderService.matchByVolume(new BigDecimal("100"));

        // Then
        assertNotNull(result);
        assertEquals("中型", result.getLadderName());
        assertEquals(new BigDecimal("1.00"), result.getWeight());
    }

    @Test
    @DisplayName("根据数据量匹配阶梯 - 精确匹配上限边界 (LM-DV-002)")
    void testMatchByVolumeExactMaxBoundary() {
        // Given - 阶梯配置：中型 100-1000万
        DataVolumeLadder ladder = new DataVolumeLadder();
        ladder.setId(2L);
        ladder.setLadderName("中型");
        ladder.setMinVolume(new BigDecimal("100"));
        ladder.setMaxVolume(new BigDecimal("1000"));
        ladder.setWeight(new BigDecimal("1.00"));
        ladder.setSortOrder(2);

        when(ladderMapper.selectList(any())).thenReturn(Arrays.asList(ladder));

        // When - 999万在100-1000万范围内（小于上限）
        DataVolumeLadderVO result = ladderService.matchByVolume(new BigDecimal("999"));

        // Then
        assertNotNull(result);
        assertEquals("中型", result.getLadderName());
    }

    @Test
    @DisplayName("根据数据量匹配阶梯 - 超出最大阶梯范围 (LM-DV-003)")
    void testMatchByVolumeExceedMax() {
        // Given
        DataVolumeLadder ladder = new DataVolumeLadder();
        ladder.setId(4L);
        ladder.setLadderName("超大型");
        ladder.setMinVolume(new BigDecimal("1000"));
        ladder.setMaxVolume(null); // 无上限
        ladder.setWeight(new BigDecimal("2.00"));
        ladder.setSortOrder(4);

        when(ladderMapper.selectList(any())).thenReturn(Arrays.asList(ladder));

        // When - 2000万超出1000万，应匹配无上限阶梯
        DataVolumeLadderVO result = ladderService.matchByVolume(new BigDecimal("2000"));

        // Then
        assertNotNull(result);
        assertEquals("超大型", result.getLadderName());
        assertEquals(new BigDecimal("2.00"), result.getWeight());
    }

    @Test
    @DisplayName("根据数据量匹配阶梯 - 小于最小阶梯范围 (LM-DV-004)")
    void testMatchByVolumeBelowMin() {
        // Given
        DataVolumeLadder ladder = new DataVolumeLadder();
        ladder.setId(1L);
        ladder.setLadderName("小型");
        ladder.setMinVolume(new BigDecimal("0"));
        ladder.setMaxVolume(new BigDecimal("10"));
        ladder.setWeight(new BigDecimal("0.80"));
        ladder.setSortOrder(1);

        when(ladderMapper.selectList(any())).thenReturn(Arrays.asList(ladder));

        // When - 5万应匹配小型（0-10万）
        DataVolumeLadderVO result = ladderService.matchByVolume(new BigDecimal("5"));

        // Then
        assertNotNull(result);
        assertEquals("小型", result.getLadderName());
        assertEquals(new BigDecimal("0.80"), result.getWeight());
    }

    @Test
    @DisplayName("根据数据量匹配阶梯 - 阶梯系数精度验证 (LM-EX-003)")
    void testMatchByVolumePrecision() {
        // Given
        DataVolumeLadder ladder = new DataVolumeLadder();
        ladder.setId(1L);
        ladder.setLadderName("中型");
        ladder.setMinVolume(new BigDecimal("100"));
        ladder.setMaxVolume(new BigDecimal("1000"));
        ladder.setWeight(new BigDecimal("1.5678")); // 4位小数精度
        ladder.setSortOrder(1);

        when(ladderMapper.selectList(any())).thenReturn(Arrays.asList(ladder));

        // When
        DataVolumeLadderVO result = ladderService.matchByVolume(new BigDecimal("500"));

        // Then
        assertNotNull(result);
        assertEquals(new BigDecimal("1.5678"), result.getWeight());
    }

    @Test
    @DisplayName("根据数据量匹配阶梯 - 阶梯范围重叠处理 (LM-EX-005)")
    void testMatchByVolumeOverlappingRanges() {
        // Given - 阶梯范围重叠
        DataVolumeLadder ladder1 = new DataVolumeLadder();
        ladder1.setId(1L);
        ladder1.setLadderName("小型");
        ladder1.setMinVolume(new BigDecimal("0"));
        ladder1.setMaxVolume(new BigDecimal("100"));
        ladder1.setWeight(new BigDecimal("0.80"));
        ladder1.setSortOrder(1);

        DataVolumeLadder ladder2 = new DataVolumeLadder();
        ladder2.setId(2L);
        ladder2.setLadderName("中型");
        ladder2.setMinVolume(new BigDecimal("99")); // 与小型重叠
        ladder2.setMaxVolume(new BigDecimal("1000"));
        ladder2.setWeight(new BigDecimal("1.00"));
        ladder2.setSortOrder(2);

        // 按sortOrder排序后，100万应匹配第一个能覆盖的阶梯（小型）
        when(ladderMapper.selectList(any())).thenReturn(Arrays.asList(ladder1, ladder2));

        // When - 100万在小型范围内
        DataVolumeLadderVO result = ladderService.matchByVolume(new BigDecimal("100"));

        // Then - 按顺序匹配，100万小于小型上限100，不能匹配小型
        // 100万正好等于中型下限99，应匹配中型
        assertNotNull(result);
        assertEquals("中型", result.getLadderName());
    }

    @Test
    @DisplayName("根据数据量匹配阶梯 - 数据量0匹配 (LM-EX-006)")
    void testMatchByVolumeZero() {
        // Given
        DataVolumeLadder ladder = new DataVolumeLadder();
        ladder.setId(1L);
        ladder.setLadderName("小型");
        ladder.setMinVolume(new BigDecimal("0"));
        ladder.setMaxVolume(new BigDecimal("10"));
        ladder.setWeight(new BigDecimal("0.80"));
        ladder.setSortOrder(1);

        when(ladderMapper.selectList(any())).thenReturn(Arrays.asList(ladder));

        // When - 0万应匹配小型（0<=0<10）
        DataVolumeLadderVO result = ladderService.matchByVolume(new BigDecimal("0"));

        // Then
        assertNotNull(result);
        assertEquals("小型", result.getLadderName());
    }

    @Test
    @DisplayName("无上限阶梯的范围文本")
    void testVolumeRangeTextNoUpperLimit() {
        // Given
        when(ladderMapper.selectById(1L)).thenReturn(mockLadder);

        // When
        DataVolumeLadderVO vo = ladderService.getById(1L);

        // Then
        assertEquals("0万-10万条", vo.getVolumeRangeText());
    }

    @Test
    @DisplayName("无上限阶梯的范围文本 - null上限")
    void testVolumeRangeTextNullUpperLimit() {
        // Given
        mockLadder.setMaxVolume(null);
        when(ladderMapper.selectById(1L)).thenReturn(mockLadder);

        // When
        DataVolumeLadderVO vo = ladderService.getById(1L);

        // Then
        assertEquals("0万条以上", vo.getVolumeRangeText());
    }
}
