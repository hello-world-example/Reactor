package xyz.kail.demo.reactor3;

import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxOperator;
import reactor.core.publisher.Mono;
import reactor.core.publisher.SynchronousSink;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * Flux 可以发出 0..N 个信号
 * Mono 可以发出 0..1 个信号
 */
public class Main {


    public static void main(String[] args) throws InterruptedException {

        Consumer<SynchronousSink<String>> consumer = (sink) -> {
            final double random = Math.random();
            sink.next("generate: " + random);

            try {
                TimeUnit.MILLISECONDS.sleep((long) (random * 100));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        };

        final Flux<String> generate = FluxOperator.generate(consumer);
        generate.window(Duration.ofSeconds(2))
                .subscribeOn(Schedulers.newSingle("asd"))
                .subscribe(f->f.count().subscribe(System.out::println));

        System.out.println("end");



    }

}
