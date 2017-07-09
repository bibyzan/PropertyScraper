package com.ben;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressBar;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.CountDownLatch;

public class Main extends Application {
    public static ObservableList<String> log = FXCollections.observableArrayList();

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        final PropertyScraperGUI gui = new PropertyScraperGUI();
        primaryStage.setScene(new Scene(gui ,650,390));
        primaryStage.show();
        primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent event) {
                gui.close();
            }
        });
    }

    public static InputStream pullResource(String path) {
        System.out.println(path);

        return Main.class.getClassLoader().getResourceAsStream(path);
    }

    private class PropertyScraperGUI extends HBox {
        private PropertyScraperRunner runner;
        private ProgressBar progressBar;
        private Button startScraping;
        private Button openExcel;
        private ListView<String> addresses;
        private long start;
        private int counter;

        public void close() {
            if (this.runner != null) {
                this.runner.close();
            }
        }

        private Service<Void> propertyService = new Service<Void>() {
            @Override
            protected Task<Void> createTask() {
                Task<Void> task = new Task<Void>() {
                    @Override
                    protected Void call() throws Exception {
                        //Background work
                        final CountDownLatch latch = new CountDownLatch(1);
                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    if (runner.getWriter().hasNext()) {
                                        String county = runner.getCounty(runner.getWriter().currentZipCode());

                                        if (county.equals("Hamilton")) {
                                            runner.runHamiltonProperty(runner.getWriter().currentStreetNumber(), runner.getWriter().currentStreetName());
                                            counter++;
                                        } else {
                                            runner.getWriter().incrementRow();
                                            Main.log.add("Couldn't scrape address for county: " + county);
                                        }
                                    }
                                } catch (Exception e) {
                                    Main.log.add("Error finding county for zip code: " + runner.getWriter().currentZipCode());
                                } finally {
                                    latch.countDown();
                                }
                            }
                        });
                        latch.await();
                        //Keep with the background work
                        return null;
                    }
                };
                task.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
                    @Override
                    public void handle(WorkerStateEvent event) {
                        progressBar.setProgress((double) runner.getWriter().getRowIndex() / (double) runner.getWriter().getListOfAddresses().size());
                        addresses.getSelectionModel().select(runner.getWriter().getRowIndex() - 1);
                        addresses.scrollTo(runner.getWriter().getRowIndex() - 1);
                        if (runner.getWriter().hasNext()) {
                            propertyService.reset();
                            propertyService.start();
                        } else {
                            runner.close();
                            try {
                                openExcel.setDisable(false);
                                runner.getWriter().closeWriter();
                                long elapsedTimeMillis = System.currentTimeMillis() - start;
                                float elapsedTimeSec = elapsedTimeMillis/1000F;
                                Main.log.add("Scraped " + counter + " properties in " + elapsedTimeSec + " seconds!");
                                float propertySec = elapsedTimeSec / counter;
                                Main.log.add(propertySec + " properties/sec");
                            } catch (Exception e) {
                                Main.log.add("Error saving to excel.");
                            }
                        }
                    }
                });
                return task;
            }
        };

        private EventHandler<ActionEvent> startScrapingAction = new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                start = System.currentTimeMillis();
                //startScraping.setText("Cancel");
                //startScraping.setOnAction(stopScrapingAction);
                addresses.getSelectionModel().select(0);
                startScraping.setDisable(true);
                //runner.go(progressBar);
                runner.openBrowser();

                propertyService.start();
            }
        };

        public PropertyScraperGUI() {
            this.runner = new PropertyScraperRunner();
            this.progressBar = new ProgressBar(0);
            this.progressBar.setPrefWidth(300);
            this.startScraping = new Button("Start Scraping");
            this.openExcel = new Button("Open scraped results");
            this.openExcel.setDisable(true);
            this.startScraping.setOnAction(this.startScrapingAction);
            this.addresses = new ListView<String>();
            this.counter = 0;
            if ((new File("Property Template.xls").exists())) {
                this.addresses.setItems(this.runner.getWriter().getListOfAddresses());
            }
            this.openExcel.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    try {
                        Desktop.getDesktop().open(new File("scraped properties.xls"));
                    } catch (IOException e) {
                        Main.launch("Couldn't find scraped properties.xls");
                    }
                }
            });
            Text title = new Text("Property Scraper");
            title.setFont(new Font("Lucida Fax Demibold Italic", 34));
            ImageView scraperImage = new ImageView(new Image(Main.pullResource("scraper.png")));
            scraperImage.setFitHeight(250);
            scraperImage.setFitWidth(268);

            HBox functionButtons = new HBox(10);
            functionButtons.getChildren().addAll(this.startScraping, this.openExcel);

            VBox firstRow = new VBox(15);
            firstRow.getChildren().addAll(title, functionButtons, this.progressBar, new Text("Addresses from template:"), this.addresses);
            firstRow.setPadding(new Insets(20, 20, 20, 20));

            Text logTitle = new Text("Log: ");
            final ListView<String> devLog = new ListView<String>();
            devLog.setItems(Main.log);
            devLog.setPrefWidth(250);
            devLog.getItems().addListener(new ListChangeListener<String>() {
                @Override
                public void onChanged(Change<? extends String> c) {
                    c.next();
                    final int size = devLog.getItems().size();
                    if (size > 0) {
                        devLog.scrollTo(size - 1);
                    }
                }
            });

            VBox secondRow = new VBox(5);
            secondRow.getChildren().addAll(scraperImage, logTitle, devLog);
            secondRow.setPadding(new Insets(20, 20, 20, 20));

            super.getChildren().addAll(firstRow, secondRow);
            if (!(new File("chromedriver.exe").exists())) {
                Main.log.add("couldn't find chromedriver.exe");
                this.startScraping.setDisable(true);
            }
            if (!(new File("Property Template.xls").exists())) {
                Main.log.add("couldn't find Property Template.xls");
                this.startScraping.setDisable(true);
            }
        }
    }
}
