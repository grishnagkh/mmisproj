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
import java.util.Map;

public class FeatureExtractor {
	public static void extractFeature(BufferedImage bimg,
			Map<BufferedImage, double[]> addTo) {

		int imgH = bimg.getHeight();
		int imgW = bimg.getWidth();

		int NCOL = 8, NDIR = 8;
		double[] features = new double[NCOL * NDIR];

		int[][] greyscalePic = new int[imgW][imgH];

		int[] arr = new int[3];

		for (int x = 0; x < imgW; x++) {
			for (int y = 0; y < imgH; y++) {
				bimg.getRaster().getPixel(x, y, arr);
				greyscalePic[x][y] = (arr[0] + arr[1] + arr[2]) / 3;
				// return (int) (0.299 * arr[0] + 0.587*arr[1] + 0.114 *
				// arr[2]);
				// return (3 * arr[0] + 6*arr[1] + 1 * arr[2])/10;
				// return (5 * arr[0] + 9*arr[1] + 2 * arr[2])>>4;
			}
		}


		for (int x = 0; x < imgW; x++) {
			for (int y = 0; y < imgH; y++) {
				int pixel = bimg.getRGB(x, y);

				int bin = -1;

				double deltaH = 0;
				double deltaV = 0;

				for (int i = 0; i < 3; i++) {
					for (int j = 0; j < 3; j++) {
						int px = Math.max(0, x - 1 + i);
						int py = Math.max(0, y - 1 + j);
						px = imgW > px ? px : imgW - 1;
						py = imgH > py ? py : imgH - 1;
						deltaV = deltaV + greyscalePic[px][py] * filterV[i][j];
						deltaH = deltaH + greyscalePic[px][py] * filterH[i][j];//
					}
				}
				// hack hack for not having to check case 1^^: (xxx-000001)
				bin = (int) ((NDIR - 0.000001)
						* (Math.PI / 2 + Math.atan(deltaV / deltaH)) / (Math.PI));

				int tmp = NCOL * bin;
				int mask = 0xC0000000;
				for (int i = 0; i < NCOL; i++) {
					if ((pixel & mask) != 0) {
						features[tmp + i]++;
					}
					/* so it wont be generic, but hey... who cares^^ */
					mask = mask >>> 4;
				}
			}
		}

		// normalize features
		double max = 0;

		for (double d : features) {
			max = max < d ? d : max;
		}
		int MAX = 100; /* maximum value for the descriptor after scaling */
		for (int i = 0; i < features.length; i++) {
			features[i] = features[i] / (max / MAX);

		}

		addTo.put(bimg, features);

	}

	private static final double[][] filterH = { { -1, 0, 1 }, { -1, 0, 1 },
			{ -1, 0, 1 } };

	private static final double[][] filterV = { { -1, -1, -1 }, { 0, 0, 0 },
			{ 1, 1, 1 } };

}
