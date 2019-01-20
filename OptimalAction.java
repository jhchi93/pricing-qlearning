import java.util.ArrayList;

public class OptimalAction {
	private final State state;
	private ArrayList<Integer> pricingAction;
	private int countLowerBest;
	private int countConstantBest;
	private int countRaiseBest;
	
	public OptimalAction(State state){
		this.state = state;
		pricingAction = new ArrayList<>();
		countLowerBest = 0;
		countConstantBest = 0;
		countRaiseBest = 0;
	}

	public State getState() {
		return state;
	}

	public void addPricingAction(int action) {
		pricingAction.add(action);
		if(action == -1){
			countLowerBest += 1;
		}
		else if(action == 0){
			countConstantBest += 1;
		}
		else if(action == 1){
			countRaiseBest += 1;
		}
	}
	
	public int getOptimalAction(){
		int bestAction = -1000;
		if(countLowerBest > countConstantBest && countLowerBest > countRaiseBest){
			bestAction = -1;
		}
		else if(countRaiseBest > countLowerBest && countRaiseBest > countConstantBest){
			bestAction = 1;
		}
		else if(countConstantBest > countLowerBest && countConstantBest > countRaiseBest){
			bestAction = 0;
		}
		return bestAction;
	}

	public int getCountLowerBest() {
		return countLowerBest;
	}

	public int getCountConstantBest() {
		return countConstantBest;
	}

	public int getCountRaiseBest() {
		return countRaiseBest;
	}
	
}
