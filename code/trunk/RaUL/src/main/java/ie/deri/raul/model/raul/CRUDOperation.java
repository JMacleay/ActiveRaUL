package ie.deri.raul.model.raul;

public class CRUDOperation {
	
	private String CRUDOperation;
	private String type ;

	public CRUDOperation(String crudValue) {
		// TODO Auto-generated constructor stub
		this.CRUDOperation = crudValue.replaceAll("\"","");
		
	}
	
	public String getOperationURI(){
		return this.CRUDOperation;
	}

}
