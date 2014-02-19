package com.lucascarvalhaes.centurion.networking.gameModel;

import java.util.HashMap;
import java.util.Map;

import com.badlogic.gdx.math.Vector2;
import com.lucascarvalhaes.centurion.model.Entity;

/**
 * A entity class that generates networking messages and reads them.<br>
 * To network more data then the entity or live entity provides, use the<br>
 * <b>Data map methods</b>:<br>
 * <li>getNWInt</li><br>
 * <li>setNWInt</li><br>
 * <li>getNWBool</li><br>
 * <li>setNWBool</li><br>
 * <li>getNWFloat</li><br>
 * <li>setNWFloat</li><br>
 * <li>getNWString</li><br>
 * <li>setNWString</li><br>
 * 
 * @author Lucas M Carvalhaes
 * 
 */
public abstract class NWEntity extends Entity implements NWInterface {
	/**
	 * This is a data map that is synchronized to the clients
	 */
	protected final HashMap<String, Object>	dataMap	= new HashMap<>();

	public NWEntity(String OID, String ssID, Vector2 sz) {
		super(OID, ssID, sz);
	}

	public NWEntity() {
	}

	@Override
	public String getEntityID() {
		return entityID + "@" + ownerID;
	}

	@Override
	public HashMap<String, Object> nwCreate() {
		// Setup the data map
		createDM();
		// Create an entity data map
		HashMap<String, Object> entityData = new HashMap<>();
		// PROP #0
		entityData.put("nw_entityID", entityID);
		// PROP #1
		entityData.put("nw_dataMap", new HashMap<>(dataMap));
		// PROP #2
		entityData.put("nw_size", size);
		// PROP #3
		entityData.put("nw_ownerID", ownerID);
		// PROP #4
		entityData.put("nw_spritesheetID", spriteSheetID);
		// PROP #5
		entityData.put("nw_position", getPos());
		// After the creation we clear the data map
		dataMap.clear();
		// Then return the message
		return entityData;
	}

	/**
	 * This method copies all data from the msg to this entity.<br>
	 * <b>THIS COPIES THE ID</b><br>
	 * The message should be a message generated from a call to a {@code nwCreate()} method.<br>
	 * Example:<br>
	 * {@code entity2.nwMakeInstance(entity.nwCreate());}<br>
	 * or<br>
	 * {@code entity2.nwMakeInstance(createMsgFromServer);}<br>
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void nwApplyCreateMessage(HashMap<String, Object> msg) {
		// #0 ID
		forceID((String) msg.get("nw_entityID"));
		// Get the data map #1
		dataMap.clear();
		dataMap.putAll((Map<String, Object>) msg.get("nw_dataMap"));
		// Get the size #2
		if (size != null)
			size.set((Vector2) msg.get("nw_size"));
		else {
			size = new Vector2((Vector2) msg.get("nw_size"));
		}
		// #3 OWNER ID
		ownerID = (String) msg.get("nw_ownerID");
		// #4 SPRITESHEETID
		spriteSheetID = (String) msg.get("nw_spritesheetID");
		// #5 BODYPOS
		if (body != null) {
			Vector2 pos = (Vector2) msg.get("nw_position");
			setPosition(pos);
		}
		receiveDMCreate();
	}

	@Override
	public HashMap<String, Object> nwUpdate() {
		// Setup the data map
		updateDM();
		// Create an entity data map
		HashMap<String, Object> entityData = new HashMap<>();
		// PROP #0
		entityData.put("nw_entityID", entityID);
		// PROP #1
		entityData.put("nw_dataMap", dataMap);
		// PROP #2
		entityData.put("nw_position", getPos());
		// proprieties.add(json.toJson(b2d_body, B2DBody.class)); Does Box2D changes the body?
		return entityData;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void nwReceive(HashMap<String, Object> msg) {
		// Invalid msg
		if (msg.get("nw_entityID") == null) {
			System.err.println("Invalid message beeing processed!");
			return;
		}
		// Check for id validity
		if (msg.get("nw_entityID").equals(entityID)) {
			// Get the entity datamap
			dataMap.clear();
			dataMap.putAll((Map<String, Object>) msg.get("nw_dataMap"));
			// Get the pos
			if (body != null) {
				Vector2 pos = (Vector2) msg.get("nw_position");
				setPosition(pos);
			}
		}
		receiveDMUpdate();
	}

	/*
	 * Data map mehtods
	 */

	/*
	 * NW BOOLEAN
	 */

	/**
	 * <h1>Data map method: Boolean SET</h1><br>
	 * This method called on the client only set the variables locally. When called<br>
	 * on the server it changes the values to the clients too.<br>
	 * Setup a variable in the data map of this entity.<br>
	 * If the variable name doesn't exist in the table, the variable is created.
	 * 
	 * @param name
	 *            The name or key to the data
	 * @param value
	 *            The data itself
	 */
	public void setNWBool(String name, Boolean value) {
		dataMap.put(name, value);
	}

	/**
	 * <h1>Data map method: Boolean GET</h1><br>
	 * Get a variable from the data map of this entity.<br>
	 * If the variable name doesn't exist in the table, the method returns the default parameter.<br>
	 * 
	 * @param name
	 *            The name or key to the desired variable
	 * @param def
	 *            The default value to return if the variable is not found
	 * @return The variable or the default parameter
	 */
	public Boolean getNWBool(String name, Boolean def) {
		Object ret = dataMap.get(name);

		if (ret == null)
			return def;

		if (!(ret instanceof Boolean))
			return def;

		return (Boolean) ret;
	}

	public Boolean getNWBool(String name) {
		return getNWBool(name, false);
	}

	/*
	 * NW FLOAT
	 */
	/**
	 * <h1>Data map method: Float SET</h1><br>
	 * This method called on the client only set the variables locally. When called<br>
	 * on the server it changes the values to the clients too.<br>
	 * Setup a variable in the data map of this entity.<br>
	 * If the variable name doesn't exist in the table, the variable is created.
	 * 
	 * @param name
	 *            The name or key to the data
	 * @param value
	 *            The data itself
	 */
	public void setNWFloat(String name, Float value) {
		dataMap.put(name, value);
	}

	/**
	 * <h1>Data map method: Float GET</h1><br>
	 * Get a variable from the data map of this entity.<br>
	 * If the variable name doesn't exist in the table, the method returns the default parameter.<br>
	 * 
	 * @param name
	 *            The name or key to the desired variable
	 * @param def
	 *            The default value to return if the variable is not found
	 * @return The variable or the default parameter
	 */
	public Float getNWFloat(String name, Float def) {
		Object ret = dataMap.get(name);

		if (ret == null)
			return def;

		if (!(ret instanceof Float))
			return def;

		return (Float) ret;
	}

	public Float getNWFloat(String name) {
		return getNWFloat(name, 1f);
	}

	/*
	 * NW INT
	 */
	/**
	 * <h1>Data map method: Int SET</h1><br>
	 * This method called on the client only set the variables locally. When called<br>
	 * on the server it changes the values to the clients too.<br>
	 * Setup a variable in the data map of this entity.<br>
	 * If the variable name doesn't exist in the table, the variable is created.
	 * 
	 * @param name
	 *            The name or key to the data
	 * @param value
	 *            The data itself
	 */
	public void setNWInt(String name, Integer value) {
		dataMap.put(name, value);
	}

	/**
	 * <h1>Data map method: Int GET</h1><br>
	 * Get a variable from the data map of this entity.<br>
	 * If the variable name doesn't exist in the table, the method returns the default parameter.<br>
	 * 
	 * @param name
	 *            The name or key to the desired variable
	 * @param def
	 *            The default value to return if the variable is not found
	 * @return The variable or the default parameter
	 */
	public Integer getNWInt(String name, Integer def) {
		Object ret = dataMap.get(name);

		if (ret == null)
			return def;

		if (!(ret instanceof Integer))
			return def;

		return (Integer) ret;
	}

	public Integer getNWInt(String name) {
		return getNWInt(name, 0);
	}

	/*
	 * NW STRING
	 */
	/**
	 * <h1>Data map method: String SET</h1><br>
	 * This method called on the client only set the variables locally. When called<br>
	 * on the server it changes the values to the clients too.<br>
	 * Setup a variable in the data map of this entity.<br>
	 * If the variable name doesn't exist in the table, the variable is created.
	 * 
	 * @param name
	 *            The name or key to the data
	 * @param value
	 *            The data itself
	 */
	public void setNWString(String name, String value) {
		dataMap.put(name, value);
	}

	/**
	 * <h1>Data map method: String GET</h1><br>
	 * Get a variable from the data map of this entity.<br>
	 * If the variable name doesn't exist in the table, the method returns the default parameter.<br>
	 * 
	 * @param name
	 *            The name or key to the desired variable
	 * @param def
	 *            The default value to return if the variable is not found
	 * @return The variable or the default parameter
	 */
	public String getNWString(String name, String def) {
		Object ret = dataMap.get(name);

		if (ret == null)
			return def;

		if (!(ret instanceof String))
			return def;

		return (String) ret;
	}

	public String getNWString(String name) {
		return getNWString(name, "");
	}

	/*
	 * NW OBJECT
	 */
	/**
	 * <h1>Data map method: Object SET</h1><br>
	 * This method called on the client only set the variables locally. When called<br>
	 * on the server it changes the values to the clients too.<br>
	 * Setup a variable in the data map of this entity.<br>
	 * If the variable name doesn't exist in the table, the variable is created.<br>
	 * <b>THE OBJECT MUST BE REGISTERED IN THE NET SERIALIZER!</b>
	 * 
	 * @param name
	 *            The name or key to the data
	 * @param value
	 *            The data itself
	 */
	public void setNWObject(String name, Object value) {
		dataMap.put(name, value);
	}

	/**
	 * <h1>Data map method: Object GET</h1><br>
	 * Get a variable from the data map of this entity.<br>
	 * If the variable name doesn't exist in the table, the method returns the default parameter.<br>
	 * 
	 * @param name
	 *            The name or key to the desired variable
	 * @param def
	 *            The default value to return if the variable is not found
	 * @return The variable or the default parameter
	 */
	public Object getNWObject(String name, Object def) {
		Object ret = dataMap.get(name);

		if (ret == null)
			return def;

		return ret;
	}

	public Object getNWObject(String name) {
		return getNWObject(name, null);
	}
}
