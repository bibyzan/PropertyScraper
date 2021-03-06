package com.ben;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
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

	private Workbook original;
	private WritableWorkbook workbook;
	private WritableSheet currentSheet;

	public PropertyScraperWriter() {
		try {
			this.original = Workbook.getWorkbook(new File("Property Template.xls"));
			this.workbook = Workbook.createWorkbook(new File("scraped properties.xls"), this.original);
			this.currentSheet = this.workbook.getSheet(0);
			this.rowIndex = 1;
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void closeWriter() throws Exception {
		this.workbook.write();
		this.workbook.close();
		this.original.close();
	}
	public boolean hasNext() {
		return !this.currentFullAddress().equals("");
	}

	public void incrementRow() {
		this.rowIndex++;
	}

	public int getRowIndex() {
		return this.rowIndex;
	}

	public ObservableList<String> getListOfAddresses() {
		int oldIndex = this.rowIndex;
		this.rowIndex = 1;
		ObservableList<String> addresses = FXCollections.observableArrayList();
		while (this.hasNext()) {
			addresses.add(this.currentFullAddress());
			this.incrementRow();
		}

		this.rowIndex = oldIndex;
		return addresses;
	}

	public String currentFullAddress() {
		return this.currentSheet.getCell(0, this.rowIndex).getContents();
	}

	public String currentStreetName() {
		try {
			try {
				return this.currentFullAddress().split(" ")[1] + " " + this.currentFullAddress().split(" ")[2];
			} catch (Exception e) {
				return this.currentFullAddress().split(" ")[1];
			}
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

	public String currentZipCode() {
		return this.currentSheet.getCell(2, this.rowIndex).getContents();
	}

	public void writeColumnCell(int col, String text) throws Exception {
		if (this.currentSheet.getWritableCell(col, this.rowIndex).getType() == CellType.LABEL) {
			((Label)this.currentSheet.getWritableCell(col, this.rowIndex)).setString(text);
		} else {
			this.currentSheet.addCell(new Label(col, this.rowIndex, text));
		}
	}

	public void writeToSheet(String marketImprovementValue, String marketLandValue, String marketTotalValue, String purchaseDate, String stories, String squareFootage, String basementType, String yearBuilt) {
		try {
			this.writeColumnCell(5, stories);
			this.writeColumnCell(8, squareFootage);
			this.writeColumnCell(11, basementType);
			this.writeColumnCell(12, yearBuilt);
			this.writeColumnCell(13, purchaseDate);
			this.writeColumnCell(15, marketTotalValue);
			this.writeColumnCell(16, marketLandValue);
			this.writeColumnCell(17, marketImprovementValue);
			this.rowIndex++;
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
