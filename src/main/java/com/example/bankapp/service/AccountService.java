package com.example.bankapp.service;


import com.example.bankapp.model.Account;
import com.example.bankapp.model.Transaction;
import com.example.bankapp.repository.AccountRepository;
import com.example.bankapp.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

@Service
public class AccountService implements UserDetailsService {


    PasswordEncoder passwordEncoder;


    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private TransactionRepository transactionRepository;

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


    public void deposit(Account account ,BigDecimal amount){

        account.setBalance(account.getBalance().add(amount));

        accountRepository.save(account);

        Transaction transcation =new Transaction(
                amount,
                "Deposit",
                LocalDateTime.now(),
                account
        );

        transactionRepository.save(transcation);
    }


    public void withdraw(Account account ,BigDecimal amount){

        if (account.getBalance().compareTo(amount)<0){
            throw new RuntimeException("Insufficient funds.");
        }
        account.setBalance(account.getBalance().subtract(amount));

        accountRepository.save(account);

        Transaction transcation =new Transaction(
                amount,
                "Withdraw",
                LocalDateTime.now(),
                account
        );

        transactionRepository.save(transcation);
    }

    public List<Transaction> getTranscationHistory(Account account){
        return transactionRepository.findByAccountId(account.getId());
    }


    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Account account =findAccountByUsername(username);
        if (account == null){
            throw new UsernameNotFoundException("Username or password not found ");
        }


        return new Account(
                account.getUsername(),
                account.getPassword(),
                account.getBalance(),
                account.getTransaction(),
                authorities()

        );
    }


    public Collection<? extends GrantedAuthority> authorities(){
        return Arrays.asList(new SimpleGrantedAuthority("User"));
    }

    public void transferAmount(Account fromAccount ,String toUsername,BigDecimal amount) {

        if (fromAccount.getBalance().compareTo(amount)<0){
            throw new RuntimeException("Insufficient funds");
        }

        Account toAccount = accountRepository.findByUsername(toUsername).orElseThrow(()-> new RuntimeException("Recipiten account not found"));

        fromAccount.setBalance(fromAccount.getBalance().subtract(amount));
        accountRepository.save(fromAccount);

        toAccount.setBalance(fromAccount.getBalance().add(amount));
        accountRepository.save(toAccount);

        Transaction debitTranscation =new Transaction(
                amount,
                "Transfer out to " + toAccount.getUsername(),
                LocalDateTime.now(),
                fromAccount
        );

        transactionRepository.save(debitTranscation);
        Transaction creditTranscation =new Transaction(
                amount,
                "Transfer in to " + fromAccount.getUsername(),
                LocalDateTime.now(),
                toAccount
        );

        transactionRepository.save(creditTranscation);


    }

    /*public List<Transaction> getTransactionHistory(Account account) {
        return transactionRepository.findAllByAccountIdOrderByTimestampDesc(account.getId());
    }*/


}
