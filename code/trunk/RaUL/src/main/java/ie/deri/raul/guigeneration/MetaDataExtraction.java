package ie.deri.raul.guigeneration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.openrdf.query.BindingSet;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.repository.RepositoryConnection;

public class MetaDataExtraction {

	private TupleQuery query= null;
	RepositoryConnection con ;
	//JSONObject json = new JSONObject();
	
	
	public MetaDataExtraction(RepositoryConnection connection) {
		// TODO Auto-generated constructor stub
		con = connection;
		//getOntologyDetail(baseURI);
	}

	/*public JSONObject getOntologyDetail(String baseURI){
		try{
			List<String> comments=getOntologyComments(baseURI);
			List<String> classes=getOntologyClasses(baseURI);
		}catch(Exception e){
			System.out.println(e);
		}
        // Passing a number to toString() adds indentation
        //System.out.println( "JSON: " + json.toString(2) );
        return null;

	}*/
	
	public List<String> getOntologyComments(String baseURI) throws Exception{
		List<String> comments=new ArrayList<String>();
		try {	
			query = con.prepareTupleQuery(QueryLanguage.SPARQL, 
			"PREFIX rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#> " +
			"PREFIX rdfs:<http://www.w3.org/2000/01/rdf-schema#> " +
			"PREFIX owl:<http://www.w3.org/2002/07/owl#> " +
			"SELECT DISTINCT ?comments " +
			"WHERE {<"+baseURI+"> rdfs:comment ?comments.}"
			);
		} catch (MalformedQueryException e1) {
			e1.printStackTrace();}
		
		TupleQueryResult resultSet = query.evaluate();

		while(resultSet.hasNext()) {
			BindingSet results = resultSet.next();
			comments.add(results.getValue("comments").stringValue());
		}
		return comments;
	}
	
	
	/*public void getOntologyClasses(String baseURI) throws Exception{
		
		System.out.println(baseURI);
		
		try {	
			query = con.prepareTupleQuery(QueryLanguage.SPARQL, 
			"PREFIX rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#> " +
			"PREFIX rdfs:<http://www.w3.org/2000/01/rdf-schema#> " +
			"PREFIX owl:<http://www.w3.org/2002/07/owl#> " +
			"SELECT DISTINCT ?class ?comment " +
			"WHERE { { ?class rdf:type owl:Class "+
			"FILTER regex(str(?class), \""+baseURI+"\") } " +
			" MINUS " +
			"{?class rdfs:subClassOf ?superClass "+
			"FILTER regex(str(?superClass), \""+baseURI+"\")" +
			"FILTER (?class != ?supperClass ) }" +
			"?class rdfs:comment ?comment}" +
			"ORDER BY ?class "
			);
		} catch (MalformedQueryException e1) {
			e1.printStackTrace();}
		
		TupleQueryResult resultSet = query.evaluate();

		while(resultSet.hasNext()) {
			JSONObject classes = new JSONObject();
	       
			BindingSet results = resultSet.next();
			
			String classURI = results.getValue("class").stringValue();
			String comment = results.getValue("comment").stringValue();
			classes.put( "classURI", classURI);
		    classes.put( "classComment", comment);
			
		    json.accumulate("classes", classes);
		}
	}*/
	
	public HashMap<String, String> getOntologyClasses(String baseURI) throws Exception{
		
		System.out.println("fff : " + baseURI);
		HashMap<String, String> classes=new HashMap<String, String>();
		try {	
			/*query = con.prepareTupleQuery(QueryLanguage.SPARQL, 
			"PREFIX rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#> " +
			"PREFIX rdfs:<http://www.w3.org/2000/01/rdf-schema#> " +
			"PREFIX owl:<http://www.w3.org/2002/07/owl#> " +
			"SELECT DISTINCT ?subClass ?comment " +
			"WHERE { {?subClass rdf:type owl:Class. "+
			"FILTER regex(str(?subClass), \""+baseURI+"\") }" +
					" MINUS " +
					"{?subClass rdfs:subClassOf ?superClass. " +
					"FILTER regex(str(?superClass), \""+baseURI+"\")" +
					"FILTER (?subClass != ?superClass ) }" +
			"Optional {?subClass rdfs:comment ?comment}"+
			"}" +
			"ORDER BY ?subClass "
			);*/
			
			query = con.prepareTupleQuery(QueryLanguage.SPARQL, 
			"PREFIX rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#> " +
			"PREFIX rdfs:<http://www.w3.org/2000/01/rdf-schema#> " +
			"PREFIX owl:<http://www.w3.org/2002/07/owl#> " +
			"SELECT DISTINCT ?subClass ?comment " +
			"WHERE { ?subClass rdf:type owl:Class. " +
		//	"?subClass rdfs:subClassOf ?restriction." +
		//	"?restriction owl:onProperty ?property. "+
		//	"Filter (!bound(?property)) " +
			"Optional {?subClass rdfs:comment ?comment}." +
			"FILTER regex(str(?subClass), \""+baseURI+"\") " +
			"} ORDER BY ?subClass "
			);
		} catch (MalformedQueryException e1) {
			e1.printStackTrace();}
		
		TupleQueryResult resultSet = query.evaluate();

		while(resultSet.hasNext()) {
			BindingSet results = resultSet.next();
			String classURI = results.getValue("subClass").stringValue();
			String comment = "";
			
			if(results.hasBinding("comment")){
				comment = results.getValue("comment").stringValue();
			}
			classes.put(classURI, comment);
	//		classes.add(results.getValue("class").stringValue());
		}
		return classes;
	}

}
