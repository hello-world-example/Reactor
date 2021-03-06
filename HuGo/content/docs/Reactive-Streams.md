# Reactive Streams 规范

> - [英文原文] [Reactive Streams](https://github.com/reactive-streams/reactive-streams-jvm/blob/v1.0.3/README.md)
> - [译文原文] [JVM平台上的响应式流（Reactive Streams）规范](https://www.cnblogs.com/lixinjie/p/a-reactive-streams-specification-on-jvm.html)



Reactive Streams 的目的是为 非阻塞背压(backpressure) 的 异步流处理提供标准。目前已被纳入 JDK9。

>  The purpose of Reactive Streams is to provide a standard for asynchronous stream processing with non-blocking backpressure.

最新版本：

```xml
<dependency>
  <groupId>org.reactivestreams</groupId>
  <artifactId>reactive-streams</artifactId>
  <version>1.0.3</version>
</dependency>

<!-- 工具包， 包含 测试、示例 等， 保证针对该规范的实现效果是一致的 -->
<dependency>
  <groupId>org.reactivestreams</groupId>
  <artifactId>reactive-streams-tck</artifactId>
  <version>1.0.3</version>
  <scope>test</scope>
</dependency>
```



## Goals, Design and Scope

处理流数据，尤其是 **数据量无法预知的实时在线数据**，在一个异步系统中要求格外小心。最需要关注的问题是资源的消耗需要被小心地控制，**避免数据流过快压垮下游服务** ( a fast data source does not overwhelm the stream destination)。异步是必要的，目的是为了并行地使用计算资源，如协调网络上多个主机，或一个机器的多个CPU核。



响应式流的主要目标是控制横穿一个异步边界的流数据的交换。考虑到向另一个线程或线程池传递元素，同时确保接收端不被强迫缓冲任意数量的数据。换句话说，后压是这个模型的一个必须部分，目的是允许队列在被界定的线程之间进行调节。如果后压信号是同步的，异步处理的好处将被否定，因此对一个响应式流实现的所有方面的完全非阻塞和异步行为的授权需要小心一些。



这个规范的意图就是允许创建许多种一致的实现，它们凭借遵守规则将能够平滑地互操作，在一个流应用的整个处理图中保留前文提到的好处和特征。



需要注意的是流操作的精确特性（转化，分割，合并等）并没有被这个规范包括。**响应式流只关心在不同的 API 组件间调节流数据**。在他们的开发中，已经非常细心地确保所有组合流的基本方式都能够被表达。



总之，响应式流是 JVM 上面向流的库的一个标准和规范：

- 依次地
- 处理一个潜在的无限数目元素
- 异步地在组件间传递元素
- 带有强制的非阻塞后压


响应式流规范由以下部分组成：

1、API 规定了需要实现的响应式流类型，并且在不同的实现间完成互操作性。

2、**The Technology Compatibility Kit (TCK)**是一个标准的测试套件，用于各种实现的一致性测试。

各种实现可以自由地实现规范中没有提到的额外特性，只要它们遵从 API 要求和在 TCK 中通过测试。



## API Components

API由以下组件组成，响应式流的实现必须提供它们：

1. `Publisher` 发布者（生产者）
2. `Subscriber`，订阅者（消费者）
3. `Subscription` 订阅
4. `Processor` 处理者



```java
public interface Publisher<T> {
    public void subscribe(Subscriber<? super T> s);
}

public interface Subscriber<T> {
    public void onSubscribe(Subscription s);
    public void onNext(T t);
    public void onError(Throwable t);
    public void onComplete();
}

public interface Subscription {
    public void request(long n);
    public void cancel();
}

public interface Processor<T, R> extends Subscriber<T>, Publisher<R> {
}
```



***Publisher*** 是一个潜在的无限数量的序列元素的一个提供者，按照收到的来自于它的订阅者的需要来发布这些元素。

对 `Publisher.subscribe(Subscriber)` 方法调用的响应，对于订阅者上的方法的可能调用顺序按下面的协议给出：

```java
onSubscribe | onNext | onError | onComplete
```



这意味着 `onSubscribe` 方法总是被调用，后面跟着一个可能的无限数量 `onNext` 方法调用（因为订阅者的请求）。如果失败的话，后跟一个 `onError` 方法调用，或当没有更多的元素可用时，是一个 `onComplete` 方法调用，只要这个 `Subscription`（订阅关系）没有被取消。



## Glossary 术语



|名词|释义|
|:--:|----|
|`Signal`<br />信号|作为一个名词，指 `onSubscribe`，`onNext`，`onComplete`，`onError`，`request(n) `或 `cancel` 中的一个 <br />作为一个动词，指 calling/invoking 这些方法|
|`Demand`<br />需求|作为一个名词，指的是一个订阅者（向发布者）请求的一定数量的元素，它还没有被发布者分发<br />作为一个动词，指的是请求更多元素的行为动作<br />可以看作是**订阅者向发布者发出的需求/动作，想要获取更多的元素，发布者暂时还没有回应**|
|`Synchronous(ly)`|在调用线程上执行，没有新开线程|
|`Return normally`<br />正常返回|仅返回已声明过的类型的值给调用者。如果想发送一个失败给订阅者，唯一合法的方式是通过 `onError`回调方法。|
|`Responsivity`<br />响应度|已准备就绪有能力来做出响应。在这个文档里用来指示不同的组件不应该互相削弱响应的能力|
|`Non-obstructing`<br />无障碍|描述一个方法的质量（品质），即在调用线程上尽可能快地执行完。这意味着，例如，避免重的计算和其它将拖住调用者线程执行的事情（因为没有新开线程）。|
|`Terminal state`<br />终止状态|本义是终止状态。对于一个发布者，指的是当onComplete或者onError已经被调用。对于一个订阅者，指的是当一个onComplete或onError（回调方法）已经收到。|
|`NOP`<br />|执行对于调用线程来说没有影响，能够安全地被调用任意次|
|`Serial(ly)`|本义是外部同步。为了线程安全的目的，协调访问在这个规范里定义的结构之外被实现，使用的技术像但不限于atomics，monitors或locks。|
|`Thread-safe`|能够安全地被同步或异步调用，不需要外部的同步来确保程序的正确性|



## SPECIFICATION

### Publisher

```java
public interface Publisher<T> {

    public void subscribe(Subscriber<? super T> s);

}
```



1. 一个发布者对一个订阅者的onNext调用总次数必须总是小于或等于订阅者的Subscription请求的元素总数
2. 一个发布者可能调用的onNext次数比要求的少，然后通过调用onComplete或onError来终止Subscription
3. 给 `Subscriber` 的 `onSubscribe`、`onNext`、`onError` 和 `onComplete` 信号必须串行
4. 如果一个发布者失败，它必须调用一个onError
5. 如果一个发布者成功地终止（对于有限流），它必须调用一个onComplete
6. 如果一个发布者调用一个订阅者上的onError或onComplete方法，那个订阅者的Subscription必须认为已被取消
7. 一旦一个terminal state已经被调用（onError，onComplete），它要求没有进一步的调用发生
8. 如果一个订阅被取消，它的订阅者必须最终停止被调用
9. 发布者的subscribe方法里必须在早于对订阅者上的任何方法调用之前先调用onSubscribe方法，且必须return normally。当订阅者是null的时候，此时必须向调用者抛出java.lang.NullPointerException异常。对于其它任何情况，通知失败（或拒绝订阅者）的唯一合法方式是调用onError
10. 发布者的subscribe方法可能被调用任意多次，但是每次必须使用一个不同的订阅者
11. 一个发布者可以支持多个订阅者，并决定每一个订阅是单播或多播。

### Subscriber

```java
public interface Subscriber<T> {

    public void onSubscribe(Subscription s);

    public void onNext(T t);

    public void onError(Throwable t);

    public void onComplete();

}
```



1、一个订阅者必须通过订阅（Subscription）的request(long n)方法声明需求，然后接收onNext调用。

2、如果一个订阅者怀疑它的调用处理将消极地影响它的发布者的响应度，建议异步地分发它的调用。

3、订阅者的onComplete()和onError(Throwable t)这两个方法里禁止调用订阅或发布者上的任何方法。

4、订阅者的onComplete()和onError(Throwable t)这两个方法在接收到调用后必须认为订阅已经被取消。

5、一个订阅者必须在收到onSubscriber之后调用指定订阅上的cancel()方法取消该订阅，如果它已经有一个活动的订阅。

6、一个订阅者必须调用订阅的cancel()方法，如果这个订阅不再需要的话。

7、一个订阅者必须确保所有对订阅发生的调用都来自于同一个线程或为它们各自提供external synchronization。

8、一个订阅者必须准备好接收一到多个onNext调用，在已经调用过订阅的cancel()方法之后如果还有请求的元素即将发送。订阅的cancel()方法并不保证立即执行底层的清理操作。

9、一个订阅者必须准备好接收一个onComplete调用，不管之前有或没有调用过订阅的request(long n)方法。

10、一个订阅者必须准备好接收一个onError调用，不管之前有或没有调用过订阅的request(long n)方法。

11、一个订阅者必须确保它的所有的方法调用发生在它们各自的处理之前。该订阅者必须小心合适地发布调用到它的处理逻辑。

12、订阅者的onSubscribe方法必须最多被调用一次，对于一个给定的订阅者。

13、对onSubscribe，onNext，onError或onComplete的调用必须return normally，除了当提供的任何参数是null时，这种情况必须向调用者抛出一个java.lang.NullPointerException异常。对于其它情况，对于一个订阅者来说，去通知一个失败的唯一合法的方式是取消它的订阅。在这个规则被违反的情况下，任何与该订阅者关联的订阅必须被认为是取消的，调用者必须激发这个错误条件，以一种对于运行环境来说是足够的方式。

### Subscription

```java
public interface Subscription {
    public void request(long n);
    public void cancel();
}
```



1、订阅的request和cancel方法必须在它的订阅者上下文里被调用。

2、订阅必须允许订阅者在onNext或onComplete方法里同步地调用订阅的request方法。

3、订阅的request方法必须放置一个关于发布者和订阅者间的同步递归调用的上界。

4、订阅的request方法应该尊重它的调用者的响应度，通过以一个适时的方式返回。

5、调用的cancel方法必须尊重它的调用者的响应度，通过以一个适时的方式返回，必须是幂等的和线程安全的。

6、在订阅取消之后，额外的request(long n)调用必须是NOP。

7、在订阅取消之后，额外的cancel()调用必须时NOP。

8、当订阅没有被取消时，request(long n)方法必须注册给定数目的额外元素，这些元素将被生产并分发给各自的订阅者。

9、当订阅没有被取消时，request(long n)必须使用一个java.lang.IllegalArgumentException异常来调用onError，如果参数小于等于0。引起的原因应该解释为不是正数的调用是非法的。

10、当订阅没有被取消，request(long n)可以同步地调用这个（或其它）订阅者上的onNext。

11、当订阅没有被取消，request(long n)可以同步地调用这个（或其它）订阅者上的onComplete或onError。

12、当订阅没有被取消，cancel()必须请求发布者最终停止调用它的订阅者上的方法。这个操作不要求立即影响订阅。

13、当订阅没有被取消，cancel()必须请求发布者最终删除对相关订阅者的任何引用。

14、当订阅没有被取消，调用cancel()可以引起发布者（如果是有状态的）进入关闭状态，如果在此刻没有其它的订阅存在。

15、调用订阅的cancel方法必须是return normally。

16、调用订阅的request方法必须是return normally。

17、一个订阅必须支持无数次地调用request方法，必须支持到2^63 - 1（Long.MAX_VALUE）次。如果一个需求等于或大于2^63 - 1（Long.MAX_VALUE），或许被发布者认为是真正的无界。

一个订阅被一个发布者和一个订阅者共享，目的是为了在它们之间调节数据交换。这也是为什么subscribe()方法并没有返回创建的那个订阅而是返回void的原因。这个订阅只能通过onSubscriber回调方法传给订阅者。

### Processor



```java
public interface Processor<T, R> extends Subscriber<T>, Publisher<R> {
}
```



1、一个处理器表示一个处理阶段，它既是一个订阅者又是一个发布者，必须遵守它们两者的契约。

2、一个处理器可以选择恢复一个onError调用。如果它选择这样做，必须认为订阅被取消，否则必须立即传播onError调用到它的订阅者。

在不被强制时，当最后一个订阅者取消它的订阅时，取消一个处理器的上游订阅是一个好主意，可以让这个取消调用往上游传播。



## Asynchronous vs Synchronous Processing

响应式流 API 要求所有的元素处理（onNext调用）或终止调用（onError，onComplete）禁止阻塞发布者。然而，每一个on*（以on开头的方法）处理器可以同步地处理事件，也可以异步地处理。

看下面这个示例：

```java
nioSelectorThreadOrigin map(f) filter(p) consumeTo(toNioSelectorOutput)
```

它有一个异步的来源和异步的目的地。让我们假设来源和目的地都是 selector 事件循环。

Subscription.request(n) 必须是一个链接从目的地到来源。这就是现在每一个实现都能选择如何做这些的地方。


下面使用管道操作符（|）来调用异步边界（队列和调度器），R#表示资源（可能是线程）。



```bash
nioSelectorThreadOrigin | map(f) | filter(p) | consumeTo(toNioSelectorOutput)
-------------- R1 ----  | - R2 - | -- R3 --- | ---------- R4 ----------------
```


在这个示例中，这3个消费者中的每一个，map，filter和consumeTo异步地调度work。它们可能在同一个事件循环上，也可能是分离的线程。


看下面这个示例



```bash
nioSelectorThreadOrigin map(f) filter(p) | consumeTo(toNioSelectorOutput)
------------------- R1 ----------------- | ---------- R2 ----------------
```




这里只有最后一步是异步地调度，通过把work添加到NioSelectorOutput事件循环。map和filter步骤都在来源线程上同步地执行。


或者另一种实现把这些操作都安装到最终消费者那里：



```bash
nioSelectorThreadOrigin | map(f) filter(p) consumeTo(toNioSelectorOutput)
--------- R1 ---------- | ------------------ R2 -------------------------
```



所有这些变体都是“异步流”。它们都有自己的位置，每一个有不同的权衡包括性能和实现复杂度。

响应式流允许实现灵活性来管理资源和调度，混合异步和同步处理，在一个非阻塞，异步，动态的推拉式流的限制内。

为了允许所有参与API元素（Publisher/Subscription/Subscriber/Processor）的完全的异步实现，这些接口定义的所有方法都返回void。



## Subscriber controlled queue bounds

其中一个底层设计原则是，所有缓冲区大小都是有界的，这些界限必须是知道的，且由订阅者控制。这些界限用元素数目（它依次转化为 onNext 的调用次数）这样的术语来表达。任何实现的目标都是为了支持无限流（尤其是高输出速率流），一般需要强迫界限都沿着避免 OOM 错误和限制资源使用的方式。



因为后压是强制的，使用无界缓冲区能够被避免。一般来说，只有在当一个队列可能无界增长时，此时也是发布者端比订阅者端保持一个更高的速率，且持续了一段较大的时间，但是这种情形是被后压来处理的。



队列界限能够被控制通过一个订阅者为了适合数目的元素而调用需求。在任何时候订阅者都知道：

- 请求的总元素数目：P
- 已经处理的元素数目：N

然后最大数量的元素可能达到是P - N，直到更多的需求被发送通知给发布者。这种情况下，订阅者也知道在它的输入缓冲区里的元素数目B，然后这个界限可以被重新精确为P - B - N。



这些界限必须被一个发布者尊重，独立于无论它表示的源是能够被后压的或不能。在这种源的生产速率不能被影响的情形下，例如钟表的滴滴答答或鼠标的移动，发布者必须选择要么缓冲元素或抛弃元素来遵守这个强加的界限。



订阅者在接收一个元素后发一个请求获取另一个元素，可以有效地实现一个停止和等待协议，这里这个需求信号和一个ACK（回应）相等。通过提需求的方式获取多个元素，ACK花销是分期偿还的。



这是值的注意的，订阅者被允许在任何时间点发信号提出需求，允许它是为了避免发布者和订阅者之间不必要的延迟（例如，保持它的输入缓冲区是满的，不需要等待完整的往返时间）。



## 相关资源

- 官方资料
  - 官网：http://www.reactive-streams.org/
  - 引导：http://www.reactive-streams.org/announce-1.0.3
  - javadoc：http://www.reactive-streams.org/reactive-streams-1.0.3-javadoc/org/reactivestreams/package-summary.html