package com.bwabwayo.app.domain.user.service;

import com.bwabwayo.app.domain.user.domain.Account;
import com.bwabwayo.app.domain.user.domain.User;
import com.bwabwayo.app.domain.auth.dto.request.UserSignUpRequest;
import com.bwabwayo.app.domain.user.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AccountService {
    private final AccountRepository accountRepository;
    public void createAccount(User user, UserSignUpRequest request) {
        Account account = Account.builder()
                .user(user)
                .accountNumber(request.getAccountNumber())
                .accountHolder(request.getAccountHolder())
                .bankName(request.getBankName())
                .build();
        accountRepository.save(account);
    }

    public void updateAccount(User user, UserSignUpRequest request) {
        Account account = accountRepository.findByUser_Id(user.getId());
        account.setAccountNumber(request.getAccountNumber());
        account.setAccountHolder(request.getAccountHolder());
        account.setBankName(request.getBankName());
        accountRepository.save(account);
    }

    public Account getAccount(String userId){
        return accountRepository.findByUser_Id(userId);
    }

    public void saveAccount(Account account){ accountRepository.save(account); }
}
