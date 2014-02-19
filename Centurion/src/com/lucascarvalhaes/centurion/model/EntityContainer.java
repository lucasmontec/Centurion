package com.lucascarvalhaes.centurion.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

/**
 * A container for entities.<br>
 * Holds entities in a ArrayList.<br>
 * Removes entities by their object or their ID.<br>
 * 
 * @author LucasM.Carvalhaes(Zombie)
 * 
 */
public class EntityContainer<T extends Entity> {

	/**
	 * The entity list
	 */
	protected HashMap<String, T>					entities	= new HashMap<>();
	/**
	 * Another internal optimization is mapping the owner to the entity.<br>
	 * This map should be: OwnerID -> ListOf(EntityID)
	 */
	protected HashMap<String, ArrayList<String>>	ownerOf		= new HashMap<>();
	/**
	 * The listener list for entity manager listeners
	 */
	ArrayList<EntityCointainerListener<T>>			listeners	= new ArrayList<EntityCointainerListener<T>>();

	/**
	 * Get the iterator from the entities list
	 * 
	 * @return
	 */
	public Iterator<Entry<String, T>> iterator() {
		return entities.entrySet().iterator();
	}

	/**
	 * Return the array list representation of this object
	 * 
	 * @return
	 */
	public ArrayList<T> asList() {
		return new ArrayList<>(entities.values());
	}

	/**
	 * Register an entity to this container.
	 * 
	 * @param e
	 *            Entity to be registered
	 * @return true if it was registered, false if it is already registered
	 */
	public boolean addEntity(T e) {
		// Can't have copies on the list
		T onList = entities.get(e.entityID);
		if (onList != null)
			if (e.getOwnerID().equals(onList.getOwnerID()))
				return false;

		// Event
		addingEntityEvent(e);
		// Now that copies are secured, we just add
		entities.put(e.getEntityID(), e);
		// Map the new entity
		mapEntityToOwner(e);
		return true;
	}

	/**
	 * Method that adds a entity ID to the list of ids a owner has
	 * 
	 * @param e
	 *            The entity to map
	 */
	protected void mapEntityToOwner(T e) {
		// Map owner id to entity id in the owner map
		ArrayList<String> idList = ownerOf.get(e.getOwnerID());
		// Check if the list exists
		if (idList == null) {
			idList = new ArrayList<>();
		}

		// Add the new ID
		idList.add(e.getEntityID());
	}

	/**
	 * Remove the existing map of the entity to its owner. Deletes the owner<br>
	 * id list if it is empty.
	 * 
	 * @param e
	 *            The entity beeing unmapped.
	 */
	protected void unmapEntityFromOwner(T e) {
		// Map owner id to entity id in the owner map
		ArrayList<String> idList = ownerOf.get(e.getOwnerID());
		// Check if the list exists
		if (idList == null) {
			return;
		}

		// Add the new ID
		idList.remove(e.getEntityID());

		// Check if the list became empty
		if (idList.size() == 0) {
			// If so, remove all references to it
			// Let the GC work
			ownerOf.remove(e.getOwnerID());
			idList.clear();
			idList = null;
		}
	}

	/**
	 * Remove an entity by its object.
	 * 
	 * @param e
	 *            The entity to unregister.
	 * @return true if it was found and removed, false otherwise.
	 */
	public boolean removeEntity(T e) {
		// Event
		removingEntityEvent(e);
		boolean ret = entities.remove(e.getEntityID()) != null;
		// Remove the mapping to the entity
		unmapEntityFromOwner(e);
		return ret;
	}

	/**
	 * Remove an entity by its ID.
	 * 
	 * @param entityID
	 *            The id of the entity to unregister.
	 * @return true if it was found and removed, false otherwise.
	 */
	public boolean removeEntity(String entityID) {
		// Find the entry first
		T e = getEntityByID(entityID);

		// If found remove
		if (e != null) {
			// Event
			removingEntityEvent(e);
			// Kill the ent from this manager
			removeEntity(e);
			// Trim to memory optimization - deprecated due to map optimization
			// entities.trimToSize();
			return true;
		} else
			return false;
	}

	/**
	 * Remove all entities from a owner.
	 * 
	 * @param ownerID
	 *            The id of the owner.
	 * @return true if it has found and removed, false otherwise.
	 */
	public boolean removeAllFromOwner(String ownerID) {
		ArrayList<String> removalList = ownerOf.get(ownerID);
		// Search all of them
		for (String entityID : removalList) {
			removeEntity(entities.get(entityID));
		}

		// Return information
		boolean ret = false;
		if (removalList.size() > 0)
			ret = true;

		// GC friendly
		removalList.clear();
		removalList = null;

		return ret;
	}

	/**
	 * Find an entity registered by its ID.
	 * 
	 * @param entityID
	 *            The ID to look for
	 * @return The entity if it is registered
	 */
	public T getEntityByID(String entityID) {
		return entities.get(entityID);
	}

	/**
	 * Find an entity registered by its ID.
	 * 
	 * @param ownerID
	 *            The owner of the entity
	 * @param entityID
	 *            The ID to look for
	 * @return The entity if it is registered
	 */
	public T getEntityWithOwnerByID(String ownerID, String entityID) {
		// FAST METHOD FIRST - Check if we got an entity with the desired ID

		// Try to find the entity by its ID
		T e = entities.get(entityID);
		if (e.getOwnerID().equals(ownerID))
			return e;

		// Didn't found any, lets now try a different more slow method... (should never land here)

		// Get all entityId's from the owner with that ID
		ArrayList<String> entsFromOwner = ownerOf.get(ownerID);
		// Check if he has the entity with that ID
		if (entsFromOwner != null) {
			if (entsFromOwner.contains(entityID))
				// Return it
				return entities.get(entityID);

			// GC
			entsFromOwner.clear();
			entsFromOwner = null;
		}

		// Didn't found any - again... return null
		return null;
	}

	/**
	 * @return The internal collection of entities
	 */
	public Collection<T> getInternalList() {
		return entities.values();
	}

	/*
	 * LISTENER INTERFACE
	 */

	/**
	 * Internal listener interface. Updates all listeners for register action.
	 * 
	 * @param e
	 *            The entity that was newly added
	 */
	private void addingEntityEvent(T e) {
		for (EntityCointainerListener<T> listener : listeners) {
			listener.onAdding(e);
		}
	}

	/**
	 * Internal listener interface. Updates all listeners for unregister action.
	 * 
	 * @param e
	 *            The entity that was newly removed
	 */
	private void removingEntityEvent(T e) {
		for (EntityCointainerListener<T> listener : listeners) {
			listener.onRemoving(e);
		}
	}

	/**
	 * Register a manaager listener
	 * 
	 * @param eml
	 *            The listener to be registered
	 */
	public void addListener(EntityCointainerListener<T> eml) {
		listeners.add(eml);
	}

	/**
	 * Register a manaager listener
	 * 
	 * @param eml
	 *            The listener to be registered
	 */
	public void removeListener(EntityCointainerListener<T> eml) {
		listeners.remove(eml);
	}

	public int size() {
		return entities.size();
	}
}
