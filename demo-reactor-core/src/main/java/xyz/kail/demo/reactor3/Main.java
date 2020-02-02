package xyz.kail.demo.reactor3;

import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import reactor.core.CorePublisher;
import reactor.core.CoreSubscriber;
import reactor.core.Disposable;
import reactor.core.publisher.*;
import reactor.core.scheduler.Schedulers;
import reactor.util.function.Tuple2;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Flux 可以发出 0..N 个信号
 * Mono 可以发出 0..1 个信号
 */
public class Main {


    public static void main(String[] args) throws InterruptedException {

        Flux.push(sink -> {
            for (int i = 0; i < 10; i++) {
                sink.next("push: " + i);
            }
        }).subscribe(System.out::println);

        Thread.currentThread().join(10_000);

    }

}
