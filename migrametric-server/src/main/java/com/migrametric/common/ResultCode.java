package com.migrametric.common;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 结果码枚举
 *
 * @author MigraMetric Team
 */
@Getter
@AllArgsConstructor
public enum ResultCode {

    /* 成功状态码 */
    SUCCESS(200, "操作成功"),

    /* 客户端错误码 4xx */
    BAD_REQUEST(400, "请求参数错误"),
    UNAUTHORIZED(401, "未授权，请先登录"),
    FORBIDDEN(403, "暂无权限访问该资源"),
    NOT_FOUND(404, "请求的资源不存在"),
    METHOD_NOT_ALLOWED(405, "请求方法不被允许"),

    /* 服务器错误码 5xx */
    INTERNAL_SERVER_ERROR(500, "服务器内部错误"),
    SERVICE_UNAVAILABLE(503, "服务暂不可用"),

    /* 业务错误码 1xxx */
    BUSINESS_ERROR(1001, "业务处理异常"),

    /* 认证错误码 10xx */
    USERNAME_PASSWORD_ERROR(10001, "用户名或密码错误"),
    TOKEN_INVALID(10002, "Token无效"),
    TOKEN_EXPIRED(10003, "Token已过期"),
    TOKEN_NOT_FOUND(10004, "未找到Token"),
    USER_DISABLED(10005, "用户已被禁用"),
    USER_LOCKED(10006, "用户已被锁定"),

    /* 权限错误码 11xx */
    PERMISSION_DENIED(11001, "权限不足"),

    /* 业务校验错误码 12xx */
    VALIDATION_ERROR(12001, "数据校验失败"),
    DATA_NOT_FOUND(12002, "数据不存在"),
    DATA_DUPLICATE(12003, "数据重复"),

    /* 文件错误码 13xx */
    FILE_UPLOAD_ERROR(13001, "文件上传失败"),
    FILE_NOT_FOUND(13002, "文件不存在"),

    /* 业务特定错误码 */
    PROJECT_NOT_FOUND(20001, "项目不存在"),
    PROJECT_NOT_DRAFT(20002, "只有草稿状态的项目可以删除"),
    SOURCE_TARGET_SAME(20003, "源系统和目标系统不能相同"),
    MODULE_NOT_FOUND(30001, "模块不存在"),
    AT_LEAST_ONE_MODULE(30002, "至少选择一个模块"),
    LADDER_NOT_FOUND(40001, "阶梯配置不存在"),
    SYSTEM_TYPE_NOT_FOUND(50001, "系统类型不存在"),
    DATA_NOT_FOUND_ALT(50002, "数据不存在"),
    DATA_ALREADY_EXISTS(50003, "数据已存在"),
    DATA_REFERENCE_EXISTS(50004, "数据被引用，无法操作"),
    PARAM_INVALID(50005, "参数无效");

    private final Integer code;
    private final String message;
}
