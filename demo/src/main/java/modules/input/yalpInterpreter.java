package modules.input;

import com.example.models.Token;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;

public class yalpInterpreter {
    private List<String> savedTokens;
    private List<String> ignoredTokens;
    private List<Token> fromLex_Tokens;

    private boolean activeComment;
    private boolean readingTokens;
    private boolean readingProductions;

    public yalpInterpreter() {
        savedTokens = new ArrayList<>();
        ignoredTokens = new ArrayList<>();
        fromLex_Tokens = new ArrayList<>();

        activeComment = false;
        readingTokens = true;
        readingProductions = false;
    }

    public void readFile(String filename) throws IOException {
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream(filename);
        try {
            if (inputStream == null) {
                throw new FileNotFoundException("No se encontró el archivo: " + filename);
            }

            try (BufferedReader br = new BufferedReader(new InputStreamReader(inputStream))) {
                String line;
                while ((line = br.readLine()) != null) {
                    parseLine(line.trim()); // Procesar cada línea
                }
            }

        } catch (FileNotFoundException e) {
            System.err.println("Error al leer el archivo: " + e.getMessage());
        }
    }

    private void parseLine(String line) {
        if (line.isEmpty())
            return; // Ignorar líneas vacías

        // Manejo de comentarios
        if (line.startsWith("/*")) {
            if (line.endsWith("*/")) {
                return;
            }
            activeComment = true; // Cambiamos al modo de lectura de comentarios
            return;
        }

        if (activeComment) {
            if (line.endsWith("*/")) {
                activeComment = false; // Salimos del modo de lectura de comentarios
            }
            return;
        }

        // Lectura de tokenst
        if (line.startsWith("%token")) {
            if (!readingTokens) {
                System.out.println("Linea inválida, se encuentra fuera de la sección de tokens");
            }

            String[] line_split = line.split("\\s+");
            // Guardar tokens omitiendo la palabra clave
            for (int i = 1; i < line_split.length; i++) {
                savedTokens.add(line_split[i]);
            }
        }
        if (line.startsWith("IGNORE")) {
            if (!readingTokens) {
                System.out.println("Linea inválida, se encuentra fuera de la sección de tokens");
            }

            String[] line_split = line.split("\\s+");
            // Guardar tokens omitiendo la palabra clave
            for (int i = 1; i < line_split.length; i++) {
                ignoredTokens.add(line_split[i]);
            }
        }

        // Separación de secciones
        if (line.equals("%%")) {
            readingTokens = false;
            readingProductions = true;
        }

        // Lectura de producciones
    }
}
