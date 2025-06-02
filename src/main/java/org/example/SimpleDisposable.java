package org.example;

import java.util.concurrent.atomic.AtomicBoolean;

class SimpleDisposable implements Disposable {
    private final AtomicBoolean isDisposed = new AtomicBoolean(false);
    public void dispose() { isDisposed.set(true); }
    public boolean isDisposed() { return isDisposed.get(); }
}

