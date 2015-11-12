package ie.deri.raul.model.raul;

import java.util.Set;

import org.openrdf.query.QueryEvaluationException;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;

public class Button extends Widget{

	public Button(RepositoryConnection con, String buttonId) {
		// TODO Auto-generated constructor stub
		super(con, buttonId);
	}

	public Set<Boolean> getRaulChecked() throws RepositoryException, QueryEvaluationException{
		return super.getRaulBooleanValues("checked");}
	
	public Set<String> getRaulCommands() throws RepositoryException, QueryEvaluationException{
		return super.getRaulStringValues("command");}
	
}
