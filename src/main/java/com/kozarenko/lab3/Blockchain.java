package com.kozarenko.lab3;

import com.google.common.hash.Hashing;
import com.kozarenko.lab3.exception.LowBalanceException;
import com.kozarenko.lab3.exception.NonExistentWalletException;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.kozarenko.lab3.BlockchainUtils.BLOCKCHAIN_ADDRESS;
import static com.kozarenko.lab3.BlockchainUtils.MINER_ADDRESS;
import static com.kozarenko.lab3.BlockchainUtils.MONTH_OF_BIRTH;

public class Blockchain {

    private final List<Block> chain = new ArrayList<>();
    private final List<Wallet> wallets = new ArrayList<>();
    private final MemoryPool memoryPool = new MemoryPool();

    public Blockchain() {
        wallets.add(new Wallet(BLOCKCHAIN_ADDRESS, 1000000000));
        wallets.add(new Wallet(MINER_ADDRESS, 0));
        wallets.add(new Wallet("a14f0d66a18a46819946767058e04073", 5));
        wallets.add(new Wallet("b25144cc205445bd84b5864e54ecb627", 2));
        newBlock(312003, "Kozarenko");
    }

    public static String hash(Block block) {
        String hashingInput = String.valueOf(block.getIndex()) +
            block.getTimestamp() +
            block.getNonce() +
            block.getPrevHash();

        return Hashing.sha256()
            .hashString(hashingInput, StandardCharsets.UTF_8)
            .toString();
    }

    public Block newBlock(int nonce, String prevHash) {
        Block block = new Block(chain.size(), nonce, prevHash, memoryPool.getTransactions());
        chain.add(block);
        memoryPool.getTransactions().forEach(this::transferFunds);
        memoryPool.clear();
        return block;
    }

    public int newTransaction(String senderAddress, String receiverAddress, double transactionAmount) throws NonExistentWalletException, LowBalanceException {
        verifyTransactionPossible(senderAddress, receiverAddress, transactionAmount);
        Transaction transaction = new Transaction(senderAddress, receiverAddress, transactionAmount);
        printBalances(transaction, false);
        memoryPool.add(transaction);
        return chain.size();
    }

    public int newCoinbaseTransaction(String receiver) throws NonExistentWalletException {
        verifyWalletExists(receiver);
        Transaction transaction = new Transaction(BLOCKCHAIN_ADDRESS, receiver, 1);
        printBalances(transaction, false);
        memoryPool.add(transaction);
        return 1;
    }

    public int size() {
        return chain.size();
    }

    public Block genesisBlock() {
        return chain.get(0);
    }

    public Block lastBlock() {
        return chain.size() > 0 ? chain.get(chain.size() - 1) : null;
    }

    public int proofOfWork(int lastProof) {
        int proof = 0;
        while (!isProofValid(lastProof, proof)) {
            proof++;
        }
        return proof;
    }

    private boolean isProofValid(int lastProof, int proof) {
        String guess = String.valueOf(lastProof) + proof;
        String guessHash = Hashing.sha256()
            .hashString(guess, StandardCharsets.UTF_8)
            .toString();
        return guessHash.endsWith(MONTH_OF_BIRTH);
    }

    private void transferFunds(Transaction transaction) {
        Wallet senderWallet = getWalletByAddress(transaction.getSender()).get();
        Wallet receiverWallet = getWalletByAddress(transaction.getReceiver()).get();
        receiverWallet.addToBalance(transaction.getAmount());
        senderWallet.removeFromBalance(transaction.getAmount());
        printBalances(transaction, true);
    }

    private Optional<Wallet> getWalletByAddress(String address) {
        return wallets.stream().filter(wallet -> wallet.getAddress().equals(address)).findFirst();
    }

    private double balanceByAddress(String address) {
        return getWalletByAddress(address).get().getBalance();
    }

    private void verifyTransactionPossible(String senderAddress, String receiverAddress, double transactionAmount) throws NonExistentWalletException, LowBalanceException {
        verifyWalletExists(senderAddress);
        verifyWalletExists(receiverAddress);

        double senderBalance = balanceByAddress(senderAddress);
        if (senderBalance < transactionAmount) {
            throw new LowBalanceException();
        }
    }

    private void verifyWalletExists(String address) throws NonExistentWalletException {
        if (getWalletByAddress(address).isEmpty()) {
            throw new NonExistentWalletException(address);
        }
    }

    private void printBalances(Transaction transaction, boolean isTransactionConfirmed) {
        String msgIntro = isTransactionConfirmed
            ? "Balances after block with transaction added to chain"
            : "Balances before block with transaction added to chain";
        System.out.printf("%s:\nSender (%s) - %f,\n Receiver (%s) - %f\n",
            msgIntro, transaction.getSender(), balanceByAddress(transaction.getSender()),
            transaction.getReceiver(), balanceByAddress(transaction.getReceiver()));
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < chain.size(); ++i) {
            String delimiter = i == chain.size() - 1 ? "]" : ", ";
            sb.append(chain.get(i)).append(delimiter);
        }
        return sb.toString();
    }
}
