package com.justedlev.account.boot;

import com.justedlev.account.common.converter.PhoneNumberConverter;
import com.justedlev.account.component.AccountComponent;
import com.justedlev.account.enumeration.AccountStatusCode;
import com.justedlev.account.enumeration.Gender;
import com.justedlev.account.enumeration.ModeType;
import com.justedlev.account.repository.entity.Account;
import com.justedlev.account.repository.entity.Contact;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@Order(value = 10)
@RequiredArgsConstructor
public class RandomAccountsBoot implements ApplicationRunner {
    private static final String SYMBOLS = "QWERTYUIOPASDFGHJKLZXCVBNMqwertyuiopasdfghjklzxcvbnm1234567890";
    private static final Boolean FILL = Boolean.TRUE;
    private final AccountComponent accountComponent;
    private final PhoneNumberConverter phoneNumberConverter;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        if (Boolean.TRUE.equals(FILL)) {
            var accountStatuses = AccountStatusCode.values();
            var modes = ModeType.values();
            var genders = Gender.values();
            var count = RandomUtils.nextInt(10, 50);
            var countries = new int[]{1202, 97253};

            List<Account> list = new ArrayList<>();
            for (int i = 0; i < count; i++) {
                var phone = phoneNumberConverter.convert("+" + countries[getRandomIndex(countries.length)] + RandomUtils.nextInt(1000000, 9999999));
                var nickname = RandomStringUtils.random(RandomUtils.nextInt(4, 9), SYMBOLS);
                var contact = Contact.builder()
                        .phoneNumber(phone)
                        .email(nickname + "@mail.co")
                        .main(true)
                        .build();
                var account = Account.builder()
                        .nickname(nickname)
                        .status(accountStatuses[getRandomIndex(accountStatuses.length)])
                        .mode(modes[getRandomIndex(modes.length)])
                        .gender(genders[getRandomIndex(genders.length)])
                        .firstName(RandomStringUtils.randomAlphanumeric(4, 8))
                        .lastName(RandomStringUtils.randomAlphanumeric(4, 8))
                        .createdAt(new Timestamp(RandomUtils.nextLong(System.currentTimeMillis() - TimeUnit.DAYS.toMillis(3), System.currentTimeMillis())))
                        .contacts(Set.of(contact))
                        .build();
                list.add(account);
            }

            var res = accountComponent.saveAll(list);
            log.info("Created {} random accounts", res.size());
        }
    }

    private int getRandomIndex(int length) {
        return RandomUtils.nextInt(0, length);
    }
}
