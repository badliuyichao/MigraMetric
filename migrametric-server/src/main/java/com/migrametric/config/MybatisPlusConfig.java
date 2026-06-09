package com.migrametric.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MyBatis-Plus 配置
 *
 * 启用分页拦截器（PaginationInnerInterceptor）。
 *
 * 原因（2026-06-09 REQ-3.1.10 模块库分页任务排查发现）：
 * 未配置此拦截器时，所有 Service 调 `BaseMapper.selectPage(page, wrapper)`
 * 返回的 `IPage.getTotal()` 永远为 0（MyBatis-Plus 3.5.x 不自动注册分页拦截器），
 * 前端 el-pagination 拿不到 total，totalPages 错，分页 UI 不可用。
 *
 * 业务影响：模块库、项目、用户、登录日志、操作日志、系统类型等 7 处分页接口
 * 均受影响。之前 E2E 覆盖度没锁死 total 数值所以没暴露。
 */
@Configuration
public class MybatisPlusConfig {

    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));
        return interceptor;
    }
}
