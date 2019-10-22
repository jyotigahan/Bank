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
import java.util.concurrent.atomic.AtomicInteger;

import static org.hamcrest.MatcherAssert.assertThat;

public class ConcurrentlyTransactionCreationAndExecutionTest {
    private TransactionsService transactionsService = TransactionsService.getInstance();
    private BankAccountService bankAccountService = BankAccountService.getInstance();

    private static final BigDecimal INITIAL_BALANCE = BigDecimal.valueOf(1000L);
    private static final BigDecimal TRANSACTION_AMOUNT = BigDecimal.ONE;
    private static final int INVOCATION_COUNT = 10;

    private Long senderAccountId;
    private Long receiverAccountId;
    private AtomicInteger invocationsDone = new AtomicInteger(0);

    @BeforeClass
    public void initData() throws ObjectModificationException {
    	
        BankAccount senderAccount = new BankAccount( "New Owner1", INITIAL_BALANCE, BigDecimal.ZERO );
        BankAccount receiverAccount = new BankAccount( "New Owner2", BigDecimal.ZERO,  BigDecimal.ZERO  );

        senderAccountId = bankAccountService.createBankAccount(senderAccount).getId();
        receiverAccountId = bankAccountService.createBankAccount(receiverAccount).getId();
    }

    @Description ( "Test Description: Verify concurrent transaction creation and execution")
    @Test(description = "Tests that concurrent transactions will successfully create and execute" ,threadPoolSize = 100, invocationCount = INVOCATION_COUNT)
    public void testConcurrentTransactionCreationAndExecution() throws ObjectModificationException {
        int currentTestNumber = invocationsDone.addAndGet(1);

        Transaction transaction = new Transaction( senderAccountId,  receiverAccountId, TRANSACTION_AMOUNT );

        transactionsService.createTransaction(transaction);

        if (currentTestNumber % 5 == 0) {
            transactionsService.executeTransactions();
        }
    }

    @AfterClass
    public void checkResults() {
        transactionsService.executeTransactions();
        BankAccount fromBankAccount = bankAccountService.getBankAccountById(senderAccountId);
        assertThat(fromBankAccount.getBalance(),
                Matchers.comparesEqualTo(
                        INITIAL_BALANCE.subtract(
                                TRANSACTION_AMOUNT.multiply(BigDecimal.valueOf(INVOCATION_COUNT)))
                )
        );
        assertThat(fromBankAccount.getBlockedAmount(), Matchers.comparesEqualTo(BigDecimal.ZERO));
    }
}
