import java.text.Normalizer;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Mantiene la configuracion y el estado compartido del arbol B+.
 * Las operaciones se delegan a clases separadas para que cada requisito
 * (buscar, insertar, eliminar, imprimir y verificar) se pueda localizar facilmente.
 */
public class ArbolBMas {
    private static final Pattern MARCAS = Pattern.compile("\\p{Mn}+");

    /** Normaliza espacios, unifica Unicode y convierte el nombre a minusculas. */
    public static String normalizarNombre(String texto) {
        String limpio = texto.replaceAll("(?U)\\s+", " ").trim();
        return Normalizer.normalize(limpio, Normalizer.Form.NFC).toLowerCase(Locale.ROOT);
    }

    private static String sinTildes(String texto) {
        String descompuesto = Normalizer.normalize(texto, Normalizer.Form.NFD);
        return MARCAS.matcher(descompuesto).replaceAll("");
    }

    /** Compara nombres sin distinguir mayusculas ni tildes para ordenarlos. */
    static int comparar(String a, String b) {
        String minusculaA = a.toLowerCase(Locale.ROOT);
        String minusculaB = b.toLowerCase(Locale.ROOT);
        int resultado = sinTildes(minusculaA).compareTo(sinTildes(minusculaB));
        return resultado != 0 ? resultado : minusculaA.compareTo(minusculaB);
    }

    private static String fmtClaves(String[] claves, int desde, int hasta) {
        if (hasta <= desde) return "[ vacio ]";
        StringBuilder texto = new StringBuilder("[");
        for (int i = desde; i < hasta; i++) {
            if (i > desde) texto.append(" | ");
            texto.append(claves[i]);
        }
        return texto.append("]").toString();
    }

    static String fmt(Nodo nodo) {
        return fmtClaves(nodo.claves, 0, nodo.numClaves);
    }

    static String fmtCompacto(Nodo nodo) {
        StringBuilder texto = new StringBuilder("[");
        for (int i = 0; i < nodo.numClaves; i++) {
            if (i > 0) texto.append("|");
            texto.append(nodo.claves[i]);
        }
        return texto.append("]").toString();
    }

    private static String repetir(char caracter, int veces) {
        StringBuilder texto = new StringBuilder();
        for (int i = 0; i < veces; i++) texto.append(caracter);
        return texto.toString();
    }

    static class Paso {
        // Guarda un nodo interno y el hijo elegido para poder volver por el camino.
        final Nodo nodo;
        final int i;

        Paso(Nodo nodo, int i) {
            this.nodo = nodo;
            this.i = i;
        }
    }

    static class Descenso {
        // Resultado de bajar desde la raiz hasta la hoja que corresponde a una clave.
        Nodo hoja;
        Paso[] camino;
        int numCamino;
        int nivel;
    }

    static class Posicion {
        // Posicion de insercion en una hoja y si la clave ya estaba presente.
        final int pos;
        final boolean hallado;

        Posicion(int pos, boolean hallado) {
            this.pos = pos;
            this.hallado = hallado;
        }
    }

    static class Division {
        // Separador que sube al padre y nuevo nodo creado durante un split.
        final String sep;
        final Nodo nuevo;

        Division(String sep, Nodo nuevo) {
            this.sep = sep;
            this.nuevo = nuevo;
        }
    }

    // Estado y limites de ocupacion del arbol segun el orden m.
    final int orden;
    final int maxClaves;     // hojas e internos
    final int minHoja;       // entradas minimas (no raiz)
    final int minInterno;    // ceil(m/2) - 1  (no raiz)
    Nodo raiz;
    int total;

    public ArbolBMas(int orden) {
        this.orden = orden;
        this.maxClaves = orden - 1;
        // Las hojas necesitan al menos floor(m/2) claves; los internos,
        // ceil(m/2)-1 separadores. La raiz tiene reglas especiales.
        this.minHoja = orden / 2;
        this.minInterno = (orden + 1) / 2 - 1;
        this.raiz = new Nodo(true, orden);  // el arbol INICIA VACIO
        this.total = 0;
    }

    public int getOrden()      { return orden; }
    public int getMaxClaves()  { return maxClaves; }
    public int getMinHoja()    { return minHoja; }
    public int getMinInterno() { return minInterno; }

    // trazas
    static void t(String texto) {
        System.out.println("   > " + texto);
    }

    static void titulo(String texto) {
        System.out.println("\n" + repetir('*', 70));
        System.out.println(texto);
        System.out.println(repetir('*', 70));
    }

    /** Cantidad de niveles (la raiz es el nivel 0). */
    int altura() {
        Nodo n = raiz;
        int h = 1;
        while (!n.esHoja) {
            n = n.hijos[0];
            h++;
        }
        return h;
    }

    // navegacion
    /** Indice del hijo por donde se debe descender (clave >= separador -> derecha). */
    private static int indiceHijo(Nodo nodo, String clave) {
        int i = 0;
        // Los separadores son el minimo del subarbol derecho:
        // si la clave es igual al separador, se debe bajar por el hijo derecho.
        while (i < nodo.numClaves && comparar(nodo.claves[i], clave) <= 0) {
            i++;
        }
        return i;
    }

    private static String explicar(Nodo nodo, String clave, int i) {
        String[] ks = nodo.claves;
        String txt;
        if (i == 0) {
            txt = "'" + clave + "' < '" + ks[0] + "' -> hijo P0";
        } else if (i == nodo.numClaves) {
            txt = "'" + clave + "' >= '" + ks[nodo.numClaves - 1] + "' -> hijo P" + i + " (ultimo)";
        } else {
            txt = "'" + ks[i - 1] + "' <= '" + clave + "' < '" + ks[i] + "' -> hijo P" + i;
        }
        if (i > 0 && comparar(ks[i - 1], clave) == 0) {
            txt += " | la clave coincide con un separador: solo es una guia, "
                 + "\nla entrada real esta en una hoja, se sigue descendiendo";
        }
        return txt;
    }

    /** Baja desde la raiz hasta la hoja correspondiente. */
    Descenso descender(String clave) {
        Descenso d = new Descenso();
        // Se reserva espacio para guardar un paso por cada nivel del arbol.
        d.camino = new Paso[altura()];
        Nodo nodo = raiz;
        int nivel = 0;
        while (!nodo.esHoja) {
            String rol = (nodo == raiz) ? "raiz" : "interno";
            int i = indiceHijo(nodo, clave);
            t("Nivel " + nivel + " (" + rol + ") " + fmt(nodo) + ": " + explicar(nodo, clave, i));
            d.camino[d.numCamino++] = new Paso(nodo, i);
            nodo = nodo.hijos[i];
            nivel++;
        }
        d.hoja = nodo;
        d.nivel = nivel;
        return d;
    }

    /** Busqueda secuencial dentro de la hoja. */
    static Posicion posEnHoja(Nodo hoja, String clave) {
        for (int idx = 0; idx < hoja.numClaves; idx++) {
            int c = comparar(hoja.claves[idx], clave);
            if (c == 0) return new Posicion(idx, true);
            if (c > 0) return new Posicion(idx, false);
        }
        return new Posicion(hoja.numClaves, false);
    }

    String nombreHoja(Nodo hoja) {
        return hoja == raiz ? "hoja raiz" : "hoja";
    }

    static String minClave(Nodo nodo) {
        // El primer valor de un subarbol siempre esta en su hoja mas a la izquierda.
        while (!nodo.esHoja) {
            nodo = nodo.hijos[0];
        }
        return nodo.claves[0];
    }

    // API publica: mantiene Main desacoplado de las clases que implementan cada operacion.
    public boolean buscar(String nombre) {
        return new BusquedaArbolBMas(this).buscar(nombre);
    }

    // INSERTAR
    public boolean insertar(String nombre) {
        return new InsercionArbolBMas(this).insertar(nombre);
    }

    // ELIMINAR
    public boolean eliminar(String nombre) {
        return new EliminacionArbolBMas(this).eliminar(nombre);
    }

    // IMPRIMIR
    public void imprimir() {
        new ImpresorArbolBMas(this).imprimir();
    }

    // VERIFICAR
    public String[] verificar() {
        return new VerificadorArbolBMas(this).verificar();
    }

    void cerrar(String resumen) {
        String[] errores = verificar();
        System.out.println("\nResumen: " + resumen);
        System.out.println("Estado: " + total + " nombre(s) | " + altura() + " nivel(es)");
        if (errores.length > 0) {
            System.out.println("Verificacion de propiedades B+: ERROR");
            for (int k = 0; k < errores.length; k++) {
                System.out.println("   - " + errores[k]);
            }
        } else {
            System.out.println("Verificacion de propiedades B+: Correcta "
                    + "(orden, ocupacion, balance de altura y enlaces de hojas)");
        }
        imprimir();
    }
}
