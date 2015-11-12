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


public class WidgetContainer extends Widget{

	
	public WidgetContainer(RepositoryConnection con, String widgetId) {
		// TODO Auto-generated constructor stub
		super(con , widgetId);
	}

	public Set<String> getRaulActions() throws RepositoryException, QueryEvaluationException {
		return super.getRaulStringValues("action");
	}
	
	public Set<CRUDOperation> getRaulMethods() throws RepositoryException, QueryEvaluationException {
		Set<CRUDOperation> results = new HashSet<CRUDOperation>();
		TupleQuery query = null;
		try {
			query = connection.prepareTupleQuery(QueryLanguage.SPARQL, "" +
					"PREFIX rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#> " +
					"PREFIX rdfs:<http://www.w3.org/2000/01/rdf-schema#> " +
					"PREFIX owl:<http://www.w3.org/2002/07/owl#> " +
					"PREFIX raul:<http://purl.org/NET/raul#> " +
					"SELECT Distinct ?value WHERE {<"+this.getPageId()+"> raul:method ?value}");
			
		} catch (MalformedQueryException e1) {
			e1.printStackTrace();
		}
		TupleQueryResult resultSet = query.evaluate();
		while (resultSet.hasNext()) {
			BindingSet result = resultSet.next();
			CRUDOperation value = new CRUDOperation(result.getValue("value").toString());
			results.add(value);
		}	
		return results; 
	}
	
}
