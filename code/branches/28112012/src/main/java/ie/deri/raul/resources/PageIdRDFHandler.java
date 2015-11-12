package ie.deri.raul.resources;

import org.openrdf.model.Statement;
import org.openrdf.rio.RDFHandler;
import org.openrdf.rio.RDFHandlerException;

public class PageIdRDFHandler implements RDFHandler {
	
	private static final String RAUL_PAGE = "http://purl.org/NET/raul#Page";
	private static final String RDF_TYPE = "http://www.w3.org/1999/02/22-rdf-syntax-ns#type";
	
	private String _pageId;
	
	@Override
	public void startRDF() throws RDFHandlerException {	}
	
	@Override
	public void endRDF() throws RDFHandlerException {	}

	@Override
	public void handleComment(String cmt) throws RDFHandlerException { }

	@Override
	public void handleNamespace(String prefix, String uri)
			throws RDFHandlerException { }

	@Override
	public void handleStatement(Statement stmt) throws RDFHandlerException {		
		if (RDF_TYPE.equals(stmt.getPredicate().stringValue()) &&
				RAUL_PAGE.equals(stmt.getObject().stringValue())) {
			_pageId = stmt.getSubject().stringValue();
		}		
	}	
	
	public String getPageId() {
		return _pageId;
	}

}
