package ie.deri.raul.model.raul;

import java.util.Set;

import org.openrdf.query.QueryEvaluationException;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;

public class Listbox extends Widget {

	public Listbox(RepositoryConnection con, String listbox) {
		// TODO Auto-generated constructor stub
		super(con,listbox);
	}

	public Set<Boolean> getRaulMultiples() throws RepositoryException, QueryEvaluationException{
		return super.getRaulBooleanValues("multiple");}
}
