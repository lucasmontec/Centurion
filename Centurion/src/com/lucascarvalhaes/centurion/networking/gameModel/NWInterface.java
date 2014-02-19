package com.lucascarvalhaes.centurion.networking.gameModel;

import java.util.HashMap;

public interface NWInterface {

	/**
	 * Implement to create a method that generates the creation message data.<br>
	 * This data is a string (JSon is a good idea) and must be returned.<br>
	 * This is the data that the server or client will use to make an instance of this entity.<br>
	 * This can be sent either to the server or to the client.
	 * 
	 * @return The packaged message string.
	 */
	public HashMap<String, Object> nwCreate();

	/**
	 * Implement to provide a update message for this entity in the server.<br>
	 * The server will call this method to generate the update information on each snapshot<br>
	 * assembly.
	 * 
	 * @return The packaged message string.
	 */
	public HashMap<String, Object> nwUpdate();

	/**
	 * Implement this method to receive a request from the server or client to make an entity.
	 * 
	 * @param msg
	 */
	public void nwApplyCreateMessage(HashMap<String, Object> msg);

	/**
	 * Implement this method to receive the update packages sent from the server to the client.<br>
	 * The msg string will be passed when a snapshot is received.
	 * 
	 * @param msg
	 *            The update message that was sent from the server.
	 */
	public void nwReceive(HashMap<String, Object> msg);

	/**
	 * Implement to call the SET NWMethods here. This is called automatically before each update message is made.<br>
	 * <b>All variables set in here will synchronize automatically! Calling any setNW*() with the<br>
	 * same name of a key in here wont make any effect.</b><br>
	 * DM means DataMap.
	 */
	public void updateDM();

	/**
	 * Implement to call the GET NWMethods here. This is called automatically after each receive method is processed.<br>
	 * DM means DataMap.
	 */
	public void receiveDMUpdate();

	/**
	 * This is called when the entity is asked to create a make message.<br>
	 * Implement to prepare the data tables with information that defines your entity.<br>
	 * Proprieties set to the data map here are set only once. After the create method<br>
	 * the data map is cleared.<br>
	 * DM means DataMap.
	 */
	public void createDM();

	/**
	 * This is called when a create message is beeing processed to make a new entity.<br>
	 * After all processing of internal message data, the data tables are processed through<br>
	 * this method. Implement to receive the data through the NW* methods.<br>
	 * DM means DataMap.
	 */
	public void receiveDMCreate();
}
