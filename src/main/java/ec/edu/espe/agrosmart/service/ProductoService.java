package ec.edu.espe.agrosmart.service;

import java.math.BigDecimal;
import java.util.List;
import org.springframework.stereotype.Service;
import ec.edu.espe.agrosmart.domain.Producto;
import ec.edu.espe.agrosmart.domain.ProductoFilters;
import ec.edu.espe.agrosmart.exception.ProductoNoEncontradoException;
import ec.edu.espe.agrosmart.mapper.ProductoMapper;
import ec.edu.espe.agrosmart.repository.ProductoRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Service
public class ProductoService {
    
    private static final Producto PRODUCTO_GENERICO = new Producto(
            0L,
            "SIN PRODUCTOS COMERCIALIZABLES",
            "Quinua",
            new BigDecimal("0.00"),
            List.of()
    );

    private final ProductoRepository repository;

    public ProductoService(ProductoRepository repository) {
        this.repository = repository;
    }

     public Flux<Producto> obtenerProductosComercializables() {
        // fromCallable difiere la consulta JPA hasta que exista una suscripción.
        return Mono.fromCallable(repository::findAll)

                // JPA es bloqueante; boundedElastic evita bloquear el event loop de Netty.
                .subscribeOn(Schedulers.boundedElastic())

                // Convierte el Mono que contiene la lista en un Flux de productos individuales.
                .flatMapMany(Flux::fromIterable)

                // Transforma cada entidad de persistencia en un objeto del dominio inmutable.
                .map(ProductoMapper::toDominio)

                // Crea una nueva instancia con el nombre en mayúsculas, sin mutar la original.
                .map(ProductoFilters.A_MAYUSCULAS)

                // Conserva únicamente los productos con precio positivo y correos disponibles.
                .filter(ProductoFilters.IS_VALID)

                // Ejecuta la trazabilidad sin modificar los elementos del flujo.
                .doOnNext(ProductoFilters.LOG_PRODUCTO)

                // Si todos fueron descartados, emite un único producto genérico.
                .defaultIfEmpty(PRODUCTO_GENERICO);
    }

    public Mono<Producto> buscarPorId(Long id) {
        // Envuelve la consulta bloqueante y la difiere hasta la suscripción.
        return Mono.fromCallable(() -> repository.findById(id))

                // Ejecuta el acceso JPA fuera de los hilos del event loop.
                .subscribeOn(Schedulers.boundedElastic())

                // Convierte el Optional en Mono: con valor emite uno y vacío no emite nada.
                .flatMap(Mono::justOrEmpty)

                // Convierte la entidad encontrada en el modelo inmutable del dominio.
                .map(ProductoMapper::toDominio)

                // Si el Mono quedó vacío, cambia el flujo por un error de producto inexistente.
                .switchIfEmpty(Mono.error(
                        new ProductoNoEncontradoException(id)
                ));
    }

}
