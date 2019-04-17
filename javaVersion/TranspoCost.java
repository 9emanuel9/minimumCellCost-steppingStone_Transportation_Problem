public class TranspoCost  {
	private boolean isNull=true;//wether the class has been initialised
	private boolean isAssigned=false;
	private int price;
	private int amountAssigned=0;
	private boolean flagged=false;


	public TranspoCost (int price){
		this.price=price;
		isNull=false;
	} 
	public void setAmountAssigned (int amount){
		amountAssigned=amount;
		isAssigned=true;
	}
	public int getAmountAssigned (){
		return amountAssigned;
	}
	public void setIsAssigned (boolean bol){
		isAssigned=bol;
	}
	public boolean getIsAssigned (){
		return isAssigned;
	}
	
	public void setPrice (int price){ 
		this.price=price; 
	}
	public void flagIt (){
		flagged=true;
	}
	public boolean isFlagged (){
		return flagged;
	}
	
	public int getPrice (){ 
		return price; 
	}
	public boolean isEmptyCell (){
		return !(amountAssigned>0);
	}
	public boolean isNull (){
		return isNull;
	}
	public String toString (){
		return "$ "+price+"("+amountAssigned+")";
	}
}