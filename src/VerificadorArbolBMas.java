/**
 * Recorre el arbol para comprobar orden, rangos, ocupacion, balance de altura,
 * separadores y la secuencia de hojas enlazadas.
 */
final class VerificadorArbolBMas {
    private final ArbolBMas arbol;
    private static class Verificacion {
        String[] errores = new String[0];
        Nodo[] hojas;
        int numHojas;
        int profundidad = -1;
        boolean profundidadesDistintas;

        void error(String mensaje) {
            String[] nuevos = new String[errores.length + 1];
            System.arraycopy(errores, 0, nuevos, 0, errores.length);
            nuevos[errores.length] = mensaje;
            errores = nuevos;
        }
    }

    VerificadorArbolBMas(ArbolBMas arbol) {
        this.arbol = arbol;
    }

    public String[] verificar() {
        Verificacion v = new Verificacion();
        // Se guarda cada hoja en orden de recorrido para contrastar los enlaces al final.
        v.hojas = new Nodo[contarHojas(arbol.raiz)];
        verificarRec(arbol.raiz, 0, null, null, v);
        if (v.profundidadesDistintas) {
            v.error("Las hojas no estan todas a la misma profundidad");
        }
        for (int k = 0; k + 1 < v.numHojas; k++) {
            if (v.hojas[k].siguiente != v.hojas[k + 1]) {
                v.error("Enlace entre hojas incorrecto");
            }
        }
        if (v.numHojas > 0 && v.hojas[v.numHojas - 1].siguiente != null) {
            v.error("La ultima hoja debe apuntar a NULL");
        }
        int suma = 0;
        for (int k = 0; k < v.numHojas; k++) suma += v.hojas[k].numClaves;
        if (suma != arbol.total) {
            v.error("La cantidad de entradas no coincide con el total");
        }
        return v.errores;
    }

    private static int contarHojas(Nodo nodo) {
        if (nodo.esHoja) return 1;
        int cuenta = 0;
        for (int k = 0; k < nodo.numHijos; k++) {
            cuenta += contarHojas(nodo.hijos[k]);
        }
        return cuenta;
    }

    private void verificarRec(Nodo nodo, int nivel, String inf, String sup, Verificacion v) {
        boolean esRaiz = (nodo == arbol.raiz);
        String[] ks = nodo.claves;
        int n = nodo.numClaves;
        for (int k = 0; k + 1 < n; k++) {
            if (ArbolBMas.comparar(ks[k], ks[k + 1]) >= 0) {
                v.error("Claves desordenadas en " + arbol.fmt(nodo));
            }
        }
        for (int k = 0; k < n; k++) {
            if (inf != null && ArbolBMas.comparar(ks[k], inf) < 0) {
                v.error("'" + ks[k] + "' fuera de rango en " + arbol.fmt(nodo));
            }
            if (sup != null && ArbolBMas.comparar(ks[k], sup) >= 0) {
                v.error("'" + ks[k] + "' fuera de rango en " + arbol.fmt(nodo));
            }
        }
        if (n > arbol.maxClaves) {
            v.error("Nodo con exceso de claves: " + arbol.fmt(nodo));
        }
        if (nodo.esHoja) {
            // Todas las hojas deben aparecer a la misma profundidad y respetar minHoja.
            if (v.profundidad == -1) v.profundidad = nivel;
            else if (v.profundidad != nivel) v.profundidadesDistintas = true;
            v.hojas[v.numHojas++] = nodo;
            if (!esRaiz && n < arbol.minHoja) {
                v.error("Hoja bajo el minimo: " + arbol.fmt(nodo));
            }
            return;
        }
        if (nodo.numHijos != n + 1) {
            v.error("k claves deben tener k+1 hijos en " + arbol.fmt(nodo));
            return;
        }
        // Una raiz interna necesita al menos un separador; las demas, minInterno.
        int minimo = esRaiz ? 1 : arbol.minInterno;
        if (n < minimo) {
            v.error("Nodo interno bajo el minimo: " + arbol.fmt(nodo));
        }
        // Cada hijo recibe el intervalo de claves que le corresponde entre separadores.
        for (int j = 0; j < nodo.numHijos; j++) {
            String lo = (j == 0) ? inf : ks[j - 1];
            String hi = (j == n) ? sup : ks[j];
            verificarRec(nodo.hijos[j], nivel + 1, lo, hi, v);
        }
        for (int j = 0; j < n; j++) {
            if (!ks[j].equals(arbol.minClave(nodo.hijos[j + 1]))) {
                v.error("Separador '" + ks[j] + "' no es la menor clave de su "
                        + "subarbol derecho");
            }
        }
    }
}
