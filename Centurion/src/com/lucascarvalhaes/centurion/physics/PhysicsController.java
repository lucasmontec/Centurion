package com.lucascarvalhaes.centurion.physics;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.World;
import com.lucascarvalhaes.centurion.model.Entity;

public class PhysicsController {

	public static final float	TO_B2D		= 0.01f;
	public static final float	FROM_B2D	= 100f;

	public static final float	STEP_TIME		= 1 / 60f;	// default 1 / 45f
	public static final int		VEL_ITERATIONS	= 6;
	public static final int		POS_ITERATIONS	= 6;

	public static final boolean	DEBUG_PHYSICS	= true;

	public static World getWorld() {
		return new World(new Vector2(), true);
	}

	public static Box2DDebugRenderer getDebug() {
		return new Box2DDebugRenderer();
	}

	/**
	 * This uses the body definition to bake the body.
	 * 
	 * @return
	 */
	public static Body createTestBody(World world, Entity e) {
		// Create the body definition
		BodyDef bodyDef = new BodyDef();

		// We set our body to dynamic, for something like ground which doesn't move we would set it to StaticBody
		bodyDef.type = BodyType.DynamicBody;
		// Set our body's starting position in the world
		bodyDef.position.set(100, 500);

		// Create our body in the world using our body definition
		Body body = world.createBody(bodyDef);

		// Create a circle shape and set its radius to 6
		CircleShape circle = new CircleShape();
		circle.setRadius(6);

		// Create a fixture definition to apply our shape to
		FixtureDef fixtureDef = new FixtureDef();
		fixtureDef.shape = circle;
		fixtureDef.density = 0.25f;
		fixtureDef.friction = 0.2f;
		fixtureDef.restitution = 0.6f; // Make it bounce a little bit

		body.createFixture(fixtureDef);
		body.setUserData(e);

		// Remember to dispose of any shapes after you're done with them!
		// BodyDef and FixtureDef don't need disposing, but shapes do.
		circle.dispose();

		return body;
	}
}
