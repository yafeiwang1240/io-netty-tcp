package com.fly.tcp.protocol;

import java.io.Serializable;

public class VoidProtocol implements Serializable {

    private static VoidProtocol instance = new VoidProtocol();

    public static VoidProtocol getInstance() {
        return instance;
    }

}
