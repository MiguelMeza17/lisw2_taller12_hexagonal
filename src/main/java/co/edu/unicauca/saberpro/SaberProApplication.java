package co.edu.unicauca.saberpro;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Punto de arranque de Spring Boot. Vive fuera del hexágono: solo levanta la infraestructura,
 * que es la que conecta los adaptadores con los puertos del dominio.
 */
@SpringBootApplication
public class SaberProApplication {

    public static void main(String[] args) {
        SpringApplication.run(SaberProApplication.class, args);
    }
}
