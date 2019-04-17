public class Factory {
	private String name;
	private int production;
	private int amountSent;
	
	public Factory (String name, int production){
		this.name=name;
		if (production<0){
			throw  new IllegalArgumentException ("Prodcution can not be negative");
		}
		this.production=production;
		amountSent=0;
	}

	public int getAmountSent (){
		return amountSent;
	}
	public void incAmountSent (int amount){
		if (amount <0){
			throw new IllegalArgumentException ("amount cannot be negative");
		}
		if (amount+amountSent>production){
			throw new IllegalArgumentException ("you are sending more than you have. From "+name);

		}
		amountSent=amountSent + amount;
	}
	public boolean allProdSent (){
		return production==amountSent;
	}

	public String getName (){
		return name;
	}
	public int getProduction (){
		return production;
	}

	public void setName (String name){
		this.name=name;
	}
	public void setProduction (int production){

	}
	public String toString (){

		return " Factory[ "+name + " : "+production+" SentAmount"+amountSent+"]"; 
	}
}
