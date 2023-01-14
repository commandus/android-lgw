package com.commandus.lgw;

public class DevAddr extends HexBytes {
    DevAddr(String hex) {
        super(hex, 4);
    }
}
