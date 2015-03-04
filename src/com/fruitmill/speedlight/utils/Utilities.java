package com.fruitmill.speedlight.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Scanner;

public class Utilities {
	private Utilities() {}
	
	public static String generateNonce(String s) {
	
		MessageDigest digest;
		try 
		{
			digest = MessageDigest.getInstance("MD5");
			digest.update(s.getBytes(),0,s.length());
			String hash = new BigInteger(1, digest.digest()).toString(16);
			return shaOne(hash);
		} 
		catch (NoSuchAlgorithmException e) 
		{
			e.printStackTrace();
		}
		return "";
		
	}
	
	private static String shaOne(String s) throws NoSuchAlgorithmException {
		MessageDigest md = MessageDigest.getInstance("SHA-1");
		md.update(s.getBytes());
		byte[] bytes = md.digest();
		StringBuffer buffer = new StringBuffer();
		for (int i = 0; i < bytes.length; i++) {
			String tmp = Integer.toString((bytes[i] & 0xff) + 0x100, 16).substring(1);
			buffer.append(tmp);
		}
		return buffer.toString();
	}

}
