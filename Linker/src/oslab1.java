import java.util.*;
import java.io.*;
import java.util.Map;

public class oslab1{

	public static void main(String[] args) throws IOException{
		// ******** STEP 1: READ FILE INPUT, PUT INPUT INTO ARRAYLIST  ***********//
		ArrayList<String> textFile = new ArrayList<>();
		Scanner input = new Scanner(System.in);
		System.out.println("Please input filename: ");
		String tp = "../" + input.next();
		System.out.println("\n");
		try{
			FileReader fr = new FileReader(tp);
			BufferedReader br = new BufferedReader(fr);
			String line = br.readLine();

			while (line != null){
				if (line.length() > 0 && !line.equals("")){
					String[] tokens = line.split("\\s+");
					for (int i = 0; i < tokens.length; i++){
						if(!tokens[i].equals("")){
							textFile.add(tokens[i].trim());
						}
					}
				}
				line = br.readLine();
			}
		}
		catch(IOException ex){
			System.out.println("File not found!");
		}

		// *********** STEP 2: PROCESS ARRAYLIST AND CHANGE INTO MODULES ************//
		ArrayList<objectModule> modules = textToObjectModule(textFile);

		// ********** STEP 3: FIRST PASS: GET SYMBOL TABLE ***************//
		HashMap<String, Integer> symbolTable = getSymbolTable(modules);
		System.out.println("Symbol Table: ");
		System.out.println(symbolTable);

		System.out.println(" ");
		// ************* STEP 4: 2ND PASS : GET MEMORY MAP**************** //
		ArrayList<Integer> memoryMap = getMemoryTable(modules, symbolTable);
		System.out.println("Memory Map: ");
		for (int i = 0; i < memoryMap.size(); i++){
			String mm = String.format("%1$-4s %2$6s", i + ": ", memoryMap.get(i));
			System.out.println(mm);
		}
		System.out.println("");
	}// end main

	public static ArrayList<Integer> getMemoryTable(ArrayList<objectModule> modules, Map<String, Integer> symbolTable){
		// rules:
		/*
			If 5th digit:
				case 1:
					push first 4 digits
				case 2:
					push first 4 digits
				case 3:
					first 4 digits + numofmodeuls so far
				case 4:
					first 3 digits + 0 + symbolValue
		*/

		ArrayList<Integer> memMap = new ArrayList<>();
		HashSet<String> symbolsUsed = new HashSet<>();

		// ******* ERROR 5: ON THE USE LIST BUT NEVER USED *****//
		// get set of use symbols. If at any time it is used, remove it from the set.
		HashSet<String> useList = new HashSet<>();
		for (int i =0 ; i < modules.size(); i++){
			ArrayList<String> temp = modules.get(i).useList;
			for (int j =0 ; j < temp.size(); j++){
				String str = temp.get(j);
				if (!Character.isDigit(str.charAt(0))){
					useList.add(str);
				}
			}
		}

		int numModules = 0;
		for (int i = 0; i < modules.size(); i++){
			int numProgramTexts = Integer.parseInt(modules.get(i).programTextList.get(0));

			for (int j = 1; j <= numProgramTexts; j++){

				String progtext = modules.get(i).programTextList.get(j);
				int lastDigit = Integer.parseInt(Character.toString(progtext.charAt(4)));

				switch (lastDigit){
					case 1:
						int memValue1 = Integer.parseInt(progtext.substring(0,4));
						memMap.add(memValue1);
						break;
					case 2:
						int memValue2 = Integer.parseInt(progtext.substring(0,4));
						int memVal2Check = Integer.parseInt(progtext.substring(1,4));
						// ***** ERROR 7: ABS exceeds size of machine ******//
						if (memVal2Check > 599){
							memValue2 = Integer.parseInt(progtext.substring(0,1)) * 1000;
							System.out.println("Error: " + " absolute address: " + Integer.parseInt(progtext.substring(0,4)) + " --> " + memValue2 + " exceeds machine size; zero used");
						}
						else{
							memValue2 = Integer.parseInt(progtext.substring(0,4));
						}
						memMap.add(memValue2);
						break;
					case 3:
						int memValue3 = Integer.parseInt(progtext.substring(0,4)) + numModules;
						int memValCheck3 = Integer.parseInt(progtext.substring(1,4));
						// ****** ERROR 8: relative address exceeds size of module ********//
						if (memValCheck3 > numProgramTexts){
							memValue3 = Integer.parseInt(progtext.substring(0,1)) * 1000;
							System.out.println("Error: " + " Relative address: " + Integer.parseInt(progtext.substring(0,4)) + " --> " + memValue3 +  " exceeds size of module; zero used");
						}
						else{
							memValue3 = Integer.parseInt(progtext.substring(0,4)) + numModules;
						}
						memMap.add(memValue3);
						break;
					case 4:
						int symNum = Integer.parseInt(Character.toString(progtext.charAt(3)));
						// ****** ERROR 6: EXTERNAL TOO LARGE TO REFERENCE SYMBOL, USE IMMEDIATE INSTEAD ****//
						if (symNum > modules.get(i).useList.size()-1){
							System.out.println("Error: External address " + Integer.parseInt(progtext.substring(0,4)) + " exceeds length of use list; treated as immediate.");
							memMap.add(Integer.parseInt(progtext.substring(0,4)));
						}
						else{
							int memValue4 = Integer.parseInt(progtext.substring(0,3)) * 10;
							String symbolWord = modules.get(i).useList.get(symNum+1);
							// for error 5 - on uselist but not used. remove from set if used
							useList.remove(symbolWord);
							int symbolValue;
							if (symbolTable.get(symbolWord)!=null){
								symbolValue = symbolTable.get(symbolWord);
								symbolsUsed.add(symbolWord);
							}
							else{
								symbolValue = 0;
								//**** ERROR 2: SYMBOL IS USED BUT NOT DEFINED ***//
								System.out.println(symbolWord + " " + "is used but not defined! Symbol will be given value of 0.");
							}
							memValue4 += symbolValue;
							memMap.add(memValue4);
						}
						break;
					default:
						break;
				}
			}
			numModules += numProgramTexts;
		}

		// ******* ERROR 3: DEFINED BUT NEVER USED **********//
		Set<Map.Entry<String,Integer>> symbolsdefined = symbolTable.entrySet();
		ArrayList<String> dnu = new ArrayList<>();

		for (Map.Entry<String,Integer> k : symbolsdefined){
			dnu.add(k.getKey());
		}
		// find symbolsdefined - symbolsUsed = symbols defined but not used
		dnu.removeAll(symbolsUsed);

		for (String a : dnu){
			for (int i = 0; i < modules.size(); i++){
				if(modules.get(i).definitionList.contains(a)){
					System.out.println("Warning: " + a  + " is defined in module " + i + " but never used");
				}
			}
		}

		// ***** Print error 5: on use list but not used *******//
		for (String k : useList){
			for (int i = 0; i < modules.size(); i++){
				ArrayList<String> inUseList = modules.get(i).useList;
				if (inUseList.contains(k)){
						System.out.println("Warning: " + "In module " + i + " " + k + " is on use list but isn't used.");
				}
			}
		}

		return memMap;
	}

	public static HashMap<String, Integer> getSymbolTable(ArrayList<objectModule> listOfModuleLists){

		HashMap<String, Integer> returnValue = new HashMap<>();
		int numOfModules = 0;
		for (int i =0 ; i < listOfModuleLists.size(); i++){
			objectModule s = listOfModuleLists.get(i);
			int numOfDefs = s.definitionList.size();
			if (numOfDefs > 1){
				int track = 1;
				while (track < numOfDefs-1){
						String newSymbol = s.definitionList.get(track);
						//System.out.println("Symbol: " + newSymbol);
						track++;
						int symbolValue = Integer.parseInt(s.definitionList.get(track)) + numOfModules;
						track++;
						// **** ERROR 4: address address appearing in a definition exceeds the size of the module, print an error message and treat
						//the address given as 0 (relative).
						if (symbolValue-numOfModules > Integer.parseInt(s.programTextList.get(0))){
							System.out.println("Address appearing in a definition " + newSymbol  + " " + symbolValue+ " exceeds the size of the module" + Integer.parseInt(s.programTextList.get(0)));
							symbolValue = 0 + numOfModules;
						}

						if (returnValue.get(newSymbol) == null){
							returnValue.put(newSymbol, symbolValue);
						}
						// **** ERROR 1: MULTIPLY DEFINED****//
						else{
							System.out.println("Error: " + newSymbol + " is multiply defined. It will be given the value: " + returnValue.get(newSymbol) + " (first value)");
						}
				}
			}
			numOfModules += Integer.parseInt(s.programTextList.get(0));
		}
		HashSet<String> symDefined = new HashSet<>();
		return returnValue;
	}

	public static ArrayList<objectModule> textToObjectModule(ArrayList<String> textFile){
		String defList = "defList";
		String useList ="useList";
		String progText = "progText";
		String current = defList;
		ArrayList<objectModule> returnValue = new ArrayList<>();
		// get num of modules
		int track = 1;
		while (track < textFile.size()){
			objectModule obm = new objectModule();
			if (current.equals(defList)){
				int useListTrack = track;
				for (int i = 0; i <= Integer.parseInt(textFile.get(useListTrack)) * 2; i++){
					obm.definitionList.add(textFile.get(track));
					track++;
				}
				current = useList;
			}
			if (current.equals(useList)){
				int useListTrack = track;
			 	for (int i = 0; i <= Integer.parseInt(textFile.get(useListTrack)); i++){
					obm.useList.add(textFile.get(track));
					track++;
				}
				current = progText;
			}
			if (current.equals(progText)){
				int useListTrack = track;
				for (int i = 0; i <= Integer.parseInt(textFile.get(useListTrack)); i++){
					obm.programTextList.add(textFile.get(track));
					track++;
				}
				current = defList;
			}
			returnValue.add(obm);
		}
		return returnValue;
	}

	public static class objectModule{
		ArrayList<String> definitionList;
		ArrayList<String> useList;
		ArrayList<String> programTextList;
		public objectModule(){
			this.definitionList = new ArrayList<>();
			this.useList = new ArrayList<>();
			this.programTextList = new ArrayList<>();
		}
	}
}
