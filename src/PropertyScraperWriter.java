import jxl.Cell;
import jxl.CellType;
import jxl.Sheet;
import jxl.Workbook;
import jxl.read.biff.BiffException;
import jxl.write.*;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by Ben Rasmussen on 6/28/2017.
 */
public class PropertyScraperWriter {
	private int rowIndex;

	private WritableWorkbook workbook;
	private WritableSheet currentSheet;

	public PropertyScraperWriter() throws Exception {
		Workbook readWorkbook = Workbook.getWorkbook(new File("Property Template.xls"));
		this.workbook = Workbook.createWorkbook(new File("scraped properties.xls"), readWorkbook);
		readWorkbook.close();
		this.currentSheet = this.workbook.getSheet(0);
		this.rowIndex = 1;
	}

	public String currentFullAddress() {
		return this.currentSheet.getCell(0, this.rowIndex).getContents();
	}

	public String currentStreetName() {
		try {
			String fullAddress = this.currentFullAddress();
			int firstSpaceIndex = fullAddress.indexOf(" ");
			return fullAddress.substring(firstSpaceIndex);
		} catch (Exception e) {
			System.out.println("Woops");
			e.printStackTrace();
			return "error";
		}
	}

	public String currentStreetNumber() {
		try {
			String fullAddress = this.currentFullAddress();
			return fullAddress.split(" ")[0];
		} catch (Exception e) {
			System.out.println("Woops");
			e.printStackTrace();
			return "error";
		}
	}

	public void writeToSheet(String marketImprovementValue, String marketLandValue, String marketTotalValue, String purchaseDate, String stories, String squareFootage, String basementType, String yearBuilt) {
		try {
			WritableCell tempCell = this.currentSheet.getWritableCell(5, this.rowIndex);


			//((Blank)this.currentSheet.getWritableCell(5,this.rowIndex)).l
			WritableCell cell = this.currentSheet.getWritableCell(0,0);
			System.out.println(this.currentSheet.getWritableCell(0,0).getContents());
			((Label)this.currentSheet.getWritableCell(5, this.rowIndex)).setString(stories);
			this.workbook.write();
			this.workbook.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		try {
			Workbook workbook = Workbook.getWorkbook(new File("Property Template.xls"));

			Sheet sheet = workbook.getSheet(0);
			Cell cell = sheet.getCell(0,19);

			System.out.println(cell.getContents());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
