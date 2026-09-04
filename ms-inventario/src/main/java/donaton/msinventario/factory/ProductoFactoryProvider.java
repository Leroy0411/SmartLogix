package donaton.msinventario.factory;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Registro de fábricas de producto.
 * Spring inyecta automáticamente todas las implementaciones de ProductoFactory.
 * El cliente solo necesita indicar la categoría para obtener la fábrica correcta.
 */
@Component
public class ProductoFactoryProvider {

    private final Map<String, ProductoFactory> fabricas;

    public ProductoFactoryProvider(List<ProductoFactory> listaFabricas) {
        this.fabricas = listaFabricas.stream()
                .collect(Collectors.toMap(
                        f -> f.getCategoria().toUpperCase(),
                        f -> f
                ));
    }

    /**
     * Retorna la fábrica correspondiente a la categoría de producto.
     * @throws IllegalArgumentException si la categoría no está soportada.
     */
    public ProductoFactory obtenerFabrica(String categoria) {
        ProductoFactory fabrica = fabricas.get(categoria.toUpperCase());
        if (fabrica == null) {
            throw new IllegalArgumentException(
                    "Categoría de producto no soportada: " + categoria +
                    ". Categorías válidas: " + fabricas.keySet()
            );
        }
        return fabrica;
    }
}
