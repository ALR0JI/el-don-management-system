package main.ctrl;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class CtrlFichero {

    public List<String> leerLineas(String ruta) {
        try {
            return Files.readAllLines(Path.of(ruta), StandardCharsets.UTF_8);
            // ALEJANDRO:
            // readAllLies lee el fichero y lo separa por los saltos de liea
            // Path.of convierte el string ruta en un objeto "Path"
            // StandardCharsets define la codificacion como UTF8

        }
        catch (IOException e) { // Las excepciones IO son checked y tienen que ser tratadas
            throw new UncheckedIOException("No se puede leer el fichero " + ruta, e);
        }
    }
}


