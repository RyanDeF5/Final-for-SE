/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.CodeforSefinal;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;

/**
 *
 * @author dominick valentine
 */
public class ConnectToDatabase {
    
// hi 
    //Hey, this is Ernie!
    //Hey, this is Ryan!

    private static final String URL = "jdbc:mysql://sql12.freesqldatabase.com:3306/sql12771146";
            
            
    private static final String USER = "sql12771146";
    private static final String PASSWORD = "XDSQkgzNe9";

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
   
    public static void checkAddItem(Item item, String invoiceName) {
        //Check the items table to see if an item with this name has already been added (item names are unique)
        //If it hasn't, add it to the invoice table
        
        //Then, regardless of if we needed to add it, we have a separate query that searches for the same item name *again*
        //since that item name should defenitely (hopefully) be in the items table now.
        //We grab the item_id from the result
        
        //We then search for the invoice based on it's unique invoice_name, grab it's invoice_id
        //RUN THIS METHOD *AFTER* CREATING THE INVOICE WITH SAVE INVOICE OR ELSE THE invoice_name WON'T BE FOUND!
        
        //Finally, add the invoice_id and item_id to a new row in the item_invoices table.
    }
  
}


