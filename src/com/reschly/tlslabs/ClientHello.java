package com.reschly.tlslabs;

import java.io.UnsupportedEncodingException;
import java.util.Random;

public class ClientHello
{
	private byte contentType;
	private int version;
	private int recordLength;
	private byte handshakeType;
	private int protocolLength;
	private int version2;
	private byte[] random;
	private byte sessionIDLength;
	private CipherSuite[] ciphers;
	private byte compressionLength;
	private byte compression;
	private int extensionLength;
	private byte[] supportedCurves = {00, 0x0a, 00, 0x08, 00, 06, 00, 0x17, 00, 0x18, 00, 0x19};
	private byte[] curveFormats = { 00, 0x0b, 00, 02, 01, 00 };
	private byte[] sni;
	Random rand = new Random();
	
	public ClientHello()
	{
		this(null);
	}
	
	public ClientHello(String hostname) 
	{
		contentType = 0x16;
		version = 0x0301;
		handshakeType = 1;
		version2 = 0x0303;
		sessionIDLength = 0;
		compressionLength = 1;
		compression = 0;
		setSni(hostname);
		extensionLength = supportedCurves.length + curveFormats.length + sni.length;
		random = new byte[32];
		rand.nextBytes(random);
	}
	
	private void setSni(String hostname)
	{
		if (hostname == null)
		{
			sni = new byte[0];
			return;
		}
		sni = new byte[hostname.length() + 9];
		sni[0] = 0; // type=0x0000
		sni[1] = 0; 
		sni[2] = (byte) (((hostname.length() + 5) >> 8) & 0xff); // extension length
		sni[3] = (byte) ((hostname.length() + 5) & 0xff);
		sni[4] = (byte) (((hostname.length() + 3) >> 8) & 0xff); // server name list length
		sni[5] = (byte) ((hostname.length() + 3) & 0xff);
		sni[6] = 0; // server name type = hostname
		sni[7] = (byte) ((hostname.length() >> 8) & 0xff);
		sni[8] = (byte) (hostname.length() & 0xff);
		try
		{
			System.arraycopy(sni, 0, hostname.getBytes("US-ASCII"), 9, hostname.getBytes("US-ASCII").length);
		} 
		catch (UnsupportedEncodingException e)
		{
			sni = new byte[0];
		}
	}
	
	public void setCiphers(CipherSuite[] ciph)
	{
		ciphers = ciph;
	}
	
	public byte[] toByteArray()
	{
		int tlsLength = 1 + 2 + 2 + 1 + 3 + 2 + 32 + 1 + 2 + (2*ciphers.length) + 1 + 1 + 2 + extensionLength;
		recordLength = tlsLength - 5;
		protocolLength = recordLength - 4;
		
		byte[] result = new byte[tlsLength];
		result[0] = contentType;
		result[1] = (byte)((version>>8)&0xff);
		result[2] = (byte)(version&0xff);
		result[3] = (byte)((recordLength>>8)&0xff);
		result[4] = (byte)(recordLength&0xff);
		result[5] = handshakeType;
		result[6] = (byte)((protocolLength>>16)&0xff);
		result[7] = (byte)((protocolLength>>8)&0xff);
		result[8] = (byte)((protocolLength)&0xff);
		result[9] = (byte)((version2>>8)&0xff);
		result[10] = (byte)(version2&0xff);
		System.arraycopy(random, 0, result, 11, 32);
		result[43] = sessionIDLength;
		result[44] = (byte)(((ciphers.length*2)>>8)&0xff);
		result[45] = (byte)((ciphers.length*2)&0xff);
		int i = 46;
		for (CipherSuite cs : ciphers)
		{
			result[i] = (cs.getSuite()[0]);
			result[i+1] = (cs.getSuite()[1]);
			i+=2;
		}
		result[i] = compressionLength;
		result[i+1] = compression;
		i+=2;
		result[i] = (byte)((extensionLength>>8)&0xff);
		result[i+1] = (byte)(extensionLength&0xff);
		i+=2;
		System.arraycopy(sni, 0, result, i, sni.length);
		i+= sni.length;
		System.arraycopy(supportedCurves, 0, result, i, supportedCurves.length);
		i+=supportedCurves.length;
		System.arraycopy(curveFormats, 0, result, i, curveFormats.length);
		i+=curveFormats.length;
				
		return result;
		
	}

}

