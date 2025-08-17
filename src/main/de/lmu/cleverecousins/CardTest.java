package de.lmu.cleverecousins;

import de.lmu.cleverecousins.cards.damageCards.Spam;
import de.lmu.cleverecousins.cards.damageCards.TrojanHorse;
import de.lmu.cleverecousins.cards.damageCards.Virus;
import de.lmu.cleverecousins.cards.damageCards.Worm;
import de.lmu.cleverecousins.cards.programmingCards.*;
import de.lmu.cleverecousins.view.LobbyView;
import de.lmu.cleverecousins.view.LoginView;
import de.lmu.cleverecousins.viewmodel.CardViewModel;
import de.lmu.cleverecousins.viewmodel.LobbyViewModel;
import de.lmu.cleverecousins.viewmodel.LoginViewModel;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class CardTest extends Application {

    private static Stage primaryStage;

    @Override
    public void start(Stage stage) {
        primaryStage = stage;

        try {
            Client client = new Client();
            client.connect("localhost", 12345);

            // Gemeinsamer MessageHandler, ViewModel wird dynamisch gesetzt
            MessageHandler sharedHandler = new MessageHandler(null);
            client.setMessageConsumer(sharedHandler::handle);

            // LoginViewModel + Login-View
            LoginViewModel loginViewModel = new LoginViewModel(client);
            sharedHandler.setViewModel(loginViewModel);

            LoginView loginView = new LoginView(loginViewModel);
            Scene loginScene = new Scene(loginView, 400, 300);
            loginViewModel.setOnLoginSuccess(() -> showLobby(client, sharedHandler));

            stage.setTitle("Clevere Cousins");
            stage.setScene(loginScene);
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showLobby(Client client, MessageHandler handler) {
        // Lobby-ViewModel + View
        LobbyViewModel lobbyViewModel = new LobbyViewModel(client);
        handler.setViewModel(lobbyViewModel);

        // Testweise GUI mit Karten aus Demo-Daten
        CardViewModel cardVM = lobbyViewModel.getCardViewModel();
        cardVM.addCardToHand(new AgainCard());
        cardVM.addCardToHand(new BackUpCard());
        cardVM.addCardToHand(new MoveOneCard());
        cardVM.addCardToHand(new MoveTwoCard());
        cardVM.addCardToHand(new MoveThreeCard());
        cardVM.addCardToHand(new PowerUpCard());
        cardVM.addCardToHand(new TurnLeftCard());
        cardVM.addCardToHand(new TurnRightCard());
        cardVM.addCardToHand(new UTurnCard());
        cardVM.addCardToHand(new Spam());
        cardVM.addCardToHand(new TrojanHorse());
        cardVM.addCardToHand(new Virus());
        cardVM.addCardToHand(new Worm());

        // Lobby anzeigen
        LobbyView lobbyView = new LobbyView(lobbyViewModel);
        Scene lobbyScene = new Scene(lobbyView, 800, 600);
        primaryStage.setScene(lobbyScene);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
