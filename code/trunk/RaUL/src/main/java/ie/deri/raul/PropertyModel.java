package ie.deri.raul;

public class PropertyModel {

	private String property;
	private String propertyType;
	private String propertyRange;
	private String propertyLabel;
	private String propertyInverseProperty;
	
	
	/* Default Constructor */
	
	public PropertyModel() {
		// TODO Auto-generated constructor stub
		this.property="";
		this.propertyType="";
		this.propertyRange="";
		this.propertyLabel="";
		this.propertyInverseProperty="";
	}
	
	/* Parametrise Constructor */
	
	public PropertyModel(String property, String type, String range, String label, String inverse) {
		// TODO Auto-generated constructor stub
		this.property= property;
		this.propertyType= type ;
		this.propertyRange= range ;
		this.propertyLabel= label ;
		this.propertyInverseProperty=inverse;
	}

	/* Getter Methods */
	
	public String getProperty(){
		return this.property;
	}
	
	public String getPropertyType(){
		return this.propertyType;
	}
	
	public String getPropertyRange(){
		return this.propertyRange;
	}
	
	public String getPropertyLabel(){
		return this.propertyLabel;
	}
	
	public String getPropertyInverseProperty(){
		return this.propertyInverseProperty;
	}
	
	/* Setter Methods */
	
	public void setProperty( String property ){
		this.property = property ;
	}
	
	public void setPropertyType(String type){
		this.propertyType= type;
	}
	
	public void setPropertyRange(String range){
		this.propertyRange = range;
	}
	
	public void setPropertyLabel(String label){
		this.propertyLabel = label;
	}
	public void setPropertyInverseProperty(String inverse){
		this.propertyLabel = inverse;
	}
}
