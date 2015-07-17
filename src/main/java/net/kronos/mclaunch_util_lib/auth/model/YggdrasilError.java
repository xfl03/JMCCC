package net.kronos.mclaunch_util_lib.auth.model;

import com.google.gson.annotations.Expose;

public class YggdrasilError extends Throwable
{
	private static final long serialVersionUID = -8933158830705701800L;

	@Expose
	private String error;
	
	@Expose
	private String errorMessage;
		
	@Override
	public String getMessage() {
		return this.error + ": " + this.errorMessage;
	}
	
	//===================Darkyoooooo Edited=======================
	public String getError() {
		return this.error;
	}
	
	public String getErrorMessage() {
		return this.errorMessage;
	}
	//============================================================
}