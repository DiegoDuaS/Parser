import java.io.IOException;
import java.util.List;

import com.example.Modules.Analisis.Complete_Lex;
import com.example.models.Token;

public class Main {
    public static void main(String[] args) {
        try {
            List<Token> tokensLex = Complete_Lex.completeLex(""); // AÑADIR PARÁMETRO DE RUTA AL ARCHIVO DE CÓDIGO
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
