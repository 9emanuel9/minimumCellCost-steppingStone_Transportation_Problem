import java.util.ArrayList;
public class Main {
	private ArrayList<String> fileString;
	private ArrayList <String> warehouseNames;
	private ArrayList <String> factoryNames;
	private ArrayList  demand;
	private ArrayList production;
	private Transportation transport;
	private Factory [] factories;
	private Warehouse [] warehouses;

//******************************IMPORTANT METHOD: ALL INFORMATION IS EXTRACTED IN THIS METHOD USING THE FileHelper class******************************************************************************************************************************************************************************
	
	public void printTransportModel(){
		System.out.println (transport);
	}

	private Factory [] makeFactories (ArrayList <String> factoryNames, ArrayList production ){
		if (factoryNames.size ()!=production.size ()){
			throw new IllegalArgumentException ("Factory name collection and production collection are not same length");
		}
		Factory [] factories= new Factory [factoryNames.size ()];
		for (int i=0 ; i<factories.length ; i ++ ){
			factories[i]=new Factory (factoryNames.get (i),(int)production.get (i)); 
		}
		return factories;
	}
	private Warehouse [] makeWarehouses (ArrayList <String>warehouseNames, ArrayList demand ){
		if (warehouseNames.size ()!=demand.size ()){
			throw new IllegalArgumentException ("Warehouse name collection and demand collection are not same length");
		}
		Warehouse [] warehouses = new Warehouse [warehouseNames.size ()];
		for (int i =0; i <warehouses.length;i ++){
			warehouses[i]=new Warehouse (warehouseNames.get (i), (int)demand.get (i));
		}
		return warehouses;
	}


	public void findOptimalTransport (String filename){
		//note: we will be extracting all key information from the input file according to the file formats
		fileString=FileHelper.readFromFile (filename);//doc is stored in a arraylist of strings
		warehouseNames=FileHelper.extractWarehouseNames (fileString);	//warehouse names being extracted
		factoryNames=FileHelper.extractFactoryNames (fileString);	   //factory names being extracted 
		demand=FileHelper.extractDemand (fileString);				  //demand numbers being extracted
		production=FileHelper.extractProduction (fileString);		 //production numbers being extracted
		transport=FileHelper.extractTranspoCosts (fileString);		//extracting transportation cost numbers 
		factories=makeFactories (factoryNames, production);		   //populating factories array through the extracted factory names and production numbers Factory(string name, int production)
		warehouses=makeWarehouses (warehouseNames, demand);		  //populating warehouses array through the extracted warehouse names and demand numbers Warehouse (string name, int demand)
		transport.addFactoryCollection (factories);
		transport.addWarehouseCollection (warehouses);
		transport.printWarehouses ();
		transport.printFactories ();
		printTransportModel ();
		System.out.println ("**************************Initialising minimum Cell Cost ****************************");
		transport.minimumCellCost ();
		if (transport.isDegenerassyCase ()){
			throw new RuntimeException ("Degenerassy Case!!!");	
		}
		transport.printWarehouses ();
		transport.printFactories ();
		printTransportModel ();
		System.out.println ("****************Stepping Stone Method being applied to find optimal solution*****************");
		transport.skippingStone ();
		printTransportModel ();
		
	}

	

	public static void main (String [] args){
		Main main=new Main ();
		String filename =args[0].trim ();
		main.findOptimalTransport (filename);
		
	}

}