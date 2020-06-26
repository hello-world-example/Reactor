package xyz.kail.demo.reactor.netty.client.http;

import reactor.netty.http.client.HttpClient;
import xyz.kail.demo.reactor.netty.client.Log;

public class HttpClientMain {

    public static void main(String[] args) {
        Log.init();

        HttpClient httpClient = HttpClient.create().wiretap(true);

        String content = httpClient.get().uri("http://www.baidu.com")
                .responseContent().aggregate().asString().block();

        System.out.println(content);

    }

}
