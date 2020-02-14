# 窗口批量操作

## 适用场景

- 缓存指定时间 或 指定量的数据，批量持久化
- 对**最近一段时间的数据进行统计**，然后持久化

## 相关 Flux API

- `window(Duration windowingTimespan)`
- `window(int maxSize)`
- `windowTimeout(int maxSize, Duration maxTime)`
- `windowUntilChanged()`
- `windowWhile(Predicate<T> inclusionPredicate)`

## 代码示例

```java

public class BatchOperateDemo<T> {

  private BlockingQueue<T> queue = new LinkedBlockingQueue<>();

  /**
   * 数据来源（从队列中拿）
   */
  private Consumer<SynchronousSink<T>> provider = sink -> {
    try {
      sink.next(queue.take());
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  };

  /**
   * 投喂数据
   */
  public void feed(final T data) throws InterruptedException {
    queue.put(data);
  }

  /**
   * 打印指定窗口 数据量
   */
  private Consumer<Flux<T>> countConsumer() {
    return (Flux<T> data) -> data.count().subscribe(System.out::println);
  }

  /**
   * 打印指定窗口 相同数据的个数
   */
  private Consumer<Flux<T>> groupConsumer() {
    return (Flux<T> data) ->
      data.groupBy(d -> d)
      .subscribe(
      gf -> gf.count()
      .subscribe(v -> System.out.println("key:" + gf.key() + " value:" + v))
    );
  }


  public static <T> BatchOperateDemo<T> count(Duration windowingTimespan) {
    final BatchOperateDemo<T> operateDemo = new BatchOperateDemo<>();
    Flux.generate(operateDemo.provider)
      .window(windowingTimespan)
      // ❤ ?
      .subscribeOn(Schedulers.newSingle("BatchOperateDemo.count.Duration" + windowingTimespan))
      .subscribe(operateDemo.countConsumer());
    return operateDemo;
  }

  public static <T> BatchOperateDemo<T> group(Duration windowingTimespan) {
    final BatchOperateDemo<T> operateDemo = new BatchOperateDemo<>();
    Flux.generate(operateDemo.provider)
      .window(windowingTimespan)
      // ❤ ?
      .subscribeOn(Schedulers.newSingle("BatchOperateDemo.group.Duration" + windowingTimespan))
      .subscribe(operateDemo.groupConsumer());
    return operateDemo;
  }


  public static void main(String[] args) throws InterruptedException {
    /**
     * 每三秒统计一次
     */
    BatchOperateDemo<String> batchOperateDemo = BatchOperateDemo.group(Duration.ofSeconds(3));

    /**
     * 生成模拟数据
     */
    for (int i = 0; i < 1_0000; i++) {
       TimeUnit.MILLISECONDS.sleep(100);
       // 投喂数据
       batchOperateDemo.feed(String.valueOf((long) (Math.random() * 10)));
    }

  }

}
```

## Read More

- [window(Duration windowingTimespan)](https://projectreactor.io/docs/core/release/api/reactor/core/publisher/Flux.html#window-java.time.Duration-)