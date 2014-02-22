package com.lucascarvalhaes.centurion.view;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.lucascarvalhaes.centurion.model.Entity;

public interface IRenderer {
	public void render(float animationTimer, Entity object, SpriteBatch batch);
}
