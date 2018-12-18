package unipd.se18.ocrcamera;

import android.content.Context;
import java.io.InputStream;
import java.util.List;

/**
 * Using singleton design pattern for single time inci db loading and text extractor initialization.
 * @author Francesco Pham
 */
class IngredExtractorSingleton {
    private static volatile IngredExtractorSingleton ourInstance = new IngredExtractorSingleton();

    IngredientsExtractor ingredientsExtractor;

    static IngredExtractorSingleton getInstance() {
        if (ourInstance == null) {
            synchronized (IngredExtractorSingleton.class) {
                if (ourInstance == null) ourInstance = new IngredExtractorSingleton();
            }
        }
        return ourInstance;
    }

    private IngredExtractorSingleton() {
        if (ourInstance != null){
            throw new RuntimeException("Use getInstance() method to get the single instance of this class.");
        }
    }

    /**
     * Load list of ingredients from INCI DB and initialize ingredients extractor.
     * @param context
     */
    void load(Context context){
        //load inci db and initialize ingredient extractor
        InputStream inciDbStream = context.getResources().openRawResource(R.raw.incidb);
        List<Ingredient> listInciIngredients = Inci.getListIngredients(inciDbStream);

        InputStream wordListStream = context.getResources().openRawResource(R.raw.inciwordlist);
        TextAutoCorrection textCorrector = new TextAutoCorrection(wordListStream);

        ingredientsExtractor = new PrecorrectionIngredientsExtractor(listInciIngredients, textCorrector);
    }
}
