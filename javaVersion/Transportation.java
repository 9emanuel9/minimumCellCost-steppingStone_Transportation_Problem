import java.util.Stack;
import java.util.ArrayList;
import java.util.Collections;
public class Transportation {
	private int rowSize;
	private int colSize;
	private TranspoCost [][] model;
	private Warehouse [] warehouses;
	private Factory [] factories;
	
	public Transportation (int row, int col){
		model = new TranspoCost [row][col];
		rowSize=row;
		colSize=col;
	}
	public Transportation	(){
		model=new TranspoCost [3][3];
		rowSize=3;
		colSize=3;	
	}
	public int getRowSize (){
		return rowSize;	
	}
	public int getColumnSize (){
		return colSize;
	}
//*************************************Factory Methods******************************************	
	public void addFactoryCollection (Factory [] factories){
		this.factories=factories;
	}
	public Factory getFactoryAt (int i){
		return factories [i];
	}
	public boolean allProdSentAt (int i ){
		return factories[i].allProdSent ();
	}
	public boolean allFactoryProdSent (){
		for (int i =0;i<factories.length;i++){
			if (allProdSentAt (i)==false){
				return false;
			}
		}
		return true;
	}

	public void printFactories (){
		if (factories==null){
			throw new NullPointerException ("factories is null have not added factories");
		}
		for (int i=0;i<factories.length;i++){
			System.out.println (factories[i]);
		}
		System.out.println ("\n");
	}

//*************************************END of Factory methods******************************************

//*************************************Warehouse Methods******************************************
	public void addWarehouseCollection (Warehouse [] warehouses){
		this.warehouses=warehouses;
	}
	public Warehouse getWarehouseAt (int i){
		return warehouses [i];
	}

	public boolean isWarehouseFullAt (int i){
		return warehouses[i].isFull ();
	}
	public boolean areWarehousesFull (){
		for (int i =0;i<warehouses.length;i++){
			if (isWarehouseFullAt (i)==false){
				return false;
			}
		}
		return true;
	}
	public void printWarehouses (){
		if (warehouses==null){
			throw new NullPointerException ("warehouses is null have not added factories");
		}
		for (int i =0;i <warehouses.length;i++){
			System.out.print (warehouses[i]+"| ");
		}
		System.out.println ("\n");
	}
//*************************************END of warehouse methods******************************************

//***********************************Minimum Cell Cost Method Algorithm methods****************************
	//
	public boolean isCostValidAt (int i, int j){
		return !(factories [i].allProdSent ())&&!(warehouses[j].isFull ());		
	}
	//return first candid that is valid(by valid we mean that its factory is still sending and warehouse is not full)
	//if returns null than we know that all warehouse and factories are full: returns an array with first position representing the i'th index second position representing the j'th position
	public int [] seekingValidCandid (){
		for (int i =0; i <factories.length;i++){
			for (int j=0; j<warehouses.length;j++){
				if (isCostValidAt (i,j)){
					return new int []{i,j};
				}
			}
		}
		return null;
	} 
	//looks for lowest, empty transpocost
	public int [] findLowestCost (){
		int [] lowest=seekingValidCandid ();
		if (lowest==null) {
			return null; //null means we have all of the factories and warehouses full
		}
		for (int i=0;i<factories.length;i++){
			for (int j=0;j<warehouses.length;j++){
				if ((model [i][j].getPrice()<model[lowest[0]][lowest[1]].getPrice ())&&isCostValidAt (i,j)){//checks and sees if the model is the lowest and is not full
					lowest[0]=i;
					lowest[1]=j;
				}

			}
		}
		return lowest;
	}
	//algorithm loops through until all factories 
	public void minimumCellCost (){
		while (!allFactoryProdSent ()&&!areWarehousesFull()){
			int [] index=findLowestCost ();
			int i=index[0];
			int j= index[1];
			Warehouse w= warehouses[j];
			Factory f=factories [i];
			if (w.getDemand ()<=f.getProduction ()){
				if (f.getAmountSent ()==0){
					int temp=w.getDemand ()-w.getReceivingAmount ();
					w.addReceivingAmount (temp);
					f.incAmountSent (temp);
					model [i][j].setAmountAssigned (temp);
				}
				else {
					int temp=f.getProduction ()-f.getAmountSent ();
					try {
						w.addReceivingAmount (temp);
					}catch (IllegalArgumentException e){
						int t=w.getDemand ()-w.getReceivingAmount();
						w.addReceivingAmount (t);
						f.incAmountSent (t);
						model [i][j].setAmountAssigned (t);
						continue;
					}
					f.incAmountSent (temp);
					model [i][j].setAmountAssigned (temp);
				}
			}
			else if (w.getDemand ()>f.getProduction ()){
				int temp=f.getProduction ()-f.getAmountSent ();
				try {
					w.addReceivingAmount (temp);
				}catch (IllegalArgumentException e){
					int t=w.getDemand ()-w.getReceivingAmount();
					w.addReceivingAmount (t);
					f.incAmountSent (t);
					model [i][j].setAmountAssigned (t);
					continue;
				}
				f.incAmountSent (temp);
				model [i][j].setAmountAssigned (temp);
			}
		}

	}
//***********************************End Of Minimum Cell Cost Method Algorithm methods****************************


//***********************************Stepping Stone Algorithm methods*********************************************
	
	//if null that means no more empty cells are available 
	public TranspoCost findEmptyCell (){
		for (int i =0; i<factories.length; i++){
			for (int j =0; j<warehouses.length;j++){
				if (model[i][j].isEmptyCell ()&&model[i][j].isFlagged ()==false){//check if it is flagged and if it empty
					return model [i][j];
				}
			}
		}
		return null;
	}
	public int [] indexOf (TranspoCost t){
		for (int i =0; i<factories.length; i++){
			for (int j =0; j<warehouses.length;j++){
				if (model[i][j]==t){
					return new int [] {i,j};
				}
			}
		}
		return null;
	}
	//if null is returned that means there was only empty cells
	public TranspoCost rowTraversal (int i, int j){
		int count =(j+1)%warehouses.length;
		TranspoCost initial= model[i][j];
		TranspoCost crawler=model [i][count];
		while (crawler!=initial){
			if (!crawler.isEmptyCell ()){
				return crawler;
			}
			count=(count+1)%warehouses.length;
			crawler=model [i][count];
		}
		return null;
	}
	public TranspoCost columnTraversal (int i, int j){
		int count =(i+1)%factories.length;
		TranspoCost initial= model[i][j];
		TranspoCost crawler=model [count][j];
		while (crawler!=initial){
			if (!crawler.isEmptyCell ()){
				return crawler;
			}
			count=(count+1)%factories.length;
			crawler=model [count][j];
		}
		return null;
	}
	public int marginalCost (Stack <TranspoCost> stack){
		int mc=0;
		int count=1;
		stack.pop  ();//to get rid of the one we dont need at the top of stack 
		TranspoCost prev= new TranspoCost (0);
		while (!stack.isEmpty ()){
			if (count%2==0){
				TranspoCost temp=stack.pop ();
				if (prev==temp){//it goes in here incase we ever have a back track; when we have a back track u dont want to add or sub the same number again
					continue;
				}
				prev=temp;
				mc=mc+temp.getPrice();
				count++;
			}
			else{
				TranspoCost temp=stack.pop ();
				if (prev==temp){//it goes in here incase we ever have a back track; when we have a back track u dont want to add or sub the same number again
					continue;
				}
				prev=temp;
				mc=mc-temp.getPrice ();
				count++;
			}
		}
		return mc;
	}
	public TranspoCost lowestNegativeTranspoCost (ArrayList<TranspoCost> al){
		int count =1;
		TranspoCost lowestNeg=al.get (al.size()-2);
		for (int i=al.size ()-2; i>=0;i--){
			if (al.get(i).getAmountAssigned ()<lowestNeg.getAmountAssigned ()){
				lowestNeg=al.get (i);
			}
		}
		return lowestNeg;
	}

	public void assignOptimalSol (Stack <TranspoCost> stack ){
		stack=frequencyMoreThanOne (stack);
		ArrayList <TranspoCost> arr=stackToArrayList (stack);
		TranspoCost lowestNegative=lowestNegativeTranspoCost (arr);
		int amountToUpdate = lowestNegative.getAmountAssigned ();
		int count=1;
		for (int i=arr.size ()-1;i>=0;i--){
			if (count%2==0){
				TranspoCost tc= arr.get (i);
				tc.setAmountAssigned (tc.getAmountAssigned ()-amountToUpdate);
				count ++;
			}
			else {
				TranspoCost tc= arr.get (i);
				tc.setAmountAssigned (tc.getAmountAssigned ()+amountToUpdate);
				count++;
			}
		}
		
	}
	public Stack <TranspoCost> closedPath (TranspoCost emptyCell){
		int count=1;
		emptyCell.setAmountAssigned (1);
		TranspoCost crawler = emptyCell;
		Stack <TranspoCost> stack= new Stack <> ();
		stack.push (crawler);
		int [] cIndex =indexOf(crawler);
		crawler=rowTraversal (cIndex[0],cIndex[1]);
		stack.push (crawler);
		count++;
		while (crawler!=emptyCell){
			if (count%2!=0){
				cIndex=indexOf (crawler);
				crawler=rowTraversal (cIndex [0],cIndex [1]);
				if (crawler==null){
					crawler=stack.pop ();
					count --;
					continue;
				}
				stack.push (crawler);
				count++;

			}
			else if (count%2==0){
				cIndex=indexOf (crawler);
				crawler=columnTraversal (cIndex [0],cIndex [1]);
				if (crawler==null){
					crawler=stack.pop ();
					count --;
					continue;
				}
				stack.push (crawler);
				count++;

			}
		}
		emptyCell.setAmountAssigned (0);
		emptyCell.setIsAssigned (false);
		emptyCell.flagIt ();//flags the transpocost so we know not to pick this as a closed path again
		return stack;
	}
	public void skippingStone (){
		Stack <TranspoCost> t=closedPath (findEmptyCell ());//initial lowest marginal cost
		Stack <TranspoCost> lowestMC=(Stack<TranspoCost>)t.clone ();//creating temp so we can keep track due to reference types
		int lwmc = marginalCost(t);//lowest marginal cost so we dont have to constantly compute again and again to much time consuming
		while (!allEmptyCellsFlagged ()){
			Stack <TranspoCost> stack=closedPath (findEmptyCell ());
			Stack <TranspoCost> temp=(Stack<TranspoCost>)stack.clone ();
			int mc=marginalCost (stack);
			if (mc<lwmc&&lwmc<0&&mc<0){
				lowestMC=temp;
				lwmc=mc;
			}
		}
		assignOptimalSol (lowestMC);
	}

//***********************************End of Stepping Stone Algorithm methods*********************************************

//*****************************************Class Helper methods**********************************************************

	public TranspoCost getCostAt (int i,int j){
		if (!inParameters (i,j)){
			throw new IllegalArgumentException ("i or j are not in the models parameters");
		}
		if (model[i][j].isNull()){
			throw new NullPointerException ("model at i,j has not been initialised");
		}
		return model [i][j];
	}
	
	public int getReceivingAmountAt  (int i) {
		return warehouses [i].getReceivingAmount ();
	}
	//replaces the transpocost if there is one in i j else just adds it there.
	public void addCostAt (int i, int j, TranspoCost transpo){
		if (!inParameters (i,j)){
			throw new IllegalArgumentException ("i or j are not in the models parameters");
		}
		model[i][j]=transpo;
		
	}
	
	public boolean inParameters (int i, int j){
		if (i<0||i>rowSize||j<0||j>colSize){
			return false;
		}
		return true;
	}

	public boolean isDegenerassyCase (){
		return (rowSize+colSize)-1!=numberOfNonEmptyCells();
	}
	public boolean allEmptyCellsFlagged (){
		for (int i =0; i<getRowSize();i++){
			for (int j=0;j<getColumnSize ();j++){
				if (model[i][j].isEmptyCell ()&&!model[i][j].isFlagged ()){
					return false;
				}
			}
		}
		return true;
	}
	public int numberOfNonEmptyCells (){
		int count=0;
		for (int i =0; i<getRowSize();i++){
			for (int j=0;j<getColumnSize ();j++){
				if (!model[i][j].isEmptyCell ()){
					count++;
				}
			}
		}
		return count;
	}
	//if a TranspoCost occurs more than once in the stack remove all instaces of it because it means it didnt find it
	private Stack<TranspoCost> frequencyMoreThanOne (Stack <TranspoCost> s){
		Stack <TranspoCost>temp =(Stack<TranspoCost>)s.clone ();
		TranspoCost t=temp.pop ();// removes top for formatting purposes
		for (int i =0; i<temp.size ();i++){
			int occ= Collections.frequency (temp, temp.get (i));
			if (occ>1){
				temp.removeAll(Collections.singleton(temp.get (i)));
			}
		}
		temp.push (t);
		return temp;

	}
	public ArrayList <TranspoCost> stackToArrayList (Stack <TranspoCost> s){
		s.pop ();//remove the top first cause its useless and dont need it
		ArrayList <TranspoCost> arr= new ArrayList ();
		TranspoCost prev=new TranspoCost (0);
		while (!s.isEmpty ()){
			TranspoCost transpocost=s.pop ();
			if (transpocost==prev){
				continue;
			}
			prev=transpocost;
			arr.add (transpocost);
		}
		return arr;
	}

	public String toString (){
		String s="";
		for (int i=0;i<rowSize;i++){
			for (int j=0;j<colSize;j++){
				s=s+" "+model[i][j];
			}
			s=s+"|\n";
		}
		return s;
	}
}