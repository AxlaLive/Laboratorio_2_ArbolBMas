import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.nio.charset.Charset;
import java.util.Scanner;

/** LABORATORIO #2 - ARBOLES B+ | Estructura de Datos */
public class Main {

    /** Maximo de caracteres permitido por nombre. */
    private static final int MAX_CARACTERES_NOMBRE = 15;

    private static Scanner entrada;

    /** Se lanza cuando se cierra la entrada estandar (fin de datos). */
    private static class FinEntrada extends RuntimeException {
        private static final long serialVersionUID = 1L;
    }

    private static String leer(String mensaje) {
        System.out.print(mensaje);
        System.out.flush();
        if (!entrada.hasNextLine()) {
            throw new FinEntrada();
        }
        return entrada.nextLine();
    }

    private static int pedirOrden() {
        // Repite la solicitud hasta recibir un entero permitido por el laboratorio.
        while (true) {
            String texto = leer("Ingrese el orden del Arbol B+ (mayor que 3 y menor que 10, "
                    + "es decir de 4 a 9): ").trim();
            int orden;
            try {
                orden = Integer.parseInt(texto);
            } catch (NumberFormatException e) {
                System.out.println("  Entrada invalida: debe ser un numero entero.");
                continue;
            }
            if (orden >= 4 && orden <= 9) {
                return orden;
            }
            System.out.println("  Orden fuera de rango: debe estar entre 4 y 9.");
        }
    }

    /** Un nombre solo puede tener letras, espacios, apostrofes y puntos. */
    private static boolean tieneCaracterInvalido(String nombre) {
        int k = 0;
        while (k < nombre.length()) {
            int cp = nombre.codePointAt(k);
            boolean permitido = Character.isLetter(cp) || cp == ' ' || cp == '-' || cp == '\'' || cp == '.';
            if (!permitido) {
                return true;
            }
            k += Character.charCount(cp);
        }
        return false;
    }

    private static boolean tieneLetra(String nombre) {
        int k = 0;
        while (k < nombre.length()) {
            int cp = nombre.codePointAt(k);
            if (Character.isLetter(cp)) {
                return true;
            }
            k += Character.charCount(cp);
        }
        return false;
    }

    private static String pedirNombre(String mensaje) {
        // Todos los caminos de entrada normalizan y validan el nombre antes de usarlo.
        while (true) {
            String nombre = ArbolBMas.normalizarNombre(leer(mensaje));
            if (nombre.isEmpty()) {
                System.out.println("  El nombre no puede estar vacio.");
            } else if (nombre.codePointCount(0, nombre.length()) > MAX_CARACTERES_NOMBRE) {
                System.out.println("  El nombre es demasiado largo (maximo "
                        + MAX_CARACTERES_NOMBRE + " caracteres).");
            } else if (tieneCaracterInvalido(nombre)) {
                System.out.println("  Nombre no valido: solo se permiten letras, espacios, "
                        + "guiones, apostrofes y puntos (sin numeros ni simbolos).");
            } else if (!tieneLetra(nombre)) {
                System.out.println("  Nombre no valido: debe contener al menos una letra.");
            } else {
                return nombre;
            }
        }
    }

    private static void mostrarMenu() {
        System.out.println("\n" + "*".repeat(70));
        System.out.println("       Menu Principal ARBOL B+");
        System.out.println("*".repeat(70));
        System.out.println(" 1. Insertar un nombre");
        System.out.println(" 2. Buscar un nombre");
        System.out.println(" 3. Eliminar un nombre");
        System.out.println(" 4. Imprimir el arbol");
        System.out.println(" 5. Verificar propiedades del arbol");
        System.out.println(" 0. Salir");
        System.out.println("*".repeat(70));
    }

    private static Charset charsetDe(String propiedad) {
        String nombre = System.getProperty(propiedad);
        try {
            if (nombre != null) return Charset.forName(nombre);
        } catch (Exception e) {
            // se usa el predeterminado
        }
        return Charset.defaultCharset();
    }

    public static void main(String[] args) {
        // Salida y entrada con la codificacion de la consola (tildes y enies)
        try {
            System.setOut(new PrintStream(new FileOutputStream(FileDescriptor.out), false,
                    charsetDe("stdout.encoding").name()));
        } catch (java.io.UnsupportedEncodingException e) {
            // se mantiene la salida estandar
        }
        entrada = new Scanner(System.in, charsetDe("stdin.encoding").name());

        System.out.println(repetir('*', 70));
        System.out.println(" Laboratorio #2 - ARBOLES B+  |  Estructura de Datos");
        System.out.println(repetir('*', 70));
        try {
            // El arbol se crea vacio; las claves solo se agregan por el menu.
            ArbolBMas arbol = new ArbolBMas(pedirOrden());
            System.out.println("\nArbol B+ de orden " + arbol.getOrden() + " creado vacio "
                    + "(hojas: max " + arbol.getMaxClaves() + " / min " + arbol.getMinHoja()
                    + " entradas; internos: max " + arbol.getMaxClaves() + " / min "
                    + arbol.getMinInterno() + " claves; max " + arbol.getOrden() + " hijos).");
            while (true) {
                mostrarMenu();
                String opcion = leer("Seleccione una opcion: ").trim();
                // Main coordina la interfaz; la logica de cada requisito vive en ArbolBMas
                // y en sus clases especializadas.
                if (opcion.equals("1")) {
                    arbol.insertar(pedirNombre("Nombre a insertar: "));
                } else if (opcion.equals("2")) {
                    arbol.buscar(pedirNombre("Nombre a buscar: "));
                } else if (opcion.equals("3")) {
                    arbol.eliminar(pedirNombre("Nombre a eliminar: "));
                } else if (opcion.equals("4")) {
                    System.out.println("\n" + "*".repeat(70));
                    System.out.println("        Impresion del ARBOL B+");
                    System.out.println("*".repeat(70));
                    arbol.imprimir();
                    System.out.println("*".repeat(70));
                } else if (opcion.equals("5")) {
                    System.out.println("\n" + "*".repeat(70));
                    System.out.println("     Verificacion de las propiedades del ARBOL B+");
                    System.out.println("*".repeat(70));
                    System.out.println("Esta funcion verificara las siguientes propiedades:");
                    System.out.println("  - Todas las hojas estan a la misma profundidad");
                    System.out.println("  - Los enlaces entre hojas son correctos");
                    System.out.println("  - El numero total de entradas coincide con el conteo");
                    System.out.println("  - Las claves estan correctamente ordenadas en cada nodo");
                    System.out.println("  - Las reglas de ocupacion minima y maxima se cumplen");
                    System.out.println("  - Las claves separadoras son correctas");
                    System.out.println("*".repeat(70));
                    String[] errores = arbol.verificar();
                    if (errores.length > 0) {
                        System.out.println("\nVerificacion: ERROR");
                        System.out.println("Se encontraron los siguientes problemas:");
                        for (int k = 0; k < errores.length; k++) {
                            System.out.println("   - " + errores[k]);
                        }
                        System.out.println("\nPosibles causas de errores:");
                        System.out.println("  * Problemas en las operaciones de insercion/eliminacion");
                        System.out.println("  * Errores en el manejo de divisiones (split) de nodos");
                        System.out.println("  * Problemas con fusiones (merge) de nodos");
                        System.out.println("  * Error en la actualizacion de los enlaces entre hojas");
                    } else {
                        System.out.println("\nVerificacion de propiedades B+: Correcta");
                        System.out.println("Todas las propiedades del arbol B+ estan correctamente implementadas.");
                    }
                    System.out.println("*".repeat(70));
                } else if (opcion.equals("0")) {
                    System.out.println("Programa finalizado.");
                    break;
                } else {
                    System.out.println("Opcion no valida. Elija una opcion del menu (0 a 5).");
                }
            }
        } catch (FinEntrada e) {
            System.out.println("\nPrograma finalizado.");
        }
        System.out.flush();
    }

    private static String repetir(char c, int n) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < n; i++) sb.append(c);
        return sb.toString();
    }
}
