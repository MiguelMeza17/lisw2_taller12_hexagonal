package co.edu.unicauca.saberpro.domain.model;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Comprobaciones compartidas de inmutabilidad para los objetos de valor del dominio:
 * la clase es {@code final}, todos sus atributos de instancia son {@code private final}
 * y no expone setters.
 */
final class ValueObjects {

    private ValueObjects() {
    }

    static void assertInmutable(Class<?> tipo) {
        assertTrue(Modifier.isFinal(tipo.getModifiers()), tipo.getSimpleName() + " debe ser final");

        Arrays.stream(tipo.getDeclaredFields())
                .filter(f -> !Modifier.isStatic(f.getModifiers()))
                .forEach(f -> {
                    assertTrue(Modifier.isFinal(f.getModifiers()),
                            tipo.getSimpleName() + "." + f.getName() + " debe ser final");
                    assertTrue(Modifier.isPrivate(f.getModifiers()),
                            tipo.getSimpleName() + "." + f.getName() + " debe ser privado");
                });

        for (Method m : tipo.getDeclaredMethods()) {
            assertFalse(m.getName().startsWith("set"),
                    tipo.getSimpleName() + " no debe tener setters: " + m.getName());
        }
    }
}
