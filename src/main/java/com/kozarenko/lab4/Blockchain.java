package com.kozarenko.lab4;

import com.google.common.hash.Hashing;
import com.google.gson.Gson;
import com.kozarenko.lab4.exception.LowBalanceException;
import com.kozarenko.lab4.exception.NonExistentWalletException;
import org.eclipse.jetty.http.HttpStatus;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static com.kozarenko.lab4.BlockchainUtils.*;

public class Blockchain {

    private List<Block> chain = new ArrayList<>();
    private final List<Wallet> wallets = new ArrayList<>();
    private final MemoryPool memoryPool = new MemoryPool();
    private final Set<String> nodes = new HashSet<>();

    public Blockchain() {
        wallets.add(new Wallet(BLOCKCHAIN_ADDRESS, 1000000000));
        wallets.add(new Wallet(MINER_ADDRESS, 0));
        wallets.add(new Wallet("a14f0d66a18a46819946767058e04073", 5));
        wallets.add(new Wallet("b25144cc205445bd84b5864e54ecb627", 2));
        newBlock(312003, "Kozarenko");
    }

    public void registerNode(String node) {
        nodes.add(node);
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

    public List<Block> chain() {
        return chain;
    }

    public Set<String> nodes() { return nodes; }

    public void resolveConflicts() {
        Gson gson = new Gson();
        int maxLen = chain.size();

        try {
            for (String host : nodes) {
                URL url = new URL(host + "/chain");
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                StringBuffer content = new StringBuffer();
                if (connection.getResponseCode() == HttpStatus.OK_200) {
                    try (BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                        String inputLine;
                        while ((inputLine = in.readLine()) != null) {
                            content.append(inputLine);
                        }
                    }
                    connection.disconnect();
                    ChainDTO response = gson.fromJson(content.toString(), ChainDTO.class);
                    if (response.getLength() > maxLen && validChain(response.getChain())) {
                        maxLen = response.getLength();
                        this.chain = response.getChain();
                    }
                }
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public boolean validChain(List<Block> chain) {
        for (int i = 1; i < chain.size(); i++) {
            Block lastBlock = chain.get(chain.size() - i);
            Block currBlock = chain.get(i);
            if (!currBlock.getPrevHash().equals(hash(lastBlock))) {
                System.out.println("Hash doesn't match");
                return false;
            }
            if (!isProofValid(lastBlock.getNonce(), currBlock.getNonce())) {
                System.out.println("Proof is not valid");
                return false;
            }
        }
        return true;
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
