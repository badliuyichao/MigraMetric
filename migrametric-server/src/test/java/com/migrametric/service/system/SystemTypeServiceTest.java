package com.migrametric.service.system;

import com.migrametric.common.BusinessException;
import com.migrametric.common.PageResult;
import com.migrametric.dto.system.SystemTypeCreateDTO;
import com.migrametric.dto.system.SystemTypeQueryDTO;
import com.migrametric.dto.system.SystemTypeUpdateDTO;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.migrametric.entity.system.SystemType;
import com.migrametric.mapper.system.SystemTypeMapper;
import com.migrametric.service.system.impl.SystemTypeServiceImpl;
import com.migrametric.vo.system.SystemTypeVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * SystemTypeService 单元测试
 *
 * @author MigraMetric Team
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("SystemTypeService 单元测试")
class SystemTypeServiceTest {

    @Mock
    private SystemTypeMapper systemTypeMapper;

    @InjectMocks
    private SystemTypeServiceImpl systemTypeService;

    private SystemType mockSystemType;

    @BeforeEach
    void setUp() {
        mockSystemType = new SystemType();
        mockSystemType.setId(1L);
        mockSystemType.setSystemName("SAP");
        mockSystemType.setSystemCategory(1);
        mockSystemType.setDescription("SAP ERP系统");
        mockSystemType.setStatus(1);
        mockSystemType.setCreateTime(LocalDateTime.now());
        mockSystemType.setCreateBy("admin");
    }

    @Test
    @DisplayName("分页查询系统类型 - 返回结果")
    void testQueryPage() {
        // Given
        SystemTypeQueryDTO queryDTO = new SystemTypeQueryDTO();
        queryDTO.setPageNum(1);
        queryDTO.setPageSize(10);

        com.baomidou.mybatisplus.extension.plugins.pagination.Page<SystemType> mockPage =
                new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(1, 10, 2);
        mockPage.setRecords(Arrays.asList(mockSystemType));

        when(systemTypeMapper.selectPage(any(Page.class), any())).thenReturn(mockPage);

        // When
        PageResult<SystemTypeVO> result = systemTypeService.queryPage(queryDTO);

        // Then
        assertNotNull(result);
        assertEquals(2, result.getTotal());
    }

    @Test
    @DisplayName("根据ID查询系统类型 - 存在时返回VO")
    void testGetByIdExists() {
        // Given
        when(systemTypeMapper.selectById(1L)).thenReturn(mockSystemType);

        // When
        SystemTypeVO vo = systemTypeService.getById(1L);

        // Then
        assertNotNull(vo);
        assertEquals("SAP", vo.getSystemName());
        assertEquals(1, vo.getSystemCategory());
        assertEquals("源系统", vo.getSystemCategoryText());
    }

    @Test
    @DisplayName("根据ID查询系统类型 - 不存在时抛出异常")
    void testGetByIdNotExists() {
        // Given
        when(systemTypeMapper.selectById(999L)).thenReturn(null);

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class,
                () -> systemTypeService.getById(999L));
        assertEquals("系统类型不存在", exception.getMessage());
    }

    @Test
    @DisplayName("创建系统类型 - 成功")
    void testCreateSuccess() {
        // Given
        SystemTypeCreateDTO createDTO = new SystemTypeCreateDTO();
        createDTO.setSystemName("Oracle");
        createDTO.setSystemCategory(1);
        createDTO.setDescription("Oracle EBS系统");

        when(systemTypeMapper.countBySystemName("Oracle")).thenReturn(0);
        when(systemTypeMapper.insert(any(SystemType.class))).thenAnswer(invocation -> {
            SystemType entity = invocation.getArgument(0);
            // 模拟MyBatis-Plus插入后自动生成ID
            ReflectionTestUtils.setField(entity, "id", 1L);
            return 1;
        });

        // When
        Long id = systemTypeService.create(createDTO);

        // Then
        assertNotNull(id);
        assertEquals(1L, id);
        verify(systemTypeMapper).insert(any(SystemType.class));
    }

    @Test
    @DisplayName("创建系统类型 - 系统名称重复")
    void testCreateDuplicateName() {
        // Given
        SystemTypeCreateDTO createDTO = new SystemTypeCreateDTO();
        createDTO.setSystemName("SAP");
        createDTO.setSystemCategory(1);

        when(systemTypeMapper.countBySystemName("SAP")).thenReturn(1);

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class,
                () -> systemTypeService.create(createDTO));
        assertEquals("系统名称已存在", exception.getMessage());
    }

    @Test
    @DisplayName("创建系统类型 - 无效的系统类别")
    void testCreateInvalidCategory() {
        // Given
        SystemTypeCreateDTO createDTO = new SystemTypeCreateDTO();
        createDTO.setSystemName("Test");
        createDTO.setSystemCategory(3); // 无效类别

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class,
                () -> systemTypeService.create(createDTO));
        assertEquals("系统类别无效", exception.getMessage());
    }

    @Test
    @DisplayName("更新系统类型 - 成功")
    void testUpdateSuccess() {
        // Given
        SystemTypeUpdateDTO updateDTO = new SystemTypeUpdateDTO();
        updateDTO.setId(1L);
        updateDTO.setSystemName("SAP R3");
        updateDTO.setSystemCategory(1);
        updateDTO.setDescription("更新后的描述");

        when(systemTypeMapper.selectById(1L)).thenReturn(mockSystemType);
        when(systemTypeMapper.countBySystemNameExcludeId("SAP R3", 1L)).thenReturn(0);
        when(systemTypeMapper.updateById(any(SystemType.class))).thenReturn(1);

        // When
        systemTypeService.update(updateDTO);

        // Then
        verify(systemTypeMapper).updateById(any(SystemType.class));
    }

    @Test
    @DisplayName("更新系统类型 - 数据不存在")
    void testUpdateNotFound() {
        // Given
        SystemTypeUpdateDTO updateDTO = new SystemTypeUpdateDTO();
        updateDTO.setId(999L);
        updateDTO.setSystemName("Test");
        updateDTO.setSystemCategory(1);

        when(systemTypeMapper.selectById(999L)).thenReturn(null);

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class,
                () -> systemTypeService.update(updateDTO));
        assertEquals("系统类型不存在", exception.getMessage());
    }

    @Test
    @DisplayName("删除系统类型 - 成功")
    void testDeleteSuccess() {
        // Given
        when(systemTypeMapper.selectById(1L)).thenReturn(mockSystemType);
        when(systemTypeMapper.countByModuleReference(1L)).thenReturn(0);
        when(systemTypeMapper.countByProjectSourceReference(1L)).thenReturn(0);
        when(systemTypeMapper.countByProjectTargetReference(1L)).thenReturn(0);
        when(systemTypeMapper.deleteById(1L)).thenReturn(1);

        // When
        systemTypeService.delete(1L);

        // Then
        verify(systemTypeMapper).deleteById(1L);
    }

    @Test
    @DisplayName("删除系统类型 - 被模块引用")
    void testDeleteReferencedByModule() {
        // Given
        when(systemTypeMapper.selectById(1L)).thenReturn(mockSystemType);
        when(systemTypeMapper.countByModuleReference(1L)).thenReturn(1);

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class,
                () -> systemTypeService.delete(1L));
        assertEquals("该系统类型已被模块引用，无法删除", exception.getMessage());
    }

    @Test
    @DisplayName("删除系统类型 - 被项目引用（源系统）")
    void testDeleteReferencedByProjectSource() {
        // Given
        when(systemTypeMapper.selectById(1L)).thenReturn(mockSystemType);
        when(systemTypeMapper.countByModuleReference(1L)).thenReturn(0);
        when(systemTypeMapper.countByProjectSourceReference(1L)).thenReturn(1);

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class,
                () -> systemTypeService.delete(1L));
        assertEquals("该系统类型已被项目引用（源系统），无法删除", exception.getMessage());
    }

    @Test
    @DisplayName("启用系统类型 - 成功")
    void testEnableSuccess() {
        // Given
        when(systemTypeMapper.selectById(1L)).thenReturn(mockSystemType);
        when(systemTypeMapper.updateById(any(SystemType.class))).thenReturn(1);

        // When
        systemTypeService.enable(1L);

        // Then
        verify(systemTypeMapper).updateById(any(SystemType.class));
    }

    @Test
    @DisplayName("禁用系统类型 - 成功")
    void testDisableSuccess() {
        // Given
        when(systemTypeMapper.selectById(1L)).thenReturn(mockSystemType);
        when(systemTypeMapper.updateById(any(SystemType.class))).thenReturn(1);

        // When
        systemTypeService.disable(1L);

        // Then
        verify(systemTypeMapper).updateById(any(SystemType.class));
    }

    @Test
    @DisplayName("获取所有启用的系统类型 - 按类别筛选")
    void testListEnabledWithCategory() {
        // Given
        SystemType sourceSystem = new SystemType();
        sourceSystem.setId(1L);
        sourceSystem.setSystemName("SAP");
        sourceSystem.setSystemCategory(1);
        sourceSystem.setStatus(1);

        SystemType targetSystem = new SystemType();
        targetSystem.setId(2L);
        targetSystem.setSystemName("用友");
        targetSystem.setSystemCategory(2);
        targetSystem.setStatus(1);

        when(systemTypeMapper.selectList(any())).thenReturn(Arrays.asList(sourceSystem));

        // When
        List<SystemTypeVO> result = systemTypeService.listEnabled(1);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("源系统", result.get(0).getSystemCategoryText());
    }

    @Test
    @DisplayName("获取所有启用的系统类型 - 无类别筛选")
    void testListEnabledWithoutCategory() {
        // Given
        SystemType sourceSystem = new SystemType();
        sourceSystem.setId(1L);
        sourceSystem.setSystemName("SAP");
        sourceSystem.setSystemCategory(1);
        sourceSystem.setStatus(1);

        SystemType targetSystem = new SystemType();
        targetSystem.setId(2L);
        targetSystem.setSystemName("用友");
        targetSystem.setSystemCategory(2);
        targetSystem.setStatus(1);

        when(systemTypeMapper.selectList(any())).thenReturn(Arrays.asList(sourceSystem, targetSystem));

        // When
        List<SystemTypeVO> result = systemTypeService.listEnabled(null);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
    }

    @Test
    @DisplayName("系统类别文本转换 - 源系统")
    void testCategoryTextSource() {
        // Given
        when(systemTypeMapper.selectById(1L)).thenReturn(mockSystemType);

        // When
        SystemTypeVO vo = systemTypeService.getById(1L);

        // Then
        assertEquals("源系统", vo.getSystemCategoryText());
        assertEquals("启用", vo.getStatusText());
    }

    @Test
    @DisplayName("系统类别文本转换 - 目标系统")
    void testCategoryTextTarget() {
        // Given
        SystemType targetSystem = new SystemType();
        targetSystem.setId(2L);
        targetSystem.setSystemName("用友");
        targetSystem.setSystemCategory(2);
        targetSystem.setStatus(0);
        targetSystem.setCreateTime(LocalDateTime.now());

        when(systemTypeMapper.selectById(2L)).thenReturn(targetSystem);

        // When
        SystemTypeVO vo = systemTypeService.getById(2L);

        // Then
        assertEquals("目标系统", vo.getSystemCategoryText());
        assertEquals("禁用", vo.getStatusText());
    }
}
