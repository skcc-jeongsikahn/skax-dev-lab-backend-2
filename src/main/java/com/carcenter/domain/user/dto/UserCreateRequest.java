package com.carcenter.domain.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Schema(description = "사용자 생성 요청")
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UserCreateRequest {

    @Schema(description = "사용자명", example = "user123", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "사용자명은 필수입니다")
    @Size(min = 3, max = 20, message = "사용자명은 3-20자 사이여야 합니다")
    private String username;

    @Schema(description = "이메일 주소", example = "user@example.com", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "이메일은 필수입니다")
    @Email(message = "올바른 이메일 형식이 아닙니다")
    private String email;

    @Schema(description = "비밀번호", example = "password123", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "비밀번호는 필수입니다")
    @Size(min = 8, max = 20, message = "비밀번호는 8-20자 사이여야 합니다")
    private String password;

    @Schema(description = "사용자 이름", example = "홍길동", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "이름은 필수입니다")
    @Size(max = 50, message = "이름은 50자를 초과할 수 없습니다")
    private String name;

    @Schema(description = "전화번호", example = "010-1234-5678", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @Size(max = 20, message = "전화번호는 20자를 초과할 수 없습니다")
    private String phone;
}
