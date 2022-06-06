package com.geekbrains.cloud.june.cloudapplication;

import com.geekbrains.cloud.*;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;

public class ChatController implements Initializable {


    public TextField clientPath;
    public TextField serverPath;
    private Path homeDir = Path.of(System.getProperty("user.home"));

    @FXML
    public ListView<String> clientView;

    @FXML
    public ListView<String> serverView;

    private Network network;

    private void readLoop() {
        try {
            while (true) {
                CloudMessage message = network.read();
                if (message instanceof ListFiles listFiles) {
                    Platform.runLater(() -> {
                        serverView.getItems().clear();
                        serverView.getItems().addAll(listFiles.getFiles());
                        serverPath.setText(listFiles.getPath());

                    });
                } else if (message instanceof FileMessage fileMessage) {
                    Path current = homeDir.resolve(fileMessage.getName());
                    Files.write(current, fileMessage.getData());
                    Platform.runLater(() -> {
                        clientView.getItems().clear();
                        clientView.getItems().addAll(getFiles(homeDir.toString()));
                    });
                }
            }
        } catch (Exception e) {
            System.err.println("Connection lost");
        }
    }

    // post init fx fields
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        try {
            clientPath.setText(homeDir.toString());
            clientView.getItems().clear();
            clientView.getItems().addAll(getFiles(homeDir.toString()));
            network = new Network(8189);
            Thread readThread = new Thread(this::readLoop);
            readThread.setDaemon(true);
            readThread.start();

        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }

    private List<String> getFiles(String dir) {
        String[] list = new File(dir).list();
        assert list != null;
        return Arrays.asList(list);
    }

    public void upload(ActionEvent actionEvent) throws IOException {
        String file = clientView.getSelectionModel().getSelectedItem();
        network.write(new FileMessage(homeDir.resolve(file)));
    }

    public void download(ActionEvent actionEvent) throws IOException {
        String file = serverView.getSelectionModel().getSelectedItem();
        network.write(new FileRequest(file));
    }

    public void pathInRequestClients(MouseEvent mouseEvent) {
        try {
            if (mouseEvent.getClickCount() == 2 ){
                String fileName = clientView.getSelectionModel().getSelectedItem();
                homeDir = homeDir.resolve(fileName);
                System.out.println(homeDir);
                if (Files.isDirectory(homeDir)){
                    clientView.getItems().clear();
                    clientView.getItems().addAll(getFiles(homeDir.toString()));
                    clientPath.clear();
                    clientPath.setText(homeDir.toString());
                }
            }
        }catch (RuntimeException ignored){
        }
    }

    public void pathUpRequestClient(ActionEvent actionEvent) {
        try {
            homeDir = homeDir.getParent();
            System.out.println(homeDir);
            clientView.getItems().clear();
            clientView.getItems().addAll(getFiles(homeDir.toString()));
            clientPath.clear();
            clientPath.setText(homeDir.toString());
        }catch (RuntimeException ignored ){
        }finally {
            homeDir = Path.of("C:\\");
            clientView.getItems().clear();
            clientView.getItems().addAll(getFiles(homeDir.toString()));
            clientPath.clear();
            clientPath.setText(homeDir.toString());
        }
    }

    public void pathUpRequestServer(ActionEvent actionEvent) throws IOException {
        network.write(new PathUpRequest());
    }

    public void pathInRequestServer(MouseEvent mouseEvent) throws IOException {
        try {
            if (mouseEvent.getClickCount() == 2 ){
                String fileName = serverView.getSelectionModel().getSelectedItem();
                network.write(new PathInRequest(fileName));
            }
        }catch (RuntimeException ignored){
        }
    }

    public void goClientPath(ActionEvent actionEvent) {
        if (Files.isDirectory(Path.of(clientPath.getText()))) {
            homeDir = Path.of(clientPath.getText()).toAbsolutePath();
            clientView.getItems().clear();
            clientView.getItems().addAll(getFiles(homeDir.toString()));
            clientPath.clear();
            clientPath.setText(homeDir.toString());
        }
    }

    public void goServerPath(ActionEvent actionEvent) throws IOException {
        String file = serverPath.getText();
        network.write(new PathFindRequest(file));
    }
}