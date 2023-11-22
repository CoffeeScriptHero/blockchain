package com.kozarenko.lab4.exception;

public class LowBalanceException extends Exception {
    public LowBalanceException() { super("Forbidden operation: Balance is lower than amount of transaction"); }
}
