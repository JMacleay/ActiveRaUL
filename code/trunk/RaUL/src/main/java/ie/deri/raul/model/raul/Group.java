package ie.deri.raul.model.raul;

import org.openrdf.repository.RepositoryConnection;

public class Group extends Widget{

	//add by Shepherd Liu 
	//decide if the group needs a "remove button" or not
	
	private boolean needRemoveButton=false;
	
	private String dynamicGroupUri;
	
	private String associatedDynamicGroupId;
	
	private boolean hasObjectProperty=false;
	
	public boolean isHasObjectProperty() {
		return hasObjectProperty;
	}

	public void setHasObjectProperty(boolean hasObjectProperty) {
		this.hasObjectProperty = hasObjectProperty;
	}

	public String getAssociatedDynamicGroupId() {
		return associatedDynamicGroupId;
	}

	public void setAssociatedDynamicGroupId(String associatedDynamicGroupId) {
		this.associatedDynamicGroupId = associatedDynamicGroupId;
	}

	public String getDynamicGroupUri() {
		return dynamicGroupUri;
	}

	public void setDynamicGroupUri(String dynamicGroupUri) {
		this.dynamicGroupUri = dynamicGroupUri;
	}

	public boolean isNeedRemoveButton() {
		return needRemoveButton;
	}

	public void setNeedRemoveButton(boolean needRemoveButton) {
		this.needRemoveButton = needRemoveButton;
	}
	
	public Group(RepositoryConnection con, String group) {
		// TODO Auto-generated constructor stub
		super(con, group);
	}

}
