import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.List;

import models.ParsingTable;
import modules.parser.Parser;


public class Yalp {
    public static void main(String[] args) throws IOException {
        models.ParsingTable parseTable;
        try (java.io.ObjectInputStream in = new java.io.ObjectInputStream(new java.io.FileInputStream("demo/src/main/resources/PARSE_TABLE.dat"))) {
            parseTable = (models.ParsingTable) in.readObject();
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Error al cargar la Parsing Table: " + e.getMessage());
            return;
        }
        java.util.List<java.util.List<String>> lineasParaParsear;
        try (java.io.ObjectInputStream in = new java.io.ObjectInputStream(new java.io.FileInputStream("demo/src/main/resources/TOKENS_LIST.dat"))) {
            lineasParaParsear = (java.util.List<java.util.List<String>>) in.readObject();
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Error al cargar los tokens: " + e.getMessage());
            return;
        }
        modules.parser.Parser parser = new modules.parser.Parser(parseTable);
        java.util.List<Boolean> results = parser.parseFile(lineasParaParsear);
        parser.printParser(results);
    }
}