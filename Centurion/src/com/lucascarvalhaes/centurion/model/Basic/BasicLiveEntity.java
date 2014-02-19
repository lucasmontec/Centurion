package com.lucascarvalhaes.centurion.model.Basic;

import com.badlogic.gdx.math.Rectangle;

/**
 * To sumarize this class rapidly, its the entity with a health field.<br>
 * Refer to {@link Entity} to understand how to use entities.
 * 
 * @author Lucas Carvalhaes
 * 
 */
public abstract class BasicLiveEntity extends BasicEntity {

	/**
	 * This entity total health is stored here.
	 */
	protected int	maxHealth;
	/*
	 * The entity current health is stored here
	 */
	protected int	health;

	public BasicLiveEntity() {
		super();
	}

	public BasicLiveEntity(String ownerID, String ssID, int maxhealth, Rectangle body) {
		super(ownerID, ssID, body);
		setMaxHealth(maxhealth);
		setHealth(maxHealth);
	}

	@Override
	/**
	 * Override to copy health values too.
	 */
	public void copyFrom(BasicEntity cpy) {
		if (cpy instanceof BasicLiveEntity) {
			maxHealth = ((BasicLiveEntity) cpy).getMaxHealth();
			health = ((BasicLiveEntity) cpy).getHealth();
		}
		super.copyFrom(cpy);
	}

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
