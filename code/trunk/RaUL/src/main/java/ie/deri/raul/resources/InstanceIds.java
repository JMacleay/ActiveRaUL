package ie.deri.raul.resources;

import java.io.Serializable;

public class InstanceIds implements Serializable {

	private static final long serialVersionUID = 7568375537968118170L;
	
	private String _context;
	private String _iri;
	
	private String baseUri;
	
	private String owlClass;
	
	public String getOwlClass() {
		return owlClass;
	}

	public void setOwlClass(String owlClass) {
		this.owlClass = owlClass;
	}

	public String getBaseUri() {
		return baseUri;
	}

	public void setBaseUri(String baseUri) {
		this.baseUri = baseUri;
	}

	public InstanceIds() {	}
	
	public InstanceIds(String uri, String context) {
		_iri = uri;
		_context = context;
	}
	
	public void setContext(String context) {
		_context = context;
	}
	
	public String getContext() {
		return _context;
	}
	
	public void setIri(String iri) {
		_iri = iri;
	}
	
	public String getIri() {
		return _iri;
	}
	
	
	
}
