package com.lucascarvalhaes.centurion.networking;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.lucascarvalhaes.centurion.model.Centurion;
import com.lucascarvalhaes.centurion.model.ManagerComponentAdapter;
import com.lucascarvalhaes.centurion.model.Player;
import com.lucascarvalhaes.centurion.networking.Messaging.AvaliableID;
import com.lucascarvalhaes.centurion.networking.Messaging.ControlShip;
import com.lucascarvalhaes.centurion.networking.Messaging.EntitiesOnTheServer;
import com.lucascarvalhaes.centurion.networking.Messaging.Login;
import com.lucascarvalhaes.centurion.networking.Messaging.NewEntities;
import com.lucascarvalhaes.centurion.networking.Messaging.PlayerDropped;
import com.lucascarvalhaes.centurion.networking.Messaging.RemoveEntities;
import com.lucascarvalhaes.centurion.networking.Messaging.Snapshot;
import com.lucascarvalhaes.centurion.networking.Messaging.SpawnPlayer;
import com.lucascarvalhaes.centurion.networking.gameModel.NWCreator;
import com.lucascarvalhaes.centurion.networking.gameModel.NWEntity;
import com.lucascarvalhaes.centurion.networking.gameModel.NWLiveEntity;

public class GameClient {
	public final Client	client;
	public int			timeout	= 5000;

	private String		IP;
	private int			TCP_PORT, UDP_PORT;
	private long		lastSnapshotTime	= 0;

	Centurion<NWEntity>	clientManager;

	private final ArrayList<NWEntity>	entitiesToAdd;

	/*
	 * Game related
	 */
	/**
	 * The shipID gathered from the server
	 */
	public String						shipID				= null;

	/**
	 * The number of retries to connect to a server
	 */
	public static int	RETRIES	= 4;

	/**
	 * @param ip
	 *            The IP to
	 * @param tcpPort
	 * @param udpPort
	 */
	public GameClient(Centurion<NWEntity> clientman, String ip, int tcpPort, int udpPort) {
		// Store the client entity manager
		clientManager = clientman;

		// Create the arraylist that holds the entities to add to the client after the update method.
		entitiesToAdd = new ArrayList<>();

		// Store the connection info
		IP = ip;
		TCP_PORT = tcpPort;
		UDP_PORT = udpPort;

		// Initialize the client
		client = NetworkFactory.getClient();
		Messaging.prepare(client);
		client.start();

		// Start the listener
		client.addListener(new ClientListener());

		// DEBUG
		// client.addListener(new DebugNetworkListener());

		// Try a first connection
		connect(IP, tcpPort, udpPort);

		// Register a component to add all entities to the client at the end of the entity manager update call.
		clientman.registerComponent("EntityAdderComponent", new ManagerComponentAdapter<NWEntity>() {
			@Override
			public void postUpdate(Centurion<NWEntity> manager, float delta) {
				if (entitiesToAdd.size() > 0) {
					for (NWEntity e : entitiesToAdd) {
						clientManager.addEntity(e);
					}
					entitiesToAdd.clear();
				}
			}
		});
	}

	/**
	 * Request to log in to the server with the player P.
	 * 
	 * @param p
	 *            The player requesting login
	 */
	public void logIn(Player p) {
		client.sendTCP(Login.make(p));
	}

	/**
	 * Requests to spawn at the server
	 */
	public void spawn(NWLiveEntity s, Player p) {
		client.sendTCP(SpawnPlayer.make(s, p));
	}

	/**
	 * Requests to move the ship at the server
	 * 
	 * @param shipID
	 *            The ship entity id
	 * @param dir
	 *            The direction to move to
	 */
	public void controlShip(String playerID, String shipID, String dir, boolean pressed) {
		client.sendTCP(ControlShip.make(playerID, shipID, dir, pressed));
	}

	/**
	 * Requests to move the ship at the server
	 * 
	 * @param shipID
	 *            The ship entity id
	 * @param dir
	 *            The direction to move to
	 */
	public void controlShip(ControlShip message) {
		client.sendTCP(message);
	}

	/**
	 * Process the message with all entities on the server after a login.
	 * 
	 * @param obj
	 *            The message obj
	 */
	public void receiveAllEntitiesOnServer(EntitiesOnTheServer obj) {
		// Messages that are maps
		HashMap<HashMap<String, Object>, String> messageData = obj.allEntitiesOnServer;

		NWEntity newEnt = null;
		for (Entry<HashMap<String, Object>, String> e : messageData.entrySet()) {
			newEnt = NWCreator.makeInstance(e.getValue(), e.getKey());
			if (newEnt != null) {
				entitiesToAdd.add(newEnt);
			}
		}
	}

	/**
	 * Process a new entities message sent from the server
	 * 
	 * @param ne
	 *            The new entities message
	 */
	private void processNewEntities(NewEntities ne) {
		// Messages that are maps
		HashMap<HashMap<String, Object>, String> mapMessages = ne.newEntities;

		// Add all new entities
		NWEntity newEnt = null;
		for (Entry<HashMap<String, Object>, String> e : mapMessages.entrySet()) {
			newEnt = NWCreator.makeInstance(e.getValue(), e.getKey());
			if (newEnt != null) {
				entitiesToAdd.add(newEnt);
			}
		}
	}

	/**
	 * Process a remove entities message sent from the server
	 * 
	 * @param re
	 *            The remove enties message
	 */
	private void processRemoveEntities(RemoveEntities re) {
		// Remove dead entities by the ID list
		Iterator<String> iter = re.removedEntities.iterator();
		while (iter.hasNext()) {
			String e = iter.next();
			clientManager.removeEntity(e);
		}
	}

	/**
	 * Process a snapshot that comes from the server.<br>
	 * This snapshot was already accepted as new.
	 * 
	 * @param snap
	 *            The snap from the server
	 */
	private void processSnapshot(Snapshot snap) {
		// Check if the snap is new
		if (snap.timestamp <= lastSnapshotTime)
			return;

		// Store the new snap time
		lastSnapshotTime = snap.timestamp;

		// Messages that are maps
		HashMap<String, HashMap<String, Object>> mapMessages = snap.updateMessages;

		//Update all entities alive
		Iterator<Entry<String, HashMap<String, Object>>> it = mapMessages.entrySet().iterator();
		while (it.hasNext()) {
			Entry<String, HashMap<String, Object>> entry = it.next();

			// Find the corresponding entity
			NWEntity ent = clientManager.getEntityByID(entry.getKey());
			if (ent != null) {
				// System.out.println("Got: " + entry.getValue());
				ent.nwReceive(entry.getValue());
			}
		}
	}

	/**
	 * Connects to a new location
	 * 
	 * @param ip
	 *            The location
	 * @param tcpPort
	 *            The TCP port
	 * @param udpPort
	 *            The UDP port
	 */
	public void connect(String ip, int tcpPort, int udpPort) {
		// Store the new connection info
		IP = ip;
		TCP_PORT = tcpPort;
		UDP_PORT = udpPort;
		connect();
	}

	/**
	 * Tries GameClient.RETRIES times to connect to the server at original IP and TCP/UDP ports
	 */
	public void connect() {
		// Try to connect several times
		do {
			try {
				client.connect(timeout, IP, TCP_PORT, UDP_PORT);
			} catch (Exception e) {
				e.printStackTrace();
			}
			// Error
			if (!client.isConnected())
				System.err.println("[" + RETRIES + "] Couldn't connect. Timeout exceeded: " + timeout);
		} while (!client.isConnected() && RETRIES-- > 0);

		if (RETRIES > 0)
			System.out.println("Connected.");
	}

	/**
	 * @return true if the client is connected to the server.
	 */
	public boolean isConnected() {
		return client.isConnected();
	}

	/**
	 * To use in order to process snapshots
	 * 
	 * @author Lucas M Carvalhaes
	 * 
	 */
	class ClientListener extends Listener {

		@Override
		public void connected(Connection con) {

		}

		@Override
		public void disconnected(Connection con) {

		}

		@Override
		public void idle(Connection con) {

		}

		@Override
		public void received(Connection con, Object obj) {
			// Receive new entities
			if (obj instanceof NewEntities)
				processNewEntities((NewEntities) obj);

			// Receive remove entities
			if (obj instanceof RemoveEntities)
				processRemoveEntities((RemoveEntities) obj);

			// Receive a snapshot
			if (obj instanceof Snapshot)
				processSnapshot((Snapshot) obj);

			// Receive already loggedin

			// Receive a entity ID for the ship
			if (obj instanceof AvaliableID)
				shipID = ((AvaliableID) obj).ID;

			// Receive the entities on the server after a succesful login
			if (obj instanceof EntitiesOnTheServer) {
				receiveAllEntitiesOnServer((EntitiesOnTheServer) obj);
			}

			// Player dropped - remove all his entities
			if (obj instanceof PlayerDropped) {
				clientManager.removeAllFromOwner(((PlayerDropped) obj).p.getPlayerID());
			}

		}

	}
}
