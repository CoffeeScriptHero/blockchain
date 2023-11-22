package com.kozarenko.lab4;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.kozarenko.lab4.exception.LowBalanceException;
import com.kozarenko.lab4.exception.NonExistentWalletException;
import org.eclipse.jetty.http.HttpStatus;
import spark.Spark;

import java.util.List;

import static com.kozarenko.lab4.BlockchainUtils.MINER_ADDRESS;
import static spark.Spark.get;
import static spark.Spark.post;

public class RestController {

    private void setupBlockchainEndpoint(Blockchain blockchain, Gson gson) {
        get("/chain", (req, res) -> {
            res.status(HttpStatus.OK_200);
            return gson.toJson(new ChainDTO(blockchain.chain(), blockchain.size()));
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

    private void setupNodesRegistrationEndpoint(Blockchain blockchain, Gson gson) {
        post("/nodes", (req, res) -> {
            List<String> nodes = gson.fromJson(req.body(), NodesDTO.class).getNodes();
            nodes.forEach(blockchain::registerNode);
            return gson.toJson(blockchain.nodes());
        });
    }

    private void setupNodesResolveEndpoint(Blockchain blockchain, Gson gson) {
        get("/nodes/resolve", (req, res) -> {
            blockchain.resolveConflicts();
            return gson.toJson(new ChainDTO(blockchain.chain(), blockchain.chain().size()));
        });
    }


    public static void main(String[] args) {
        RestController restController = new RestController();
        Blockchain blockchain = new Blockchain();
        Gson gson = new Gson();

        restController.setupMineEndpoint(blockchain, gson);
        restController.setupTransactionEndpoint(blockchain, gson);
        restController.setupBlockchainEndpoint(blockchain, gson);
        restController.setupNodesRegistrationEndpoint(blockchain, gson);
        restController.setupNodesResolveEndpoint(blockchain, gson);
    }
}
