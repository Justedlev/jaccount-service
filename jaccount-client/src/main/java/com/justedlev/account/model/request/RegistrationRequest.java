package com.justedlev.account.model.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class RegistrationRequest {
    @NotBlank(message = "Input email cannot be empty.")
    @Email(message = "Email should be valid.")
    private String email;
    @Size(min = 8, message = "Password must be more than 8 characters.")
    @NotBlank(message = "Input password cannot be empty.")
    private String password;
    @NotBlank(message = "Input nickname cannot be empty.")
    private String nickname;
}
