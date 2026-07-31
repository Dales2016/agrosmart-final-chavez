package ec.edu.espe.agrosmart;

import ec.edu.espe.agrosmart.entity.ProductoEntity;
import ec.edu.espe.agrosmart.repository.ProductoRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.math.BigDecimal;
import java.util.List;

@SpringBootApplication
public class AgrosmartApplication {

	public static void main(String[] args) {
		SpringApplication.run(AgrosmartApplication.class, args);
	}

@Bean
    CommandLineRunner sembrarProductos(ProductoRepository repository) {
        return args -> {
            if (repository.count() == 0) {
                repository.saveAll(List.of(
                        new ProductoEntity(
                                "Quinua orgánica de altura",
                                new BigDecimal("125.50"),
                                900,
                                "Quinua",
                                "ventas@agrosmart.ec,exportaciones@agrosmart.ec"
                        ),
                        new ProductoEntity(
                                "Quinua roja premium",
                                new BigDecimal("98.75"),
                                650,
                                "Quinua",
                                "comercial@agrosmart.ec"
                        ),
                        new ProductoEntity(
                                "Quinua negra andina",
                                new BigDecimal("110.00"),
                                420,
                                "Quinua",
                                "pedidos@agrosmart.ec"
                        ),
                        new ProductoEntity(
                                "Quinua blanca de muestra",
                                new BigDecimal("0.00"),
                                150,
                                "Quinua",
                                "muestras@agrosmart.ec"
                        ),
                        new ProductoEntity(
                                "Quinua perlada sin contacto",
                                new BigDecimal("85.40"),
                                500,
                                "Quinua",
                                ""
                        )
                ));
            }
        };
    }

}
