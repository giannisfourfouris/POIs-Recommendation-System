package kat.recsystem;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import static kat.recsystem.AndroidClient.pois;
import java.io.Serializable;
import java.util.Arrays;

import kat.recsystem.R;

public class HomeScreen extends AppCompatActivity implements Serializable {

    private static final String[] Categories = new String[]{"Arts & Entertainment", "Food", "Bars", "Everything"};
    public static final String LATITUDE = "Latitude";
    public static final String LONGITUDE = "Longitude";


    private EditText numOfPoisText, userLongitudeText, userLatitudeText, userIDText;
    private AutoCompleteTextView category;
    private Button searchButton;
    public AndroidClient client;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_screen);


        final AsyncTaskRunner runner = new AsyncTaskRunner();

        numOfPoisText = (EditText) findViewById(R.id.edtTxtNumOfPois);
        userLatitudeText = (EditText) findViewById(R.id.edtTxtUserLatitude);
        userLongitudeText = (EditText) findViewById(R.id.edtTxtUserLongitude);
        userIDText = (EditText) findViewById(R.id.edtTxtUserID);

        ArrayAdapter<String> DayAdapter = new ArrayAdapter<String>(this,
                android.R.layout.select_dialog_item, Categories);
        category = (AutoCompleteTextView) findViewById(R.id.autoCTxtViewPoiCategory);
        category.setAdapter(DayAdapter);
        category.setThreshold(0);


        searchButton = (Button) findViewById(R.id.btnSearch);

        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isInteger(userIDText.getText().toString()) || !isInteger(numOfPoisText.getText().toString()) || !isDouble(userLongitudeText.getText().toString())
                        || !isDouble(userLatitudeText.getText().toString()) || !Arrays.asList(Categories).contains(category.getText().toString())) {
                    Toast.makeText(v.getContext(), "Please enter all fields with correct values", Toast.LENGTH_SHORT).show();
                }else{
                    runner.execute();
                        Intent myIntent = new Intent(v.getContext(), MapsActivity.class);
                        myIntent.putExtra(LATITUDE, Double.parseDouble(userLatitudeText.getText().toString()));
                        myIntent.putExtra(LONGITUDE, Double.parseDouble(userLongitudeText.getText().toString()));
                        startActivityForResult(myIntent, 0);
                }

            }
        });
    }

    private class AsyncTaskRunner extends AsyncTask<String, String, String> {

        private String resp;
        ProgressDialog progressDialog;

        @Override
        protected String doInBackground(String... params) {
            publishProgress("Establishing connection"); // Calls onProgressUpdate()
                client = new AndroidClient(Integer.parseInt(userIDText.getText().toString().trim()),
                        Double.parseDouble(userLatitudeText.getText().toString().trim()), Double.parseDouble(userLongitudeText.getText().toString().trim()),
                        Integer.parseInt(numOfPoisText.getText().toString().trim()), category.getText().toString());
                publishProgress("Processing your request");
                client.initializeAndroidClient();
                resp = "Recommendations received";
                client = null;
                return resp;
        }


        @Override
        protected void onPostExecute(String result) {
            // execution of result of Long time consuming operation
            progressDialog.setMessage(result);
            progressDialog.dismiss();
            recreate();
            finish();
        }


        @Override
        protected void onPreExecute() {
            progressDialog = ProgressDialog.show(HomeScreen.this,
                    "ProgressDialog",
                    "Wait to receive recommendations");
        }


        @Override
        protected void onProgressUpdate(String... text) {
            progressDialog.setMessage(text[0]);
        }
    }


    public boolean isInteger(String input) {
        try {
            Integer.parseInt(input.trim());
            return true;
        } catch (Exception e) {
            return false;
        }

    }

    public boolean isDouble(String input) {
        try {
            Double.parseDouble(input.trim());
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}

