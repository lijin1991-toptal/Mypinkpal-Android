package com.brodev.socialapp.cache;

import java.util.HashSet;
import java.util.LinkedList;

public class UniqueSynchronizedQueue<E> {
	private final HashSet<E> set = new HashSet<E>();
	private final LinkedList<E> queue = new LinkedList<E>();

	public synchronized void enqueue(E object) {
		if (set.add(object)) {
			queue.addLast(object);
		}
	}

	public synchronized E dequeue() {

		if (queue.isEmpty())
			return null;

		final E result = queue.removeFirst();
		set.remove(result);
		return result;
	}
}
