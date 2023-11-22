package com.kozarenko.lab4;

import java.util.List;

public class ChainDTO {

    private List<Block> chain;
    private int length;

    public ChainDTO() {}

    public ChainDTO(List<Block> chain, int length) {
        this.chain = chain;
        this.length = length;
    }

    public List<Block> getChain() {
        return chain;
    }

    public int getLength() {
        return length;
    }

    public void setChain(List<Block> chain) {
        this.chain = chain;
    }

    public void setLength(int length) {
        this.length = length;
    }
}
