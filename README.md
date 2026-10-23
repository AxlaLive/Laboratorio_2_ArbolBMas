# Laboratorio 2 - Árboles B+

## Descripción
Implementación de un árbol B+ en memoria principal con claves de tipo nombre (cadenas de texto).

## Participantes
Alexandra Garavito Vargas - Maria Alejandra Ramirez

## Estructura del Proyecto
```
Lab_2_AlexandraGaravito_AlejandraRamirez/
├── src/
│   ├── Main.java
│   ├── ArbolBMas.java
│   ├── BusquedaArbolBMas.java
│   ├── InsercionArbolBMas.java
│   ├── EliminacionArbolBMas.java
│   ├── ImpresorArbolBMas.java
│   ├── VerificadorArbolBMas.java
│   └── Nodo.java
├── bin/
└── README.md
```

## Características
- Las claves son nombres (cadenas de texto)
- Implementado solo con arreglos (sin colecciones de Java)
- Manejo adecuado de mayúsculas y minúsculas
- Soporte para acentos
- Equilibrio del árbol mediante split y merge de nodos
- Hojas enlazadas entre sí

## Implementación
### Ubicación de los requisitos

- **Configuración y estado del árbol:** `ArbolBMas.java` conserva el orden, los
  límites de ocupación y la raíz, inicia el árbol vacío y ofrece los métodos
  públicos que usa el menú.
- **Búsqueda:** `BusquedaArbolBMas.java` desciende hasta la hoja correspondiente,
  revisa la clave e informa si se encontró.
- **Inserción:** `InsercionArbolBMas.java` evita duplicados y resuelve los
  desbordamientos mediante divisiones (split) de hojas e internos.
- **Eliminación:** `EliminacionArbolBMas.java` resuelve underflow mediante
  préstamos o fusiones y contempla el cambio de raíz.
- **Trazabilidad:** las clases de búsqueda, inserción y eliminación muestran en
  consola el recorrido y las decisiones tomadas durante cada operación.
- **Impresión visual:** `ImpresorArbolBMas.java` dibuja los niveles y nodos, y
  muestra la cadena de hojas enlazadas.
- **Propiedades formales:** `VerificadorArbolBMas.java` comprueba el orden de
  claves, la ocupación, la altura, los separadores y los enlaces de hojas.
- **Interfaz y validación de entrada:** `Main.java` solicita un orden entre 4 y 9
  y permite operar el árbol desde el menú.
- **Estructura de nodos:** `Nodo.java` representa las hojas e internos usando
  arreglos en memoria principal.

### Comparaciones de cadenas
El árbol utiliza una implementación específica de comparación que:
1. Normaliza las cadenas a minúsculas
2. Elimina acentos para ordenamiento consistente
3. Utiliza comparación lexicográfica para definir el orden

### Uso con compareTo
El código sigue una lógica similar a tu ejemplo:
```java
// EJEMPLO DE COMPARACIÓN
int result = "Ana".compareTo("Alexa"); // > 0

// EN EL ÁRBOL B+:
String nombre1 = "Ana";
String nombre2 = "Luis";
int resultado = comparar(nombre1, nombre2); // método implementado
if (resultado < 0) {
    // "Ana" es menor que "Luis" (en orden lexicográfico)
    System.out.println("Ana es menor que Luis");
} else if (resultado > 0) {
    // "Ana" es mayor que "Luis"
    System.out.println("Ana es mayor que Luis");
} else {
    // Son iguales
    System.out.println("Son iguales");
}
```

## Compilación y Ejecución
```bash
# Compilar
javac -d bin src/*.java

# Ejecutar
java -cp bin Main
```

## Funcionalidades Implementadas
1. **Inserción**: Agregar nombres al árbol B+
2. **Búsqueda**: Encontrar nombres en el árbol
3. **Eliminación**: Remover nombres del árbol
4. **Impresión**: Visualizar la estructura del árbol
5. **Verificación**: Validar las propiedades del árbol B+