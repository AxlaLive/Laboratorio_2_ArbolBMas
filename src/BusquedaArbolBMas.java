/**
 * Implementa la busqueda: los nodos internos guian hacia una hoja,
 * pero las claves reales se confirman unicamente en las hojas.
 */
final class BusquedaArbolBMas {
    private final ArbolBMas arbol;

    BusquedaArbolBMas(ArbolBMas arbol) {
        this.arbol = arbol;
    }

    public boolean buscar(String nombre) {
        arbol.titulo("Buscar '" + nombre + "'");
        if (arbol.total == 0) {
            arbol.t("El arbol esta vacio (la raiz es una hoja sin entradas).");
        }
        // Los separadores solo indican el camino; no son las entradas almacenadas.
        ArbolBMas.Descenso d = arbol.descender(nombre);
        arbol.t("Nivel " + d.nivel + " (" + arbol.nombreHoja(d.hoja) + ") " + arbol.fmt(d.hoja)
                + ": busqueda secuencial de la clave dentro de la hoja");
        // Al llegar a la hoja se compara en orden hasta hallar la clave o su lugar.
        ArbolBMas.Posicion p = arbol.posEnHoja(d.hoja, nombre);
        if (p.hallado) {
            arbol.t("Entrada encontrada en la posicion " + (p.pos + 1) + " de la hoja.");
            System.out.println("\nResultado: el nombre '" + d.hoja.claves[p.pos]
                    + "' Si existe en el arbol.");
        } else {
            arbol.t("La clave no esta en la hoja que le corresponde; una clave "
                    + "ausente siempre se confirma revisando su hoja.");
            System.out.println("\nResultado: el nombre '" + nombre + "' No existe en el arbol.");
        }
        return p.hallado;
    }
}
