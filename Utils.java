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

import java.awt.EventQueue;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JOptionPane;

public class Utils {
	/* returns greyscale representation of a specific pixel */

	public static void scanDir(File baseDir, List<File> allFiles, String filter) {
		File[] files = baseDir.listFiles();
		if (files != null) {
			for (int i = 0; i < files.length; i++) {

				if (files[i].isDirectory()) {
					scanDir(files[i], allFiles, filter);
				} else {
					if (files[i].getPath().endsWith(filter)) {
						allFiles.add(files[i]);
					}
				}
			}
		}
	}

	public static void showImage(String title, BufferedImage f) {
		EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {

				ImageIcon icon = new ImageIcon(f);
				JOptionPane.showMessageDialog(null, title, title,
						JOptionPane.INFORMATION_MESSAGE, icon);

			}
		});
	}/*
	 * returns between 0 and 100
	 */

	public static double tanimotoClassifier(double[] t1, double[] t2) {
		double tmp1 = 0, tmp2 = 0;

		for (int i = 0; i < t1.length; i++) {
			tmp1 += t1[i];
			tmp2 += t2[i];
		}
		if (tmp1 == 0 || tmp2 == 0) {
			double result = 100;
			if (tmp1 == 0 && tmp2 == 0) {
				result = 0;
			}
			return result;
		}
		double tmpcnt1 = 0, tmpcnt2 = 0, tmpcnt3 = 0;
		double tr1 = 0, tr2 = 0;
		for (int i = 0; i < t1.length; i++) {
			tr1 = t1[i] / tmp1;
			tr2 = t2[i] / tmp2;
			tmpcnt1 += tr1 * tr2;
			tmpcnt2 += tr2 * tr2;
			tmpcnt3 += tr1 * tr1;
		}

		return 100 - 100 * (tmpcnt1 / (tmpcnt3 + tmpcnt2 - tmpcnt1));

	}

}
