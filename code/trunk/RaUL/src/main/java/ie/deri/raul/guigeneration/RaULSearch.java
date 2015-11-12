package ie.deri.raul.guigeneration;

import ie.deri.raul.persistence.RDFRepository;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;

import org.openrdf.model.Statement;
import org.openrdf.query.BindingSet;

import org.openrdf.query.GraphQueryResult;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.rdfxml.RDFXMLWriter;
import org.openrdf.rio.turtle.TurtleWriter;
import org.openrdf.query.GraphQuery;

public class RaULSearch {

	/**
	 * @param args
	 */
	RepositoryConnection connection;
	TupleQuery query= null;
	
	public RaULSearch(RDFRepository repository) {
		// TODO Auto-generated constructor stub
		try{ 
			this.connection = repository.createConnection();
		}catch(Exception e){}
	}
	
	public RaULSearch(Repository repository) {
		// TODO Auto-generated constructor stub
		try{ 
			this.connection = repository.getConnection();
		}catch(Exception e){}
	}
	
	public HashMap<String,HashMap<String, String>> getInstances(String range, String queryString) throws RepositoryException, QueryEvaluationException,  MalformedQueryException{


		 HashMap<String, HashMap<String, String>> instances = new  HashMap<String,HashMap<String, String>>();
		 
		String output ="";
		try {			
			query = connection.prepareTupleQuery(QueryLanguage.SPARQL, 
			"PREFIX rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#> " +
			"PREFIX rdfs:<http://www.w3.org/2000/01/rdf-schema#> " +
			"PREFIX owl:<http://www.w3.org/2002/07/owl#> " +
			
			"SELECT DISTINCT ?subject ?slabel ?property ?plabel ?object ?olabel " +
			"WHERE {" +
				"?subject rdf:type <"+range+">." +
				"?subject rdfs:label ?slabel." +
			//	"FILTER(?slabel = )" +
		        "FILTER regex(?slabel, \""+ queryString +"\", \"i\")"+
				"OPTIONAL {?subject ?property ?object." +
					"OPTIONAL {?object rdfs:label ?olabel.}" +
					"OPTIONAL {?property rdfs:label ?plabel.}" +
					"FILTER (?property != rdf:type)" +
					"FILTER (?property != rdfs:label)" +
				"}" +
				"OPTIONAL {" +
					"?subject ?subProperty ?object." +
					"?subProperty rdfs:subPropertyOf ?property." +
					"FILTER (?property != ?subProperty)} " +
					"FILTER (!bound(?subProperty))" +
				"} ORDER BY (?property)" );
			
			/*
			"SELECT DISTINCT ?subject ?slabel ?property ?plabel ?object ?olabel " +
			"WHERE {" +
				"?subject rdf:type <"+range+">." +
				"OPTIONAL {?subject rdfs:label ?slabel.}" +
				"?subject ?property ?object." +
				"OPTIONAL {?property rdfs:label ?plabel.}" +
				"OPTIONAL {?object rdfs:label ?olabel}" +
				"OPTIONAL {" +
				"?subject ?subProperty ?object." +
				"?subProperty rdfs:subPropertyOf ?property." +
				"FILTER (?property != ?subProperty)" +
				"} " +
				"FILTER (?property != rdf:type)" +
				"FILTER (?property != rdfs:label)" +
				"FILTER (!bound(?subProperty))" +
				"}"*/
		} catch (MalformedQueryException e1) {
			System.out.print(e1);
			}
		
		TupleQueryResult resultSet = query.evaluate();


		HashMap<String,String> instDetail = new HashMap<String, String>();
		
		String name="start";
		ArrayList<String> plabels = new ArrayList<String>();
		int count=1;
		
		if (resultSet.hasNext()) {
		
		while(resultSet.hasNext()) {
			
			String subject="", predicate="", object="" , slabel="", plabel="", olabel="", prevPlabel = "";

			BindingSet results = resultSet.next();
			subject = results.getValue("subject").toString();
			if (results.hasBinding("slabel")){
				slabel = results.getValue("slabel").toString();
			} else {
				slabel="slabel";
			}
			
			if(results.hasBinding("property")){
				predicate = results.getValue("property").toString(); 
			} else {
				predicate = "predicate";
			}
			
			if (results.hasBinding("plabel")){
				plabel = results.getValue("plabel").stringValue();
				if (plabels.contains(plabel))
				{
					plabel = plabel+"_"+count;
					count++;
				}else{ 
					plabels.add(plabel);
				}
				
			} else {
				if(predicate.equalsIgnoreCase("predicate") || !(predicate.contains("#"))){	
					 predicate= "predicate";
				}else{
					plabel= predicate.split("#")[1];
				}
			}

			
			if (results.hasBinding("object")){
				object = results.getValue("object").toString();
				if (object.contains("/service/public/form")) {
					if(results.hasBinding("olabel")){
						olabel = results.getValue("olabel").stringValue();
					} else {
						olabel = "olabel";
					}
				} else {
					olabel = object;
				}
			} else {
				object = "object";
			}
			
			
			String sub = subject + "|" + slabel;
			String pred = predicate+ "|" + plabel;
			String obj = object+ "|" + olabel;
			
			
			if(name.equalsIgnoreCase("start")){
				name = sub;
				if(predicate.equalsIgnoreCase("predicate")  || object.equalsIgnoreCase("object") || olabel.equalsIgnoreCase("olabel")){
				} else {
					instDetail.put(pred, obj); }			
			} else if(name.equalsIgnoreCase(sub)){
				if(predicate.equalsIgnoreCase("predicate")  || object.equalsIgnoreCase("object") || olabel.equalsIgnoreCase("olabel")){
				} else {
					instDetail.put(pred, obj); 
					}
				
			} else {
				
					instances.put(name, instDetail);
					instDetail = new HashMap<String, String>();
					if(predicate.equalsIgnoreCase("predicate")  || object.equalsIgnoreCase("object") || olabel.equalsIgnoreCase("olabel")){
					} else {
						instDetail.put(pred, obj); 
						}
					
					name = sub;
					
				}	
		//	System.out.println( subject  + "	 " + slabel  + "	 " + predicate  + "	 " + plabel  + "	 " + object  + "	 " +olabel   );
			}
		instances.put(name, instDetail);
		} else {}
	
		return instances;
	}
	
	public String getAllTriples() throws RepositoryException, QueryEvaluationException,  MalformedQueryException{

		String output ="";
		
		try {			
			query = connection.prepareTupleQuery(QueryLanguage.SPARQL, 
			"PREFIX rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#> " +
			"PREFIX rdfs:<http://www.w3.org/2000/01/rdf-schema#> " +
			"PREFIX owl:<http://www.w3.org/2002/07/owl#> " +
			
			"SELECT DISTINCT ?subject ?property ?object " +
			"WHERE {" +
			"?subject ?property ?object."+"}" );
			
		} catch (MalformedQueryException e1) {
			System.out.print(e1);
			}
		
		TupleQueryResult resultSet = query.evaluate();

		while(resultSet.hasNext()) {
			
			String subject="", predicate="", object="" ;

			BindingSet results = resultSet.next();
			
			subject = results.getValue("subject").toString();
			
			predicate = results.getValue("property").toString(); 
			
			object = results.getValue("object").toString();
			
			output = output + subject + " 		" + predicate + "		"+ object+"\n";
			
		}
	
		return output;
	}
	
	public String getAllTriplesOfClass(String range) throws RepositoryException, QueryEvaluationException,  MalformedQueryException{

		String output ="";
		
		try {			
			query = connection.prepareTupleQuery(QueryLanguage.SPARQL, 
			"PREFIX rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#> " +
			"PREFIX rdfs:<http://www.w3.org/2000/01/rdf-schema#> " +
			"PREFIX owl:<http://www.w3.org/2002/07/owl#> " +
			
			"SELECT DISTINCT ?subject ?property ?object ?property2 ?object2 ?property3 ?object3 ?property4 ?object4 " +
			"WHERE {" +
			"?subject rdf:type <"+range+">." +
			"?subject ?property ?object." +
			"FILTER (!isBlank(?object))" +
			"FILTER (?object != rdfs:Resource)" +
				"OPTIONAL {?object ?property2 ?object2." +
				"FILTER (!isBlank(?object2))" +
				"FILTER (?object2 != rdfs:Resource)"  +
					"OPTIONAL {?object2 ?property3 ?object3." +
					"FILTER (!isBlank(?object3))" +
					"FILTER (?object3 != rdfs:Resource)" +
						"OPTIONAL {?object3 ?property4 ?object4." +
						"FILTER (!isBlank(?object4))" +
						"FILTER (?object4 != rdfs:Resource)" +
						"}" +
					"}" +
				"}" +
			"}" );
			
		} catch (MalformedQueryException e1) {
			System.out.print(e1);
			}
		
		TupleQueryResult resultSet = query.evaluate();

		while(resultSet.hasNext()) {
			
			String subject="", property="", object="";
			String property2="", object2="", property3="",  object3="", property4="", object4 ="";

			BindingSet results = resultSet.next();
			
			subject = results.getValue("subject").toString();			
			property = results.getValue("property").toString(); 			
			object = results.getValue("object").toString();
			if(results.hasBinding("property2")){
				property2= results.getValue("property2").toString(); 			
			}
			if(results.hasBinding("object2")){
				object2= results.getValue("object2").toString(); 			
			}
			if(results.hasBinding("property3")){
				property3= results.getValue("property2").toString(); 			
			}
			if(results.hasBinding("object3")){
				object3= results.getValue("object2").toString(); 			
			}
			if(results.hasBinding("property4")){
				property4= results.getValue("property2").toString(); 			
			}
			if(results.hasBinding("object4")){
				object4= results.getValue("object2").toString(); 			
			}
			
			
			output = output + subject + " 		" + property + "		"+ object + " 		" + property2 + "		"+ object2 + " 		" + property3 + "		"+ object3+ " 		" + property4 + "		"+ object4+"\n";
			
		}
	
		return output;
	}
	
	public String getGraph(String range) throws RepositoryException, QueryEvaluationException,  MalformedQueryException{

		String output ="";
		//GraphQueryResult graphResult=null;
		ByteArrayOutputStream byteout = new ByteArrayOutputStream();
		TurtleWriter turtleWriter = new TurtleWriter(byteout);
		RDFXMLWriter rdfWriter = new RDFXMLWriter(byteout);
		
		try {	
			try {
				connection.prepareGraphQuery(QueryLanguage.SPARQL, 
					      
				"PREFIX rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#> " +
				"PREFIX rdfs:<http://www.w3.org/2000/01/rdf-schema#> " +
				"PREFIX owl:<http://www.w3.org/2002/07/owl#> " +
				
				"Construct {" +
				"?subject ?property ?object." +
				//"?object ?property2 ?object2." +
				//"?object2 ?property3 ?object3." +
				//"?object3 ?property4 ?object4." +
				"}" +
			//	"DESCRIBE ?subject " +
				"WHERE {" +
				"?subject rdf:type <"+range+">." +
				"?subject ?property ?object." +
				"FILTER (!isBlank(?object))" +
				"FILTER (?object != rdfs:Resource)"+
			//	"OPTIONAL {?object ?property2 ?object2." +
//						"OPTIONAL {?object2 ?property3 ?object3." +
//							"OPTIONAL {?object3 ?property4 ?object4.}" +
//						"}" +
				//	"}" +
				"}").evaluate(rdfWriter);
			} catch (RDFHandlerException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		} catch (MalformedQueryException e1) {
			System.out.print(e1);
		}
		
	//	TupleQueryResult resultSet = query.evaluate();


		output = byteout.toString();
	
	
		return output;
	}
	
	public String getCompleteGraph() throws RepositoryException, QueryEvaluationException,  MalformedQueryException{

		String output ="";
		//GraphQueryResult graphResult=null;
		ByteArrayOutputStream byteout = new ByteArrayOutputStream();
		TurtleWriter turtleWriter = new TurtleWriter(byteout);
		RDFXMLWriter rdfWriter = new RDFXMLWriter(byteout);
		
		try {	
			try {
				connection.prepareGraphQuery(QueryLanguage.SPARQL, 
					      
				"PREFIX rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#> " +
				"PREFIX rdfs:<http://www.w3.org/2000/01/rdf-schema#> " +
				"PREFIX owl:<http://www.w3.org/2002/07/owl#> " +
				
				"Construct {" +
				"?subject ?property ?object." +
				"}" +
				"WHERE {" +
				"?subject ?property ?object." +
				"FILTER (!isBlank(?object))" +
				"FILTER (?object != rdfs:Resource)"+
				"}").evaluate(rdfWriter);
			} catch (RDFHandlerException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		} catch (MalformedQueryException e1) {
			System.out.print(e1);
		}
		
	//	TupleQueryResult resultSet = query.evaluate();


		output = byteout.toString();
	
	
		return output;
	}


}