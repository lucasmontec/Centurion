package com.lucascarvalhaes.centurion.testing;

import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;

public class DebugNetworkListener extends Listener {

	@Override
	public void connected(Connection arg0) {
		System.out.println("Connection received["+arg0.getID()+"]: "+arg0.getRemoteAddressTCP());
		super.connected(arg0);
	}

	@Override
	public void disconnected(Connection arg0) {
		System.out.println("Connection lost["+arg0.getID()+"]");
		super.disconnected(arg0);
	}

	@Override
	public void received(Connection arg0, Object obj) {
		System.out.println("Received: "+obj.getClass().getSimpleName());
		super.received(arg0, obj);
	}

}
