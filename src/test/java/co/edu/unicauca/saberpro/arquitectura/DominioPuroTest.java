package co.edu.unicauca.saberpro.arquitectura;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Regla de dependencias del hexágono: el paquete {@code domain} es Java puro. Esta prueba falla si algún
 * archivo de {@code domain} (o de {@code application}, que solo puede depender del dominio) importa Spring,
 * JPA o cualquier clase de {@code infrastructure}. Es la misma verificación que hace el workflow de CI,
 * pero ejecutable localmente con {@code mvn test}.
 */
class DominioPuroTest {

    private static final Path FUENTES = Path.of("src", "main", "java", "co", "edu", "unicauca", "saberpro");

    private static final Pattern FRAMEWORKS = Pattern.compile(
            "^import\\s+(static\\s+)?(org\\.springframework|jakarta\\.persistence|javax\\.persistence|org\\.hibernate)\\.");
    private static final Pattern INFRAESTRUCTURA = Pattern.compile(
            "^import\\s+(static\\s+)?co\\.edu\\.unicauca\\.saberpro\\.infrastructure\\.");

    @Test
    void elDominioNoImportaSpringNiJpa() throws IOException {
        List<String> violaciones = buscar(FUENTES.resolve("domain"), FRAMEWORKS);

        assertTrue(violaciones.isEmpty(), "domain importa frameworks:\n" + String.join("\n", violaciones));
    }

    @Test
    void elDominioNoConoceLaInfraestructura() throws IOException {
        List<String> violaciones = buscar(FUENTES.resolve("domain"), INFRAESTRUCTURA);

        assertTrue(violaciones.isEmpty(), "domain depende de infrastructure:\n" + String.join("\n", violaciones));
    }

    @Test
    void laCapaDeAplicacionSoloDependeDelDominio() throws IOException {
        List<String> violaciones = new ArrayList<>();
        violaciones.addAll(buscar(FUENTES.resolve("application"), FRAMEWORKS));
        violaciones.addAll(buscar(FUENTES.resolve("application"), INFRAESTRUCTURA));

        assertTrue(violaciones.isEmpty(), "application depende de frameworks o infraestructura:\n"
                + String.join("\n", violaciones));
    }

    private static List<String> buscar(Path raiz, Pattern prohibido) throws IOException {
        assertTrue(Files.isDirectory(raiz), "No existe el paquete " + raiz);
        List<String> violaciones = new ArrayList<>();
        try (Stream<Path> archivos = Files.walk(raiz)) {
            for (Path archivo : archivos.filter(p -> p.toString().endsWith(".java")).toList()) {
                List<String> lineas = Files.readAllLines(archivo, StandardCharsets.UTF_8);
                for (int i = 0; i < lineas.size(); i++) {
                    if (prohibido.matcher(lineas.get(i).trim()).find()) {
                        violaciones.add(archivo + ":" + (i + 1) + "  " + lineas.get(i).trim());
                    }
                }
            }
        }
        return violaciones;
    }
}
