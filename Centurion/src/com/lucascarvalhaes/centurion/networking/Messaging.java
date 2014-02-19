package com.lucascarvalhaes.centurion.networking;

import java.util.ArrayList;
import java.util.HashMap;

import com.badlogic.gdx.math.Vector2;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.serializers.DeflateSerializer;
import com.esotericsoftware.kryonet.EndPoint;
import com.lucascarvalhaes.centurion.model.Player;
import com.lucascarvalhaes.centurion.networking.gameModel.NWEntity;
import com.lucascarvalhaes.centurion.networking.gameModel.NWLiveEntity;

/**
 * All the messages between the server and the client
 * 
 * @author Lucas Carvalhaes
 * 
 */
public class Messaging {

	public static final int	PORT_TCP	= 27012, PORT_UDP = 27013;

	/**
	 * This registers objects that are going to be sent over the network.
	 * 
	 * @param endPoint
	 *            The server or client to be prepared to talk for this app.
	 */
	public static void prepare(EndPoint endPoint) {
		Kryo kryo = endPoint.getKryo();

		// Inner classes
		kryo.register(Player.class);
		kryo.register(HashMap.class);
		kryo.register(ArrayList.class);
		kryo.register(Vector2.class);

		// Register message classes
		kryo.register(Login.class);
		kryo.register(AlreadyLoggedIn.class);
		kryo.register(SpawnPlayer.class);
		kryo.register(SpawnEntity.class);
		kryo.register(SpawnLiveEntity.class);
		kryo.register(AvaliableID.class);

		// Server to client
		kryo.register(Snapshot.class, new DeflateSerializer(kryo.getDefaultSerializer(Snapshot.class)));
		kryo.register(NewEntities.class, new DeflateSerializer(kryo.getDefaultSerializer(NewEntities.class)));
		kryo.register(
				RemoveEntities.class,
				new DeflateSerializer(kryo.getDefaultSerializer(RemoveEntities.class)));

		kryo.register(
				EntitiesOnTheServer.class,
				new DeflateSerializer(kryo.getDefaultSerializer(EntitiesOnTheServer.class)));

		// Client to server
		kryo.register(ControlShip.class);

	}

	/*
	 * Messages
	 */

	/*
	 * CLIENT TO SERVER
	 */

	/**
	 * A client asks the server to move the ship.
	 * 
	 * @author Lucas M Carvalhaes
	 * 
	 */
	public static class ControlShip {
		public String	entityID;
		public String	ownerID;
		public String	dir;
		public boolean	pressed;

		public static final String	LEFT	= "left", RIGHT = "right", UP = "up", DOWN = "down",
				FIRE = "fire";

		public static ControlShip make(String ownerID, String shipID, String command, boolean pressed) {
			ControlShip ret = new ControlShip();
			ret.entityID = shipID;
			ret.ownerID = ownerID;
			ret.pressed = pressed;
			ret.dir = command;
			return ret;
		}
	}

	/**
	 * Client asks to spawn his ship
	 * 
	 * @author Lucas M Carvalhaes
	 * 
	 */
	public static class SpawnPlayer {
		public HashMap<String, Object>	entCreateMsg;
		public Player					p;

		public static SpawnPlayer make(NWLiveEntity ent, Player owner) {
			SpawnPlayer sp = new SpawnPlayer();
			sp.entCreateMsg = ent.nwCreate();
			sp.p = owner;
			return sp;
		}
	}

	/**
	 * Client asks to login to the game server
	 * 
	 * @author Lucas M Carvalhaes
	 * 
	 */
	public static class Login {
		public Player	p;

		public static Login make(Player p) {
			Login log = new Login();
			log.p = p;
			return log;
		}
	}

	/*
	 * SERVER TO CLIENT
	 */

	/**
	 * Before the client asks the server to create an entity,<br>
	 * since ID's are controlled by the server, the server should <br>
	 * send this message so that the client changes the local entity<br>
	 * ID to match the one on the server.
	 * 
	 * @author Lucas M Carvalhaes
	 * 
	 */
	public static class AvaliableID {
		public String	ID;

		public static AvaliableID make(String id) {
			AvaliableID ret = new AvaliableID();
			ret.ID = id;
			return ret;
		}
	}

	/**
	 * The server will send this to each player after a sucessful login.<br>
	 * This message contains a package with information to create all entities<br>
	 * currently on the server.
	 * 
	 * @author Lucas M Carvalhaes
	 * 
	 */
	public static class EntitiesOnTheServer {
		public HashMap<HashMap<String, Object>, String>	allEntitiesOnServer;

		public static EntitiesOnTheServer make(HashMap<HashMap<String, Object>, String> allEnts) {
			EntitiesOnTheServer inst = new EntitiesOnTheServer();
			inst.allEntitiesOnServer = allEnts;
			return inst;
		}
	}

	/**
	 * Message the player his login was invalid
	 * 
	 * @author Lucas M Carvalhaes
	 * 
	 */
	public static class AlreadyLoggedIn {
	}

	/**
	 * A spawn entity message
	 * 
	 * @author Lucas M Carvalhaes
	 * 
	 */
	public static class SpawnEntity {
		public HashMap<String, Object>	entCreateMsg;
		public String					ownerID;

		public static SpawnEntity make(NWEntity ent, String owner) {
			SpawnEntity ret = new SpawnEntity();
			ret.entCreateMsg = ent.nwCreate();
			ret.ownerID = owner;
			return ret;
		}
	}

	/**
	 * A spawn live entity message
	 * 
	 * @author Lucas M Carvalhaes
	 * 
	 */
	public static class SpawnLiveEntity {
		public HashMap<String, Object>	entCreateMsg;
		public String					ownerID;

		public static SpawnLiveEntity make(NWLiveEntity ent, String owner) {
			SpawnLiveEntity ret = new SpawnLiveEntity();
			ret.entCreateMsg = ent.nwCreate();
			ret.ownerID = owner;
			return ret;
		}
	}

	/**
	 * The server pushes news each sim
	 * 
	 * @author Lucas
	 * 
	 */
	public static class Snapshot {
		/**
		 * The time the snapshot was made
		 */
		public long										timestamp;
		/**
		 * A map that maps the entity ID to its update message
		 */
		public HashMap<String, HashMap<String, Object>>	updateMessages;

		public static Snapshot make(long stamp, HashMap<String, HashMap<String, Object>> updateMessages) {
			Snapshot ret = new Snapshot();
			ret.timestamp = stamp;
			ret.updateMessages = updateMessages;

			return ret;
		}
	}

	/**
	 * A message generated with the snapshot when entities were removed.
	 * 
	 * @author Lucas M Carvalhaes
	 * 
	 */
	public static class RemoveEntities {
		/**
		 * Just a list containing all entities ID's to remove
		 */
		public ArrayList<String>	removedEntities;

		public static RemoveEntities make(ArrayList<String> rmvEnts) {
			RemoveEntities ret = new RemoveEntities();
			ret.removedEntities = rmvEnts;
			return ret;
		}

	}

	/**
	 * A message generated with the snapshot when entities were created.
	 * 
	 * @author Lucas M Carvalhaes
	 * 
	 */
	public static class NewEntities {
		/**
		 * A map that maps a class name and the information to apply after<br>
		 * instantiation on the nwMakeInstance method.<br>
		 * This maps CreateMessage -> ClassName
		 */
		public HashMap<HashMap<String, Object>, String>	newEntities;

		public static NewEntities make(HashMap<HashMap<String, Object>, String> newEntities2) {
			NewEntities ret = new NewEntities();
			ret.newEntities = newEntities2;
			return ret;
		}
	}
}

//Possible serializer
//https://groups.google.com/forum/#!topic/kryonet-users/i3I60OWGGuE
/*
 * public void write (Kryo kryo, Output output, Object object) { Deflater deflater = new Deflater(compressionLevel, noHeaders); OutputChunked
 * outputChunked = new OutputChunked(output, 256); DeflaterOutputStream deflaterStream = new DeflaterOutputStream(outputChunked, deflater); Output
 * deflaterOutput = new Output(deflaterStream, 256); kryo.writeObject(deflaterOutput, object, serializer); deflaterOutput.flush(); try {
 * deflaterStream.finish(); } catch (IOException ex) { throw new KryoException(ex); } outputChunked.endChunks(); }
 * 
 * public Object read (Kryo kryo, Input input, Class type) { InflaterInputStream inflaterStream = new InflaterInputStream(new InputChunked(input,
 * 256), new Inflater(noHeaders)); return kryo.readObject(new Input(inflaterStream, 256), type, serializer); }
 */