/**
 * Nodo de un Arbol B+, implementado solo con arreglos.
 *  - Hoja    : 'claves' son las entradas reales y 'siguiente' enlaza con la hoja vecina.
 *  - Interno : 'claves' son separadores y 'hijos' los punteros (k claves -> k+1 hijos).
 *
 * Capacidad de los arreglos: m claves y m+1 hijos, es decir, UNA posicion mas que el
 * maximo permitido (m-1 claves), para poder guardar el estado temporal de overflow
 * antes de hacer el split.
 */
public class Nodo {
    // esHoja distingue si las claves son datos reales o separadores de navegacion.
    boolean esHoja;
    String[] claves;
    int numClaves;
    Nodo[] hijos;       // solo nodos internos
    int numHijos;
    Nodo siguiente;     // solo hojas

    Nodo(boolean esHoja, int orden) {
        this.esHoja = esHoja;
        this.claves = new String[orden];
        this.hijos = esHoja ? null : new Nodo[orden + 1];
    }

    /** Inserta una clave en la posicion pos, corriendo las demas a la derecha. */
    void insertarClave(int pos, String clave) {
        // Se desplaza desde el final para abrir espacio sin perder claves.
        for (int k = numClaves; k > pos; k--) {
            claves[k] = claves[k - 1];
        }
        claves[pos] = clave;
        numClaves++;
    }

    /** Quita la clave de la posicion pos, corriendo las demas a la izquierda. */
    String quitarClave(int pos) {
        String quitada = claves[pos];
        for (int k = pos; k < numClaves - 1; k++) {
            claves[k] = claves[k + 1];
        }
        numClaves--;
        claves[numClaves] = null;
        return quitada;
    }

    /** Inserta un puntero a hijo en la posicion pos. */
    void insertarHijo(int pos, Nodo hijo) {
        // Mantiene los hijos contiguos y en el mismo orden que sus separadores.
        for (int k = numHijos; k > pos; k--) {
            hijos[k] = hijos[k - 1];
        }
        hijos[pos] = hijo;
        numHijos++;
    }

    /** Quita el puntero a hijo de la posicion pos. */
    Nodo quitarHijo(int pos) {
        Nodo quitado = hijos[pos];
        for (int k = pos; k < numHijos - 1; k++) {
            hijos[k] = hijos[k + 1];
        }
        numHijos--;
        hijos[numHijos] = null;
        return quitado;
    }
}
