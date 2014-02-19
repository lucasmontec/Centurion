package com.lucascarvalhaes.centurion.model;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;


import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.lucascarvalhaes.centurion.view.IRenderer;

public class Centurion<T extends Entity> extends EntityContainer<T> {

	/**
	 * The renderer map for rendering of all entities
	 */
	protected HashMap<Class<?>, com.lucascarvalhaes.centurion.view.IRenderer>		renderers			= new HashMap<>();
	/**
	 * This is the exclusion map. It is used for excluding classes from parts of the manager<br>
	 * actions. You can register Bullet.class here for example and prevent it from beeing rendered.
	 */
	HashMap<Class<?>, Byte>						exclusionMap		= new HashMap<>();
	/**
	 * Flag to use for exclusion from rendering in the exclusion map
	 */
	public static final Byte					EXCLUDE_FROM_RENDER	= 0b001;
	/**
	 * Flag to use for exclusion from updating in the exclusion map
	 */
	public static final Byte					EXCLUDE_FROM_UPDATE	= 0b010;

	/**
	 * Animation timing is necessary for all renderers
	 */
	protected float								animationTimer		= 0;

	/**
	 * This is for the component pattern.
	 */
	protected HashMap<String, ManagerComponent<T>>	components			= new HashMap<>();

	/**
	 * Registers your component to the manager.
	 * 
	 * @param ID
	 *            The ID of your component to the manager.
	 * @param component
	 *            The component itself.
	 */
	public void registerComponent(String ID, ManagerComponent<T> component) {
		components.put(ID, component);
		component.onInstall(this);
	}

	/**
	 * Gets a component that goes by ID.
	 * 
	 * @param ID
	 *            The id this component was registered;
	 * @return The component or null.
	 */
	public ManagerComponent<T> getComponent(String ID) {
		return components.get(ID);
	}

	/**
	 * Delete a component that goes by the ID.
	 * 
	 * @param ID
	 *            The id to remove.
	 */
	public void removeComponent(String ID) {
		ManagerComponent<T> compo = components.get(ID);
		if (compo != null)
			compo.onRemove(this);
		components.remove(ID);
	}

	/**
	 * Register a renderer for rendering a kind of entity
	 * 
	 * @param renderer
	 *            The actual renderer
	 * @param whatThisCanRender
	 *            The entity class that it can render
	 */
	public void registerRenderer(IRenderer renderer, Class<?> whatThisCanRender) {
		renderers.put(whatThisCanRender, renderer);
	}

	/**
	 * Render all entities that have registered renderers for their classes
	 * 
	 * @param delta
	 *            The time last frame took to process
	 * @return The number of entities that weren't rendered because of the lack of a rendering object for the entity class
	 */
	public int render(float delta, SpriteBatch batch) {
		// Store the things that we didn't rendered
		int cannotRender = 0;

		// Update our global animation timer
		animationTimer += delta;

		// Read through all entities
		Iterator<Entry<String, T>> iterator = iterator();
		while (iterator.hasNext()) {
			T e = iterator.next().getValue();
			// Check if we can render entity e
			if (!renderEntity(e, batch))
				cannotRender++;

			// Render all components for the entity method
			Iterator<ManagerComponent<T>> compoIterator = components.values().iterator();
			while (compoIterator.hasNext()) {
				ManagerComponent<T> mcomp = compoIterator.next();
				if (mcomp.SHOULD_RENDER)
					mcomp.renderEntity(e);
			}
		}

		// Render all components
		Iterator<ManagerComponent<T>> compoIterator = components.values().iterator();
		while (compoIterator.hasNext()) {
			ManagerComponent<T> mcomp = compoIterator.next();
			if (mcomp.SHOULD_RENDER)
				mcomp.render(this, delta, batch);
		}

		// Return the unrendered amount
		return cannotRender;
	}

	/**
	 * Updates all entities
	 * 
	 * @param delta
	 *            The delta time from last frame
	 */
	public void update(float delta, int screenWidth, int screenHeight) {
		// pre update all components
		Iterator<ManagerComponent<T>> compoIterator = components.values().iterator();
		while (compoIterator.hasNext()) {
			ManagerComponent<T> mcomp = compoIterator.next();
			if (mcomp.SHOULD_UPDATE)
				mcomp.preUpdate(this, delta);
		}

		// Grab the local entities iterator
		Iterator<Entry<String, T>> iterator = iterator();
		while (iterator.hasNext()) {
			// Get the new entity in the line
			T e = iterator.next().getValue();
			// Check if we can update entity e
			if (isNotExcluded(e.getClass(), EXCLUDE_FROM_UPDATE)) {
				// Update the entity
				e.update(delta);
			}

			// Update all components for the entity method
			compoIterator = components.values().iterator();
			while (compoIterator.hasNext()) {
				ManagerComponent<T> mcomp = compoIterator.next();
				if (mcomp.SHOULD_UPDATE)
					mcomp.updateEntity(e, delta);
			}

			// Check for valid removals
			if (e.shouldRemove(screenWidth, screenHeight)) {
				iterator.remove();
			}
		}

		// post update all components
		compoIterator = components.values().iterator();
		while (compoIterator.hasNext()) {
			ManagerComponent<T> mcomp = compoIterator.next();
			if (mcomp.SHOULD_UPDATE)
				mcomp.postUpdate(this, delta);
		}
	}

	/**
	 * Exclude a class from a determined autmatic feature such<br>
	 * as update or collsion check.<br>
	 * You can exclude from multiple features at once using a bitwise or<br>
	 * with the flags like:<br>
	 * manager.excludeClass(Bullet.class, EntityManager.EXCLUDE_FROM_RENDER_FLAG | <br>
	 * EntityManager.EXCLUDE_FROM_UPDATE_FLAG);<br>
	 * <br>
	 * Only one exclusion is more obvious:<br>
	 * manager.excludeClass(Bullet.class, EntityManager.EXCLUDE_FROM_RENDER_FLAG);<br>
	 * <b>Calling this more than once for the same class is destructive, not additive.</b><br>
	 * This means if you call twice, only the second call is going to be that class flags.<br>
	 * 
	 * @param cla
	 *            The class to be excluded
	 * @param flag
	 *            The flag to check for exclusion
	 */
	public void excludeClass(Class<?> cla, Byte flag) {
		// Add or update the class feature
		exclusionMap.put(cla, flag);
	}

	/**
	 * Auto casting to Byte.<br>
	 * {@link Centurion#excludeClass(Class, Byte)}
	 * 
	 * @param cla
	 *            The class to be excluded
	 * @param flag
	 *            The flag to check for exclusion
	 */
	public void excludeClass(Class<?> cla, int flag) {
		// Add or update the class feature
		exclusionMap.put(cla, (byte) flag);
	}

	/**
	 * Checks if the entity's class is excluded from the flag.
	 * 
	 * @param e
	 *            The entity class to check for exclusion.
	 * @param flag
	 *            The flag to see if the entity's class is excluded.
	 * @return True if the class is not excluded from that flag.
	 */
	public boolean isNotExcluded(Class<?> e, int flag) {
		// Get the flags for e class
		Byte flags = getExclusionFlags(e);
		// Not registered
		if (flags == 0)
			return true;
		// Check the flag itself
		if ((flags & flag) == flag)
			return false;

		// Not equal, return false
		return true;
	}

	/**
	 * Retrieve the exclusion flags for the entity class.
	 * 
	 * @param e
	 *            The entity to find the flags from its class.
	 * @return The flags as an integer
	 */
	private Byte getExclusionFlags(Class<?> e) {
		Byte flags = exclusionMap.get(e);
		if (flags == null)
			return 0;
		return flags;
	}

	/**
	 * Render a single entity with the renderer designed for it.<br>
	 * Doesn't check for class exclusion flags.
	 * 
	 * @param e
	 *            The entity to be rendered.
	 * @param batch
	 *            The sprite batch to use.
	 * @return True if the entity has a registered renderer and it was rendered.
	 */
	protected boolean renderEntity(T e, SpriteBatch batch) {
		// Try to retrieve a renderer for this entity
		IRenderer renderer = renderers.get(e.getClass());
		// If we have found a renderer, use it!
		if (renderer != null) {
			renderer.render(animationTimer, e, batch);
			return true;
		}
		return false;
	}
}
