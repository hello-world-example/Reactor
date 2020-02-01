package xyz.kail.demo.reactor.webflux.controller;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.server.*;
import reactor.core.publisher.Mono;

import javax.annotation.Resource;
import java.util.Date;

@RestController
@RequestMapping("/webflux")
public class WebfluxController {

    @GetMapping("/index")
    public Date index() {
        return new Date();
    }

    public Mono<ServerResponse> mono(ServerRequest request) {
        return ServerResponse.ok().bodyValue(new Date());
    }

    @Configuration
    public static class Conf {

        @Resource
        private WebfluxController fluxController;

        @Bean(name = "FluxController.route")
        public RouterFunction<ServerResponse> route() {
            return RouterFunctions.route()
                    .path("/webflux", p -> {
                        p.GET("/mono", fluxController::mono);
                    })
                    .build();
        }
    }


}
