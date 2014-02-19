package com.lucascarvalhaes.centurion.networking.gameModel;

import java.util.HashMap;

public abstract class NWCreator implements NWInterface {

	/**
	 * This method will create an instance and apply its create message.<br>
	 * The object returned whould be compliant to the classname provided and<br>
	 * its attributes should be the ones passed in the create message.
	 * 
	 * @param className
	 *            The class name to instantiate
	 * @param msg
	 *            The message to apply
	 * @return If successfull, an instance of the class with the nwApply already applyied
	 */
	public static NWEntity makeInstance(String className, HashMap<String, Object> msg) {
		Class<?> cl = null;
		NWEntity ent = null;
		Object inst = null;
		try {
			cl = Class.forName(className);
			inst = cl.newInstance();
		} catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
			System.out.println("COULDN'T MAKE!");
			e.printStackTrace();
		}

		// Check if we got a valid instance of a nwentity
		if (inst instanceof NWEntity)
			ent = (NWEntity) inst;

		// Try to apply the create message
		if (ent != null)
			ent.nwApplyCreateMessage(msg);

		// Return it
		return ent;
	}

}
