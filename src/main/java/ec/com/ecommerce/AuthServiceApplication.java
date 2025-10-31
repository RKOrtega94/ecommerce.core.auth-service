package ec.com.ecommerce;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.ComponentScan;

@EnableDiscoveryClient
@SpringBootApplication
@ComponentScan(basePackages = { //
        "ec.com.ecommerce", //
        "ec.com.ecommerce.config", //
        "ec.com.ecommerce.grpc_lib.commons", //
        "ec.com.ecommerce.grpc_lib.auth", //
})
@OpenAPIDefinition(info = @Info(title = "Auth Service API", //
        version = "1.0", //
        description = "Auth Service for E-commerce Platform" //
))
public class AuthServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(AuthServiceApplication.class, args);
    }
}
