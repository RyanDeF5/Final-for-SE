/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.CodeforSefinal;
import com.mycompany.CodeforSefinal.Objects.ReferenceItem;
import com.mycompany.CodeforSefinal.Objects.QuantityItem;
import com.mycompany.CodeforSefinal.Objects.Item;
import com.mycompany.CodeforSefinal.Objects.Invoice;
import com.mycompany.CodeforSefinal.factor.DAOFactory;

import com.mycompany.CodeforSefinal.DAO.InvoiceDAO;
import com.mycompany.CodeforSefinal.DAO.InvoiceDAOImpl;
import com.mycompany.CodeforSefinal.DAO.ReferenceItemDAO;
import com.mycompany.CodeforSefinal.DAO.ReferenceItemDAOImpl;
import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.sql.Timestamp;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ListView;
import javafx.scene.control.ListCell;
import javafx.scene.input.MouseEvent;
import javafx.util.Callback;
import javafx.fxml.Initializable;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.stage.Stage;

public class InvoicePageController implements Initializable {

    @FXML private TextField clientNameField;
    @FXML private TextField invoiceNumberField;
  
    @FXML private TextField zipCodeField; 
    
    @FXML private TextField itemQuantityField;
    @FXML private MenuButton itemMenu;
    @FXML private ListView<String> itemsListView;
    @FXML private Button returnButton;
    @FXML
      private DatePicker datePicker;
    
    private Stage stage;
    private Scene scene;
    private Parent root;

    private ArrayList<QuantityItem> items = new ArrayList<>();
    
    private void setItemMenuList(ArrayList<ReferenceItem> items) {
        //Remove current options
        itemMenu.getItems().clear();
        
        for (int i = 0; i < items.size(); i++) {
            //Get each item...
            ReferenceItem item = items.get(i);
            
            //Create a menu option for each reference item...
            MenuItem menuItem = new MenuItem(item.getName());
            itemMenu.getItems().add(menuItem);
            
            //Have each menu option call handleAddItem when selected!
            menuItem.setOnAction(event -> {
                handleAddItem(item);
            });
            
        }
    }

    // This method handles adding items to the invoice
    @FXML
    private void handleAddItem(ReferenceItem rItem) {
        //Setup
        int itemId = rItem.getItemId();
        String itemName = rItem.getName();
        Double itemPrice = rItem.getPrice();
        String itemPriceText = ""+rItem.getPrice();
        int itemQuantity = Integer.parseInt(itemQuantityField.getText());
        if (itemQuantity <= 0)
            itemQuantity = 1;
        
        //Create QuantityItem using ReferenceItem's data and the itemQuantity
        QuantityItem qItem = new QuantityItem(itemId, itemName, itemPrice, itemQuantity);
                
        //Check if item has already been added
        for (int i = 0; i < items.size(); i++) {
            QuantityItem checkItem = items.get(i);
            if (qItem.getItemId() == checkItem.getItemId()) {
                showError("Item has already been added.");
                return;
            }
        }
        
        try {
            // Add the new item to item list
            items.add(qItem);

            // Display the new item in the ListView
            itemsListView.getItems().add(itemName + " - $" + itemPrice + " - QTY: " + itemQuantity);


        } catch (NumberFormatException e) {
            showError("Invalid field values.");
        }
    }

    // This method handles creating the invoice
    @FXML
    private void handleCreateInvoice() {
      
    String clientName = clientNameField.getText();
    String invoiceName = invoiceNumberField.getText();
    String zipCode = zipCodeField.getText();  // <-- Now using ZIP

    // Validate input
    if (clientName.isEmpty() || invoiceName.isEmpty() || zipCode.isEmpty()) {
        showError("Please fill in all fields.");
        return;
    }

    LocalDate localDate = datePicker.getValue();
    if (localDate == null) {
        showError("Please select a date.");
        return;
    }

    Timestamp selectedDate = Timestamp.valueOf(localDate.atStartOfDay());

    try {
        // Create invoice using ZIP-based constructor
        Invoice invoice = new Invoice(invoiceName, selectedDate, clientName, items, zipCode);
        
        // ⭐️ ⭐️ Important: fetch coordinates and totals HERE!
        invoice.fetchCoordinatesAndCalculateTotals();

    InvoiceDAO invoiceDAO = DAOFactory.getInvoiceDAO();

    // ⭐️ CHECK if the invoice name already exists
    if (invoiceDAO.invoiceNameExists(invoiceName)) {
        showError("Invoice name already exists. Please choose a different name.");
        return; // <--- Important! STOP HERE if duplicate name!
    }

    // Save to database
    invoiceDAO.saveInvoice(invoice);
    showInfo("Success", "Invoice has been saved to the database.");

    showInvoiceDetails(invoice);

} catch (Exception e) {
    showError("Error creating invoice: " + e.getMessage());
}
    }

    
    
    @FXML
    private void handleReturnToPrimary(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("primary.fxml"));
        root = loader.load();
        PrimaryController primaryController = loader.getController();
        
        stage = (Stage)((Node)event.getSource()).getScene().getWindow(); 
        scene = new Scene(root);
        stage.setScene(scene);
        stage.show(); 
    }

   private void showInvoiceDetails(Invoice invoice) {
    // Round prices to 2 decimal places
    double shippingPrice = invoice.getShippingPrice();
    double totalPrice = invoice.getTotalPrice();

    // Format the shipping and total prices to 2 decimal places
    String formattedShippingPrice = String.format("%.2f", shippingPrice);
    String formattedTotalPrice = String.format("%.2f", totalPrice);

    // Create a string to display the list of items
    StringBuilder itemsList = new StringBuilder();
    for (Item item : invoice.getItems()) {
        itemsList.append(item.getName())
                 .append(" - $")
                 .append(String.format("%.2f", item.getPrice()))
                 .append("\n");
    }

    // Create the message string with invoice details
    String message = "Invoice Name: " + invoice.getInvoiceName() + "\n" +
                     "Client: " + invoice.getClientName() + "\n" +
                     "Total Price: $" + formattedTotalPrice + "\n" +
                     "Shipping Price: $" + formattedShippingPrice + "\n" +
                     "Distance: " + invoice.getDistance() + " km\n\n" +
                     "Date: " + invoice.getDate().toString() + "\n" +  // <- Added date here
                     "Items:\n" + itemsList.toString();
    
    // Show the message in a pop-up or alert
    showInfo("Invoice Created", message);
    
    
    
    }
   
   
   
   
    private void showError(String message) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showInfo(String title, String message) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
      try {
        ReferenceItemDAO itemDAO = new ReferenceItemDAOImpl();
        List<Item> dbItems = itemDAO.getAllItems();

        // Convert List<Item> into ArrayList<ReferenceItem> safely
        ArrayList<ReferenceItem> referenceItems = new ArrayList<>();
        for (Item item : dbItems) {
            if (item instanceof ReferenceItem) {
                referenceItems.add((ReferenceItem) item);
            }
        }

        setItemMenuList(referenceItems);

    } catch (SQLException e) {
        showError("Failed to load items: " + e.getMessage());
    }
    }
    
    @FXML
    private void faqButton(javafx.event.ActionEvent event) throws IOException {

        Parent root = FXMLLoader.load(getClass().getResource("FAQ.fxml"));
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }
}
