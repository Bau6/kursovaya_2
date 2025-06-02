package org.example;

import java.util.function.Function;
import java.util.function.Predicate;

public class Observable<T> {
    public interface OnSubscribe<T> {
        Disposable subscribe(Observer<? super T> observer);
    }

    private final OnSubscribe<T> onSubscribe;

    private Observable(OnSubscribe<T> onSubscribe) {
        this.onSubscribe = onSubscribe;
    }

    public static <T> Observable<T> create(OnSubscribe<T> source) {
        return new Observable<>(source);
    }

    public Disposable subscribe(Observer<? super T> observer) {
        return onSubscribe.subscribe(observer);
    }

    public <R> Observable<R> map(Function<T, R> mapper) {
        return create(observer ->
                this.subscribe(new Observer<T>() {
                    public void onNext(T item) {
                        try {
                            observer.onNext(mapper.apply(item));
                        } catch (Throwable t) {
                            observer.onError(t);
                        }
                    }
                    public void onError(Throwable t) { observer.onError(t); }
                    public void onComplete() { observer.onComplete(); }
                })
        );
    }

    public Observable<T> filter(Predicate<T> predicate) {
        return create(observer ->
                this.subscribe(new Observer<T>() {
                    public void onNext(T item) {
                        try {
                            if (predicate.test(item)) {
                                observer.onNext(item);
                            }
                        } catch (Throwable t) {
                            observer.onError(t);
                        }
                    }
                    public void onError(Throwable t) { observer.onError(t); }
                    public void onComplete() { observer.onComplete(); }
                })
        );
    }

    public <R> Observable<R> flatMap(Function<T, Observable<? extends R>> mapper) {
        return Observable.create(observer -> {
            CompositeDisposable composite = new CompositeDisposable();

            Disposable outerDisposable = this.subscribe(new Observer<T>() {
                public void onNext(T item) {
                    try {
                        Observable<? extends R> inner = mapper.apply(item);
                        Disposable innerDisposable = inner.subscribe(new Observer<R>() {
                            public void onNext(R innerItem) {
                                observer.onNext(innerItem);
                            }

                            public void onError(Throwable t) {
                                observer.onError(t);
                                composite.dispose();
                            }

                            public void onComplete() {
                                // ничего не делаем, чтобы дожидаться всех внутренних
                            }
                        });
                        composite.add(innerDisposable);
                    } catch (Throwable t) {
                        observer.onError(t);
                        composite.dispose();
                    }
                }

                public void onError(Throwable t) {
                    observer.onError(t);
                    composite.dispose();
                }

                public void onComplete() {
                    observer.onComplete();
                }
            });

            composite.add(outerDisposable);
            return composite;
        });
    }



    public Observable<T> subscribeOn(Scheduler scheduler) {
        return Observable.create(observer -> {
            Disposable[] disposable = new Disposable[1];
            scheduler.execute(() -> disposable[0] = Observable.this.subscribe(observer));
            return () -> {
                if (disposable[0] != null) disposable[0].dispose();
            };
        });
    }


    public Observable<T> observeOn(Scheduler scheduler) {
        return create(observer ->
                this.subscribe(new Observer<T>() {
                    public void onNext(T item) {
                        scheduler.execute(() -> observer.onNext(item));
                    }
                    public void onError(Throwable t) {
                        scheduler.execute(() -> observer.onError(t));
                    }
                    public void onComplete() {
                        scheduler.execute(observer::onComplete);
                    }
                })
        );
    }
}

