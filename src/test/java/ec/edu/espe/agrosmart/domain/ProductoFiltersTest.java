package ec.edu.espe.agrosmart.domain;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.Test;

class ProductoFiltersTest {

    @Test
    void isValid_conPrecioPositivoYCorreo_debeRetornarTrue() {
        // Arrange
        Producto producto = new Producto(
                1L,
                "Quinua orgánica",
                "Quinua",
                new BigDecimal("125.50"),
                List.of("ventas@agrosmart.ec")
        );

        // Act
        boolean resultado = ProductoFilters.IS_VALID.test(producto);

        // Assert
        assertTrue(resultado);
    }

    @Test
    void isValid_conPrecioCero_debeRetornarFalse() {
        // Arrange
        Producto producto = new Producto(
                2L,
                "Quinua de muestra",
                "Quinua",
                new BigDecimal("0.00"),
                List.of("muestras@agrosmart.ec")
        );

        // Act
        boolean resultado = ProductoFilters.IS_VALID.test(producto);

        // Assert
        assertFalse(resultado);
    }

    @Test
    void isValid_sinCorreos_debeRetornarFalse() {
        // Arrange
        Producto producto = new Producto(
                3L,
                "Quinua sin contacto",
                "Quinua",
                new BigDecimal("85.40"),
                List.of()
        );

        // Act
        boolean resultado = ProductoFilters.IS_VALID.test(producto);

        // Assert
        assertFalse(resultado);
    }
}