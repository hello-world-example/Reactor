package xyz.kail.demo.reactor.netty.server;

import org.reactivestreams.Publisher;
import reactor.core.publisher.Mono;
import reactor.netty.DisposableServer;
import reactor.netty.NettyInbound;
import reactor.netty.NettyOutbound;
import reactor.netty.tcp.TcpServer;

import java.util.function.BiFunction;

/**
 * https://projectreactor.io/docs/netty/release/reference/index.html#tcp-server
 */
public class TcpServerMain {

    public static void main(String[] args) {
        DisposableServer server = TcpServer.create()
                .host("127.0.0.1")
                .port(8081)
                .wiretap(true)
                .handle(new BiFunction<NettyInbound, NettyOutbound, Publisher<Void>>() {
                    @Override
                    public Publisher<Void> apply(NettyInbound nettyInbound, NettyOutbound nettyOutbound) {
                        return nettyOutbound.sendString(Mono.just("sss"));
                    }
                })
                .bindNow();

        server.onDispose().block();
    }
}