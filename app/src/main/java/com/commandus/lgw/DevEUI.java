package com.commandus.lgw;

public class DevEUI extends HexBytes{
    DevEUI(String hex) {
        super(hex, 8);
    }
}
