package xyz.kail.demo.reactor3;

import java.time.Duration;

public class DurationMain {

    public static void main(String[] args) {
        System.out.println(Duration.parse("P1d").getSeconds());
        System.out.println(Duration.parse("PT1h").getSeconds());
        System.out.println(Duration.parse("PT1m").getSeconds());
    }

}
