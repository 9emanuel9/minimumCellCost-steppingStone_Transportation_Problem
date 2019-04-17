package main



import (
    "fmt"
    "os"
    "log"
    "bufio"
    "strings"
    "strconv"
    "runtime"
    "sync"
    "errors"
    
)
var factories [] Factory
var warehouses [] Warehouse
var model [][]TranspoCost

type Stack struct {
     lock sync.Mutex // you don't have to do this if you don't want thread safety
     s []*TranspoCost
     head *TranspoCost
     tail *TranspoCost
}

type Factory struct{
	name string
	production int
	amountSent int
}
type Warehouse struct{
	name string
	demand int
	receivingAmount int 
}
type TranspoCost struct{
	price int 
	amountAssigned int
	flagged bool //nill of bool is false
	isAssigned bool
	visited bool//taking care of closed path scenario

}

func main() {
	var fromFile [][] string
	var fileName string
	fmt.Println ("Please enter the file name: ")
	fmt.Scanln(&fileName)
    file, err := os.Open(fileName)
    if err != nil {
        log.Fatal(err)
    }
    defer file.Close()

    f, err :=os.Create ("solution.txt")//creating file to write the solution in given the name "solution.txt"
    if err != nil {					  //if file already exists it overwrites it
        fmt.Println(err)
                f.Close()
        return
    }
    defer f.Close ()				//closes the solution.txt file before the main() finnished execution 

    scanner := bufio.NewScanner(file)     //setting up scanner to read from user input given file 
    var str string
    for scanner.Scan() {             // internally, it advances token based on sperator
        str =scanner.Text() // token in unicode-char
        fromFile=append (fromFile, strings.Split(str," "))//reads line by line and puts it into a list. each line is a string
    }
    fmt.Println ("*****************BEFORE*****************")
    factories=extractFactory(fromFile)//extracting all factory information from file and instanciating the global factories var
    warehouses=extractWarehouse (fromFile)//extracting all warehouse information from file and instanciating the global warehouses var
    model=extractTranspoCost(fromFile)//extracting all TranspoCost information from file and instanciating the global model var
    minimumCellCost ()//evenly distributing the production amount to every warehouse using the minimumCell cost algorithm
    if isDegenrassyCase (){
    	printFactories ()
    	printWarehouses ()
    	printModel ()
    	printModelToFile (f,"******Before******")
    	fmt.Fprintln (f,"DEGENERASSY CASE!! COULD NOT DO SKIPPING STONE ALGORITHM BECAUSE ROW+COLUMN-1 != NUMBER OF OCCUPIED CELLS")
    	log.Fatal ("DEGENERASSY CASE!! COULD NOT DO SKIPPING STONE ALGORITHM BECAUSE ROW+COLUMN-1 != NUMBER OF OCCUPIED CELLS")

    }
    printFactories ()
    printWarehouses ()
    printModel ()
    printModelToFile (f,"******Before******")//writing the model to the "solution.txt" file
    fmt.Println ("\n\n","*****************AFTER*****************")
    assignSolution ()
    printFactories ()
    printWarehouses ()
    printModel ()
    printModelToFile (f,"******After******")




}

/*********************************STACK METHODS**********************************************/

func NewStack() *Stack { return &Stack {lock:sync.Mutex{},s:make([] *TranspoCost,0) } }

func (s *Stack) Push(v *TranspoCost) {
    s.lock.Lock()
    defer s.lock.Unlock()

    s.s = append(s.s, v)
    s.head=s.s[0]
    s.tail=s.s[len(s.s)-1]
}

func (s *Stack) Pop() (*TranspoCost, error) {
    s.lock.Lock()
    defer s.lock.Unlock()
    l := len(s.s)
    if l == 0 {
        return &TranspoCost{}, errors.New("Empty Stack")
    }

    res := s.s[l-1]
    s.s = s.s[:l-1]
    s.tail=s.s[len(s.s)-1]
    return res, nil
}

func (s *Stack) Peek()(*TranspoCost,error){
    s.lock.Lock()
    defer s.lock.Unlock()
    if s.isEmpty(){
    	return &TranspoCost{}, errors.New("Empty Stack")
    }
    return s.tail,nil
    
}

func (s *Stack)isEmpty ()(bool){
	return len(s.s)==0
}

func (stack *Stack)Print(){
	fmt.Println ("*********Printing Stack********")
	st:=stack.s
	for i:=0;i<len(st);i++{
		fmt.Println (*st[i])
	}
}

func (stack *Stack) reasignVisitsInStack(){
	for i:=0;i<len(stack.s);i++{
		stack.s[i].visited=false
	}
}

func (st *Stack)marginalCost ()(mc int){
	count :=1
	for i:=0;i<len(st.s);i++{
		if count%2!=0{
			mc=mc+st.s[i].price
			count++
		}else{
			mc=mc-st.s[i].price
			count++
		}
	}
	return mc
}

func (st *Stack)largestTranspoCost ()(int){
	lg:=st.s[1]
	for i:=2;i<len (st.s);i++{
		if st.s[i].price>=lg.price{//if the selected cell has a transpoCost greater than or equal to the "largest" go into the if statment
			if st.s[i].price==lg.price {//in here we check are they equal. if not equal goes to else statment because we know it can be either greater or equal
				if st.s[i].amountAssigned<lg.amountAssigned{//another condition that needs to be verified is that when they are equal we take the one who has the smallest amount assigned
					lg=st.s[i]                                 //we make the "largest" equal to the selected cell when if they are equal the condition must be that the amount assigned is to be less than the largest
				}
			}else{	//if it comes in here we know that the selected cell has a transpo cost bigger than the "largest" and therefore make the new selected cell the largest
				lg=st.s[i]
			}

		}
	}
	return lg.amountAssigned
}

/*******************************FACTORY METHODS**********************************************/
func (f *Factory) getName()(string){  return f.name }

func (f *Factory) getProduction ()(int){ return f.production }

func (f *Factory) getAmountSent()(int){ return f.amountSent }

func (f *Factory) allProdSent ()(bool){ return f.production==f.amountSent }

func (f *Factory) incAmountSent (amount int){
	if (amount<0){
		_, fn, line, _ := runtime.Caller(1)
		log.Fatal("amount cannot be less than 0 ",fn," line: ",line)
	}
	if (amount+f.amountSent>f.production){
		_, fn, line, _ := runtime.Caller(1)
		log.Fatal("amount + amount Sent can not be greater than production ",fn," line: ",line)
	}
	f.amountSent=amount+f.amountSent
}
/*******************************WAREHOUSE METHODS**********************************************/
func (w *Warehouse)getName()(string){ return w.name }

func (w *Warehouse)getDemand()(int){ return w.demand }

func (w *Warehouse)getReceivingAmount()(int){ return w.receivingAmount }

func (w *Warehouse)isFull()(bool){ return w.receivingAmount==w.demand }

func (w *Warehouse)addReceivingAmount(amount int){
	if (amount<0){
		_, fn, line, _ := runtime.Caller(1)
		log.Fatal("receiving amount cannot be less than 0 ",fn," line: ",line)
	}
	if (amount+w.receivingAmount>w.demand){
		_, fn, line, _ := runtime.Caller(1)
		log.Fatal("receiving amount + amount cannot be greater than demand ",fn," line: ",line)
	}
	w.receivingAmount=amount+w.receivingAmount
}

/*******************************TRANSPOCOST METHODS**********************************************/

func (tc *TranspoCost)setIsAssigned (b bool){ tc.isAssigned=b }

func (tc *TranspoCost)setAmountAssigned (amount int){
	tc.amountAssigned=amount
	tc.setIsAssigned (true)
}

func (tc *TranspoCost) setPrice (p int){
	if (p<0){
		_, fn, line, _ := runtime.Caller(1)
		log.Fatal("Price cannot be less than 0 ",fn," line: ",line)
	}
	tc.price=p
}

func (tc *TranspoCost)beenVisited ()(bool){return tc.visited==true}

func (tc *TranspoCost)flagIt(){ tc.flagged=true }

func (tc *TranspoCost)isFlagged() (bool) {return tc.flagged}

func (tc *TranspoCost)getPrice ()(int){ return tc.price}

func (tc *TranspoCost)getAmountAssigned()(int){ return tc.amountAssigned }

func (tc *TranspoCost)isEmptyCell ()(bool){return !(tc.amountAssigned>0)}

/*******************************MINIMUM COST ALGORITHM METHODS********************************************/
func isCostValidAt(i,j int) (bool){
	return !(factories [i].allProdSent ())&&!(warehouses[j].isFull ());
}

func seekingValidCandid()(int, int ,bool){
	for i :=0; i <len(factories);i++{
		for j:=0; j<len(warehouses);j++{
			if (isCostValidAt (i,j)){
				return i,j,true
			}
		}
	}
	return -1,-1,false//returns this when all candidates are no longer valid
}
//if returns -1,-1,false means no more candidates available
func findLowestCost ()(int,int,bool){
	lowestI,lowestJ,val:=seekingValidCandid ()
	if (val==false){
		return lowestI,lowestJ,val
	}
	for i:=0;i<len(factories);i++{
		for j:=0;j<len(warehouses);j++{
			if (model[i][j].getPrice()<model[lowestI][lowestJ].getPrice()&&isCostValidAt(i,j)){
				lowestI=i
				lowestJ=j
			}
		}
	}
	return lowestI,lowestJ,val
}

func minimumCellCost (){
	for !allFactoryProdSent()||!allWarehouseFull(){
		i,j,found :=findLowestCost ()
		if (found==false){
			fmt.Println ("break")
			break//algorithm cannot find anymore lowest
		}
		fac:=factories[i].getProduction()-factories[i].getAmountSent ()
		war:=warehouses[j].getDemand()-warehouses[j].getReceivingAmount ()
		if fac<=war{
			warehouses[j].receivingAmount=warehouses[j].receivingAmount+fac
			factories[i].amountSent=factories[i].amountSent+fac
			model[i][j].setAmountAssigned(fac)

		}else{
			warehouses[j].receivingAmount=warehouses[j].receivingAmount+war
			factories[i].amountSent=factories[i].amountSent+war
			model[i][j].setAmountAssigned(war)
		}
	}
}
/***********************************Stepping Stone Algorithm methods*********************************************/
func indexOf (tc *TranspoCost) (int,int,bool){
	for i:=0;i<len(factories);i++{
		for j:=0;j<len(warehouses);j++{
			if &model[i][j]==tc{
				return i,j,true
			}
		}
	}
	return -1,-1,false
}
func findEmptyCell()(*TranspoCost, bool){
	for i:=0;i<len(factories);i++{
		for j:=0;j<len(warehouses);j++{
			if model[i][j].isEmptyCell () && model[i][j].isFlagged ()==false{
				return &model[i][j],true
			}
		}
	}
	return &TranspoCost{},false
}
func allEmptyCellsFlagged ()(bool){
	for i:=0;i<len(factories);i++{
		for j:=0;j<len(warehouses);j++{
			if model[i][j].isEmptyCell ()&&!model[i][j].isFlagged (){
				return false
			}
		}
	}
	return true
}
/*
goes through the row specified by incrimenting j and returns the first non emtpy cell encountered
SideNotes:
		-always goes to the right of the specified i,j Cell and wraps around till it reaches the initial cell
		-if the entire row consists of empty cells than returns TranspoCost {false,0,0,false,false},false 
		-if non empty cell is encountered it returns it and true
*/
func rowTraversal (i,j int ) (*TranspoCost,bool){
	count:=(j+1)%len(warehouses)
	initial :=&model[i][j]
	crawler :=&model[i][count]
	for (crawler!=initial){
		if !crawler.isEmptyCell ()&&!crawler.beenVisited (){
			return crawler,true
		}
		count=(count+1)%len(warehouses)
		crawler=&model[i][count]
	}
	return initial,false
}
/*
goes through the column specified by incrimenting i and returns the first non emtpy cell encountered
SideNotes:
		-always goes to the bottom of the specified i,j Cell and wraps around till it reaches the initial cell
		-if the entire column consists of empty cells than returns TranspoCost {false,0,0,false,false},false 
		-if non empty cell is encountered it returns it and true
*/
func columnTraversal (i,j int ) (*TranspoCost,bool){
	count:=(i+1)%len(factories)
	initial :=&model[i][j]
	crawler :=&model[count][j]
	for (crawler!=initial){
		if !crawler.isEmptyCell ()&&!crawler.beenVisited (){
			return crawler,true
		}
		count=(count+1)%len(factories)
		crawler=&model[count][j]
	}
	return initial,false
}
/*recevies a cell reference and alternates between row and column traversal until it reaches its the initial cell provided in the argument
	1. if returns false means the cell did not have enough non empty cells to properly alternate between rows and columns to succesfully make its way back to the initial cell
	2. if returns stack and true means the stepping stone works for the cell and the alternation was successful 
	note: when false is returned most case the stack is just one elem in it that elem being the initial cell*/
func closedPath(emptyCell *TranspoCost)(*Stack,bool){
	visited:=NewStack ()			//this stack will carry all the cells that have been visited but got popped 
	stack:=NewStack ()				//initialize stack that will contain all the cells that lead up to the closed path
	emptyCell.setAmountAssigned (1) //we set amount to 1 because the rowTraversal method only looks for "non empty cells"(we change it back to 0 later)
	count:=1
	stack.Push (emptyCell)//we push initial cell into stack
	for !stack.isEmpty (){ //loop while stack is not empty 
		if (count%2!=0){				//check wether the count is odd or even if odd it does row traversal
			p,_:=stack.Peek ()			
			peekI,peekJ,_:=indexOf(p)//we know the element that goes in or out of the stack exists in the model so we dont care about the bool return in indexOf method
			crawler,found :=rowTraversal(peekI,peekJ)   //if odd we do rowTraversal of whatever is on the top of the stack
			crawler.visited=true						//keeping track of cells we have visited so we dont visit them again and dont go into a infitinte loop (comes in handy incase a traversal cannot find any non empty cells)we will remove and change all cells visited value back to false
			if (crawler==emptyCell){					//the "only" condition that breaks the loop regardless if the skipping stone solution works on the cell or not it will eventually come back to the initial cell-
				break   								//-once it comes back to the initial cell we break the loop and know algorithm is finnised
			}
			if found==false{							//if found is false means we did not find a non empty cell that has not been visited in the row
				temp,_:=stack.Pop ()							//since it does not have any further solutions its is not required in the stepping stone solution and therefore must be removed from stack and now go back a step
				visited.Push (temp)
				count--									//we decrement count cause algorthim goes back a step(backtrack)
				continue								//continue the loop
			}
			stack.Push (crawler)						//if there is non empty cell that has not been visited in the cell than we have reached a new solution and push it to stack and next step will do column traversal of the cell since it is now top of stack  
			count++										// increment count so now we know it will be column traversal
			continue 									//next iteration of loop
		}
		if (count%2==0){              //check wether the count is odd or even if even it does column traversal 
			p,_:=stack.Peek ()
			peekI,peekJ,_:=indexOf(p) //we know the element that goes in or out of the stack exists in the model so we dont care about the bool return in indexOf method
			crawler,found:=columnTraversal(peekI,peekJ) //if even we do rcolumnTraversal of whatever is on the top of the stack
			crawler.visited=true 						//keeping track of cells we have visited so we dont visit them again and dont go into a infitinte loop (comes in handy incase a traversal cannot find any non empty cells)we will remove and change all cells visited value back to false
			if (crawler==emptyCell){					//the "only" condition that breaks the loop regardless if the skipping stone solution works on the cell or not it will eventually come back to the initial cell-
				break 			                        //-once it comes back to the initial cell we break the loop and know algorithm is finnised						
			}
			if found==false{							//if found is false means we did not find a non empty cell that has not been visited in the column
				temp,_:=stack.Pop ()							//since it does not have any further solutions its is not required in the stepping stone solution and therefore must be removed from stack and now go back a step
				visited.Push (temp)
				count--									//we decrement count cause algorthim goes back a step(backtrack)
				continue
			}
			stack.Push (crawler)						//if there is non empty cell that has not been visited in the cell than we have reached a new solution and push it to stack and next step will do row traversal of the cell since it is now top of stack  
			count++										//increment count so now we know it will be row traversal
			continue 
		}
	}
	emptyCell.setAmountAssigned (0) //setting emptyCell amount back to 0 for other skipping stone iterations when doing the maarginal cost
	emptyCell.flagged=true			//setting the var flagged to true so that we dont do the skipping stone algorithm on the cell again(keeping track)
	stack.reasignVisitsInStack ()	//setting back false to all the visited var for every cell visited and is taken into account, so that it does not cause complications for other empty cells
	visited.reasignVisitsInStack ()	//setting back false to all the visited var but were not being taken into account
	if len(stack.s)<=1{				//if closedPath returns false means that it did not find a closedPath(reason why its <1 is because if u do a case where it cant find a closed path the end result will always leave out to be the initial cell) 
		return stack,false
	}
	return stack,true				//if closed Path returns true means we have a closedPath and the stack is returned containg the cells that make up the closedPath
}

func marginalCost (cell *TranspoCost,mcChan chan int ,mcStackChan chan *Stack){
	stack,_:=closedPath (cell)
	mc:=stack.marginalCost ()
	mcStackChan<-stack
	mcChan<-mc

}


func skippingStone ()(lowestMCStack *Stack, lowestMc int){
	mcChan:=make (chan int ,1)
	mcStackChan:=make (chan *Stack, 1)
	for (!allEmptyCellsFlagged ()){
		emptycell,_:=findEmptyCell ()
		go marginalCost (emptycell,mcChan,mcStackChan)
		stack:=<-mcStackChan
		mc:=<-mcChan
		if mc<lowestMc{
			lowestMc=mc
			lowestMCStack=stack
		}
	}
	return
}

func assignSolution (){
	sMarginalCost,_:= skippingStone()	//stack of lowest marginal cost, lowest marginal cost
	if sMarginalCost==nil {//if the stack is nil it means we dont have a more optimal solution the solution we currently have is the most optimal
		return
	}
	amount:=sMarginalCost.largestTranspoCost ()
	count:=1
	
	for i:=0;i<len(sMarginalCost.s);i++{
		if count%2!=0{	
			elem:=sMarginalCost.s[i]
			elem.amountAssigned=elem.amountAssigned+amount
			count++
		}else{
			elem:=sMarginalCost.s[i]
			elem.amountAssigned=elem.amountAssigned-amount
			count++
		}
	}
}

/*******************************EXTRACT METHODS********************************************/

func extractFactory (file [][] string )([]Factory){
	var fac [] Factory
	for i:=1;i<len(file)-1;i++{
		prod,_:=strconv.Atoi(file[i][len(file[i])-1])
		fac=append(fac,Factory{name:file[i][0],production:prod})
	}
	return fac
}

func extractWarehouse (file [][] string)(war [] Warehouse){
	temp:=file[0]//first row
	tempL:=file[len(file)-1]//final row which contains the demand amount
	for i:=1;i<len(temp)-1;i++{
		war=append(war,Warehouse {name:temp[i]})

	}
	count:=0
	for i:=1;i<len(tempL);i++{
		dem,_:=strconv.Atoi(tempL[i])
		war[count].demand=dem
		count++
	}
	return
}

func extractTranspoCost (file [][] string)(m [][]TranspoCost){
	for i:=1;i<len(file)-1;i++{
		var tc [] TranspoCost
		for j:=1;j<len(file[i])-1;j++{
			cost,_:=strconv.Atoi (file[i][j])
			tc=append(tc, TranspoCost{price:cost})
		}
		m=append(m,tc)
	}
	return
}


/*******************************PACKAGE METHODS********************************************/
func isDegenrassyCase ()(bool){ return (len(factories)+len(warehouses))-1!=numberOfNonEmptyCells () }

func numberOfNonEmptyCells ()(int){
	count:=0;
	for i:=0; i<len(factories);i++{
		for j:=0;j<len(warehouses);j++{
			if (!model[i][j].isEmptyCell ()){
				count++;
			}
		}
	}
	return count;
}

func printWarehouses (){
	fmt.Println ("Warehouses:","\n",warehouses)
}

func printFactories (){
	fmt.Println ("Factories:","\n",factories)
}

func printModel (){
	fmt.Println("Model:")
	for i:=0;i<len(model);i++{
		for j:=0;j<len(model[i]);j++{
			fmt.Print(model[i][j].getPrice(),"(",model[i][j].getAmountAssigned(),")"," ")
		}
		fmt.Println ()
	}
}
func allFactoryProdSent ()(bool){
	for i:=0;i<len(factories);i++{
		if factories[i].getProduction()!=factories[i].getAmountSent (){
			return false
		}
	}
	return true
}
func allWarehouseFull ()(bool){
	for i:=0;i<len(warehouses);i++{
		if warehouses[i].getDemand ()!=warehouses[i].getReceivingAmount(){
			return false
		}
	}
	return true
}

func printModelToFile (f *os.File ,s string){
	fmt.Fprintln (f,s)
	fmt.Fprint (f,"COSTS ")
    for i:=0;i<len(warehouses);i++{
    	fmt.Fprint (f,warehouses[i].name," ")
    }
    fmt.Fprint (f,"SUPPLY")
    fmt.Fprintln (f,)
    for i:=0;i<len (factories);i++{
    	fmt.Fprint (f,factories[i].name," ")
	    for j:=0;j<len(warehouses);j++{
	    		fmt.Fprint (f,"(",model[i][j].amountAssigned,")",model[i][j].price," ")
	    }
	    fmt.Fprint (f,factories[i].production)
	    fmt.Fprintln (f,)
	}
	fmt.Fprint (f,"DEMAND ")
    for i:=0;i<len(warehouses);i++{
    	fmt.Fprint (f,warehouses[i].demand," ")
    }
    fmt.Fprintln (f,"\n\n")

}
//10by10_inputdata.txt
//3by3_inputdata.txt
//3by4_inputdata.txt
//3by3_other_inputdata.txt
//3by3_inputdataexample.txt



















