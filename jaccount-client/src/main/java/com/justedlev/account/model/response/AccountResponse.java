package com.justedlev.account.model.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.justedlev.account.enumeration.AccountStatusCode;
import com.justedlev.account.enumeration.Gender;
import com.justedlev.account.enumeration.ModeType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountResponse {
    private String nickname;
    private String firstName;
    private String lastName;
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Date birthDate;
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Gender gender;
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private AccountStatusCode status;
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private ModeType mode;
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Date registrationDate;
    private String avatarUrl;
    private ContactResponse mainContact;
    private Set<ContactResponse> contacts;
}
