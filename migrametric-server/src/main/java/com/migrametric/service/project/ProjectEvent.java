package com.migrametric.service.project;

/**
 * 项目状态流转事件（REQ-3.2.3）
 *
 * @author MigraMetric Team
 */
public enum ProjectEvent {

    /** 项目创建（写入首条 CREATE 历史，N/A→DRAFT） */
    CREATE,

    /** 进入评估（草稿 → 进行中） */
    EVAL_START,

    /** 完成评估（进行中 → 已完成） */
    EVAL_COMPLETE,

    /** 归档（已完成 → 已归档） */
    ARCHIVE,

    /** 人工补录（仅 ADMIN 入口，状态机不参与） */
    MANUAL_EDIT
}
