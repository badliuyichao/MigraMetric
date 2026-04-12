package com.migrametric.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 用户创建DTO
 *
 * @author MigraMetric Team
 */
@Data
@Schema(description = "用户创建DTO")
public class UserCreateDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 用户名（登录账号）
     */
    @NotBlank(message = "用户名不能为空")
    @Size(min = 3, max = 50, message = "用户名长度为3-50个字符")
    @Schema(description = "用户名")
    private String username;

    /**
     * 密码
     */
    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 100, message = "密码长度为6-100个字符")
    @Schema(description = "密码")
    private String password;

    /**
     * 用户姓名
     */
    @NotBlank(message = "用户姓名不能为空")
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
     * 角色：ADMIN-系统管理员，USER-普通用户
     */
    @NotBlank(message = "角色不能为空")
    @Schema(description = "角色")
    private String role;

    /**
     * 备注
     */
    @Size(max = 500, message = "备注最多500个字符")
    @Schema(description = "备注")
    private String remark;
}
