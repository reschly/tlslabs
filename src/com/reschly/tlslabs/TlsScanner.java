package com.reschly.tlslabs;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.util.Arrays;

public class TlsScanner
{	
	public static String doScan(String host, int port) throws IOException
	{
		StringBuilder result = new StringBuilder();
		CipherSuite[] ciphers = CipherSuite.getAllCipherSuites();
		byte[] response = new byte[8192];
		
		result.append("Host: " + host);
		result.append("\nPort: " + Integer.toString(port));
		result.append("\nIP: " + InetAddress.getByName(host).getHostAddress());
		try
		{
			while (ciphers.length > 0)
			{
				ClientHello ch = new ClientHello(host);
				ch.setCiphers(ciphers);
				byte[] tlsBytes = ch.toByteArray();

				Socket sock = new Socket(); // = new Socket(host, port);
				sock.setSoTimeout(5000);
				sock.connect(new InetSocketAddress(host, port), 5000);

				sock.getOutputStream().write(tlsBytes);
				int respLength = sock.getInputStream().read(response);
				if ((respLength < 1) || (response[0] != 0x16))
				{
					System.out.println("\nno (more) supported ciphers");
					break;
				}
				sock.close();
				int chosenCipherOffset = 1 + 2 + 2 + 1 + 3 + 2 + 32;
				chosenCipherOffset += response[chosenCipherOffset];
				chosenCipherOffset++;

				byte[] chosenCipher = { response[chosenCipherOffset], response[chosenCipherOffset+1] };

				CipherSuite[] newCiphers = new CipherSuite[ciphers.length-1];
				int i = 0;

				for (CipherSuite cs : ciphers)
				{
					if (Arrays.equals(chosenCipher, cs.getSuite()))
					{
						result.append("\nSupports 0x" + 
								Integer.toHexString(((chosenCipher[0]&0xff)*256) + (chosenCipher[1]&0xff)) +
								" (" + cs.getDescription() + ")");
					}
					else
					{
						newCiphers[i] = cs;
						i++;
					}
				}
				ciphers = newCiphers;
			}
		}
		catch (SocketException se)
		{
			
		}
		
		return result.toString();
	}
	
}
