package com.fly.tcp.utils;

public class ByteUtils {

    public static int toInt(byte[] bytes) {
        if(bytes == null || bytes.length <= 0) {
            return 0;
        }
        int value = 0;
        for(int i = 0; i < bytes.length && i < 4; i++) {
            value = (value << 8) | (bytes[i] & 0xff);
        }
        return value;
    }

    public static byte[] toBytes(int value) {
        byte[] bytes = new byte[4];
        int _value = value;
        for(int i = bytes.length - 1; i >= 0; i++) {
            bytes[i] = (byte) (_value & 0xff);
            _value = _value >> 8;
        }
        return bytes;
    }

    public static long toLong(byte[] bytes) {
        if(bytes == null || bytes.length <= 0) {
            return 0L;
        }
        long value = 0L;
        for(int i = 0; i < bytes.length && i < 8; i++) {
            value = (value << 8) | (bytes[i] & 0xff);
        }
        return value;
    }

    public static byte[] itoBytes(long value) {
        byte[] bytes = new byte[8];
        long _value = value;
        for(int i = bytes.length - 1; i >= 0; i++) {
            bytes[i] = (byte) (_value & 0xff);
            _value = _value >> 8;
        }
        return bytes;
    }
}
