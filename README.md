
# PROP — Subgrupo 12.2

Proyecto desarrollado para la asignatura PROP de la FIB - UPC.


## Estructura del proyecto

El proyecto se divide en un `backend` (lógica de dominio y datos en memoria) y un `frontend` (interfaz). El frontend depende del backend.

```
backend/
  lib/
  src/
    main/
      model/            entidades, enumerados y clases de solo datos
      uc/               controladores de caso de uso (XxxUC) y algoritmos
      ctrl/             controladores de datos (CtrlX) y FactoriaCtrl
    drivers/            DriverPrincipal, un driver por caso de uso e inout
    test/               tests, en el mismo paquete que la clase que prueban
      model/ 
      uc/ 
      integracion/  
frontend/
  src/
    main/
      presentacion/     Main, CtrlPresentacion y las vistas Swing
ejemplos/               ficheros de entrada con el formato del enunciado
```

## Cómo ejecutarlo

```
make compile   # compila backend en out/backend y frontend en out/frontend
make run       # compila y lanza presentacion.Main
make clean     # borra out/
```

## Drivers

Los drivers son la capa de presentación de la primera entrega y permiten probar el dominio desde la terminal.

`DriverPrincipal` es el punto de entrada. Muestra un menú con un driver por caso de uso (Datos, Tiempo, Secuaces, Misiones, Planificación, Ejecución y Seguimiento). Al elegir uno se muestra su menú, se ejecuta la opción escogida y se vuelve al menú principal.

Todos los drivers comparten los mismos datos a través de `FactoriaCtrl`, así que lo que se carga o se ejecuta en uno se ve en los demás. También comparten una única instancia de `inout`, que está en `DriverPrincipal`, para que no se pierdan líneas de la entrada.

En `DriverDatosUC`, si se deja la ruta vacía se usan los ficheros `ejemplos/misiones_driver.txt` y `ejemplos/secuaces_enunciado.txt` respectivamente.

```
make driver                     # lanza DriverPrincipal de forma interactiva
make driver IN=ruta/entrada.txt # lee las opciones desde un fichero
```

Un recorrido típico para probar las dos funcionalidades principales sería cargar secuaces y misiones en Datos, planificar en Planificación, proponer y confirmar la asignación automática en Ejecución, avanzar horas en Tiempo y consultar los resultados en Seguimiento.
