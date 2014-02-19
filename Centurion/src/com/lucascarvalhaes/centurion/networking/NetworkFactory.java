package com.lucascarvalhaes.centurion.networking;

import java.io.IOException;

import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Server;

public class NetworkFactory {

	public static Server getServer(int tcpPort, int udpPort){
		// Make the server - double the default size: 16384 / 2048
		Server ret = new Server(32768, 4096);

		//Make it work
		try {
			ret.start();
			ret.bind(tcpPort, udpPort);
		} catch (IOException e) {
			e.printStackTrace();
		}

		//Return it,
		return ret;
	}

	public static Client getClient() {
		// Default write buffer size: 8192
		// Default object buffer size: 2048
		return new Client(16384, 4096);
	}
}
