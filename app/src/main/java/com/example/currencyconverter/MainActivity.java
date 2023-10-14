package com.example.currencyconverter;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.okhttp.Callback;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    List<String> keysList;
    Spinner toCurrency;
    TextView textView;
    OkHttpClient client; // Create OkHttpClient instance
    String YOUR_API = "YOUR_API_KEY";
    String BASE_URL = "http://api.exchangeratesapi.io/v1/latest";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        toCurrency = findViewById(R.id.planets_spinner);
        EditText edtEuroValue = findViewById(R.id.editText4);
        Button btnConvert = findViewById(R.id.button);
        textView = findViewById(R.id.textView7);

        client = new OkHttpClient(); // Initialize OkHttpClient

        btnConvert.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!edtEuroValue.getText().toString().isEmpty()) {
                    String toCurr = toCurrency.getSelectedItem().toString();
                    double euroValue = Double.parseDouble(edtEuroValue.getText().toString()); // Use Double.parseDouble
                    Toast.makeText(MainActivity.this, "Please Wait..", Toast.LENGTH_SHORT).show();

                    // Call the method to convert currency
                    try {
                        convertCurrency(toCurr, euroValue);
                    } catch (IOException e) {
                        e.printStackTrace();
                        Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(MainActivity.this, "Please Enter a Value to Convert..", Toast.LENGTH_SHORT).show();
                }
            }
        });

        try {
            loadConvTypes();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void loadConvTypes() throws IOException {
        // Construct the API URL with your API key
        String apiKey = YOUR_API;  // Replace with your actual API key
        String baseUrl = BASE_URL; // Corrected the API URL
        String apiUrl = baseUrl + "?access_key=" + apiKey;

        Request request = new Request.Builder()
                .url(apiUrl)
                .build();

        // Asynchronously fetch the exchange rate data
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
                e.printStackTrace();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MainActivity.this, "Failed to load conversion types", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(Response response) throws IOException {
                if (response.isSuccessful()) {
                    final String responseBody = response.body().string();
                    try {
                        JSONObject obj = new JSONObject(responseBody);
                        JSONObject rates = obj.getJSONObject("rates");
                        Iterator<String> keys = rates.keys();
                        keysList = new ArrayList<>();

                        while (keys.hasNext()) {
                            String key = keys.next();
                            keysList.add(key);
                        }

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<>(MainActivity.this, android.R.layout.simple_spinner_item, keysList);
                                toCurrency.setAdapter(spinnerArrayAdapter);
                            }
                        });
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    Toast.makeText(MainActivity.this, "Failed to load conversion types", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public void convertCurrency(final String toCurr, final double euroValue) throws IOException {
        // Construct the API URL with your API key
        String apiKey = YOUR_API;  // Replace with your actual API key
        String baseUrl = BASE_URL; // Corrected the API URL
        String apiUrl = baseUrl + "?access_key=" + apiKey;

        Request request = new Request.Builder()
                .url(apiUrl)
                .build();

        // Asynchronously fetch the exchange rate data
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
                e.printStackTrace();
                Toast.makeText(MainActivity.this, "Failed to convert currency", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onResponse(Response response) throws IOException {
                if (response.isSuccessful()) {
                    final String responseBody = response.body().string();
                    try {
                        JSONObject obj = new JSONObject(responseBody);
                        JSONObject rates = obj.getJSONObject("rates");
                        if (rates.has(toCurr)) {
                            double rate = rates.getDouble(toCurr);
                            final double output = euroValue * rate;
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    textView.setText(String.valueOf(output));
                                }
                            });
                        } else {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    textView.setText("Conversion not available for the selected currency");
                                }
                            });
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    Toast.makeText(MainActivity.this, "Failed to convert currency", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}