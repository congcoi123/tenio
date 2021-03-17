package com.tenio.core.monitoring;

import java.io.File;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.nio.charset.Charset;

import com.tenio.common.logger.AbstractLogger;

/**
 * For logging the system information
 * 
 * <h1>Operation System</h1>
 * <ul>
 * <li>Name</li>
 * <li>Version</li>
 * <li>Architecture</li>
 * </ul>
 * <h1>Disk</h1>
 * <ul>
 * <li>Total Space</li>
 * <li>Usable Space</li>
 * <li>Free Space</li>
 * </ul>
 * 
 * @author kong
 *
 */
public final class SystemInfo extends AbstractLogger {

	private static SystemInfo __instance;

	// preventing Singleton object instantiation from outside
	// creates multiple instance if two thread access this method simultaneously
	public static SystemInfo getInstance() {
		if (__instance == null) {
			__instance = new SystemInfo();
		}
		return __instance;
	}

	private SystemInfo() {
	}

	public final void logSystemInfo() {
		var logger = _buildgen("\n");
		var runtime = Runtime.getRuntime();

		logger.append("\t").append("Processor(s): ").append(runtime.availableProcessors()).append("\n");
		logger.append("\t").append("VM Max. memory: ").append(runtime.maxMemory() / 1000000L).append(" MB")
				.append("\n");

		for (var prop : SytemInfoType.values()) {
			logger.append("\t").append(prop.toString()).append(": ").append(System.getProperty(prop.getValue()))
					.append("\n");
		}

		logger.append("\t").append("Default charset: " + Charset.defaultCharset()).append("\n");

		_info("SYSTEM INFORMATION", logger);
	}

	public final void logNetCardsInfo() {
		var logger = _buildgen("\n");

		try {
			var list = NetworkInterface.getNetworkInterfaces();

			while (list.hasMoreElements()) {
				NetworkInterface iFace = (NetworkInterface) list.nextElement();
				logger.append("\t").append("Card: ").append(iFace.getDisplayName()).append("\n");

				var addresses = iFace.getInetAddresses();

				while (addresses.hasMoreElements()) {
					InetAddress adr = (InetAddress) addresses.nextElement();
					logger.append("\t").append("\t").append(" ->").append(adr.getHostAddress()).append("\n");
				}
			}

			_info("NETWORK INFORMATION", logger);

		} catch (SocketException e) {
			_error(e, "Exception while discovering network cards");
		}
	}

	public final void logDiskInfo() {
		var logger = _buildgen("\n");

		/* Get a list of all file system roots on this system */
		File[] roots = File.listRoots();

		/* For each file system root, print some info */
		for (File root : roots) {
			logger.append("File system root: ").append(root.getAbsolutePath()).append("\n");
			logger.append("Total space (bytes): ").append(root.getTotalSpace()).append("\n");
			logger.append("Free space (bytes): ").append(root.getFreeSpace()).append("\n");
			logger.append("Usable space (bytes): ").append(root.getUsableSpace()).append("\n");
		}

		_info("DISK INFORMATION", logger);
	}
}
