package com.migrametric.service.config;

import com.migrametric.common.BusinessException;
import com.migrametric.dto.config.ReportConfigUpdateDTO;
import com.migrametric.entity.config.ReportConfig;
import com.migrametric.mapper.config.ReportConfigMapper;
import com.migrametric.service.config.impl.ReportConfigServiceImpl;
import com.migrametric.vo.config.ReportConfigVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 报表配置服务单元测试
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("报表配置服务测试")
class ReportConfigServiceTest {

    @Mock
    private ReportConfigMapper configMapper;

    @InjectMocks
    private ReportConfigServiceImpl configService;

    private ReportConfig sampleConfig;

    @BeforeEach
    void setUp() {
        sampleConfig = new ReportConfig();
        sampleConfig.setId(1L);
        sampleConfig.setConfigKey("REPORT_MIGRATION_COEFFICIENT");
        sampleConfig.setConfigValue("2.5");
        sampleConfig.setConfigName("报表迁移系数");
        sampleConfig.setDescription("每个报表迁移所需的工作量（人天/个）");
        sampleConfig.setStatus(1);
        sampleConfig.setCreateTime(LocalDateTime.now());
        sampleConfig.setCreateBy("admin");
    }

    @Nested
    @DisplayName("listAll 测试")
    class ListAllTests {

        @Test
        @DisplayName("应返回所有配置并按配置键排序")
        void shouldReturnAllConfigsSortedByConfigKey() {
            ReportConfig config1 = createConfig(1L, "AAA_COEFFICIENT", "1.0");
            ReportConfig config2 = createConfig(2L, "BBB_COEFFICIENT", "2.0");
            when(configMapper.selectList(any())).thenReturn(Arrays.asList(config1, config2));

            List<ReportConfigVO> result = configService.listAll();

            assertThat(result).hasSize(2);
            assertThat(result.get(0).getConfigKey()).isEqualTo("AAA_COEFFICIENT");
            assertThat(result.get(1).getConfigKey()).isEqualTo("BBB_COEFFICIENT");
        }

        @Test
        @DisplayName("配置列表为空时返回空列表")
        void shouldReturnEmptyListWhenNoConfigs() {
            when(configMapper.selectList(any())).thenReturn(Collections.emptyList());

            List<ReportConfigVO> result = configService.listAll();

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("VO 应包含正确的状态文本")
        void shouldIncludeCorrectStatusText() {
            ReportConfig enabled = createConfig(1L, "ENABLED_KEY", "1.0");
            enabled.setStatus(1);
            ReportConfig disabled = createConfig(2L, "DISABLED_KEY", "2.0");
            disabled.setStatus(0);
            when(configMapper.selectList(any())).thenReturn(Arrays.asList(enabled, disabled));

            List<ReportConfigVO> result = configService.listAll();

            assertThat(result.get(0).getStatusText()).isEqualTo("启用");
            assertThat(result.get(1).getStatusText()).isEqualTo("禁用");
        }

        private ReportConfig createConfig(Long id, String key, String value) {
            ReportConfig config = new ReportConfig();
            config.setId(id);
            config.setConfigKey(key);
            config.setConfigValue(value);
            config.setConfigName("测试配置");
            config.setDescription("测试描述");
            config.setStatus(1);
            config.setCreateTime(LocalDateTime.now());
            config.setCreateBy("admin");
            return config;
        }
    }

    @Nested
    @DisplayName("getValueByKey 测试")
    class GetValueByKeyTests {

        @Test
        @DisplayName("应返回启用状态配置的值")
        void shouldReturnValueForEnabledConfig() {
            when(configMapper.selectByConfigKey("REPORT_MIGRATION_COEFFICIENT")).thenReturn(sampleConfig);

            String result = configService.getValueByKey("REPORT_MIGRATION_COEFFICIENT");

            assertThat(result).isEqualTo("2.5");
        }

        @Test
        @DisplayName("配置键为空时返回 null")
        void shouldReturnNullWhenConfigKeyIsBlank() {
            String result = configService.getValueByKey("");

            assertThat(result).isNull();
        }

        @Test
        @DisplayName("配置不存在时返回 null")
        void shouldReturnNullWhenConfigNotFound() {
            when(configMapper.selectByConfigKey("NON_EXISTENT")).thenReturn(null);

            String result = configService.getValueByKey("NON_EXISTENT");

            assertThat(result).isNull();
        }

        @Test
        @DisplayName("配置已禁用时返回 null")
        void shouldReturnNullWhenConfigIsDisabled() {
            sampleConfig.setStatus(0);
            when(configMapper.selectByConfigKey("REPORT_MIGRATION_COEFFICIENT")).thenReturn(sampleConfig);

            String result = configService.getValueByKey("REPORT_MIGRATION_COEFFICIENT");

            assertThat(result).isNull();
        }
    }

    @Nested
    @DisplayName("getValueAsBigDecimal 测试")
    class GetValueAsBigDecimalTests {

        @Test
        @DisplayName("应正确转换为 BigDecimal")
        void shouldConvertToBigDecimalCorrectly() {
            when(configMapper.selectByConfigKey("REPORT_MIGRATION_COEFFICIENT")).thenReturn(sampleConfig);

            BigDecimal result = configService.getValueAsBigDecimal("REPORT_MIGRATION_COEFFICIENT", null);

            assertThat(result).isEqualByComparingTo(new BigDecimal("2.5"));
        }

        @Test
        @DisplayName("配置值无效时返回默认值")
        void shouldReturnDefaultWhenValueIsInvalid() {
            sampleConfig.setConfigValue("invalid");
            when(configMapper.selectByConfigKey("REPORT_MIGRATION_COEFFICIENT")).thenReturn(sampleConfig);

            BigDecimal result = configService.getValueAsBigDecimal("REPORT_MIGRATION_COEFFICIENT", new BigDecimal("1.0"));

            assertThat(result).isEqualByComparingTo(new BigDecimal("1.0"));
        }

        @Test
        @DisplayName("配置不存在时返回默认值")
        void shouldReturnDefaultWhenConfigNotFound() {
            when(configMapper.selectByConfigKey("NON_EXISTENT")).thenReturn(null);

            BigDecimal result = configService.getValueAsBigDecimal("NON_EXISTENT", new BigDecimal("3.0"));

            assertThat(result).isEqualByComparingTo(new BigDecimal("3.0"));
        }
    }

    @Nested
    @DisplayName("update 测试")
    class UpdateTests {

        @Test
        @DisplayName("应成功更新配置值")
        void shouldUpdateConfigValueSuccessfully() {
            ReportConfigUpdateDTO updateDTO = new ReportConfigUpdateDTO();
            updateDTO.setId(1L);
            updateDTO.setConfigKey("REPORT_MIGRATION_COEFFICIENT");
            updateDTO.setConfigValue("3.0");
            when(configMapper.selectByConfigKey("REPORT_MIGRATION_COEFFICIENT")).thenReturn(sampleConfig);

            configService.update(updateDTO);

            verify(configMapper).updateById(any(ReportConfig.class));
        }

        @Test
        @DisplayName("配置不存在时抛出异常")
        void shouldThrowExceptionWhenConfigNotFound() {
            ReportConfigUpdateDTO updateDTO = new ReportConfigUpdateDTO();
            updateDTO.setId(999L);
            updateDTO.setConfigKey("NON_EXISTENT");
            updateDTO.setConfigValue("3.0");
            when(configMapper.selectByConfigKey("NON_EXISTENT")).thenReturn(null);

            assertThatThrownBy(() -> configService.update(updateDTO))
                    .isInstanceOf(BusinessException.class);
        }
    }
}
