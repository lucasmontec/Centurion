package com.lucascarvalhaes.centurion.physics;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.physics.box2d.World;
import com.lucascarvalhaes.centurion.model.Centurion;
import com.lucascarvalhaes.centurion.model.EntityCointainerListener;
import com.lucascarvalhaes.centurion.model.ManagerComponentAdapter;
import com.lucascarvalhaes.centurion.networking.gameModel.NWEntity;

public class PhysicsManagerComponent extends ManagerComponentAdapter<NWEntity> {

	/**
	 * Created to prevent render calls.
	 */
	public PhysicsManagerComponent() {
		// to debug set this to true
		SHOULD_RENDER = true;
	}

	/**
	 * This is the Box2D World that controls local and server collision for entities.<br>
	 * No gravity. Entities do sleep!
	 */
	protected final World				b2d_world		= PhysicsController.getWorld();

	@Override
	public void preUpdate(Centurion<NWEntity> manager, float delta) {
		/*
		 * Update our physics wold. A 1/45 step time, 6 velocity iterations and 2 position iterations.
		 */
		b2d_world.step(
				PhysicsController.STEP_TIME,
				PhysicsController.VEL_ITERATIONS,
				PhysicsController.POS_ITERATIONS);
	}

	@Override
	public void render(Centurion<NWEntity> manager, float delta, SpriteBatch batch) {
		// Not called! Only debug
		// debugRenderer.render(b2d_world, new Matrix4());
	}

	@Override
	public void onInstall(Centurion<NWEntity> manager) {
		manager.addListener(new EntityCointainerListener<NWEntity>() {
			@Override
			public void onRemoving(NWEntity e) {
				b2d_world.destroyBody(e.getBody());
			}

			@Override
			public void onAdding(NWEntity e) {
				if (!e.hasBody())
					e.setBody(PhysicsController.createTestBody(b2d_world, e));
			}
		});
	}

	@Override
	public void onRemove(Centurion<NWEntity> manager) {
		// Do nothing since we are very lazy
	}

}
