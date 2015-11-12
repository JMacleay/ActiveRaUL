package ie.deri.raul.model.rdf;

import java.util.Set;

import org.openrdf.query.QueryEvaluationException;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;


import ie.deri.raul.model.rdfs.Resource;

public class List extends Resource{

	public List(RepositoryConnection con, String resourceURI) {
		// TODO Auto-generated constructor stub
		super(con, resourceURI);
	}

	public Set<String> getRdfFirsts() throws RepositoryException, QueryEvaluationException{
		return getObjectValues("rdf" , "first"); }
	
	public Set<List> getRdfRests() throws RepositoryException, QueryEvaluationException{
		return getListValues("rdf" , "rest"); }
}
