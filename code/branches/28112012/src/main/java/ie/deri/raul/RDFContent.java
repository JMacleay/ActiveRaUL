package ie.deri.raul;

import org.openrdf.rio.RDFFormat;

public class RDFContent {

	private RDFFormat _format;
	private String _content;
	
	public RDFContent(RDFFormat format, String content) {
		_format = format;
		_content = content;
	}

	public RDFFormat getFormat() {
		return _format;
	}

	public void setFormat(RDFFormat format) {
		_format = format;
	}

	public String getContent() {
		return _content;
	}

	public void setContent(String content) {
		_content = content;
	}
	
	
	
}
