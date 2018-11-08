package unipd.se18.ocrcamera;


import android.graphics.Bitmap;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;


/**
 * Class built to test the application's OCR
 * @author Luca Moroldo (g3)
 */
public class PhotoTester {

    public static final String[] IMAGE_EXTENSIONTS = {"jpeg", "jpg"};
    private ArrayList<TestInstance> testInstances;

    /**
     * Load test instances (images + correct ingredients)
     * @param dirPath stores the path to the directory containing photos and description
     */
    public PhotoTester(String dirPath) {
        File directory = new File(dirPath);


        for (File file : directory.listFiles()) {

            Bitmap photoBitmap = null;
            String photoDesc = null;

            String filePath = file.getPath();
            String fileExtension = Utils.getFileExtension(filePath);

            if(Arrays.asList(IMAGE_EXTENSIONTS).contains(fileExtension)) {
                photoBitmap = Utils.loadBitmap(filePath);
            } else if(fileExtension.equals("txt")) {
                photoDesc = Utils.getIngredientsFromFile(filePath);
            }

            testInstances.add(new TestInstance(photoBitmap, photoDesc));
        }
    }

    /**
     * @return String with the test's report, each line contains: image name, extracted text,
     *  real text, photo tags, notes if present, confidence
     */
    public String testAndReport() {
        //For each test instance apply ocr, compare texts, build report

        return null;
    }

    /**
     * Compare the list of ingredients extracted by OCR and the correct list of ingredients
     * @param correct correct list of ingredients loaded from file
     * @param extracted list of ingredients extracted by the OCR
     * @return percentage of matched words.
     */
    private int ingredientsStringComparison(String correct, String extracted){
        return 0;
    }


    /**
     *
     * @param bitmap from which the text is extracted
     * @return String - the text extracted
     */
    private String executeOcr(Bitmap bitmap) {
        return null;
    }

}
