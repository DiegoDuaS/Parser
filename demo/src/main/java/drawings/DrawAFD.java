package drawings;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JFrame;
import javax.swing.JPanel;

import models.AFD;
import models.EstadoAFD;
import models.Grammar;
import models.GrammarExtended;
import models.GrammarExtended.ProductionWithPointer;
import static modules.automaton.automatom.crearEstadoInicial;
import static modules.automaton.automatom.generarAFD;
import static modules.automaton.extension.extenderGramatica;

public class DrawAFD extends JPanel {

    private AFD automaton;
    private Map<String, Point> statePositions;
    private static final int STATE_RADIUS = 30;
    private static final int ARROW_SIZE = 10;
    private int offsetX = 0;
    private int offsetY = 0;

    public DrawAFD(AFD automaton) {
        this.automaton = automaton;
        this.statePositions = new HashMap<>();
        calculateStatePositions();
    }

    private void calculateStatePositions() {
        int centerX = 400;
        int centerY = 300;

        List<String> states = new ArrayList<>(automaton.getEstados().keySet());
        String initial = automaton.getInitialState();

        // Estado inicial al centro
        statePositions.put(initial, new Point(centerX, centerY));
        states.remove(initial);

        int totalStates = states.size();

        if (totalStates == 0) return;

        int maxStatesPerLayer = 12;

        int layers = (int) Math.ceil((double) totalStates / maxStatesPerLayer);

        int minRadius = 150; // radio mínimo de la capa 1
        int maxRadius = 450; // radio máximo que quieres permitir

        // Escala el incremento según capas
        int radiusIncrement = (layers > 1) ? (maxRadius - minRadius) / (layers - 1) : 0;

        int stateIndex = 0;

        for (int layer = 1; layer <= layers; layer++) {
            int statesInLayer = Math.min(maxStatesPerLayer, totalStates - (layer - 1) * maxStatesPerLayer);
            double angleStep = 2 * Math.PI / statesInLayer;
            int radius = minRadius + (layer - 1) * radiusIncrement;

            for (int i = 0; i < statesInLayer; i++) {
                if (stateIndex >= totalStates) break;
                double angle = i * angleStep;
                int x = centerX + (int)(radius * Math.cos(angle));
                int y = centerY + (int)(radius * Math.sin(angle));
                statePositions.put(states.get(stateIndex), new Point(x, y));
                stateIndex++;
            }
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Trasladar todo el dibujo según offset calculado
        g2d.translate(offsetX, offsetY);

        drawTransitions(g2d);
        drawStates(g2d);
        drawInitialArrow(g2d);
    }

    private void drawStates(Graphics2D g2d) {
        List<String> acceptanceStates = automaton.getAcceptanceStates();
        String initialState = automaton.getInitialState();

        for (String state : automaton.getEstados().keySet()) {
            Point pos = statePositions.get(state);

            // Colores según tipo de estado
            if (state.equals(initialState)) {
                g2d.setColor(Color.GREEN);
            } else if (acceptanceStates.contains(state)) {
                g2d.setColor(Color.YELLOW);
            } else {
                g2d.setColor(Color.WHITE);
            }

            // Relleno del círculo
            g2d.fillOval(pos.x - STATE_RADIUS, pos.y - STATE_RADIUS, STATE_RADIUS * 2, STATE_RADIUS * 2);

            // Borde negro
            g2d.setColor(Color.BLACK);
            g2d.drawOval(pos.x - STATE_RADIUS, pos.y - STATE_RADIUS, STATE_RADIUS * 2, STATE_RADIUS * 2);

            // Doble círculo para estados de aceptación
            if (acceptanceStates.contains(state)) {
                g2d.drawOval(pos.x - STATE_RADIUS + 5, pos.y - STATE_RADIUS + 5,
                        2 * (STATE_RADIUS - 5), 2 * (STATE_RADIUS - 5));
            }

            // Texto centrado
            FontMetrics fm = g2d.getFontMetrics();
            int textWidth = fm.stringWidth(state);
            g2d.setColor(Color.BLACK);
            g2d.drawString(state, pos.x - textWidth / 2, pos.y + fm.getAscent() / 2);
        }
    }

    private void drawTransitions(Graphics2D g2d) {
        Map<String, Map<String, String>> transitions = automaton.getTransitionsTable();

        for (String fromState : transitions.keySet()) {
            Point fromPos = statePositions.get(fromState);
            Map<String, String> symbolToState = transitions.get(fromState);
            int direction = 1;

            for (Map.Entry<String, String> entry : symbolToState.entrySet()) {
                String symbol = entry.getKey();
                String toState = entry.getValue();
                Point toPos = statePositions.get(toState);

                if (fromPos == null || toPos == null) continue;

                if (fromState.equals(toState)) {
                    // Self-loop
                    drawSelfLoop(g2d, fromPos, symbol);
                } else {
                    drawArrowTransition(g2d, fromPos, toPos, entry.getKey(), direction);
                    direction = -direction;
                }
            }
        }
    }

    private void drawSelfLoop(Graphics2D g2d, Point pos, String symbol) {
        int x = pos.x;
        int y = pos.y - STATE_RADIUS;

        g2d.drawArc(x - 20, y - 20, 40, 40, 0, 360);
        g2d.drawString(symbol, x - 5, y - 25);
    }

        // Modifica drawArrowTransition() para usar curvas
    private void drawArrowTransition(Graphics2D g2d, Point from, Point to, String symbol, int direction) {
        // direction: +1 o -1, para alternar lado de la curva

        double dx = to.x - from.x;
        double dy = to.y - from.y;
        double dist = Math.sqrt(dx*dx + dy*dy);

        // Punto medio
        double midX = (from.x + to.x) / 2.0;
        double midY = (from.y + to.y) / 2.0;

        // Vector perpendicular unitario (para control)
        double perpX = -dy / dist;
        double perpY = dx / dist;

        // Ajustamos desplazamiento perpendicular proporcional a la distancia
        double offset = dist / 4;  // Puedes ajustar la división para curvas más o menos pronunciadas

        // Punto de control desplazado según direction
        double ctrlX = midX + direction * offset * perpX;
        double ctrlY = midY + direction * offset * perpY;

        // Curva cuadrática
        java.awt.geom.QuadCurve2D q = new java.awt.geom.QuadCurve2D.Double();
        q.setCurve(from.x, from.y, ctrlX, ctrlY, to.x, to.y);
        g2d.draw(q);

        // Texto: calcular posición desplazada perpendicularmente para que no se monte en la curva
        double textOffset = 10; // separación del texto respecto a la curva (ajusta si quieres)
        int baseTextX = (int) ((from.x + 2 * ctrlX + to.x) / 4);
        int baseTextY = (int) ((from.y + 2 * ctrlY + to.y) / 4);

        int textX = (int)(baseTextX + direction * textOffset * perpX);
        int textY = (int)(baseTextY + direction * textOffset * perpY);

        // Medir texto
        String text = symbol;
        FontMetrics fm = g2d.getFontMetrics();
        int padding = 4;
        int textWidth = fm.stringWidth(text);
        int textHeight = fm.getHeight();

        // Rectángulo de fondo para el texto (con bordes redondeados)
        int rectX = textX - padding;
        int rectY = textY - textHeight + fm.getDescent() - padding;
        int rectWidth = textWidth + 2 * padding;
        int rectHeight = textHeight + 2 * padding;

        // Guardar color actual
        Color oldColor = g2d.getColor();

        // Dibujar fondo blanco semitransparente con bordes redondeados
        g2d.setColor(new Color(255, 255, 255, 200));
        g2d.fillRoundRect(rectX, rectY, rectWidth, rectHeight, 8, 8);
        g2d.setColor(Color.BLACK);
        g2d.drawRoundRect(rectX, rectY, rectWidth, rectHeight, 8, 8);

        // Dibujar texto encima
        g2d.drawString(text, textX, textY);

        // Restaurar color original
        g2d.setColor(oldColor);

        // Ángulo para flecha al final de la curva
        double angle = Math.atan2(to.y - ctrlY, to.x - ctrlX);
        int endX = (int) (to.x - STATE_RADIUS * Math.cos(angle));
        int endY = (int) (to.y - STATE_RADIUS * Math.sin(angle));
        drawArrowHead(g2d, endX, endY, angle);
    }


    private void drawArrowHead(Graphics2D g2d, int x, int y, double angle) {
        int size = ARROW_SIZE;

        Polygon arrowHead = new Polygon();
        arrowHead.addPoint(0, 0);
        arrowHead.addPoint(-size, -size / 2);
        arrowHead.addPoint(-size, size / 2);

        AffineTransform tx = new AffineTransform();
        tx.setToTranslation(x, y);
        tx.rotate(angle);

        Shape arrow = tx.createTransformedShape(arrowHead);
        g2d.fill(arrow);
    }

    private void drawInitialArrow(Graphics2D g2d) {
        String initialState = automaton.getInitialState();
        Point pos = statePositions.get(initialState);

        if (pos == null) return;

        int startX = pos.x - STATE_RADIUS - 40;
        int startY = pos.y;

        int endX = pos.x - STATE_RADIUS;
        int endY = pos.y;

        g2d.drawLine(startX, startY, endX, endY);
        drawArrowHead(g2d, endX, endY, 0);
    }

    // Mostrar ventana
    public void display() {
        JFrame frame = new JFrame("AFD Visualization");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        // JScrollPane con barras de scroll siempre visibles
        javax.swing.JScrollPane scrollPane = new javax.swing.JScrollPane(this,
            javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
            javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);

        frame.add(scrollPane);
        
        // Tamaño fijo inicial
        frame.setSize(900, 700);

        // Centrar en pantalla
        frame.setLocationRelativeTo(null);

        frame.setVisible(true);
    }

    @Override
    public Dimension getPreferredSize() {
        if (statePositions.isEmpty()) {
            return new Dimension(800, 600); // tamaño por defecto si no hay estados
        }

        int minX = Integer.MAX_VALUE;
        int minY = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE;
        int maxY = Integer.MIN_VALUE;

        for (Point p : statePositions.values()) {
            if (p.x < minX) minX = p.x;
            if (p.y < minY) minY = p.y;
            if (p.x > maxX) maxX = p.x;
            if (p.y > maxY) maxY = p.y;
        }

        int margin = STATE_RADIUS * 4;

        // Calculamos offset para trasladar el dibujo y que el más pequeño quede en margin
        offsetX = margin - minX;
        offsetY = margin - minY;

        int width = (maxX - minX) + margin * 2;
        int height = (maxY - minY) + margin * 2;

        return new Dimension(width, height);
    }

    public void saveAsPNG(File file) {
        double zoom = 1;

        Dimension size = getPreferredSize();  // Tamaño total real del panel
        int width = (int) (size.width * zoom);
        int height = (int) (size.height * zoom);

        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = image.createGraphics();

        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);

        g2d.scale(zoom, zoom);

        g2d.setColor(Color.WHITE);
        g2d.fillRect(0, 0, width, height);

        this.paint(g2d);

        g2d.dispose();

        try {
            javax.imageio.ImageIO.write(image, "png", file);
            System.out.println("Guardado como PNG: " + file.getAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }






    // Para pruebas rápidas (debes tener una clase EstadoAFD para que compile)
    public static void main(String[] args) {

        Grammar g = new Grammar("S");
        g.agregarNoTerminal("S");
        g.agregarNoTerminal("P");
        g.agregarNoTerminal("Q");

        g.agregarTerminal("^");
        g.agregarTerminal("V");
        g.agregarTerminal("[");
        g.agregarTerminal("]");
        g.agregarTerminal("sentence");

        g.agregarProduccion("S", "S ^ P");
        g.agregarProduccion("S", "P");
        g.agregarProduccion("P", "P V Q");
        g.agregarProduccion("P", "Q");
        g.agregarProduccion("Q", "[ S ]");
        g.agregarProduccion("Q", "sentence");

        // 2. Extender la gramática
        GrammarExtended extendida = extenderGramatica(g);

        // 3. Mostrar producciones extendidas
        System.out.println("Producciones extendidas:");
        for (Map.Entry<String, List<ProductionWithPointer>> entry : extendida.getProductions().entrySet()) {
            for (ProductionWithPointer prod : entry.getValue()) {
                System.out.println(entry.getKey() + " -> " + prod);
            }
        }

        // 4. Crear el estado inicial del AFD
        EstadoAFD estado0 = crearEstadoInicial(extendida);
        System.out.println("\nEstado inicial:");
        System.out.println(estado0.getId());
        for (ProductionWithPointer item : estado0.getItems()) {
            System.out.println("  " + item);
        }

        // 5. Generar todos los estados del AFD
        AFD afd = generarAFD(extendida, estado0);

        DrawAFD panel = new DrawAFD(afd);
        panel.display();
        panel.saveAsPNG(new File("demo/src/main/java/drawings/mi_automata.png"));
    }
}
