
public class QValue {
	private State state;
	private int priceAction;
	private final double learningRate;
	private double currentQ;
	
	public QValue(State state, int priceAction, double learningRate, double initQ){
		this.state = state;
		this.priceAction = priceAction;
		this.learningRate = learningRate;
		currentQ = initQ;
	}
	
	public void updateQ(double expectedReward){
		currentQ = (1-learningRate)*currentQ + learningRate*(expectedReward); //How to update Q?????
	}

	public State getState() {
		return state;
	}

	public int getAction() {
		return priceAction;
	}

	public double getQ() {
		return currentQ;
	}
	
}
