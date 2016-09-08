package com.simplexservers.minecraft.mojangapi;

import java.util.UUID;

/**
 * Basic utility functions for Minecraft.
 */
public class MinecraftUtil {

	/**
	 * Checks if the username is in the proper Minecraft format.
	 *
	 * @param username The username to check.
	 * @return true if the username is a valid Minecraft format, false if it's not.
	 */
	public static boolean isValidUsername(String username) {
		return username.matches("^[a-zA-Z0-9_]{2,16}$");
	}

	/**
	 * Converts the hexadecimal UUID string to a Java UUID object.
	 *
	 * @param uuidString The string to convert to a UUID.
	 * @param hasHyphens If the UUID string is hyphenated to the RFC standard.
	 * @return The Java UUID
	 */
	public static UUID uuidFromString(String uuidString, boolean hasHyphens) {
		if (hasHyphens) {
			return UUID.fromString(uuidString);
		}

		// Format the string with hyphens
		String withHyphens = uuidString.replaceAll("^(\\w{8})(\\w{4})(\\w{4})(\\w{4})(\\w{12})$", "$1-$2-$3-$4-$5");
		return UUID.fromString(withHyphens);
	}

}
