import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import org.apache.commons.math3.random.MersenneTwister;
import org.apache.commons.math3.random.RandomGenerator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class Simulation {
	
	public static void main(String[] args){
		String file = "OptimalActions25.xlsx";
		int numberOfDemandStates = 10;
		int numberOfPriceStates = 10;
		ArrayList<Integer> periods = new ArrayList<>();
//		for (int i = 1; i < 51; i++){
//			periods.add(i*12);
//		}
		periods.add(12); periods.add(24); periods.add(120); periods.add(600); periods.add(6000); periods.add(12000); periods.add(60000);
		periods.add(120000); periods.add(600000); periods.add(1200000);
		int availability = 7;
		int inventory = 24;
		int seed = 67890;
		ArrayList<State> states = new ArrayList<>();
		try {
			FileInputStream fis = new FileInputStream(new File(file));
			
			Workbook wb = new XSSFWorkbook(fis);
			Sheet sheet = wb.getSheetAt(0);
			int rowNum = 1;
			Row row = sheet.getRow(rowNum);
			for (int i = 0; i < numberOfDemandStates; i++){
				for (int j = 0; j < numberOfPriceStates; j++){
					State state = new State(i, new Price(j), availability, inventory);
					double lowerCount = row.getCell(2).getNumericCellValue();
					double keepCount = row.getCell(3).getNumericCellValue();
					double raiseCount = row.getCell(4).getNumericCellValue();
					if(lowerCount > keepCount && lowerCount > raiseCount){
						state.setOptimalPricingAction(-1);
					}
					else if(keepCount > lowerCount && keepCount > raiseCount){
						state.setOptimalPricingAction(0);
					}
					else if(raiseCount > lowerCount && raiseCount > keepCount){
						state.setOptimalPricingAction(1);
					}
					states.add(state);
					rowNum++;
					row = sheet.getRow(rowNum);
				}
			}
			wb.close();
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		try{
			String xlsxFileAddress = new java.io.File( "." ).getCanonicalPath() + "/Performance.xlsx"; //output file
			XSSFWorkbook workbook = new XSSFWorkbook();
			XSSFSheet sheet = workbook.createSheet("Performance");
			sheet.createRow(0).createCell(0).setCellValue("Time horizon (months)");
			sheet.createRow(1).createCell(0).setCellValue("Total expected profit");
			sheet.createRow(2).createCell(0).setCellValue("Average expected profit per month");
			sheet.createRow(3).createCell(0).setCellValue("Total expected demand");
			sheet.createRow(4).createCell(0).setCellValue("Average expected demand per month");
			sheet.createRow(5).createCell(0).setCellValue("Average selling price (USD)");
			sheet.createRow(6).createCell(0).setCellValue("Fraction of time price kept constant");
			sheet.createRow(7).createCell(0).setCellValue("Number of months with no demand");
			sheet.createRow(8).createCell(0).setCellValue("Fraction of time with no demand");
			sheet.createRow(9).createCell(0).setCellValue("Units purchased from market");
			sheet.createRow(10).createCell(0).setCellValue("Units purchased from supplier");
			
			int colNum = 1;
			for (int p:periods){
				RandomGenerator rng = new MersenneTwister(seed);
				
				// State variables
				int demand = rng.nextInt(numberOfDemandStates);
				int price = rng.nextInt(numberOfPriceStates);
				State currentState = new State(demand, new Price(price), availability, inventory);
				
				// Performance related variables
				double avgExpectedProfit = 0;
				double totalExpectedProfit = 0;
				double avgPrice = 0;
				double fractionKeep = 0;
				int totalDemand = 0;
				double avgDemand = 0;
				int totalFromMarket = 0;
				int totalFromSupplier = 0;
				int periodsNoDemand = 0;
				double fractionNoDemand = 0;
				
				//Run for a certain number of periods
				for (int i = 0; i < p; i++){
					//determine optimal action in the current state
					int optimalAction = -1000;
					for (State s:states){
						if(s.getDemand() == currentState.getDemand() && s.getPrice() == currentState.getPrice()){
							if(s.getOptimalPricingAction() == -1000){
								optimalAction = 0;
							}
							else{
								optimalAction = s.getOptimalPricingAction();
							}
							break;
						}
					}
					
					//determine next state
					double determinator = rng.nextDouble();
					currentState.createNextStates();
					ArrayList<StateTransition> transitions = new ArrayList<>();
					if(optimalAction == -1){
						transitions = currentState.getLowerTransitions();
					}
					else if(optimalAction == 0){
						transitions = currentState.getKeepTransitions();
						fractionKeep++;
					}
					else if(optimalAction == 1){
						transitions = currentState.getUpperTransitions();
					}
					double probabilitySum = 0;
					StateTransition nextTransition = null;
					for(StateTransition transition:transitions){
						probabilitySum += transition.getProbability();
						if(determinator <= probabilitySum){
							nextTransition = transition;
							break;
						}
					}
					State nextState = nextTransition.getNextState();
					if(nextTransition.getAction().getMarketAction() == true){
						totalFromMarket += Math.min(availability, nextState.getDemand());
					}
					if(nextTransition.getAction().getSupplierAction() == true){
						totalFromSupplier += nextState.getDemand() - availability;
					}
					totalExpectedProfit += nextState.getCurrentReward();
					totalDemand += nextState.getDemand();
					avgPrice += nextState.getPrice();
					if(nextState.getDemand() == 0){
						periodsNoDemand++;
					}
					currentState = nextState;
				}
				avgExpectedProfit = totalExpectedProfit/p;
				avgDemand = (totalDemand + 0.0)/p;
				avgPrice = avgPrice/p;
				fractionKeep = Math.round(fractionKeep/p*1000.0)/1000.0;
				fractionNoDemand = Math.round((periodsNoDemand + 0.0)/p*1000.0)/1000.0;
				System.out.println("The system's performance after " + p + " months: ");
				System.out.println("Total expected profit: " + totalExpectedProfit);
				System.out.println("Average expected profit: " + avgExpectedProfit);
				System.out.println("Total expected demand: " + totalDemand);
				System.out.println("Average expected demand: " + avgDemand);
				System.out.println("Average price: " + avgPrice);
				System.out.println("Fraction of time price is kept constant: " + fractionKeep);
				System.out.println("Periods with no demand: " + periodsNoDemand);
				System.out.println("Number of units purchased from market: " + totalFromMarket);
				System.out.println("Number of units purchased from supplier: " + totalFromSupplier);
				
				sheet.getRow(0).createCell(colNum).setCellValue(p);
				sheet.getRow(1).createCell(colNum).setCellValue(Math.round(totalExpectedProfit*100)/100.0);
				sheet.getRow(2).createCell(colNum).setCellValue(Math.round(avgExpectedProfit*100)/100.0);
				sheet.getRow(3).createCell(colNum).setCellValue(totalDemand);
				sheet.getRow(4).createCell(colNum).setCellValue(Math.round(avgDemand*100)/100.0);
				sheet.getRow(5).createCell(colNum).setCellValue(Math.round(avgPrice*100)/100.0);
				sheet.getRow(6).createCell(colNum).setCellValue(fractionKeep);
				sheet.getRow(7).createCell(colNum).setCellValue(periodsNoDemand);
				sheet.getRow(8).createCell(colNum).setCellValue(fractionNoDemand);
				sheet.getRow(9).createCell(colNum).setCellValue(totalFromMarket);
				sheet.getRow(10).createCell(colNum).setCellValue(totalFromSupplier);
				
				colNum++;
			}
			for (int column = 0; column < sheet.getRow(0).getLastCellNum(); column++){ //expand columns
				sheet.autoSizeColumn(column);
				}
			FileOutputStream fileOutputStream =  new FileOutputStream(xlsxFileAddress);
			try {
				workbook.write(fileOutputStream);
				}
			finally {
				fileOutputStream.close();
				workbook.close();
				}
			
		} catch(Exception e){
			e.printStackTrace();
		}
	}
}
