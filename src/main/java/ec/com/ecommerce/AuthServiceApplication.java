package ec.com.ecommerce;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.ComponentScan;

@EnableDiscoveryClient
@SpringBootApplication
@ComponentScan(basePackages = { //
        "ec.com.ecommerce", //
        "ec.com.ecommerce.grpc_lib.commons", //
        "ec.com.ecommerce.grpc_lib.auth", //
})
//@OpenAPIDefinition()
public class AuthServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(AuthServiceApplication.class, args);
    }
}
