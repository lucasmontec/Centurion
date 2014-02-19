package com.lucascarvalhaes.centurion.view;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.lucascarvalhaes.centurion.model.Basic.BasicEntity;

public interface IBasicRenderer {

	public void render(float animationTimer, BasicEntity object, SpriteBatch batch);

}
