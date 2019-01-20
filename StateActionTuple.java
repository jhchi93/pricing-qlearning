
public class StateActionTuple {
	private int demand;
	private int price;
	private int action;
	private int count;
	
	StateActionTuple(int demand, int price, int action){
		this.demand = demand;
		this.price = new Price(price).getValue();
		this.action = action;
		count = 0;
	}

	public int getDemand() {
		return demand;
	}

	public int getPrice() {
		return price;
	}

	public int getAction() {
		return action;
	}

	public int getCount() {
		return count;
	}
	
	public void incrementCount() {
		count++;
	}
}
