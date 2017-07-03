package com.ben;

import java.util.concurrent.TimeUnit;

import javafx.scene.control.ProgressBar;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;

import javax.swing.*;

public class PropertyScraperRunner {
	private PropertyScraperWriter excelFile;
	private WebDriver driver;

	public PropertyScraperRunner() {
		try {
			this.excelFile = new PropertyScraperWriter();
		} catch (Exception e) {
			PropertyScraperWindow.log.add("Failed loading excel addresses. Check the xls file");
			System.exit(1);
		}
	}

	public void openBrowser() {
		System.setProperty("webdriver.chrome.driver", "chromedriver.exe");
		//System.setProperty("webdriver.gecko.driver", "geckodriver.exe");
		this.driver = new ChromeDriver();
	}

	public void close() {
		driver.close();
	}

	public void go(ProgressBar progressBar) {
		this.openBrowser();

		int hamilton = 0; int campbell = 0;int kenton = 0; int franklin = 0; int clermont = 0; int brown = 0; int other = 0; int butler = 0;

		while (this.excelFile.hasNext()) {
			try {
				String county = this.getCounty(this.excelFile.currentZipCode());

				if (county.equals("Hamilton")) {
					runHamiltonProperty(this.excelFile.currentStreetNumber(), this.excelFile.currentStreetName());
				} else {
					this.excelFile.incrementRow();
				}

				/*if (county.equals("Hamilton")) {
					hamilton++;
				} else if (county.equals("Campbell")) {
					campbell++;
				} else if (county.equals("Brown")) {
					brown++;
				} else if (county.equals("Franklin")) {
					franklin++;
				} else if (county.equals("Clermont")) {
					clermont++;
				} else if (county.equals("Kenton")) {
					com.ben.PropertyScraperWindow.log.add(excelFile.currentFullAddress());
					kenton++;
				} else if (county.equals("Butler")) {
					butler++;
				} else {
					other++;
					com.ben.PropertyScraperWindow.log.add("No function for getting info from: " + county);
				}*/

			} catch (Exception e) {
				e.printStackTrace();
				PropertyScraperWindow.log.add("Error finding zip");
				this.excelFile.incrementRow();
			}

			progressBar.setProgress((double)this.excelFile.getRowIndex()/(double)this.excelFile.getListOfAddresses().size());
		}

		try {
			this.excelFile.closeWriter();
		} catch (Exception e) {
			e.printStackTrace();
			PropertyScraperWindow.log.add("Welp you almost made it");
		}
		this.close();
	}

	public String getCounty(String zipCode) throws Exception {
		Document doc = Jsoup.connect("http://www.getzips.com/cgi-bin/ziplook.exe?What=1&Zip=" + zipCode + "&Submit=Look+It+Up").get();
		return doc.select("table").get(2).select("td").get(6).text();
	}

	public void runKentonProperty(String streetNumber, String streetName) {
		this.driver.get("http://kcor.org/");
		this.driver.findElement(By.xpath("//*[@id=\"submit1\"]")).click();

		String capcha = (String)JOptionPane.showInputDialog(
				null,
				"Please enter the CAPCHA",
				"Property Scraper", JOptionPane.PLAIN_MESSAGE, null, null, "");

		this.driver.findElement(By.xpath("//*[@id=\"input1\"]")).sendKeys(capcha);
		this.driver.findElement(By.xpath("//*[@id=\"submit1\"]")).click();
		this.driver.findElement(By.xpath("//*[@id=\"submit5\"]")).click();
		this.driver.findElement(By.xpath("//*[@id=\"submit2\"]")).click();

		this.driver.findElement(By.xpath("//*[@id=\"input2\"]")).sendKeys(streetNumber);
		this.driver.findElement(By.xpath("//*[@id=\"input1\"]")).sendKeys(streetName);
		this.driver.findElement(By.xpath("//*[@id=\"submit3\"]")).click();
	}

	public void runFranklinProperty(String streetNumber, String streetName) {


	}

	public void runButlerProperty(String streetNumber, String streetName) {

	}

	public void runWarrenProperty(String streetNumber, String streetName) {

	}

	public void runCampbellProperty(String streetNumber, String streetName) {

	}

	public void runBrownProperty(String streetNumber, String streetName) {

	}

	public void runHamiltonProperty(String houseNumber, String streetName) {
		driver.get("http://wedge1.hcauditor.org");

		try {
			driver.findElement(By.xpath("//*[@id=\"house_number_low\"]")).sendKeys(houseNumber);
			driver.findElement(By.xpath("//*[@id=\"street_name\"]")).sendKeys(streetName);
			driver.findElement(By.xpath("//*[@id=\"search_by_street_address\"]/div[2]/button[1]")).click();
			driver.manage().timeouts().implicitlyWait(25, TimeUnit.MILLISECONDS);
		} catch (Exception e) {
			e.printStackTrace();
			PropertyScraperWindow.log.add("Error filling out form");
			this.excelFile.incrementRow();
		}

		try {
			if (driver.findElement(By.xpath("//*[@id=\"search-results\"]/tbody/tr/td")).getText().equals("No data available in table")) {
				PropertyScraperWindow.log.add("No results, continuing...");
				this.excelFile.incrementRow();
				return;
			}
		} catch (Exception e) {
			PropertyScraperWindow.log.add("No empty table found trying to read info, continuing...");
		}

		try {
			driver.findElement(By.xpath("//*[@id=\"search-results\"]/tbody/tr[1]")).click();
			driver.manage().timeouts().implicitlyWait(25, TimeUnit.MILLISECONDS);
		} catch (Exception e) {
			PropertyScraperWindow.log.add("No multiple results found, continuing...");
		}

		try {
			driver.findElement(By.xpath("//*[@id=\"parcel-tabs\"]/a[1]")).click();
			String marketImprovementValue = driver.findElement(By.xpath("//*[@id=\"tax-credit-value-summary\"]/tbody/tr[9]/td[2]")).getText();
			String marketLandValue = driver.findElement(By.xpath("//*[@id=\"tax-credit-value-summary\"]/tbody/tr[7]/td[2]")).getText();
			String marketTotalValue = driver.findElement(By.xpath("//*[@id=\"tax-credit-value-summary\"]/tbody/tr[10]/td[2]")).getText();
			String purchaseDate = driver.findElement(By.xpath("//*[@id=\"property_overview_wrapper\"]/table[1]/tbody/tr[6]/td[2]")).getText();
			driver.findElement(By.xpath("//*[@id=\"parcel-tabs\"]/a[2]")).click();

			driver.manage().timeouts().implicitlyWait(5, TimeUnit.SECONDS);
			String stories = driver.findElements(By.xpath("//table[@class=\"residential datagrid left\"]/tbody/tr[2]/td[2]")).get(1).getText() + " Story";
			String squareFootage = driver.findElement(By.xpath("//*[@id=\"ajaxDiv\"]/table/tbody/tr[2]/td[2]")).getText();
			String basementType = driver.findElements(By.xpath("//table[@class=\"residential datagrid left\"]/tbody/tr[5]/td[2]")).get(0).getText(); //driver.findElement(By.xpath("//*[@id=\"ui-widget\"]/table[1]/tbody/tr[5]/td[2]")).getText();
			String yearBuilt = driver.findElement(By.xpath("//*[@id=\"ajaxDiv\"]/table/tbody/tr[2]/td[3]")).getText();

			this.excelFile.writeColumnCell(4, this.driver.getCurrentUrl());
			this.excelFile.writeColumnCell(5, stories);
			this.excelFile.writeColumnCell(8, squareFootage);
			this.excelFile.writeColumnCell(11, basementType);
			this.excelFile.writeColumnCell(12, yearBuilt);
			this.excelFile.writeColumnCell(13, purchaseDate);
			this.excelFile.writeColumnCell(15, marketTotalValue);
			this.excelFile.writeColumnCell(16, marketLandValue);
			this.excelFile.writeColumnCell(17, marketImprovementValue);
			this.excelFile.incrementRow();
		} catch (Exception e) {
			PropertyScraperWindow.log.add("Attempted searching: " + houseNumber + " " + streetName);
			e.printStackTrace();
			PropertyScraperWindow.log.add("Continuing...");
			this.excelFile.incrementRow();
		}

	}

	public PropertyScraperWriter getWriter() {
		return this.excelFile;
	}

    public static void main(String[] args) throws Exception {
		long start = System.currentTimeMillis();

		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
			e.printStackTrace();
		}

		PropertyScraperRunner runner = new PropertyScraperRunner();
		//runner.runKentonProperty("725","Dalton");
		//runner.go();
		long elapsedTimeMillis = System.currentTimeMillis() - start;
		float elapsedTimeSec = elapsedTimeMillis/1000F;
		//com.ben.PropertyScraperWindow.log.add(elapsedTimeSec);
		//runner.runHamiltonProperty("2604", "Hackberry");
    }
}
