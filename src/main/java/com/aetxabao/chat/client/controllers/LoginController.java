package com.aetxabao.chat.client.controllers;

import com.aetxabao.chat.client.app.ClientApplication;
import com.aetxabao.chat.client.net.ChatClient;
import com.fasterxml.jackson.core.JsonProcessingException;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.ConnectException;

import static com.aetxabao.chat.client.consts.AppConsts.*;

public class LoginController extends AController {

    @FXML
    private PasswordField txtContrasena;

    @FXML
    private TextField txtPuerto;

    @FXML
    private TextField txtServidor;

    @FXML
    private TextField txtUsuario;



    private String username = "";
    private ChatClient chatClient;

    private Stage stage;


    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public void setChatClient(ChatClient chatClient) {
        this.chatClient = chatClient;
    }


    @FXML
    void logearse(ActionEvent event) {
        try {
            chatClient.connect("127.0.0.1", 4242);
            chatClient.login("yo","1234");
        } catch (InterruptedException e) {
            //
        } catch (ConnectException e) {
            //
        } catch (JsonProcessingException e) {
            //
        }

    }

    @Override
    public void loginResponse(boolean result, String response) {
        if (result){
            try {
                Platform.runLater(()->{
                    stage.setTitle(TITLE + " " + username);
                });

                FXMLLoader fxmlLoader = new FXMLLoader(ClientApplication.class.getResource("/com/aetxabao/chat/client/app/GuiClient.fxml"));

                Parent parent = fxmlLoader.load();

                ChatController controller = fxmlLoader.getController();
                controller.setStage(stage);
                controller.setChatClient(chatClient);
                chatClient.setController(controller);

                stage.getScene().setRoot(parent);

                stage.setOnCloseRequest(event -> {
                    controller.shutdown();
                });

            } catch (IOException e) {
                e.printStackTrace();
            }
        }else{
            Platform.runLater(()->{

            });
        }
    }

    public void shutdown() {
        try{
            chatClient.close();
        }catch(Exception e){
        }finally {
            Platform.exit();
            System.exit(0);
        }
    }

    private void handlerAlertas(String titulo, String mensaje)
    {
        Alert alerta = new Alert(Alert.AlertType.WARNING);
        alerta.setHeaderText(titulo);
        alerta.setContentText(mensaje.toUpperCase());
        alerta.showAndWait();
    }
}
