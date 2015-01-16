package com.munepom.appmanager;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author munepom
 *
 */
public interface ApplicationInstanceUtil {

	Logger log = LoggerFactory.getLogger( ApplicationInstanceUtil.class );

	default InetAddress getLocalInetAddress() {
		try {
			return InetAddress.getByAddress(new byte[]{127,0,0,1});
		} catch (UnknownHostException e) {
			log.error(e.getMessage(), e);
		}
		return null;
	}

	default ServerSocket getServerSocket(int port, int backlog, InetAddress bindAddr) {
		ServerSocket socket = null;
		try {
			socket = new ServerSocket(port, backlog, bindAddr);
			log.info("Listening for application instances on socket {}", port);
		} catch (UnknownHostException e) {
			log.error(e.getMessage(), e);
			socket = null;
		} catch (IOException e) {
			log.warn("Port is already used.");
			socket = null;
		}

		return socket;
	}

	default Socket getClientSocket(InetAddress address, int port) {
		Socket socket = null;
		try {
			socket = new Socket(address, port);
		} catch (UnknownHostException e) {
			log.error(e.getMessage(), e);
			socket = null;
		} catch (IOException e1) {
			log.error("Error connecting to local port for single instance notification");
			log.error(e1.getMessage(), e1);
			socket = null;
		}
		return socket;
	}
}
