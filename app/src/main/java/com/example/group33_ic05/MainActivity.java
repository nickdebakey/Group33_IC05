package com.example.group33_ic05;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
    String keywordResult;
    String[] keywordResultArray;
    String imageURLResult;
    String[] imageURLResultArray;

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
                keywordResult = stringBuilder.toString().trim();
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
            return keywordResult;
        }

        @Override
        protected void onPostExecute(String s) {
            keywordResultArray = s.split(";");
            AlertDialog.Builder keywordDialog = new AlertDialog.Builder(MainActivity.this);
            keywordDialog.setTitle("Choose a Keyword");
            keywordDialog.setItems(keywordResultArray, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    tv_keyword.setText(keywordResultArray[i]);
                    new GetImageURL().execute("http://dev.theappsdr.com/apis/photos/index.php" + "?keyword=" + keywordResultArray[i]);
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
                    Log.d("demo", "urls for images: " + line.toString());
                }
                imageURLResult = stringBuilder.toString().trim();
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
            return imageURLResult;
        }

        @Override
        protected void onPostExecute(String s) {
            // rework so that the image urls on the webpage get stored in an array, current code not working
            imageURLResultArray = s.split("\\s+");
        }
    }

    private class DisplayImages extends AsyncTask<String, Void, Void> {
        Bitmap bitmap = null;

        @Override
        protected Void doInBackground(String... strings) {
            HttpURLConnection connection = null;
            try {
                URL url = new URL(strings[0]);
                connection = (HttpURLConnection) url.openConnection();
                connection.connect();
                if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    bitmap = BitmapFactory.decodeStream(connection.getInputStream());
                }
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }finally {
                if(connection != null) {
                    connection.disconnect();
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            // just testing the first image
            iv_mainImage.setImageBitmap(bitmap);

            // refractor for the prev + next buttons on click to display circular queue of images in array
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
