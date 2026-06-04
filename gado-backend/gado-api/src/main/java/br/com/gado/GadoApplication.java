package br.com.gado;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Component;

@SpringBootApplication
public class GadoApplication {

    public static void main(String[] args) {
        SpringApplication.run(GadoApplication.class, args);
    }
}