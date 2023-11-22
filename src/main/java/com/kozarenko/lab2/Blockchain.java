package com.kozarenko.lab2;

import com.google.common.hash.Hashing;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class Blockchain {

    private final List<Block> KDO_chain = new ArrayList<>();
    private final List<Transaction> KDO_currentTransactions = new ArrayList<>();

    public Blockchain() {
        KDO_newBlock(312003, "Kozarenko");
    }

    public static String KDO_hash(Block KDO_block) {
        String KDO_hashingInput = String.valueOf(KDO_block.getKDO_Index()) +
            KDO_block.getKDO_Timestamp() +
            KDO_block.getKDO_Nonce() +
            KDO_block.getKDO_PrevHash();

        return Hashing.sha256()
            .hashString(KDO_hashingInput, StandardCharsets.UTF_8)
            .toString();
    }

    public Block KDO_newBlock(int KDO_nonce, String KDO_prevHash) {
        List<Transaction> KDO_transactions = KDO_currentTransactions.stream().toList();
        Block block = new Block(KDO_chain.size(), KDO_nonce, KDO_prevHash, KDO_transactions);
        KDO_currentTransactions.clear();
        KDO_chain.add(block);
        return block;
    }

    public int KDO_newTransaction(String KDO_sender, String KDO_receiver, int KDO_amount) {
        KDO_currentTransactions.add(new Transaction(KDO_sender, KDO_receiver, KDO_amount));
        return KDO_chain.size();
    }

    public int KDO_newCoinbaseTransaction(String KDO_receiver) {
        KDO_currentTransactions.add(new Transaction("0", KDO_receiver, 1));
        return 1;
    }

    public int KDO_size() {
        return KDO_chain.size();
    }

    public Block KDO_genesisBlock() {
        return KDO_chain.get(0);
    }

    public Block KDO_lastBlock() {
        return KDO_chain.size() > 0 ? KDO_chain.get(KDO_chain.size() - 1) : null;
    }

    public int KDO_proofOfWork(int lastProof) {
        int KDO_proof = 0;
        while (!KDO_isProofValid(lastProof, KDO_proof)) {
            KDO_proof++;
        }
        return KDO_proof;
    }

    private boolean KDO_isProofValid(int KDO_lastProof, int KDO_proof) {
        String KDO_guess = String.valueOf(KDO_lastProof) + KDO_proof;
        String KDO_guessHash = Hashing.sha256()
            .hashString(KDO_guess, StandardCharsets.UTF_8)
            .toString();
        return KDO_guessHash.endsWith(BlockchainUtils.KDO_MONTH_OF_BIRTH);
    }

    @Override
    public String toString() {
        StringBuilder KDO_sb = new StringBuilder("[");
        for (int i = 0; i < KDO_chain.size(); ++i) {
            String KDO_delimiter = i == KDO_chain.size() - 1 ? "]" : ", ";
            KDO_sb.append(KDO_chain.get(i)).append(KDO_delimiter);
        }
        return KDO_sb.toString();
    }
}
