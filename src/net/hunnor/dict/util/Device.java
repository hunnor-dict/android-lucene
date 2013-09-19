package net.hunnor.dict.util;

/**
 *
 * Handles the device's hardware and operating system
 *
 * @author Ádám Z. Kövér
 *
 */
public class Device {

	public static final String appDirectory = "net.hunnor.dict.lucene";

	private Storage storage;
	private Network network;

	public Device() {
		storage = new Storage();
		if (storage.readable()) {
			storage.setAppDirectory(appDirectory);
		}
		network = new Network();
	}

	public Storage storage() {
		return storage;
	}

	public Network network() {
		return network;
	}

}
