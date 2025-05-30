package modules.input;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;

public class yalpInterpreter {
    private List<String> savedTokens;
    private List<String> ignoredTokens;
    private Map<String, List<String>> savedProductions;
    private boolean activeComment;
    private boolean readingTokens;

    public yalpInterpreter() {
        savedTokens = new ArrayList<>();
        ignoredTokens = new ArrayList<>();
        savedProductions = new LinkedHashMap<>();

        activeComment = false;
        readingTokens = true;
    }

    public List<String> getSavedTokens() {
        return savedTokens;
    }

    public List<String> getIgnoredTokens() {
        return ignoredTokens;
    }

    public Map<String, List<String>> getSavedProductions() {
        return savedProductions;
    }

    public void readFile(String filename) throws IOException {
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream(filename);
        try {
            if (inputStream == null) {
                throw new FileNotFoundException("No se encontró el archivo: " + filename);
            }

            try (BufferedReader br = new BufferedReader(new InputStreamReader(inputStream))) {
                parseFile(br);
            }

        } catch (FileNotFoundException e) {
            System.err.println("Error al leer el archivo: " + e.getMessage());
        }
    }

    private void parseFile(BufferedReader br) {
        String line;
        int line_num = 0;

        try {
            while ((line = br.readLine()) != null) {
                line_num++;
                if (line.isEmpty())
                    continue; // Ignorar líneas vacías

                // Manejo de comentarios
                line = checkforComments(line);
                if (line.isEmpty()) {
                    continue;
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
                        System.err.println("Error en la linea: " + line_num);
                        System.err.println("Linea inválida, se encuentra fuera de la sección de tokens");
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
                }

                // Lectura de producciones
                boolean reading_a_production = false;
                String actualProductionHead = "";
                List<String> actualProductions = new ArrayList<>();

                if (line.contains(":")) {
                    actualProductionHead = line.replace(":", "");
                    reading_a_production = true;
                }

                while (reading_a_production && (line = br.readLine()) != null) {
                    line_num++;
                    line = line.trim();

                    // Manejo de comentarios
                    line = checkforComments(line);
                    if (line.isEmpty()) {
                        continue;
                    }

                    // Si la línea contiene ';', es el fin de la producción
                    boolean endsWithSemicolon = line.endsWith(";");

                    // Si contiene '|', dividir por '|'
                    if (line.contains("|")) {
                        String[] parts = line.split("\\|");
                        for (String part : parts) {
                            part = part.trim();
                            if (!part.isEmpty()) {
                                // Eliminar ';' si está al final del fragmento
                                if (part.endsWith(";")) {
                                    part = part.substring(0, part.length() - 1).trim();
                                    if (!part.isEmpty()) {
                                        if (part.equals("NULL") || part.isEmpty()) {
                                            actualProductions.add(null);
                                        } else {
                                            actualProductions.add(part);
                                        }
                                    }
                                    continue; // salimos del for
                                } else {
                                    if (part.equals("NULL") || part.isEmpty()) {
                                        actualProductions.add(null);
                                    } else {
                                        actualProductions.add(part);
                                    }
                                }
                            }
                        }
                        if (endsWithSemicolon) {
                            reading_a_production = false;
                            savedProductions.put(actualProductionHead, actualProductions);
                        }
                    } else {
                        // Línea sin '|'
                        if (endsWithSemicolon) {
                            String production = line.substring(0, line.length() - 1).trim();
                            if (!production.isEmpty()) {
                                if (production.equals("NULL") || production.isEmpty()) {
                                    actualProductions.add(null);
                                } else {
                                    actualProductions.add(production);
                                }
                            }
                            reading_a_production = false;
                            savedProductions.put(actualProductionHead, actualProductions);
                        } else {
                            if (line.equals("NULL") || line.isEmpty()) {
                                actualProductions.add(null);
                            }
                            actualProductions.add(line);
                        }
                    }
                }

            }
        } catch (IOException e) {
            System.err.println("Error al leer el archivo en la linea: " + line_num);
            System.err.println(e.getMessage());
        }
    }

    private String checkforComments(String line) {
        // Si ya estamos dentro de un comentario
        if (activeComment) {
            if (line.contains("*/")) {
                activeComment = false;
                line = line.substring(line.indexOf("*/") + 2).strip();
                return line;
            } else {
                return line; // Seguimos ignorando
            }
        }

        // Comentario de apertura en la misma línea
        if (line.contains("/*")) {
            int start = line.indexOf("/*");
            int end = line.indexOf("*/", start + 2);

            if (end != -1) {
                // El comentario termina en la misma línea
                String before = line.substring(0, start);
                String after = line.substring(end + 2);
                line = (before + after).strip();

                return line;
            } else {
                // El comentario sigue en varias líneas
                line = line.substring(0, start).strip();
                activeComment = true;
                return line;
            }
        }

        return line;
    }

}