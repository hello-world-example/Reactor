# 操作符

> reactor.core.publisher.Flux [API 文档](https://projectreactor.io/docs/core/release/api/reactor/core/publisher/Flux.html)



## Static Methods

### just / fromArray / range / interval

```java
Flux.just(1, 2, 3).subscribe(System.out::println);

// Flux.just 内部调用 Flux.fromArray
Flux.fromArray(new Integer[]{1, 2, 3, 4}).subscribe(System.out::println);

// 参数(int start, int count),
// 这里生成 1 ~ 10 十个数字
Flux.range(1, 10).subscribe(System.out::println);

// 每秒生成一个数字，从 0 开始
Flux.interval(Duration.ofSeconds(1)).subscribe(System.out::println);
```



### empty / never / error

```java
final CoreSubscriber<Object> coreSubscriber = new CoreSubscriber<Object>() {
    @Override
    public void onSubscribe(Subscription s) {
        System.out.println(s.getClass());
    }
  
    @Override
    public void onNext(Object o) {
        System.out.println("onNext -> " + o);
    }

    @Override
    public void onError(Throwable t) {
        System.out.println("onError -> " + t);
    }

    @Override
    public void onComplete() {
        System.out.println("onComplete ~");
    }
};

// 输出
// class reactor.core.publisher.Operators$EmptySubscription
// onComplete ~
Flux.empty().subscribe(coreSubscriber);

// 输出
// class reactor.core.publisher.Operators$EmptySubscription
Flux.never().subscribe(coreSubscriber);

// 输出 
// class reactor.core.publisher.Operators$EmptySubscription
// onError -> java.lang.NullPointerException
Flux.error(new NullPointerException()).subscribe(coreSubscriber);
```

### generate / create

```java
// 不断生成数据，直到显式调用 complete() 方法
Flux.generate(sink -> {
    final double random = Math.random();
    sink.next("generate: " + random);

    // ❤ next 只能调用1次，否则会抛出以下异常
    // reactor.core.Exceptions$ErrorCallbackNotImplemented: java.lang.IllegalStateException: More than one call to onNext
    // sink.next("BBBBB");

    // ❤ 需要显示调用 complete
    if (random * 10 >= 9) {
        sink.complete();
    }
}).subscribe(System.out::println);

// 创建数据流
Flux.create(sink -> {
    for (int i = 0; i < 10; i++) {
        sink.next("create: " + i);
    }
}).subscribe(System.out::println);
```

### zip

```java
Flux.zip(
        Flux.just("A", "B", "C"),
        Flux.just("1", "2", "3")
).subscribe(System.out::println);

// 输出
[A,1]
[B,2]
[C,3]
```

![](https://projectreactor.io/docs/core/release/api/reactor/core/publisher/doc-files/marbles/zipFixedSourcesForFlux.svg)

### merge / mergeSequential / concat

```java
// 按元素实际产生的顺序 合并
Flux.merge(
  // 每 400ms 产生一次数据
  Flux.interval(Duration.of(400, ChronoUnit.MILLIS)).take(5),
  // 延时一秒，每 400ms 产生一次数据
  Flux.interval(Duration.ofSeconds(1), Duration.ofMillis(400)).take(5)
).toStream().forEach(System.out::println);


// 按被订阅的顺序 合并
Flux.mergeSequential(
  // 每 400ms 产生一次数据
  Flux.interval(Duration.of(400, ChronoUnit.MILLIS)).take(5),
  // 延时一秒，每 400ms 产生一次数据
  Flux.interval(Duration.ofSeconds(1), Duration.ofMillis(400)).take(5)
).toStream().forEach(System.out::println);

// ❤❤❤ concat 与 mergeSequential 的区别 ❤❤❤
// mergeSequential 的数据流是同时产生的，只是后一个数据流在前一个数据流之后输出，1~5 一次性立即输出
// concat 的后一个数据流是在前一个数据流结束后才开始，1~5 是一个一个生成输出的
Flux.concat(
  // 每 400ms 产生一次数据
  Flux.interval(Duration.of(400, ChronoUnit.MILLIS)).take(5),
  // 延时一秒，每 400ms 产生一次数据
  Flux.interval(Duration.ofSeconds(1), Duration.ofMillis(400)).take(5)
).toStream().forEach(System.out::println);

```

![](https://projectreactor.io/docs/core/release/api/reactor/core/publisher/doc-files/marbles/mergeFixedSources.svg)

---

![](https://projectreactor.io/docs/core/release/api/reactor/core/publisher/doc-files/marbles/mergeSequentialVarSources.svg)

---

![](https://projectreactor.io/docs/core/release/api/reactor/core/publisher/doc-files/marbles/concatVarSources.svg)

### combineLatest

> 多个数据库 产生的 最新数据组合 输出

```java
Flux.combineLatest(
    Arrays::toString,
    Flux.interval(Duration.ofSeconds(2)).take(3),
    Flux.just("A", "B")
).subscribe(System.out::println);
// 输出
[0, B]
[1, B]
[2, B]

Flux.combineLatest(
    Arrays::toString,
    Flux.interval(Duration.ofMillis(10)).take(3),
    Flux.interval(Duration.ofMillis(15)).take(3)
).toStream().forEach(System.out::println);
// 输出
[0, 0]
[1, 0]
[1, 1]
[2, 1]
[2, 2]
```

![](https://projectreactor.io/docs/core/release/api/reactor/core/publisher/doc-files/marbles/combineLatest.svg)

### first

```java
// 多个数据流 只 输出第一数据库 -> 1,2,3
Flux.first(
  Flux.just(1, 2, 3),
  Flux.just("A", "B", "C")
).toStream().forEach(System.out::println);
```

### defer

```java
Supplier<Integer> data = () -> {
    System.out.println("getData");
    return 1;
};

final Flux<Integer> deferFlux = Flux.defer(() -> Flux.just(data.get(), data.get()));
System.out.println("subscribe");
deferFlux.subscribe(System.out::println);

System.out.println();

final Flux<Integer> justFlux = Flux.just(data.get(), data.get());
System.out.println("subscribe");
justFlux.subscribe(System.out::println);

// 从输出上查看区别
subscribe  // 在 subscribe 的时候进行调用
getData
getData
1
1

getData
getData
subscribe  // 声明时候就立即被调用
1
1
```



## Instance Methods

### map / collectMap

```java
// 对现有数据进行转换
Flux.just(1, 2, 3).map(d -> d * d).subscribe(System.out::println);

// 抽取 Map 的 Key -> {1=1, 4=2, 9=3}
final Mono<Map<Integer, Integer>> mapMono = Flux.just(1, 2, 3).collectMap(d -> d * d);
final Map<Integer, Integer> block = mapMono.block();
System.out.println(block);
```

### filter

```java
// 输出 偶数
Flux.range(0, 10).filter(c -> c % 2 == 0).subscribe(System.out::println);

// 与 Java 8 Stream 一致
Flux.range(0, 10).toStream().filter(c -> c % 2 == 0).forEach(System.out::println);
```



### buffer / window

```java
Flux.range(0, 5).buffer(3).subscribe(System.out::println);

Flux.interval(Duration.of(1, ChronoUnit.SECONDS))
        .bufferTimeout(2, Duration.ofSeconds(3))
        .subscribe(System.out::println);
```
![](https://projectreactor.io/docs/core/release/api/reactor/core/publisher/doc-files/marbles/buffer.svg)
``` java
// window 和 buffer 的区别是 window 发出的数据类型 是 Flux<T>，buffer 是 T
Flux.range(0, 5).window(2).subscribe((Flux<Integer> up) -> {
    System.out.println("--");
    up.subscribe(System.out::println);
});
// 输出
--
0
1
--
2
3
--
4
```
![](https://projectreactor.io/docs/core/release/api/reactor/core/publisher/doc-files/marbles/windowWithMaxSize.svg)



### zipWith / zipWithIterable

```java
Flux.just("A", "B", "C")
    .zipWith(Flux.just("1", "2"))
    .zipWith(Flux.just("a", "b", "c", "d"))
    .subscribe((Tuple2<Tuple2<String, String>, String> x) -> System.out.println(x));

Flux.just("A", "B", "C")
    .zipWithIterable(Arrays.asList("1", "2", "3"))
    .subscribe(System.out::println);
```



### take / takeWhile / takeUntil

```java
// 取 前3个 -> 1,2,3
Flux.range(1, 10).take(3).subscribe(System.out::println);

// 取 后3个 -> 8,9,10
Flux.range(1, 10).takeLast(3).subscribe(System.out::println);

// 先判断条件是否成立，然后再决定是否取元素，即 如果一开始条件不成立，就直接终止了 -> 什么也不输出
Flux.range(1, 10).takeWhile(c -> c > 1 && c < 5).subscribe(System.out::println);

// 先取元素，直到遇到条件成立才停下 -> 1,2
Flux.range(1, 10).takeUntil(c -> c > 1 && c < 5).subscribe(System.out::println);

final Flux<Long> intervalFlux = Flux.interval(Duration.ofSeconds(1));
intervalFlux.subscribe(System.out::println);
// 三秒后 takeUntilOther 结束
TimeUnit.SECONDS.sleep(3);
intervalFlux.takeUntilOther(Flux.empty());
// 输出 0,1,2
```

### reduce / reduceWith

```java
// 1 ~ 10 累加 -> 55
Flux.range(1, 10).reduce((x, y) -> x + y).subscribe(System.out::println);

// 初始值10 与 1 ~ 10 累加 -> 65
Flux.range(1, 10).reduceWith(() -> 10, (x, y) -> x + y).subscribe(System.out::println);
```




## Read More

- [Reactor 3 学习笔记(1)](https://www.cnblogs.com/yjmyzz/p/reactor-tutorial-1.html)
- [Reactor 3 学习笔记(2)](https://www.cnblogs.com/yjmyzz/p/reactor-tutorial-2.html)