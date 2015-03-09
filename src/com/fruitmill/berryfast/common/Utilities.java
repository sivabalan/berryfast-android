package com.fruitmill.berryfast.common;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.List;

import android.graphics.Color;

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

//	public static List<Integer> generateLEDColor(List<Integer> args) {
//		if(args == null)
//		{
//			args = Arrays.asList(255, 0, 0);
//		}
//		else
//		{
//			args.set(0, (args.get(0) + 20)%256);
//			args.set(1, (args.get(1) + 20)%256);
//			args.set(2, (args.get(2) + 20)%256);
//		}
//		return args;
//	}
	
	public static double[] generateLEDColor(double[] args) {
		if(args == null)
		{
			args = new double[]{0, 255, 0};
		}
		else
		{
			if(args[0] < 255)
			{
				args[0] = Math.pow(2,(Math.log(args[0]+1)/Math.log(2))+0.01); 
				//args[0] = (args[0] + 1)%256;
				args[1] = Math.pow(2,(Math.log(args[1])/Math.log(2))-0.01);
			}
			args[2] = 0;
			
		}
		return args;
	}
}
