package com.kepo.view;

import com.kepo.service.AIRecommendationService;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

public class AIChatPanel extends VBox implements RefreshablePanel {

    private final AIRecommendationService aiRecService;
    private final MainLayout mainLayout;

    private VBox chatFlow;
    private ScrollPane chatScroll;
    private TextField inputField;
    private Button sendBtn;

    public AIChatPanel(AIRecommendationService aiRecService, MainLayout mainLayout) {
        this.aiRecService = aiRecService;
        this.mainLayout = mainLayout;

        initUI();
    }

    private void initUI() {
        setSpacing(20);
        setPadding(new Insets(24));
        setStyle("-fx-background-color: " + ThemeConstants.AI_BG + ";");

        // --- Header Row ---
        HBox header = new HBox(15);
        header.setAlignment(Pos.CENTER_LEFT);

        Label title = new Label("Pusat Kendali Asisten AI");
        title.setFont(Font.font("System", FontWeight.BOLD, 22));
        title.setTextFill(Color.web(ThemeConstants.AI_TEXT));

        Label subtitle = new Label("Sistem RAG Terintegrasi Database Bencana");
        subtitle.setFont(Font.font("System", FontWeight.NORMAL, 12));
        subtitle.setTextFill(Color.web(ThemeConstants.AI_MUTED));

        header.getChildren().addAll(title, subtitle);
        getChildren().add(header);

        // --- Suggested Prompt Pills Row ---
        HBox promptBox = new HBox(10);
        promptBox.setAlignment(Pos.CENTER_LEFT);
        
        String[] suggestions = {
            "Obat apa yang hampir habis?",
            "Shelter mana yang penuh?",
            "Bantuan apa yang harus diprioritaskan?",
            "Tampilkan ringkasan status posko"
        };

        for (String suggest : suggestions) {
            Button pill = new Button(suggest);
            pill.setStyle("-fx-background-color: " + ThemeConstants.AI_SURFACE + ";" +
                    "-fx-text-fill: " + ThemeConstants.AI_TEXT + ";" +
                    "-fx-border-color: " + ThemeConstants.AI_BORDER + ";" +
                    "-fx-border-radius: 15;" +
                    "-fx-background-radius: 15;" +
                    "-fx-cursor: hand;" +
                    "-fx-font-size: 11;");
            pill.setOnAction(e -> {
                inputField.setText(suggest);
                handleSend();
            });
            promptBox.getChildren().add(pill);
        }
        getChildren().add(promptBox);

        // --- Chat Bubble Scroll Area ---
        chatFlow = new VBox(15);
        chatFlow.setPadding(new Insets(10));
        chatFlow.setStyle("-fx-background-color: transparent;");

        chatScroll = new ScrollPane(chatFlow);
        VBox.setVgrow(chatScroll, Priority.ALWAYS);
        chatScroll.setFitToWidth(true);
        // ScrollPane styling for dark theme
        chatScroll.setStyle("-fx-background-color: transparent; -fx-background: transparent; -fx-border-color: " + ThemeConstants.AI_BORDER + "; -fx-border-radius: 12; -fx-background-radius: 12;");
        
        // Welcome message
        addMessageBubble("Halo! Saya asisten AI KEPO. Saya siap membantu Anda menganalisis data korban, shelter evakuasi, inventaris obat, dan logistik bantuan bencana secara real-time dari database. Apa yang ingin Anda tanyakan?", false);

        getChildren().add(chatScroll);

        // --- Input Message Panel ---
        HBox inputBar = new HBox(12);
        inputBar.setAlignment(Pos.CENTER_LEFT);

        inputField = new TextField();
        HBox.setHgrow(inputField, Priority.ALWAYS);
        inputField.setPromptText("Tulis pertanyaan Anda di sini...");
        inputField.setPrefHeight(45);
        inputField.setStyle("-fx-background-color: " + ThemeConstants.AI_SURFACE + ";" +
                "-fx-text-fill: " + ThemeConstants.AI_TEXT + ";" +
                "-fx-border-color: " + ThemeConstants.AI_BORDER + ";" +
                "-fx-border-radius: 8;" +
                "-fx-background-radius: 8;" +
                "-fx-padding: 0 12 0 12;");
        inputField.setOnAction(e -> handleSend());

        sendBtn = new Button("Kirim");
        sendBtn.setPrefHeight(45);
        sendBtn.setStyle(ThemeConstants.PRIMARY_BTN_STYLE);
        sendBtn.setOnAction(e -> handleSend());

        inputBar.getChildren().addAll(inputField, sendBtn);
        getChildren().add(inputBar);
    }

    private void handleSend() {
        String msg = inputField.getText().trim();
        if (msg.isEmpty()) return;

        // Add user bubble
        addMessageBubble(msg, true);
        inputField.clear();

        // Loading indicator
        HBox loaderBubble = addMessageBubble("Sedang mengetik...", false);

        // Run AI request in background to prevent UI freeze
        new Thread(() -> {
            String aiResponse = aiRecService.chat(msg);
            javafx.application.Platform.runLater(() -> {
                chatFlow.getChildren().remove(loaderBubble);
                addMessageBubble(aiResponse, false);
            });
        }).start();
    }

    private HBox addMessageBubble(String text, boolean isUser) {
        HBox wrapper = new HBox();
        wrapper.setMaxWidth(Double.MAX_VALUE);
        wrapper.setAlignment(isUser ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);

        VBox bubble = new VBox(5);
        bubble.setPadding(new Insets(12, 16, 12, 16));
        bubble.setMaxWidth(600);
        
        Label senderLabel = new Label(isUser ? "Anda (Petugas)" : "Asisten AI KEPO");
        senderLabel.setFont(Font.font("System", FontWeight.BOLD, 10));
        senderLabel.setTextFill(Color.web(isUser ? ThemeConstants.PRIMARY_LIGHT : ThemeConstants.SECONDARY));

        TextFlow textFlow = createFormattedTextFlow(text);

        bubble.getChildren().addAll(senderLabel, textFlow);

        if (isUser) {
            bubble.setStyle("-fx-background-color: " + ThemeConstants.AI_SURFACE + ";" +
                    "-fx-background-radius: 16 16 2 16;" +
                    "-fx-border-color: " + ThemeConstants.AI_BORDER + ";" +
                    "-fx-border-radius: 16 16 2 16;");
        } else {
            bubble.setStyle("-fx-background-color: #0b1320;" +
                    "-fx-background-radius: 16 16 16 2;" +
                    "-fx-border-color: " + ThemeConstants.AI_BORDER + ";" +
                    "-fx-border-radius: 16 16 16 2;");
        }

        wrapper.getChildren().add(bubble);
        chatFlow.getChildren().add(wrapper);

        // Auto scroll to bottom
        chatScroll.layout();
        chatScroll.setVvalue(1.0);

        return wrapper;
    }

    private TextFlow createFormattedTextFlow(String text) {
        TextFlow textFlow = new TextFlow();
        textFlow.setMaxWidth(560);
        
        String[] lines = text.split("\n");
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];
            
            // Check if it is a bullet point list item
            boolean isBullet = false;
            if (line.trim().startsWith("* ")) {
                isBullet = true;
                line = line.trim().substring(2);
                
                Text bullet = new Text("  • ");
                bullet.setFont(Font.font("Inter", FontWeight.BOLD, 13));
                bullet.setFill(Color.web(ThemeConstants.AI_TEXT));
                textFlow.getChildren().add(bullet);
            } else if (line.trim().startsWith("- ")) {
                isBullet = true;
                line = line.trim().substring(2);
                
                Text bullet = new Text("  • ");
                bullet.setFont(Font.font("Inter", FontWeight.BOLD, 13));
                bullet.setFill(Color.web(ThemeConstants.AI_TEXT));
                textFlow.getChildren().add(bullet);
            }
            
            // Parse bold elements **bold** in the line
            int idx = 0;
            while (idx < line.length()) {
                int start = line.indexOf("**", idx);
                if (start == -1) {
                    String normalText = line.substring(idx);
                    Text textNode = new Text(normalText);
                    textNode.setFont(Font.font("Inter", FontWeight.NORMAL, 13));
                    textNode.setFill(Color.web(ThemeConstants.AI_TEXT));
                    textFlow.getChildren().add(textNode);
                    break;
                } else {
                    if (start > idx) {
                        String normalText = line.substring(idx, start);
                        Text textNode = new Text(normalText);
                        textNode.setFont(Font.font("Inter", FontWeight.NORMAL, 13));
                        textNode.setFill(Color.web(ThemeConstants.AI_TEXT));
                        textFlow.getChildren().add(textNode);
                    }
                    
                    int end = line.indexOf("**", start + 2);
                    if (end == -1) {
                        String normalText = line.substring(start);
                        Text textNode = new Text(normalText);
                        textNode.setFont(Font.font("Inter", FontWeight.NORMAL, 13));
                        textNode.setFill(Color.web(ThemeConstants.AI_TEXT));
                        textFlow.getChildren().add(textNode);
                        break;
                    } else {
                        String boldText = line.substring(start + 2, end);
                        Text textNode = new Text(boldText);
                        textNode.setFont(Font.font("Inter", FontWeight.BOLD, 13));
                        textNode.setFill(Color.web(ThemeConstants.AI_TEXT));
                        textFlow.getChildren().add(textNode);
                        idx = end + 2;
                    }
                }
            }
            
            if (i < lines.length - 1) {
                textFlow.getChildren().add(new Text("\n"));
            }
        }
        
        return textFlow;
    }

    @Override
    public void refreshData() {
        // No auto state to refresh in chat flow, persists until navigation reload
    }
}
