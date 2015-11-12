package ie.deri.raul.guigeneration;

public class RDFBean {

	/**
	 * @param args
	 */
	private String uri="";
	private String type ="";
	private String range="";
	private String subject="";
	private String property="";
	private String object="";

	public RDFBean(String uri, String type, String range, String subject, String property, String object){
		this.uri=uri;
		this.type = type;
		this.range = range;
		this.subject = subject;
		this.property = property;
		this.object = object;
	} 
	
	public RDFBean(){
		this.uri="";
		this.type = "";
		this.range = "";
		this.subject = "";
		this.property = "";
		this.object = "";
	} 

	public void setURI(String uri){
		this.uri = uri;
	}
	public void setType(String type){
		this.type = type;
	}
	public void setRange(String range){
		this.range = range;
	}
	public void setSubject(String sub){
		this.subject = sub;
	}
	public void setProperty(String prop){
		this.property = prop;
	}
	public void setObject(String obj){
		this.object = obj;
	}

	public String getURI(){
		return this.uri ;
	}
	public String getType(){
		return this.type;
	}
	public String getRange(){
		return this.range;
	}
	public String getSubject(){
		return this.subject;
	}
	public String getProperty(){
		return this.property;
	}
	public String getObject(){
		return this.object;
	}

}
