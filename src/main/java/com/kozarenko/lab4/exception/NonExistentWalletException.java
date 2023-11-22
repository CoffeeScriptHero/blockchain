package com.kozarenko.lab4.exception;

public class NonExistentWalletException extends Exception {
    public NonExistentWalletException(String address) { super("Wallet with address " + address + " does not exist"); }
}
