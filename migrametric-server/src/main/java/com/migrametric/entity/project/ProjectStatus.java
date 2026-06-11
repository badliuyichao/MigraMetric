package com.migrametric.entity.project;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Arrays;

/**
 * 项目状态枚举（REQ-3.2.3）
 *
 * <pre>
 * 状态流转：
 *   DRAFT → IN_PROGRESS → COMPLETED → ARCHIVED
 * </pre>
 *
 * @author MigraMetric Team
 */
public enum ProjectStatus {

    /** 草稿：项目创建后默认 */
    DRAFT("DRAFT", "草稿"),

    /** 进行中：进入评估页面后 */
    IN_PROGRESS("IN_PROGRESS", "进行中"),

    /** 已完成：评估完成、报告已生成 */
    COMPLETED("COMPLETED", "已完成"),

    /** 已归档：项目归档后，只读状态 */
    ARCHIVED("ARCHIVED", "已归档");

    @EnumValue
    @JsonValue
    private final String code;

    private final String text;

    ProjectStatus(String code, String text) {
        this.code = code;
        this.text = text;
    }

    public String getCode() {
        return code;
    }

    public String getText() {
        return text;
    }

    @Override
    public String toString() {
        return code;
    }

    /**
     * JSON 反序列化用：接受 code 字符串或枚举名
     */
    @JsonCreator
    public static ProjectStatus fromCode(String value) {
        if (value == null) return null;
        return Arrays.stream(values())
                .filter(s -> s.code.equalsIgnoreCase(value) || s.name().equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("未知项目状态: " + value));
    }
}
