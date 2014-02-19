package com.lucascarvalhaes.centurion.model;


import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.lucascarvalhaes.centurion.view.IRenderer;

public abstract class ManagerComponent<T extends Entity> {

	/**
	 * Set this to true to make the entity manager call update on this component.
	 */
	public boolean	SHOULD_UPDATE	= true;
	/**
	 * Set this to true to make the entity manager call render on this component.
	 */
	public boolean	SHOULD_RENDER	= true;

	public abstract void onInstall(Centurion<T> manager);

	public abstract void onRemove(Centurion<T> manager);

	/**
	 * Implement to add a custom call before the entity manager update.
	 * 
	 * @param manager
	 *            The manager will be passed to the method.
	 * @param delta
	 *            The delta time since last update call will be passed to the manager.
	 */
	public abstract void preUpdate(Centurion<T> manager, float delta);

	/**
	 * Implement to add a custom call after the entity manager update.
	 * 
	 * @param manager
	 *            The manager will be passed to the method.
	 * @param delta
	 *            The delta time since last update call will be passed to the manager.
	 */
	public abstract void postUpdate(Centurion<T> manager, float delta);

	/**
	 * Implement to add a custom rendering call to the manager. Don't use this to render entities.<br>
	 * To render entities you should implement a {@link IRenderer}.
	 * 
	 * @param manager
	 * @param delta
	 * @param batch
	 */
	public abstract void render(Centurion<T> manager, float delta, SpriteBatch batc);

	/**
	 * The manager calls this after updating each single entity
	 * 
	 * @param entity
	 *            The entity beeing updated
	 * @param delta
	 *            The delta time since last update call
	 */
	public abstract void updateEntity(T entity, float delta);

	/**
	 * THe manager calls this after rendering each entity
	 * 
	 * @param entity
	 *            The entity just rendered
	 */
	public abstract void renderEntity(T entity);
}
