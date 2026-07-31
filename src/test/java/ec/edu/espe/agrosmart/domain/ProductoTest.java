package ec.edu.espe.agrosmart.domain;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

public class ProductoTest {

    @Test
    void getters_alConstruirProducto_debenDevolverValoresRecibidos() {
        // Arrange
        List<String> correos = List.of("ventas@agrosmart.ec");

        Producto producto = new Producto(
                1L,
                "Quinua orgánica",
                "Quinua",
                new BigDecimal("125.50"),
                correos
        );

        // Act & Assert
        assertAll(
                () -> assertEquals(1L, producto.getId()),
                () -> assertEquals("Quinua orgánica", producto.getNombre()),
                () -> assertEquals("Quinua", producto.getCategoria()),
                () -> assertEquals(
                        new BigDecimal("125.50"),
                        producto.getPrecioUsd()
                ),
                () -> assertEquals(correos, producto.getCorreosNotificacion())
        );
    }

    @Test
    void constructor_alMutarListaOriginal_noDebeModificarCorreosInternos() {
        // Arrange
        List<String> correos = new ArrayList<>();
        correos.add("ventas@agrosmart.ec");

        Producto producto = new Producto(
                1L,
                "Quinua orgánica",
                "Quinua",
                new BigDecimal("125.50"),
                correos
        );

        // Act
        correos.add("intruso@mail.com");

        // Assert
        assertEquals(1, producto.getCorreosNotificacion().size());
        assertNotSame(correos, producto.getCorreosNotificacion());
    }

    @Test
    void getCorreosNotificacion_alIntentarModificar_debeLanzarExcepcion() {
        // Arrange
        List<String> correosOriginales = new ArrayList<>();
        correosOriginales.add("ventas@agrosmart.ec");

        Producto producto = new Producto(
                1L,
                "Quinua orgánica",
                "Quinua",
                new BigDecimal("125.50"),
                correosOriginales
        );

        // Act
        List<String> correosDevueltos =
                producto.getCorreosNotificacion();

        // Assert
        assertNotSame(correosOriginales, correosDevueltos);
        assertThrows(
                UnsupportedOperationException.class,
                () -> correosDevueltos.add("alterado@mail.com")
        );
        assertEquals(1, producto.getCorreosNotificacion().size());
    }
 
}
