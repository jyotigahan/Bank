package com.bank.test.controller;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.testng.Assert.assertNotEquals;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Random;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.hamcrest.Matchers;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.bank.controller.BankAccountsController;
import com.bank.BankTransactionApp;
import com.bank.dao.BankAccountDao;
import com.bank.model.BankAccount;
import com.bank.service.BankAccountService;

import io.qameta.allure.Description;
import spark.Spark;
 

public class BankAccountControllerTest {
    private static WebTarget target;

    @BeforeClass
    public static void beforeAll() {
    	BankTransactionApp.main(null);
        Client c = ClientBuilder.newClient();
        target = c.target("http://localhost:4567");
    }

    @AfterClass
    public static void afterAll() {
		Spark.stop();
    }

    @Description ( "Test Description: Verify successful creation of new bank account")
	@Test(description="Tests that method will successfully create new bank account in database")
    public void testCreateBankAccount() {
    	
        BankAccountService bankAccountService = BankAccountService.getInstance();
        String owner = "jyoti-g";

        BankAccount requestedAccount = new BankAccount(owner, BigDecimal.ZERO, BigDecimal.ZERO);

        Response response = target.path(BankAccountsController.BASE_URL).request().post(from(requestedAccount));

        assertEquals(Response.Status.CREATED.getStatusCode(), response.getStatusInfo().getStatusCode());

        BankAccount returnedAccount = response.readEntity(BankAccount.class);
        BankAccount createdAccount = bankAccountService.getBankAccountById(returnedAccount.getId());

        assertNotNull(returnedAccount);
        assertNotNull(createdAccount);

        assertNotEquals(returnedAccount.getId(), requestedAccount.getId());
        assertEquals(returnedAccount.getId(), createdAccount.getId());
        assertEquals(owner, createdAccount.getOwnerName());
        
    }


    @Description ( "Test Description: Verify successful retrival of a particular bank accounts ")
	@Test (description= "Tests that method will successfully retrive valid bank accounts with valid id from database")
    public void testGetBankAccountById() {
        Response response = getById(BankAccountDao.JYOTI);

        assertEquals(Response.Status.FOUND.getStatusCode(), response.getStatusInfo().getStatusCode());

        BankAccount bankAccount = response.readEntity(BankAccount.class);

        assertEquals(bankAccount.getId(), BankAccountDao.JYOTI);
    }
    
    
    @Description ( "Test Description: Verify successful retrival of all bank accounts")
	@Test (description= "Tests that method will successfully retrive all bank accounts from database")
    public void testGetAllBankAccounts() {
        Response response = target.path(BankAccountsController.BASE_URL).request().get();
        assertEquals(Response.Status.FOUND.getStatusCode(), response.getStatusInfo().getStatusCode());
       
        Collection<BankAccount> bankAccount = response.readEntity(new GenericType<Collection<BankAccount>>(){});
        assertEquals(bankAccount.size(), BankAccountDao.getInstance().getAllBankAccounts().size());
    }


    @Description ( "Test Description: Verify unsuccessful retrival of non-existing (null id ) bank account")
	@Test(description="Tests that method will fail to retrive  (internal server error) accounts with blank id from database")
    public void testGetNullBankAccount() {
        Response response = getById(null);
        assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatusInfo().getStatusCode());
    }


    @Description ( "Test Description: Verify unsuccessful retrival of invalid bank account")
	@Test(description="Tests that method will fail to retrive ( Not Found) accounts with blank id from database")
    public void testNonExistingBankAccountById() {
        Response response = getById(new Random().nextLong());        
        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatusInfo().getStatusCode());
    }


    @Description ( "Test Description: Verify successful update of bank account")
	@Test(description="Tests that method will successfully update bank account in database. Even if it will be attempt to update balance it will not be updated")
    public void testUpdateBankAccount() {
        BankAccountService bankAccountService = BankAccountService.getInstance();
        String newOwner = "New Name";

        BankAccount secondAccount = bankAccountService.getBankAccountById(BankAccountDao.RANJAN);
        secondAccount.setOwnerName(newOwner);
        BigDecimal accountBalance = secondAccount.getBalance();
        secondAccount.setBalance(accountBalance.add(BigDecimal.TEN));

        Response response = target.path(BankAccountsController.BASE_URL).request().put(from(secondAccount));

        assertEquals(Response.Status.OK.getStatusCode(), response.getStatusInfo().getStatusCode());

        BankAccount updatedAccount = bankAccountService.getBankAccountById(BankAccountDao.RANJAN);

        assertEquals(newOwner, updatedAccount.getOwnerName());
        assertThat(accountBalance, Matchers.comparesEqualTo(updatedAccount.getBalance()));
    }


    
    @Description ( "Test Description: Verify unsuccessful update of invalid bank account")
	@Test(description="Test that method will fail to update bank account with invalid account id in database ")
    public void testUpdateNonExistingBankAccount() {
        BankAccount bankAccount = new BankAccount(new Random().nextLong(),"", BigDecimal.ZERO, BigDecimal.ZERO);

        Response response = target.path(BankAccountsController.BASE_URL).request().put(from(bankAccount));

        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatusInfo().getStatusCode());
    }

    
    @Description ( "Test Description: Verify unsuccessful update of incorrect bank account")
    @Test(description="Tests that method will fail to update incorrect bank account in database")
    public void testIncorrectUpdateBankAccount() {
        BankAccount bankAccount = new BankAccount();
        bankAccount.setId(new Random().nextLong());

        Response response = target.path(BankAccountsController.BASE_URL).request().put(from(bankAccount));

        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatusInfo().getStatusCode());
    }


    private Response getById(Long id) {
        return target.path(BankAccountsController.BASE_URL + "/{" + BankAccountsController.GET_BANK_ACCOUNT_BY_ID_PATH + "}")
                .resolveTemplate("id", id == null ? "null" : id)
                .request().get();
    }

    private static Entity from(BankAccount bankAccount) {
        return Entity.entity(bankAccount, MediaType.valueOf(MediaType.APPLICATION_JSON));
    }
    

}
