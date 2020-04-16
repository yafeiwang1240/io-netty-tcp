package com.github.yafeiwang124.tcp.liaison.pool;

import java.io.Closeable;
import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class ShareablePool<T extends Closeable> implements Closeable {

    private long expireAfter;
    private ShareablePooledEntry<T> entry;
    private ScheduledExecutorService cleaner;
    private ConnectionManager<T> guardian;
    private Class<T> tClass;

    public ShareablePool(final long expireAfter,
                         ConnectionManager<T> guardian,
                         Class<T> tClass
    ) {
        this.expireAfter = expireAfter;
        this.guardian = guardian;
        this.cleaner = Executors.newSingleThreadScheduledExecutor();
        cleaner.scheduleAtFixedRate(() -> expireConnections(), expireAfter, expireAfter, TimeUnit.MILLISECONDS);
        this.tClass = tClass;
    }

    public synchronized T getConnection() throws Exception {
        T connection = null;
        if (entry != null) {
            connection = entry.getValue();
            if (!guardian.isValid(connection)) {
                guardian.release(connection);
                entry = null;
                connection = null;
            }
        }
        if (connection == null){
            initEntry();
            connection = entry.getValue();
        }
        entry.referenceIncrement();
        return createProxy(connection);
    }

    private synchronized void initEntry() throws Exception {
        if (entry == null) {
            T connection = guardian.build();
            entry = new ShareablePooledEntry(connection);
        }
    }

    private T createProxy(T connection){
        return (T) Proxy.newProxyInstance(this.getClass().getClassLoader(), new Class[]{tClass}, new CloseHandler(connection, entry));
    }


    private synchronized void expireConnections() {
        try {
            if (entry != null && entry.getReference() == 0 && entry.pooledTime + expireAfter < System.currentTimeMillis()) {
                entry.getValue().close();
                entry = null;
            }
        } catch (IOException e) {

        }

    }


    @Override
    public void close() throws IOException {
        cleaner.shutdown();
        if (entry != null) {
            entry.getValue().close();
            entry = null;
        }
    }

    public static class ShareablePooledEntry<T extends Closeable> {
        private volatile long pooledTime;
        private T value;
        private AtomicInteger referenceCount = new AtomicInteger();
        public ShareablePooledEntry(T value) {
            this.value = value;
            this.pooledTime = System.currentTimeMillis();
        }

        public long getPooledTime() {
            return pooledTime;
        }

        public void setPooledTime(long pooledTime) {
            this.pooledTime = pooledTime;
        }

        public T getValue() {
            return value;
        }

        public void setValue(T value) {
            this.value = value;
        }

        public void referenceIncrement(){
            referenceCount.incrementAndGet();
        }

        public void referenceDecrement(){
            referenceCount.decrementAndGet();
        }

        public int getReference(){
            return referenceCount.intValue();
        }
    }

    public static class CloseHandler<T extends Closeable> implements InvocationHandler {
        private ShareablePooledEntry<T> entry;
        private T target;

        public CloseHandler(T target, ShareablePooledEntry<T> entry) {
            this.target = target;
            this.entry = entry;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            if ("close".equals(method.getName())) {
                entry.referenceDecrement();
                return null;
            }
            try {
                return method.invoke(target, args);
            } catch (InvocationTargetException e) {
                throw e.getTargetException();
            }
        }
    }
}
