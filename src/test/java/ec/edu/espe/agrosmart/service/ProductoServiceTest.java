package ec.edu.espe.agrosmart.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import ec.edu.espe.agrosmart.entity.ProductoEntity;
import ec.edu.espe.agrosmart.exception.ProductoNoEncontradoException;
import ec.edu.espe.agrosmart.repository.ProductoRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

class ProductoServiceTest {

    @Test
    void obtenerProductosComercializables_conTresValidosYDosInvalidos_debeEmitirTres() {
        // Arrange
        ProductoRepository repository = mock(ProductoRepository.class);

        when(repository.findAll()).thenReturn(List.of(
                crearEntidad("Quinua orgánica", "125.50", "ventas@agrosmart.ec"),
                crearEntidad("Quinua roja", "98.75", "comercial@agrosmart.ec"),
                crearEntidad("Quinua negra", "110.00", "pedidos@agrosmart.ec"),
                crearEntidad("Quinua muestra", "0.00", "muestras@agrosmart.ec"),
                crearEntidad("Quinua sin contacto", "85.40", "")
        ));

        ProductoService service =
                new ProductoService(repository, null);

        // Act
        Flux<?> flujo =
                service.obtenerProductosComercializables();

        // Assert
        StepVerifier.create(flujo)
                .expectNextCount(3)
                .verifyComplete();
    }

    @Test
    void obtenerProductosComercializables_conTodosInvalidos_debeEmitirGenerico() {
        // Arrange
        ProductoRepository repository = mock(ProductoRepository.class);

        when(repository.findAll()).thenReturn(List.of(
                crearEntidad("Quinua muestra", "0.00", "muestras@agrosmart.ec"),
                crearEntidad("Quinua sin contacto", "85.40", "")
        ));

        ProductoService service =
                new ProductoService(repository, null);

        // Act
        Flux<?> flujo =
                service.obtenerProductosComercializables();

        // Assert
        StepVerifier.create(flujo)
                .assertNext(producto -> assertEquals(
                        "SIN PRODUCTOS COMERCIALIZABLES",
                        ((ec.edu.espe.agrosmart.domain.Producto) producto)
                                .getNombre()
                ))
                .verifyComplete();
    }

    @Test
    void buscarPorId_conIdInexistente_debeEmitirProductoNoEncontradoException() {
        // Arrange
        ProductoRepository repository = mock(ProductoRepository.class);

        when(repository.findById(9999L))
                .thenReturn(Optional.empty());

        ProductoService service =
                new ProductoService(repository, null);

        // Act
        Mono<?> resultado = service.buscarPorId(9999L);

        // Assert
        StepVerifier.create(resultado)
                .expectError(ProductoNoEncontradoException.class)
                .verify();
    }

    private ProductoEntity crearEntidad(
            String nombre,
            String precio,
            String correos
    ) {
        ProductoEntity entidad = new ProductoEntity();
        entidad.setNombreProducto(nombre);
        entidad.setPrecioUsd(new BigDecimal(precio));
        entidad.setStockKg(100);
        entidad.setCategoria("Quinua");
        entidad.setCorreosNotificacion(correos);
        return entidad;
    }
}