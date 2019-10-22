package com.bank.test.integration;

 
import org.hamcrest.Matchers;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import com.bank.exceptions.ObjectModificationException;
import com.bank.model.BankAccount;
import com.bank.model.Transaction;
import com.bank.service.BankAccountService;
import com.bank.service.TransactionsService;

import io.qameta.allure.Description;

import java.math.BigDecimal;

import static org.hamcrest.MatcherAssert.assertThat;

public class ConcurrentlyTransactionCreationTest {
    private TransactionsService transactionsService = TransactionsService.getInstance();
    private BankAccountService bankAccountService = BankAccountService.getInstance();

    private static final BigDecimal INITIAL_BALANCE = BigDecimal.valueOf(1000L);
    private static final BigDecimal TRANSACTION_AMOUNT = BigDecimal.ONE;
    private static final int INVOCATION_COUNT = 10;

    private Long senderAccountId;
    private Long receiverAccountId;

    @BeforeClass
    public void initData() throws ObjectModificationException {
    	
        BankAccount senderAccount = new BankAccount( "New Owner1", INITIAL_BALANCE, BigDecimal.ZERO );
        BankAccount receiverAccount = new BankAccount( "New Owner2", BigDecimal.ZERO,  BigDecimal.ZERO  );
     

        senderAccountId = bankAccountService.createBankAccount(senderAccount).getId();
        receiverAccountId = bankAccountService.createBankAccount(receiverAccount).getId();
    }

    @Description ( "Test Description: Verify concurrent transaction creation only not execution")
    @Test(description = "Tests that concurrent transactions will successfully create but not execute" ,threadPoolSize = 100, invocationCount = INVOCATION_COUNT)
       public void testConcurrentTransactionCreation() throws ObjectModificationException {
        Transaction transaction = new Transaction(senderAccountId,receiverAccountId,TRANSACTION_AMOUNT);
        transactionsService.createTransaction(transaction);
    }

    @AfterClass
    public void checkResults() {
        BankAccount bankAccount = bankAccountService.getBankAccountById(senderAccountId);

        assertThat(bankAccount.getBalance(), Matchers.comparesEqualTo(INITIAL_BALANCE));
        assertThat(bankAccount.getBlockedAmount(),
                Matchers.comparesEqualTo(
                        BigDecimal.ZERO.add(
                            TRANSACTION_AMOUNT.multiply(BigDecimal.valueOf(INVOCATION_COUNT)))
                )
        );
    }
}
