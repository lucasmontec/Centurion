package com.lucascarvalhaes.centurion.model;

/**
 * Listen to changes in a entity container.<br>
 * Changes listened: Adding, removing
 * 
 * @author LucasM.Carvalhaes(Zombie)
 *
 */
public interface EntityCointainerListener<T extends Entity> {

	public void onAdding(T e);

	public void onRemoving(T e);

}
