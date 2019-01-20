import java.io.FileOutputStream;
import java.util.ArrayList;
import org.apache.commons.math3.random.MersenneTwister;
import org.apache.commons.math3.random.RandomGenerator;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class MarkovDecisionProcess {
	private static ArrayList<QValue> QList;
	private static ArrayList<StateActionTuple> counts;
	
	public static void main(String[] args) {
		int numberOfDemandStates = 10;
		int numberOfPriceStates = 10;
		int iterations = 5;
		
		int availability = 7;
		int inventory = 24;
		
		ArrayList<Integer> seeds = new ArrayList<>();
		int startSeed = 12345;
		for(int i = 0; i < iterations; i++){
			seeds.add(startSeed);
			startSeed++;
		}
		
		ArrayList<OptimalAction> optimalActions = new ArrayList<>();
		for (int i = 0; i < 10; i++){
			for (int j = 0; j < 10; j++){
				OptimalAction optimalAction = new OptimalAction(new State(i, new Price(j), availability, inventory));
				optimalActions.add(optimalAction);
			}
		}
		
		int timeHorizon = 100000000;
		double discountRate = 0.9;
		double learningRate = 0.05;
		
		for (int seed:seeds){
			QList = createQ(numberOfDemandStates, numberOfPriceStates, learningRate, availability, inventory); //Initialize Q-values
			counts = new ArrayList<>();
			for(int i=0; i<10;i++){
				for(int j=0;j<10;j++){
					for(int k=-1;k<2;k++){
						counts.add(new StateActionTuple(i, j, k));
					}
				}
			}
			RandomGenerator rng = new MersenneTwister(seed);
//			System.out.println(QList.size());
//			for(QValue Q: QList){
//				System.out.println("Demand: " + Q.getState().getDemand() + " Price: " + Q.getState().getPrice()+" Action: "+Q.getAction());
//			}
			
			int initialDemand = rng.nextInt(10);
			int initialPrice = rng.nextInt(10);
			
			State s = new State(initialDemand, new Price(initialPrice), availability, inventory); //Initial state
			for (int i = 0; i < timeHorizon; i++){ //For each period until T
				State nextState = moveToNextState(s, rng, discountRate); //Go to next state
				s = nextState;
			}
			
			for(OptimalAction opt:optimalActions){
				int bestAction = -1000;
				double bestQ = Double.NEGATIVE_INFINITY;
				for (QValue Q:QList){
					if(opt.getState().getDemand() == Q.getState().getDemand() && opt.getState().getPrice() == Q.getState().getPrice()){
						if (Q.getQ() > bestQ && Q.getQ() != 0){
							bestAction = Q.getAction();
							bestQ = Q.getQ();
						}
					}
				}
				opt.addPricingAction(bestAction);
			}
			countToExcel();
//			writeToExcel(seed);
//			for (QValue Q:QList){
//				System.out.print("Demand state: " + Q.getState().getDemand());
//				System.out.print("  Price state: " + Q.getState().getPrice());
//				System.out.print("  Action: " + Q.getAction());
//				System.out.println("  Q-Value: " + Q.getQ());
//			}
		}
		actionsToExcel(optimalActions);
//		for (OptimalAction opt:optimalActions){
//			System.out.println("Best action for state (" + opt.getState().getDemand() + ", " + opt.getState().getPrice() + "): " 
//		+ opt.getOptimalAction() + " with lower optimal " + opt.getCountLowerBest() + " time(s), constant best " + 
//					opt.getCountConstantBest() + " time(s), raise best " + opt.getCountRaiseBest() + " time(s).");
//		}
		
	}
	
	private static State moveToNextState(State s, RandomGenerator rng, double discountRate){
		double actionGenerator = rng.nextDouble(); //Generate random number to determine next pricing action
		ArrayList<Integer> feasibleActions = s.getActionSpace(); //Determine feasible pricing actions
		double actionProbability = 1.0/feasibleActions.size();
		int action = 0;
		for (int i = 0; i < feasibleActions.size(); i++){
			if(actionGenerator <= (i+1)*actionProbability){
				action = feasibleActions.get(i); //Determine pricing action
				break;
			}
		}
		
		for(StateActionTuple tuple:counts){
			if(tuple.getDemand() == s.getDemand() && tuple.getPrice() == s.getPrice() && tuple.getAction() == action){
				tuple.incrementCount();
				break;
			}
		}
		
		double nextStateGenerator = rng.nextDouble(); //Generate random number to determine next state transition
		s.createNextStates();
		ArrayList<StateTransition> transitions = new ArrayList<>(); //Determine all possible transitions given chosen pricing action
		if(action == -1){
			transitions = s.getLowerTransitions();
		}
		else if(action == 0){
			transitions = s.getKeepTransitions();
		}
		else if(action == 1){
			transitions = s.getUpperTransitions();
		}
		double probabilitySum = 0;
		StateTransition nextTransition = null; //Determine the transition after chosen pricing action
		for(StateTransition transition:transitions){
			probabilitySum += transition.getProbability();
			if(nextStateGenerator <= probabilitySum){
				nextTransition = transition;
				break;
			}
		}
		State nextState = nextTransition.getNextState(); //Get the next state
		double directRewards = nextState.getCurrentReward(); //Get the direct rewards
		double bestQ = Double.NEGATIVE_INFINITY;
		for (QValue Q:QList){
			if(Q.getState().getDemand() == nextState.getDemand() && Q.getState().getPrice() == nextState.getPrice()){
				if(Q.getQ() > bestQ){
					bestQ = Q.getQ();
				}
			}
		}
		bestQ = bestQ*discountRate; //Discount the expected future rewards
		for (QValue Q:QList){
			if(Q.getState().getDemand() == s.getDemand() && Q.getState().getPrice() == s.getPrice() && Q.getAction() == action){
				Q.updateQ(directRewards + bestQ); //Update respective Q-value
				break;
			}
		}
		
		return nextState;
	}
	
	private static ArrayList<QValue> createQ(int numberOfDemandStates, int numberOfPriceStates, double learningRate, 
			int availability, int inventory){
		ArrayList<QValue> QList = new ArrayList<>();
		
		for (int i = 0; i < numberOfDemandStates; i++){ //Demand
			for (int j = 0; j < numberOfPriceStates; j++){
				State state = new State(i, new Price(j), availability, inventory);
				if(i != numberOfDemandStates - 1 && j != 0){
					QValue QLower = new QValue(state, -1, learningRate, 0);
					QList.add(QLower);
				}
				QValue QKeep = new QValue(state, 0, learningRate, 0);
				QList.add(QKeep);
				if (i != 0 && j != numberOfPriceStates - 1){
					QValue QUpper = new QValue(state, 1, learningRate, 0);
					QList.add(QUpper);
				}
			}
		}
		return QList;
	}
	
//	private static void writeToExcel(int name){
//		try{
//			String xlsxFileAddress = new java.io.File( "." ).getCanonicalPath() + "/MarkovResults" + name + ".xlsx"; //output file
//			XSSFWorkbook workbook = new XSSFWorkbook();
//			XSSFSheet sheet = workbook.createSheet("Stats");
//			XSSFRow headerRow = sheet.createRow(0);
//			headerRow.createCell(0).setCellValue("Demand/Price state");
//			headerRow.createCell(1).setCellValue("Lower Price");
//			headerRow.createCell(2).setCellValue("Keep Price");
//			headerRow.createCell(3).setCellValue("Raise Price");
//			
//			int rowNum = 0;
//			int demand = -1;
//			int price = -1;
//			XSSFRow nextRow = sheet.getRow(rowNum);
//			for (QValue Q:QList){
//				if(Q.getState().getDemand() != demand || Q.getState().getPrice() != price){
//					demand = Q.getState().getDemand();
//					price = Q.getState().getPrice();
//					rowNum++;
//					nextRow = sheet.createRow(rowNum);
//					nextRow.createCell(0).setCellValue("(" + Q.getState().getDemand() + ", " + Q.getState().getPrice() + ")");
//				}
//				if(Q.getAction() == -1 && Q.getQ() != 0){
//					nextRow.createCell(1).setCellValue(Math.round(Q.getQ()*100)/100.0);
//				}
//				else if (Q.getAction() == 0 && Q.getQ() != 0){
//					nextRow.createCell(2).setCellValue(Math.round(Q.getQ()*100)/100.0);
//				}
//				else if(Q.getAction() == 1 && Q.getQ() != 0){
//					nextRow.createCell(3).setCellValue(Math.round(Q.getQ()*100)/100.0);
//				}
//			}
//			for (int column = 0; column < sheet.getRow(0).getLastCellNum(); column++){ //expand columns
//				sheet.autoSizeColumn(column);
//				}
//			FileOutputStream fileOutputStream =  new FileOutputStream(xlsxFileAddress);
//			try {
//				workbook.write(fileOutputStream);
//				}
//			finally {
//				fileOutputStream.close();
//				workbook.close();
//				}
//			
//		} catch(Exception e){
//			e.printStackTrace();
//		}
//	}
	
	private static void actionsToExcel(ArrayList<OptimalAction> optimalActions){
		try{
			String xlsxFileAddress = new java.io.File( "." ).getCanonicalPath() + "/OptimalActions.xlsx"; //output file
			XSSFWorkbook workbook = new XSSFWorkbook();
			XSSFSheet sheet = workbook.createSheet("Stats");
			XSSFRow headerRow = sheet.createRow(0);
			headerRow.createCell(0).setCellValue("Demand/Price state");
			headerRow.createCell(1).setCellValue("Best action");
			headerRow.createCell(2).setCellValue("#Lower");
			headerRow.createCell(3).setCellValue("#Keep");
			headerRow.createCell(4).setCellValue("#Raise");
			
			int rowNum = 0;
			for (OptimalAction opt:optimalActions){
				rowNum++;
				XSSFRow nextRow = sheet.createRow(rowNum);
				nextRow.createCell(0).setCellValue("(" + opt.getState().getDemand() + ", " + opt.getState().getPrice() + ")");
				if(opt.getOptimalAction() == -1){
					nextRow.createCell(1).setCellValue("Lower");
				}
				else if(opt.getOptimalAction() == 0){
					nextRow.createCell(1).setCellValue("Keep");
				}
				else if(opt.getOptimalAction() == 1){
					nextRow.createCell(1).setCellValue("Raise");
				}
				nextRow.createCell(2).setCellValue(opt.getCountLowerBest());
				nextRow.createCell(3).setCellValue(opt.getCountConstantBest());
				nextRow.createCell(4).setCellValue(opt.getCountRaiseBest());
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
	
	private static void countToExcel(){
		try{
			String xlsxFileAddress = new java.io.File( "." ).getCanonicalPath() + "/CountActions.xlsx"; //output file
			XSSFWorkbook workbook = new XSSFWorkbook();
			XSSFSheet sheet = workbook.createSheet("Counts");
			XSSFRow headerRow = sheet.createRow(0);
			headerRow.createCell(0).setCellValue("Demand/Price state");
			headerRow.createCell(1).setCellValue("#Lower");
			headerRow.createCell(2).setCellValue("#Keep");
			headerRow.createCell(3).setCellValue("#Raise");
			
			int curDemand = -1;
			int curPrice = -1;
			int rowNum = 0;
			for (StateActionTuple tuple:counts){
				XSSFRow nextRow = null;
				if(tuple.getDemand() != curDemand || tuple.getPrice() != curPrice){
					curDemand = tuple.getDemand();
					curPrice = tuple.getPrice();
					rowNum++;
					nextRow = sheet.createRow(rowNum);
					nextRow.createCell(0).setCellValue("(" + tuple.getDemand() + ", " + tuple.getPrice() + ")");
				}
				else{
					nextRow = sheet.getRow(rowNum);
				}
				if(tuple.getAction()==-1){
					nextRow.createCell(1).setCellValue(tuple.getCount());
				} else if(tuple.getAction() == 0){
					nextRow.createCell(2).setCellValue(tuple.getCount());
				}
				else if(tuple.getAction() == 1){
					nextRow.createCell(3).setCellValue(tuple.getCount());
				}
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
