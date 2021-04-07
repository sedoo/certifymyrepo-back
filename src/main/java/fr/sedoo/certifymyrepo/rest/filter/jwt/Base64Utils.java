package fr.sedoo.certifymyrepo.rest.filter.jwt;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class Base64Utils {

	private Base64Utils() {}

	public static String encode(String value) throws Exception {
		return  Base64.getEncoder().encodeToString(value.getBytes(StandardCharsets.UTF_8));
	}

	public static String decode(String value) throws Exception {
		byte[] decodedValue = Base64.getDecoder().decode(value);  // Basic Base64 decoding
		return new String(decodedValue, StandardCharsets.UTF_8);
	}

}


