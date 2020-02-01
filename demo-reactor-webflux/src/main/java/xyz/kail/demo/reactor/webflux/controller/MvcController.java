package xyz.kail.demo.reactor.webflux.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Date;

@RestController
@RequestMapping("/web")
public class MvcController {

    @GetMapping("/index")
    public Date index() {
        return new Date();
    }

    @GetMapping("/mono")
    public Mono<Date> mono() {
        return Mono.just(new Date());
    }

    @GetMapping("/flux")
    public Flux<Integer> flux() {
        return Flux.range(1, 10);
    }


}
