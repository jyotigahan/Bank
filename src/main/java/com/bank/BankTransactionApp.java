package com.bank;
import static spark.Spark.*;

import com.bank.controller.BankAccountsController;
import com.bank.controller.TransactionsController;
public class BankTransactionApp {
	
	public static void main(String[] args) {
        get("/hello", (request, response) -> "Hello BankTransactionApp!");
       new BankAccountsController( );
       new TransactionsController();
	}
}
