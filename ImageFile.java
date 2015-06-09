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
import java.awt.image.BufferedImage;

public class ImageFile implements Comparable<ImageFile> {
	BufferedImage f;
	double sim;

	public ImageFile(BufferedImage f, double sim) {
		this.f = f;
		this.sim = sim;
	}

	@Override
	public int compareTo(ImageFile o) {
		return Double.compare(sim, o.sim);
	}

	@Override
	public String toString() {
		return f.toString() + ": similarity " + sim + "to query [0--100]";
	}
}