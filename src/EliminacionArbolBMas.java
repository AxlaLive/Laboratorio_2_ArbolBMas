/**
 * Elimina una entrada real de una hoja y repara cualquier perdida de ocupacion:
 * primero intenta prestar desde hermanos y, si no es posible, fusiona nodos.
 */
final class EliminacionArbolBMas {
    private final ArbolBMas arbol;

    EliminacionArbolBMas(ArbolBMas arbol) {
        this.arbol = arbol;
    }

    public boolean eliminar(String nombre) {
        arbol.titulo("Eliminar '" + nombre + "'");
        // La entrada se busca en la hoja; si no existe, el arbol queda intacto.
        ArbolBMas.Descenso d = arbol.descender(nombre);
        Nodo hoja = d.hoja;
        arbol.t("Nivel " + d.nivel + " (" + arbol.nombreHoja(hoja) + ") " + arbol.fmt(hoja)
                + ": busqueda secuencial de la entrada real");
        ArbolBMas.Posicion p = arbol.posEnHoja(hoja, nombre);
        if (!p.hallado) {
            System.out.println("\nRESULTADO: el nombre '" + nombre + "' No existe; no hay nada que "
                    + "eliminar. El arbol no cambio.");
            return false;
        }

        String eliminado = hoja.claves[p.pos];
        String antes = arbol.fmt(hoja);
        hoja.quitarClave(p.pos);
        arbol.total--;
        arbol.t("Se elimina la entrada real '" + eliminado + "' de la hoja: " + antes + " -> " + arbol.fmt(hoja));

        // Caso 1 es la eliminacion sin underflow; el escenario puede cambiar
        // a 2a/2b por una reparacion en hojas o a 3 si afecta internos/la raiz.
        String caso = "1";
        Nodo nodo = hoja;
        while (true) {
            // ---------- el nodo actual es la raiz
            if (nodo == arbol.raiz) {
                if (nodo.esHoja) {
                    arbol.t("CASO 1 - Eliminacion simple: la raiz es una hoja y puede "
                            + "tener menos del minimo; no hay underflow.");
                    if (nodo.numClaves == 0) {
                        arbol.t("El arbol quedo vacio.");
                    }
                } else if (nodo.numClaves == 0) {
                    caso = "3";
                    Nodo nueva = nodo.hijos[0];
                    arbol.t("CASO 3 - La raiz interna quedo sin claves (un solo hijo). "
                            + "Ese hijo " + arbol.fmt(nueva) + " pasa a ser la nueva raiz.");
                    arbol.raiz = nueva;
                    arbol.t("La altura disminuye: ahora hay " + arbol.altura() + " niveles.");
                }
                break;
            }

            int minimo = nodo.esHoja ? arbol.minHoja : arbol.minInterno;
            String tipo = nodo.esHoja ? "hoja" : "nodo interno";

            // Si el nodo conserva su minimo de claves, la reparacion termina aqui.
            if (nodo.numClaves >= minimo) {
                if (caso.equals("1")) {
                    arbol.t("CASO 1 - Eliminacion simple: la hoja queda con "
                            + nodo.numClaves + " entradas (minimo " + minimo + "). Sin underflow, "
                            + "sin prestamo ni merge.");
                } else {
                    arbol.t("El " + tipo + " " + arbol.fmt(nodo) + " cumple la ocupacion minima "
                            + "(" + nodo.numClaves + " claves, minimo " + minimo + "): el underflow "
                            + "no se propaga mas.");
                }
                break;
            }

            // La eliminacion dejo menos claves que el minimo y hay que redistribuir o fusionar.
            if (nodo.esHoja) {
                arbol.t("Underflow en hoja " + arbol.fmt(nodo) + ": " + nodo.numClaves + " entradas "
                        + "(minimo " + minimo + ").");
            } else {
                caso = "3";
                arbol.t("CASO 3 - El underflow se propaga a un nodo interno: "
                        + arbol.fmt(nodo) + " tiene " + nodo.numClaves + " claves (minimo " + minimo + ").");
            }

            ArbolBMas.Paso paso = d.camino[--d.numCamino];
            Nodo padre = paso.nodo;
            int i = paso.i;
            Nodo izq = i > 0 ? padre.hijos[i - 1] : null;
            Nodo der = i + 1 < padre.numHijos ? padre.hijos[i + 1] : null;

            // El orden requerido es: intentar hermano izquierdo antes que el derecho.
            if (izq != null) {
                boolean puede = izq.numClaves > minimo;
                arbol.t("Hermano izquierdo " + arbol.fmt(izq) + ": " + izq.numClaves + " claves "
                        + "(minimo " + minimo + ") -> " + (puede ? "Puede prestar" : "NO puede prestar"));
                if (puede) {
                    if (nodo.esHoja) caso = "2a";
                    prestar(padre, i, nodo, izq, true);
                    break;
                }
            }
            // Si el izquierdo no puede prestar, se prueba con el hermano derecho.
            if (der != null) {
                boolean puede = der.numClaves > minimo;
                arbol.t("Hermano derecho " + arbol.fmt(der) + ": " + der.numClaves + " claves "
                        + "(minimo " + minimo + ") -> " + (puede ? "Puede prestar" : "NO puede prestar"));
                if (puede) {
                    if (nodo.esHoja) caso = "2a";
                    prestar(padre, i, nodo, der, false);
                    break;
                }
            }
            // Si ninguno puede prestar, se fusiona con el izquierdo cuando existe.
            // El padre pierde una clave y tambien puede quedar en underflow.
            if (nodo.esHoja) caso = "2b";
            merge(padre, izq != null ? i - 1 : i);
            nodo = padre;   // el padre perdio un separador y un hijo: se revisa
        }

        // el separador debe seguir siendo la menor clave del subarbol derecho
        actualizarSeparadores(arbol.raiz);

        String nombreCaso;
        switch (caso) {
            case "1":  nombreCaso = "CASO 1 (eliminacion simple, sin underflow)"; break;
            case "2a": nombreCaso = "CASO 2a (underflow resuelto con PRESTAMO de un hermano)"; break;
            case "2b": nombreCaso = "CASO 2b (underflow resuelto con MERGE de hojas, el padre sigue valido)"; break;
            default:   nombreCaso = "CASO 3 (el underflow llego a nodos internos y/o cambio la raiz)"; break;
        }
        System.out.println("\nesultado: '" + eliminado + "' eliminado. Escenario resuelto: "
                + nombreCaso + ".");
        arbol.cerrar("Eliminacion - Caso " + caso + ".");
        return true;
    }

    /** Redistribuye una entrada o un hijo desde un hermano que puede prestar. */
    private void prestar(Nodo padre, int i, Nodo nodo, Nodo hermano, boolean desdeIzq) {
        String lado = desdeIzq ? "izquierdo" : "derecho";
        if (nodo.esHoja) {
            // Prestamo entre HOJAS: se mueve una entrada real
            String sepAnt, sepNuevo, x;
            if (desdeIzq) {
                sepAnt = padre.claves[i - 1];
                x = hermano.quitarClave(hermano.numClaves - 1);
                nodo.insertarClave(0, x);
                padre.claves[i - 1] = nodo.claves[0];
                sepNuevo = padre.claves[i - 1];
            } else {
                sepAnt = padre.claves[i];
                x = hermano.quitarClave(0);
                nodo.insertarClave(nodo.numClaves, x);
                padre.claves[i] = hermano.claves[0];
                sepNuevo = padre.claves[i];
            }
            arbol.t("CASO 2a - Prestamo entre hojas desde el hermano " + lado + ": la entrada "
                    + "real '" + x + "' pasa al nodo en underflow.");
            arbol.t("Resultado: hoja " + arbol.fmt(nodo) + ", hermano " + arbol.fmt(hermano));
            arbol.t("Separador del padre actualizado: '" + sepAnt + "' -> '" + sepNuevo + "' "
                    + "(menor clave del subarbol derecho)");
        } else {
            // Prestamo entre INTERNOS: rotacion a traves del padre (con puntero)
            // El separador baja al nodo necesitado y una clave del hermano sube al padre.
            String baja, sube;
            if (desdeIzq) {
                baja = padre.claves[i - 1];
                sube = hermano.quitarClave(hermano.numClaves - 1);
                Nodo puntero = hermano.quitarHijo(hermano.numHijos - 1);
                nodo.insertarClave(0, baja);
                nodo.insertarHijo(0, puntero);
                padre.claves[i - 1] = sube;
            } else {
                baja = padre.claves[i];
                sube = hermano.quitarClave(0);
                Nodo puntero = hermano.quitarHijo(0);
                nodo.insertarClave(nodo.numClaves, baja);
                nodo.insertarHijo(nodo.numHijos, puntero);
                padre.claves[i] = sube;
            }
            arbol.t("Prestamo entre nodos internos desde el hermano " + lado + ": el separador "
                    + "del padre '" + baja + "' DESCIENDE al nodo y el separador '" + sube + "' del "
                    + "hermano ASCIENDE al padre; tambien se transfiere un puntero a un "
                    + "subarbol completo.");
            arbol.t("Resultado: nodo " + arbol.fmt(nodo) + " (" + nodo.numHijos + " hijos), "
                    + "hermano " + arbol.fmt(hermano) + " (" + hermano.numHijos + " hijos)");
        }
    }

    /** Fusiona los hijos contiguos idx e idx+1 y elimina su separador del padre. */
    private void merge(Nodo padre, int idx) {
        Nodo izq = padre.hijos[idx];
        Nodo der = padre.hijos[idx + 1];
        String sep = padre.claves[idx];
        String antIzq = arbol.fmt(izq);
        String antDer = arbol.fmt(der);
        if (izq.esHoja) {
            // En hojas se copian entradas y se conserva el enlace hacia la siguiente hoja.
            arbol.t("CASO 2b - Merge (fusion) de hojas: ningun hermano puede prestar.");
            for (int k = 0; k < der.numClaves; k++) {
                izq.claves[izq.numClaves++] = der.claves[k];
            }
            izq.siguiente = der.siguiente;
            arbol.t(antIzq + " + " + antDer + " -> " + arbol.fmt(izq));
            arbol.t("Se elimina el separador '" + sep + "' del padre (NO se agrega al nodo "
                    + "fusionado). Enlaces horizontales actualizados; desaparece una hoja.");
        } else {
            // En internos tambien baja el separador del padre y se transfieren todos los hijos.
            arbol.t("Merge (fusion) de nodos internos: ningun hermano puede prestar.");
            izq.claves[izq.numClaves++] = sep;
            for (int k = 0; k < der.numClaves; k++) {
                izq.claves[izq.numClaves++] = der.claves[k];
            }
            for (int k = 0; k < der.numHijos; k++) {
                izq.hijos[izq.numHijos++] = der.hijos[k];
            }
            arbol.t(antIzq + " + separador del padre '" + sep + "' (desciende) + " + antDer
                    + " -> " + arbol.fmt(izq) + " (" + izq.numHijos + " hijos)");
        }
        padre.quitarClave(idx);
        padre.quitarHijo(idx + 1);
        arbol.t("El padre pierde un separador y un hijo: ahora " + arbol.fmt(padre)
                + " (" + padre.numHijos + " hijos)");
    }

    /** Restablece que cada separador sea la menor clave del subarbol derecho. */
    private void actualizarSeparadores(Nodo nodo) {
        if (nodo.esHoja) return;
        // Se actualiza de abajo hacia arriba usando el minimo de cada subarbol derecho.
        for (int k = 0; k < nodo.numHijos; k++) {
            actualizarSeparadores(nodo.hijos[k]);
        }
        for (int j = 0; j < nodo.numClaves; j++) {
            String correcto = arbol.minClave(nodo.hijos[j + 1]);
            if (!nodo.claves[j].equals(correcto)) {
                arbol.t("Separador actualizado en " + arbol.fmt(nodo) + ": '" + nodo.claves[j] + "' -> "
                        + "'" + correcto + "' (menor clave del subarbol derecho)");
                nodo.claves[j] = correcto;
            }
        }
    }
}
