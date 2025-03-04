package br.com.fernandoramosjr.sisgerdoc;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@SpringBootApplication
@EnableScheduling
@EnableAsync
public class SistemaGerenciadorDeDocumentosApplication implements WebMvcConfigurer {
    public static void main(String[] args) {
        SpringApplication.run(SistemaGerenciadorDeDocumentosApplication.class, args);
    }
}
