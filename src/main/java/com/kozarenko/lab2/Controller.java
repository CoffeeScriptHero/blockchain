package com.kozarenko.lab2;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import org.eclipse.jetty.http.HttpStatus;

import java.util.List;
import java.util.UUID;

import static spark.Spark.get;
import static spark.Spark.post;

public class Controller {

    private static final List<Transaction> KDO_transactions = List.of(
        new Transaction("John Smith", "Joe Biden", 100),
        new Transaction("49632fd46c463162ded5", "d35a841d2c6e422ba298", 12),
        new Transaction("30d2561c04d44f71a298", "ea1dc5b9ce314b159314", 1),
        new Transaction("304155ef8cb04824bc5c", "Denys Kozarenko", 10000)
    );

    private static void setupBlockchainEndpoint(Blockchain KDO_blockchain, Gson KDO_gson) {
        get("/chain", (req, res) -> {
            BlockchainDTO KDO_blockchainDTO = new BlockchainDTO();
            KDO_blockchainDTO.setChain(KDO_blockchain.toString());
            KDO_blockchainDTO.setLength(KDO_blockchain.KDO_size());

            res.status(HttpStatus.OK_200);
            return KDO_gson.toJson(KDO_blockchainDTO);
        });
    }

    private static void setupMineEndpoint(Blockchain KDO_blockchain, Gson KDO_gson) {
        get("/mine", (req, res) -> {
            Block KDO_lastBlock = KDO_blockchain.KDO_lastBlock();
            int KDO_lastNonce = KDO_lastBlock.getKDO_Nonce();
            int KDO_nonce = KDO_blockchain.KDO_proofOfWork(KDO_lastNonce);

            // унікальний ідентифікатор, який *умовно* представляє адресу майнера
            String KDO_minerAddress = UUID.randomUUID().toString().replace("-", "");
            // створення coinbase транзакції (транзакції з винагородою для майнера)
            int KDO_coinReward = KDO_blockchain.KDO_newCoinbaseTransaction(KDO_minerAddress);

            Block KDO_block = KDO_blockchain.KDO_newBlock(KDO_nonce, Blockchain.KDO_hash(KDO_lastBlock));
            MineResultDTO KDO_mineResult = new MineResultDTO();
            KDO_mineResult.setBlock(KDO_block);
            KDO_mineResult.setReward(KDO_coinReward);

            res.status(HttpStatus.OK_200);
            return KDO_gson.toJson(KDO_mineResult);
        });
    }

    private static void setupTransactionEndpoint(Blockchain KDO_blockchain) {
        post("/transactions", (req, res) -> {
            try {
                // симулюємо отримання якоїсь нової транзакції, ніби вона приходить нам у запиті
                // (беремо заздалегідь заготовлену шаблонну транзакцію)
                Transaction KDO_transaction = KDO_randomTransaction();
                int KDO_index = KDO_blockchain.KDO_newTransaction(
                    KDO_transaction.getKDO_Sender(),
                    KDO_transaction.getKDO_Receiver(),
                    KDO_transaction.getKDO_Amount()
                );

                res.status(HttpStatus.CREATED_201);
                return String.format("Transaction with index %s created", KDO_index);
            } catch (JsonSyntaxException ex) {
                res.status(HttpStatus.BAD_REQUEST_400);
                return "Invalid JSON";
            }
        });
    }

    private static Transaction KDO_randomTransaction() {
        return KDO_transactions.get(BlockchainUtils.KDO_RANDOM.nextInt(0, KDO_transactions.size()));
    }

    public static void main(String[] args) {
        Blockchain KDO_blockchain = new Blockchain();
        Gson KDO_gson = new Gson();

        setupMineEndpoint(KDO_blockchain, KDO_gson);
        setupTransactionEndpoint(KDO_blockchain);
        setupBlockchainEndpoint(KDO_blockchain, KDO_gson);
    }
}
