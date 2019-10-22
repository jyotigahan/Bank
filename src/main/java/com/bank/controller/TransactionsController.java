package com.bank.controller;

import static spark.Spark.get;
import static spark.Spark.post;

import java.util.Collection;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bank.exceptions.ObjectModificationException;
import com.bank.model.BankAccount;
import com.bank.model.Transaction;
import com.bank.service.BankAccountService;
import com.bank.service.TransactionsService;
import com.google.gson.Gson;


/**
 * @author Jyoti Gahan
 * The resource is responsible for the Transaction entity. Make it possible to create
 * and provide transactions. There is no ability to update an existing transaction as it is controversial operation
 * for this type of object. This object could be modified only by the system itself
 */

public class TransactionsController {
    private final Logger log = LoggerFactory.getLogger(TransactionsController.class);

    public static final String BASE_URL = "/api/v1/transactions";
    public static final String GET_TRANSACTION_BY_ID_PATH = "id";
	private final static TransactionsService TRANSACTION_SERVICE = TransactionsService.getInstance();

     
	public TransactionsController(){
    	
	post(BASE_URL, (request, response) -> {
			response.type("application/json");
			Transaction transaction = new Gson().fromJson(request.body(), Transaction.class);
			Transaction transactionCreated = TRANSACTION_SERVICE.createTransaction(transaction);
			response.status(Response.Status.CREATED.getStatusCode()); // 201 Created
			return new Gson().toJson(transactionCreated);
 		}); 	
 
	get(BASE_URL, (request, response) -> {
		response.type("application/json");
		Collection<Transaction> transactionList= TRANSACTION_SERVICE.getAllTransactions();
        if (!transactionList.isEmpty()) {
        response.status(Response.Status.FOUND.getStatusCode()); // 302 found
        }else {
        response.status(Response.Status.NOT_FOUND.getStatusCode()); // 404 not found
	    }
        return new Gson().toJson(transactionList);	
	});

    get(BASE_URL+"/:id", (request, response) -> {
        response.type("application/json");           
        Long id = Long.parseLong(request.params(":id"));
        Transaction transaction=TRANSACTION_SERVICE.getTransactionById(id);
        if (transaction!=null) {
        response.status(Response.Status.FOUND.getStatusCode()); // 302 found
        }else {
        response.status(Response.Status.NOT_FOUND.getStatusCode()); // 404 not found
	    }
		return new Gson().toJson(transaction);
     });
    
    
    
    
    }
}
