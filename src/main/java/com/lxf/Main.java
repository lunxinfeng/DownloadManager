package com.lxf;

import com.lxf.download.DownloadManager;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

public class Main extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {

        Pane root = new Pane();
        root.setPrefWidth(800);
        root.setPrefHeight(600);
        Button button = new Button("下载");
        button.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                download("http://www.izis.cn/GoWebService/yike.apk", "yike.apk");
            }
        });
        root.getChildren().addAll(button);
        Scene scene = new Scene(root);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

    private void download(String url, String filePath){
        DownloadManager manager = new DownloadManager();
        manager.setListener(new DownloadManager.DownloadListener() {
            @Override
            public void onStart(long totalLength) {
                System.out.println("start load :" + totalLength);
            }

            @Override
            public void onProgress(int progress) {
                System.out.println("loading :" + progress);
            }

            @Override
            public void onComplete() {
                System.out.println("load complete");
            }

            @Override
            public void onFail() {
                System.out.println("load fail");
            }
        });
        manager.down(url, filePath);

//        DownloadManager.down(url, filePath, new DownloadListener() {
//            @Override
//            public void onStart(long totalLength) {
//                System.out.println("开始下载:" + totalLength);
//            }
//
//            @Override
//            public void onProgress(int progress) {
//                System.out.println("下载中:" + progress);
//            }
//
//            @Override
//            public void onComplete() {
//                System.out.println("下载完成");
//            }
//
//            @Override
//            public void onFail() {
//                System.out.println("下载失败");
//            }
//        });
    }
}
