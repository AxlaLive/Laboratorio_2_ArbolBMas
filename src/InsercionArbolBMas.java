/**
 * Inserta primero en la hoja correspondiente. Si una hoja o un interno supera
 * su capacidad, divide el nodo y propaga el separador hacia la raiz si hace falta.
 */
final class InsercionArbolBMas {
    private final ArbolBMas arbol;

    InsercionArbolBMas(ArbolBMas arbol) {
        this.arbol = arbol;
    }

    public boolean insertar(String nombre) {
        arbol.titulo("Insertar '" + nombre + "'");
        // Guardar el camino permite actualizar padres cuando se propaga un split.
        ArbolBMas.Descenso d = arbol.descender(nombre);
        Nodo hoja = d.hoja;
        arbol.t("Nivel " + d.nivel + " (" + arbol.nombreHoja(hoja) + ") " + arbol.fmt(hoja)
                + ": se llego a la hoja donde corresponde la clave");
        ArbolBMas.Posicion p = arbol.posEnHoja(hoja, nombre);

        // Paso 1: no se permiten claves duplicadas.
        if (p.hallado) {
            arbol.t("Clave duplicada: '" + hoja.claves[p.pos] + "' ya existe en la hoja.");
            System.out.println("\nResultados: no se inserto '" + nombre + "' porque ya existe "
                    + "(clave duplicada). El arbol no cambio.");
            return false;
        }

        // Paso 2: insertar ordenadamente en la hoja, desplazando las claves mayores.
        String antes = arbol.fmt(hoja);
        hoja.insertarClave(p.pos, nombre);
        arbol.total++;
        arbol.t("Insercion ordenada en la hoja: " + antes + " -> " + arbol.fmt(hoja));

        // Una hoja llena todavia es valida; el split ocurre solo al exceder maxClaves.
        if (hoja.numClaves <= arbol.maxClaves) {
            arbol.t("Capacidad de la hoja: " + hoja.numClaves + "/" + arbol.maxClaves
                    + ". NO hay overflow (un nodo lleno no es overflow). NO hubo split.");
            arbol.cerrar("Insercion simple, sin split.");
            return true;
        }

        // Si hay overflow, dividir y llevar el separador al padre; repetir si este tambien se llena.
        ArbolBMas.Division div = splitHoja(hoja);
        Nodo nodo = hoja;
        while (true) {
            if (d.numCamino == 0) {
                Nodo nueva = new Nodo(false, arbol.orden);
                nueva.insertarClave(0, div.sep);
                nueva.insertarHijo(0, nodo);
                nueva.insertarHijo(1, div.nuevo);
                arbol.raiz = nueva;
                String origen = nodo.esHoja ? "hoja" : "interna";
                arbol.t("SPlit de la raiz: la raiz era " + origen + " y se dividio. Se crea una "
                        + "nueva raiz interna " + arbol.fmt(nueva) + " con 2 hijos. "
                        + "La Altura aumenta ahora hay " + arbol.altura() + " niveles.");
                break;
            }
            ArbolBMas.Paso paso = d.camino[--d.numCamino];
            Nodo padre = paso.nodo;
            int i = paso.i;
            antes = arbol.fmt(padre);
            padre.insertarClave(i, div.sep);
            padre.insertarHijo(i + 1, div.nuevo);
            arbol.t("Se inserta el separador '" + div.sep + "' en el padre: " + antes + " -> "
                    + arbol.fmt(padre) + " (" + padre.numHijos + " hijos)");
            if (padre.numClaves <= arbol.maxClaves) {
                arbol.t("El padre queda con " + padre.numClaves + "/" + arbol.maxClaves
                        + " claves: no hay overflow, la division no se propaga mas.");
                break;
            }
            div = splitInterno(padre);
            nodo = padre;
        }
        arbol.cerrar("Insercion con Split.");
        return true;
    }

    private ArbolBMas.Division splitHoja(Nodo hoja) {
        int n = hoja.numClaves;
        // La mediana inferior permanece a la izquierda; la primera clave derecha
        // se copia al padre y tambien sigue siendo una entrada real de la hoja.
        int idx = (n - 1) / 2;
        arbol.t("Overflow en hoja: estado temporal " + arbol.fmt(hoja)
                + " (" + n + " entradas, maximo permitido " + arbol.maxClaves + ")");
        arbol.t("*** Split de la hoja ***");
        arbol.t("Mediana inferior: '" + hoja.claves[idx] + "' (indice " + idx + " de " + n
                + " elementos); se queda en la hoja izquierda");
        Nodo nuevo = new Nodo(true, arbol.orden);
        for (int k = idx + 1; k < n; k++) {          // mitad derecha pasa al nodo nuevo
            nuevo.claves[nuevo.numClaves++] = hoja.claves[k];
            hoja.claves[k] = null;
        }
        hoja.numClaves = idx + 1;
        nuevo.siguiente = hoja.siguiente;
        hoja.siguiente = nuevo;
        String sep = nuevo.claves[0];
        arbol.t("Hoja izquierda: " + arbol.fmt(hoja) + "   Hoja derecha: " + arbol.fmt(nuevo));
        arbol.t("Se copia al padre la primera clave de la hoja derecha: '" + sep + "' "
                + "(tambien permanece en la hoja derecha como entrada real)");
        arbol.t("Enlaces entre hojas actualizados (izquierda -> derecha -> antigua siguiente)");
        return new ArbolBMas.Division(sep, nuevo);
    }

    private ArbolBMas.Division splitInterno(Nodo nodo) {
        int n = nodo.numClaves;
        // En nodos internos la mediana se promueve y deja de pertenecer a ambos hijos.
        int idx = (n - 1) / 2;
        arbol.t("Overflow en nodo interno: estado temporal " + arbol.fmt(nodo)
                + " (" + n + " claves, maximo permitido " + arbol.maxClaves + ")");
        arbol.t("*** Split Interno ***");
        String sep = nodo.claves[idx];
        arbol.t("Mediana inferior: '" + sep + "' (indice " + idx + "); se Promueve: asciende al "
                + "padre y se retira de ambos nodos resultantes");
        Nodo nuevo = new Nodo(false, arbol.orden);
        for (int k = idx + 1; k < n; k++) {          // claves de la derecha
            nuevo.claves[nuevo.numClaves++] = nodo.claves[k];
            nodo.claves[k] = null;
        }
        for (int k = idx + 1; k <= n; k++) {         // punteros P(idx+1)..Pn
            nuevo.hijos[nuevo.numHijos++] = nodo.hijos[k];
            nodo.hijos[k] = null;
        }
        nodo.claves[idx] = null;                     // la mediana sube: se retira
        nodo.numClaves = idx;
        nodo.numHijos = idx + 1;                     // punteros P0..Pidx
        arbol.t("Nodo izquierdo: " + arbol.fmt(nodo) + " recibe los punteros P0..P" + idx
                + " (" + nodo.numHijos + " hijos)");
        arbol.t("Nodo derecho:   " + arbol.fmt(nuevo) + " recibe los punteros P" + (idx + 1) + "..P" + n
                + " (" + nuevo.numHijos + " hijos)");
        return new ArbolBMas.Division(sep, nuevo);
    }
}
