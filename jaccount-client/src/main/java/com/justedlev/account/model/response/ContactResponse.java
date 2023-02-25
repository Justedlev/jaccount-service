package com.justedlev.account.model.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContactResponse {
    private boolean main = Boolean.FALSE;
    private String email;
    private PhoneNumberResponse phoneNumber;
}
