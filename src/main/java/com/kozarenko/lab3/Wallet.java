package com.kozarenko.lab3;

public class Wallet {

    private final String address;
    private double balance;

    public Wallet(String address, double balance) {
        this.address = address;
        this.balance = balance;
    }

    public Wallet(String address) {
        this(address, 0);
    }

    public String getAddress() {
        return address;
    }

    public double getBalance() {
        return balance;
    }

    public void addToBalance(double amount) {
        balance += amount;
    }

    public void removeFromBalance(double amount) {
        balance -= amount;
    }
}
