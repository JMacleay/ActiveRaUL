package ie.deri.raul.model.raul;

import java.util.HashSet;
import java.util.Set;

import org.openrdf.query.BindingSet;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;


public class Page {
	protected RepositoryConnection connection;
	protected String pageId;
	
	//private Set<String> results = new HashSet<String>();
	//private TupleQuery query = null;
	
	public Page(RepositoryConnection con, String pageId){
		this.connection = con;
		this.pageId = pageId.replaceAll("\"", "");
	}
	
	public String getPageId(){
		return this.pageId.replaceAll("\"", "");
	}
		
	public Set<String> getRaulClasses() throws RepositoryException, QueryEvaluationException{
		return getRaulStringValues("class");
		//return results;
	} 
	public Set<String> getRaulIds() throws RepositoryException, QueryEvaluationException{
		return getRaulStringValues("id");
		//return results;
	} 
	public Set<ie.deri.raul.model.rdfs.Class> getRaulLists() throws RepositoryException, QueryEvaluationException{
		return getRaulClassValues("list");
		//return results;
	} 
	
	public Set<String> getRaulTitles() throws RepositoryException, QueryEvaluationException{
		return getRaulStringValues("title");
		//return results;
	} 
	
	
	protected Set<String> getRaulStringValues(String predicate) throws RepositoryException, QueryEvaluationException{
		Set<String> results = new HashSet<String>();
		TupleQuery query = null;
		try {
			query = connection.prepareTupleQuery(QueryLanguage.SPARQL, "" +
					"PREFIX rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#> " +
					"PREFIX rdfs:<http://www.w3.org/2000/01/rdf-schema#> " +
					"PREFIX owl:<http://www.w3.org/2002/07/owl#> " +
					"PREFIX raul:<http://purl.org/NET/raul#> " +
					"SELECT Distinct ?value WHERE {<"+pageId+"> raul:"+predicate+" ?value}");
			
		} catch (MalformedQueryException e1) {
			e1.printStackTrace();
		}
		TupleQueryResult resultSet = query.evaluate();
		while (resultSet.hasNext()) {
			BindingSet result = resultSet.next();
			String value = result.getValue("value").toString().replaceAll("\"", "");
			results.add(value);
		}
		return results;
	} 	
	
	protected Set<Integer> getRaulIntegerValues(String predicate) throws RepositoryException, QueryEvaluationException{
		Set<Integer> results = new HashSet<Integer>();
		TupleQuery query = null;
		try {
			query = connection.prepareTupleQuery(QueryLanguage.SPARQL, "" +
					"PREFIX rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#> " +
					"PREFIX rdfs:<http://www.w3.org/2000/01/rdf-schema#> " +
					"PREFIX owl:<http://www.w3.org/2002/07/owl#> " +
					"PREFIX raul:<http://purl.org/NET/raul#> " +
					"SELECT Distinct ?value WHERE {<"+pageId+"> raul:"+predicate+" ?value}");
			
		} catch (MalformedQueryException e1) {
			e1.printStackTrace();
		}
		TupleQueryResult resultSet = query.evaluate();
		while (resultSet.hasNext()) {
			BindingSet result = resultSet.next();
			String value = result.getValue("value").toString().replaceAll("\"", "");
			Integer intVal = Integer.parseInt(value.split("\\^")[0]);
			results.add(intVal);
		}
		return results;
	} 	
	
	protected Set<Boolean> getRaulBooleanValues(String predicate) throws RepositoryException, QueryEvaluationException{
		Set<Boolean> results = new HashSet<Boolean>();
		TupleQuery query = null;
		try {
			query = connection.prepareTupleQuery(QueryLanguage.SPARQL, "" +
					"PREFIX rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#> " +
					"PREFIX rdfs:<http://www.w3.org/2000/01/rdf-schema#> " +
					"PREFIX owl:<http://www.w3.org/2002/07/owl#> " +
					"PREFIX raul:<http://purl.org/NET/raul#> " +
					"SELECT Distinct ?value WHERE {<"+pageId+"> raul:"+predicate+" ?value}");
			
		} catch (MalformedQueryException e1) {
			e1.printStackTrace();
		}
		TupleQueryResult resultSet = query.evaluate();
		while (resultSet.hasNext()) {
			BindingSet result = resultSet.next();
			String value = result.getValue("value").toString().replaceAll("\"", "");
			boolean boolVal = Boolean.parseBoolean(value.split("\\^")[0]);
			results.add(boolVal);
		}
		return results;
	} 	
	
	protected Set<ie.deri.raul.model.rdfs.Class> getRaulClassValues(String predicate) throws RepositoryException, QueryEvaluationException{
		Set<ie.deri.raul.model.rdfs.Class> results = new HashSet<ie.deri.raul.model.rdfs.Class>();
		TupleQuery query = null;
		try {
			query = connection.prepareTupleQuery(QueryLanguage.SPARQL, "" +
					"PREFIX rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#> " +
					"PREFIX rdfs:<http://www.w3.org/2000/01/rdf-schema#> " +
					"PREFIX owl:<http://www.w3.org/2002/07/owl#> " +
					"PREFIX raul:<http://purl.org/NET/raul#> " +
					"SELECT Distinct ?value WHERE {<"+pageId+"> raul:"+predicate+" ?value}");
			
		} catch (MalformedQueryException e1) {
			e1.printStackTrace();
		}
		TupleQueryResult resultSet = query.evaluate();
		while (resultSet.hasNext()) {
			BindingSet result = resultSet.next();
			ie.deri.raul.model.rdfs.Class value = new ie.deri.raul.model.rdfs.Class(connection , result.getValue("value").toString().replaceAll("\"", ""));
			System.out.print(value.toString());
			results.add(value);
		}
		return results;
	} 
	
	
	protected String getRdfsStringValue(String predicate) throws RepositoryException, QueryEvaluationException{
		TupleQuery query = null;
		String dataType=null;
		try {
			query = connection.prepareTupleQuery(QueryLanguage.SPARQL, "" +
					"PREFIX rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#> " +
					"PREFIX rdfs:<http://www.w3.org/2000/01/rdf-schema#> " +
					"PREFIX raul:<http://purl.org/NET/raul#> "+
					"SELECT Distinct ?value " +
					"WHERE {<"+this.pageId+"> "+predicate+" ?value}");
			TupleQueryResult resultSet = query.evaluate();
			if(resultSet.hasNext()){
				dataType= resultSet.next().getValue("value").stringValue();	
			}
			resultSet.close();
		} catch (MalformedQueryException e1) {
			e1.printStackTrace();
		}
		return dataType;
	}
}
