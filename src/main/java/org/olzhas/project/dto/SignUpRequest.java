package org.olzhas.project.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SignUpRequest {
    @Email(message = "Email is not valid")
    private String email;
    @Size(min = 8, message = "Password must be between 8 and 20 characters")
    private String password;
    @Size(max = 100)
    @NotBlank(message = "Username cannot be blank")
    private String username;
}
