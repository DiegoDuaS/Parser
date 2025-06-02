import java.io.FileWriter;
import java.io.IOException;

public class JavaFileGenerator {
    public static void generateYalpFile() {
        String className = "Yalp"; // Nombre de la clase generada
        String javaOutputPath = "demo/src/main/java/" + className + ".java"; // Archivo .java
        String batOutputPath = "run.bat"; // Archivo .bat

        // Código fuente en forma de String
        StringBuilder javaCode = new StringBuilder();
        javaCode.append("import java.io.FileInputStream;\n")
            	.append("import java.io.IOException;\n")
                .append("import java.io.ObjectInputStream;\n")
                .append("import java.util.List;\n\n")
                .append("import models.ParsingTable;\n")
                .append("import modules.parser.Parser;\n\n")
                .append("\npublic class ").append(className).append(" {\n")
                .append("    public static void main(String[] args) throws IOException {\n")
                .append("        models.ParsingTable parseTable;\n")
                .append("        try (java.io.ObjectInputStream in = new java.io.ObjectInputStream(new java.io.FileInputStream(\"demo/src/main/resources/PARSE_TABLE.dat\"))) {\n")
                .append("            parseTable = (models.ParsingTable) in.readObject();\n")
                .append("        } catch (IOException | ClassNotFoundException e) {\n")
                .append("            System.err.println(\"Error al cargar la Parsing Table: \" + e.getMessage());\n")
                .append("            return;\n")
                .append("        }\n")
                .append("        java.util.List<java.util.List<String>> lineasParaParsear;\n")
                .append("        try (java.io.ObjectInputStream in = new java.io.ObjectInputStream(new java.io.FileInputStream(\"demo/src/main/resources/TOKENS_LIST.dat\"))) {\n")
                .append("            lineasParaParsear = (java.util.List<java.util.List<String>>) in.readObject();\n")
                .append("        } catch (IOException | ClassNotFoundException e) {\n")
                .append("            System.err.println(\"Error al cargar los tokens: \" + e.getMessage());\n")
                .append("            return;\n")
                .append("        }\n")
                .append("        modules.parser.Parser parser = new modules.parser.Parser(parseTable);\n")
                .append("        java.util.List<Boolean> results = parser.parseFile(lineasParaParsear);\n")
                .append("        parser.printParser(results);\n")
                .append("    }\n")
                .append("}");

        // Escribir el archivo .java
        writeFile(javaOutputPath, javaCode.toString());

        String batCode = "@echo off\n"
        + "set CLASSPATH=bin\n"
        + "javac -d bin -cp demo/src/main/java demo/src/main/java/Yalp.java\n"
        + "java -cp bin Yalp\n";

        writeFile(batOutputPath, batCode);
    }

    private static void writeFile(String filePath, String content) {
        try (FileWriter fileWriter = new FileWriter(filePath)) {
            fileWriter.write(content);
            System.out.println("Archivo generado exitosamente: " + filePath);
        } catch (IOException e) {
            System.err.println("Error al escribir el archivo: " + e.getMessage());
        }
    }
}

