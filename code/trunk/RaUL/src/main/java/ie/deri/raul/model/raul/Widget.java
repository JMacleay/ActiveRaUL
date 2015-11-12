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

public class Widget extends Page {
	
	public Widget(RepositoryConnection con , String widgetId) {
		// TODO Auto-generated constructor stub
		super(con , widgetId);
		
	}
	
	public Set<Boolean> getRaulDisabled() throws RepositoryException, QueryEvaluationException{
		return super.getRaulBooleanValues("disabled");}
	
	public Set<Boolean> getRaulHiddens() throws RepositoryException, QueryEvaluationException{
		return super.getRaulBooleanValues("hidden");}
	
	public Set<Boolean> getRaulIsIdentifier() throws RepositoryException, QueryEvaluationException{
		return super.getRaulBooleanValues("isIdentifier");}
	
	public Set<Integer> getRaulRows() throws RepositoryException, QueryEvaluationException{
		return super.getRaulIntegerValues("row"); }
	
	public Set<String> getRaulLabels() throws RepositoryException, QueryEvaluationException{
		Set<String> results = new HashSet<String>();
		TupleQuery query = null;
		try {
			String queryString = "PREFIX rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#> " +
					"PREFIX rdfs:<http://www.w3.org/2000/01/rdf-schema#> " +
					"PREFIX owl:<http://www.w3.org/2002/07/owl#> " +
					"PREFIX raul:<http://purl.org/NET/raul#> " +
					"SELECT Distinct ?value WHERE {<"+super.getPageId()+"> raul:label ?value}";
			//System.out.println(queryString);
			
			query = connection.prepareTupleQuery(QueryLanguage.SPARQL, queryString);
			
		} catch (MalformedQueryException e1) {
			e1.printStackTrace();
		}
		TupleQueryResult resultSet = query.evaluate();
		while (resultSet.hasNext()) {
			BindingSet result = resultSet.next();
			String[] valueString = result.getValue("value").toString().split("\\^");
			String value = valueString[0].replaceAll("\"","");
			results.add(value);
		}
		return results;
		}
	
	public Set<String> getRaulNames() throws RepositoryException, QueryEvaluationException{
		return super.getRaulStringValues("name"); }
	
	public Set<String> getRaulValues() throws RepositoryException, QueryEvaluationException{
		return super.getRaulStringValues("value");
	}
	
	public Set<Group> getRaulGroups() throws RepositoryException, QueryEvaluationException{
		Set<ie.deri.raul.model.raul.Group> results = new HashSet<ie.deri.raul.model.raul.Group>();
		TupleQuery query = null;
		try {
			query = connection.prepareTupleQuery(QueryLanguage.SPARQL, "" +
					"PREFIX rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#> " +
					"PREFIX rdfs:<http://www.w3.org/2000/01/rdf-schema#> " +
					"PREFIX owl:<http://www.w3.org/2002/07/owl#> " +
					"PREFIX raul:<http://purl.org/NET/raul#> " +
					"SELECT Distinct ?value WHERE {<"+this.getPageId()+"> raul:group ?value}");
			
		} catch (MalformedQueryException e1) {
			e1.printStackTrace();
		}
		TupleQueryResult resultSet = query.evaluate();
		while (resultSet.hasNext()) {
			BindingSet result = resultSet.next();
			Group value = new Group(connection, result.getValue("value").toString());
			results.add(value);
		}	
		return results; 
	}
	
	public Set<Boolean> isWidgetMultiple() throws RepositoryException, QueryEvaluationException{
		return super.getRaulBooleanValues("multiple");
	}
	
	public String getPropertyType() throws RepositoryException, QueryEvaluationException{
		/*TupleQuery query = null;
		String dataType=null;
		try {
			query = connection.prepareTupleQuery(QueryLanguage.SPARQL, "" +
					"PREFIX rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#> " +
					"PREFIX rdfs:<http://www.w3.org/2000/01/rdf-schema#> " +
					"PREFIX raul:<http://purl.org/NET/raul#> "+
					"SELECT Distinct ?value " +
					"WHERE {<"+this.pageId+"> rdfs:comment ?value}");
			TupleQueryResult resultSet = query.evaluate();
			if(resultSet.hasNext()){
				dataType= resultSet.next().getValue("value").stringValue();
				resultSet.close();
			}else{
				resultSet.close();
				return null;
			}
		} catch (MalformedQueryException e1) {
			e1.printStackTrace();
		}
		return dataType;*/
		return super.getRdfsStringValue("rdfs:comment");
	}
	
	public String getRange() throws RepositoryException, QueryEvaluationException{
		return super.getRdfsStringValue("rdfs:range");
	}
        
        public Set<Integer> getLevel() throws RepositoryException, QueryEvaluationException{
               Set<Integer> set=super.getRaulIntegerValues("level");
               if(set.size()==0){
                   Set<Integer> newSet=new HashSet<Integer>();
                   newSet.add(1);
                   return newSet;
               }
               return set;
        }

}
