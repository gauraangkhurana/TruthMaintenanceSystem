//
//  JTMS - Justiication Based Truth Maintenence System
//
//
//  Created by Gauraang Khurana on 4/12/18.

import java.util.*;
import java.util.regex.*;
import java.io.*;

public class JTMS {
	
	//Please change file path here before proceeding.
	static final String filePath = "/Users/Isha/eclipse-workspace/Assign4/src/TMS.txt";  
	
	public Map<String,List<String>> implyTable = new HashMap<String,List<String>>();
	public List<String> tellTable = new ArrayList<String>();
	public List<String> dependencyTable = new ArrayList<String>();
	public List<String> knownTable = new ArrayList<String>();
	public List<String> tellTableCopy = new ArrayList<String>();
	
	//Function to read file and store in 
	public List<String> readFile() throws IOException{
		File inputFile = new File(filePath);
		FileReader fileReader = new FileReader(inputFile);
		BufferedReader buffer = new BufferedReader(fileReader);
		String str;
		List<String> inputFileInString = new ArrayList<String>();
		while ((str = buffer.readLine()) != null) {
			inputFileInString.add(str);
		}
		return inputFileInString;
	}
	
	public void maintainSystem(List<String> inputStatements) {
		
		//to Read and process every statement. 
		for(String str : inputStatements) {
			if(str.contains("Tell:")) {
				Matcher m = Pattern.compile(": (.*)").matcher(str);
				if(m.find()) {
					performAction(1,m.group(1));
				}
			}
			else if(str.contains("Retract:")) {
				Matcher m = Pattern.compile(": (.*)").matcher(str);
				if(m.find()) {
					performAction(2,m.group(1));
				}
			}
		}
	}
	
	public void performAction(int action, String instruction) {
		int ruleNumber = tellTable.size();
		
		if(action == 1) {
			if(instruction.length() > 2) {				//The string has more than 1 character. Now check LHS characters
				
				//to add to imply table in form of arraylist of strings.
				int breakPoint = instruction.indexOf(">");
				String rhs = instruction.substring(breakPoint+1);
				String lhs = instruction.substring(0, breakPoint);
				if(knownTable.contains("-"+rhs) || knownTable.contains(rhs.substring(1))) {
					System.out.println("Conflict :\t" + instruction);
					System.out.println("------------------------------------------------------------");
					return;
				}
				if(!tellTable.contains(instruction)) {
					tellTable.add(instruction);
					tellTableCopy.add(instruction);
				}
				
				String implication = new String();		//Break LHS into strings.
				String[] lhsArray = new String[2];		//Break LHS to store in array
				
				if(lhs.length() > 1 && lhs.contains("(") && lhs.contains(")")) {
					Matcher m = Pattern.compile("\\((.*?)\\)").matcher(lhs);
					if(m.find()) { 
						lhs = m.group(1);
					}
					if(lhs.contains("*")) {
						lhsArray = lhs.split("\\*");
						implication = "2" + "," + lhsArray[0] + "," +  lhsArray[1] + "," + ruleNumber;
					}else if(lhs.contains("+")) {
						lhsArray = lhs.split("\\+");
						implication = "1" + "," + lhsArray[0] + "," +  lhsArray[1] + "," + ruleNumber;
					}
					for(String str : lhsArray) {
						dependencyTable.add(str);
					}
				}
				else {
					dependencyTable.add(lhs);
					implication = "1" + "," + lhs + "," + ruleNumber;
				}
								
				List<String> implicationList = new ArrayList<String>();
				
				if(!implyTable.containsKey(rhs)) {
					implicationList.add(implication);
					implyTable.put(rhs,implicationList);
				}
				else {
					implicationList = implyTable.get(rhs);
					implicationList.add(implication);
					implyTable.put(rhs, implicationList);
				}				
				boolean contradiction = checkAgain(true);
				if(!contradiction) {
					//restore all to last stage Here. 
					System.out.println("Conflict :\t" + instruction);
					System.out.println("------------------------------------------------------------");
				}
			}
			else {										//Single tell literal received
				if(knownTable.contains("-"+instruction) || knownTable.contains(instruction.substring(1))) {
					System.out.println("Conflict :\t" + instruction);
					System.out.println("------------------------------------------------------------");
					return;
				}
				
				if(!tellTable.contains(instruction)) {
					tellTable.add(instruction);
					tellTableCopy.add(instruction);
				}
				knownTable.add(instruction);
				boolean didUpdateTMS = true;
				
				while(didUpdateTMS) {
					didUpdateTMS = false;
					if(dependencyTable.contains(instruction)) {						//Check if this single instruction changes the imply table.i.e, it is a dependency
						for(String str : implyTable.keySet()) {						//Iterate over all values in imply table to check 
							List<String> literalDependency = implyTable.get(str);
							
							for(String str2 : literalDependency) {					//Iterate over all dependecy list for particular key in implyTable
								String implication = new String();
								String tempImplication = new String();
								
								String[] literalDependencyByCharacter = str2.split(",");				//Split string by , Ignore last element which is rule number
								
								if(literalDependencyByCharacter[0].equals("2")) {				//AND OPERATOR. Check by first element
									boolean allDependencyMatch = true;					//to check if all dependecy match in case of AND
									for(int i=1; i<literalDependencyByCharacter.length - 1; i++) {
										tempImplication += literalDependencyByCharacter[i] + ", ";
										if(!knownTable.contains(literalDependencyByCharacter[i])) { 
											allDependencyMatch = false;
											tempImplication = "";
											break;
										}
									}
									if(allDependencyMatch) {
										//Add this to tell table.
										if(!knownTable.contains(str)) {
											knownTable.add(str);
											didUpdateTMS = true;
										}
										int implicationByRuleNumber = Integer.valueOf(literalDependencyByCharacter[literalDependencyByCharacter.length - 1]);
										implication = str + ": {" + tempImplication + tellTable.get(implicationByRuleNumber) + "}";
										if(!tellTable.contains(implication)) {
											tellTable.add(implication);
											tellTableCopy.add(implication);
										}
									}
								}
								else if(literalDependencyByCharacter[0].equals("1")) {			//OR Operator
									List<String> tempImply = new ArrayList<String>(); 
									boolean anyDependencyMatch = false;
									for(int i=1; i<literalDependencyByCharacter.length - 1; i++) {
										if(knownTable.contains(literalDependencyByCharacter[i])) { 
											String literalTemp = literalDependencyByCharacter[i] + ", ";
											tempImply.add(literalTemp);
											anyDependencyMatch = true;
										}
									}
									if(anyDependencyMatch) {
										if(!knownTable.contains(str)) {
											knownTable.add(str);
											didUpdateTMS = true;
										}
										int implicationByRuleNumber = Integer.valueOf(literalDependencyByCharacter[literalDependencyByCharacter.length - 1]);
										for(String temp : tempImply) {
											implication = str + ": {" + temp + tellTable.get(implicationByRuleNumber) + "}";
										
										if(!tellTable.contains(implication)) {
											tellTable.add(implication);
											tellTableCopy.add(implication);
										}
									}
										}
								}
							}
						}
					}
				}
			}
		}
		
		
		else if(action == 2) { 							//The retract statement, we need to change the tables.
			
			if(instruction.length() > 2) {
				retractImplicationRule(instruction);
			}
			else {
				 List<String> alsoRetracting = retract(instruction);
				 for(int i=0; i<alsoRetracting.size(); i++) {
					 List<String> toAdd = retract(alsoRetracting.get(i));
					 alsoRetracting.addAll(toAdd);
				 }
				 
				 if(knownTable.contains(instruction)) knownTable.remove(instruction);
				 for(int i=0; i<alsoRetracting.size(); i++) {
					 if(knownTable.contains(alsoRetracting.get(i))) knownTable.remove(alsoRetracting.get(i));
				 } 
			}	
		}
		printTable();
//		System.out.println(tellTable);
	}
	
	public void printTable() {
		for(String str : tellTable) {
			if(!str.equals("~")) System.out.println(str);
		}
		System.out.println("------------------------------------------------------------");
	}
	
	public void retractImplicationRule(String instruction) {
		
		if(!tellTable.contains(instruction)) return;
			if(instruction.contains("*") || instruction.contains("+")) {
				String[] literals = instruction.split(">");
				String lhs = literals[0];
				String ruleNumber = String.valueOf(tellTable.indexOf(instruction));
				String implication = new String();
				String[] lhsArray = new String[2];
				
				if(lhs.length() > 1 && lhs.contains("(") && lhs.contains(")")) {
					Matcher m = Pattern.compile("\\((.*?)\\)").matcher(lhs);
					if(m.find()) { 
						lhs = m.group(1);
					}
					if(lhs.contains("*")) {
						lhsArray = lhs.split("\\*");
						implication = "2" + "," + lhsArray[0] + "," +  lhsArray[1] + "," + ruleNumber;
					}else if(lhs.contains("+")) {
						lhsArray = lhs.split("\\+");
						implication = "1" + "," + lhsArray[0] + "," +  lhsArray[1] + "," + ruleNumber;
					}
				}
				for(String str : lhsArray) {
					if(dependencyTable.contains(str)) dependencyTable.remove(str);
				}
				if(implyTable.containsKey(literals[1])) {
					List<String> implications = implyTable.get(literals[1]);
					List<String> toRemoveFromList = new ArrayList<String>();
					
					for(String tempStr : implications) {
						if(tempStr.equals(implication)) {
							toRemoveFromList.add(implications.get(implications.indexOf(tempStr)));
						}
					}
					for(String tempStr : toRemoveFromList) {
						implications.remove(tempStr);
					}
					implyTable.put(literals[1], implications);
				}
				if(!tellTable.contains(literals[1])) if(knownTable.contains(literals[1])) knownTable.remove(literals[1]);
				if(tellTable.contains(instruction)) {
					tellTable.set(tellTable.indexOf(instruction),"~");
					tellTableCopy.set(tellTableCopy.indexOf(instruction),"~");
					if(tellTableCopy.contains(instruction)) {
						tellTableCopy.set(tellTableCopy.indexOf(instruction),"~");
					}
				}
				for(String str : tellTable) {
					if(str.contains(":")) {
						tellTable.set(tellTable.indexOf(str),"~");
					}
				}
				for(String str : tellTableCopy) {
					if(str.contains(":")) {
						tellTableCopy.set(tellTableCopy.indexOf(str),"~");
					}
				}
				checkAgain(true);
			}
			else {
				String[] literals = instruction.split(">");
				if(dependencyTable.contains(literals[0])) dependencyTable.remove(literals[0]);
				String reconstructImplyStatement = "1" + "," + literals[0] + "," + String.valueOf(tellTable.indexOf(instruction));
				if(implyTable.containsKey(literals[1])){
					List<String> implications = implyTable.get(literals[1]);
					List<String> toRemoveFromList = new ArrayList<String>();
					
					for(String tempStr : implications) {
						if(tempStr.equals(reconstructImplyStatement)) {
							toRemoveFromList.add(implications.get(implications.indexOf(tempStr)));
						}
					}
					for(String tempStr : toRemoveFromList) {
						implications.remove(tempStr);
					}
					implyTable.put(literals[1], implications);
				}
				if(!tellTable.contains(literals[1])) if(knownTable.contains(literals[1])) knownTable.remove(literals[1]);
				if(tellTable.contains(instruction)){
					tellTable.set(tellTable.indexOf(instruction),"~");
					tellTableCopy.set(tellTableCopy.indexOf(instruction),"~");
					if(tellTableCopy.contains(instruction)) {
						tellTableCopy.set(tellTableCopy.indexOf(instruction),"~");
					}
				}
				for(String str : tellTable) {
					if(str.contains(":")) {
						tellTable.set(tellTable.indexOf(str),"~");
					}
				}
				for(String str : tellTableCopy) {
					if(str.contains(":")) {
						tellTableCopy.set(tellTableCopy.indexOf(str),"~");
					}
				}
				checkAgain(true);
			}
		
	}
	
	
	public List<String> retract(String instruction) {
		
		if(tellTable.contains(instruction)) {
			int index = tellTable.indexOf(instruction);
			String temp = tellTable.get(index);
			temp = "~";
			tellTable.set(index, temp);
		}
		List<String> nowRetractingInstructions = new ArrayList<String>();
		//now lets check imply table and remove all implications. Then remove elements from known table. 
		
		for(String str : implyTable.keySet()) {
			List<String> dependency = implyTable.get(str);
			for(String str2 : dependency) {
				String[] literalDependencyArray = str2.split(",");
				
				if(literalDependencyArray.length > 0) {
					if(literalDependencyArray[0].equals("1")) {
						
						boolean allMatch = true; //make changes to remove only one character from tellTable. **********
						boolean anyMatch = false;
						String implication = new String();
						String tempImplication = new String();
						List<String> tempImply = new ArrayList<String>();
						for(int i = 1; i<literalDependencyArray.length-1; i++) {
							if(!literalDependencyArray[i].equals(instruction)) {
								allMatch = false;
							}else {
								anyMatch = true;
							}
							if(knownTable.contains(literalDependencyArray[i])) { 
								String literalImplication = literalDependencyArray[i] + ", ";
								tempImply.add(literalImplication);								
							}
						}
						
						if(allMatch) {
							int implicationByRuleNumber = Integer.valueOf(literalDependencyArray[literalDependencyArray.length-1]);
							for(String strTemp: tempImply) {
								implication = str + ": {" + strTemp + tellTableCopy.get(implicationByRuleNumber) + "}";
								if(tellTableCopy.contains(implication)) {
									changeTellTableOr(instruction,implication);
								}
							}
							nowRetractingInstructions.add(str);
						}
						if(!allMatch && anyMatch) {
							int implicationByRuleNumber = Integer.valueOf(literalDependencyArray[literalDependencyArray.length-1]);
							for(String strTemp : tempImply) {
								implication = str + ": {" + strTemp + tellTableCopy.get(implicationByRuleNumber) + "}";
								if(tellTableCopy.contains(implication)) {
									changeTellTableOr(instruction,implication);
								}
							}
						}
						
					}
					else if(literalDependencyArray[0].equals("2")) {
						boolean anyMatch = false;
						String implication = new String();
						String tempImplication = new String();
						for(int i = 1; i<literalDependencyArray.length-1; i++) {
							if(literalDependencyArray[i].equals(instruction)) {
								anyMatch = true;
							}
							tempImplication += literalDependencyArray[i] + ", ";
						}
						if(anyMatch) {
							int implicationByRuleNumber = Integer.valueOf(literalDependencyArray[literalDependencyArray.length-1]);
							implication = str + ": {" + tempImplication + tellTable.get(implicationByRuleNumber) + "}";
							if(tellTable.contains(implication)) {
								tellTable.set(tellTable.indexOf(implication), "~");
							}
							nowRetractingInstructions.add(str);
						}
					}
				}
			}
		}
		return nowRetractingInstructions;
	}
	
	public void changeTellTableOr(String instruction, String implication) {
		String fromTellTable = new String();
		if(tellTable.contains(implication)) {
			fromTellTable = tellTable.get(tellTable.indexOf(implication));
		} else
			fromTellTable = tellTable.get(tellTableCopy.indexOf(implication));
		
		String[] elements = fromTellTable.split(",");
		if(elements.length > 2) {
			String replacement = fromTellTable.replace(instruction+", ", "");
			tellTable.set(tellTableCopy.indexOf(implication), replacement);
		}
		else {
			if(elements[0].contains(instruction)) {
				if(tellTable.contains(implication)) {
					tellTable.set(tellTable.indexOf(implication), "~");
				} else {
					tellTable.set(tellTableCopy.indexOf(implication), "~");
				}
			}
				
		}
		
	}
	
	public boolean checkAgain(boolean didUpdateTMS) {
		
		while(didUpdateTMS) {
			didUpdateTMS = false;
				for(String str : implyTable.keySet()) {						//Iterate over all values in imply table to check 
					List<String> literalDependency = implyTable.get(str);
					
					for(String str2 : literalDependency) {					//Iterate over all dependecy list for particular key in implyTable
						String implication = new String();
						String tempImplication = new String();
						
						String[] literalDependencyByCharacter = str2.split(",");				//Split string by , Ignore last element which is rule number
						
						if(literalDependencyByCharacter[0].equals("2")) {				//AND OPERATOR. Check by first element
							boolean allDependencyMatch = true;					//to check if all dependecy match in case of AND
							for(int i=1; i<literalDependencyByCharacter.length - 1; i++) {
								tempImplication += literalDependencyByCharacter[i] + ", ";
								if(!knownTable.contains(literalDependencyByCharacter[i])) { 
									allDependencyMatch = false;
									tempImplication = "";
									break;
								}
							}
							if(allDependencyMatch) {
								//Add this to tell table.
								int implicationByRuleNumber = Integer.valueOf(literalDependencyByCharacter[literalDependencyByCharacter.length - 1]);
								implication = str + ": {" + tempImplication + tellTable.get(implicationByRuleNumber) + "}";
								if(knownTable.contains("-"+str) || knownTable.contains(str.substring(1))) {
									return false;
								}
								if(!knownTable.contains(str)) {
									knownTable.add(str);
									didUpdateTMS = true;
								}
								if(!tellTable.contains(implication)) {
									tellTable.add(implication);
									tellTableCopy.add(implication);
								}
							}
						}
						else if(literalDependencyByCharacter[0].equals("1")) {			//OR Operator
							boolean anyDependencyMatch = false;
							List<String> tempImply = new ArrayList<String>();
							for(int i=1; i<literalDependencyByCharacter.length - 1; i++) {
								if(knownTable.contains(literalDependencyByCharacter[i])) { 
									String literalImply = literalDependencyByCharacter[i] + ", ";
									tempImply.add(literalImply);
									anyDependencyMatch = true;
								}
							}
							if(anyDependencyMatch) {
								int implicationByRuleNumber = Integer.valueOf(literalDependencyByCharacter[literalDependencyByCharacter.length - 1]);
								for(String strTemp : tempImply) {
									implication = str + ": {" + strTemp + tellTable.get(implicationByRuleNumber) + "}";
									if(knownTable.contains("-"+str) || knownTable.contains(str.substring(1))) {
										return false;
									}
									if(!knownTable.contains(str)) {
										knownTable.add(str);
										didUpdateTMS = true;
									}
									if(!tellTable.contains(implication)) {
										tellTable.add(implication);
										tellTableCopy.add(implication);
									}
								}
							}
					}
				}
				}
		}
		return true;
	}
	
	
	public static void main(String[] args) {
		
		JTMS instance = new JTMS();
		List<String> inputStatements;
		System.out.println();
		
		try {
			inputStatements = instance.readFile();				//Parse the inputFile and receive it in array.
			instance.maintainSystem(inputStatements);			//This function will maintain the system.
		} catch (IOException e) {
			e.printStackTrace();
		}			
							
	}
}
