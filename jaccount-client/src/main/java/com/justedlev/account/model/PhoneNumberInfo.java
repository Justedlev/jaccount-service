package com.justedlev.account.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class PhoneNumberInfo implements Serializable {
    private Long national;
    private String international;
    private Integer countryCode;
    private String regionCode;
}
