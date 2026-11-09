BACKEND_SRC  := $(shell find backend/src/main -name "*.java")
FRONTEND_SRC := $(shell find frontend/src/main -name "*.java")
TEST_SRC     := $(shell find backend/src/test -name "*.java")
# Clases de test: ficheros *Test.java que contienen algún @Test.
# El nombre se saca de su línea "package" (p. ej. test.model.CandidatoTest).
TEST_CLASSES := $(shell for f in $$(grep -rl "@Test" backend/src/test --include="*Test.java"); do \
                  pkg=$$(sed -n 's/^package \([^;]*\);.*/\1/p' $$f); \
                  echo $${pkg:+$$pkg.}$$(basename $$f .java); done)
TEST_CP      := out/backend:out/test:backend/lib/*

.PHONY: compile run clean compile-test test

compile:
	mkdir -p out/backend out/frontend
	javac -encoding UTF-8 -d out/backend $(BACKEND_SRC)
	javac -encoding UTF-8 -cp out/backend -d out/frontend $(FRONTEND_SRC)

run: compile
	java -cp out/backend:out/frontend presentacion.Main

compile-test: compile
	mkdir -p out/test
	javac -encoding UTF-8 -cp "$(TEST_CP)" -d out/test $(TEST_SRC)

# make test                          -> ejecuta todos los tests
# make test T=test.model.CandidatoTest -> ejecuta solo esa clase (o varias separadas por espacios)
test: compile-test
	java -cp "$(TEST_CP)" org.junit.runner.JUnitCore $(if $(T),$(T),$(TEST_CLASSES))

clean:
	rm -rf out
