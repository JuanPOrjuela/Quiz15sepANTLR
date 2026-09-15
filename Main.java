import org.antlr.v4.runtime.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Main {

    /** Marca invisible que algunos programas ponen al inicio del texto UTF-8. */
    static final String BOM = String.valueOf((char) 0xFEFF);

    /**
     * Guarda los errores del lexer y del parser en vez de imprimirlos sueltos.
     *
     * ANTLR construye un arbol aunque la entrada sea invalida (inventa tokens
     * para recuperarse), asi que la unica forma confiable de saber si la
     * expresion fue aceptada es contar si hubo errores.
     */
    static class ColectorErrores extends BaseErrorListener {
        final List<String> errores = new ArrayList<>();

        @Override
        public void syntaxError(Recognizer<?, ?> r, Object simbolo, int linea,
                                int columna, String msg, RecognitionException e) {
            errores.add("columna " + (columna + 1) + ": " + msg);
        }
    }

    public static void main(String[] args) {
        // java Main archivo1.txt archivo2.txt ...  -> analiza los archivos y termina
        // java Main                                -> modo consola
        if (args.length > 0) {
            for (String ruta : args) {
                analizarArchivo(ruta);
            }
        } else {
            modoConsola();
        }
    }

    static void modoConsola() {
        System.out.println("Escribe una expresion, la ruta de un archivo .txt, o 'salir'.");
        Scanner entrada = new Scanner(System.in, "UTF-8");

        while (true) {
            System.out.print("> ");
            if (!entrada.hasNextLine()) {
                break;  // fin de la entrada (Ctrl+Z o texto redirigido)
            }
            // PowerShell antepone un BOM al redirigir texto con |
            String linea = entrada.nextLine().replace(BOM, "").trim();

            if (linea.isEmpty()) {
                continue;
            }
            if (linea.equalsIgnoreCase("salir")) {
                break;
            }

            // Al arrastrar un archivo a la terminal de Windows se pega entre comillas
            String ruta = linea.replaceAll("^\"|\"$", "");
            if (ruta.toLowerCase().endsWith(".txt")) {
                analizarArchivo(ruta);
            } else {
                analizar(linea);
            }
        }
    }

    /** Analiza el archivo linea por linea: cada linea no vacia es una expresion. */
    static void analizarArchivo(String ruta) {
        List<String> lineas;
        try {
            lineas = Files.readAllLines(Paths.get(ruta), StandardCharsets.UTF_8);
        } catch (IOException e) {
            System.out.println("No se pudo leer '" + ruta + "': " + e);
            return;
        }

        System.out.println("== " + ruta + " ==");
        int total = 0;
        int aceptadas = 0;

        for (int i = 0; i < lineas.size(); i++) {
            // Algunos editores guardan un BOM al inicio que el lexer no reconoce
            String linea = lineas.get(i).replace(BOM, "").trim();
            if (linea.isEmpty()) {
                continue;
            }
            total++;
            System.out.print("Linea " + (i + 1) + ": ");
            if (analizar(linea)) {
                aceptadas++;
            }
        }

        System.out.println(aceptadas + " de " + total + " expresiones aceptadas.");
        System.out.println();
    }

    /** Analiza una sola expresion. Devuelve true si la gramatica la acepta. */
    static boolean analizar(String texto) {
        ColectorErrores colector = new ColectorErrores();

        ExprLexer lexer = new ExprLexer(CharStreams.fromString(texto));
        lexer.removeErrorListeners();
        lexer.addErrorListener(colector);

        ExprParser parser = new ExprParser(new CommonTokenStream(lexer));
        parser.removeErrorListeners();
        parser.addErrorListener(colector);

        ExprParser.InicioContext arbol = parser.inicio();

        if (colector.errores.isEmpty()) {
            System.out.println(texto + "  ->  ACEPTADA");
            System.out.println("    arbol: " + arbol.expr().toStringTree(parser));
            return true;
        }

        System.out.println(texto + "  ->  RECHAZADA");
        for (String error : colector.errores) {
            System.out.println("    " + error);
        }
        return false;
    }
}
