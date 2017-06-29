import java.io.File;
import java.util.Date;
import java.util.HashMap;

import jxl.*;
import jxl.write.*;
import jxl.write.Number;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;

public class PropertyScraperRunner {
	private PropertyScraperWriter excelFile;
	private WebDriver driver;

	public PropertyScraperRunner() {
		System.setProperty("webdriver.gecko.driver", "C:/geckodriver.exe");
		this.driver = new FirefoxDriver();
		try {
			this.excelFile = new PropertyScraperWriter();
		} catch (Exception e) {
			System.out.println("Failed loading excel addresses. Check the xls file");
			System.exit(1);
		}
	}

	public void close() {
		driver.quit();
	}

	public void runHamiltonProperty(String houseNumber, String streetName) {
		driver.get("http://wedge1.hcauditor.org");


		driver.findElement(By.xpath("//*[@id=\"house_number_low\"]")).sendKeys(houseNumber);
		driver.findElement(By.xpath("//*[@id=\"street_name\"]")).sendKeys(streetName);
		driver.findElement(By.xpath("//*[@id=\"search_by_street_address\"]/div[2]/button[1]")).click();

		String marketImprovementValue = driver.findElement(By.xpath("//*[@id=\"tax-credit-value-summary\"]/tbody/tr[9]/td[2]")).getText();
		String marketLandValue = driver.findElement(By.xpath("//*[@id=\"tax-credit-value-summary\"]/tbody/tr[7]/td[2]")).getText();
		String marketTotalValue = driver.findElement(By.xpath("//*[@id=\"tax-credit-value-summary\"]/tbody/tr[10]/td[2]")).getText();
		String purchaseDate = driver.findElement(By.xpath("//*[@id=\"property_overview_wrapper\"]/table[1]/tbody/tr[6]/td[2]")).getText();
		driver.findElement(By.xpath("//*[@id=\"parcel-tabs\"]/a[2]")).click();

		String stories = driver.findElement(By.xpath("//*[@id=\"3656845\"]/table[2]/tbody/tr[2]/td[2]")).getText() + " Story";
		String squareFootage = driver.findElement(By.xpath("//*[@id=\"ajaxDiv\"]/table/tbody/tr[2]/td[2]")).getText();
		String basementType = driver.findElement(By.xpath("//*[@id=\"3656845\"]/table[1]/tbody/tr[5]/td[2]")).getText();
		String yearBuilt = driver.findElement(By.xpath("//*[@id=\"ajaxDiv\"]/table/tbody/tr[2]/td[3]")).getText();

		excelFile.writeToSheet(marketImprovementValue, marketLandValue, marketTotalValue, purchaseDate, stories, squareFootage, basementType, yearBuilt);
		System.out.println(marketImprovementValue);
		System.out.println(marketLandValue);
		System.out.println(marketTotalValue);
		System.out.println(purchaseDate);
		System.out.println(stories);
		System.out.println(squareFootage);
		System.out.println(basementType);
		System.out.println(yearBuilt);
	}

    public static void main(String[] args) {
		PropertyScraperRunner runner = new PropertyScraperRunner();
		runner.runHamiltonProperty("10311","September Dr");
		runner.close();
    }
}
