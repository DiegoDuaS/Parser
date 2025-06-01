import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;

import models.ParsingTable;
import modules.parser.Parser;

public class Yalp {
    public static void main(String[] args) throws IOException {
        //if (args.length == 0) {
          //  System.out.println("Uso: java Yalp <archivo_de_tokens>");
            //return;
        //}
        //String inputFile = args[0];
        ParsingTable parseTable;
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream("demo/src/main/resources/PARSE_TABLE.dat"))) {
            parseTable = (ParsingTable) in.readObject();
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Error al cargar la Parsing Table: " + e.getMessage());
            return;
        }
        Parser parser = new Parser(parseTable);

        parseTable.printParsingTables("$");
        parseTable.printReduceDictionary();
        //parser.parse(inputFile); // Aquí asumo que tienes un método 'run' que lee los tokens del archivo
    }
}
