package com.justedlev.account.model.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PhoneNumberResponse {
    private Long national;
    private String international;
    private Integer countryCode;
    private String regionCode;
}
