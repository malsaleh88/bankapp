package com.example.bankapp.service;


import com.example.bankapp.model.Account;
import com.example.bankapp.repository.AccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class AccountService implements UserDetailsService {


    PasswordEncoder passwordEncoder;


    @Autowired
    private AccountRepository accountRepository;

    public Account findAccountByUsername(String username){
        return accountRepository.findByUsername(username).orElseThrow(()->new RuntimeException("Account not found"));
    }

    public Account registerAccount(String username,String password){

        if (accountRepository.findByUsername(username).isPresent()){
            throw new RuntimeException("Username already exist.");
        }

        Account account =new Account();
        account.setUsername(username);
        account.setPassword(passwordEncoder.encode(password));
        account.setBalance(BigDecimal.ZERO);
        return accountRepository.save(account);
    }
}
