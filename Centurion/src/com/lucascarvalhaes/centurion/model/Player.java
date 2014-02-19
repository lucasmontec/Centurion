package com.lucascarvalhaes.centurion.model;

import com.badlogic.gdx.utils.TimeUtils;

public class Player {

	private final String name;
	private final String playerID;
	private int score;

	public Player() {
		playerID = "noplayer";
		name = "noplayer";
	}

	/**
	 * The player name this player will have.<br>
	 * Player names can repeat, the player ID is generated with<br>
	 * the time in millis that this class was instanced.
	 */
	public Player(String nm) {
		name = nm;
		playerID = "player_" + name + "x" + TimeUtils.millis();
	}

	@Override
	public String toString() {
		return "Player [name=" + name + ", playerID=" + playerID + ", score=" + score + "]";
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof Player)
			return ((Player) o).getPlayerID().equals(playerID);

		return false;
	}

	/**
	 * Increases the player score.
	 * @param sco The amount to increase.
	 */
	public void addScore(int sco){
		score += sco;
	}

	/*
	 * Gets and sets
	 */

	/**
	 * Use the player ID as the owner for entities, not this!
	 * @return The player visual name.
	 */
	public String getName() {
		return name;
	}

	public int getScore() {
		return score;
	}

	public void setScore(int score) {
		this.score = score;
	}

	/**
	 * Use this as the ownerID for entities.<br>
	 * @return The UNIQUE player ID.
	 */
	public String getPlayerID() {
		return playerID;
	}

}
