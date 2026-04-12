package com.migrametric.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 用户更新DTO
 *
 * @author MigraMetric Team
 */
@Data
@Schema(description = "用户更新DTO")
public class UserUpdateDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 用户ID
     */
    @Schema(description = "用户ID")
    private Long id;

    /**
     * 用户姓名
     */
    @Size(max = 50, message = "用户姓名最多50个字符")
    @Schema(description = "用户姓名")
    private String name;

    /**
     * 邮箱
     */
    @Email(message = "邮箱格式不正确")
    @Size(max = 100, message = "邮箱最多100个字符")
    @Schema(description = "邮箱")
    private String email;

    /**
     * 手机号
     */
    @Size(max = 20, message = "手机号最多20个字符")
    @Schema(description = "手机号")
    private String phone;

    /**
     * 头像URL
     */
    @Size(max = 500, message = "头像URL最多500个字符")
    @Schema(description = "头像URL")
    private String avatar;

    /**
     * 角色：ADMIN-系统管理员，USER-普通用户
     */
    @Schema(description = "角色")
    private String role;

    /**
     * 状态：0-禁用，1-启用
     */
    @Schema(description = "状态")
    private Integer status;

    /**
     * 备注
     */
    @Size(max = 500, message = "备注最多500个字符")
    @Schema(description = "备注")
    private String remark;
}
