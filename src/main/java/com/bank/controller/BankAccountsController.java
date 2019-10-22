package com.bank.controller;

import static spark.Spark.get;
import static spark.Spark.post;
import static spark.Spark.put;

import java.util.Collection;

import javax.ws.rs.core.Response;

import com.bank.exceptions.ObjectModificationException;
import com.bank.model.BankAccount;
import com.bank.service.BankAccountService;
import com.google.gson.Gson;

/**
 * @author Jyoti Gahan
 * This class is responsible for CRUD operations of Bank Account object
 */

public class BankAccountsController {
	  public static final String BASE_URL = "/api/v1/accounts";
	  public static final String GET_BANK_ACCOUNT_BY_ID_PATH = "id";
	  private final static BankAccountService BANK_ACCOUNT_SERVICE = BankAccountService.getInstance();

    public BankAccountsController() {
 
		post(BASE_URL, (request, response) -> {
			response.type("application/json");

			BankAccount account = new Gson().fromJson(request.body(), BankAccount.class);
			BankAccount accountCreated = BANK_ACCOUNT_SERVICE.createBankAccount(account);
			response.status(Response.Status.CREATED.getStatusCode()); // 201 Created
			return new Gson().toJson(accountCreated);	           

 		});

		get(BASE_URL, (request, response) -> {
			response.type("application/json");
			Collection<BankAccount> accountsList= BANK_ACCOUNT_SERVICE.getAllBankAccounts();
	        if (accountsList!=null) {
	            response.status(Response.Status.FOUND.getStatusCode()); // 302 found
	            }else {
	            response.status(Response.Status.NOT_FOUND.getStatusCode()); // 404 not found
	    	        }
	        return new Gson().toJson(accountsList);	
		});

     
        get(BASE_URL+"/:id", (request, response) -> {
            response.type("application/json");           
            Long id = Long.parseLong(request.params(":id"));
            BankAccount account=BANK_ACCOUNT_SERVICE.getBankAccountById(id);
            if (account!=null) {
            response.status(Response.Status.FOUND.getStatusCode()); // 302 found
            }else {
            response.status(Response.Status.NOT_FOUND.getStatusCode()); // 404 not found
 	        }
			return new Gson().toJson(account);
         });

         
        put(BASE_URL, (request, response) -> {
            response.type("application/json");
             try {
            BankAccount toUpdate = new Gson().fromJson(request.body(), BankAccount.class);
            BANK_ACCOUNT_SERVICE.updateBankAccount(toUpdate);
            
            BankAccount updated = BANK_ACCOUNT_SERVICE.getBankAccountById(toUpdate.getId());
			response.status(Response.Status.OK.getStatusCode()); // 200 ok
			return new Gson().toJson(updated);
            
           }catch(ObjectModificationException e ) {
        	   response.status(Response.Status.NOT_FOUND.getStatusCode());  // 404 not found
                 return new Gson().toJson("Account not found or error in edit account");
          }
        });

     
      
    }


}
