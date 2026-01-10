package com.backend.cypherflow.util;

import com.backend.cypherflow.entity.User;
import com.backend.cypherflow.exception.InactiveAccountException;
import com.backend.cypherflow.exception.UnverfiedAccountException;
import org.springframework.stereotype.Component;

@Component
public class AccountStatusValidator {

    public void validate(User user) {

        switch (user.getAccountStatus())
        {
            case ACTIVE -> {
                // allowed
            }
            case PENDING_VERIFICATION ->
                    throw new UnverfiedAccountException("Account not verified. Complete verification first");

            case LOCKED -> throw new InactiveAccountException("Account is locked");

            case BLOCKED -> throw new InactiveAccountException("Account is blocked");

            default ->
                    throw new InactiveAccountException("Account is not accessible");
        }
    }
}
