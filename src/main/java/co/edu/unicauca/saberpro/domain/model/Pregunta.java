package co.edu.unicauca.saberpro.domain.model;

import co.edu.unicauca.saberpro.domain.exception.ReglaNegocioException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Raíz del agregado Pregunta.
 * <p>
 * Todo cambio sobre las opciones y el estado pasa por esta clase, que garantiza las invariantes de negocio:
 * <ul>
 *     <li>Las opciones y el contenido solo se modifican en estado {@link EstadoPregunta#BORRADOR}.</li>
 *     <li>Una pregunta no puede tener más de {@value #NUMERO_OPCIONES} opciones ni opciones repetidas.</li>
 *     <li>Para enviarse a revisión debe tener enunciado, justificación, exactamente {@value #NUMERO_OPCIONES}
 *     opciones y exactamente 1 clave correcta.</li>
 *     <li>Los cambios de estado respetan las transiciones de {@link EstadoPregunta}.</li>
 * </ul>
 */
public class Pregunta {

    /** Número exacto de opciones que debe tener una pregunta de selección múltiple. */
    public static final int NUMERO_OPCIONES = 4;

    private Long id;
    private Enunciado enunciado;
    private Justificacion justificacion;
    private final List<OpcionRespuesta> opciones = new ArrayList<>();
    private EstadoPregunta estado = EstadoPregunta.BORRADOR;

    /**
     * Crea una pregunta vacía en borrador. El enunciado y la justificación se pueden asignar después,
     * pero son obligatorios para enviarla a revisión.
     */
    public Pregunta() {
    }

    /**
     * Crea una pregunta en borrador con su enunciado y justificación.
     *
     * @param enunciado     enunciado de la pregunta
     * @param justificacion justificación de la clave
     */
    public Pregunta(Enunciado enunciado, Justificacion justificacion) {
        this.enunciado = enunciado;
        this.justificacion = justificacion;
    }

    /**
     * Asigna el identificador una única vez (lo hace el repositorio al guardar).
     *
     * @param id identificador
     */
    public void asignarId(Long id) {
        if (this.id != null) {
            throw new ReglaNegocioException("La pregunta ya tiene identificador.");
        }
        this.id = id;
    }

    /**
     * Cambia el enunciado. Solo en borrador.
     *
     * @param nuevoEnunciado nuevo enunciado
     */
    public void cambiarEnunciado(Enunciado nuevoEnunciado) {
        exigirBorrador("modificar el enunciado");
        this.enunciado = nuevoEnunciado;
    }

    /**
     * Cambia la justificación. Solo en borrador.
     *
     * @param nuevaJustificacion nueva justificación
     */
    public void cambiarJustificacion(Justificacion nuevaJustificacion) {
        exigirBorrador("modificar la justificación");
        this.justificacion = nuevaJustificacion;
    }

    /**
     * Agrega una opción de respuesta. Solo en borrador y hasta {@value #NUMERO_OPCIONES} opciones.
     *
     * @param texto      texto de la opción
     * @param esCorrecta {@code true} si es la clave
     */
    public void agregarOpcion(String texto, boolean esCorrecta) {
        exigirBorrador("modificar opciones");
        if (opciones.size() >= NUMERO_OPCIONES) {
            throw new ReglaNegocioException("Una pregunta no puede tener más de " + NUMERO_OPCIONES + " opciones.");
        }
        OpcionRespuesta nueva = new OpcionRespuesta(texto, esCorrecta);
        boolean repetida = opciones.stream().anyMatch(o -> o.getTexto().equalsIgnoreCase(nueva.getTexto()));
        if (repetida) {
            throw new ReglaNegocioException("Las opciones de una pregunta no pueden repetirse.");
        }
        opciones.add(nueva);
    }

    /**
     * Quita todas las opciones. Solo en borrador.
     */
    public void quitarOpciones() {
        exigirBorrador("modificar opciones");
        opciones.clear();
    }

    /**
     * Envía la pregunta a revisión después de validar las invariantes.
     */
    public void enviarARevision() {
        exigirBorrador("enviar a revisión");
        validarInvariantesParaEnvio();
        transitarA(EstadoPregunta.PENDIENTE_REVISION);
    }

    private void validarInvariantesParaEnvio() {
        if (enunciado == null) {
            throw new ReglaNegocioException("La pregunta debe tener enunciado.");
        }
        if (justificacion == null) {
            throw new ReglaNegocioException("La pregunta debe tener justificación.");
        }
        if (opciones.size() != NUMERO_OPCIONES) {
            throw new ReglaNegocioException("Debe tener exactamente " + NUMERO_OPCIONES + " opciones de respuesta.");
        }
        long correctas = opciones.stream().filter(OpcionRespuesta::isEsCorrecta).count();
        if (correctas != 1) {
            throw new ReglaNegocioException("Debe existir exactamente 1 opción correcta.");
        }
    }

    /** Inicia la revisión de una pregunta pendiente. */
    public void iniciarRevision() {
        transitarA(EstadoPregunta.EN_REVISION);
    }

    /** Aprueba una pregunta en revisión. */
    public void aprobar() {
        transitarA(EstadoPregunta.APROBADA);
    }

    /** Rechaza la pregunta y la devuelve a borrador para su corrección. */
    public void rechazar() {
        transitarA(EstadoPregunta.BORRADOR);
    }

    /** Publica una pregunta aprobada para usarla en simulacros. */
    public void publicar() {
        transitarA(EstadoPregunta.PUBLICADA);
    }

    /** Archiva una pregunta publicada. */
    public void archivar() {
        transitarA(EstadoPregunta.ARCHIVADA);
    }

    /**
     * Indica si la pregunta puede usarse en un simulacro.
     *
     * @return {@code true} si está aprobada o publicada
     */
    public boolean esUtilizableEnSimulacro() {
        return estado == EstadoPregunta.APROBADA || estado == EstadoPregunta.PUBLICADA;
    }

    private void exigirBorrador(String accion) {
        if (estado != EstadoPregunta.BORRADOR) {
            throw new ReglaNegocioException("Solo se puede " + accion + " en estado Borrador.");
        }
    }

    private void transitarA(EstadoPregunta destino) {
        if (!estado.puedeTransitarA(destino)) {
            throw new ReglaNegocioException("Transición no permitida de " + estado + " a " + destino + ".");
        }
        this.estado = destino;
    }

    public Long getId() {
        return id;
    }

    public Enunciado getEnunciado() {
        return enunciado;
    }

    public Justificacion getJustificacion() {
        return justificacion;
    }

    /**
     * Devuelve las opciones en orden. La lista es de solo lectura para que nadie las modifique sin pasar
     * por la raíz del agregado.
     *
     * @return opciones de respuesta
     */
    public List<OpcionRespuesta> getOpciones() {
        return Collections.unmodifiableList(opciones);
    }

    public EstadoPregunta getEstado() {
        return estado;
    }
}
