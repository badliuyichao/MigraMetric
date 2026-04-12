package com.migrametric.dto.evaluation;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * EvaluationUpdateDTO 验证测试
 * <p>
 * 测试评估指标DTO的字段验证规则。
 * </p>
 *
 * @author MigraMetric Team
 */
@DisplayName("EvaluationUpdateDTO 验证测试")
class EvaluationUpdateDTOTest {

    private static Validator validator;

    @BeforeAll
    static void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    /**
     * 创建有效的DTO
     */
    private EvaluationUpdateDTO createValidDTO() {
        EvaluationUpdateDTO dto = new EvaluationUpdateDTO();
        dto.setTableCount(100);
        dto.setDataVolume(new BigDecimal("500"));
        dto.setDataVolumeLadderId(1L);
        dto.setUserCount(500);
        dto.setUserCountLadderId(1L);
        dto.setReportCount(50);
        dto.setHasCustomDev(true);
        dto.setCustomDevCount(2);
        dto.setCustomDevWorkload(new BigDecimal("20"));
        dto.setDataCleanDesc("测试描述");
        dto.setDataCleanComplexity(2);
        return dto;
    }

    @Nested
    @DisplayName("tableCount 验证 (EVAL-DTO-TC-*)")
    class TableCountValidationTests {

        @Test
        @DisplayName("EVAL-DTO-TC-001: 有效值通过验证")
        void shouldPassWhenValid() {
            EvaluationUpdateDTO dto = createValidDTO();
            Set<ConstraintViolation<EvaluationUpdateDTO>> violations = validator.validate(dto);
            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("EVAL-DTO-TC-002: tableCount为null时验证失败")
        void shouldFailWhenNull() {
            EvaluationUpdateDTO dto = createValidDTO();
            dto.setTableCount(null);

            Set<ConstraintViolation<EvaluationUpdateDTO>> violations = validator.validate(dto);

            assertThat(violations).isNotEmpty();
            assertThat(violations.stream().anyMatch(v -> v.getMessage().contains("数据库表数量不能为空"))).isTrue();
        }

        @Test
        @DisplayName("EVAL-DTO-TC-003: tableCount为负数时验证失败")
        void shouldFailWhenNegative() {
            EvaluationUpdateDTO dto = createValidDTO();
            dto.setTableCount(-1);

            Set<ConstraintViolation<EvaluationUpdateDTO>> violations = validator.validate(dto);

            assertThat(violations).isNotEmpty();
            assertThat(violations.stream().anyMatch(v -> v.getMessage().contains("不能为负数"))).isTrue();
        }

        @Test
        @DisplayName("EVAL-DTO-TC-004: tableCount为0时通过验证")
        void shouldPassWhenZero() {
            EvaluationUpdateDTO dto = createValidDTO();
            dto.setTableCount(0);

            Set<ConstraintViolation<EvaluationUpdateDTO>> violations = validator.validate(dto);

            assertThat(violations).isEmpty();
        }
    }

    @Nested
    @DisplayName("dataVolume 验证 (EVAL-DTO-DV-*)")
    class DataVolumeValidationTests {

        @Test
        @DisplayName("EVAL-DTO-DV-001: dataVolume为null时验证失败")
        void shouldFailWhenNull() {
            EvaluationUpdateDTO dto = createValidDTO();
            dto.setDataVolume(null);

            Set<ConstraintViolation<EvaluationUpdateDTO>> violations = validator.validate(dto);

            assertThat(violations).isNotEmpty();
            assertThat(violations.stream().anyMatch(v -> v.getMessage().contains("数据量不能为空"))).isTrue();
        }

        @Test
        @DisplayName("EVAL-DTO-DV-002: dataVolume为负数时验证失败")
        void shouldFailWhenNegative() {
            EvaluationUpdateDTO dto = createValidDTO();
            dto.setDataVolume(new BigDecimal("-100"));

            Set<ConstraintViolation<EvaluationUpdateDTO>> violations = validator.validate(dto);

            assertThat(violations).isNotEmpty();
            assertThat(violations.stream().anyMatch(v -> v.getMessage().contains("不能为负数"))).isTrue();
        }

        @Test
        @DisplayName("EVAL-DTO-DV-003: dataVolume为0时通过验证")
        void shouldPassWhenZero() {
            EvaluationUpdateDTO dto = createValidDTO();
            dto.setDataVolume(BigDecimal.ZERO);

            Set<ConstraintViolation<EvaluationUpdateDTO>> violations = validator.validate(dto);

            assertThat(violations).isEmpty();
        }
    }

    @Nested
    @DisplayName("userCount 验证 (EVAL-DTO-UC-*)")
    class UserCountValidationTests {

        @Test
        @DisplayName("EVAL-DTO-UC-001: userCount为null时验证失败")
        void shouldFailWhenNull() {
            EvaluationUpdateDTO dto = createValidDTO();
            dto.setUserCount(null);

            Set<ConstraintViolation<EvaluationUpdateDTO>> violations = validator.validate(dto);

            assertThat(violations).isNotEmpty();
            assertThat(violations.stream().anyMatch(v -> v.getMessage().contains("用户数量不能为空"))).isTrue();
        }

        @Test
        @DisplayName("EVAL-DTO-UC-002: userCount为负数时验证失败")
        void shouldFailWhenNegative() {
            EvaluationUpdateDTO dto = createValidDTO();
            dto.setUserCount(-10);

            Set<ConstraintViolation<EvaluationUpdateDTO>> violations = validator.validate(dto);

            assertThat(violations).isNotEmpty();
            assertThat(violations.stream().anyMatch(v -> v.getMessage().contains("不能为负数"))).isTrue();
        }
    }

    @Nested
    @DisplayName("reportCount 验证 (EVAL-DTO-RC-*)")
    class ReportCountValidationTests {

        @Test
        @DisplayName("EVAL-DTO-RC-001: reportCount为null时验证失败")
        void shouldFailWhenNull() {
            EvaluationUpdateDTO dto = createValidDTO();
            dto.setReportCount(null);

            Set<ConstraintViolation<EvaluationUpdateDTO>> violations = validator.validate(dto);

            assertThat(violations).isNotEmpty();
            assertThat(violations.stream().anyMatch(v -> v.getMessage().contains("报表数量不能为空"))).isTrue();
        }

        @Test
        @DisplayName("EVAL-DTO-RC-002: reportCount为负数时验证失败")
        void shouldFailWhenNegative() {
            EvaluationUpdateDTO dto = createValidDTO();
            dto.setReportCount(-5);

            Set<ConstraintViolation<EvaluationUpdateDTO>> violations = validator.validate(dto);

            assertThat(violations).isNotEmpty();
            assertThat(violations.stream().anyMatch(v -> v.getMessage().contains("不能为负数"))).isTrue();
        }

        @Test
        @DisplayName("EVAL-DTO-RC-003: reportCount为0时通过验证")
        void shouldPassWhenZero() {
            EvaluationUpdateDTO dto = createValidDTO();
            dto.setReportCount(0);

            Set<ConstraintViolation<EvaluationUpdateDTO>> violations = validator.validate(dto);

            assertThat(violations).isEmpty();
        }
    }

    @Nested
    @DisplayName("hasCustomDev 验证 (EVAL-DTO-CD-*)")
    class HasCustomDevValidationTests {

        @Test
        @DisplayName("EVAL-DTO-CD-001: hasCustomDev为null时验证失败")
        void shouldFailWhenNull() {
            EvaluationUpdateDTO dto = createValidDTO();
            dto.setHasCustomDev(null);

            Set<ConstraintViolation<EvaluationUpdateDTO>> violations = validator.validate(dto);

            assertThat(violations).isNotEmpty();
            assertThat(violations.stream().anyMatch(v -> v.getMessage().contains("是否有客开不能为空"))).isTrue();
        }

        @Test
        @DisplayName("EVAL-DTO-CD-002: hasCustomDev为false时通过验证")
        void shouldPassWhenFalse() {
            EvaluationUpdateDTO dto = createValidDTO();
            dto.setHasCustomDev(false);

            Set<ConstraintViolation<EvaluationUpdateDTO>> violations = validator.validate(dto);

            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("EVAL-DTO-CD-003: hasCustomDev为true时通过验证")
        void shouldPassWhenTrue() {
            EvaluationUpdateDTO dto = createValidDTO();
            dto.setHasCustomDev(true);

            Set<ConstraintViolation<EvaluationUpdateDTO>> violations = validator.validate(dto);

            assertThat(violations).isEmpty();
        }
    }

    @Nested
    @DisplayName("customDevWorkload 验证 (EVAL-DTO-CW-*)")
    class CustomDevWorkloadValidationTests {

        @Test
        @DisplayName("EVAL-DTO-CW-001: customDevWorkload为负数时验证失败")
        void shouldFailWhenNegative() {
            EvaluationUpdateDTO dto = createValidDTO();
            dto.setCustomDevWorkload(new BigDecimal("-10"));

            Set<ConstraintViolation<EvaluationUpdateDTO>> violations = validator.validate(dto);

            assertThat(violations).isNotEmpty();
            assertThat(violations.stream().anyMatch(v -> v.getMessage().contains("不能为负数"))).isTrue();
        }

        @Test
        @DisplayName("EVAL-DTO-CW-002: customDevWorkload为null时通过验证")
        void shouldPassWhenNull() {
            EvaluationUpdateDTO dto = createValidDTO();
            dto.setCustomDevWorkload(null);

            Set<ConstraintViolation<EvaluationUpdateDTO>> violations = validator.validate(dto);

            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("EVAL-DTO-CW-003: customDevWorkload为0时通过验证")
        void shouldPassWhenZero() {
            EvaluationUpdateDTO dto = createValidDTO();
            dto.setCustomDevWorkload(BigDecimal.ZERO);

            Set<ConstraintViolation<EvaluationUpdateDTO>> violations = validator.validate(dto);

            assertThat(violations).isEmpty();
        }
    }

    @Nested
    @DisplayName("dataCleanDesc 验证 (EVAL-DTO-DC-*)")
    class DataCleanDescValidationTests {

        @Test
        @DisplayName("EVAL-DTO-DC-001: dataCleanDesc超过2000字符时验证失败")
        void shouldFailWhenTooLong() {
            EvaluationUpdateDTO dto = createValidDTO();
            dto.setDataCleanDesc("a".repeat(2001));

            Set<ConstraintViolation<EvaluationUpdateDTO>> violations = validator.validate(dto);

            assertThat(violations).isNotEmpty();
            assertThat(violations.stream().anyMatch(v -> v.getMessage().contains("不能超过2000个字符"))).isTrue();
        }

        @Test
        @DisplayName("EVAL-DTO-DC-002: dataCleanDesc为null时通过验证")
        void shouldPassWhenNull() {
            EvaluationUpdateDTO dto = createValidDTO();
            dto.setDataCleanDesc(null);

            Set<ConstraintViolation<EvaluationUpdateDTO>> violations = validator.validate(dto);

            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("EVAL-DTO-DC-003: dataCleanDesc正好2000字符时通过验证")
        void shouldPassWhenExactly2000() {
            EvaluationUpdateDTO dto = createValidDTO();
            dto.setDataCleanDesc("a".repeat(2000));

            Set<ConstraintViolation<EvaluationUpdateDTO>> violations = validator.validate(dto);

            assertThat(violations).isEmpty();
        }
    }

    @Nested
    @DisplayName("dataCleanComplexity 验证 (EVAL-DTO-CC-*)")
    class DataCleanComplexityValidationTests {

        @Test
        @DisplayName("EVAL-DTO-CC-001: dataCleanComplexity为0时验证失败")
        void shouldFailWhenZero() {
            EvaluationUpdateDTO dto = createValidDTO();
            dto.setDataCleanComplexity(0);

            Set<ConstraintViolation<EvaluationUpdateDTO>> violations = validator.validate(dto);

            assertThat(violations).isNotEmpty();
            assertThat(violations.stream().anyMatch(v -> v.getMessage().contains("值无效"))).isTrue();
        }

        @Test
        @DisplayName("EVAL-DTO-CC-002: dataCleanComplexity为4时验证失败")
        void shouldFailWhenOutOfRange() {
            EvaluationUpdateDTO dto = createValidDTO();
            dto.setDataCleanComplexity(4);

            Set<ConstraintViolation<EvaluationUpdateDTO>> violations = validator.validate(dto);

            assertThat(violations).isNotEmpty();
            assertThat(violations.stream().anyMatch(v -> v.getMessage().contains("值无效"))).isTrue();
        }

        @Test
        @DisplayName("EVAL-DTO-CC-003: dataCleanComplexity为1时通过验证")
        void shouldPassWhenSimple() {
            EvaluationUpdateDTO dto = createValidDTO();
            dto.setDataCleanComplexity(1);

            Set<ConstraintViolation<EvaluationUpdateDTO>> violations = validator.validate(dto);

            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("EVAL-DTO-CC-004: dataCleanComplexity为2时通过验证")
        void shouldPassWhenMedium() {
            EvaluationUpdateDTO dto = createValidDTO();
            dto.setDataCleanComplexity(2);

            Set<ConstraintViolation<EvaluationUpdateDTO>> violations = validator.validate(dto);

            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("EVAL-DTO-CC-005: dataCleanComplexity为3时通过验证")
        void shouldPassWhenComplex() {
            EvaluationUpdateDTO dto = createValidDTO();
            dto.setDataCleanComplexity(3);

            Set<ConstraintViolation<EvaluationUpdateDTO>> violations = validator.validate(dto);

            assertThat(violations).isEmpty();
        }
    }

    @Nested
    @DisplayName("阶梯ID验证 (EVAL-DTO-LI-*)")
    class LadderIdValidationTests {

        @Test
        @DisplayName("EVAL-DTO-LI-001: dataVolumeLadderId为null时验证失败")
        void shouldFailWhenDataVolumeLadderIdNull() {
            EvaluationUpdateDTO dto = createValidDTO();
            dto.setDataVolumeLadderId(null);

            Set<ConstraintViolation<EvaluationUpdateDTO>> violations = validator.validate(dto);

            assertThat(violations).isNotEmpty();
            assertThat(violations.stream().anyMatch(v -> v.getMessage().contains("数据量阶梯ID不能为空"))).isTrue();
        }

        @Test
        @DisplayName("EVAL-DTO-LI-002: userCountLadderId为null时验证失败")
        void shouldFailWhenUserCountLadderIdNull() {
            EvaluationUpdateDTO dto = createValidDTO();
            dto.setUserCountLadderId(null);

            Set<ConstraintViolation<EvaluationUpdateDTO>> violations = validator.validate(dto);

            assertThat(violations).isNotEmpty();
            assertThat(violations.stream().anyMatch(v -> v.getMessage().contains("用户数阶梯ID不能为空"))).isTrue();
        }
    }
}
