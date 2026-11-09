
# PROP — Subgrupo 12.2

Proyecto desarrollado para la asignatura PROP de la FIB - UPC.

## Descripción

(Pendiente)

## Tecnologías

- Java
- Swing (frontend)

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
