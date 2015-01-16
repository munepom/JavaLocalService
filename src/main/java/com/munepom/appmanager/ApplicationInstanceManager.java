package com.munepom.appmanager;

import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author munepom
 *
 */
public class ApplicationInstanceManager implements ApplicationInstanceProps, ApplicationInstanceUtil {

	private static Logger log = LoggerFactory.getLogger( ApplicationInstanceManager.class );

	/**
	 * アプリケーション制御用 ServerSocket を作成します。
	 * Registers this instance of the application.
	 *
	 * @return true if first instance, false if not.
	 */
	public static synchronized boolean registerInstance(int port, String threadName, ApplicationInstanceListener... listenerArray) throws Exception {
		ApplicationInstanceManager clsThis = new ApplicationInstanceManager();

		// returnValueOnError should be true if lenient (allows app to run on network error) or false if strict.
		boolean isFirstInstance = false;

		// try to open network socket
		// if success, listen to socket for new instance message, return true
		// if unable to open, connect to existing and send new instance message, return false
		//ServerSocket の close は、ApplicationInstanceRunnable 側で行う
		ServerSocket socket = clsThis.getServerSocket(port, 10, clsThis.getLocalInetAddress());
		if( socket != null ) {
			isFirstInstance = true;

			// アプリケーション制御 Runnable 生成
			ApplicationInstanceRunnable runnable = new ApplicationInstanceRunnable(socket);
			runnable.setSubListenerArray(listenerArray);

			ExecutorService service = Executors.newFixedThreadPool(1,  r -> {
				// daemon 化しておくと、メインスレッド終了後にシャットダウンしてくれる
				Thread t = new Thread(r, threadName);
				t.setDaemon(false);
				return t;
			});
			try {
				service.execute(runnable);
			}
			finally {
			//shutdown をお忘れなく。すぐに停止するわけではなく、現在のタスクが完了後にシャットダウンしてくれる。
				service.shutdown();
			}
		}

		return isFirstInstance;
	}

	public static synchronized void sendKey(int port, String key,  boolean isFirstInstance) {
		log.info("Key : {}", key);
		boolean isStartKey = SingleInstanceSharedKey.Start.getTrimmedKey().equals(key);
		if ( ! isFirstInstance && isStartKey ) {
			log.warn("Service has already launched.");
			return;
		}

		ApplicationInstanceManager clsThis = new ApplicationInstanceManager();

		try ( Socket clientSocket = clsThis.getClientSocket(clsThis.getLocalInetAddress(), port) ) {
			try ( OutputStream out = clientSocket.getOutputStream() ) {
				out.write(key.getBytes());
			}
			catch (IOException e) {
				log.error("Error sending key.");
			}
		}
		catch (IOException e) {
			log.error("Error connecting to local port for single instance notification.");
		}

	}

}
