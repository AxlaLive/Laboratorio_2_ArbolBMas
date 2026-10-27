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
        
        System.out.println("[1. Descenso en el Arbol]");
        // Guardar el camino permite actualizar padres cuando se propaga un split.
        ArbolBMas.Descenso d = arbol.descender(nombre);
        Nodo hoja = d.hoja;
        arbol.t("Nivel " + d.nivel + " (" + arbol.nombreHoja(hoja) + ") " + arbol.fmt(hoja)
                + ": se llego a la hoja correspondiente.");
        ArbolBMas.Posicion p = arbol.posEnHoja(hoja, nombre);

        // Paso 1: no se permiten claves duplicadas.
        if (p.hallado) {
            arbol.t("Clave duplicada: '" + hoja.claves[p.pos] + "' ya existe en la hoja.");
            System.out.println("\n--------------------------------------------------------------");
            System.out.println("Resultado: No se inserto '" + nombre + "' por ser duplicado. El arbol no cambio.");
            System.out.println("--------------------------------------------------------------");
            return false;
        }

        System.out.println("\n[2. Insercion y Capacidad]");
        // Paso 2: insertar ordenadamente en la hoja, desplazando las claves mayores.
        String antes = arbol.fmt(hoja);
        hoja.insertarClave(p.pos, nombre);
        arbol.total++;
        arbol.t("Insercion ordenada en la hoja:");
        arbol.t("   " + antes + " -> " + arbol.fmt(hoja));

        // Una hoja llena todavia es valida; el split ocurre solo al exceder maxClaves.
        if (hoja.numClaves <= arbol.maxClaves) {
            arbol.t("Capacidad de la hoja: " + hoja.numClaves + "/" + arbol.maxClaves
                    + ". Sin overflow, sin split.");
            System.out.println("\n--------------------------------------------------------------");
            System.out.println("Resultado: '" + nombre + "' insertado correctamente (sin split).");
            System.out.println("--------------------------------------------------------------");
            arbol.cerrar("Insercion simple, sin split.");
            return true;
        }

        System.out.println("\n[3. Propagacion Y Splits]");
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
                arbol.t("Split de la raiz (" + origen + "): Se crea nueva raiz interna");
                arbol.t("   " + arbol.fmt(nueva) + " con 2 hijos.");
                arbol.t("La altura aumenta: ahora hay " + arbol.altura() + " niveles.");
                break;
            }
            ArbolBMas.Paso paso = d.camino[--d.numCamino];
            Nodo padre = paso.nodo;
            int i = paso.i;
            antes = arbol.fmt(padre);
            padre.insertarClave(i, div.sep);
            padre.insertarHijo(i + 1, div.nuevo);
            arbol.t("Se inserta el separador '" + div.sep + "' en el padre:");
            arbol.t("   " + antes + " -> " + arbol.fmt(padre) + " (" + padre.numHijos + " hijos)");
            if (padre.numClaves <= arbol.maxClaves) {
                arbol.t("El padre queda con " + padre.numClaves + "/" + arbol.maxClaves
                        + " claves: no hay overflow, fin de la propagacion.");
                break;
            }
            div = splitInterno(padre);
            nodo = padre;
        }
        
        System.out.println("\n--------------------------------------------------------------");
        System.out.println("Resultado: '" + nombre + "' insertado con Split.");
        System.out.println("--------------------------------------------------------------");
        arbol.cerrar("Insercion con Split.");
        return true;
    }

    private ArbolBMas.Division splitHoja(Nodo hoja) {
        int n = hoja.numClaves;
        // La mediana inferior permanece a la izquierda; la primera clave derecha
        // se copia al padre y tambien sigue siendo una entrada real de la hoja.
        int idx = (n - 1) / 2;
        arbol.t("Overflow en hoja: estado temporal " + arbol.fmt(hoja));
        arbol.t("   (" + n + " entradas, maximo permitido " + arbol.maxClaves + ")");
        arbol.t("*** Split de la hoja ***");
        arbol.t("Mediana inferior: '" + hoja.claves[idx] + "' (indice " + idx + "); se queda a la izquierda.");
        Nodo nuevo = new Nodo(true, arbol.orden);
        for (int k = idx + 1; k < n; k++) {          // mitad derecha pasa al nodo nuevo
            nuevo.claves[nuevo.numClaves++] = hoja.claves[k];
            hoja.claves[k] = null;
        }
        hoja.numClaves = idx + 1;
        nuevo.siguiente = hoja.siguiente;
        hoja.siguiente = nuevo;
        String sep = nuevo.claves[0];
        arbol.t("Hoja izquierda: " + arbol.fmt(hoja));
        arbol.t("Hoja derecha:   " + arbol.fmt(nuevo));
        arbol.t("Se copia al padre la primera clave de la hoja derecha: '" + sep + "'");
        arbol.t("Enlaces entre hojas actualizados.");
        return new ArbolBMas.Division(sep, nuevo);
    }

    private ArbolBMas.Division splitInterno(Nodo nodo) {
        int n = nodo.numClaves;
        // En nodos internos la mediana se promueve y deja de pertenecer a ambos hijos.
        int idx = (n - 1) / 2;
        arbol.t("Overflow en nodo interno: estado temporal " + arbol.fmt(nodo));
        arbol.t("   (" + n + " claves, maximo permitido " + arbol.maxClaves + ")");
        arbol.t("*** Split Interno ***");
        String sep = nodo.claves[idx];
        arbol.t("Mediana inferior: '" + sep + "' (indice " + idx + "); asciende al padre.");
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
        arbol.t("Nodo izquierdo: " + arbol.fmt(nodo) + " (" + nodo.numHijos + " hijos)");
        arbol.t("Nodo derecho:   " + arbol.fmt(nuevo) + " (" + nuevo.numHijos + " hijos)");
        return new ArbolBMas.Division(sep, nuevo);
    }
}