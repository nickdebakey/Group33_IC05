package com.example.group33_ic05;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.commons.io.IOUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;

public class MainActivity extends AppCompatActivity {

    Button button_go;
    TextView tv_keyword;
    ImageView iv_prev;
    ImageView iv_next;
    ImageView iv_mainImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setTitle("");

        button_go = findViewById(R.id.button_go);
        tv_keyword = findViewById(R.id.tv_keyword);
        iv_prev = findViewById(R.id.iv_prev);
        iv_next = findViewById(R.id.iv_next);
        iv_mainImage = findViewById(R.id.iv_mainImage);

        button_go.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // get keywords here
                if(isConnected() == true){
                    Toast.makeText(MainActivity.this, "Internet Connected!", Toast.LENGTH_SHORT).show();
                    new GetKeywords().execute("http://dev.theappsdr.com/apis/photos/keywords.php");
                } else {
                    Toast.makeText(MainActivity.this, "Internet Disconnected", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    private class GetKeywords extends AsyncTask<String, Void, String>{
        String result;
        String[] resultArray;

        @Override
        protected String doInBackground(String... strings) {
            StringBuilder stringBuilder = new StringBuilder();
            HttpURLConnection connection = null;
            InputStream inputStream = null;
            try {
                URL keywordURL = new URL(strings[0]);
                connection = (HttpURLConnection) keywordURL.openConnection();
                inputStream = connection.getInputStream();
                connection.connect();
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                String line = "";

                // while loop for splitting line
                while((line = reader.readLine()) != null){
                    stringBuilder.append(line);
                    Log.d("demo", "keywords: " + line.toString());
                }
                result = stringBuilder.toString().trim();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if(connection != null) {
                    connection.disconnect();
                }
                if(inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            return result;
        }

        @Override
        protected void onPostExecute(String s) {
            resultArray = s.split(";");
            AlertDialog.Builder keywordDialog = new AlertDialog.Builder(MainActivity.this);
            keywordDialog.setTitle("Choose a Keyword");
            keywordDialog.setItems(resultArray, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    tv_keyword.setText(resultArray[i]);
                    // GetImageURL(resultArray[i]).execute("http://dev.theappsdr.com/apis/photos/index.php");
                }
            });
            AlertDialog dialog = keywordDialog.create();
            dialog.show();
        }
    }

    private class GetImageURL extends AsyncTask<String, Void, String> {


        @Override
        protected String doInBackground(String... strings) {
            // get the image URL here
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            // return the image URL ?
        }
    }
    

    private boolean isConnected() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();

        if (networkInfo == null || !networkInfo.isConnected() ||
                (networkInfo.getType() != ConnectivityManager.TYPE_WIFI
                        && networkInfo.getType() != ConnectivityManager.TYPE_MOBILE)) {
            return false;
        }
        return true;
    }

}
