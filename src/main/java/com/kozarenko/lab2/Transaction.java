package com.kozarenko.lab2;

public class Transaction {

    private final String KDO_sender;
    private final String KDO_receiver;
    private final int KDO_amount;

    public Transaction(String KDO_sender, String KDO_receiver, int KDO_amount) {
        this.KDO_sender = KDO_sender;
        this.KDO_receiver = KDO_receiver;
        this.KDO_amount = KDO_amount;
    }

    public String getKDO_Sender() {
        return KDO_sender;
    }

    public String getKDO_Receiver() {
        return KDO_receiver;
    }

    public int getKDO_Amount() {
        return KDO_amount;
    }
}
