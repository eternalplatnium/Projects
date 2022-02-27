package apps;

import java.io.*;
import java.util.*;
import java.util.regex.*;

import structures.Stack;

public class Expression {

	/**
	 * Expression to be evaluated
	 */
	String expr;                
    
	/**
	 * Scalar symbols in the expression 
	 */
	ArrayList<ScalarSymbol> scalars;   
	
	/**
	 * Array symbols in the expression
	 */
	ArrayList<ArraySymbol> arrays;
    
    /**
     * String containing all delimiters (characters other than variables and constants), 
     * to be used with StringTokenizer
     */
    public static final String delims = " \t*+-/()[]";
    
    /**
     * Initializes this Expression object with an input expression. Sets all other
     * fields to null.
     * 
     * @param expr Expression
     */
    public Expression(String expr) {
        this.expr = expr;
    }

    /**
     * Populates the scalars and arrays lists with symbols for scalar and array
     * variables in the expression. For every variable, a SINGLE symbol is created and stored,
     * even if it appears more than once in the expression.
     * At this time, values for all variables are set to
     * zero - they will be loaded from a file in the loadSymbolValues method.
     */
    public void buildSymbols() {
    		/** COMPLETE THIS METHOD **/
    	scalars = new ArrayList<ScalarSymbol>();
	    arrays = new ArrayList<ArraySymbol>();
		String var;
		int z = 0;
		for (int i = 0; i < expr.length(); i++){
			if(Character.isLetter(expr.charAt(i))){ 		
				var = getVariable(expr.substring(i));       
				i += var.length();
				if (i >= expr.length()){
					for( int y = 0; y < scalars.size(); y++){
						if( var.equals( scalars.get(y).name )){
							z = 1;
							break;
						}
					}
					if( z == 1 ){
						z = 0;
						break;
					}
					scalars.add(new ScalarSymbol(var));
					break;
				}
			    if(expr.charAt(i) == '['){
			    	for( int y = 0; y < arrays.size(); y++){
						if( var.equals( arrays.get(y).name )){
							z = 1;
							break;
						}
					}
			    	if( z == 1 ){
			    		z = 0;
						continue;
					}
	                arrays.add(new ArraySymbol(var));
		    	}    	
		    	else {
		    		for( int y = 0; y < scalars.size(); y++){
						if( var.equals( scalars.get(y).name )){
							z = 1;
							break;
						}
					}
		    		if( z == 1 ){
		    			z = 0;
						continue;
					}
		    		scalars.add(new ScalarSymbol(var));
		    	}
			}
		}	
    }
    
    private String getVariable(String subStr) {
        String variable = "";
        int i = 0;
        while (Character.isLetter(subStr.charAt(i))) {
           variable += subStr.charAt(i);
           i++;     
           if (i >= subStr.length()){
        	   break;
           }
        }
        return variable;
    }
    
    
    /**
     * Loads values for symbols in the expression
     * 
     * @param sc Scanner for values input
     * @throws IOException If there is a problem with the input 
     */
    public void loadSymbolValues(Scanner sc) 
    throws IOException {
        while (sc.hasNextLine()) {
            StringTokenizer st = new StringTokenizer(sc.nextLine().trim());
            int numTokens = st.countTokens();
            String sym = st.nextToken();
            ScalarSymbol ssymbol = new ScalarSymbol(sym);
            ArraySymbol asymbol = new ArraySymbol(sym);
            int ssi = scalars.indexOf(ssymbol);
            int asi = arrays.indexOf(asymbol);
            if (ssi == -1 && asi == -1) {
            	continue;
            }
            int num = Integer.parseInt(st.nextToken());
            if (numTokens == 2) { // scalar symbol
                scalars.get(ssi).value = num;
            } else { // array symbol
            	asymbol = arrays.get(asi);
            	asymbol.values = new int[num];
                // following are (index,val) pairs
                while (st.hasMoreTokens()) {
                    String tok = st.nextToken();
                    StringTokenizer stt = new StringTokenizer(tok," (,)");
                    int index = Integer.parseInt(stt.nextToken());
                    int val = Integer.parseInt(stt.nextToken());
                    asymbol.values[index] = val;              
                }
            }
        }
    }
    
    
    /**
     * Evaluates the expression, using RECURSION to evaluate subexpressions and to evaluate array 
     * subscript expressions.
     * 
     * @return Result of evaluation
     */
    public float evaluate() {
		/** COMPLETE THIS METHOD **/
		Stack<Character> opStack = new Stack<Character>();
		Stack<Character> currentOp = new Stack<Character>();
		ArrayList<Float> numbers = new ArrayList<Float>();
		int indexOpen = 0, indexClose = 0, numOpen = 0, numClose = 0;
		String newExpr;
		String temp;
		float ans = 0;
		int temp2 = 0;
		
		char operation;
		int first, second;
		
		expr = expr.replaceAll(" ", "");
		expr = expr.replaceAll("\t", "");
		expr += " ";
		
		for (int i = 0; i < expr.length(); i++) {
			if ( expr.charAt(i) == '[') {
				indexOpen = i;
				i++;
				for (; i < expr.length(); i++){
		    		if (expr.charAt(i) == ']'){
		    			numClose++;
		    		}
		    		if (expr.charAt(i) == '['){
		    			numOpen++;
		    		}
		    		if (expr.charAt(i) == ']' && numOpen < numClose){
		    			indexClose = i;
		    			numOpen = 0;
		    			numClose = 0;
		    			break;
					}
		    	}
				
	        	newExpr = expr;
	        	expr = expr.substring(indexOpen+1, indexClose);
	        	temp = newExpr.substring(indexOpen, indexClose+1);
	        		
	        	temp2 = temp.length();
	        	temp = "" + (int)evaluate();
	        	
	        	expr = newExpr.substring(0, indexOpen+1) + temp
	        		+ newExpr.substring(indexClose, newExpr.length());
	
	        	i-=temp2 - temp.length();
			}
		}
		
		for (int i = 0; i < expr.length(); i++) {
			if ( expr.charAt(i) == '(') {
				indexOpen = i;
				i++;
				for (; i < expr.length(); i++){
		    		if ( expr.charAt(i) == ')'){
		    			numClose++;
		    		}
		    		if (expr.charAt(i) == '('){
		    			numOpen++;
		    		}
		    		if ( expr.charAt(i) == ')' && numOpen < numClose){
		    			indexClose = i;
		    			numOpen = 0;
		    			numClose = 0;
		    			break;
					}
		    	}
	        	newExpr = expr;
	        	expr = expr.substring(indexOpen+1, indexClose);
	        	temp = newExpr.substring(indexOpen, indexClose+1);
	        	temp2 = temp.length();
	        	temp = "" + evaluate();
	        	expr = newExpr.substring(0, indexOpen) + temp
	        		+ newExpr.substring(indexClose+1, newExpr.length());
	        	i -= temp2 - temp.length();
			}
		}
		opStack = getStackOps(expr);
		numbers = getNumbers(expr);
		if ( opStack.isEmpty() ){
			return numbers.get(0);
		}
		int test = opStack.size();
		for (int x = 0; x < test; x++){
			operation = opStack.pop();
			currentOp.push(operation);
			if ( hasPriority(operation, opStack ) ) {
				first = currentOp.size()-1;
				second = currentOp.size();	
				ans = doMath(numbers.get(first) , numbers.get(second), operation);
				numbers.set(first, ans);
				numbers.remove(second);
				currentOp.pop();
			}
			else {
	    		operation = opStack.pop();
	        	currentOp.push(operation);
	        	first = currentOp.size()-1;
				second = currentOp.size();
				ans = doMath(numbers.get(first) , numbers.get(second), operation);
				numbers.set(first, ans);
				numbers.remove(second);
				currentOp.pop();
				opStack.push( currentOp.pop() );
			}
		}
		return ans;
	}
	
	private float doMath (float first, float second, char operator) {
		float ans = 0;
		
		switch (operator) {
	    	case '+':
	    		ans = first + second;
	    		break;
	    	case '-':
	    		ans = first - second;
	    		break;
	    	case '*':
	    		ans = first * second;
	    		break;
	    	case '/':
	    		ans = first / second;
	    		break;
		}
		return ans;
	 }
	
	private boolean hasPriority(char c, Stack<Character> opStack) {
		boolean priority = true;
		if ( ( !opStack.isEmpty() ) && (c == '+' || c == '-') &&
				(opStack.peek() == '*' || opStack.peek() == '/') ) {
			priority = false;
		}	
		return priority;
	}
	
	private Stack<Character> getStackOps (String expr){
		Stack<Character> operations = new Stack<Character>();
		Stack<Character> reverse = new Stack<Character>();
		for (int i = 0; i < expr.length(); i++) {
			if (expr.charAt(i) == '+' || expr.charAt(i) == '-'|| expr.charAt(i) == '*'|| expr.charAt(i) == '/'){
				operations.push( expr.charAt(i) );
			}
		}
		while ( !operations.isEmpty() ) {
	 		reverse.push( operations.pop() );
		}
		
		return reverse;
	}
	
	public ArrayList<Float> getNumbers (String expr) {
		ArrayList<Float> numbers = new ArrayList<Float>();
		String num = "";
		
		for (int i = 0; i < expr.length(); i++) {
			if (Character.isDigit(expr.charAt(i)) ){
				for (; i < expr.length(); i++) {
					if ( Character.isDigit(expr.charAt(i)) || expr.charAt(i) == '.' ){
		    			num += expr.charAt(i);
		    		}
		    		else{									
		    			numbers.add( Float.parseFloat(num) );
		    			num = "";		
		    			break; 
		    		}
				}
			}
			else if ( Character.isLetter(expr.charAt(i)) ){
				for (; i < expr.length(); i++) {
					if ( Character.isLetter(expr.charAt(i)) ) {
		    			num += expr.charAt(i);
		    		}
		    		else {							
		    			if ( expr.charAt(i) == '[' ) {
		    				i++;
		    				for (int x = 0; x < arrays.size(); x++) {
		    					if ( num.equals(arrays.get(x).name) ){
		    						num = "";
		    						for (; i < expr.length(); i++) {
		    		    				if ( Character.isDigit(expr.charAt(i)) || expr.charAt(i) == '.' ){
		    		    	    			num += expr.charAt(i);
		    		    	    		}
		    		    	    		
		    		    	    		else {	    	    		    	    			
		    		    	    			numbers.add( (float)arrays.get(x).values[ (int)Float.parseFloat(num) ] );
		    		    	    			num = "";		
		    		    	    			break; 
		    		    	    		}
		    		    			}
		    						break;
		    					}
		    				}
		    				break;
		    			}
		    			else {
		    				for (int x = 0; x < scalars.size(); x++) {
		    					if ( num.equals(scalars.get(x).name) ){
		    						numbers.add( (float)scalars.get(x).value );
		    						num = "";
		    						break;
		    					}
		    				}
		    				break;
		    			}
		    		}
				}
			}
		}
		
		return numbers;
	}

    /**
     * Utility method, prints the symbols in the scalars list
     */
    public void printScalars() {
        for (ScalarSymbol ss: scalars) {
            System.out.println(ss);
        }
    }
    
    /**
     * Utility method, prints the symbols in the arrays list
     */
    public void printArrays() {
    		for (ArraySymbol as: arrays) {
    			System.out.println(as);
    		}
    }

}
