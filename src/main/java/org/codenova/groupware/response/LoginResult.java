package org.codenova.groupware.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.codenova.groupware.entity.Employee;

@Setter
@Getter
@Builder
public class LoginResult {
    private String token;
    private Employee employee;
}
