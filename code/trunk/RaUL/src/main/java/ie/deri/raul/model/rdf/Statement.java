package ie.deri.raul.model.rdf;

import java.util.Set;

import org.openrdf.query.QueryEvaluationException;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;

import ie.deri.raul.model.rdfs.Resource;

public class Statement extends Resource{

	public Statement(RepositoryConnection con, String resourceURI) {
		// TODO Auto-generated constructor stub
		super(con, resourceURI);
	}

	public Set<String> getRdfObjects() throws RepositoryException, QueryEvaluationException{
		return getObjectValues("rdf" , "object"); }
	
	public Set<String> getRdfPredicates() throws RepositoryException, QueryEvaluationException{
		return getObjectValues("rdf" , "predicate"); }
	
	public Set<String> getRdfSubjects() throws RepositoryException, QueryEvaluationException{
		return getObjectValues("rdf" , "subject"); }
	
}
