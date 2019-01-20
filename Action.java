
public class Action {
	
	final private int price;
	final private boolean market;
	final private boolean supplier;
	
	public Action(int priceChange, boolean purchaseMarket, boolean purchaseSupplier){
		price = priceChange;
		market = purchaseMarket;
		supplier = purchaseSupplier;
	}
	public int getPriceAction() {
		return price;
	}
	
	public boolean getMarketAction() {
		return market;
	}
	
	public boolean getSupplierAction() {
		return supplier;
	}

}
