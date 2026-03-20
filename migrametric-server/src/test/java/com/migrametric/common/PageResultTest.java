package com.migrametric.common;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * PageResult分页结果类单元测试
 *
 * @author MigraMetric Team
 */
@DisplayName("PageResult分页结果类测试")
class PageResultTest {

    @Test
    @DisplayName("PageResult 正确封装分页数据")
    void testPageResult() {
        // Given
        List<String> records = Arrays.asList("a", "b", "c");
        long total = 100;
        int pageNum = 1;
        int pageSize = 10;

        // When
        PageResult<String> pageResult = new PageResult<>(records, total, pageNum, pageSize);

        // Then
        assertEquals(3, pageResult.getRecords().size());
        assertEquals(100, pageResult.getTotal());
        assertEquals(1, pageResult.getPageNum());
        assertEquals(10, pageResult.getPageSize());
        assertEquals(10, pageResult.getTotalPages());
        assertFalse(pageResult.getHasPrevious()); // 第1页没有上一页
        assertTrue(pageResult.getHasNext());      // 第1页有下一页
        assertEquals(1, pageResult.getFromIndex());  // 第1条记录序号
        assertEquals(10, pageResult.getToIndex());   // 第10条记录序号
    }

    @Test
    @DisplayName("PageResult 首页无上一页")
    void testPageResultFirstPage() {
        // Given
        List<Integer> records = Arrays.asList(1, 2, 3);
        long total = 30;
        int pageNum = 1;
        int pageSize = 10;

        // When
        PageResult<Integer> pageResult = new PageResult<>(records, total, pageNum, pageSize);

        // Then
        assertFalse(pageResult.getHasPrevious());
        assertTrue(pageResult.getHasNext());
        assertEquals(1, pageResult.getPageNum());
    }

    @Test
    @DisplayName("PageResult 末页无下一页")
    void testPageResultLastPage() {
        // Given
        List<Integer> records = Arrays.asList(21, 22, 23);
        long total = 23;
        int pageNum = 3;
        int pageSize = 10;

        // When
        PageResult<Integer> pageResult = new PageResult<>(records, total, pageNum, pageSize);

        // Then
        assertTrue(pageResult.getHasPrevious());
        assertFalse(pageResult.getHasNext());
        assertEquals(3, pageResult.getTotalPages());
    }

    @Test
    @DisplayName("PageResult 单页无分页导航")
    void testPageResultSinglePage() {
        // Given
        List<String> records = Arrays.asList("a", "b");
        long total = 2;
        int pageNum = 1;
        int pageSize = 10;

        // When
        PageResult<String> pageResult = new PageResult<>(records, total, pageNum, pageSize);

        // Then
        assertEquals(1, pageResult.getTotalPages());
        assertFalse(pageResult.getHasPrevious());
        assertFalse(pageResult.getHasNext());
    }

    @Test
    @DisplayName("PageResult 空数据")
    void testPageResultEmpty() {
        // Given
        List<String> records = List.of();
        long total = 0;
        int pageNum = 1;
        int pageSize = 10;

        // When
        PageResult<String> pageResult = new PageResult<>(records, total, pageNum, pageSize);

        // Then
        assertEquals(0, pageResult.getRecords().size());
        assertEquals(0, pageResult.getTotal());
        assertEquals(0, pageResult.getTotalPages());
        assertFalse(pageResult.getHasPrevious());
        assertFalse(pageResult.getHasNext());
    }

    @Test
    @DisplayName("PageResult.totalPages计算正确")
    void testTotalPagesCalculation() {
        // Test case 1: 整除
        PageResult<String> page1 = new PageResult<>(List.of(), 20L, 1, 10);
        assertEquals(2, page1.getTotalPages());

        // Test case 2: 非整除
        PageResult<String> page2 = new PageResult<>(List.of(), 25L, 1, 10);
        assertEquals(3, page2.getTotalPages());

        // Test case 3: 边界值
        PageResult<String> page3 = new PageResult<>(List.of(), 10L, 1, 10);
        assertEquals(1, page3.getTotalPages());
    }

    @Test
    @DisplayName("PageResult.fromIndex和toIndex计算正确")
    void testIndexCalculation() {
        // Given - 第2页，每页10条
        List<String> records = Arrays.asList("k", "l");
        long total = 25;
        int pageNum = 2;
        int pageSize = 10;

        // When
        PageResult<String> pageResult = new PageResult<>(records, total, pageNum, pageSize);

        // Then
        assertEquals(11, pageResult.getFromIndex()); // (2-1)*10+1 = 11
        assertEquals(20, pageResult.getToIndex());  // min(2*10, 25) = 20
    }

    @Test
    @DisplayName("PageResult.empty() 创建空分页结果")
    void testEmptyPageResult() {
        // When
        PageResult<String> empty = PageResult.empty(1, 10);

        // Then
        assertTrue(empty.getRecords().isEmpty());
        assertEquals(0L, empty.getTotal());
        assertEquals(1, empty.getPageNum());
        assertEquals(10, empty.getPageSize());
    }
}
