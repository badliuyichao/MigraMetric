package com.migrametric.service.ladder;

import com.migrametric.service.ladder.impl.LadderMatchingServiceImpl;
import com.migrametric.vo.ladder.DataVolumeLadderVO;
import com.migrametric.vo.ladder.LadderMatchResultVO;
import com.migrametric.vo.ladder.UserCountLadderVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * LadderMatchingService 单元测试
 * <p>
 * 测试统一的阶梯匹配服务，包括数据量和用户数阶梯匹配。
 * </p>
 *
 * @author MigraMetric Team
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("LadderMatchingService 单元测试")
class LadderMatchingServiceTest {

    @Mock
    private DataVolumeLadderService dataVolumeLadderService;

    @Mock
    private UserCountLadderService userCountLadderService;

    @InjectMocks
    private LadderMatchingServiceImpl ladderMatchingService;

    // ========== 数据量阶梯匹配测试 ==========

    @Nested
    @DisplayName("matchDataVolume 测试 (LM-DV-*)")
    class MatchDataVolumeTests {

        @Test
        @DisplayName("LM-DV-001: 精确匹配数据量阶梯下限")
        void shouldMatchLowerBoundary() {
            // Given
            DataVolumeLadderVO mockLadder = new DataVolumeLadderVO();
            mockLadder.setId(2L);
            mockLadder.setLadderName("中型");
            mockLadder.setWeight(new BigDecimal("1.00"));
            mockLadder.setVolumeRangeText("100万-1000万条");

            when(dataVolumeLadderService.matchByVolume(new BigDecimal("100")))
                    .thenReturn(mockLadder);

            // When
            LadderMatchResultVO result = ladderMatchingService.matchDataVolume(new BigDecimal("100"));

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getLadderId()).isEqualTo(2L);
            assertThat(result.getLadderName()).isEqualTo("中型");
            assertThat(result.getWeight()).isEqualByComparingTo("1.00");
            assertThat(result.getLadderType()).isEqualTo("DATA_VOLUME");
            assertThat(result.getInputValue()).isEqualTo("100万条");
            assertThat(result.getRangeText()).isEqualTo("100万-1000万条");
        }

        @Test
        @DisplayName("LM-DV-002: 精确匹配数据量阶梯上限")
        void shouldMatchUpperBoundary() {
            // Given
            DataVolumeLadderVO mockLadder = new DataVolumeLadderVO();
            mockLadder.setId(2L);
            mockLadder.setLadderName("中型");
            mockLadder.setWeight(new BigDecimal("1.00"));
            mockLadder.setVolumeRangeText("100万-1000万条");

            when(dataVolumeLadderService.matchByVolume(new BigDecimal("999")))
                    .thenReturn(mockLadder);

            // When
            LadderMatchResultVO result = ladderMatchingService.matchDataVolume(new BigDecimal("999"));

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getLadderName()).isEqualTo("中型");
            assertThat(result.getWeight()).isEqualByComparingTo("1.00");
        }

        @Test
        @DisplayName("LM-DV-003: 超出最大阶梯范围")
        void shouldMatchUnlimitedLadder() {
            // Given
            DataVolumeLadderVO mockLadder = new DataVolumeLadderVO();
            mockLadder.setId(4L);
            mockLadder.setLadderName("超大型");
            mockLadder.setWeight(new BigDecimal("2.00"));
            mockLadder.setVolumeRangeText("1000万条以上");

            when(dataVolumeLadderService.matchByVolume(new BigDecimal("2000")))
                    .thenReturn(mockLadder);

            // When
            LadderMatchResultVO result = ladderMatchingService.matchDataVolume(new BigDecimal("2000"));

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getLadderName()).isEqualTo("超大型");
            assertThat(result.getWeight()).isEqualByComparingTo("2.00");
        }

        @Test
        @DisplayName("LM-DV-004: 小于最小阶梯范围")
        void shouldMatchMinLadder() {
            // Given
            DataVolumeLadderVO mockLadder = new DataVolumeLadderVO();
            mockLadder.setId(1L);
            mockLadder.setLadderName("小型");
            mockLadder.setWeight(new BigDecimal("0.80"));
            mockLadder.setVolumeRangeText("0万-100万条");

            when(dataVolumeLadderService.matchByVolume(new BigDecimal("5")))
                    .thenReturn(mockLadder);

            // When
            LadderMatchResultVO result = ladderMatchingService.matchDataVolume(new BigDecimal("5"));

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getLadderName()).isEqualTo("小型");
            assertThat(result.getWeight()).isEqualByComparingTo("0.80");
        }

        @Test
        @DisplayName("LM-DV-005: 空数据量返回null")
        void shouldReturnNullWhenVolumeIsNull() {
            // Given - 无需stub，因为service层在volume==null时直接返回null
            // When
            LadderMatchResultVO result = ladderMatchingService.matchDataVolume(null);

            // Then
            assertThat(result).isNull();
        }

        @Test
        @DisplayName("LM-DV-006: 阶梯无数据返回null")
        void shouldReturnNullWhenNoLadders() {
            // Given
            when(dataVolumeLadderService.matchByVolume(any())).thenReturn(null);

            // When
            LadderMatchResultVO result = ladderMatchingService.matchDataVolume(new BigDecimal("500"));

            // Then
            assertThat(result).isNull();
        }
    }

    // ========== 用户数阶梯匹配测试 ==========

    @Nested
    @DisplayName("matchUserCount 测试 (LM-UC-*)")
    class MatchUserCountTests {

        @Test
        @DisplayName("LM-UC-001: 精确匹配用户数阶梯下限")
        void shouldMatchLowerBoundary() {
            // Given
            UserCountLadderVO mockLadder = new UserCountLadderVO();
            mockLadder.setId(2L);
            mockLadder.setLadderName("中规模");
            mockLadder.setWeight(new BigDecimal("1.20"));
            mockLadder.setCountRangeText("100人-500人");

            when(userCountLadderService.matchByCount(100)).thenReturn(mockLadder);

            // When
            LadderMatchResultVO result = ladderMatchingService.matchUserCount(100);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getLadderId()).isEqualTo(2L);
            assertThat(result.getLadderName()).isEqualTo("中规模");
            assertThat(result.getWeight()).isEqualByComparingTo("1.20");
            assertThat(result.getLadderType()).isEqualTo("USER_COUNT");
            assertThat(result.getInputValue()).isEqualTo("100人");
            assertThat(result.getRangeText()).isEqualTo("100人-500人");
        }

        @Test
        @DisplayName("LM-UC-002: 精确匹配用户数阶梯上限")
        void shouldMatchUpperBoundary() {
            // Given
            UserCountLadderVO mockLadder = new UserCountLadderVO();
            mockLadder.setId(2L);
            mockLadder.setLadderName("中规模");
            mockLadder.setWeight(new BigDecimal("1.20"));
            mockLadder.setCountRangeText("100人-500人");

            when(userCountLadderService.matchByCount(499)).thenReturn(mockLadder);

            // When
            LadderMatchResultVO result = ladderMatchingService.matchUserCount(499);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getLadderName()).isEqualTo("中规模");
        }

        @Test
        @DisplayName("LM-UC-003: 超出最大用户数阶梯")
        void shouldMatchUnlimitedLadder() {
            // Given
            UserCountLadderVO mockLadder = new UserCountLadderVO();
            mockLadder.setId(4L);
            mockLadder.setLadderName("超大规模");
            mockLadder.setWeight(new BigDecimal("2.00"));
            mockLadder.setCountRangeText("1000人以上");

            when(userCountLadderService.matchByCount(2000)).thenReturn(mockLadder);

            // When
            LadderMatchResultVO result = ladderMatchingService.matchUserCount(2000);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getLadderName()).isEqualTo("超大规模");
            assertThat(result.getWeight()).isEqualByComparingTo("2.00");
        }

        @Test
        @DisplayName("LM-UC-004: 小于最小用户数阶梯")
        void shouldMatchMinLadder() {
            // Given
            UserCountLadderVO mockLadder = new UserCountLadderVO();
            mockLadder.setId(1L);
            mockLadder.setLadderName("小规模");
            mockLadder.setWeight(new BigDecimal("1.00"));
            mockLadder.setCountRangeText("0人-100人");

            when(userCountLadderService.matchByCount(50)).thenReturn(mockLadder);

            // When
            LadderMatchResultVO result = ladderMatchingService.matchUserCount(50);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getLadderName()).isEqualTo("小规模");
        }

        @Test
        @DisplayName("LM-UC-005: 空用户数返回null")
        void shouldReturnNullWhenCountIsNull() {
            // Given - 无需stub，因为service层在count==null时直接返回null
            // When
            LadderMatchResultVO result = ladderMatchingService.matchUserCount(null);

            // Then
            assertThat(result).isNull();
        }

        @Test
        @DisplayName("LM-UC-006: 用户数为0匹配")
        void shouldMatchZeroCount() {
            // Given
            UserCountLadderVO mockLadder = new UserCountLadderVO();
            mockLadder.setId(1L);
            mockLadder.setLadderName("小规模");
            mockLadder.setWeight(new BigDecimal("1.00"));
            mockLadder.setCountRangeText("0人-100人");

            when(userCountLadderService.matchByCount(0)).thenReturn(mockLadder);

            // When
            LadderMatchResultVO result = ladderMatchingService.matchUserCount(0);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getInputValue()).isEqualTo("0人");
        }
    }

    // ========== 系数获取测试 ==========

    @Nested
    @DisplayName("getDataVolumeWeight 测试 (LM-EX-*)")
    class GetDataVolumeWeightTests {

        @Test
        @DisplayName("LM-EX-003: 阶梯系数精度验证")
        void shouldReturnCorrectWeightPrecision() {
            // Given
            DataVolumeLadderVO mockLadder = new DataVolumeLadderVO();
            mockLadder.setWeight(new BigDecimal("1.5678"));

            when(dataVolumeLadderService.matchByVolume(new BigDecimal("500")))
                    .thenReturn(mockLadder);

            // When
            BigDecimal weight = ladderMatchingService.getDataVolumeWeight(new BigDecimal("500"));

            // Then
            assertThat(weight).isEqualByComparingTo("1.5678");
        }

        @Test
        @DisplayName("LM-EX-006: 无阶梯配置时返回null")
        void shouldReturnNullWhenNoLadder() {
            // Given
            when(dataVolumeLadderService.matchByVolume(any())).thenReturn(null);

            // When
            BigDecimal weight = ladderMatchingService.getDataVolumeWeight(new BigDecimal("500"));

            // Then
            assertThat(weight).isNull();
        }
    }

    @Nested
    @DisplayName("getUserCountWeight 测试")
    class GetUserCountWeightTests {

        @Test
        @DisplayName("LM-EX-003: 用户数阶梯系数精度验证")
        void shouldReturnCorrectWeightPrecision() {
            // Given
            UserCountLadderVO mockLadder = new UserCountLadderVO();
            mockLadder.setWeight(new BigDecimal("1.5678"));

            when(userCountLadderService.matchByCount(300)).thenReturn(mockLadder);

            // When
            BigDecimal weight = ladderMatchingService.getUserCountWeight(300);

            // Then
            assertThat(weight).isEqualByComparingTo("1.5678");
        }

        @Test
        @DisplayName("无阶梯配置时返回null")
        void shouldReturnNullWhenNoLadder() {
            // Given
            when(userCountLadderService.matchByCount(any())).thenReturn(null);

            // When
            BigDecimal weight = ladderMatchingService.getUserCountWeight(300);

            // Then
            assertThat(weight).isNull();
        }
    }
}
