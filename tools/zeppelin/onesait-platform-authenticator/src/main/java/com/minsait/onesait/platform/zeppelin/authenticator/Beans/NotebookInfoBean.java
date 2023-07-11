package com.minsait.onesait.platform.zeppelin.authenticator.Beans;

public class NotebookInfoBean {



	private String id;
	private String identification;
	private String idzep;
	private String user;
	private boolean isPublic;
	private String accessType;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getIdentification() {
		return identification;
	}

	public void setIdentification(String identification) {
		this.identification = identification;
	}

	public String getIdzep() {
		return idzep;
	}

	public void setIdzep(String idzep) {
		this.idzep = idzep;
	}

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public boolean isPublic() {
		return isPublic;
	}

	public void setPublic(boolean aPublic) {
		isPublic = aPublic;
	}

	public String getAccessType() {
		return accessType;
	}

	public void setAccessType(String accessType) {
		this.accessType = accessType;
	}

	public String toString() {
		return this.identification;
	}
	
}
