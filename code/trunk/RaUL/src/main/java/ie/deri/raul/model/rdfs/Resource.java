package ie.deri.raul.model.rdfs;

import ie.deri.raul.model.rdf.List;
import ie.deri.raul.model.rdf.Property;

import java.util.ArrayList;
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

public class Resource {

	protected RepositoryConnection connection;
	protected String resourceURI;
	
	public Resource(RepositoryConnection con, String uri){
		// TODO Auto-generated constructor stub
		this.connection = con;
		this.resourceURI = uri.replace("\"", "");
	}
	
	public String getResourceURI(){
		return this.resourceURI;}
	
	public Set<Class> getRdfTypes() throws RepositoryException, QueryEvaluationException{
		return getClassValues("rdf" , "type"); }
	
	public Set<String> getRdfValues() throws RepositoryException, QueryEvaluationException{
		return getObjectValues("rdf" , "value"); }
	
	public Set<String> getRdfsComments() throws RepositoryException, QueryEvaluationException{
		return getObjectValues("rdfs" , "comment"); }
	
	public Set<String> getRdfsIsDefinedBy() throws RepositoryException, QueryEvaluationException{
		return getObjectValues("rdfs" , "isDefinedBy"); }
	
	public Set<String> getRdfsLabels() throws RepositoryException, QueryEvaluationException{
		
		Set<String> results = new HashSet<String>();
		TupleQuery query = null;
		try {
			String queryString = "PREFIX rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#> " +
					"PREFIX rdfs:<http://www.w3.org/2000/01/rdf-schema#> " +
					"PREFIX owl:<http://www.w3.org/2002/07/owl#> " +
					"PREFIX raul:<http://purl.org/NET/raul#> " +
					"SELECT Distinct ?value WHERE {<"+resourceURI+"> rdfs:label ?value}";
			//System.out.println(queryString);
			
			query = connection.prepareTupleQuery(QueryLanguage.SPARQL, queryString);
			
		} catch (MalformedQueryException e1) {
			e1.printStackTrace();
		}
		TupleQueryResult resultSet = query.evaluate();
		while (resultSet.hasNext()) {
			BindingSet result = resultSet.next();
			String[] valueString = result.getValue("value").toString().split("\\^^");
			String value = valueString[0];
			results.add(value);
		}
		return results;
	}
	
	public Set<String> getRdfsMembers() throws RepositoryException, QueryEvaluationException{
		return getObjectValues("rdfs" , "member"); }
	
	public ArrayList<String> getRdfsMembersInSeq() throws RepositoryException, QueryEvaluationException{
		return getMemberValues(); }
	
	public Set<String> getRdfsSeeAlso() throws RepositoryException, QueryEvaluationException{
		return getObjectValues("rdfs" , "seeAlso"); }


	protected Set<String> getObjectValues(String prefix, String predicate) throws RepositoryException, QueryEvaluationException{
		Set<String> results = new HashSet<String>();
		TupleQuery query = null;
		try {
			String queryString = "PREFIX rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#> " +
					"PREFIX rdfs:<http://www.w3.org/2000/01/rdf-schema#> " +
					"PREFIX owl:<http://www.w3.org/2002/07/owl#> " +
					"PREFIX raul:<http://purl.org/NET/raul#> " +
					"SELECT Distinct ?value WHERE {<"+resourceURI+"> "+prefix+":"+predicate+" ?value}";
			//System.out.println(queryString);
			
			query = connection.prepareTupleQuery(QueryLanguage.SPARQL, queryString);
			
		} catch (MalformedQueryException e1) {
			e1.printStackTrace();
		}
		TupleQueryResult resultSet = query.evaluate();
		while (resultSet.hasNext()) {
			BindingSet result = resultSet.next();
			String value = result.getValue("value").toString().replaceAll("\"","");
			results.add(value);
		}
		return results;
	} 
	
	protected ArrayList<String> getMemberValues() throws RepositoryException, QueryEvaluationException{
		ArrayList<String> results = new ArrayList<String>();
		TupleQuery query = null;
		try {
			String queryString = "PREFIX rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#> " +
					"PREFIX rdfs:<http://www.w3.org/2000/01/rdf-schema#> " +
					"PREFIX owl:<http://www.w3.org/2002/07/owl#> " +
					"PREFIX raul:<http://purl.org/NET/raul#> " +
					"SELECT Distinct ?value WHERE {<"+resourceURI+"> ?seqNo ?value." +
							"?seqNo rdfs:subPropertyOf rdfs:member." +
							"}" +
							" ORDER BY (?seqNo) ";
			//query//(queryString);
			
			query = connection.prepareTupleQuery(QueryLanguage.SPARQL, queryString);
			
		} catch (MalformedQueryException e1) {
			e1.printStackTrace();
		}
		String submit="";
		TupleQueryResult resultSet = query.evaluate();
		while (resultSet.hasNext()) {
			BindingSet result = resultSet.next();
			String value = result.getValue("value").toString();
			if(value.contains("#submit")){
				submit = value;
			} else {
				results.add(value);
			}
		}
		//System.out.println(results);
		if(submit.contains("#submit")){
			results.add(submit);
		}
		return results;
	} 	
	
	
	protected Set<Class> getClassValues(String prefix, String predicate) throws RepositoryException, QueryEvaluationException{
		Set<Class> results = new HashSet<Class>();
		TupleQuery query = null;
		try {
			query = connection.prepareTupleQuery(QueryLanguage.SPARQL, "" +
					"PREFIX rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#> " +
					"PREFIX rdfs:<http://www.w3.org/2000/01/rdf-schema#> " +
					"PREFIX owl:<http://www.w3.org/2002/07/owl#> " +
					"PREFIX raul:<http://purl.org/NET/raul#> " +
					"SELECT Distinct ?value WHERE {<"+resourceURI+"> "+prefix+":"+predicate+" ?value}");
			
		} catch (MalformedQueryException e1) {
			e1.printStackTrace();
		}
		TupleQueryResult resultSet = query.evaluate();
		while (resultSet.hasNext()) {
			BindingSet result = resultSet.next();
			Class value = new Class(connection , result.getValue("value").toString());
			results.add(value);
		}
		return results;
	} 	
	
	protected Set<Property> getPropertyValues(String prefix, String predicate) throws RepositoryException, QueryEvaluationException{
		Set<Property> results = new HashSet<Property>();
		TupleQuery query = null;
		try {
			query = connection.prepareTupleQuery(QueryLanguage.SPARQL, "" +
					"PREFIX rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#> " +
					"PREFIX rdfs:<http://www.w3.org/2000/01/rdf-schema#> " +
					"PREFIX owl:<http://www.w3.org/2002/07/owl#> " +
					"PREFIX raul:<http://purl.org/NET/raul#> " +
					"SELECT Distinct ?value WHERE {<"+resourceURI+"> "+prefix+":"+predicate+" ?value}");
			
		} catch (MalformedQueryException e1) {
			e1.printStackTrace();
		}
		TupleQueryResult resultSet = query.evaluate();
		while (resultSet.hasNext()) {
			BindingSet result = resultSet.next();
			Property value = new Property(connection , result.getValue("value").toString());
			results.add(value);
		}
		return results;
	} 
	
	protected Set<List> getListValues(String prefix, String predicate) throws RepositoryException, QueryEvaluationException{
		Set<List> results = new HashSet<List>();
		TupleQuery query = null;
		try {
			query = connection.prepareTupleQuery(QueryLanguage.SPARQL, "" +
					"PREFIX rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#> " +
					"PREFIX rdfs:<http://www.w3.org/2000/01/rdf-schema#> " +
					"PREFIX owl:<http://www.w3.org/2002/07/owl#> " +
					"PREFIX raul:<http://purl.org/NET/raul#> " +
					"SELECT Distinct ?value WHERE {<"+resourceURI+"> "+prefix+":"+predicate+" ?value} ");
			
		} catch (MalformedQueryException e1) {
			e1.printStackTrace();
		}
		TupleQueryResult resultSet = query.evaluate();
		while (resultSet.hasNext()) {
			BindingSet result = resultSet.next();
			List value = new List(connection , result.getValue("value").toString());
			results.add(value);
		}
		return results;
	} 
}
