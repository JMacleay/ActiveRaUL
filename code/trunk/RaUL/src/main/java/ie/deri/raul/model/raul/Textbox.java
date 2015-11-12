package ie.deri.raul.model.raul;

import java.util.Set;

import org.openrdf.query.QueryEvaluationException;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;

public class Textbox extends Widget{

	public Textbox(RepositoryConnection con, String textboxId) {
		// TODO Auto-generated constructor stub
		super(con , textboxId);
	}

	public Set<Boolean> getRaulIsPassword() throws RepositoryException, QueryEvaluationException{
		return super.getRaulBooleanValues("isPassword");}
	
	public Set<Integer> getRaulMaxlengths() throws RepositoryException, QueryEvaluationException{
		return super.getRaulIntegerValues("maxlength") ;}
	
	public Set<String> getRaulSizes() throws RepositoryException, QueryEvaluationException{
		return super.getRaulStringValues("size") ;}
}
