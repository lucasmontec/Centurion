package com.lucascarvalhaes.centurion.networking.gameModel;

import java.util.HashMap;
import java.util.Map;

import com.badlogic.gdx.math.Vector2;

/**
 * A live entity class that generates networking messages and reads them.<br>
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
public abstract class NWLiveEntity extends NWEntity implements NWInterface {

	/*
	 * Live entity model
	 */

	/**
	 * This entity total health is stored here.
	 */
	protected int	maxHealth;
	/*
	 * The entity current health is stored here
	 */
	protected int	health;

	/*
	 * NW model
	 */

	public NWLiveEntity(String OID, String ssID, int maxHealth, Vector2 sz) {
		super(OID, ssID, sz);
		this.maxHealth = maxHealth;
		health = maxHealth;
	}

	public NWLiveEntity() {}

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
		// PROP #6
		entityData.put("nw_health", health);
		// PROP #7
		entityData.put("nw_maxHealth", maxHealth);
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
		// #6 HEALTH
		health = (int) msg.get("nw_health");
		// #7 MAX HEALTH
		maxHealth = (int) msg.get("nw_maxHealth");
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
		// PROP #3
		entityData.put("nw_health", health);
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
			// Get the health
			health = (int) msg.get("nw_health");
		}
		receiveDMUpdate();
	}

	/*
	 * Live entity methods
	 */

	/**
	 * @return The current health level from 0 to 1
	 */
	public float getHealthNormalized() {
		return (health * 1f / maxHealth * 1f);
	}

	/**
	 * @return true if health is greater than 0. false otherwise.
	 */
	public boolean isAlive() {
		return health > 0;
	}

	/**
	 * Sets the healt to 0
	 */
	public void die() {
		health = 0;
	}

	/**
	 * Take damage. handles death.
	 * 
	 * @param amount
	 *            The amount of damage to take
	 */
	public void takeDamage(int amount) {
		setHealth(health - amount);
	}

	/*
	 * Gets and Sets
	 */

	public int getHealth() {
		return health;
	}

	/**
	 * Sets the health of this entity.<br>
	 * Atumatically keeps the health value locked between 0 and maxHealth.
	 * 
	 * @param health
	 *            The new health to set
	 */
	public void setHealth(int health) {
		if (health > maxHealth)
			setHealth(maxHealth);
		else if (health < 0)
			this.health = 0;
		else
			this.health = health;
	}

	/**
	 * @return This entity max health.
	 */
	public int getMaxHealth() {
		return maxHealth;
	}

	/**
	 * Changes the entity maxHealth.<br>
	 * if the entity current health is above the new value,<br>
	 * the current health beacome the new value.
	 * 
	 * @param maxHealth
	 *            The new max health for this entity.(must be above 0)
	 */
	public void setMaxHealth(int maxHealth) {
		// Only above 0
		if (maxHealth > 0) {
			this.maxHealth = maxHealth;
			// Lock the health in the boudaries
			if (health > maxHealth)
				health = maxHealth;
		}
	}
}
