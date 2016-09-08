package com.simplexservers.minecraft.mojangapi;

/**
 * An exception that occurred with the MojangAPI.
 */
public class MojangAPIException extends Exception {

	public MojangAPIException(String message) {
		super(message);
	}

	public MojangAPIException(String message, Throwable cause) {
		super(message, cause);
	}

	public MojangAPIException(Throwable cause) {
		super(cause);
	}

}
