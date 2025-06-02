package org.example;

import java.util.ArrayList;
import java.util.List;

class CompositeDisposable implements Disposable {
    private final List<Disposable> disposables = new ArrayList<>();
    private volatile boolean disposed = false;

    public synchronized void add(Disposable d) {
        if (!disposed) {
            disposables.add(d);
        } else {
            d.dispose();
        }
    }

    public synchronized void dispose() {
        if (!disposed) {
            disposed = true;
            for (Disposable d : disposables) {
                d.dispose();
            }
            disposables.clear();
        }
    }

    public boolean isDisposed() {
        return disposed;
    }
}
