BACKEND_SRC  := $(shell find backend/src/main -name "*.java")
FRONTEND_SRC := $(shell find frontend/src/main -name "*.java")
TEST_SRC     := $(shell find backend/src/test -name "*.java")

TEST_CLASSES := $(shell for f in $$(grep -rl "@Test" backend/src/test --include="*Test.java"); do \
                  pkg=$$(sed -n 's/^package \([^;]*\);.*/\1/p' $$f); \
                  echo $${pkg:+$$pkg.}$$(basename $$f .java); done)
TEST_CP      := out/backend:out/test:backend/lib/*
DRIVER_SRC   := $(shell find backend/src/drivers -name "*.java")
DRIVER_CP    := out/backend:out/drivers

.PHONY: compile run clean compile-test test compile-drivers driver

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

compile-drivers: compile
	mkdir -p out/drivers
	javac -encoding UTF-8 -cp out/backend -d out/drivers $(DRIVER_SRC)

# make driver D=DriverSecuacesUC
# make driver D=DriverSecuacesUC IN=ruta/entrada.txt
driver: compile-drivers
	java -cp "$(DRIVER_CP)" drivers.$(D) $(if $(IN),< $(IN))

clean:
	rm -rf out
