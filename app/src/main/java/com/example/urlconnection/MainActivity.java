package com.example.urlconnection;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends AppCompatActivity {
    private EditText urlText;
    private TextView textView;
    private Button downloadButton;

    private static final String DEBUG_TAG = "HttpExample";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        urlText = (EditText) findViewById(R.id.linkET);
        textView = (TextView) findViewById(R.id.textTV);
        downloadButton = (Button)findViewById(R.id.downloadB);

        downloadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isConnected()) {
                    DownloadWebpageText dwt = new DownloadWebpageText();
                    dwt.execute(urlText.getText().toString());
                } else {
                    Toast.makeText(getApplicationContext(), "No Internet Connection", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    public boolean isConnected() {
        boolean connected = false;
        try{

            ConnectivityManager connMgr = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
            if (networkInfo != null && networkInfo.isConnected()) {
                connected=true;
            } else {
                connected=false;
            }
        }
        catch (Exception e){
            Log.e("Connectivity Exception", e.getMessage());
        }
        return connected;
    }
    private class DownloadWebpageText extends AsyncTask<String,String,String> {

        @Override
        protected String doInBackground(String... urls) {

            try {
                return downloadUrl(urls[0]);
            } catch (IOException e) {
                return "Unable to retrieve web page. URL may be invalid.";
            }
        }

        @Override
        protected void onPostExecute(String result) {
            String text ="";
            boolean finish = false;
            boolean found = false;
            int i=0;
            while (!found) {
                if (result.charAt(i)=='<' && result.charAt(i+1)=='p' && result.charAt(i+2)=='>'){
                    found = true;
                }else {
                    i++;
                }
            }
            i+=3;
            while (!finish) {
                if (result.charAt(i)=='<' && result.charAt(i+1)=='/' && result.charAt(i+2)=='p' && result.charAt(i+3)=='>'){
                    finish=true;
                }else {
                    text += result.charAt(i);
                    i++;
                }
            }

            textView.setText(text);
        }


        private String downloadUrl(String myurl) throws IOException {
            InputStream is = null;
            int len = 500000;

            try {
                URL url = new URL(myurl);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(10000);
                conn.setConnectTimeout(15000);
                conn.setRequestMethod("GET");
                conn.setDoInput(true);


                conn.connect();
                int response = conn.getResponseCode();
                is = conn.getInputStream();

                String contentAsString = readIt(is, len);
                return contentAsString;

            } finally {
                if (is != null) {
                    is.close();
                }
            }
        }
        public String readIt(InputStream stream, int len) throws IOException, UnsupportedEncodingException {
            Reader reader = null;
            reader = new InputStreamReader(stream, "UTF-8");
            char[] buffer = new char[len];
            reader.read(buffer);
            return new String(buffer);
        }
    }
}