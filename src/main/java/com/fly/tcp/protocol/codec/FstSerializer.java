package com.fly.tcp.protocol.codec;

import org.nustaq.serialization.FSTConfiguration;

public class FstSerializer {
    private static FSTConfiguration conf = FSTConfiguration.createDefaultConfiguration();

    public static void addClass(Class _class) {
        conf.registerClass(_class);
    }

    public static void addClasses(Class... _classes) {
        conf.registerClass(_classes);
    }

    public static byte[] encode(Object object) {
        return conf.asByteArray(object);
    }

    public static Object decode(byte[] bytes) {
        return conf.asObject(bytes);
    }
}
