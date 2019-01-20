
public class StateTransition {
	final private Action action;
	final private State nextState;
	final private double probability;
	
	public StateTransition(Action action, State nextState, double probability){
		this.action = action;
		this.nextState = nextState;
		this.probability = probability;
	}

	public Action getAction() {
		return action;
	}

	public State getNextState() {
		return nextState;
	}

	public double getProbability() {
		return probability;
	}

}
