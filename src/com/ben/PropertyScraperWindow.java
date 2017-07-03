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
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.util.concurrent.CountDownLatch;

/**
 * Created by Ben Rasmussen on 6/28/2017.
 */
public class PropertyScraperWindow extends Application {
	public static ObservableList<String> log = FXCollections.observableArrayList();

	public static void main(String[] args) {
		launch(args);
	}

	@Override
	public void start(Stage primaryStage) throws Exception {
		primaryStage.setScene(new Scene(new PropertyScraperGUI(),700,350));
		primaryStage.show();
	}

	private class PropertyScraperGUI extends HBox {
		private PropertyScraperRunner runner;
		private ProgressBar progressBar;
		private Button startScraping;
		private ListView<String> addresses;
		private long start;
		private int counter;

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
											PropertyScraperWindow.log.add("Couldn't scrape address for county: " + county);
										}
									}
								} catch (Exception e) {
									PropertyScraperWindow.log.add("Error finding county for zip code: " + runner.getWriter().currentZipCode());
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
						addresses.getSelectionModel().select(runner.getWriter().getRowIndex());
						addresses.scrollTo(runner.getWriter().getRowIndex());
						if (runner.getWriter().hasNext()) {
							propertyService.reset();
							propertyService.start();
						} else {
							runner.close();
							try {
								runner.getWriter().closeWriter();
								long elapsedTimeMillis = System.currentTimeMillis() - start;
								float elapsedTimeSec = elapsedTimeMillis/1000F;
								PropertyScraperWindow.log.add("Scraped " + counter + " properties in " + elapsedTimeSec + " seconds!");
							} catch (Exception e) {
								PropertyScraperWindow.log.add("Error saving to excel.");
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
			this.progressBar.setPrefWidth(250);
			this.startScraping = new Button("Start Scraping");
			this.startScraping.setOnAction(this.startScrapingAction);
			this.addresses = new ListView<String>();
			this.counter = 0;
			addresses.setItems(this.runner.getWriter().getListOfAddresses());
			Text title = new Text("Property Scraper");
			title.setFont(new Font("Lucida Fax Demibold Italic", 34));

			//System.out.println(Font.getFontNames());

			VBox firstRow = new VBox(15);
			firstRow.getChildren().addAll(title , this.startScraping, this.progressBar, this.addresses);
			firstRow.setPadding(new Insets(20, 20, 20, 20));

			Text logTitle = new Text("Log: ");
			final ListView<String> devLog = new ListView<String>();
			devLog.setItems(PropertyScraperWindow.log);
			devLog.setPrefWidth(300);
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
			secondRow.getChildren().addAll(logTitle, devLog);
			secondRow.setPadding(new Insets(20, 20, 20, 20));

			super.getChildren().addAll(firstRow, secondRow);
		}
	}
}
