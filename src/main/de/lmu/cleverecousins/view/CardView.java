package de.lmu.cleverecousins.view;

import de.lmu.cleverecousins.cards.Card;
import de.lmu.cleverecousins.viewmodel.CardViewModel;
import de.lmu.cleverecousins.viewmodel.LobbyViewModel;
import javafx.collections.ListChangeListener;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;


/**
 * View für die Kartenanzeige: Handkarten und Programmierregister.
 */
public class CardView extends VBox {

    private final LobbyViewModel lobbyViewModel;
    private final CardViewModel viewModel;
    private final HBox registerBox = new HBox(10);
    private final HBox handBox = new HBox(10);

    public CardView(LobbyViewModel lobbyViewModel) {
        this.lobbyViewModel = lobbyViewModel;
        this.viewModel = lobbyViewModel.getCardViewModel();

        VBox.setVgrow(this, Priority.NEVER);

        // Layout-Stil
        this.setSpacing(20);
        this.setPadding(new Insets(10));
        this.setAlignment(Pos.CENTER);
        this.setFillWidth(true);
        this.setMaxWidth(Double.MAX_VALUE);
        this.setMaxHeight(Region.USE_COMPUTED_SIZE);
        VBox.setVgrow(this, Priority.NEVER); // Gesamtbereich nicht wachsen lassen

        // RegisterBox konfigurieren
        registerBox.setAlignment(Pos.CENTER);
        registerBox.setSpacing(10);
        registerBox.setStyle("-fx-border-color: black; -fx-border-width: 2px; -fx-padding: 10px; -fx-border-radius: 8px;");
        registerBox.setPrefHeight(100);          // Maximal erlaubte Höhe für Karten
        registerBox.setMaxHeight(100);
        registerBox.setMinHeight(Region.USE_PREF_SIZE);
        HBox.setHgrow(registerBox, Priority.ALWAYS);
        VBox.setVgrow(registerBox, Priority.NEVER);

        // HandBox konfigurieren
        handBox.setAlignment(Pos.CENTER);
        handBox.setSpacing(10);
        handBox.setStyle("-fx-border-color: black; -fx-border-width: 2px; -fx-padding: 10px; -fx-border-radius: 8px;");
        handBox.setPrefHeight(100);              // Gleiche Höhe wie oben
        handBox.setMaxHeight(100);
        handBox.setMinHeight(Region.USE_PREF_SIZE);
        HBox.setHgrow(handBox, Priority.ALWAYS);
        VBox.setVgrow(handBox, Priority.NEVER);

        // Labels
        Label registerLabel = new Label("Programmier-Register:");
        registerLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        Label handLabel = new Label("Handkarten:");
        handLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        // Zusammenfügen
        this.getChildren().addAll(registerLabel, registerBox, handLabel, handBox);


        // Vgrow-Regeln für Boxen
        VBox.setVgrow(registerBox, Priority.NEVER); // feste Höhe über binding
        VBox.setVgrow(handBox, Priority.ALWAYS);    // darf wachsen, wenn Platz da ist

        // Inhalte anzeigen
        updateRegister();
        updateHand();

        // Listener für Änderungen
        viewModel.getRegisterSlots().addListener((ListChangeListener<Card>) change -> updateRegister());
        viewModel.getHandCards().addListener((ListChangeListener<Card>) change -> updateHand());

        // Max. Höhe des gesamten Fensters nutzen
        this.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                this.maxHeightProperty().bind(newScene.heightProperty().multiply(0.45));
            }
        });

    }

    private void updateRegister() {
        registerBox.getChildren().clear();
        var slots = viewModel.getRegisterSlots();
        int totalSlots = slots.size();

        for (int i = 0; i < totalSlots; i++) {
            Card card = slots.get(i);
            ImageView slotView = createCardImageView(card, registerBox, totalSlots); // ✅ dritter Parameter ergänzt
            int slotIndex = i;

            slotView.setOnMouseClicked(e -> {
                // Phase 1: Auswahlphase – Karte entfernen
                if (viewModel.isCanSelect()) {
                    viewModel.removeCardFromRegisterSlot(slotIndex);
                    lobbyViewModel.sendSelectedCard(null, slotIndex);
                }

                // Phase 3: Ausführungsphase – Karte spielen
                else if (lobbyViewModel.isMyTurnProperty().get() && lobbyViewModel.isExecutionPhaseProperty().get()) {
                    Card selectedCard = viewModel.getRegisterSlots().get(slotIndex);
                    if (selectedCard != null) {
                        lobbyViewModel.sendPlayCard(selectedCard.getName());
                        viewModel.clearRegisterSlot(slotIndex);
                    }
                }
            });

            registerBox.getChildren().add(slotView);
        }
    }

    private void updateHand() {
        handBox.getChildren().clear();
        var handCards = viewModel.getHandCards();
        int totalCards = handCards.size();

        for (Card card : handCards) {
            ImageView cardView = createCardImageView(card, handBox, totalCards);

            cardView.setOnMouseClicked(e -> {
                if (viewModel.isCanSelect()) {
                    int placedIndex = viewModel.assignCardToNextFreeSlot(card);
                    if (placedIndex != -1) {
                        lobbyViewModel.sendSelectedCard(card.getName(), placedIndex);

                        if (viewModel.isRegisterFull()) {
                            viewModel.discardRemainingCards();
                            viewModel.setSelectionFinished(true);
                            lobbyViewModel.sendSelectionFinished();
                        }
                    }
                }
            });

            handBox.getChildren().add(cardView);
        }
    }



    private ImageView createCardImageView(Card card, Region parentBox, int totalCards) {
        ImageView imageView = new ImageView();
        imageView.setPreserveRatio(true);

        double maxHeight = 100; // Max. Höhe der Karte in px
        double aspectRatio = 1.5;

        imageView.setFitHeight(maxHeight);
        imageView.setFitWidth(maxHeight / aspectRatio);

        if (card != null) {
            imageView.setImage(new Image(card.getImagePath()));
        }

        // Hovereffekt
        imageView.setOnMouseEntered(e -> imageView.setStyle("-fx-effect: dropshadow(gaussian, blue, 8, 0.5, 0, 0);"));
        imageView.setOnMouseExited(e -> imageView.setStyle(""));

        return imageView;
    }



}

