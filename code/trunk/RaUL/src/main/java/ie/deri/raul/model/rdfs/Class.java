package ie.deri.raul.model.rdfs;

import java.util.Set;

import org.openrdf.query.QueryEvaluationException;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;


public class Class extends Resource {
	
	private boolean isAssociatedWithDynamicClass=false;
	
	//private String assoicatedClassUri;
	
	private String associatedClassId;
	
	public String getAssociatedClassId() {
		return associatedClassId;
	}

	public void setAssociatedClassId(String associatedClassId) {
		this.associatedClassId = associatedClassId;
	}

	/*public String getAssoicatedClassUri() {
		return assoicatedClassUri;
	}

	public void setAssoicatedClassUri(String assoicatedClassUri) {
		this.assoicatedClassUri = assoicatedClassUri;
	}*/

	public boolean isAssociatedWithDynamicClass() {
		return isAssociatedWithDynamicClass;
	}

	public void setAssociatedWithDynamicClass(boolean isAssociatedWithDynamicClass) {
		this.isAssociatedWithDynamicClass = isAssociatedWithDynamicClass;
	}

	public Class(RepositoryConnection con, String classURI) {
		// TODO Auto-generated constructor stub
		super(con, classURI);
	}
	
	public Set<Class> getRdfsSubClassOf() throws RepositoryException, QueryEvaluationException{
		return super.getClassValues("rdfs" , "subClassOf"); }
}
