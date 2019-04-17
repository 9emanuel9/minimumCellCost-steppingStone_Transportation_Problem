public class Warehouse {

	private String name;
	private int demand;
	private int receivingAmount=0;
	public Warehouse (String name, int demand){
		this.name=name;
		this.demand=demand;
	}
	public void addReceivingAmount (int amount){
		if (amount<0){
			throw new IllegalArgumentException ("amount can not be negative");
		}
		if (receivingAmount+amount >demand){
			throw new IllegalArgumentException ("warehouse Full");
		}
		receivingAmount=receivingAmount + amount;

	}
	public int getReceivingAmount (){
		return receivingAmount;		
	}
	public void setDemand (int d){
		demand=d;
	}
	public void setName (String name){
		this.name=name;
	} 
	
	public String getName (){
		return name;
	}
	public int getDemand (){
		return demand;
	}
	public boolean isFull (){
		return receivingAmount==demand;
	}
	
	public String toString (){
		return " Warehouse("+name +" : "+demand+" : "+"RAmount"+receivingAmount+")";
	}	
}