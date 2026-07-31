package ec.edu.espe.agrosmart.service;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;

import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

class PublicidadServiceTest {

    @Test
    void generarPublicidad_cuandoProveedorResponde_debeEmitirTextoGenerado() {
        // Arrange
        AgroSmartAIService aiService =
                mock(AgroSmartAIService.class);

        String publicidad =
                "Quinua saludable para una alimentación de calidad";

        when(aiService.generarPublicidad(
                "Quinua",
                "tiendas saludables"
        )).thenReturn(publicidad);

        ProductoService service =
                new ProductoService(null, aiService);

        // Act
        Mono<String> resultado = service.generarPublicidad(
                "Quinua",
                "tiendas saludables"
        );

        // Assert
        StepVerifier.create(resultado)
                .expectNext(publicidad)
                .verifyComplete();
    }

    @Test
    void generarPublicidad_cuandoProveedorFalla_debeEmitirMensajeRespaldo() {
        // Arrange
        AgroSmartAIService aiService =
                mock(AgroSmartAIService.class);

        when(aiService.generarPublicidad(
                "Quinua",
                "tiendas saludables"
        )).thenThrow(
                new RuntimeException("429 Too Many Requests")
        );

        ProductoService service =
                new ProductoService(null, aiService);

        // Act
        Mono<String> resultado = service.generarPublicidad(
                "Quinua",
                "tiendas saludables"
        );

        // Assert
        StepVerifier.create(resultado)
                .expectNext(
                        "Publicidad no disponible en este momento "
                                + "(RuntimeException)"
                )
                .verifyComplete();
    }
}