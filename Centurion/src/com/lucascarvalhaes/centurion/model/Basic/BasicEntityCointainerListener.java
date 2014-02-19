package com.lucascarvalhaes.centurion.model.Basic;

/**
 * Listen to changes in a entity container.<br>
 * Changes listened: Adding, removing
 * 
 * @author LucasM.Carvalhaes(Zombie)
 *
 */
public interface BasicEntityCointainerListener<T extends BasicEntity> {

	public void onAdding(T e);

	public void onRemoving(T e);

}
