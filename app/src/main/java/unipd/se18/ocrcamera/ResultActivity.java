package unipd.se18.ocrcamera;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import unipd.se18.ocrcamera.inci.Ingredient;
import unipd.se18.ocrcamera.inci.IngredientsExtractor;
import unipd.se18.textrecognizer.OCR;
import unipd.se18.textrecognizer.OCRListener;
import unipd.se18.textrecognizer.TextRecognizer;

import static unipd.se18.textrecognizer.TextRecognizer.getTextRecognizer;

// OCR module

/**
 * Class used for showing the result of the OCR processing
 * @author Pietro Prandini (g2) - Francesco Pham (g3)
 */
public class ResultActivity extends AppCompatActivity {

    /**
     * listview used to show the ingredients extracted according with the INCI database
     */
    private ListView ingredientsListView;

    /**
     * progress bar used to show the progress on the ingredients extraction from the photo taken
     */
    private ProgressBar progressBar;
    /**
     * view used to show progress messages
     */
    private TextView emptyTextView;

    private final String TAG = "ResultActivity";

    /**
     * Contains the last photo taken by the user
     */
    Bitmap lastPhoto;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        // UI components
        ImageView mImageView = findViewById(R.id.img_captured_view);
        ingredientsListView = findViewById(R.id.ingredients_list);
        progressBar = findViewById(R.id.progress_bar);

        //set on empty list view
        emptyTextView= findViewById(R.id.empty_list);
        emptyTextView.setText(R.string.finding_text);
        ingredientsListView.setEmptyView(emptyTextView);

        // Floating action buttons listeners (Francesco Pham)
        FloatingActionButton fabNewPic = findViewById(R.id.newPictureFab);
        fabNewPic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(ResultActivity.this, CameraActivity.class));
            }
        });

        SharedPreferences prefs = getSharedPreferences("prefs", MODE_PRIVATE);
        //load the path to the last taken picture, can be null if the user didn't take any picture
        String lastImagePath = prefs.getString("imagePath", null);

        //only if lastImagePath is not null we set our view
        if(lastImagePath != null) {

            // Bitmap of the lastPhoto saved
            lastPhoto = BitmapFactory.decodeFile(lastImagePath);

            // Sets the image to the view
            mImageView.setImageBitmap(
                    // Scales the image firstly
                    Bitmap.createScaledBitmap(
                            lastPhoto,
                            lastPhoto.getWidth(),
                            lastPhoto.getHeight(),
                            false
                    )
            );
            //create a listener for the end of the text extraction by the OCR
            OCRListener textExtractionListener = new OCRListener() {
                //function called when the OCR extraction is finished
                @Override
                public void onTextRecognized(String text) {
                    emptyTextView.setText(R.string.searching_ingredients);
                    progressBar.setProgress(33);

                    //save photo in the gallery and the last recognized text
                    saveTheResult(text);

                    //search for ingredients in the INCI db and update the UI
                    new AsyncUIUpdate().execute(text);
                }

                @Override
                public void onTextRecognizedError(int code) {
                    /*
                     Text not correctly recognized
                     -> prints the error on the screen and doesn't save it in the preferences
                     */
                    String errorText = R.string.extraction_error
                            + " (" + R.string.error_code + code + ")";
                    Log.e(TAG, errorText);
                }
            };

            //get an OCR instance
            OCR textRecognizer = getTextRecognizer(TextRecognizer.Recognizer.mlKit,
                    textExtractionListener);
            //extract text
            textRecognizer.getTextFromImg(lastPhoto);
            progressBar.setVisibility(ProgressBar.VISIBLE);
        }
    }


    /**
     * Saves the result obtained in the "prefs" preferences (Context.MODE_PRIVATE)
     * - the name of the String is "text"
     * @param text The text extracted by the process
     * @author Pietro Prandini (g2)
     */
    private void saveTheResult(String text) {
        // Saving in the preferences
        SharedPreferences sharedPref = getApplicationContext().getSharedPreferences("prefs",
                Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("text", text);
        editor.apply();

        //I cant understand the ingredients yet, for now I put everything as one ingredient
        ArrayList<String> txt = new ArrayList<>();
        String formattedText=String.valueOf(Html.fromHtml(text));
        txt.add(formattedText);
        try {
            GalleryManager.storeImage(getBaseContext(),lastPhoto,txt,"0%");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * Class used to run extract ingredients from INCI db and update the UI setting a list view with
     * the recognized ingredients list
     * @author Francesco Pham - refactored by Luca Moroldo
     */
    @SuppressLint("StaticFieldLeak")
    private class AsyncUIUpdate extends AsyncTask<String, Void, List<Ingredient>> {

        @Override
        protected void onPreExecute() {
            //be sure the progress bar is visible
            progressBar.setVisibility(ProgressBar.VISIBLE);
        }

        /**
         *
         * @param strings text scanned for ingredients
         * @return a list of ingredients, null if the list is empty or the param is null or empty
         */
        @Override
        protected List<Ingredient> doInBackground(String... strings) {

            //check if text is empty or null
            if(strings[0] == null || strings[0].equals(""))
                return null;

            //load inci db and initialize extractor if not already loaded
            if (IngredExtractorSingleton.getInstance().ingredientsExtractor == null)
                IngredExtractorSingleton.getInstance().load(getApplicationContext());

            IngredientsExtractor extractor = IngredExtractorSingleton.getInstance().ingredientsExtractor;

            progressBar.incrementProgressBy(33);


            List<Ingredient> ingredientList =extractor.findListIngredients(strings[0]);

            //if the list is empty then return null
            if(ingredientList.size() == 0)
                return null;

            return ingredientList;

        }

        @Override
        protected void onPostExecute(List<Ingredient> ingredients) {

            progressBar.setVisibility(ProgressBar.INVISIBLE);
            //if something has been found then set the list of ingredients recognized inside INCI db
            if(ingredients != null) {
                emptyTextView.setVisibility(TextView.INVISIBLE);

                AdapterIngredient adapter =
                        new AdapterIngredient(
                                ResultActivity.this,
                                ingredients
                        );
                ingredientsListView.setAdapter(adapter);
            } else
                emptyTextView.setText(R.string.no_ingredient_found);
        }
    }


    /**
     * Menu inflater
     * @author Francesco Pham
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.result_menu, menu);
        return true;
    }

    /**
     * Handling click events on the menu
     * @author Francesco Pham - modified by Stefano Romanello
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.test:
                Intent i = new Intent(ResultActivity.this, TestsListActivity.class);
                startActivity(i);
                return true;
            case R.id.download_photos:
                Intent download_intent = new Intent(ResultActivity.this,
                        DownloadDbActivity.class);
                startActivity(download_intent);
                return true;
            case R.id.gallery:
                Intent gallery_intent = new Intent(ResultActivity.this, GalleryActivity.class);
                startActivity(gallery_intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}

