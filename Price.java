
public class Price {
	final private int value;
	final private int id;
	
	public Price(int i){
		if(i <= 0){
			value = 15087;
		}
		else if(i == 1){
			value = 15389;
		}
		else if(i == 2){
			value = 15697;
		}
		else if(i == 3){
			value = 16011;
		}
		else if(i == 4){
			value = 16331;
		}
		else if(i == 5){
			value = 16658;
		}
		else if(i == 6){
			value = 16991;
		}
		else if(i == 7){
			value = 17331;
		}
		else if(i == 8){
			value = 17678;
		}
		else if(i == 9){
			value = 18032;
		}
		else{
			value = 18032;
		}
		id = i;
	}

	public int getValue() {
		return value;
	}
	
	public int getId() {
		return id;
	}
	
	public Price getLowerValue(){
		return new Price(id-1);
	}
	
	public Price getUpperValue(){
		return new Price(id+1);
	}

}
