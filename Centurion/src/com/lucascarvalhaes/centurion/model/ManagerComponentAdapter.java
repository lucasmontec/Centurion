package com.lucascarvalhaes.centurion.model;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.lucascarvalhaes.centurion.networking.gameModel.NWEntity;

/**
 * This works like java adapters. Provide the same interface of a manager component<br>
 * but allows the developer only to see the methods that do something.
 * 
 * @author Lucas M Carvalhaes
 * 
 * @param <T>
 *            The type of NWEntity you are working with
 */
public abstract class ManagerComponentAdapter<T extends NWEntity> extends ManagerComponent<T> {

	@Override
	public void onInstall(Centurion<T> manager) {}

	@Override
	public void onRemove(Centurion<T> manager) {}

	@Override
	public void postUpdate(Centurion<T> manager, float delta) {}

	@Override
	public void preUpdate(Centurion<T> manager, float delta) {}

	@Override
	public void render(Centurion<T> manager, float delta, SpriteBatch batc) {}

	@Override
	public void updateEntity(T entity, float delta) {}

	@Override
	public void renderEntity(T entity) {}

}
