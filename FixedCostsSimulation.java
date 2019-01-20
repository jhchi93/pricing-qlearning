import java.io.FileOutputStream;
import java.util.ArrayList;

import org.apache.commons.math3.random.MersenneTwister;
import org.apache.commons.math3.random.RandomGenerator;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class FixedCostsSimulation {
	
	public static void main(String[] args){
		int numberOfDemandStates = 10;
		ArrayList<Integer> periods = new ArrayList<>();
//		for (int i = 1; i < 51; i++){
//			periods.add(i*12);
//		}
		periods.add(12); periods.add(24); periods.add(120); periods.add(600); periods.add(6000); periods.add(12000); periods.add(60000);
		periods.add(120000); periods.add(600000); periods.add(1200000);
		int price = 3;
		int availability = 7;
		int inventory = 24;
		int seed = 67890;
		
		try{
			String xlsxFileAddress = new java.io.File( "." ).getCanonicalPath() + "/PerformanceFixed.xlsx"; //output file
			XSSFWorkbook workbook = new XSSFWorkbook();
			XSSFSheet sheet = workbook.createSheet("Performance");
			sheet.createRow(0).createCell(0).setCellValue("Time horizon (months)");
			sheet.createRow(1).createCell(0).setCellValue("Total expected profit");
			sheet.createRow(2).createCell(0).setCellValue("Average expected profit per month");
			sheet.createRow(3).createCell(0).setCellValue("Total expected demand");
			sheet.createRow(4).createCell(0).setCellValue("Average expected demand per month");
			sheet.createRow(5).createCell(0).setCellValue("Number of months with no demand");
			sheet.createRow(6).createCell(0).setCellValue("Fraction of time with no demand");
			sheet.createRow(7).createCell(0).setCellValue("Units purchased from market");
			sheet.createRow(8).createCell(0).setCellValue("Units purchased from supplier");
			
			int colNum = 1;
			for (int p:periods){
				RandomGenerator rng = new MersenneTwister(seed);
				
				// State variables
				int demand = rng.nextInt(numberOfDemandStates);
				State currentState = new State(demand, new Price(price), availability, inventory);
				
				// Performance related variables
				double avgExpectedProfit = 0;
				double totalExpectedProfit = 0;
				int totalDemand = 0;
				double avgDemand = 0;
				int totalFromMarket = 0;
				int totalFromSupplier = 0;
				int periodsNoDemand = 0;
				double fractionNoDemand = 0;
				
				//Run for a certain number of periods
				for (int i = 0; i < p; i++){
					//determine next state
					double determinator = rng.nextDouble();
					currentState.createNextStates();
					ArrayList<StateTransition> transitions = currentState.getKeepTransitions();
					
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
					if(nextState.getDemand() == 0){
						periodsNoDemand++;
					}
					currentState = nextState;
				}
				avgExpectedProfit = totalExpectedProfit/p;
				avgDemand = (totalDemand + 0.0)/p;
				fractionNoDemand = Math.round((periodsNoDemand + 0.0)/p*1000.0)/1000.0;
				System.out.println("The system's performance after " + p + " months: ");
				System.out.println("Total expected profit: " + totalExpectedProfit);
				System.out.println("Average expected profit: " + avgExpectedProfit);
				System.out.println("Total expected demand: " + totalDemand);
				System.out.println("Average expected demand: " + avgDemand);
				System.out.println("Periods with no demand: " + periodsNoDemand);
				System.out.println("Number of units purchased from market: " + totalFromMarket);
				System.out.println("Number of units purchased from supplier: " + totalFromSupplier);
				
				sheet.getRow(0).createCell(colNum).setCellValue(p);
				sheet.getRow(1).createCell(colNum).setCellValue(Math.round(totalExpectedProfit*100)/100.0);
				sheet.getRow(2).createCell(colNum).setCellValue(Math.round(avgExpectedProfit*100)/100.0);
				sheet.getRow(3).createCell(colNum).setCellValue(totalDemand);
				sheet.getRow(4).createCell(colNum).setCellValue(Math.round(avgDemand*100)/100.0);
				sheet.getRow(5).createCell(colNum).setCellValue(periodsNoDemand);
				sheet.getRow(6).createCell(colNum).setCellValue(fractionNoDemand);
				sheet.getRow(7).createCell(colNum).setCellValue(totalFromMarket);
				sheet.getRow(8).createCell(colNum).setCellValue(totalFromSupplier);
				
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
