import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

import javax.imageio.ImageIO;

public class Main {

	/*
	 * TODO: very much :D
	 */

	public static void main(String[] args) {

		sc = (new SeamCarving());

		System.out.println("Loading dataset...");

		System.out.println("dataset folder: " + datasetPath);
		System.out.println("query: " + queryPath);

		long l = System.currentTimeMillis();

		File dir = new File(datasetPath);

		List<File> datasetFiles = new ArrayList<File>();

		List<BufferedImage> datasetOrig = new ArrayList<BufferedImage>();

		Utils.scanDir(dir, datasetFiles, "jpg");

		for (File imageFile : datasetFiles) {

			ThreadPool.executeandWait(new Runnable() {
				@Override
				public void run() {
					try {
						datasetOrig.add(ImageLoader.load(imageFile));
						// datasetOrig.add(ImageIO.read(imageFile));
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			});
		}

		while (ThreadPool.busy()) {
			try {
				System.out.println("Thread pool is busy (" + ThreadPool.cnt
						+ " thread(s) running)");
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		System.out.println("Dataset loaded in "
				+ (System.currentTimeMillis() - l) + "ms");

		/*
		 * PREPROCESSING: SEAM CARVING
		 */

		l = System.currentTimeMillis();
		System.out.println("Starting preprocessing");
		List<BufferedImage> dataset = new ArrayList<BufferedImage>();
		for (BufferedImage img : datasetOrig) {
			ThreadPool.executeandWait(new Runnable() {

				@Override
				public void run() {
					dataset.add(sc.seamCarve(img, seamsRemoved));
				}
			});
		}
		while (ThreadPool.busy()) {
			try {
				System.out.println("Thread pool is busy... wait: ("

				+ ThreadPool.cnt + "threads running)");
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		System.out.println("Preprocessing finished in "
				+ (System.currentTimeMillis() - l) + "ms");

		/*
		 * START FEATURE EXTRACTION
		 */

		l = System.currentTimeMillis();
		System.out.println("Starting feature extraction ");

		Map<BufferedImage, double[]> datasetFeatures = new HashMap<BufferedImage, double[]>();
		for (BufferedImage img : dataset) {
			ThreadPool.executeandWait(new Runnable() {

				@Override
				public void run() {
					FeatureExtractor.extractFeature(img, datasetFeatures);
				}
			});
		}
		while (ThreadPool.busy()) {
			try {
				System.out.println("Thread pool is busy... wait: ("
						+ ThreadPool.cnt + " threads running)");
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		System.out.println("Feature extraction finished in "
				+ (System.currentTimeMillis() - l) + "ms");

		/*
		 * linear search
		 */

		Map<BufferedImage, double[]> qFeatures = new HashMap<BufferedImage, double[]>();
		BufferedImage queryImg = null;
		try {
			queryImg = sc.seamCarve(ImageIO.read(new File(queryPath)),
					seamsRemoved);
		} catch (IOException e) { // TODO Auto-generated catch block
			e.printStackTrace();
		}
		FeatureExtractor.extractFeature(queryImg, qFeatures);
		PriorityQueue<ImageFile> pq = new PriorityQueue<>();

		for (BufferedImage f : datasetFeatures.keySet()) {
			pq.add(new ImageFile(f, Utils.tanimotoClassifier(
					qFeatures.get(queryImg), datasetFeatures.get(f))));
		}

		Utils.showImage("your query ", queryImg);

		int maxRes = 10;
		for (int i = 0; i < maxRes; i++) {
			System.out.println("Result " + i + " : " + pq.poll());
			Utils.showImage("Result " + i + " with sim: " + pq.peek().sim,
					pq.poll().f);
		}

	}

	static int seamsRemoved = 50;

	static SeamCarving sc;

	static String queryPath = "C:\\Users\\stefan\\workspace\\Ex04\\wang\\172.jpg";
	// static String queryPath =
	// "C:\\Users\\stefan\\workspace\\Ex04\\dataset\\red\\44070187_5e5a50b675_b.jpg";
	static String datasetPath = "C:\\Users\\stefan\\workspace\\Ex04\\wang";

	// static String datasetPath =
	// "C:\\Users\\stefan\\workspace\\Ex04\\dataset";

}
