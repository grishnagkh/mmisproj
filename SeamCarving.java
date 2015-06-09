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
import java.awt.image.DataBufferByte;

public class SeamCarving {

	public double[][] getEnergyMap(int[][] in, int imgW, int imgH) {
		double Gx[][], Gy[][], G[][];
		Gx = new double[imgW][imgH];
		Gy = new double[imgW][imgH];
		G = new double[imgW][imgH];

		int[][] img = new int[imgW + 2][imgH + 2]; // enlarge image
		for (int x = 0; x < imgW; x++) {
			System.arraycopy(in[x], 0, img[x + 1], 1, imgH);
		}
		// (int) (Math.random() * 255);
		for (int x = 0; x < imgW + 2; x++) {
			img[x][0] = (int) (Math.random() * 255);
			img[x][imgH + 1] = (int) (Math.random() * 255);
		}
		for (int y = 0; y < imgH + 2; y++) {
			img[0][y] = (int) (Math.random() * 255);
			img[imgW + 1][y] = (int) (Math.random() * 255);
		}

		for (int i = 1; i < 1 + imgW; i++) {
			for (int j = 1; j < 1 + imgH; j++) {
				if (j == 0 || j == imgH - 1) {
					Gx[i - 1][j - 1] = Gy[i - 1][j - 1] = G[i - 1][j - 1] = 0; // Image
																				// boundary
					// cleared
				} else {
					Gx[i - 1][j - 1] = img[i + 1][j - 1] + 2 * img[i + 1][j]
							+ img[i + 1][j + 1] - img[i - 1][j - 1] - 2
							* img[i - 1][j] - img[i - 1][j + 1];
					Gy[i - 1][j - 1] = img[i - 1][j + 1] + 2 * img[i][j + 1]
							+ img[i + 1][j + 1] - img[i - 1][j - 1] - 2
							* img[i][j - 1] - img[i + 1][j - 1];
					G[i - 1][j - 1] = Math.abs(Gx[i - 1][j - 1])
							+ Math.abs(Gy[i - 1][j - 1]);
				}
			}
		}
		return G;
	}

	public BufferedImage seamCarve(BufferedImage orig) {

		int width = orig.getWidth();
		int height = orig.getHeight();

		BufferedImage out = orig;
		int[][] greyscale = new int[width][height];

		int[] arr = new int[3];

		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				orig.getRaster().getPixel(x, y, arr);
				greyscale[x][y] =
				// (arr[0] + arr[1] + arr[2]) / 3;
				(int) (0.299 * arr[0] + 0.587 * arr[1] + 0.114 * arr[2]);
				//  (3 * arr[0] + 6*arr[1] + 1 * arr[2])/10;
				//  (5 * arr[0] + 9*arr[1] + 2 * arr[2])>>4;
			}
		}

		double[][] energy = getEnergyMap(greyscale, width, height);

		// compute cumulated energy map
		double[][] cenergy = new double[width][height];

		final int L = -1, R = 1, U = 0;

		double l, r, u; // left, right upper
		double tmp;
		int[][] pathMap = new int[width][height];
		for (int x = 0; x < width; x++) {
			cenergy[x][0] = energy[x][0];
			pathMap[x][0] = U;
		}

		for (int y = 1; y < height; y++) {
			// x=0:
			r = cenergy[0 + R][y - 1];
			u = cenergy[0 + U][y - 1];
			tmp = r < u ? r : u;
			pathMap[0][y] = r < u ? R : U;
			cenergy[0][y] = tmp + energy[0][y];

			// 0<x<energy[x].length-1
			for (int x = 1; x < width - 1; x++) {
				l = cenergy[x + L][y - 1];
				r = cenergy[x + R][y - 1];
				u = cenergy[x + U][y - 1];

				tmp = l < r ? l : r;
				pathMap[x][y] = l < r ? L : R;
				tmp = tmp < u ? tmp : u;
				pathMap[x][y] = pathMap[x][y] < u ? pathMap[x][y] : U;
				cenergy[x][y] = tmp + energy[x][y];
			}
			// y=energy[x].length-1
			l = cenergy[width - 1 + L][y - 1];
			u = cenergy[width - 1 + U][y - 1];
			tmp = l < u ? l : u;
			pathMap[width - 1][y] = l < u ? L : U;
			cenergy[width - 1][y] = tmp + energy[width - 1][y];
		}

		// find optimal seam

		int[] optseam = new int[height];
		double startV = 0;
		int startI = 0;
		// find starting point:
		for (int i = 1; i < width; i++) {
			if (cenergy[i][height - 1] < startV) {
				startV = cenergy[i][cenergy[0].length - 1];
				startI = i;
			}
		}
		optseam[0] = startI;
		for (int y = 1; y < height; y++) {
			optseam[y] = optseam[y - 1] + pathMap[optseam[y - 1]][y - 1];
		}

		out = new BufferedImage(width - 1, height, orig.getType());

		byte[] src = ((DataBufferByte) orig.getRaster().getDataBuffer())
				.getData();
		byte[] dest = ((DataBufferByte) out.getRaster().getDataBuffer())
				.getData();

		int nbands = 3;
		int rowstart = 0;
		int toCopy, copyLeft, copyRight;
		toCopy = nbands * (width - 1);

		for (int y = 0; y < height; y++, rowstart += nbands * width) {
			copyLeft = nbands * optseam[y];
			copyRight = toCopy - (nbands * optseam[y]);
			System.arraycopy(src, rowstart, dest, rowstart - (nbands * y),
					copyLeft);
			System.arraycopy(src, rowstart + copyLeft + nbands, dest, rowstart
					- (nbands * y) + copyLeft, copyRight);
		}

		return out;

	}

	public BufferedImage seamCarve(BufferedImage orig, int toRemove) {

		/* split input image */
		int w = orig.getWidth();
		int h = orig.getHeight();

		int w1 = w >> 1;
		int w2 = w - w1;

		BufferedImage t1 = new BufferedImage(w1, h, orig.getType());
		BufferedImage t2 = new BufferedImage(w2, h, orig.getType());

		byte[] src = ((DataBufferByte) orig.getRaster().getDataBuffer())
				.getData();
		byte[] dest1 = ((DataBufferByte) t1.getRaster().getDataBuffer())
				.getData();
		byte[] dest2 = ((DataBufferByte) t2.getRaster().getDataBuffer())
				.getData();

		for (int row = 0; row < h; row++) {
			System.arraycopy(src, 3 * row * w, dest1, 3 * w1 * row, 3 * w1);
			System.arraycopy(src, 3 * row * w + 3 * w1, dest2, 3 * w2 * row,
					3 * w2);
		}
		for (int i = 0; i < toRemove; i++) {
			t1 = seamCarve(t1);
			w1--;
			t2 = seamCarve(t2);
			w2--;
		}
		w = w1 + w2;
		BufferedImage tmp = new BufferedImage(w, h, orig.getType());

		byte[] src1 = ((DataBufferByte) t1.getRaster().getDataBuffer())
				.getData();
		byte[] src2 = ((DataBufferByte) t2.getRaster().getDataBuffer())
				.getData();
		byte[] dest = ((DataBufferByte) tmp.getRaster().getDataBuffer())
				.getData();

		for (int row = 0; row < h; row++) {
			System.arraycopy(src1, 3 * row * w1, dest, 3 * row * w, 3 * w1);
			System.arraycopy(src2, 3 * row * w2, dest, 3 * row * w + 3 * w1,
					3 * w2);
		}

		return tmp;
	}

}