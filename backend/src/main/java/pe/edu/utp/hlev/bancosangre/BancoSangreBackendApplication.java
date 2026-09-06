package pe.edu.utp.hlev.bancosangre;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;

@SpringBootApplication(exclude = UserDetailsServiceAutoConfiguration.class)
public class BancoSangreBackendApplication {
    public static void main(String[] args) {
        SpringApplication.run(BancoSangreBackendApplication.class, args);
    }
}
