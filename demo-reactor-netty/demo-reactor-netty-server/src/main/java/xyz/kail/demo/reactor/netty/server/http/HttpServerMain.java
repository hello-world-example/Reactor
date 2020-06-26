package xyz.kail.demo.reactor.netty.server.http;

import io.netty.channel.ChannelOption;
import reactor.core.publisher.Mono;
import reactor.netty.DisposableServer;
import reactor.netty.http.server.HttpServer;
import xyz.kail.demo.reactor.netty.server.Log;

/**
 * https://projectreactor.io/docs/netty/release/reference/index.html#http-server
 */
public class HttpServerMain {

    static {
        Log.init();

        System.setProperty("reactor.netty.http.server.accessLogEnabled", "true");
    }

    public static void main(String[] args) {

        DisposableServer server = HttpServer.create()
                .port(10001)
                .wiretap(false)
                .metrics(true)
                .tcpConfiguration(tcpServer -> {
                    tcpServer.configure()
                            .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 10000);
                    return tcpServer;
                })
                // .handle((request, response) -> response.sendString(Mono.just("handle")))
                .route(routes -> {
                    /* 首页 */
                    routes.get("/", (request, response) -> response.sendString(Mono.just("index")));
                    /* Path */
                    routes.get("/hello", (request, response) -> response.sendString(Mono.just("Hello World!")));
                    routes.get("/echo", (request, response) -> response.send(request.receive().retain()));
                    routes.get("/path/{param}", (request, response) -> response.sendString(Mono.just(request.param("param"))));
                    /* 任意页面 */
                    routes.index((request, response) -> response.sendString(Mono.just("404")));
                })
                .bindNow();

        server.onDispose().block();
    }

}
