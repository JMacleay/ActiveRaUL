package ie.deri.raul;

// THIS CLASS IS OBSOLETE AND UNUSED (just for testing and demonstration stuff)
//import foaf.Person;


import ie.deri.raul.guigeneration.MetaDataExtraction;
import ie.deri.raul.guigeneration.RDFTripleExtractor;
import ie.deri.raul.guigeneration.RaULSearch;
import ie.deri.raul.persistence.RDFRepository;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map.Entry;

import org.openrdf.query.BindingSet;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.RepositoryResult;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.XMLBaseParser;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.sail.inferencer.fc.ForwardChainingRDFSInferencer;
import org.openrdf.sail.memory.MemoryStore;
import org.openrdf.model.URI;


public class TestClassLatest {

	private static TupleQuery query= null;
	static RepositoryConnection con ;
	static String endpoint = "http://w3c.org.au/raul/service/public/forms/ssnSurvivalRange";
	static HashMap<String, String> listElements = new HashMap<String, String>();
	
	public static void main(String[] args) throws Exception {
		
		Repository myRepository = new SailRepository(new ForwardChainingRDFSInferencer(new MemoryStore()));
		//Repository myRepository = new SailRepository(new MemoryStore());
		myRepository.initialize();
		
		//LinkedHashMap<String, LinkedHashMap<String, List<String>>> uri = new LinkedHashMap<String, LinkedHashMap<String,List<String>>>();
		//RaULGUIGeneration gui = new RaULGUIGeneration();
	//	 String classURI = "http://www.ontologydesignpatterns.org/ont/dul/DUL.owl#Event";
		//String classURI = "http://matinf.cmse.csiro.au/ontologies/owl/treatment#maximum_weight_specification";
		String classURI = "http://example.org/swj#Person"; 
	//	 String classURI = "http://purl.oclc.org/NET/ssnx/ssn#System";
		//String classURI = "http://xmlns.com/foaf/0.1/Person";
		 
	    String filePath = "/home/u5096831/PhDResearch/Papers/SWJ2013/resources/example.owl";	
	  //  String filePath = "/home/u5096831/PhDResearch/Ontologies/treatment.owl";	
	   // String filePath = "/home/u5096831/PhDResearch/FOAF/foaf.rdf";
	//	String filePath = "/home/u5096831/PhDResearch/SSN/ssn.owl";
	 // String filePath = "/home/u5096831/Desktop/xml1.rdf";
		   
		// String filePath = "/home/u5096831/Desktop/text.rdf";
			
			File file = new File(filePath);
		//	File file2 = new File(filePath2);
		    
		    XMLBaseParser parser=new XMLBaseParser(new InputStreamReader(new FileInputStream(file)));

			String baseURI = parser.getXMLBase();
			//System.out.println( "base URI : " + baseURI);
			
			con = myRepository.getConnection();
		   
			try {
			  con.add(file, "", RDFFormat.RDFXML);
			//  con.add(file2, "", RDFFormat.RDFXML);
			}
			finally {
    		  //    con.close(); 
			}
				
			//print();
		  String endpoints = "http://www.activeraul.org/service/public/forms/person";
		//  print();
		  
		 // RDFTripleExtractor rdf = new RDFTripleExtractor(myRepository);
		 // RDFRepository graph = rdf.addTriples();
		//  RepositoryConnection  cooo = graph.createConnection();
		  

	/*		try {	
				
				query = con.prepareTupleQuery(QueryLanguage.SPARQL, 
				"PREFIX rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#> " +
				"PREFIX rdfs:<http://www.w3.org/2000/01/rdf-schema#> " +
				"PREFIX owl:<http://www.w3.org/2002/07/owl#> " +
				"PREFIX ifkm:<http://purl.org/ontology/ifkm#> " +
				
				"SELECT ?x ?y " +
				"WHERE {?x rdfs:label ?y." +
				"FILTER (?x = <"+"http://www.activeraul.org/service/public/forms/ssnSystem/1366938491060_label"+">)}"
				);
				
			} catch (MalformedQueryException e1) {
				e1.printStackTrace();}
			
			TupleQueryResult resultSet = query.evaluate();

			while(resultSet.hasNext()) {
				BindingSet results = resultSet.next();
				String x = results.getValue("x").toString();
				String y = results.getValue("y").toString();
				//String z = results.getValue("z").toString();
				System.out.println( x + "         "+ y );
			}	*/


		// getAllTriples(classURI);
		//  getRelevantPropertiesListTest(classURI);
	//	GenerateRaULRDFTest test = new GenerateRaULRDFTest(myRepository, endpoints, classURI);

		  
		//  getAllTriples(classURI);
		  
		/*  ExistingResources resources = new ExistingResources();
		  ArrayList<String> resourcesList = resources.getAllInstances(classURI);
		  System.out.println(resourcesList);
		  
		  for(int index=0; index<resourcesList.size(); index++){
			  resources.getAllProperties(resourcesList.get(index));
		  }*/
		 
		 // test.isTransitive("http://purl.oclc.orRDFBean bb=null;g/NET/ssnx/ssn#deployedOnPlatform");
		  //test.isInverse("http://purl.oclc.org/NET/ssnx/ssn#deployedOnPlatform", "http://purl.oclc.org/NET/ssnx/ssn#inDeployment");
		  //test.isFunctional("http://purl.oclc.org/NET/ssnx/ssn#deployedOnPlatform");
		  
		//  test.writePropertiesDescription(classURI, "");
		 
			/*******************     Get Classes and Comments about an ontology         **********************/
			
//		  MetaDataExtraction ontologyData = new MetaDataExtraction(con);
//		  HashMap<String,String> map = ontologyData.getOntologyClasses(baseURI);
//		  
//		  int count = 0;
//		  for(Entry<String,String> entry: map.entrySet()){
//			  System.out.println("******************************");
//			  System.out.println(entry.getKey());
//			  System.out.println(entry.getValue());
//			  count++;
//		  }
//		  System.out.println(count);
		  
		  
		  
		  
		 //TestClass tc = new TestClass(pageURI, myRepository);
		  GenerateRaULRDFTest raulRdf = new GenerateRaULRDFTest(myRepository, endpoints, classURI);
		// System.out.println(raulRdf.generateRaULBasedRDFGraph());
		  // System.out.println(getRangeRelevantPropertiesList(URI, property))
		   //getInverseOf();
		   //generateRaULRDF();
		  
		  
	}
	
	public static String getBaseURI(String path){
		String strLine = "" , baseURI ="";
		try{
			  FileInputStream fstream = new FileInputStream(path);
			  DataInputStream in = new DataInputStream(fstream);
			  BufferedReader br = new BufferedReader(new InputStreamReader(in));
			  while ((strLine = br.readLine()) != null)   {
			  // Print the content on the console
				  if (strLine.contains("xml:base") ){
					  baseURI = strLine.split("=")[1].replaceAll("\"", "").replaceAll(">", "");
					  
					  if(baseURI.startsWith("&")){
						  System.out.println (baseURI);	  
						  baseURI = baseURI.replace("&","");
						  baseURI = baseURI.replace(";","");
					  } else {
						  System.out.println (baseURI);
					  }
					  break;  
				  } else if (strLine.contains("owl:Ontology rdf:about=")) { 
					  baseURI = strLine.split("owl:Ontology rdf:about=")[1];
					  baseURI = baseURI.split(" ")[0].replaceAll("\"", "");
					  System.out.println (baseURI);
				  }
			  	}
			  in.close();
			}catch (Exception e){//Catch exception if any
			  System.err.println("Error: " + e.getMessage()); }
		return baseURI;
	}
	
	public static void getRelevantPropertiesListTest(String URI) throws RepositoryException, QueryEvaluationException,  MalformedQueryException{
		//ArrayList<String> al = new ArrayList<String>();
		//HashMap<String, String> properties = new HashMap<String, String>();
		
		//System.out.println(URI);
		try {	

			String queryString = "PREFIX rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#> " +
					"PREFIX rdfs:<http://www.w3.org/2000/01/rdf-schema#> " +
					"PREFIX owl:<http://www.w3.org/2002/07/owl#> " +
					
					"SELECT DISTINCT ?property ?label ?range ?type ?iProperty " +
					"WHERE {" +
					"{ {<"+URI+"> rdfs:subClassOf ?restriction." +
					"?restriction owl:onProperty ?property." +
					"OPTIONAL {" +
						"{?restriction owl:allValuesFrom ?range.} " +
						"UNION " +
						"{?restriction owl:someValuesFrom ?range} " +
						"}" +
					"}" +
					" UNION " +
					"{?property rdfs:domain <"+URI+">." +
					"OPTIONAL " +
						"{?property rdfs:range ?range.}" +
					"}" +
					"}" +
					"OPTIONAL {?property rdfs:label ?label.}" +
					"OPTIONAL {" +
						"?property rdf:type ?type. " +
						"FILTER ((?type = owl:ObjectProperty) || (?type = owl:DatatypeProperty))" +
					"}" +
					"OPTIONAL {?property owl:inverseOf ?iProperty.}" +
					"} ORDER BY (?label) ";
			
			query = con.prepareTupleQuery(QueryLanguage.SPARQL, queryString);
		} catch (MalformedQueryException e1) {
			//e1.printStackTrace();
			System.out.println(e1);
		}
		
		TupleQueryResult resultSet = query.evaluate();
		
		int count = 0;
		while(resultSet.hasNext()) {
			String label="", range="", type = "", iproperty ="";
			BindingSet results = resultSet.next();
			String property = results.getValue("property").stringValue();
			if(results.hasBinding("label")){
				label = results.getValue("label").stringValue();}
			if(results.hasBinding("range")){
				range = results.getValue("range").toString();}
			if(results.hasBinding("type")) {
				type = results.getValue("type").toString();
			}
			if(results.hasBinding("iProperty")) {
				iproperty = results.getValue("iProperty").toString();
			}
			
			//PropertyModel model = new PropertyModel(property, type , range, label, inverse);
			//properties.put(property, range);
			
			System.out.println("Property : " + property + " Label : " + label + " TYpe : " +type +" Range : "+ range +" IProperty : " +iproperty) ;
			count++;
			//al.add(property);
		}
		System.out.println(count);
		//traversedNodes.put(URI, properties);
		//return al;
	}
	
	public static void getAllTriples(String URI) throws RepositoryException, QueryEvaluationException,  MalformedQueryException{

		try {	
			
			String queryString = "PREFIX rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#> " +
					"PREFIX rdfs:<http://www.w3.org/2000/01/rdf-schema#> " +
					"PREFIX owl:<http://www.w3.org/2002/07/owl#>" +
					"PREFIX raul:<http://purl.org/NET/raul#> " +
					
					"SELECT ?value ?range ?type " +

					"WHERE {" +
					"?uri raul:value ?value." +
					//"?uri rdf:type ?textbox." +
				//	"OPTIONAL {?uri rdfs:label ?label.}" +
					"?uri rdfs:range ?range." +
					"FILTER (?range = \""+URI+"\" )" +
					"OPTIONAL { ?uri rdfs:comment ?type.} " +
					"}";
			query = con.prepareTupleQuery(QueryLanguage.SPARQL, queryString);
		} catch (MalformedQueryException e1) {
			//e1.printStackTrace();
			System.out.println(e1);
		}
		
		TupleQueryResult resultSet = query.evaluate();
		
		int count = 0;
		while(resultSet.hasNext()) {
	
			BindingSet results = resultSet.next();
			
			String label="", range ="", type = "", label2 ="" ;
			if(results.hasBinding("value")){
			label = results.getValue("value").stringValue();

			getTriples(label);

			}
			
			if(results.hasBinding("range")){
				range = results.getValue("range").stringValue();}
			if(results.hasBinding("type")){
				type = results.getValue("type").stringValue();}
			
			
			System.out.println(  label +"	" + range + "	"+ type) ;
			System.out.println(  " *   * * * * * * * * * * * ** * * *  * * **		"  ) ;
			count++;
		}
		System.out.println(count);

	}
	
	
	public static void getTriples(String URI) throws RepositoryException, QueryEvaluationException,  MalformedQueryException{

		try {	
			
			String queryString = "PREFIX rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#> " +
					"PREFIX rdfs:<http://www.w3.org/2000/01/rdf-schema#> " +
					"PREFIX owl:<http://www.w3.org/2002/07/owl#>" +
					"PREFIX raul:<http://purl.org/NET/raul#> " +
					
					"SELECT ?subject ?property ?object " +

					"WHERE {" +
						"<"+URI+"> rdf:subject ?subject." +
						"<"+URI+"> rdf:predicate ?property." +
						"OPTIONAL {<"+URI+"> rdf:object ?object.}" +
					"}";
			query = con.prepareTupleQuery(QueryLanguage.SPARQL, queryString);
		} catch (MalformedQueryException e1) {
			//e1.printStackTrace();
			System.out.println(e1);
		}
		
		TupleQueryResult resultSet = query.evaluate();
		
		int count = 0;
		while(resultSet.hasNext()) {
	
			BindingSet results = resultSet.next();
			
			String subject = results.getValue("subject").stringValue();
			String property = results.getValue("property").stringValue();
			String object="";
			if(results.hasBinding("object")) {
			object = results.getValue("object").stringValue();}
			
			System.out.println( "SUBJECT : : :" + subject + "	PROPERTY :::	" + property +	"	OBJECT :: : " + object ) ;

			count++;
		}

	}
	
	public static void print() throws RepositoryException, QueryEvaluationException,  MalformedQueryException{

		try {	
			
			query = con.prepareTupleQuery(QueryLanguage.SPARQL, 
			"PREFIX rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#> " +
			"PREFIX rdfs:<http://www.w3.org/2000/01/rdf-schema#> " +
			"PREFIX owl:<http://www.w3.org/2002/07/owl#> " +
			"PREFIX ifkm:<http://purl.org/ontology/ifkm#> " +
			
			"SELECT ?x ?y " +
			"WHERE {<http://matinf.cmse.csiro.au/ontologies/owl/treatment#refrigeration_process> <http://www.w3.org/2002/07/owl#equivalentClass> ?z." +
			"?z <http://www.w3.org/2002/07/owl#someValuesFrom> ?a." +
			"?a <http://www.w3.org/2002/07/owl#intersectionOf> ?b." +
			"?b <http://www.w3.org/1999/02/22-rdf-syntax-ns#rest> ?c." +
			"?c ?x ?y }"
			);
			
		} catch (MalformedQueryException e1) {
			e1.printStackTrace();}
		
		TupleQueryResult resultSet = query.evaluate();

		while(resultSet.hasNext()) {
			BindingSet results = resultSet.next();
			String x = results.getValue("x").toString();
			String y = results.getValue("y").toString();
			//String z = results.getValue("z").toString();
			System.out.println( x + "         "+ y );
		}	

	}
}