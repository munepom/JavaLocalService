package com.munepom.appmanager;

/**
 *
 * @author munepom
 *
 */
public interface ApplicationInstanceListener {
	public void newInstanceCreated() throws Exception;

	public void stop() throws Exception;
}
