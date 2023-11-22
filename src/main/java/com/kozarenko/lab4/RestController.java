package com.kozarenko.lab4;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.kozarenko.lab4.exception.LowBalanceException;
import com.kozarenko.lab4.exception.NonExistentWalletException;
import org.eclipse.jetty.http.HttpStatus;

import static com.kozarenko.lab3.BlockchainUtils.MINER_ADDRESS;
import static spark.Spark.get;
import static spark.Spark.post;

public class RestController {

    private void setupBlockchainEndpoint(Blockchain blockchain, Gson gson) {
        get("/chain", (req, res) -> {
            BlockchainDTO blockchainDTO = new BlockchainDTO();
            blockchainDTO.setChain(blockchain.toString());
            blockchainDTO.setLength(blockchain.size());

            res.status(HttpStatus.OK_200);
            return gson.toJson(blockchainDTO);
        });
    }

    private void setupMineEndpoint(Blockchain blockchain, Gson gson) {
        get("/mine", (req, res) -> {
            Block lastBlock = blockchain.lastBlock();
            int lastNonce = lastBlock.getNonce();
            int nonce = blockchain.proofOfWork(lastNonce);
            int coinReward = blockchain.newCoinbaseTransaction(MINER_ADDRESS);

            Block block = blockchain.newBlock(nonce, Blockchain.hash(lastBlock));
            MineResultDTO mineResult = new MineResultDTO();
            mineResult.setBlock(block);
            mineResult.setReward(coinReward);

            res.status(HttpStatus.OK_200);
            return gson.toJson(mineResult);
        });
    }

    private void setupTransactionEndpoint(Blockchain blockchain, Gson gson) {
        post("/transactions", (req, res) -> {
            try {
                Transaction transaction = gson.fromJson(req.body(), Transaction.class);

                int index = blockchain.newTransaction(
                    transaction.getSender(),
                    transaction.getReceiver(),
                    transaction.getAmount()
                );

                res.status(HttpStatus.CREATED_201);
                return String.format("Transaction will be added to block with index %s", index);
            } catch (JsonSyntaxException ex) {
                res.status(HttpStatus.BAD_REQUEST_400);
                return "Invalid JSON";
            } catch (NonExistentWalletException ex) {
                res.status(HttpStatus.BAD_REQUEST_400);
                return "Wallet does not exist";
            } catch (LowBalanceException ex) {
                res.status(HttpStatus.BAD_REQUEST_400);
                return "Not enough funds on balance";
            }
        });
    }


    public static void main(String[] args) {
        RestController restController = new RestController();
        Blockchain blockchain = new Blockchain();
        Gson gson = new Gson();

        restController.setupMineEndpoint(blockchain, gson);
        restController.setupTransactionEndpoint(blockchain, gson);
        restController.setupBlockchainEndpoint(blockchain, gson);
    }
}
