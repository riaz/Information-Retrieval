import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

//Compare by document Index in Increasing Order
class DocIndex implements Comparator<String> {
	public int compare(String arg0, String arg1) {	
		Integer docId1 = new Integer(Integer.parseInt(arg0.split("/")[0].trim()));
		Integer docId2 = new Integer(Integer.parseInt(arg1.split("/")[0].trim()));		
		return docId1.compareTo(docId2);
	}
}

//Compare by document Index in Decreasing Order
class TermFreq implements Comparator<String> {
	public int compare(String arg0, String arg1) {	
		Integer docId1 = new Integer(Integer.parseInt(arg0.split("/")[1].trim()));
		Integer docId2 = new Integer(Integer.parseInt(arg1.split("/")[1].trim()));
		
		return docId2.compareTo(docId1);
	}
}

//Compare By Posting Size in Decreasing Order
class PostingSize implements Comparator<Posting> {
	public int compare(Posting arg0, Posting arg1) {	
		return new Integer(arg1.size()).compareTo(new Integer(arg0.size()));
	}
}

//An Posting List Entity Definition
class Entity{
	private int doc;
	private int freq;

	public Entity(int doc,int freq){
		this.doc = doc;
		this.freq = freq;
	}
	
	public int getDocNum(){
		return this.doc;
	}
	
	public int getFreq(){
		return this.freq;
	}
}

//A posting list definition
class Posting{
	private String term;
	private int size;
	
	public Posting(String term,int size){
		this.term = term;
		this.size = size;
	}
	
	public String getTerm(){
		return this.term;
	}
	
	public int size(){
		return this.size;
	}
}

// Stopwatch definition

class StopWatch {
	private static double currentTime;
	
	public StopWatch(){
	   currentTime = System.currentTimeMillis();	
	}
	
	public double getTimeElapsed(){ //returns time in seconds
		double instantTime = System.currentTimeMillis();
		return (instantTime - currentTime)/1000.0;
	}
}

public class CSE535Assignment {	
	private Map<String,LinkedList<Entity>> daatList;
	LinkedList<Posting> topTerms;
	Map<String,LinkedList<Entity>> taatList;
	
	PrintWriter out;
	
	/**
	 * 
	 * @param K  - The Top K Terms to be printed
	 */
	
	public void getTopK(int K){
		out.println("FUNCTION: getTopK "+ K);
		out.print("Result: ");
		
		System.out.println("FUNCTION: getTopK "+ K);
		System.out.print("Result: ");
		
		int i=0;
		for(;i<K-1;i++){			
			out.print(topTerms.get(i).getTerm() + ", ");
			System.out.print(topTerms.get(i).getTerm() + ", ");
		}
		System.out.println(topTerms.get(i).getTerm());
		out.println(topTerms.get(i).getTerm());
		
		out.flush();

	}
	
	/**
	 * 
	 * @param term   - Term for which we display the appropriate postings list in 
	 * 					a) Document IDs sorted in ascending order of Document IDs
	 * 					b) Document IDs sorted in decreasing order of Term Frequencies
	 */
	
	public void getPostings(String term){
		out.println("FUNCTION: getPostings "+ term);
		System.out.println("FUNCTION: getPostings "+ term);
		
		LinkedList<Entity> list = daatList.get(term);
		if(list == null){
			out.println("term not found");
			System.out.println("term not found");			

		}
		else{			
			out.print("Ordered by doc IDs: ");			
			System.out.print("Ordered by doc IDs: ");			
			int i=0;
			for(;i<list.size()-1;i++){
				Entity et = (Entity)list.get(i);
				out.print(et.getDocNum()+", ");
				System.out.print(et.getDocNum()+", ");
				
			}
			out.println(list.get(i).getDocNum());
			System.out.println(list.get(i).getDocNum());
			
			
			list = taatList.get(term);
			out.print("Ordered by TF: ");			
			System.out.print("Ordered by TF: ");	
			
			for(i=0;i<list.size()-1;i++){
				Entity et = (Entity)list.get(i);
				out.print(et.getDocNum()+", ");				
				System.out.print(et.getDocNum()+", ");
				
			}
			out.println(list.get(i).getDocNum());
			System.out.println(list.get(i).getDocNum());
			
		}	
		
		out.flush();
	}
	
	
	
	public void termAtATimeQueryAnd(String[] terms){
		int docsFound=0;
		int compares;
		boolean tnf = false;
		Double time;
		
		LinkedHashMap<Integer,Integer> processor;
		LinkedList<Integer> result = new LinkedList<Integer>();
		LinkedList<Posting> ordered =new LinkedList<Posting>();
		StopWatch sw;
		
		out.print("FUNCTION: termAtATimeQueryAnd ");
		System.out.print("FUNCTION: termAtATimeQueryAnd ");
		
		int i=0;
		for(;i<terms.length-1;i++){
			out.print(terms[i]+ ", ");
			System.out.print(terms[i]+ ", ");
			if(taatList.get(terms[i])  == null) 
			{
				tnf = true;				
			}
			else
				docsFound += taatList.get(terms[i]).size();
		}
		out.println(terms[i]);
		System.out.println(terms[i]);		
		
		if(taatList.get(terms[i]) == null){
			tnf = true;
		}
		else
			docsFound += taatList.get(terms[i]).size();	
		
		if(!tnf){
			
			sw = new StopWatch();
			processor = new LinkedHashMap<Integer,Integer>();		
			compares = 0;
			
			for(int j=0;j<terms.length;j++){
				LinkedList<Entity> list = taatList.get(terms[j]);
				for(i=0;i<list.size();i++){
					if(j==0){
						processor.put(list.get(i).getDocNum(),1);
					}
					else{
						if(processor.get(list.get(i).getDocNum()) != null ){ //exists
							//This hash table increment step gives us the instantaneous results based on the total number
							//of terms scanned
							
							//eg: if the total terms scanned is 2: then we pick only the docs set to value 2 as the results
							processor.put(list.get(i).getDocNum(),processor.get(list.get(i).getDocNum()) + 1);
							compares += i;
						}
						else{ // doesn't exits
							if(j!=0)  //don't compare when entries are being made
								compares += processor.size();
						}
					}			
					
				}
			}		
			
			for(Integer doc: processor.keySet()){
				if(processor.get(doc) == terms.length){
					result.push(new Integer(doc));
				}
			}
			
			time = sw.getTimeElapsed();
			
			//Bonus: sorting terms based on the postingList
			for(String t: terms){
				ordered.push(new Posting(t,taatList.get(t).size()));
			}
			
			Collections.sort(ordered, new PostingSize());
			
			out.println(docsFound + " documents are found");
			System.out.println(docsFound + " documents are found"); 
			
			out.println(compares+" comparisions are made");
			System.out.println(compares+" comparisions are made");
			
			out.println(time + " seconds are used");
			System.out.println(time + " seconds are used");
			
			/* Optimized Comparison Section */
			processor = new LinkedHashMap<Integer,Integer>();		
			compares = 0;
			
			for(int j=ordered.size()-1;j>=0;j--)
			{			
				LinkedList<Entity> list = taatList.get(ordered.get(j).getTerm());
				for(i=0;i<list.size();i++){
					if(j==ordered.size()-1){
						processor.put(list.get(i).getDocNum(),1);
						break;
					}else
					{
						if(processor.get(list.get(i).getDocNum()) != null ){ //exists
							//This hash table increment step gives us the instantaneous results based on the total number
							//of terms scanned					
							//eg: if the total terms scanned is 2: then we pick only the docs set to value 2 as the results
							processor.put(list.get(i).getDocNum(),processor.get(list.get(i).getDocNum()) + 1);
							compares += i;
						}
						else{ // doesn't exits
							if(j!=ordered.size()-1)  //don't compare when entries are being made
								compares += processor.size();
						}
					}						
				}			
				
			}
			
			/* Optimized Comparison Section */		
			
			out.println(compares +" comparisons are made with optimization"); 
			System.out.println(compares +" comparisons are made with optimization"); 
			
			
			out.print("Result: ");
			System.out.print("Result: ");
			
			i=0;
			for(;i<result.size()-1;i++){
				out.print(result.get(i) + ", ");
				System.out.print(result.get(i) + ", ");
			}
			out.println(result.get(i));		
			System.out.println(result.get(i));
		}else{
			time = 0.0;
			compares = 0;
			docsFound = 0;
			
			out.println(docsFound + " documents are found");
			System.out.println(docsFound + " documents are found"); 
			
			out.println(compares+" comparisions are made");
			System.out.println(compares+" comparisions are made");
			
			out.println(time + " seconds are used");
			System.out.println(time + " seconds are used");
			
			out.println(compares +" comparisons are made with optimization"); 
			System.out.println(compares +" comparisons are made with optimization"); 
			
			out.println("Result: term not found");
			System.out.println("Result: term not found");
			
		}	
		out.flush();
	}
	
	public void termAtATimeQueryOr(String[] terms){
		int docsFound=0;
		int compares;
		Double time;
		
		LinkedHashMap<Integer,Integer> processor;
		LinkedList<Integer> result = new LinkedList<Integer>();
		LinkedList<Posting> ordered =new LinkedList<Posting>();
		StopWatch sw;
		
		out.print("FUNCTION: termAtATimeQueryOr ");
		System.out.print("FUNCTION: termAtATimeQueryOr ");
		
		int i=0;
		for(;i<terms.length-1;i++){
			out.print(terms[i]+ ", ");
			System.out.print(terms[i]+ ", ");
			if(taatList.get(terms[i]) == null)
				 continue;
			docsFound += taatList.get(terms[i]).size();
		}
		
		out.println(terms[i]);
		System.out.println(terms[i]);
		
		if(taatList.get(terms[i]) != null)
			docsFound += taatList.get(terms[i]).size();		
		
		
		sw = new StopWatch();
		processor = new LinkedHashMap<Integer,Integer>();		
		compares = 0;
		
		for(int j=0;j<terms.length;j++){
			LinkedList<Entity> list = taatList.get(terms[j]);			
			
			if(list == null) continue; //dealing with non-existent terms
			
				for(i=0;i<list.size();i++){
				if(processor.get(list.get(i).getDocNum()) != null ){ //exists
					//This hash table increment step gives us the instananeous results based on the total number
					//of terms scanned
					
					//eg: if the total terms scanned is 2: then we pick only the docs set to value 2 as the results
					processor.put(list.get(i).getDocNum(),processor.get(list.get(i).getDocNum()) + 1);
					compares += i;
				}
				else{ // doesn't exits
					if(j!=0){  //don't compare when entries are being made
						compares += processor.size();
						processor.put(list.get(i).getDocNum(),1);
					}
				}
				
				if(j==0)
					processor.put(list.get(i).getDocNum(),1);				
				
			}
		}		
	
		for(Integer doc: processor.keySet()){
				result.push(new Integer(doc));			
		}
		
		time = sw.getTimeElapsed();
		
		//Bonus: sorting terms based on the postingList
		for(String t: terms){
			if(taatList.get(t) != null) //include the terms which exists only
				ordered.push(new Posting(t,taatList.get(t).size()));
		}
		
		Collections.sort(ordered, new PostingSize());
		
		out.println(docsFound + " documents are found");
		System.out.println(docsFound + " documents are found"); 
		
		out.println(compares+" comparisions are made");
		System.out.println(compares+" comparisions are made");
		
		out.println(time + " seconds are used");
		System.out.println(time + " seconds are used");
		
		/* Optimized Comparison Section */
		processor = new LinkedHashMap<Integer,Integer>();		
		compares = 0;
		
		for(int j=ordered.size()-1;j>=0;j--){			
			LinkedList<Entity> list = taatList.get(ordered.get(j).getTerm());
			for(i=0;i<list.size();i++){
				if(processor.get(list.get(i).getDocNum()) != null ){ //exists
					//This hash table increment step gives us the instantaneous results based on the total number
					//of terms scanned					
					//eg: if the total terms scanned is 2: then we pick only the docs set to value 2 as the results
					processor.put(list.get(i).getDocNum(),processor.get(list.get(i).getDocNum()) + 1);
					compares += i;
				}
				else{ // doesn't exits
					if(j!=ordered.size()-1){  //don't compare when entries are being made
						compares += processor.size();
						processor.put(list.get(i).getDocNum(),1);						
					}
				}
				
				if(j == ordered.size()-1)
					processor.put(list.get(i).getDocNum(),1);				
			}
		}		
		/* Optimized Comparison Section */		
		
		out.println(compares +" comparisons are made with optimization"); 
		System.out.println(compares +" comparisons are made with optimization"); 
		
		
		out.print("Result: ");
		System.out.print("Result: ");
		
		if(result.size() != 0){
			i=0;
			for(;i<result.size()-1;i++){
				out.print(result.get(i) + ", ");
				System.out.print(result.get(i) + ", ");
			}
			out.println(result.get(i));		
			System.out.println(result.get(i));			
		}
		else{
			out.println("term not found");		
			System.out.println("term not found");		
		}
		
		out.flush();
	}
	
	public void docAtATimeQueryAnd(String[] terms){
		int docsFound=0;
		int compares = 0;
		double timeElapsed;
		StopWatch sw;
		boolean tnf = false;
		
		LinkedList<Integer> result = new LinkedList<Integer>();
		
		out.print("FUNCTION: docAtATimeQueryAnd ");
		System.out.print("FUNCTION: docAtATimeQueryAnd ");
		
		LinkedHashMap<Integer,List<Boolean>> matrix = new LinkedHashMap<Integer,List<Boolean>>();

		int i=0;
		for(;i<terms.length-1;i++){
			out.print(terms[i]+ ", ");
			System.out.print(terms[i]+ ", ");
			if(daatList.get(terms[i])  == null) 
			{
				tnf = true;				
			}
			else
				docsFound += daatList.get(terms[i]).size();
		}
		
		out.println(terms[i]);
		System.out.println(terms[i]);
		
		if(daatList.get(terms[i]) == null){
			tnf = true;
		}
		else
			docsFound += daatList.get(terms[i]).size();	
		
		if(!tnf){		
		
		
		sw = new StopWatch();
		for(i=0;i<terms.length;i++){
			LinkedList<Entity> lst =  daatList.get(terms[i]);
			for(Entity t: lst){
				if(i==0){
					Boolean[] list = new Boolean[terms.length];
					Arrays.fill(list, false);
					list[0] = true;
					matrix.put(new Integer(t.getDocNum()), Arrays.asList(list));
				}
				else if(matrix.containsKey(t.getDocNum())){
					matrix.get(t.getDocNum()).set(i, true);				
				}
			}
		}	
		
		for(Integer t: matrix.keySet()){			
			List<Boolean> lst= matrix.get(t);
			Boolean yes = true;
			for(int j=0; j< lst.size();j++)
			{
				 yes = yes & lst.get(j); //boolean operation happening
				 if(yes){
					 compares += 1;
				 }else{
					 break; //since its already false ,and further anding is futile
				 }
			}
			if(yes){
				//make an entry into results
				result.push(t);
			}		
		}
		timeElapsed = sw.getTimeElapsed();
		
		out.println(docsFound + " documents are found"); 
		System.out.println(docsFound + " documents are found"); 
		
		out.println(compares + " comparisions are made");
		System.out.println(compares + " comparisions are made");

		out.println(timeElapsed + " seconds are used");
		System.out.println(timeElapsed + " seconds are used");
		
		out.print("Result: ");
		System.out.print("Result: ");
		
		if(result.size()>0){
			for(i=result.size()-1;i>0;i--){
				out.print(result.get(i) + ", ");
				System.out.print(result.get(i) + ", ");
			}
			out.println(result.get(i));		
			System.out.println(result.get(i));
		}
		}
		else{
			timeElapsed = 0.0;
			compares = 0;
			docsFound = 0;
			
			out.println(docsFound + " documents are found");
			System.out.println(docsFound + " documents are found"); 
			
			out.println(compares+" comparisions are made");
			System.out.println(compares+" comparisions are made");
			
			out.println(timeElapsed + " seconds are used");
			System.out.println(timeElapsed + " seconds are used");
						
			out.println("Result: term not found");
			System.out.println("Result: term not found");
		}
		
		out.flush();
	}
	
	public void docAtATimeQueryOr(String[] terms){
		int docsFound=0;
		int compares = 0;
		double timeElapsed;
		StopWatch sw;
		
		LinkedList<Integer> result = new LinkedList<Integer>();
		
		out.print("FUNCTION: docAtATimeQueryOr ");
		System.out.print("FUNCTION: docAtATimeQueryOr ");
		
		LinkedHashMap<Integer,List<Boolean>> matrix = new LinkedHashMap<Integer,List<Boolean>>();

		int i=0;
		for(;i<terms.length-1;i++){
			out.print(terms[i]+ ", ");
			System.out.print(terms[i]+ ", ");
			if(daatList.get(terms[i]) == null)
				 continue;
			docsFound += daatList.get(terms[i]).size();
		}
		
		out.println(terms[i]);
		System.out.println(terms[i]);

		if(daatList.get(terms[i]) != null)
			docsFound += daatList.get(terms[i]).size();		
		
		sw = new StopWatch();
		for(i=0;i<terms.length;i++){
			LinkedList<Entity> lst =  daatList.get(terms[i]);
			
			if(lst == null) continue; //dealing with non-existent terms
			
			for(Entity t: lst){
				if(i==0){
					Boolean[] list = new Boolean[terms.length];
					Arrays.fill(list, false);
					list[0] = true;
					matrix.put(new Integer(t.getDocNum()), Arrays.asList(list));
				}
				else if(matrix.containsKey(t.getDocNum())){
					matrix.get(t.getDocNum()).set(i, true);				
				}
				else if(!matrix.containsKey(t.getDocNum())){ //doesn't contains key, make an new entry in 'OR'
					Boolean[] list = new Boolean[terms.length];
					Arrays.fill(list, false);
					list[i] = true;
					matrix.put(new Integer(t.getDocNum()), Arrays.asList(list));				
				}
			}
		}	
		
		for(Integer t: matrix.keySet()){			
			List<Boolean> lst= matrix.get(t);
			Boolean yes = true;
			for(int j=0; j< lst.size();j++)
			{
				 yes = yes | lst.get(j); //boolean operation happening
				 if(yes){
					 compares += 1; //since its already true ,no further or is needed
					 break;
				 }else{
					 compares += 1; //since the validity of the bit maze is not yet determined..unless u get a true value
				 }
			}
			if(yes){
				//make an entry into results
				result.push(t);
			}		
		}
		timeElapsed = sw.getTimeElapsed();
		
		out.println(docsFound + " documents are found"); 
		System.out.println(docsFound + " documents are found"); 
		
		out.println(compares + " comparisions are made");
		System.out.println(compares + " comparisions are made");

		out.println(timeElapsed + " seconds are used");
		System.out.println(timeElapsed + " seconds are used");
		
		out.print("Result: ");
		System.out.print("Result: ");	
		
		if(result.size()>0){
			for(i=result.size()-1;i>0;i--){
				out.print(result.get(i) + ", ");
				System.out.print(result.get(i) + ", ");
			}
			out.println(result.get(i));		
			System.out.println(result.get(i));
		}else{
			out.println("term not found");		
			System.out.println("term not found");
		}
		
		out.flush();
		
	}
	
	public CSE535Assignment(String idxFile,String outputFile,int K,String qFile){
		try{
			
			File indexFile = new File(idxFile);
//			File queryFile = new File(qFile);
			
			out = new PrintWriter(new FileWriter(outputFile));
			
			List<String> entries = Files.readAllLines(Paths.get(indexFile.getAbsolutePath()
					), StandardCharsets.UTF_8);			
			
			topTerms= new LinkedList<Posting>();
			
			//DAAT List			
			daatList = new HashMap<String,LinkedList<Entity>>();
			
			//TAAT List
			taatList = new HashMap<String,LinkedList<Entity>>();
			
			for(String entry: entries){		
				String[] m = entry.split("\\\\");
					
			    if (m.length > 0) {
			    	String term = m[0];	
			    	String termFreq = m[1].substring(1,m[1].length());
			    	String x = m[2];		
			    	
			    	//topTerms.put(Integer.parseInt(termFreq), term); // <Term Frequency, Term>
			    	topTerms.add(new Posting(term, Integer.parseInt(termFreq)));
			    	
			    	String[] docs = x.substring(2,x.length()-1 ).split(",");
			    	
			    	List<String> docList  = Arrays.asList(docs);
			    	List<String> termList = Arrays.asList(docs.clone());
			    	
			    	Collections.sort(docList,new DocIndex());
			    	Collections.sort(termList,new TermFreq());
			    	
			    	for(String s: docList)
			    	{
			    		String[] entity = s.split("/");
			    		if(daatList.get(term)!=null)
			    		{
			    			daatList.get(term).add(new Entity(Integer.parseInt(entity[0].trim()),Integer.parseInt(entity[1].trim())));
			    		}
			    		else{
			    			LinkedList<Entity> list = new LinkedList<Entity>();
			    			list.add(new Entity(Integer.parseInt(entity[0].trim()),Integer.parseInt(entity[1].trim())));
			    			daatList.put(term, list);	    			
						    
			    		}
			    	}				    	
			    	
			    	for(String s: termList)
			    	{
			    		String[] entity = s.split("/");
			    		if(taatList.get(term)!=null)
			    		{
			    			taatList.get(term).add(new Entity(Integer.parseInt(entity[0].trim()),Integer.parseInt(entity[1].trim())));
			    		}
			    		else{
			    			LinkedList<Entity> list = new LinkedList<Entity>();
			    			list.add(new Entity(Integer.parseInt(entity[0].trim()),Integer.parseInt(entity[1].trim())));
			    			taatList.put(term, list);	    			
						    
			    		}
			    	}		    	
			    	
			    }
			}	
			
			//topK = topTerms.descendingMap();
			Collections.sort(topTerms,new PostingSize());			
				
			getTopK(K);
			
			BufferedReader br = new BufferedReader(new FileReader(qFile));
			while(br.ready()){
				String line = br.readLine();
				
				String terms[] = line.split(" ");
				for(String word: terms){					
					getPostings(word);
				}
			
				//Performs "AND" Operation on DocIDs taking a Term at a time
				termAtATimeQueryAnd(terms);
				
				//Performs "OR" Operation on DocIDs taking a Term at a time
				termAtATimeQueryOr(terms);
				
				//Performs "AND" Operation on Docs taking a Doc at a time
				docAtATimeQueryAnd(terms);
				
				//Performs "Or" Operation on Docs taking a Doc at a time				
				docAtATimeQueryOr(terms);
			}	
			br.close();
		}
		catch(ArrayIndexOutOfBoundsException aobe){
			System.out.println("ArrayIndexOutOfBoundsException has occured.");
			aobe.printStackTrace();
		}	
		catch(IOException ioe){
			System.out.println("IOException");
			ioe.printStackTrace();
		}	
		
		out.flush(); //flushing out remaining output, if any
		out.close(); //closing the PrintWriter Object to avoid resource leak
	}
	
	public static void main(String[] args){
		try
		{
			String indexFile = args[0];
			String outputFile = args[1];
			int K = Integer.parseInt(args[2]);
			String queryFile = args[3];
			
			new CSE535Assignment(indexFile,outputFile,K,queryFile);	
			
		}
		catch(IllegalArgumentException iae){
			System.out.println("You didn't pass the arguments correctly:\nFormat: java CSE535Assignment <index_file> <output_file> <topK> <query_file>");
			iae.printStackTrace();
		}
	}
}

