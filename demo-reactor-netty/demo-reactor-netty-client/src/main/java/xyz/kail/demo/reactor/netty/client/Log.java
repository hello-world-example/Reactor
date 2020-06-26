package xyz.kail.demo.reactor.netty.client;

import org.slf4j.impl.SimpleLogger;

public class Log {

    static {
        init();
    }

    public static void init() {
        // slf4j-simple , 日志级别设置为 Debug
        System.setProperty(SimpleLogger.DEFAULT_LOG_LEVEL_KEY, "DEBUG");
    }
}
