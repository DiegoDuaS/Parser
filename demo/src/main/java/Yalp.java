import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.List;


import models.ParsingTable;
import modules.parser.Parser;

public class Yalp {
    public static void main(String[] args) throws IOException {
    
        ParsingTable parseTable;
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream("demo/src/main/resources/PARSE_TABLE.dat"))) {
            parseTable = (ParsingTable) in.readObject();
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Error al cargar la Parsing Table: " + e.getMessage());
            return;
        }

        List<List<String>> lineasParaParsear ;
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream("demo/src/main/resources/TOKENS_LIST.dat"))) {
            lineasParaParsear = (List<List<String>>) in.readObject();
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Error al cargar la Parsing Table: " + e.getMessage());
            return;
        }
        
        Parser parser = new Parser(parseTable);

        List<Boolean> results = parser.parseFile(lineasParaParsear);
        parser.printParser(results);
 
    }
}
