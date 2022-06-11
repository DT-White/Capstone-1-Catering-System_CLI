package com.techelevator;

import com.techelevator.filereader.InventoryFileReader;
import com.techelevator.filereader.LogFileWriter;
import com.techelevator.items.CateringItem;
import com.techelevator.transactions.Sale;
import com.techelevator.view.Bank;
import com.techelevator.view.Menu;

import java.io.FileNotFoundException;
import java.util.List;
import java.util.Map;

/*
    This class should encapsulate all the functionality of the Catering system, meaning it should
    contain all the "work"
 */
public class CateringSystem {

    private Menu menu;
    private Cart cart;
    private Bank bank;
    private Inventory inventory;
private LogFileWriter logFileWriter;
    public CateringSystem(Menu menu, Cart cart, Bank bank, Inventory inventory, LogFileWriter logFileWriter) {
        this.menu = menu;
        this.cart = cart;
        this.bank = bank;
        this.inventory = inventory;
        this.logFileWriter = logFileWriter;
    }

    public int userSelectedNumber(String input) {
        int number = Integer.parseInt(input);
        if (number > 0 && number < 4) {
            return number;
        }
        menu.showCaseMessage("Invalid entry. Please enter 1, 2, or 3.");
        return 0;
    }

    public String userSelectedAddToCart() throws NullPointerException, NumberFormatException{
        String cartMessage = "Invalid product code.";
        String productCode = menu.readUserSelection("Please enter a product code to add to cart: ");
        int quantity = Integer.parseInt(menu.readUserSelection("Please enter a quantity for item " + productCode + ": "));
        try {
            cartMessage = cart.addToCart(productCode, quantity);
        } finally {
            return cartMessage;
        }

    }

    public String checkTotalBalance() {
        String totalMessage = "  Current total: $" + cart.getSubtotal();
        if (cart.getSubtotal() > bank.getBalance()) {
            totalMessage += " Insufficient Funds";

        }
        return totalMessage;
    }

    public int findLongestNameLength(List<CateringItem> productIdList) {
        int nameLength = 0;
        for (Map.Entry<String, CateringItem> currentEntry : inventory.getInventoryMap().entrySet()) {
            if (currentEntry.getValue().getName().length() > nameLength && productIdList.contains(currentEntry.getValue())) {
                nameLength = currentEntry.getValue().getName().length();
            }
        }
        return nameLength;

    }

    public String returnChange(){
        Map<String, Integer> changeMap = bank.makeChange(cart.getSubtotal());
        String changeString = " You received";
        String[] stringParts = new String[changeMap.size()];
        int i = 0;
        for (Map.Entry<String, Integer> denomination: changeMap.entrySet()){
             stringParts[i] = " (" + denomination.getValue() + ") " + denomination.getKey();
             i++;
        }
        changeString += stringParts[0];
        for (int j = 1; j < stringParts.length; j++) {
            changeString +="," + stringParts[j];
        }
        changeString += " in change";
        return changeString;
    }

    public void setCart(Cart cart) {
        this.cart = cart;
    }

    public void completeTransaction () {
        menu.showCart(inventory, cart, this);
        menu.showOrderTotal(cart);
        menu.showChange(this);
        bank.removeBalance(bank.getBalance());
        for (String logMessage: cart.formatLogMessage()){
                logFileWriter.writeToLog(new Sale( logMessage,cart.getSubtotal(), bank.getBalance() ));}
        cart = new Cart (inventory);
        setCart(cart);
    }
}