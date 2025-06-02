import org.example.*;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

public class ObservableTest {

    @Test
    public void testMapOperator() {
        List<Integer> results = new ArrayList<>();
        Observable<Integer> observable = Observable.create(emitter -> {
            emitter.onNext(1);
            emitter.onNext(2);
            emitter.onComplete();
            return () -> {};
        });

        observable
                .map(x -> x * 10)
                .subscribe(new Observer<Integer>() {
                    public void onNext(Integer item) { results.add(item); }
                    public void onError(Throwable t) { fail("Error not expected"); }
                    public void onComplete() { results.add(-1); }
                });

        System.out.println("testMapOperator results: " + results);

        assertEquals(Arrays.asList(10, 20, -1), results);
    }

    @Test
    public void testFilterOperator() {
        List<Integer> results = new ArrayList<>();
        Observable<Integer> observable = Observable.create(emitter -> {
            emitter.onNext(1);
            emitter.onNext(2);
            emitter.onNext(3);
            emitter.onComplete();
            return () -> {};
        });

        observable
                .filter(x -> x % 2 == 1)
                .subscribe(new Observer<Integer>() {
                    public void onNext(Integer item) { results.add(item); }
                    public void onError(Throwable t) { fail("Error not expected"); }
                    public void onComplete() { results.add(-1); }
                });

        System.out.println("testFilterOperator results: " + results);

        assertEquals(Arrays.asList(1, 3, -1), results);
    }

    @Test
    public void testFlatMapOperator() {
        List<String> results = new ArrayList<>();
        Observable<Integer> observable = Observable.create(emitter -> {
            emitter.onNext(1);
            emitter.onNext(2);
            emitter.onComplete();
            return () -> {};
        });

        observable
                .flatMap(x -> Observable.create(obs -> {
                    obs.onNext("Num:" + x);
                    obs.onComplete();
                    return () -> {};
                }))
                .subscribe(new Observer<Object>() {  // <--- исправлено
                    public void onNext(Object item) { results.add((String) item); }
                    public void onError(Throwable t) { fail("Error not expected"); }
                    public void onComplete() { results.add("done"); }
                });

        System.out.println("testFlatMapOperator results: " + results);

        assertEquals(Arrays.asList("Num:1", "Num:2", "done"), results);
    }

    @Test
    public void testErrorHandling() {
        List<String> errors = new ArrayList<>();
        Observable<Integer> observable = Observable.create(emitter -> {
            emitter.onNext(1);
            emitter.onError(new RuntimeException("Test error"));
            return () -> {};
        });

        observable.subscribe(new Observer<Integer>() {
            public void onNext(Integer item) { }
            public void onError(Throwable t) { errors.add(t.getMessage()); }
            public void onComplete() { fail("Should not complete"); }
        });

        System.out.println("testErrorHandling results: " + errors);

        assertEquals(1, errors.size());
        assertEquals("Test error", errors.get(0));
    }

    @Test
    public void testSubscribeOnAndObserveOnSchedulers() throws InterruptedException {
        List<String> threadNames = Collections.synchronizedList(new ArrayList<>());
        CountDownLatch latch = new CountDownLatch(1);

        Observable<Integer> observable = Observable.create(emitter -> {
            emitter.onNext(1);
            emitter.onComplete();
            return () -> {};
        });

        observable
                .subscribeOn(new IOThreadScheduler())
                .observeOn(new SingleThreadScheduler())
                .subscribe(new Observer<Integer>() {
                    public void onNext(Integer item) {
                        threadNames.add(Thread.currentThread().getName());
                    }
                    public void onError(Throwable t) {}
                    public void onComplete() {
                        latch.countDown();
                    }
                });

        boolean completed = latch.await(2, TimeUnit.SECONDS);
        assertTrue(completed);
        assertTrue(threadNames.size() > 0);

        boolean singleThreadFound = threadNames.stream().anyMatch(name -> name.contains("pool-") || name.contains("thread"));
        assertTrue(singleThreadFound);
    }
}
