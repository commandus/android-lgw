package com.commandus.lgw;

public class HexBytes {
    public byte[] value;

    HexBytes(String hex, int size) {
        parseHexString(hex, size);
    }

    private void parseHexString(String hex, int size) {
        if (hex == null) {
            return;
        }
        int bytes = hex.length();
        value = new byte[size];
        for (int i = 0; i < bytes && (i < size * 2); i++) {
            int nybble = Character.digit(hex.charAt(bytes - 1 - i), 16);
            if ((i & 1) != 0) {
                nybble = nybble << 4;
            }
            value[size - 1 - (i / 2)] |= (byte) nybble;
        }
    }

    @Override
    public String toString() {
        if (value == null)
            return "";
        StringBuilder builder = new StringBuilder(value.length * 8);
        for (byte b : value) {
            String hex = Integer.toHexString(b);
            int l = hex.length();
            switch (l) {
                case 1:
                    builder.append("0").append(hex);
                    break;
                case 8:
                    builder.append(hex.substring(6, 8));
                    break;
                default:
                    builder.append(hex);
            }
        }
        return builder.toString();
    }

    public boolean empty() {
        if (value != null) {
            for (byte b : value) {
                if (b != 0)
                    return false;
            }
        }
        return true;
    }

}
