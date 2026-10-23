/** Dibuja el arbol por niveles y muestra por separado la cadena de hojas enlazadas. */
final class ImpresorArbolBMas {
    private final ArbolBMas arbol;
    private static final int ANCHO_MAX_DIBUJO = 180;
    private static final int SEPARACION = 3;

    ImpresorArbolBMas(ArbolBMas arbol) {
        this.arbol = arbol;
    }

    public void imprimir() {
        arbol.titulo("ARBOL B+ Actual");
        if (arbol.total == 0) {
            System.out.println("(El arbol esta vacio)");
            System.out.println("Orden: " + arbol.orden + " | Nombres almacenados: 0");
            return;
        }
        int niveles = arbol.altura();
        System.out.println("Orden: " + arbol.orden + " | Nombres: " + arbol.total + " | Niveles: " + niveles);

        // Etiquetas de nivel en el margen izquierdo
        String[] etiquetas = new String[niveles];
        int margen = 0;
        for (int k = 0; k < niveles; k++) {
            if (niveles == 1)           etiquetas[k] = "Nivel 0 (raiz y hoja)";
            else if (k == 0)            etiquetas[k] = "Nivel 0 (raiz)";
            else if (k == niveles - 1)  etiquetas[k] = "Nivel " + k + " (hojas)";
            else                        etiquetas[k] = "Nivel " + k + " (internos)";
            margen = Math.max(margen, etiquetas[k].length() + 2);
        }

        System.out.println();
        // Se usa el dibujo horizontal si cabe; para arboles anchos se imprime
        // una estructura vertical que sigue mostrando niveles e hijos.
        int ancho = margen + anchoSubarbol(arbol.raiz);
        if (ancho <= ANCHO_MAX_DIBUJO) {
            // Dibujo horizontal: cada nodo es una caja [a | b], el padre queda centrado
            // sobre sus hijos y se une a ellos con lineas.
            int filas = (niveles - 1) * 4 + 1;
            char[][] lienzo = new char[filas][ancho];
            for (int f = 0; f < filas; f++) {
                for (int c = 0; c < ancho; c++) lienzo[f][c] = ' ';
            }
            pintar(lienzo, arbol.raiz, 0, margen);
            for (int k = 0; k < niveles; k++) {
                escribir(lienzo, k * 4, 0, etiquetas[k]);
            }
            for (int f = 0; f < filas; f++) {
                int fin = ancho;
                while (fin > 0 && lienzo[f][fin - 1] == ' ') fin--;
                System.out.println(new String(lienzo[f], 0, fin));
            }
        } else {
            System.out.println("(El dibujo horizontal no cabe en la consola; se muestra la vista vertical)");
            dibujar(arbol.raiz, 0, "", "");
        }

       // Cadena de hojas enlazadas
        Nodo h = arbol.raiz;
        while (!h.esHoja) h = h.hijos[0];

        StringBuilder cadena = new StringBuilder("Hojas enlazadas: ");
        while (h != null) {
            cadena.append(arbol.fmt(h));
    
            // Solo añade la flecha si queda otra hoja por procesar
            if (h.siguiente != null) {
                cadena.append(" -> ");
            }
            h = h.siguiente;
        }
        System.out.println();
        System.out.println(cadena);
        System.out.println("Leyenda: [a|b] representa un bloque de memoria (nodo). " 
        + "\nLas hojas contienen los registros reales; los nodos superiores actúan como índices de búsqueda.");

    }

    private static int anchoSubarbol(Nodo n) {
        // Calcula recursivamente el ancho necesario para centrar padre e hijos.
        int propio = ArbolBMas.fmtCompacto(n).length();
        if (n.esHoja) return propio;
        int suma = SEPARACION * (n.numHijos - 1);
        for (int k = 0; k < n.numHijos; k++) {
            suma += anchoSubarbol(n.hijos[k]);
        }
        return Math.max(propio, suma);
    }

    private static void escribir(char[][] lienzo, int fila, int col, String texto) {
        for (int k = 0; k < texto.length(); k++) {
            lienzo[fila][col + k] = texto.charAt(k);
        }
    }

    private int pintar(char[][] lienzo, Nodo n, int nivel, int x0) {
        String et = arbol.fmtCompacto(n);
        int ancho = anchoSubarbol(n);
        int fila = nivel * 4;
        if (n.esHoja) {
            int izq = x0 + (ancho - et.length()) / 2;
            escribir(lienzo, fila, izq, et);
            return izq + et.length() / 2;
        }
        int k = n.numHijos;
        int total = SEPARACION * (k - 1);
        for (int j = 0; j < k; j++) total += anchoSubarbol(n.hijos[j]);
        int x = x0 + (ancho - total) / 2;
        // Se dibujan primero los hijos para conocer sus centros y conectar el padre.
        int[] centros = new int[k];
        for (int j = 0; j < k; j++) {
            centros[j] = pintar(lienzo, n.hijos[j], nivel + 1, x);
            x += anchoSubarbol(n.hijos[j]) + SEPARACION;
        }
        int izq = (centros[0] + centros[k - 1]) / 2 - et.length() / 2;
        if (izq < x0) izq = x0;
        if (izq + et.length() > x0 + ancho) izq = x0 + ancho - et.length();
        // si el centro queda a una columna de un hijo, se alinea con el para que la linea sea recta
        for (int j = 0; j < k; j++) {
            int desfase = centros[j] - (izq + et.length() / 2);
            if ((desfase == 1 || desfase == -1)
                    && izq + desfase >= x0 && izq + desfase + et.length() <= x0 + ancho) {
                izq += desfase;
                break;
            }
        }
        escribir(lienzo, fila, izq, et);
        int cp = izq + et.length() / 2;
        // lineas de conexion: bajada desde el padre, barra horizontal y bajadas a los hijos
        int lo = Math.min(cp, centros[0]);
        int hi = Math.max(cp, centros[k - 1]);
        lienzo[fila + 1][cp] = '|';
        for (int c = lo; c <= hi; c++) lienzo[fila + 2][c] = '-';
        lienzo[fila + 2][cp] = '+';
        for (int j = 0; j < k; j++) {
            lienzo[fila + 2][centros[j]] = '+';
            lienzo[fila + 3][centros[j]] = '|';
        }
        return cp;
    }

    private void dibujar(Nodo nodo, int nivel, String cabecera, String prefijo) {
        String tipo;
        if (nodo == arbol.raiz && !nodo.esHoja) tipo = "RAIZ interna";
        else if (nodo == arbol.raiz)            tipo = "RAIZ y hoja";
        else if (nodo.esHoja)             tipo = "hoja";
        else                              tipo = "interno";
        System.out.println(cabecera + arbol.fmt(nodo) + "   <Nivel " + nivel + ", " + tipo + ">");
        if (!nodo.esHoja) {
            int n = nodo.numHijos;
            for (int j = 0; j < n; j++) {
                boolean ult = (j == n - 1);
                dibujar(nodo.hijos[j], nivel + 1,
                        prefijo + (ult ? "`-- " : "|-- ") + "P" + j + ": ",
                        prefijo + (ult ? "    " : "|   "));
            }
        }
    }
}
