package com.kozarenko.lab3.exception;

public class LowBalanceException extends Exception {
    public LowBalanceException() { super("Forbidden operation: Balance is lower than amount of transaction"); }
}
