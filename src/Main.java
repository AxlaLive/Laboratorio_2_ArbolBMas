import java.util.Scanner;

public class Main {
    private static final int MAX_CARACTERES_NOMBRE = 30;
    private static final Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        System.out.println("**********************************************");
        System.out.println("       Laboratorio 2 - ARBOLES B+       ");
        System.out.println("**********************************************");

        int orden = pedirOrden();
        ArbolBMas arbol = new ArbolBMas(orden);
        System.out.println("\nArbol B+ creado con exito. Orden (m) = " + orden 
                + " | Max Claves = " + arbol.getMaxClaves() 
                + " | Min Hoja = " + arbol.getMinHoja() 
                + " | Min Interno = " + arbol.getMinInterno());

        boolean salir = false;
        while (!salir) {
            mostrarMenu();
            String entradaMenu = leer("Elige una opcion: ");
            int opcion;
            try {
                opcion = Integer.parseInt(entradaMenu.trim());
            } catch (NumberFormatException e) {
                opcion = -1;
            }

            switch (opcion) {
                case 1: {
                    String nombre = pedirNombre("Ingrese el nombre a insertar: ");
                    arbol.insertar(nombre);
                    break;
                }
                case 2: {
                    String nombre = pedirNombre("Ingrese el nombre a buscar: ");
                    boolean encontrado = arbol.buscar(nombre);
                    if (encontrado) {
                        System.out.println("\nResultado: El nombre '" + nombre + "' SI se encuentra en el arbol.");
                    } else {
                        System.out.println("\nResultado: El nombre '" + nombre + "' NO se encuentra en el arbol.");
                    }
                    break;
                }
                case 3: {
                    String nombre = pedirNombre("Ingrese el nombre a eliminar: ");
                    arbol.eliminar(nombre);
                    break;
                }
                case 4: {
                    arbol.imprimir();
                    break;
                }
                case 5: {
                    System.out.println("\nVerificacion de las propiedades B+");
                    System.out.println("Verificando limites de ocupacion (maxClaves y minimos)");
                    System.out.println("Verificando balance de altura (todas las hojas al mismo nivel)");
                    System.out.println("Verificando enlaces secuenciales horizontales entre hojas");
                    System.out.println("Verificando consistencia de punteros en nodos internos y raiz");
                    
                    String[] errores = arbol.verificar();
                    if (errores.length == 0) {
                        System.out.println("Estado: Correcto, el arbol cumple al 100% con todas las propiedades formales de los Arboles B+");
                    } else {
                        System.out.println("Estado: Error detectado, se encontraron fallas estructurales:");
                        for (String err : errores) {
                            System.out.println("   [X] " + err);
                        }
                    }
                    break;
                }
                case 6:
                    salir = true;
                    System.out.println("\nSaliendo del programa. Hasta luego");
                    break;
                default:
                    System.out.println("\n[Error] Opcion invalida. Por favor, ingrese un numero del 1 al 6.");
            }
        }
        scanner.close();
    }

    private static int pedirOrden() {
        while (true) {
            String entrada = leer("Ingrese el orden del arbol B+ (entre 4 y 9): ");
            try {
                int o = Integer.parseInt(entrada.trim());
                if (o >= 4 && o <= 9) {
                    return o;
                }
                System.out.println("  El orden debe estar entre 4 y 9.");
            } catch (NumberFormatException e) {
                System.out.println("  Por favor, ingrese un numero entero valido.");
            }
        }
    }

    private static void mostrarMenu() {
        System.out.println("\n***************** Menu *****************");
        System.out.println("1. Insertar nombre");
        System.out.println("2. Buscar nombre");
        System.out.println("3. Eliminar nombre");
        System.out.println("4. Imprimir arbol");
        System.out.println("5. Verificar propiedades del arbol");
        System.out.println("6. Salir");
        System.out.println("*****************************************");
    }

    private static String leer(String mensaje) {
        System.out.print(mensaje);
        return scanner.nextLine();
    }

    /** Retorna true si encuentra cualquier caracter que no sea una letra pura (espacios, numeros, simbolos). */
    private static boolean tieneCaracterInvalido(String texto) {
        for (int i = 0; i < texto.length(); i++) {
            char c = texto.charAt(i);
            if (!Character.isLetter(c)) {
                return true;
            }
        }
        return false;
    }

    private static String pedirNombre(String mensaje) {
        while (true) {
            String entradaOriginal = leer(mensaje);
            
            if (entradaOriginal.isEmpty()) {
                System.out.println("  El nombre no puede estar vacio.");
            } else if (tieneCaracterInvalido(entradaOriginal)) {
                System.out.println("  Nombre no valido: solo se permiten letras continuas "
                        + "(sin espacios, numeros, tildes escritas o simbolos).");
            } else {
                String nombreNormalizado = ArbolBMas.normalizarNombre(entradaOriginal);
                if (nombreNormalizado.length() > MAX_CARACTERES_NOMBRE) {
                    System.out.println("  El nombre es demasiado largo.");
                } else {
                    return nombreNormalizado;
                }
            }
        }
    }
}