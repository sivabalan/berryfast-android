package com.fruitmill.berryfast;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;

public abstract class Actor extends Thread {
	private String name;

	private volatile boolean stop;

	private BlockingQueue<Object[]> queue;

	public Actor() {
		name = this.getClass().getSimpleName();
		queue = new LinkedBlockingDeque<>();
		stop = false;
		start();
	}

	protected void stopMyself() {
		this.stop = true;
	}

	@Override
	public void run() {
		while (!stop) {
			Object[] message;
			try {
				message = queue.take();
				dispatch(message);
				if ("die".equals(message[0])) {
					stopMyself();
				}
			} catch (InterruptedException e) {
				stopMyself();
			}
		}
	}

	@Override
	public String toString() {
		return this.name;
	}

	public static void send(Actor recipient, Object[] message) {
		try {
			recipient.queue.put(message);
		} catch (InterruptedException e) {
			System.err.println("Message could not be delivered to " + recipient);
		}
	}

	protected abstract void dispatch(Object[] message);
}
