package com.lucascarvalhaes.centurion.model.Basic;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

/**
 * The base of the entity framework.<br>
 * An entity is basically an identifiable object that has an owner.<br>
 * The entity holds a UNIQUE ID that identitifies it. It even overides<br>
 * java's equals(Object o) method to compare only the ID.<br>
 * Also every entity holds an owner ID tho know who own's it.<br>
 * For helping in game data models, the entity have a spriteSheetID field.<br>
 * This field can be used to hold a string that identifies the name of this<br>
 * entity sprite in a sprite atlas for example.<br>
 * The entity position is store with its size in a LibGDX {@link com.badlogic.gdx.math.Rectangle Rectangle} class.<br>
 * This class depends on LibGDX.<br>
 * <br>
 * For using this abstract class you will have to implement the abstract methods.<br>
 * There are three abstract methods:<br>
 * <br>
 * collided(), update(), shouldRemove()<br>
 * Their intent is described in their java doc.<br>
 * 
 * @author Lucas
 * 
 */
public abstract class BasicEntity {

	/**
	 * This is for generation entity id's
	 */
	private static long uniqueness = 0;
	/**
	 * The entity name on the spriteatlas.
	 */
	protected String spriteSheetID;
	/**
	 * This is internal and shouldn't be changed. Its this entity UNIQUE
	 * IDENTIFIER.
	 */
	protected String entityID;
	/**
	 * This is this entity owner ID. The player ID not the name (should be
	 * unique too for each player).
	 */
	protected String ownerID;
	/**
	 * This is the Box2D body controller for entities. This can be ignored if wanted.
	 */
	protected transient Rectangle	body;

	public BasicEntity(String OID, String ssID, Rectangle rect) {
		genUniqueID();
		spriteSheetID = ssID;
		ownerID = OID;
		body = rect;
	}

	/**
	 * Copy constructor
	 * 
	 * @param cpy
	 *            The entity to copy from
	 */
	public BasicEntity(BasicEntity cpy) {
		copyFrom(cpy);
	}

	public BasicEntity() {
	}

	public void setBody(Rectangle b) {
		if (body == null)
			body = b;
	}

	/**
	 * Copies all attributes from parameter entity.
	 * @param cpy The entity to be copied
	 */
	public void copyFrom(BasicEntity cpy) {
		// Copies the unique id
		entityID = cpy.getEntityID();
		spriteSheetID = cpy.getSpriteSheetID();
		ownerID = cpy.getOwnerID();

		// Do not copy references
		body = new Rectangle(cpy.body);
	}

	/**
	 * Implement this to tell how this entity should react to collisions.
	 * 
	 * @param collidedTo
	 *            The entity this ent collided
	 */
	public abstract void collided(BasicEntity collidedTo);

	/**
	 * Implement this to update this entity model logically.
	 * 
	 * @param delta
	 *            The time in seconds since last frame
	 */
	public abstract void update(float delta);

	/**
	 * Implement this to tell whether this entity should be removed both from
	 * the game and the memory.
	 * 
	 * @param screenWidth
	 *            The screen width for checking boundaries in removal.
	 * @param screenHeight
	 *            The screen height for checking boundaries in removal.
	 * @return True if this entity should be removed from the game
	 */
	public abstract boolean shouldRemove(int screenWidth, int screenHeight);

	/**
	 * Move this entity in a direction.<br>
	 * This method clones the direction vector for you, using it without<br>
	 * changing it.<br>
	 * The movement is applied to the body.
	 * 
	 * @param direction
	 * @param speed
	 * @param delta
	 */
	public void move(Vector2 direction, float speed) {
		Vector2 dir = new Vector2(direction);
		body.setPosition(body.getPosition(new Vector2()).add(dir.scl(speed)));
	};

	@Override
	/**
	 * Entities are equal by their IDs and ownerids
	 */
	public boolean equals(Object o) {
		if (o == null)
			return false;

		if (!(o instanceof BasicEntity))
			return false;

		return ((BasicEntity) o).getEntityID().equals(entityID) && ((BasicEntity) o).getOwnerID().equals(ownerID);
	}

	/*
	 * GETS AND SETS
	 */

	public String getSpriteSheetID() {
		return spriteSheetID;
	}

	/**
	 * Sets the image size, not the box2d body
	 * 
	 * @param w
	 *            The new Width
	 * @param h
	 *            The new Height
	 */
	public void setBounds(float x, float y, float w, float h) {
		body.set(x, y, w, h);
	}

	public Vector2 getPos() {
		return body.getPosition(new Vector2());
	}

	public float getX() {
		return body.x;
	}

	public float getY() {
		return body.y;
	}

	public float getWidth() {
		return body.width;
	}

	public float getHeight() {
		return body.height;
	}

	/**
	 * Sets this ship position.
	 * 
	 * @param vector2
	 *            Where to.
	 */
	public void setPosition(Vector2 vector2) {
		body.setPosition(vector2);
	}

	/**
	 * Sets this ship position.
	 * 
	 * @param x
	 *            the x coordinate
	 * @param y
	 *            the y coordinate
	 */
	public void setPosition(float x, float y) {
		body.setPosition(x, y);
	}

	/**
	 * Sets this ship size.
	 * 
	 * @param x
	 *            the x coordinate
	 * @param y
	 *            the y coordinate
	 */
	public void setSize(float w, float h) {
		body.width = w;
		body.height = h;
	}

	public void setOwner(String ID) {
		if (ID != null)
			ownerID = ID;
	}

	public String getEntityID() {
		return entityID;
	}

	public String getOwnerID() {
		return ownerID;
	}

	public Rectangle getBody()
	{
		return body;
	}

	/**
	 * Used to register a new body in the world automatically.
	 * 
	 * @return if this entity has a body.
	 */
	public boolean hasBody() {
		if(body == null)
			return false;
		return true;
	}

	/*
	 * HELPERS
	 */

	/**
	 * This is a method that set the entity ID to a unique ID.<br>
	 * You are not supposed to call this yourself. Only the default constructor<br>
	 * and the copy constructor of entity don't call this. The others call this to <br>
	 * generate a unique local ID.<br>
	 */
	protected void genUniqueID() {
		entityID = "ENT_" + (uniqueness++);
	}

	/**
	 * You shouldn't need to use this method ever.<br>
	 * This is only for a client-server communication structure<br>
	 * to avoid the need of streaming entire entities to the server.
	 * 
	 * @param id
	 *            The ID to override the generated one.
	 */
	public void forceID(String id) {
		entityID = id;
	}

	/**
	 * This was made public so that the server may call it to generate ID's for entities.<br>
	 * You shouldn't need to call this ever.
	 * 
	 * @return A new unique entity ID with the internal entity ID controller.
	 */
	public static String getNewID() {
		return "ENT_" + (uniqueness++);
	}
}
