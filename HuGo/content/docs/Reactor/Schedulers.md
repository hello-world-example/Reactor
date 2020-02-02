# 线程调度

> reactor.core.scheduler.Schedulers [API 文档](https://projectreactor.io/docs/core/release/api/reactor/core/scheduler/Schedulers.html)



- Schedulers.immediate() - 使用当前线程
- Schedulers.elastic() - 使用线程池
- Schedulers.single() - 单个线程
- Schedulers.parallel() - 使用并行处理的线程池（取决于CPU核数)
- Schedulers.fromExecutorService(Executors.newScheduledThreadPool(5)) - 使用Executor（这个最灵活)