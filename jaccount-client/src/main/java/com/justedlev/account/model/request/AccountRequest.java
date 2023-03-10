package com.justedlev.account.model.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.justedlev.account.converter.LowerCaseConverter;
import com.justedlev.account.enumeration.Gender;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.sql.Timestamp;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class AccountRequest {
    @Email
    @NotBlank
    @NotNull
    @JsonDeserialize(converter = LowerCaseConverter.class)
    private String email;
    @NotBlank
    @NotNull
    @JsonDeserialize(converter = LowerCaseConverter.class)
    private String nickname;
    private String firstName;
    private String lastName;
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Timestamp birthDate;
    private String phoneNumber;
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Gender gender;
}
