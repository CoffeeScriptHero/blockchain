package com.kozarenko.lab3.exception;

public class NonExistentWalletException extends Exception {
    public NonExistentWalletException(String address) { super("Wallet with address " + address + " does not exist"); }
}
