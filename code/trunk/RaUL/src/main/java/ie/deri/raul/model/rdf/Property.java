package ie.deri.raul.model.rdf;

import java.util.Set;

import org.openrdf.query.QueryEvaluationException;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import ie.deri.raul.model.rdfs.Class;
import ie.deri.raul.model.rdfs.Resource;

public class Property extends Resource{

	public Property(RepositoryConnection con, String resourceURI) {
		// TODO Auto-generated constructor stub
		super(con, resourceURI);
	}

	public Set<Class> getRdfsDomains() throws RepositoryException, QueryEvaluationException{
		return getClassValues("rdfs" , "domain"); }
	
	public Set<Class> getRdfsRanges() throws RepositoryException, QueryEvaluationException{
		return getClassValues("rdfs" , "range"); }
	
	public Set<Property> getRdfsSubPropertyOf() throws RepositoryException, QueryEvaluationException{
		return getPropertyValues("rdfs" , "subPropertyOf"); }
	
}
