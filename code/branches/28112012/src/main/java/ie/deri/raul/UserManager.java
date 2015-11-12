package ie.deri.raul;


public class UserManager {
	
	private String _defaultUser;
	
	public UserManager() {
		_defaultUser = RaULProperties.getProperties().getProperty("public.user", "public");
		//_defaultUser = RaULProperties.getProperties().getProperty("testuser.user", "testuser");
	}
	
	public boolean isAutenticated(String username, String credentials) {
	
		return false;
	}

	public boolean isValidUser(String username) {
		if (_defaultUser.equals(username)) { // we have a default user
			return true;
		}
		return false;
	}

}
