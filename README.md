
# PROP — Subgrupo 12.2

Proyecto desarrollado para la asignatura PROP de la FIB - UPC.


## Estructura del proyecto

Sigue la estructura de directorios de la normativa: `DOCS` para la documentación, `FONTS` para el código y `EXE` para los ejecutables y los juegos de prueba. El código se divide en un `backend` (lógica de dominio y datos en memoria) y un `frontend` (interfaz), y el frontend depende del backend.

```
DOCS/                   documentación de la entrega
FONTS/
  Makefile
  backend/
    lib/
    src/
      main/
        model/          entidades, enumerados y clases de solo datos
        uc/             controladores de caso de uso (XxxUC) y algoritmos
        ctrl/           controladores de datos (CtrlX) y FactoriaCtrl
      drivers/          DriverPrincipal, un driver por caso de uso e inout
      test/             tests, con la misma estructura de paquetes que main
        model/ 
        uc/ 
  frontend/             Pendiente 2a entrega
    src/
      main/
        presentacion/   Main, CtrlPresentacion y las vistas Swing
EXE/
  out/                  drivers compilados (se genera con make compile-drivers)
  ejemplos/             ficheros de entrada con el formato del enunciado
  01_.../ a 08_.../     juegos de prueba de cada driver (entradas, datos y salidas)
```

## Cómo ejecutarlo

Todos los comandos se lanzan desde `FONTS`.

```
make compile   # compila backend en out/backend y frontend en out/frontend
make run       # compila y lanza presentacion.Main (pendiente 2a Entrega)
make clean     # borra out/
```

## Drivers

Los drivers son la capa de presentación de la primera entrega y permiten probar el dominio desde la terminal.

`DriverPrincipal` es el punto de entrada. Muestra un menú con un driver por caso de uso (Datos, Tiempo, Secuaces, Misiones, Planificación, Ejecución y Seguimiento). Al elegir uno se muestra su menú, se ejecuta la opción escogida y se vuelve al menú principal.

Todos los drivers comparten los mismos datos a través de `FactoriaCtrl`, así que lo que se carga o se ejecuta en uno se ve en los demás. También comparten una única instancia de `inout`, que está en `DriverPrincipal`, para que no se pierdan líneas de la entrada.

En `DriverDatosUC`, si se deja la ruta vacía se usan los ficheros `EXE/ejemplos/misiones_enunciado.txt` y `EXE/ejemplos/secuaces_enunciado.txt` respectivamente.

Los drivers se compilan en `EXE/out` y se ejecutan desde `EXE`, así que las rutas de los ficheros (las de `IN` y las que se escriben dentro de los drivers) son relativas a `EXE`.

```
make driver                     # lanza DriverPrincipal de forma interactiva
make driver IN=ruta/entrada.txt # lee las opciones desde un fichero

# por ejemplo:
make driver IN=01_DriverPlanificacionUC/entrada_ejemplo_enunciado.txt
```

Un recorrido típico para probar las dos funcionalidades principales sería cargar secuaces y misiones en Datos, planificar en Planificación, proponer y confirmar la asignación automática en Ejecución, avanzar horas en Tiempo y consultar los resultados en Seguimiento.

## Juegos de prueba

Están en `EXE`, con una carpeta por driver. Cada `entrada*.txt` es lo que se escribiría por teclado y el `salida*.out` con el mismo nombre es lo que tiene que mostrar el programa. Para comprobar un juego se ejecuta su entrada y se compara con su salida (desde `FONTS`). Si `diff` no muestra nada, la salida es la misma:

```
make -s driver IN=01_DriverPlanificacionUC/entrada_ejemplo_enunciado.txt | diff - ../EXE/01_DriverPlanificacionUC/salida_ejemplo_enunciado.out
```

Para comprobarlos todos de una vez, `make -s test-juegos` ejecuta cada entrada y escribe OK o DIFERENTE según coincida o no con su salida.

Nota: -s en make hace que make no escriba sus propios mensajes y así no ensucian la comparación
