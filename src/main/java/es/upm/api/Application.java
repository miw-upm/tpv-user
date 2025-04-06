package es.upm.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.web.servlet.error.ErrorMvcAutoConfiguration;

@SpringBootApplication(exclude = {ErrorMvcAutoConfiguration.class})
public class Application {

    public static void main(String[] args) { // mvn clean spring-boot:run
        SpringApplication.run(Application.class, args);
    }

}
