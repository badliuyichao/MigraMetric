package com.migrametric.service.ladder;

import com.migrametric.common.BusinessException;
import com.migrametric.dto.ladder.UserCountLadderCreateDTO;
import com.migrametric.dto.ladder.UserCountLadderUpdateDTO;
import com.migrametric.entity.ladder.UserCountLadder;
import com.migrametric.mapper.ladder.UserCountLadderMapper;
import com.migrametric.service.ladder.impl.UserCountLadderServiceImpl;
import com.migrametric.vo.ladder.UserCountLadderVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * UserCountLadderService 单元测试
 *
 * @author MigraMetric Team
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("UserCountLadderService 单元测试")
class UserCountLadderServiceTest {

    @Mock
    private UserCountLadderMapper ladderMapper;

    @InjectMocks
    private UserCountLadderServiceImpl ladderService;

    private UserCountLadder mockLadder;

    @BeforeEach
    void setUp() {
        mockLadder = new UserCountLadder();
        mockLadder.setId(1L);
        mockLadder.setLadderName("小规模");
        mockLadder.setMinCount(0);
        mockLadder.setMaxCount(100);
        mockLadder.setWeight(new BigDecimal("1.00"));
        mockLadder.setSortOrder(1);
        mockLadder.setCreateTime(LocalDateTime.now());
        mockLadder.setCreateBy("admin");
    }

    @Test
    @DisplayName("获取所有阶梯 - 按排序顺序")
    void testListAll() {
        // Given
        UserCountLadder ladder1 = new UserCountLadder();
        ladder1.setId(1L);
        ladder1.setLadderName("小规模");
        ladder1.setMinCount(0);
        ladder1.setMaxCount(100);
        ladder1.setWeight(new BigDecimal("1.00"));
        ladder1.setSortOrder(1);

        UserCountLadder ladder2 = new UserCountLadder();
        ladder2.setId(2L);
        ladder2.setLadderName("中规模");
        ladder2.setMinCount(100);
        ladder2.setMaxCount(500);
        ladder2.setWeight(new BigDecimal("1.20"));
        ladder2.setSortOrder(2);

        when(ladderMapper.selectList(any())).thenReturn(Arrays.asList(ladder1, ladder2));

        // When
        List<UserCountLadderVO> result = ladderService.listAll();

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("小规模", result.get(0).getLadderName());
        assertEquals("中规模", result.get(1).getLadderName());
    }

    @Test
    @DisplayName("根据ID获取阶梯 - 存在时返回VO")
    void testGetByIdExists() {
        // Given
        when(ladderMapper.selectById(1L)).thenReturn(mockLadder);

        // When
        UserCountLadderVO vo = ladderService.getById(1L);

        // Then
        assertNotNull(vo);
        assertEquals("小规模", vo.getLadderName());
        assertEquals("0人-100人", vo.getCountRangeText());
        assertEquals(new BigDecimal("1.00"), vo.getWeight());
    }

    @Test
    @DisplayName("根据ID获取阶梯 - 不存在时抛出异常")
    void testGetByIdNotExists() {
        // Given
        when(ladderMapper.selectById(999L)).thenReturn(null);

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class,
                () -> ladderService.getById(999L));
        assertEquals("用户数阶梯不存在", exception.getMessage());
    }

    @Test
    @DisplayName("创建阶梯 - 成功")
    void testCreateSuccess() {
        // Given
        UserCountLadderCreateDTO createDTO = new UserCountLadderCreateDTO();
        createDTO.setLadderName("大规模");
        createDTO.setMinCount(500);
        createDTO.setMaxCount(1000);
        createDTO.setWeight(new BigDecimal("1.50"));
        createDTO.setSortOrder(3);

        when(ladderMapper.insert(any(UserCountLadder.class))).thenAnswer(invocation -> {
            UserCountLadder entity = invocation.getArgument(0);
            ReflectionTestUtils.setField(entity, "id", 3L);
            return 1;
        });

        // When
        Long id = ladderService.create(createDTO);

        // Then
        assertNotNull(id);
        assertEquals(3L, id);
        verify(ladderMapper).insert(any(UserCountLadder.class));
    }

    @Test
    @DisplayName("创建阶梯 - 用户数上限小于下限")
    void testCreateInvalidRange() {
        // Given
        UserCountLadderCreateDTO createDTO = new UserCountLadderCreateDTO();
        createDTO.setLadderName("测试");
        createDTO.setMinCount(100);
        createDTO.setMaxCount(10); // 无效：上限小于下限
        createDTO.setWeight(new BigDecimal("1.00"));

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class,
                () -> ladderService.create(createDTO));
        assertEquals("用户数下限必须小于上限", exception.getMessage());
    }

    @Test
    @DisplayName("更新阶梯 - 成功")
    void testUpdateSuccess() {
        // Given
        UserCountLadderUpdateDTO updateDTO = new UserCountLadderUpdateDTO();
        updateDTO.setId(1L);
        updateDTO.setLadderName("小规模V2");
        updateDTO.setMinCount(0);
        updateDTO.setMaxCount(150);
        updateDTO.setWeight(new BigDecimal("1.10"));
        updateDTO.setSortOrder(1);

        when(ladderMapper.selectById(1L)).thenReturn(mockLadder);
        when(ladderMapper.updateById(any(UserCountLadder.class))).thenReturn(1);

        // When
        ladderService.update(updateDTO);

        // Then
        verify(ladderMapper).updateById(any(UserCountLadder.class));
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
        mockLadder.setSortOrder(2);

        UserCountLadder prevLadder = new UserCountLadder();
        prevLadder.setId(1L);
        prevLadder.setSortOrder(1);

        when(ladderMapper.selectById(2L)).thenReturn(mockLadder);
        when(ladderMapper.selectBySortOrder(1)).thenReturn(prevLadder);
        when(ladderMapper.updateById(any(UserCountLadder.class))).thenReturn(1);

        // When
        ladderService.moveUp(2L);

        // Then
        verify(ladderMapper, times(2)).updateById(any(UserCountLadder.class));
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
        UserCountLadder nextLadder = new UserCountLadder();
        nextLadder.setId(2L);
        nextLadder.setSortOrder(2);

        mockLadder.setSortOrder(1);
        when(ladderMapper.selectById(1L)).thenReturn(mockLadder);
        when(ladderMapper.selectMaxSortOrder()).thenReturn(2);
        when(ladderMapper.selectBySortOrder(2)).thenReturn(nextLadder);
        when(ladderMapper.updateById(any(UserCountLadder.class))).thenReturn(1);

        // When
        ladderService.moveDown(1L);

        // Then
        verify(ladderMapper, times(2)).updateById(any(UserCountLadder.class));
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
    @DisplayName("根据用户数匹配阶梯 - 匹配成功")
    void testMatchByCountSuccess() {
        // Given
        UserCountLadder ladder1 = new UserCountLadder();
        ladder1.setId(1L);
        ladder1.setLadderName("小规模");
        ladder1.setMinCount(0);
        ladder1.setMaxCount(100);
        ladder1.setWeight(new BigDecimal("1.00"));
        ladder1.setSortOrder(1);

        UserCountLadder ladder2 = new UserCountLadder();
        ladder2.setId(2L);
        ladder2.setLadderName("中规模");
        ladder2.setMinCount(100);
        ladder2.setMaxCount(500);
        ladder2.setWeight(new BigDecimal("1.20"));
        ladder2.setSortOrder(2);

        when(ladderMapper.selectList(any())).thenReturn(Arrays.asList(ladder1, ladder2));

        // When - 测试50人应该匹配"小规模"
        UserCountLadderVO result = ladderService.matchByCount(50);

        // Then
        assertNotNull(result);
        assertEquals("小规模", result.getLadderName());
        assertEquals(new BigDecimal("1.00"), result.getWeight());
    }

    @Test
    @DisplayName("根据用户数匹配阶梯 - 匹配无上限阶梯")
    void testMatchByCountNoUpperLimit() {
        // Given
        UserCountLadder ladder = new UserCountLadder();
        ladder.setId(4L);
        ladder.setLadderName("超大规模");
        ladder.setMinCount(1000);
        ladder.setMaxCount(null); // 无上限
        ladder.setWeight(new BigDecimal("2.00"));
        ladder.setSortOrder(4);

        when(ladderMapper.selectList(any())).thenReturn(Arrays.asList(ladder));

        // When - 测试1500人应该匹配"超大规模"
        UserCountLadderVO result = ladderService.matchByCount(1500);

        // Then
        assertNotNull(result);
        assertEquals("超大规模", result.getLadderName());
        assertEquals(new BigDecimal("2.00"), result.getWeight());
    }

    @Test
    @DisplayName("根据用户数匹配阶梯 - 无匹配时返回null")
    void testMatchByCountNoMatch() {
        // Given
        UserCountLadder ladder = new UserCountLadder();
        ladder.setId(1L);
        ladder.setLadderName("小规模");
        ladder.setMinCount(0);
        ladder.setMaxCount(100);
        ladder.setWeight(new BigDecimal("1.00"));
        ladder.setSortOrder(1);

        when(ladderMapper.selectList(any())).thenReturn(Arrays.asList(ladder));

        // When - 测试200人，没有阶梯能匹配
        UserCountLadderVO result = ladderService.matchByCount(200);

        // Then
        assertNull(result);
    }

    @Test
    @DisplayName("无上限阶梯的范围文本")
    void testCountRangeTextNoUpperLimit() {
        // Given
        mockLadder.setMaxCount(null);
        when(ladderMapper.selectById(1L)).thenReturn(mockLadder);

        // When
        UserCountLadderVO vo = ladderService.getById(1L);

        // Then
        assertEquals("0人以上", vo.getCountRangeText());
    }
}
