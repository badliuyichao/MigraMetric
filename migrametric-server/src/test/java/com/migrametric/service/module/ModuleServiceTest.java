package com.migrametric.service.module;

import com.migrametric.common.BusinessException;
import com.migrametric.common.PageResult;
import com.migrametric.dto.module.ModuleCreateDTO;
import com.migrametric.dto.module.ModuleQueryDTO;
import com.migrametric.dto.module.ModuleUpdateDTO;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.migrametric.entity.module.Module;
import com.migrametric.entity.system.SystemType;
import com.migrametric.mapper.module.ModuleMapper;
import com.migrametric.mapper.system.SystemTypeMapper;
import com.migrametric.service.module.impl.ModuleServiceImpl;
import com.migrametric.vo.module.ModuleVO;
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
import org.springframework.test.util.ReflectionTestUtils;

/**
 * ModuleService 单元测试
 *
 * @author MigraMetric Team
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ModuleService 单元测试")
class ModuleServiceTest {

    @Mock
    private ModuleMapper moduleMapper;

    @Mock
    private SystemTypeMapper systemTypeMapper;

    @InjectMocks
    private ModuleServiceImpl moduleService;

    private Module mockModule;
    private SystemType mockSystemType;

    @BeforeEach
    void setUp() {
        // 设置系统类型Mapper
        ReflectionTestUtils.setField(moduleService, "systemTypeMapper", systemTypeMapper);

        mockSystemType = new SystemType();
        mockSystemType.setId(1L);
        mockSystemType.setSystemName("SAP");
        mockSystemType.setSystemCategory(1);
        mockSystemType.setStatus(1);

        mockModule = new Module();
        mockModule.setId(1L);
        mockModule.setModuleName("总账管理");
        mockModule.setSystemId(1L);
        mockModule.setCategory("财务模块");
        mockModule.setBaseWorkload(new BigDecimal("15.00"));
        mockModule.setDefaultWeight(new BigDecimal("1.20"));
        mockModule.setDescription("总账核算与管理");
        mockModule.setStatus(1);
        mockModule.setCreateTime(LocalDateTime.now());
        mockModule.setCreateBy("admin");
    }

    @Test
    @DisplayName("分页查询模块 - 返回结果")
    void testQueryPage() {
        // Given
        ModuleQueryDTO queryDTO = new ModuleQueryDTO();
        queryDTO.setPageNum(1);
        queryDTO.setPageSize(10);

        com.baomidou.mybatisplus.extension.plugins.pagination.Page<Module> mockPage =
                new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(1, 10, 2);
        mockPage.setRecords(Arrays.asList(mockModule));

        when(systemTypeMapper.selectById(1L)).thenReturn(mockSystemType);
        when(moduleMapper.selectPage(any(Page.class), any())).thenReturn(mockPage);

        // When
        PageResult<ModuleVO> result = moduleService.queryPage(queryDTO);

        // Then
        assertNotNull(result);
        assertEquals(2, result.getTotal());
    }

    @Test
    @DisplayName("根据ID查询模块 - 存在时返回VO")
    void testGetByIdExists() {
        // Given
        when(systemTypeMapper.selectById(1L)).thenReturn(mockSystemType);
        when(moduleMapper.selectById(1L)).thenReturn(mockModule);

        // When
        ModuleVO vo = moduleService.getById(1L);

        // Then
        assertNotNull(vo);
        assertEquals("总账管理", vo.getModuleName());
        assertEquals("SAP", vo.getSystemName());
        assertEquals("启用", vo.getStatusText());
    }

    @Test
    @DisplayName("根据ID查询模块 - 不存在时抛出异常")
    void testGetByIdNotExists() {
        // Given
        when(moduleMapper.selectById(999L)).thenReturn(null);

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class,
                () -> moduleService.getById(999L));
        assertEquals("模块不存在", exception.getMessage());
    }

    @Test
    @DisplayName("创建模块 - 成功")
    void testCreateSuccess() {
        // Given
        ModuleCreateDTO createDTO = new ModuleCreateDTO();
        createDTO.setModuleName("应收管理");
        createDTO.setSystemId(1L);
        createDTO.setCategory("财务模块");
        createDTO.setBaseWorkload(new BigDecimal("12.00"));
        createDTO.setDefaultWeight(new BigDecimal("1.10"));
        createDTO.setDescription("应收账款管理");

        when(systemTypeMapper.selectById(1L)).thenReturn(mockSystemType);
        when(moduleMapper.countByModuleNameExcludeId("应收管理", null)).thenReturn(0);
        when(moduleMapper.insert(any(Module.class))).thenAnswer(invocation -> {
            Module entity = invocation.getArgument(0);
            ReflectionTestUtils.setField(entity, "id", 2L);
            return 1;
        });

        // When
        Long id = moduleService.create(createDTO);

        // Then
        assertNotNull(id);
        assertEquals(2L, id);
        verify(moduleMapper).insert(any(Module.class));
    }

    @Test
    @DisplayName("创建模块 - 模块名称重复")
    void testCreateDuplicateName() {
        // Given
        ModuleCreateDTO createDTO = new ModuleCreateDTO();
        createDTO.setModuleName("总账管理");
        createDTO.setSystemId(1L);
        createDTO.setBaseWorkload(new BigDecimal("15.00"));
        createDTO.setDefaultWeight(new BigDecimal("1.20"));

        when(systemTypeMapper.selectById(1L)).thenReturn(mockSystemType);
        when(moduleMapper.countByModuleNameExcludeId("总账管理", null)).thenReturn(1);

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class,
                () -> moduleService.create(createDTO));
        assertEquals("模块名称已存在", exception.getMessage());
    }

    @Test
    @DisplayName("创建模块 - 系统不存在")
    void testCreateSystemNotExists() {
        // Given
        ModuleCreateDTO createDTO = new ModuleCreateDTO();
        createDTO.setModuleName("测试模块");
        createDTO.setSystemId(999L);
        createDTO.setBaseWorkload(new BigDecimal("10.00"));
        createDTO.setDefaultWeight(new BigDecimal("1.00"));

        when(systemTypeMapper.selectById(999L)).thenReturn(null);

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class,
                () -> moduleService.create(createDTO));
        assertEquals("所属系统不存在", exception.getMessage());
    }

    @Test
    @DisplayName("更新模块 - 成功")
    void testUpdateSuccess() {
        // Given
        ModuleUpdateDTO updateDTO = new ModuleUpdateDTO();
        updateDTO.setId(1L);
        updateDTO.setModuleName("总账管理V2");
        updateDTO.setSystemId(1L);
        updateDTO.setCategory("财务模块");
        updateDTO.setBaseWorkload(new BigDecimal("18.00"));
        updateDTO.setDefaultWeight(new BigDecimal("1.30"));
        updateDTO.setDescription("更新后的描述");

        when(systemTypeMapper.selectById(1L)).thenReturn(mockSystemType);
        when(moduleMapper.selectById(1L)).thenReturn(mockModule);
        when(moduleMapper.countByModuleNameExcludeId("总账管理V2", 1L)).thenReturn(0);
        when(moduleMapper.updateById(any(Module.class))).thenReturn(1);

        // When
        moduleService.update(updateDTO);

        // Then
        verify(moduleMapper).updateById(any(Module.class));
    }

    @Test
    @DisplayName("删除模块 - 成功")
    void testDeleteSuccess() {
        // Given
        when(moduleMapper.selectById(1L)).thenReturn(mockModule);
        when(moduleMapper.countByProjectReference(1L)).thenReturn(0);
        when(moduleMapper.deleteById(1L)).thenReturn(1);

        // When
        moduleService.delete(1L);

        // Then
        verify(moduleMapper).deleteById(1L);
    }

    @Test
    @DisplayName("删除模块 - 被项目引用")
    void testDeleteReferenced() {
        // Given
        when(moduleMapper.selectById(1L)).thenReturn(mockModule);
        when(moduleMapper.countByProjectReference(1L)).thenReturn(1);

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class,
                () -> moduleService.delete(1L));
        assertEquals("该模块已被项目引用，无法删除", exception.getMessage());
    }

    @Test
    @DisplayName("启用模块 - 成功")
    void testEnableSuccess() {
        // Given
        when(moduleMapper.selectById(1L)).thenReturn(mockModule);
        when(moduleMapper.updateById(any(Module.class))).thenReturn(1);

        // When
        moduleService.enable(1L);

        // Then
        verify(moduleMapper).updateById(any(Module.class));
    }

    @Test
    @DisplayName("禁用模块 - 成功")
    void testDisableSuccess() {
        // Given
        when(moduleMapper.selectById(1L)).thenReturn(mockModule);
        when(moduleMapper.updateById(any(Module.class))).thenReturn(1);

        // When
        moduleService.disable(1L);

        // Then
        verify(moduleMapper).updateById(any(Module.class));
    }

    @Test
    @DisplayName("获取所有启用的模块")
    void testListEnabled() {
        // Given
        when(systemTypeMapper.selectById(1L)).thenReturn(mockSystemType);
        when(moduleMapper.selectList(any())).thenReturn(Arrays.asList(mockModule));

        // When
        List<ModuleVO> result = moduleService.listEnabled(1L);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("总账管理", result.get(0).getModuleName());
    }

    @Test
    @DisplayName("获取所有模块分类")
    void testListCategories() {
        // Given
        Module module1 = new Module();
        module1.setCategory("财务模块");
        Module module2 = new Module();
        module2.setCategory("供应链模块");

        when(moduleMapper.selectList(any())).thenReturn(Arrays.asList(module1, module2));

        // When
        List<String> result = moduleService.listCategories(null);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
    }

    @Test
    @DisplayName("状态文本转换")
    void testStatusText() {
        // Given
        when(systemTypeMapper.selectById(1L)).thenReturn(mockSystemType);
        when(moduleMapper.selectById(1L)).thenReturn(mockModule);

        // When
        ModuleVO vo = moduleService.getById(1L);

        // Then
        assertEquals("启用", vo.getStatusText());
    }
}
