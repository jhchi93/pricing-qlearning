import java.util.ArrayList;

public class State {
	final private int demand;
	final private Price price;
	final private int availability;
	final private int inventory;
	final private int marketPrice;
	final private int supplierPrice;
	final private ArrayList<StateTransition> nextLowerStates;
	final private ArrayList<StateTransition> nextStates;
	final private ArrayList<StateTransition> nextUpperStates;
	private int optimalPricingAction;
	
	public State(int demandState, Price priceState, int availabilityState, int inventoryState){
		demand = demandState;
		price = priceState;
		availability = availabilityState;
		inventory = inventoryState;
		marketPrice = 15087;
		supplierPrice = 17286;
		nextLowerStates = new ArrayList<>();
		nextStates = new ArrayList<>();
		nextUpperStates = new ArrayList<>();
		optimalPricingAction = -1000;
	}

	public int getOptimalPricingAction() {
		return optimalPricingAction;
	}

	public void setOptimalPricingAction(int optimalPricingAction) {
		this.optimalPricingAction = optimalPricingAction;
	}

	public int getDemand() {
		return demand;
	}

	public int getPrice() {
		return price.getValue();
	}

	public int getAvailability() {
		return availability;
	}

	public int getInventory() {
		return inventory;
	}
	
	public ArrayList<Integer> getActionSpace() {
		ArrayList<Integer> feasibleActions = new ArrayList<>();
		if(demand == 0 && price.getId() == 0){
			feasibleActions.add(0);
		}
		else if(demand == 9 && price.getId() == 9){
			feasibleActions.add(0);
		}
		else if(demand == 0 || price.getId() == 9){
			feasibleActions.add(-1); feasibleActions.add(0);
		}
		else if (demand == 9 || price.getId() == 0){
			feasibleActions.add(0); feasibleActions.add(1);
		}
		else {
			feasibleActions.add(-1); feasibleActions.add(0); feasibleActions.add(1);
		}
		return feasibleActions;
	}
	
	public ArrayList<StateTransition> getKeepTransitions() {
		return nextStates;
	}
	
	public ArrayList<StateTransition> getLowerTransitions() {
		return nextLowerStates;
	}
	
	public ArrayList<StateTransition> getUpperTransitions() {
		return nextUpperStates;
	}
	
	public double getCurrentReward(){
		if(demand > availability){
			return demand*price.getValue() - (demand - availability)*supplierPrice - availability*marketPrice 
					- (0.2/12)*supplierPrice*inventory;
		}
		else{
			return demand*(price.getValue() - marketPrice) - (0.2/12)*supplierPrice*inventory;
		}
	}
	
	public double getExpectedLowerReward(){
		double r = 0;
		if(nextLowerStates.size() != 0){
			for (StateTransition s:nextLowerStates){
				double expectedReward = s.getNextState().getCurrentReward();
				r += s.getProbability()*expectedReward;
			}
		}
		return r;
	}
	
	public double getExpectedKeepReward(){
		double r = 0;
		if(nextStates.size() !=0){
			for (StateTransition s:nextStates){
				double expectedReward = s.getNextState().getCurrentReward();
				r += s.getProbability()*expectedReward;
			}
		}
		return r;
	}
	
	public double getExpectedUpperReward(){
		double r = 0;
		if(nextUpperStates.size() != 0){
			for (StateTransition s:nextUpperStates){
				double expectedReward = s.getNextState().getCurrentReward();
				r += s.getProbability()*expectedReward;
			}
		}
		return r;
	}
	
	public void createNextStates(){
		int priceAction = 0;
		if(demand == 0){
			if(price.getId() > 0){
				priceAction = -1; //Lower price
				nextLowerStates.add(new StateTransition(new Action(priceAction, false, false), 
						new State(0, price.getLowerValue(), availability, inventory), 0.5));
				nextLowerStates.add(new StateTransition(new Action(priceAction, true, false), 
						new State(1, price.getLowerValue(), availability, inventory), 0.15));
				nextLowerStates.add(new StateTransition(new Action(priceAction, true, false), 
						new State(2, price.getLowerValue(), availability, inventory), 0.15));
				nextLowerStates.add(new StateTransition(new Action(priceAction, true, false), 
						new State(3, price.getLowerValue(), availability, inventory), 0.1));
				nextLowerStates.add(new StateTransition(new Action(priceAction, true, false), 
						new State(4, price.getLowerValue(), availability, inventory), 0.1));
			}
			priceAction = 0; //Keep price
			nextStates.add(new StateTransition(new Action(priceAction, false, false), 
					new State(0, price, availability, inventory), 0.7));
			nextStates.add(new StateTransition(new Action(priceAction, true, false), 
					new State(1, price, availability, inventory), 0.2));
			nextStates.add(new StateTransition(new Action(priceAction, true, false), 
					new State(2, price, availability, inventory), 0.1));
		}
		else if(demand == 1){
			if(price.getId() > 0){
				priceAction = -1; //Lower price
				nextLowerStates.add(new StateTransition(new Action(priceAction, false, false), 
						new State(0, price.getLowerValue(), availability, inventory), 0.4));
				nextLowerStates.add(new StateTransition(new Action(priceAction, true, false), 
						new State(1, price.getLowerValue(), availability, inventory), 0.2));
				nextLowerStates.add(new StateTransition(new Action(priceAction, true, false), 
						new State(2, price.getLowerValue(), availability, inventory), 0.15));
				nextLowerStates.add(new StateTransition(new Action(priceAction, true, false), 
						new State(3, price.getLowerValue(), availability, inventory), 0.1));
				nextLowerStates.add(new StateTransition(new Action(priceAction, true, false), 
						new State(4, price.getLowerValue(), availability, inventory), 0.1));
				nextLowerStates.add(new StateTransition(new Action(priceAction, true, false), 
						new State(5, price.getLowerValue(), availability, inventory), 0.05));
			}
			priceAction = 0; //Keep price
			nextStates.add(new StateTransition(new Action(priceAction, false, false), 
					new State(0, price, availability, inventory), 0.55));
			nextStates.add(new StateTransition(new Action(priceAction, true, false), 
					new State(1, price, availability, inventory), 0.2));
			nextStates.add(new StateTransition(new Action(priceAction, true, false), 
					new State(2, price, availability, inventory), 0.15));
			nextStates.add(new StateTransition(new Action(priceAction, true, false), 
					new State(3, price, availability, inventory), 0.1));
			if(price.getId() < 9){
				priceAction = 1; //Raise price
				nextUpperStates.add(new StateTransition(new Action(priceAction, false, false), 
						new State(0, price.getUpperValue(), availability, inventory), 0.7));
				nextUpperStates.add(new StateTransition(new Action(priceAction, true, false), 
						new State(1, price.getUpperValue(), availability, inventory), 0.2));
				nextUpperStates.add(new StateTransition(new Action(priceAction, true, false), 
						new State(2, price.getUpperValue(), availability, inventory), 0.1));
			}
		}
		else if(demand == 2){
			if(price.getId() > 0){
				priceAction = -1; //Lower price
				nextLowerStates.add(new StateTransition(new Action(priceAction, true, false), 
						new State(1, price.getLowerValue(), availability, inventory), 0.2));
				nextLowerStates.add(new StateTransition(new Action(priceAction, true, false), 
						new State(2, price.getLowerValue(), availability, inventory), 0.3));
				nextLowerStates.add(new StateTransition(new Action(priceAction, true, false), 
						new State(3, price.getLowerValue(), availability, inventory), 0.2));
				nextLowerStates.add(new StateTransition(new Action(priceAction, true, false), 
						new State(4, price.getLowerValue(), availability, inventory), 0.15));
				nextLowerStates.add(new StateTransition(new Action(priceAction, true, false), 
						new State(5, price.getLowerValue(), availability, inventory), 0.1));
				nextLowerStates.add(new StateTransition(new Action(priceAction, true, false), 
						new State(6, price.getLowerValue(), availability, inventory), 0.05));
			}
			priceAction = 0; //Keep price
			nextStates.add(new StateTransition(new Action(priceAction, false, false), 
					new State(0, price, availability, inventory), 0.4));
			nextStates.add(new StateTransition(new Action(priceAction, true, false), 
					new State(1, price, availability, inventory), 0.2));
			nextStates.add(new StateTransition(new Action(priceAction, true, false), 
					new State(2, price, availability, inventory), 0.2));
			nextStates.add(new StateTransition(new Action(priceAction, true, false), 
					new State(3, price, availability, inventory), 0.1));
			nextStates.add(new StateTransition(new Action(priceAction, true, false), 
					new State(4, price, availability, inventory), 0.1));
			if(price.getId() < 9){
				priceAction = 1; //Raise price
				nextUpperStates.add(new StateTransition(new Action(priceAction, false, false), 
						new State(0, price.getUpperValue(), availability, inventory), 0.6));
				nextUpperStates.add(new StateTransition(new Action(priceAction, true, false), 
						new State(1, price.getUpperValue(), availability, inventory), 0.25));
				nextUpperStates.add(new StateTransition(new Action(priceAction, true, false), 
						new State(2, price.getUpperValue(), availability, inventory), 0.1));
				nextUpperStates.add(new StateTransition(new Action(priceAction, true, false), 
						new State(3, price.getUpperValue(), availability, inventory), 0.05));
			}
		}
		else if(demand == 3){
			if(price.getId() > 0){
				priceAction = -1; //Lower price
				nextLowerStates.add(new StateTransition(new Action(priceAction, true, false), 
						new State(2, price.getLowerValue(), availability, inventory), 0.3));
				nextLowerStates.add(new StateTransition(new Action(priceAction, true, false), 
						new State(3, price.getLowerValue(), availability, inventory), 0.3));
				nextLowerStates.add(new StateTransition(new Action(priceAction, true, false), 
						new State(4, price.getLowerValue(), availability, inventory), 0.2));
				nextLowerStates.add(new StateTransition(new Action(priceAction, true, false), 
						new State(5, price.getLowerValue(), availability, inventory), 0.1));
				nextLowerStates.add(new StateTransition(new Action(priceAction, true, false), 
						new State(6, price.getLowerValue(), availability, inventory), 0.1));
			}
			priceAction = 0; //Keep price
			nextStates.add(new StateTransition(new Action(priceAction, true, false), 
					new State(1, price, availability, inventory), 0.2));
			nextStates.add(new StateTransition(new Action(priceAction, true, false), 
					new State(2, price, availability, inventory), 0.3));
			nextStates.add(new StateTransition(new Action(priceAction, true, false), 
					new State(3, price, availability, inventory), 0.35));
			nextStates.add(new StateTransition(new Action(priceAction, true, false), 
					new State(4, price, availability, inventory), 0.1));
			nextStates.add(new StateTransition(new Action(priceAction, true, false), 
					new State(5, price, availability, inventory), 0.05));
			if(price.getId() < 9){
				priceAction = 1; //Raise price
				nextUpperStates.add(new StateTransition(new Action(priceAction, false, false), 
						new State(0, price.getUpperValue(), availability, inventory), 0.6));
				nextUpperStates.add(new StateTransition(new Action(priceAction, true, false), 
						new State(1, price.getUpperValue(), availability, inventory), 0.2));
				nextUpperStates.add(new StateTransition(new Action(priceAction, true, false), 
						new State(2, price.getUpperValue(), availability, inventory), 0.1));
				nextUpperStates.add(new StateTransition(new Action(priceAction, true, false), 
						new State(3, price.getUpperValue(), availability, inventory), 0.05));
				nextUpperStates.add(new StateTransition(new Action(priceAction, true, false), 
						new State(4, price.getUpperValue(), availability, inventory), 0.05));
			}
		}
		else if(demand == 4){
			if(price.getId() > 0){
				priceAction = -1; //Lower price
				nextLowerStates.add(new StateTransition(new Action(priceAction, true, false), 
						new State(3, price.getLowerValue(), availability, inventory), 0.25));
				nextLowerStates.add(new StateTransition(new Action(priceAction, true, false), 
						new State(4, price.getLowerValue(), availability, inventory), 0.25));
				nextLowerStates.add(new StateTransition(new Action(priceAction, true, false), 
						new State(5, price.getLowerValue(), availability, inventory), 0.25));
				nextLowerStates.add(new StateTransition(new Action(priceAction, true, false), 
						new State(6, price.getLowerValue(), availability, inventory), 0.15));
				nextLowerStates.add(new StateTransition(new Action(priceAction, true, false), 
						new State(7, price.getLowerValue(), availability, inventory), 0.1));
			}
			priceAction = 0; //Keep price
			nextStates.add(new StateTransition(new Action(priceAction, true, false), 
					new State(2, price, availability, inventory), 0.4));
			nextStates.add(new StateTransition(new Action(priceAction, true, false), 
					new State(3, price, availability, inventory), 0.25));
			nextStates.add(new StateTransition(new Action(priceAction, true, false), 
					new State(4, price, availability, inventory), 0.25));
			nextStates.add(new StateTransition(new Action(priceAction, true, false), 
					new State(5, price, availability, inventory), 0.1));
			if(price.getId() < 9){
				priceAction = 1; //Raise price
				nextUpperStates.add(new StateTransition(new Action(priceAction, false, false), 
						new State(0, price.getUpperValue(), availability, inventory), 0.4));
				nextUpperStates.add(new StateTransition(new Action(priceAction, true, false), 
						new State(1, price.getUpperValue(), availability, inventory), 0.25));
				nextUpperStates.add(new StateTransition(new Action(priceAction, true, false), 
						new State(2, price.getUpperValue(), availability, inventory), 0.2));
				nextUpperStates.add(new StateTransition(new Action(priceAction, true, false), 
						new State(3, price.getUpperValue(), availability, inventory), 0.1));
				nextUpperStates.add(new StateTransition(new Action(priceAction, true, false), 
						new State(4, price.getUpperValue(), availability, inventory), 0.05));
			}
		}
		else if(demand == 5){
			if(price.getId() > 0){
				priceAction = -1; //Lower price
				nextLowerStates.add(new StateTransition(new Action(priceAction, true, false), 
						new State(4, price.getLowerValue(), availability, inventory), 0.35));
				nextLowerStates.add(new StateTransition(new Action(priceAction, true, false), 
						new State(5, price.getLowerValue(), availability, inventory), 0.35));
				nextLowerStates.add(new StateTransition(new Action(priceAction, true, false), 
						new State(6, price.getLowerValue(), availability, inventory), 0.15));
				nextLowerStates.add(new StateTransition(new Action(priceAction, true, false), 
						new State(7, price.getLowerValue(), availability, inventory), 0.1));
				nextLowerStates.add(new StateTransition(new Action(priceAction, true, true), 
						new State(8, price.getLowerValue(), availability, inventory), 0.05));
			}
			priceAction = 0; //Keep price
			nextStates.add(new StateTransition(new Action(priceAction, true, false), 
					new State(3, price, availability, inventory), 0.25));
			nextStates.add(new StateTransition(new Action(priceAction, true, false), 
					new State(4, price, availability, inventory), 0.25));
			nextStates.add(new StateTransition(new Action(priceAction, true, false), 
					new State(5, price, availability, inventory), 0.3));
			nextStates.add(new StateTransition(new Action(priceAction, true, false), 
					new State(6, price, availability, inventory), 0.2));
			if(price.getId() < 9){
				priceAction = 1; //Raise price
				nextUpperStates.add(new StateTransition(new Action(priceAction, true, false), 
						new State(1, price.getUpperValue(), availability, inventory), 0.35));
				nextUpperStates.add(new StateTransition(new Action(priceAction, true, false), 
						new State(2, price.getUpperValue(), availability, inventory), 0.3));
				nextUpperStates.add(new StateTransition(new Action(priceAction, true, false), 
						new State(3, price.getUpperValue(), availability, inventory), 0.2));
				nextUpperStates.add(new StateTransition(new Action(priceAction, true, false), 
						new State(4, price.getUpperValue(), availability, inventory), 0.1));
				nextUpperStates.add(new StateTransition(new Action(priceAction, true, false), 
						new State(5, price.getUpperValue(), availability, inventory), 0.05));
			}
		}
		else if(demand == 6){
			if(price.getId() > 0){
				priceAction = -1; //Lower price
				nextLowerStates.add(new StateTransition(new Action(priceAction, true, false), 
						new State(5, price.getLowerValue(), availability, inventory), 0.35));
				nextLowerStates.add(new StateTransition(new Action(priceAction, true, false), 
						new State(6, price.getLowerValue(), availability, inventory), 0.35));
				nextLowerStates.add(new StateTransition(new Action(priceAction, true, false), 
						new State(7, price.getLowerValue(), availability, inventory), 0.2));
				nextLowerStates.add(new StateTransition(new Action(priceAction, true, true), 
						new State(8, price.getLowerValue(), availability, inventory), 0.1));
			}
			priceAction = 0; //Keep price
			nextStates.add(new StateTransition(new Action(priceAction, true, false), 
					new State(4, price, availability, inventory), 0.3));
			nextStates.add(new StateTransition(new Action(priceAction, true, false), 
					new State(5, price, availability, inventory), 0.35));
			nextStates.add(new StateTransition(new Action(priceAction, true, false), 
					new State(6, price, availability, inventory), 0.2));
			nextStates.add(new StateTransition(new Action(priceAction, true, false), 
					new State(7, price, availability, inventory), 0.15));
			if(price.getId() < 9){
				priceAction = 1; //Raise price
				nextUpperStates.add(new StateTransition(new Action(priceAction, true, false), 
						new State(2, price.getUpperValue(), availability, inventory), 0.25));
				nextUpperStates.add(new StateTransition(new Action(priceAction, true, false), 
						new State(3, price.getUpperValue(), availability, inventory), 0.25));
				nextUpperStates.add(new StateTransition(new Action(priceAction, true, false), 
						new State(4, price.getUpperValue(), availability, inventory), 0.3));
				nextUpperStates.add(new StateTransition(new Action(priceAction, true, false), 
						new State(5, price.getUpperValue(), availability, inventory), 0.15));
				nextUpperStates.add(new StateTransition(new Action(priceAction, true, false), 
						new State(6, price.getUpperValue(), availability, inventory), 0.05));
			}
		}
		else if(demand == 7){
			if(price.getId() > 0){
				priceAction = -1; //Lower price
				nextLowerStates.add(new StateTransition(new Action(priceAction, true, false), 
						new State(6, price.getLowerValue(), availability, inventory), 0.3));
				nextLowerStates.add(new StateTransition(new Action(priceAction, true, false), 
						new State(7, price.getLowerValue(), availability, inventory), 0.35));
				nextLowerStates.add(new StateTransition(new Action(priceAction, true, true), 
						new State(8, price.getLowerValue(), availability, inventory), 0.25));
				nextLowerStates.add(new StateTransition(new Action(priceAction, true, true), 
						new State(9, price.getLowerValue(), availability, inventory), 0.1));
			}
			priceAction = 0; //Keep price
			nextStates.add(new StateTransition(new Action(priceAction, true, false), 
					new State(5, price, availability, inventory), 0.4));
			nextStates.add(new StateTransition(new Action(priceAction, true, false), 
					new State(6, price, availability, inventory), 0.35));
			nextStates.add(new StateTransition(new Action(priceAction, true, false), 
					new State(7, price, availability, inventory), 0.25));
			if(price.getId() < 9){
				priceAction = 1; //Raise price
				nextUpperStates.add(new StateTransition(new Action(priceAction, true, false), 
						new State(3, price.getUpperValue(), availability, inventory), 0.3));
				nextUpperStates.add(new StateTransition(new Action(priceAction, true, false), 
						new State(4, price.getUpperValue(), availability, inventory), 0.3));
				nextUpperStates.add(new StateTransition(new Action(priceAction, true, false), 
						new State(5, price.getUpperValue(), availability, inventory), 0.25));
				nextUpperStates.add(new StateTransition(new Action(priceAction, true, false), 
						new State(6, price.getUpperValue(), availability, inventory), 0.15));
			}
		}
		else if(demand == 8){
			if(price.getId() > 0){
				priceAction = -1; //Lower price
				nextLowerStates.add(new StateTransition(new Action(priceAction, true, false), 
						new State(7, price.getLowerValue(), availability, inventory), 0.55));
				nextLowerStates.add(new StateTransition(new Action(priceAction, true, true), 
						new State(8, price.getLowerValue(), availability, inventory), 0.3));
				nextLowerStates.add(new StateTransition(new Action(priceAction, true, true), 
						new State(9, price.getLowerValue(), availability, inventory), 0.15));
			}
			priceAction = 0; //Keep price
			nextStates.add(new StateTransition(new Action(priceAction, true, false), 
					new State(6, price, availability, inventory), 0.4));
			nextStates.add(new StateTransition(new Action(priceAction, true, false), 
					new State(7, price, availability, inventory), 0.35));
			nextStates.add(new StateTransition(new Action(priceAction, true, true), 
					new State(8, price, availability, inventory), 0.25));
			if(price.getId() < 9){
				priceAction = 1; //Raise price
				nextUpperStates.add(new StateTransition(new Action(priceAction, true, false), 
						new State(4, price.getUpperValue(), availability, inventory), 0.4));
				nextUpperStates.add(new StateTransition(new Action(priceAction, true, false), 
						new State(5, price.getUpperValue(), availability, inventory), 0.3));
				nextUpperStates.add(new StateTransition(new Action(priceAction, true, false), 
						new State(6, price.getUpperValue(), availability, inventory), 0.2));
				nextUpperStates.add(new StateTransition(new Action(priceAction, true, false), 
						new State(7, price.getUpperValue(), availability, inventory), 0.1));
			}
		}
		else if(demand == 9){
			priceAction = 0; //Keep price
			nextStates.add(new StateTransition(new Action(priceAction, true, false), 
					new State(7, price, availability, inventory), 0.8));
			nextStates.add(new StateTransition(new Action(priceAction, true, true), 
					new State(8, price, availability, inventory), 0.2));
			if(price.getId() < 9){
				priceAction = 1; //Raise price
				nextUpperStates.add(new StateTransition(new Action(priceAction, true, false), 
						new State(5, price.getUpperValue(), availability, inventory), 0.7));
				nextUpperStates.add(new StateTransition(new Action(priceAction, true, false), 
						new State(6, price.getUpperValue(), availability, inventory), 0.25));
				nextUpperStates.add(new StateTransition(new Action(priceAction, true, false), 
						new State(7, price.getUpperValue(), availability, inventory), 0.05));
			}
		}
	}
	
}
