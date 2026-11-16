BACKEND_SRC  := $(shell find backend/src/main -name "*.java")
FRONTEND_SRC := $(shell find frontend/src/main -name "*.java")
TEST_SRC     := $(shell find backend/src/test -name "*.java")

TEST_CLASSES := $(shell for f in $$(grep -rl "@Test" backend/src/test --include="*Test.java"); do \
                  pkg=$$(sed -n 's/^package \([^;]*\);.*/\1/p' $$f); \
                  echo $${pkg:+$$pkg.}$$(basename $$f .java); done)
TEST_CP      := out/backend:out/test:backend/lib/*
DRIVER_SRC   := $(shell find backend/src/drivers -name "*.java")
DRIVER_CP    := ../EXE/out

.PHONY: compile run clean compile-test test compile-drivers driver test-juegos

compile:
	mkdir -p out/backend out/frontend
	javac -encoding UTF-8 -d out/backend $(BACKEND_SRC)
	javac -encoding UTF-8 -cp out/backend -d out/frontend $(FRONTEND_SRC)

run: compile
	java -cp out/backend:out/frontend presentacion.Main

compile-test: compile
	mkdir -p out/test
	javac -encoding UTF-8 -cp "$(TEST_CP)" -d out/test $(TEST_SRC)


test: compile-test
	java -cp "$(TEST_CP)" org.junit.runner.JUnitCore $(if $(T),$(T),$(TEST_CLASSES))

compile-drivers:
	mkdir -p $(DRIVER_CP)
	javac -encoding UTF-8 -d $(DRIVER_CP) $(BACKEND_SRC) $(DRIVER_SRC)

# make driver
# make driver IN=ruta/entrada.txt (ruta relativa al directorio EXE)
D ?= DriverPrincipal
driver: compile-drivers
	cd ../EXE && java -cp out drivers.$(D) $(if $(IN),< $(IN))

# ejecuta cada juego de prueba de EXE y compara su salida con la esperada
test-juegos: compile-drivers
	@cd ../EXE && for e in 0*/entrada*.txt; do \
		s=$$(echo $$e | sed 's/entrada/salida/; s/\.txt$$/.out/'); \
		java -cp out drivers.DriverPrincipal < $$e | diff -q - $$s > /dev/null && echo "OK $$e" || echo "DIFERENTE $$e"; \
	done

clean:
	rm -rf out $(DRIVER_CP)
