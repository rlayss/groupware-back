package org.codenova.groupware.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ChangePassword {
    @NotBlank
    private String employeeId;
    @NotBlank
    private String oldPassword;

    @NotBlank
    @Pattern(regexp = "(?=.*[a-z])(?=.*[A-Z])(?=.*[0-9]).+")      // 정규표현식 = 문자열이 특정 형태에 부합하는지 확인할때 사용하는 표현식
    private String newPassword;
}
