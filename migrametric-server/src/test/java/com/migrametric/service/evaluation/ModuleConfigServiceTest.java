package com.migrametric.service.evaluation;

import com.migrametric.common.BusinessException;
import com.migrametric.dto.evaluation.ModuleConfigDTO;
import com.migrametric.entity.evaluation.ProjectModuleConfig;
import com.migrametric.entity.module.Module;
import com.migrametric.entity.project.Project;
import com.migrametric.mapper.evaluation.ProjectModuleConfigMapper;
import com.migrametric.mapper.module.ModuleMapper;
import com.migrametric.mapper.project.ProjectMapper;
import com.migrametric.service.evaluation.impl.ModuleConfigServiceImpl;
import com.migrametric.vo.evaluation.ModuleConfigVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * ModuleConfigService 单元测试
 *
 * @author MigraMetric Team
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ModuleConfigService 单元测试")
class ModuleConfigServiceTest {

    @Mock
    private ProjectMapper projectMapper;

    @Mock
    private ModuleMapper moduleMapper;

    @Mock
    private ProjectModuleConfigMapper moduleConfigMapper;

    @InjectMocks
    private ModuleConfigServiceImpl moduleConfigService;

    private Project mockProject;
    private Module mockModule1;
    private Module mockModule2;

    @BeforeEach
    void setUp() {
        // 模拟项目
        mockProject = new Project();
        mockProject.setId(1L);
        mockProject.setProjectName("测试项目");
        mockProject.setTargetSystemId(100L);

        // 模拟模块1
        mockModule1 = new Module();
        mockModule1.setId(1L);
        mockModule1.setModuleName("财务管理");
        mockModule1.setSystemId(100L);
        mockModule1.setCategory("财务");
        mockModule1.setBaseWorkload(new BigDecimal("15"));
        mockModule1.setDefaultWeight(new BigDecimal("1.2"));
        mockModule1.setStatus(1);

        // 模拟模块2
        mockModule2 = new Module();
        mockModule2.setId(2L);
        mockModule2.setModuleName("供应链管理");
        mockModule2.setSystemId(100L);
        mockModule2.setCategory("采购");
        mockModule2.setBaseWorkload(new BigDecimal("20"));
        mockModule2.setDefaultWeight(new BigDecimal("1.5"));
        mockModule2.setStatus(1);
    }

    // ========== getProjectModules 测试 ==========

    @Nested
    @DisplayName("getProjectModules 测试 (MC-GET-*)")
    class GetProjectModulesTests {

        @Test
        @DisplayName("MC-GET-001: 获取项目可选模块列表")
        void shouldReturnAvailableModules() {
            // Given
            when(projectMapper.selectById(1L)).thenReturn(mockProject);
            when(moduleMapper.selectList(any())).thenReturn(Arrays.asList(mockModule1, mockModule2));
            when(moduleConfigMapper.selectList(any())).thenReturn(Collections.emptyList());

            // When
            List<ModuleConfigVO> result = moduleConfigService.getProjectModules(1L);

            // Then
            assertThat(result).hasSize(2);
            assertThat(result.get(0).getModuleName()).isEqualTo("财务管理");
            assertThat(result.get(0).getDefaultWeight()).isEqualByComparingTo("1.2");
            assertThat(result.get(0).getChecked()).isFalse();
            assertThat(result.get(1).getModuleName()).isEqualTo("供应链管理");
        }

        @Test
        @DisplayName("MC-GET-002: 项目不存在抛出异常")
        void shouldThrowWhenProjectNotFound() {
            // Given
            when(projectMapper.selectById(999L)).thenReturn(null);

            // When & Then
            assertThatThrownBy(() -> moduleConfigService.getProjectModules(999L))
                    .isInstanceOf(BusinessException.class);
        }

        @Test
        @DisplayName("MC-GET-003: 项目未设置目标系统抛出异常")
        void shouldThrowWhenTargetSystemNotSet() {
            // Given
            mockProject.setTargetSystemId(null);
            when(projectMapper.selectById(1L)).thenReturn(mockProject);

            // When & Then
            assertThatThrownBy(() -> moduleConfigService.getProjectModules(1L))
                    .isInstanceOf(BusinessException.class);
        }

        @Test
        @DisplayName("MC-GET-004: 返回已有配置时使用配置的系数")
        void shouldReturnConfiguredWeightWhenExists() {
            // Given
            ProjectModuleConfig existingConfig = new ProjectModuleConfig();
            existingConfig.setId(10L);
            existingConfig.setProjectId(1L);
            existingConfig.setModuleId(1L);
            existingConfig.setWeight(new BigDecimal("1.5"));

            when(projectMapper.selectById(1L)).thenReturn(mockProject);
            when(moduleMapper.selectList(any())).thenReturn(Arrays.asList(mockModule1, mockModule2));
            when(moduleConfigMapper.selectList(any())).thenReturn(Arrays.asList(existingConfig));

            // When
            List<ModuleConfigVO> result = moduleConfigService.getProjectModules(1L);

            // Then
            assertThat(result).hasSize(2);
            assertThat(result.get(0).getWeight()).isEqualByComparingTo("1.5"); // 使用配置的系数
            assertThat(result.get(0).getChecked()).isTrue();
        }
    }

    // ========== saveModuleConfig 测试 ==========

    @Nested
    @DisplayName("saveModuleConfig 测试 (MC-SAVE-*)")
    class SaveModuleConfigTests {

        @Test
        @DisplayName("MC-SAVE-001: 保存单个模块配置")
        void shouldSaveSingleModuleConfig() {
            // Given
            ModuleConfigDTO dto = new ModuleConfigDTO();
            dto.setModuleId(1L);
            dto.setWeight(new BigDecimal("1.3"));
            dto.setChecked(true);
            dto.setModuleName("财务管理");
            dto.setCategory("财务");
            dto.setBaseWorkload(new BigDecimal("15"));
            dto.setDefaultWeight(new BigDecimal("1.2"));

            when(moduleConfigMapper.selectList(any())).thenReturn(Collections.emptyList());
            when(moduleConfigMapper.insert(any(ProjectModuleConfig.class))).thenReturn(1);

            // When
            moduleConfigService.saveModuleConfig(1L, Arrays.asList(dto));

            // Then
            verify(moduleConfigMapper).insert(any(ProjectModuleConfig.class));
        }

        @Test
        @DisplayName("MC-SAVE-002: 保存多个模块配置")
        void shouldSaveMultipleModuleConfigs() {
            // Given
            ModuleConfigDTO dto1 = new ModuleConfigDTO();
            dto1.setModuleId(1L);
            dto1.setWeight(new BigDecimal("1.2"));
            dto1.setChecked(true);
            dto1.setModuleName("财务管理");
            dto1.setCategory("财务");
            dto1.setBaseWorkload(new BigDecimal("15"));
            dto1.setDefaultWeight(new BigDecimal("1.2"));

            ModuleConfigDTO dto2 = new ModuleConfigDTO();
            dto2.setModuleId(2L);
            dto2.setWeight(new BigDecimal("1.5"));
            dto2.setChecked(true);
            dto2.setModuleName("供应链管理");
            dto2.setCategory("采购");
            dto2.setBaseWorkload(new BigDecimal("20"));
            dto2.setDefaultWeight(new BigDecimal("1.5"));

            when(moduleConfigMapper.selectList(any())).thenReturn(Collections.emptyList());
            when(moduleConfigMapper.insert(any(ProjectModuleConfig.class))).thenReturn(1);

            // When
            moduleConfigService.saveModuleConfig(1L, Arrays.asList(dto1, dto2));

            // Then
            verify(moduleConfigMapper, times(2)).insert(any(ProjectModuleConfig.class));
        }

        @Test
        @DisplayName("MC-SAVE-003: 更新已有模块配置")
        void shouldUpdateExistingModuleConfig() {
            // Given
            ProjectModuleConfig existingConfig = new ProjectModuleConfig();
            existingConfig.setId(10L);
            existingConfig.setProjectId(1L);
            existingConfig.setModuleId(1L);
            existingConfig.setWeight(new BigDecimal("1.2"));

            ModuleConfigDTO dto = new ModuleConfigDTO();
            dto.setModuleId(1L);
            dto.setWeight(new BigDecimal("1.8")); // 更新为新系数
            dto.setChecked(true);

            when(moduleConfigMapper.selectList(any())).thenReturn(Arrays.asList(existingConfig));
            when(moduleConfigMapper.updateById(any(ProjectModuleConfig.class))).thenReturn(1);

            // When
            moduleConfigService.saveModuleConfig(1L, Arrays.asList(dto));

            // Then
            verify(moduleConfigMapper).updateById(any(ProjectModuleConfig.class));
            verify(moduleConfigMapper, never()).insert(any(ProjectModuleConfig.class));
        }

        @Test
        @DisplayName("MC-SAVE-004: 未选中模块应删除配置")
        void shouldDeleteUncheckedModuleConfig() {
            // Given - 同时包含一个选中的模块，这样至少有一个被选中
            ModuleConfigDTO uncheckedDto = new ModuleConfigDTO();
            uncheckedDto.setModuleId(1L);
            uncheckedDto.setWeight(new BigDecimal("1.2"));
            uncheckedDto.setChecked(false); // 未选中

            ModuleConfigDTO checkedDto = new ModuleConfigDTO();
            checkedDto.setModuleId(2L);
            checkedDto.setWeight(new BigDecimal("1.5"));
            checkedDto.setChecked(true); // 选中
            checkedDto.setModuleName("供应链管理");
            checkedDto.setCategory("采购");
            checkedDto.setBaseWorkload(new BigDecimal("20"));
            checkedDto.setDefaultWeight(new BigDecimal("1.5"));

            ProjectModuleConfig existingConfig = new ProjectModuleConfig();
            existingConfig.setId(10L);
            existingConfig.setProjectId(1L);
            existingConfig.setModuleId(1L);
            existingConfig.setWeight(new BigDecimal("1.2"));

            when(moduleConfigMapper.selectList(any())).thenReturn(Arrays.asList(existingConfig));
            when(moduleConfigMapper.deleteById(10L)).thenReturn(1);
            when(moduleConfigMapper.insert(any(ProjectModuleConfig.class))).thenReturn(1);

            // When
            moduleConfigService.saveModuleConfig(1L, Arrays.asList(uncheckedDto, checkedDto));

            // Then
            verify(moduleConfigMapper).deleteById(10L);
            verify(moduleConfigMapper).insert(any(ProjectModuleConfig.class)); // 插入选中的模块
        }

        @Test
        @DisplayName("MC-SAVE-005: 未选择任何模块抛出异常")
        void shouldThrowWhenNoModuleSelected() {
            // Given - 不需要mock，因为会先检查是否有选中的模块
            ModuleConfigDTO dto = new ModuleConfigDTO();
            dto.setModuleId(1L);
            dto.setWeight(new BigDecimal("1.2"));
            dto.setChecked(false);

            // When & Then
            assertThatThrownBy(() -> moduleConfigService.saveModuleConfig(1L, Arrays.asList(dto)))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("请至少选择一个模块");
        }

        @Test
        @DisplayName("MC-SAVE-006: 系数为0时应删除配置")
        void shouldDeleteModuleWhenWeightIsZero() {
            // Given - 同时包含一个选中的模块，通过验证
            ProjectModuleConfig existingConfig1 = new ProjectModuleConfig();
            existingConfig1.setId(10L);
            existingConfig1.setProjectId(1L);
            existingConfig1.setModuleId(1L);
            existingConfig1.setWeight(new BigDecimal("1.2"));

            // 未选中的模块（系数为0）
            ModuleConfigDTO uncheckedDto = new ModuleConfigDTO();
            uncheckedDto.setModuleId(1L);
            uncheckedDto.setWeight(BigDecimal.ZERO);
            uncheckedDto.setChecked(false);

            // 选中的模块（通过验证）
            ModuleConfigDTO checkedDto = new ModuleConfigDTO();
            checkedDto.setModuleId(2L);
            checkedDto.setWeight(new BigDecimal("1.5"));
            checkedDto.setChecked(true);
            checkedDto.setModuleName("供应链管理");
            checkedDto.setCategory("采购");
            checkedDto.setBaseWorkload(new BigDecimal("20"));
            checkedDto.setDefaultWeight(new BigDecimal("1.5"));

            when(moduleConfigMapper.selectList(any())).thenReturn(Arrays.asList(existingConfig1));
            when(moduleConfigMapper.deleteById(10L)).thenReturn(1);
            when(moduleConfigMapper.insert(any(ProjectModuleConfig.class))).thenReturn(1);

            // When
            moduleConfigService.saveModuleConfig(1L, Arrays.asList(uncheckedDto, checkedDto));

            // Then
            verify(moduleConfigMapper).deleteById(10L); // 删除了未选中的模块
            verify(moduleConfigMapper).insert(any(ProjectModuleConfig.class)); // 插入了选中的模块
        }
    }

    // ========== getConfiguredModules 测试 ==========

    @Nested
    @DisplayName("getConfiguredModules 测试 (MC-CONFIG-*)")
    class GetConfiguredModulesTests {

        @Test
        @DisplayName("MC-CONFIG-001: 获取已配置模块")
        void shouldReturnConfiguredModules() {
            // Given
            ProjectModuleConfig config = new ProjectModuleConfig();
            config.setId(10L);
            config.setProjectId(1L);
            config.setModuleId(1L);
            config.setWeight(new BigDecimal("1.3"));

            when(moduleConfigMapper.selectList(any())).thenReturn(Arrays.asList(config));
            when(moduleMapper.selectBatchIds(Arrays.asList(1L))).thenReturn(Arrays.asList(mockModule1));

            // When
            List<ModuleConfigVO> result = moduleConfigService.getConfiguredModules(1L);

            // Then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getModuleName()).isEqualTo("财务管理");
            assertThat(result.get(0).getWeight()).isEqualByComparingTo("1.3");
            assertThat(result.get(0).getChecked()).isTrue();
        }

        @Test
        @DisplayName("MC-CONFIG-002: 无已配置模块返回空列表")
        void shouldReturnEmptyListWhenNoConfig() {
            // Given
            when(moduleConfigMapper.selectList(any())).thenReturn(Collections.emptyList());

            // When
            List<ModuleConfigVO> result = moduleConfigService.getConfiguredModules(1L);

            // Then
            assertThat(result).isEmpty();
        }
    }

    // ========== clearModuleConfig 测试 ==========

    @Nested
    @DisplayName("clearModuleConfig 测试 (MC-CLEAR-*)")
    class ClearModuleConfigTests {

        @Test
        @DisplayName("MC-CLEAR-001: 清空模块配置")
        void shouldClearModuleConfig() {
            // Given
            when(moduleConfigMapper.delete(any())).thenReturn(1);

            // When
            moduleConfigService.clearModuleConfig(1L);

            // Then
            verify(moduleConfigMapper).delete(any());
        }
    }
}
