package com.lucascarvalhaes.centurion.networking;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

import com.badlogic.gdx.utils.TimeUtils;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.kryonet.Server;
import com.lucascarvalhaes.centurion.model.Centurion;
import com.lucascarvalhaes.centurion.model.Entity;
import com.lucascarvalhaes.centurion.model.EntityCointainerListener;
import com.lucascarvalhaes.centurion.model.ManagerComponentAdapter;
import com.lucascarvalhaes.centurion.model.Player;
import com.lucascarvalhaes.centurion.networking.Messaging.AlreadyLoggedIn;
import com.lucascarvalhaes.centurion.networking.Messaging.AvaliableID;
import com.lucascarvalhaes.centurion.networking.Messaging.EntitiesOnTheServer;
import com.lucascarvalhaes.centurion.networking.Messaging.Login;
import com.lucascarvalhaes.centurion.networking.Messaging.NewEntities;
import com.lucascarvalhaes.centurion.networking.Messaging.PlayerDropped;
import com.lucascarvalhaes.centurion.networking.Messaging.RemoveEntities;
import com.lucascarvalhaes.centurion.networking.Messaging.Snapshot;
import com.lucascarvalhaes.centurion.networking.gameModel.NWEntity;
import com.lucascarvalhaes.centurion.physics.PhysicsManagerComponent;
import com.lucascarvalhaes.centurion.testing.DebugNetworkListener;

public abstract class GameServer {

	/*
	 * SERVER
	 */

	Server											server	= null;
	public final int								stepTime;
	private final Timer								timer;

	// Store the entities for each owner
	public Centurion<NWEntity>					entities;
	// Messages for updating entities - Should be EntityID -> UpdateMessage
	public HashMap<String, HashMap<String, Object>>	updateMessages;
	// Messages for creating entities - Should be CreateMessage -> EntityClass
	public HashMap<HashMap<String, Object>, String>	newEntities;
	// Messages for entities that have been removed
	public ArrayList<String>						removedEntities;

	// Store online players and their connection IDs
	public HashMap<Integer, Player>					players;

	public GameServer(int stepTime) {
		// Make the default server on the default ports
		server = NetworkFactory.getServer(Messaging.PORT_TCP, Messaging.PORT_UDP);
		// Prepare the server for messages
		Messaging.prepare(server);
		// Debug shit
		server.addListener(new DebugNetworkListener());
		// Listener for answering client requests
		server.addListener(new InternalListener());
		// Store step time
		this.stepTime = stepTime;
		// Create the entities hashmap
		entities = new Centurion<>();
		removedEntities = new ArrayList<>();
		newEntities = new HashMap<>();
		updateMessages = new HashMap<>();

		players = new HashMap<>();
		// A timer to control server update calls
		timer = new Timer();

		// Add the physics manager to the entities
		entities.registerComponent("physicsComponent", new PhysicsManagerComponent());
		// Lets register all entity registrations
		entities.addListener(new EntityCointainerListener<NWEntity>() {

			@Override
			public void onRemoving(NWEntity e) {
				// Store to send to clients
				removedEntities.add(e.getEntityID());
			}

			@Override
			public void onAdding(NWEntity e) {
				// I can safely use the e.nwCreate() method as key because the string it returns
				// contains the entity ID which is unique
				newEntities.put(e.nwCreate(), e.getClass().getName());
			}
		});
		// Lets now register a component to manage networking internals on the manager
		entities.registerComponent("networkingComponent", new ManagerComponentAdapter<NWEntity>() {
			@Override
			public void updateEntity(NWEntity e, float delta) {
				// Store the updated entities as network messages
				updateMessages.put(e.getEntityID(), e.nwUpdate());
			}
		});
	}

	/*
	 * USER OPERATIONS
	 */

	/**
	 * Install a listener to listen for messges of your game model.
	 * 
	 * @param listener
	 *            The listener to be installed
	 */
	public void installListener(Listener listener) {
		if (listener != null)
			if (server != null)
				server.addListener(listener);
	}

	/*
	 * SERVER OPERATIONS
	 */

	/**
	 * Stop thet server.
	 */
	public void close() {
		timer.cancel();
		server.close();
	}

	/**
	 * Implement to add server update code
	 * 
	 * @param dt
	 */
	public abstract void update(float dt);

	/**
	 * Simulate the server
	 * 
	 * @param dt
	 *            The time sice last update call. This shuould be fixed 15 ms.
	 */
	public void innerUpdate(float dt) {
		// Update the user game model code
		update(dt);

		// Update all entities
		entities.update(dt / 1000f, -1, -1);

		// Send the newest snapshot to all players
		if (entities.size() > 0) {
			server.sendToAllUDP(Snapshot.make(TimeUtils.millis(), updateMessages));
			updateMessages.clear();
		}
		// If there were created entities send them here
		if (newEntities.size() > 0) {
			server.sendToAllTCP(NewEntities.make(newEntities));
			newEntities.clear();
		}
		// If there were removed entities send here
		if (removedEntities.size() > 0) {
			server.sendToAllTCP(RemoveEntities.make(removedEntities));
			removedEntities.clear();
		}

	}

	/*
	 * MESSAGE HANDLERS
	 */

	private boolean login(int connectionid, Login msg) {
		if (!players.containsValue(msg.p)) {
			// Store the player
			players.put(connectionid, msg.p);
			// Respond him with the existing entitites as a list of new entities
			HashMap<HashMap<String, Object>, String> allEnts = new HashMap<>();
			for (NWEntity ent : entities.asList()) {
				allEnts.put(ent.nwCreate(), ent.getClass().getName());
			}
			// Make the message
			EntitiesOnTheServer messsage = EntitiesOnTheServer.make(allEnts);
			server.sendToTCP(connectionid, messsage);
			// Send the ship avaliable id too
			server.sendToTCP(connectionid, AvaliableID.make(Entity.getNewID()));
			return false; // no problems
		}
		return true;
	}

	private void playerDropped(int connectionID) {
		server.sendToAllTCP(PlayerDropped.make(players.get(connectionID)));
		players.remove(connectionID);
	}

	/*
	 * SERVER FRAMEWORK
	 */

	/**
	 * Start the server thread that calls update each stepTime milliseconds.
	 */
	public void start() {
		timer.scheduleAtFixedRate(new TimerTask() {
			@Override
			public void run() {
				innerUpdate(stepTime);
			}
		}, 0, stepTime);
	}

	/**
	 * The internal listener. This maps to calls of the game server methods.
	 * 
	 * @author Lucas M carvalhaes
	 * 
	 */
	private class InternalListener extends Listener {
		@Override
		public void connected(Connection con) {
		}

		@Override
		public void disconnected(Connection con) {
			playerDropped(con.getID());
		}

		@Override
		public void received(Connection con, Object obj) {
			/*
			 * Map spawn message to method
			 */
			if (obj instanceof Login) {
				// If login returns true, the player is already logged!
				if (login(con.getID(), (Login) obj)) {
					server.sendToTCP(con.getID(), new AlreadyLoggedIn());
					con.close();
				}
			}

		}
	}
}
