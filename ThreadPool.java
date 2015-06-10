/*
 *
 * Copyright (c) 2015, Stefan Petscharnig. All rights reserved.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301 USA
 */

public class ThreadPool {
	public static boolean busy() {
		return cnt > 0;
	}

	public static boolean execute(Runnable r) {
		if (cnt >= maxThreads)
			return false;

		Thread t = new Thread(new Runnable() {
			@Override
			public void run() {
				r.run();
				synchronized (monitor) {
					cnt--;
				}
			}
		});

		synchronized (monitor) {
			cnt++;
		}
		t.start();

		return true;
	}

	public static void executeandWait(Runnable r) {
		while (!execute(r)) {
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	static {
		monitor = new Object();
	}
	public static Object monitor;
	public static int cnt = 0;
	public static int maxThreads = 12;

}