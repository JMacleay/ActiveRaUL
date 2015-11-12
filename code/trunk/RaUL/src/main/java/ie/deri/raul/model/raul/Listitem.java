package ie.deri.raul.model.raul;

import java.util.Set;

import org.openrdf.query.QueryEvaluationException;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;


public class Listitem extends Widget{

	public Listitem(RepositoryConnection con, String listItem) {
		// TODO Auto-generated constructor stub
		super(con,listItem);
	}

	public Set<Boolean> getRaulSelected() throws RepositoryException, QueryEvaluationException{
		return super.getRaulBooleanValues("selected");}
}
