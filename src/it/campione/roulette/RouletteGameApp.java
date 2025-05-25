package it.campione.roulette;

// Importiamo le librerie necessarie per JavaFX, animazioni, layout e componenti grafici
import java.util.ArrayList;
import java.util.List;

import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.ParallelTransition;
import javafx.animation.RotateTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.effect.InnerShadow;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import javafx.util.Duration;

/**
 * La classe principale dell'applicazione che simula strategie di gioco alla roulette.
 * Estende JavaFX Application per creare un'interfaccia grafica interattiva.
 * 
 * Castle method 01: {
 * You bet 1 EUR on 0.
 * You bet 5 EUR on the "1st 12" sector.
 * You bet 5 EUR on the "2nd 12" sector.
 * Only if possible, you bet 1 EUR on the "3rd 12" sector straddling the two pairs at the
 * bottom "25 and 28" and "31 and 34" (1 EUR and 1 EUR respectively). In
 * total, therefore, 11.00 EUR is staked without the horses.
 * }
 * 
 * Castle method 02: {
 * You bet 1 EUR on 0.
 * You bet 5 EUR on the "2nd 12" sector.
 * You bet 5 EUR on the "3rd 12" sector.
 * Only if possible, you bet 1 EUR on the "1st 12" sector straddling the two pairs at the
 * bottom "1 and 4" and "7 and 10" (1 EUR and 1 EUR respectively). In
 * total, therefore, 11.00 EUR is staked without the horses.
 * }
 * 
 * The "Opposite Color" method takes over in the event of a "loss" with the
 * "Castle" method 02, i.e. when the gain with the "Castle" method decreases the
 * total gain despite the victory. "Opposite Color" mode: {
 * You bet 8.00 EUR on the color opposite to that of the number that was drawn
 * just before (if a red number belonging to the first sector "1st 12" had just
 * been drawn, you would now bet on the opposite color, i.e. the color black).
 * Both in case of victory and defeat, the "Castle" method is applied again and
 * so on.
 * }
 * 
 * For more details see the video https://www.youtube.com/watch?v=VPmbUqGtrOY
 */
public class RouletteGameApp extends Application {
    // Istanza della roulette per generare numeri casuali
    Roulette roulette;

    // Componenti UI principali
    private WebView outputWebView; // Mostra risultati dei lanci in formato HTML
    private TextArea statsTextArea; // Visualizza statistiche finali (massimo guadagno, posizione, totale)
    private LineChart<Number, Number> profitChart; // Grafico dell'andamento del profitto
    private XYChart.Series<Number, Number> series; // Serie dati per il grafico

    // ComboBox per input utente
    private ComboBox<Integer> numberOfSpinsComboBox; // Numero di lanci per simulazione
    private ComboBox<Integer> sufficientCapitalComboBox; // Capitale minimo per vincita

    // Variabili per gestire le strategie Castello
    private ComboBox<String> strategyComboBox; // Casella di selezione strategie
    private static final String STRATEGY_CASTELLO_01 = "Castello Metodo 01";
    private static final String STRATEGY_CASTELLO_02 = "Castello Metodo 02";

    // Variabili per gestire la visibilità del grafico
    private VBox chartBox; // Riferimento al contenitore del grafico
    private CheckBox showChartCheckBox; // Riferimento alla casella di controllo

    // Costanti per scommesse specifiche
    private static final int[] THIRD_12 = { 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36 };
    private static final int[] FIRST_12 = { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12 };
    private static final int[] SECOND_12 = { 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24 };

    // Numeri rossi e neri nella roulette
    private static final int[] RED_NUMBERS = { 1, 3, 5, 7, 9, 12, 14, 16, 18, 19, 21, 23, 25, 27, 30, 32, 34, 36 };
    private static final int[] BLACK_NUMBERS = { 2, 4, 6, 8, 10, 11, 13, 15, 17, 20, 22, 24, 26, 28, 29, 31, 33, 35 };

    // Variabili di stato
    private int lastLossNumber = -1; // Ultimo numero uscito in caso di perdita
    private boolean isBackupStrategyActive = false; // Indica se è attiva la strategia di backup

    // Lista per memorizzare le simulazioni storiche
    private List<SimulationData> simulationHistory = new ArrayList<>();

    /**
     * Metodo principale di inizializzazione dell'applicazione JavaFX.
     * Configura l'interfaccia utente, i componenti grafici e gli eventi.
     */
    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Roulette Game - Castello Strategy");
        roulette = new Roulette();
        outputWebView = new WebView();
        statsTextArea = new TextArea();
        statsTextArea.setEditable(false);
        statsTextArea.setWrapText(true);
        applyStartupAnimations(statsTextArea);

        numberOfSpinsComboBox = new ComboBox<>();
        for (int i = 1; i <= 5500; i++) {
            numberOfSpinsComboBox.getItems().add(i);
        }
        numberOfSpinsComboBox.getSelectionModel().select(99); // Default value

        sufficientCapitalComboBox = new ComboBox<>();
        sufficientCapitalComboBox.getItems().addAll(0, 25, 50, 60, 75, 90, 100, 150, 200);
        sufficientCapitalComboBox.getSelectionModel().selectFirst(); // Default value

        Button startButton = new Button("Avvia Simulazione");
        startButton.getStyleClass().add("button");
        startButton.setOnAction(e -> startSimulation());
        applyButtonEffects(startButton);

        // Crea la ComboBox per selezionare la strategia Castello
        strategyComboBox = new ComboBox<>();
        strategyComboBox.getItems().addAll(STRATEGY_CASTELLO_01, STRATEGY_CASTELLO_02);
        strategyComboBox.getSelectionModel().selectFirst(); // Seleziona Metodo 01 di default
        strategyComboBox.setPromptText("Seleziona Strategia");

        VBox controlsBox = new VBox(10, new Label("Numero di lanci nella serie:"), numberOfSpinsComboBox,
                new Label("Capitale minimo di vittoria:"), sufficientCapitalComboBox, new Label("Strategia Castello:"),
                strategyComboBox, startButton);
        controlsBox.setPadding(new Insets(10));
        applyComboBoxAnimation(numberOfSpinsComboBox);
        applyComboBoxAnimation(sufficientCapitalComboBox);

        // Crea la casella di controllo per mostrare/nascondere il grafico
        showChartCheckBox = new CheckBox("Mostra Grafico");
        showChartCheckBox.setSelected(true);
        showChartCheckBox.setOnAction(e -> {
            boolean isVisible = showChartCheckBox.isSelected();
            chartBox.setVisible(isVisible);
            chartBox.setManaged(isVisible);
        });

        // Aggiungi la casella di controllo in prima posizione
        controlsBox.getChildren().add(0, showChartCheckBox); // Inserita in posizione 0

        // Chart Setup
        final NumberAxis xAxis = new NumberAxis();
        final NumberAxis yAxis = new NumberAxis();
        xAxis.setLabel("Lancio");
        yAxis.setLabel("Profitto/Perdita (€)");

        profitChart = new LineChart<>(xAxis, yAxis);
        profitChart.setTitle("Andamento Profitto Totale");
        series = new XYChart.Series<>();
        series.setName("Profitto/Perdita");

        // Creazione del contenitore del grafico (ora come variabile di classe)
        chartBox = new VBox(profitChart);
        chartBox.setPadding(new Insets(10));
        chartBox.setVisible(true); // Visibile all'avvio
        chartBox.setManaged(true); // Gestito dal layout all'avvio

        Button historyButton = new Button("Mostra Storico Grafici");
        historyButton.getStyleClass().add("button");
        historyButton.setOnAction(e -> showHistoryWindow());

        controlsBox.getChildren().add(historyButton);

        BorderPane root = new BorderPane();
        root.setCenter(outputWebView);
        root.setRight(controlsBox);
        root.setBottom(statsTextArea);
        root.setLeft(chartBox);

        Scene scene = new Scene(root, 1100, 600);
        scene.getStylesheets().add(getClass().getResource("styles.css").toExternalForm());
        primaryStage.setScene(scene);

        primaryStage.setOnCloseRequest(event -> {
            event.consume();
            applyExitAnimations(primaryStage);
        });
        primaryStage.show();
    }

    /**
     * Applica animazioni di ingresso al TextArea delle statistiche.
     * Combina dissolvenza, scala e rotazione per un effetto teatrale.
     */
    private void applyStartupAnimations(TextArea textArea) {
        // Dissolvenza da trasparente a opaco
        FadeTransition fadeTransition = new FadeTransition(Duration.millis(1000), textArea);
        fadeTransition.setFromValue(0.0);
        fadeTransition.setToValue(1.0);

        // Scala da 80% a 100% della dimensione originale
        ScaleTransition scaleTransition = new ScaleTransition(Duration.millis(1000), textArea);
        scaleTransition.setFromX(0.8);
        scaleTransition.setFromY(0.8);
        scaleTransition.setToX(1.0);
        scaleTransition.setToY(1.0);

        // Rotazione completa (360 gradi)
        RotateTransition rotateTransition = new RotateTransition(Duration.millis(1000), textArea);
        rotateTransition.setByAngle(360);

        // Esegue tutte le transizioni in parallelo
        ParallelTransition parallelTransition = new ParallelTransition(fadeTransition, scaleTransition,
                rotateTransition);
        parallelTransition.play();
    }

    /**
     * Applica animazioni di uscita alla finestra principale.
     * Combina dissolvenza, riduzione di scala e rotazione per un effetto drammatico.
     */
    private void applyExitAnimations(Stage primaryStage) {
        FadeTransition fadeTransition = new FadeTransition(Duration.millis(1000), primaryStage.getScene().getRoot());
        fadeTransition.setFromValue(1.0);
        fadeTransition.setToValue(0.0);

        ScaleTransition scaleTransition = new ScaleTransition(Duration.millis(1000), primaryStage.getScene().getRoot());
        scaleTransition.setFromX(1.0);
        scaleTransition.setFromY(1.0);
        scaleTransition.setToX(0.5);
        scaleTransition.setToY(0.5);

        RotateTransition rotateTransition = new RotateTransition(Duration.millis(1000),
                primaryStage.getScene().getRoot());
        rotateTransition.setByAngle(360);

        ParallelTransition parallelTransition = new ParallelTransition(fadeTransition, scaleTransition,
                rotateTransition);
        parallelTransition.setOnFinished(e -> primaryStage.close());
        parallelTransition.play();
    }

    /**
     * Avvia la simulazione quando l'utente clicca sul pulsante.
     * Calcola risultati, aggiorna l'interfaccia e gestisce le strategie.
     */
    private void startSimulation() {
        addNeonEffect(statsTextArea);
        outputWebView.getEngine().loadContent("");
        statsTextArea.clear();
        profitChart.getData().clear();
        series = new XYChart.Series<>();
        profitChart.getData().add(series);
        series.setName("Profitto/Perdita");

        int numberOfSpins = numberOfSpinsComboBox.getValue();
        int sufficientCapital = sufficientCapitalComboBox.getValue();
        double totalProfitLoss = 0;
        double maxProfit = Double.MIN_VALUE;
        StringBuilder output = new StringBuilder();
        StringBuilder stats = new StringBuilder();
        String maxProfitLine = "";
        int maxProfitIndex = -1;
        List<Double> profitList = new ArrayList<>();

        output.append(
                "<html><head><meta charset='UTF-8'></head><body style='font-family: Courier New; font-size: 12px;'>");

        String currentStrategy = "Castello";
        String lastColor = "";

        // Ottieni la strategia selezionata
        String selectedStrategy = strategyComboBox.getValue();

        for (int i = 0; i < numberOfSpins; i++) {
            int number = roulette.spin();
            double result = 0;
            String strategy = "";

            switch (currentStrategy) {
            case "Castello":
                // Usa la strategia selezionata
                if (STRATEGY_CASTELLO_01.equals(selectedStrategy)) {
                    result = calculateBetResultMethod1(number);
                } else {
                    result = calculateBetResultMethod2(number);
                }
                strategy = "(" + selectedStrategy + ")";
                if (result < 0) {
                    currentStrategy = "Colore opposto";
                    lastColor = getColor(number);
                }
                break;

            case "Colore opposto":
                String targetColor = lastColor.equals("Rosso") ? "Nero" : "Rosso";
                boolean isWin = getColor(number).equals(targetColor);
                result = isWin ? 8 : -8; // Vittoria o perdita fissa
                strategy = "(Colore opposto)";
                currentStrategy = result < 0 ? "Colore opposto" : "Castello"; // Torna a Castello in caso di vittoria
                break;
            }

            // Aggiorna i totali e i dati statistici
            totalProfitLoss += result;
            profitList.add(totalProfitLoss);
            series.getData().add(new XYChart.Data<>(i + 1, totalProfitLoss));

            if (totalProfitLoss > maxProfit) {
                maxProfit = totalProfitLoss;
                maxProfitIndex = i;
            }

            // Costruisce la riga di output HTML
            String color = getColor(number);
            String parity = getParity(number);
            String range = getRange(number);
            String situation = getSituation(result);
            String profitLoss = result >= 0 ? "Guadagno: " + result + "€" : "Perdita: " + Math.abs(result) + "€";
            String line = getSymbol(result) + " " + number + " | Colore: " + color + " | Parità: " + parity
                    + " | Range: " + range + " | Situazione: " + situation + " | " + profitLoss + " | Totale: "
                    + totalProfitLoss + "€ " + strategy + "<br>";

            if (totalProfitLoss == maxProfit) {
                maxProfitLine = line;
            }

            // Colora la riga in base al risultato
            if (totalProfitLoss < 0) {
                output.append("<span style='color:red;'>").append(line).append("</span>");
            } else if (sufficientCapital > 0 && totalProfitLoss >= sufficientCapital) {
                output.append("<span style='color:blue;'>").append(line).append("</span>");
            } else {
                output.append("<span style='color:black;'>").append(line).append("</span>");
            }
        }

        // Chiude il tag HTML
        output.append("</body></html>");

        // Costruisce le statistiche finali
        stats.append("Massimo guadagno raggiunto: ").append(maxProfit).append("€\n");
        stats.append("Posizione del massimo guadagno: ").append(maxProfitIndex + 1).append("\n");
        stats.append("Profitto/Perdita totale: ").append(totalProfitLoss).append("€\n");

        // Evidenzia la riga del massimo guadagno
        String highlightedLine = "<span style='background-color: #F0E68C; font-weight: bold; color: black;'>"
                + maxProfitLine + "</span>";
        String finalOutput = output.toString().replace(maxProfitLine, highlightedLine);

        // Aggiorna i componenti UI
        outputWebView.getEngine().loadContent(finalOutput);
        statsTextArea.setText(stats.toString());
        removeNeonEffect(statsTextArea); // Rimuove l'effetto neon

        // Salva la simulazione nello storico
        simulationHistory.add(
                new SimulationData(new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new java.util.Date()),
                        profitList, numberOfSpins, totalProfitLoss));
    }

    /**
     * Strategia Castello Metodo 01: 
     * Scommesse su 0, Q1 (FIRST_12) e Q2 (SECOND_12)
     */
    private double calculateBetResultMethod1(int number) {
        double totalWin = 0;
        if (number == 0) {
            totalWin += 1 * 35; // Vincita su 0 (35:1)
            totalWin -= 5; // Costo puntata su Q1
            totalWin -= 5; // Costo puntata su Q2
        } else if (contains(FIRST_12, number)) {
            totalWin += 5 * 2; // Vincita su Q1 (2:1)
            totalWin -= 1; // Costo puntata su 0
            totalWin -= 5; // Costo puntata su Q2
        } else if (contains(SECOND_12, number)) {
            totalWin += 5 * 2; // Vincita su Q2 (2:1)
            totalWin -= 1; // Costo puntata su 0
            totalWin -= 5; // Costo puntata su Q1
        } else if (contains(THIRD_12, number)) {
            totalWin -= 1; // Perdita su 0
            totalWin -= 5; // Perdita su Q1
            totalWin -= 5; // Perdita su Q2
        }
        return totalWin;
    }

    /**
     * Strategia Castello Metodo 02: 
     * Scommesse su 0, Q2 (SECOND_12) e Q3 (THIRD_12)
     */
    private double calculateBetResultMethod2(int number) {
        double totalWin = 0;
        if (number == 0) {
            totalWin += 1 * 35; // Vincita su 0 (35:1)
            totalWin -= 5; // Costo puntata su Q2
            totalWin -= 5; // Costo puntata su Q3
        } else if (contains(FIRST_12, number)) {
            totalWin -= 1; // Perdita su 0
            totalWin -= 5; // Perdita su Q2
            totalWin -= 5; // Perdita su Q3
        } else if (contains(SECOND_12, number)) {
            totalWin += 5 * 2; // Vincita su Q2 (2:1)
            totalWin -= 1; // Costo puntata su 0
            totalWin -= 5; // Costo puntata su Q3
        } else if (contains(THIRD_12, number)) {
            totalWin += 5 * 2; // Vincita su Q3 (2:1)
            totalWin -= 1; // Costo puntata su 0
            totalWin -= 5; // Costo puntata su Q2
        }
        return totalWin;
    }

    /**
     * Restituisce un simbolo "." per vincite e "X" per perdite.
     * Utile per visualizzare rapidamente i risultati nel WebView.
     */
    private String getSymbol(double result) {
        return result > 0 ? "." : "X";
    }

    /**
     * Determina il colore del numero uscito.
     * 
     * Regole:
     * - 0 = Verde
     * - Numeri rossi = Rosso (come definito in RED_NUMBERS)
     * - Altrimenti = Nero (come definito in BLACK_NUMBERS)
     */
    private String getColor(int number) {
        if (number == 0)
            return "Verde";
        if (contains(RED_NUMBERS, number))
            return "Rosso";
        if (contains(BLACK_NUMBERS, number))
            return "Nero";
        return "N/A";
    }

    /**
     * Determina se il numero è pari o dispari.
     * 
     * Regole:
     * - 0 = N/A (non è né pari né dispari)
     * - Altrimenti: "Pari" se divisibile per 2, "Dispari" altrimenti
     */
    private String getParity(int number) {
        if (number == 0)
            return "N/A";
        return (number % 2 == 0) ? "Pari" : "Dispari";
    }

    /**
     * Determina se il numero è basso (1-18) o alto (19-36).
     * 
     * Regole:
     * - 0 = N/A
     * - 1-18 = "Basso"
     * - 19-36 = "Alto"
     */
    private String getRange(int number) {
        if (number == 0)
            return "N/A";
        return (number <= 18) ? "Basso" : "Alto";
    }

    /**
     * Determina se il risultato è una vittoria o una perdita.
     * 
     * Regole:
     * - Risultato positivo (> 0) = "Vittoria"
     * - Risultato negativo (<= 0) = "Perdita"
     */
    private String getSituation(double result) {
        return result > 0 ? "Vittoria" : "Perdita";
    }

    /**
     * Controlla se un numero è presente in un array.
     * 
     * Utile per verificare a quale dozzina appartiene un numero.
     */
    private boolean contains(int[] array, int value) {
        for (int num : array)
            if (num == value)
                return true;
        return false;
    }

    /**
     * Applica effetti visivi ai pulsanti (ingrandimento e ombra).
     * 
     * Effetti:
     * - Su hover: ingrandimento del 10% e ombra
     * - Su uscita: ritorno allo stato originale
     */
    private void applyButtonEffects(Button button) {
        button.setOnMouseEntered(e -> {
            button.setStyle(
                    "-fx-background-color: #45a049; -fx-effect: dropshadow(three-pass-box, rgba(0, 0, 0, 0.2), 10, 0, 0, 5); -fx-cursor: hand;");
            ScaleTransition scale = new ScaleTransition(Duration.millis(200), button);
            scale.setToX(1.1);
            scale.setToY(1.1);
            scale.play();
        });
        button.setOnMouseExited(e -> {
            button.setStyle(
                    "-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-background-radius: 5px; -fx-padding: 10 20; -fx-font-size: 14px;");
            ScaleTransition scale = new ScaleTransition(Duration.millis(200), button);
            scale.setToX(1.0);
            scale.setToY(1.0);
            scale.play();
        });
    }

    /**
     * Applica un'animazione di ingrandimento alle ComboBox.
     * 
     * Effetto: aumento temporaneo del 10% su selezione.
     */
    private void applyComboBoxAnimation(ComboBox<?> comboBox) {
        comboBox.setOnAction(e -> {
            ScaleTransition scale = new ScaleTransition(Duration.millis(200), comboBox);
            scale.setFromX(1.0);
            scale.setFromY(1.0);
            scale.setToX(1.1);
            scale.setToY(1.1);
            scale.setAutoReverse(true);
            scale.setCycleCount(2);
            scale.play();
        });
    }

    /**
     * Applica un effetto neon al TextArea delle statistiche.
     * 
     * Effetto: transizione dal trasparente al blu acceso.
     */
    private void addNeonEffect(TextArea textArea) {
        InnerShadow innerShadow = new InnerShadow();
        innerShadow.setColor(Color.TRANSPARENT);
        textArea.setEffect(innerShadow);
        Timeline timeline = new Timeline(
                new KeyFrame(Duration.ZERO, new KeyValue(innerShadow.colorProperty(), Color.TRANSPARENT)),
                new KeyFrame(Duration.seconds(1), new KeyValue(innerShadow.colorProperty(), Color.BLUE)));
        timeline.play();
    }

    /**
     * Rimuove l'effetto neon dal TextArea.
     * 
     * Effetto: transizione dal blu acceso al trasparente.
     */
    private void removeNeonEffect(TextArea textArea) {
        InnerShadow innerShadow = (InnerShadow) textArea.getEffect();
        if (innerShadow == null) {
            innerShadow = new InnerShadow();
            innerShadow.setColor(Color.BLUE);
            textArea.setEffect(innerShadow);
        }
        Timeline timeline = new Timeline(
                new KeyFrame(Duration.ZERO, new KeyValue(innerShadow.colorProperty(), Color.BLUE)),
                new KeyFrame(Duration.seconds(1), new KeyValue(innerShadow.colorProperty(), Color.TRANSPARENT)));
        timeline.setOnFinished(e -> {
            textArea.setEffect(null);
            textArea.setStyle(""); // Ripristina lo stile originale
        });
        timeline.play();
    }

    /**
     * Mostra una finestra con lo storico delle simulazioni.
     * 
     * Funzionalità:
     * - Elenco scorrevole delle simulazioni (timestamp, totale, lanci)
     * - Cliccando su un elemento, apre un grafico dettagliato
     */
    private void showHistoryWindow() {
        Stage stage = new Stage();
        stage.setTitle("Storico Grafici");
        ListView<String> list = new ListView<>();
        for (SimulationData data : simulationHistory) {
            list.getItems().add(data.timestamp + " | Totale: " + data.totalProfit + "€ | Lanci: " + data.spins);
        }
        list.setOnMouseClicked(event -> {
            int index = list.getSelectionModel().getSelectedIndex();
            if (index >= 0)
                openGraphWindow(simulationHistory.get(index));
        });
        VBox layout = new VBox(10, new Label("Seleziona una simulazione:"), list);
        layout.setPadding(new Insets(10));
        stage.setScene(new Scene(layout, 400, 300));
        stage.show();
    }

    /**
     * Apre una finestra con il grafico dell'andamento del profitto/perdita.
     * 
     * Parametro: SimulationData - dati della simulazione selezionata
     */
    private void openGraphWindow(SimulationData data) {
        Stage graphStage = new Stage();
        graphStage.setTitle("Grafico Simulazione - " + data.timestamp);
        final NumberAxis xAxis = new NumberAxis();
        final NumberAxis yAxis = new NumberAxis();
        xAxis.setLabel("Lancio");
        yAxis.setLabel("Profitto/Perdita (€)");
        LineChart<Number, Number> chart = new LineChart<>(xAxis, yAxis);
        chart.setTitle("Andamento Profitto - " + data.timestamp);
        XYChart.Series<Number, Number> series = new XYChart.Series<>();
        series.setName("Profitto/Perdita");
        for (int i = 0; i < data.profits.size(); i++) {
            series.getData().add(new XYChart.Data<>(i + 1, data.profits.get(i)));
        }
        chart.getData().add(series);
        Scene scene = new Scene(chart, 800, 600);
        graphStage.setScene(scene);
        graphStage.show();
    }

    /**
     * Classe ausiliaria per memorizzare i dati delle simulazioni.
     */
    private static class SimulationData {
        String timestamp;
        List<Double> profits;
        int spins;
        double totalProfit;

        public SimulationData(String timestamp, List<Double> profits, int spins, double totalProfit) {
            this.timestamp = timestamp;
            this.profits = profits;
            this.spins = spins;
            this.totalProfit = totalProfit;
        }
    }

    /**
     * Metodo principale che avvia l'applicazione.
     */
    public static void main(String[] args) {
        launch(args);
    }
}