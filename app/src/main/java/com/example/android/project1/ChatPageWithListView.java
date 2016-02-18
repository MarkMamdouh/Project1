package com.example.android.project1;

import android.app.ListActivity;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;


public class ChatPageWithListView extends ListActivity {

    //String SERVER_IP = "197.45.183.87";
    String SERVER_IP = "192.168.1.44";
    EditText enteredRecepient;

    MessageAdapter adapter;
    List msgs;

    @Override
     public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_page_with_listview);
        msgs =  new ArrayList();
        adapter = new MessageAdapter(this, msgs);
        setListAdapter(adapter);

        SharedPreferences tempPrefs = getSharedPreferences("com.example.android.project1.NetworkPreferences",0);
        SERVER_IP = tempPrefs.getString("SERVER_IP","192.168.1.44");

        enteredRecepient = (EditText) findViewById(R.id.entered_recepient);
    }

    public void sendMessage(View view) {
        EditText message = (EditText) findViewById(R.id.textInput);
        String mText = message.getText().toString();

        msgs.add(new MessageData(mText));
        adapter.notifyDataSetChanged();
        message.setText("");

        //Get the unique device ID that will be stored in the database to uniquely identify this device
        SharedPreferences prefs = getSharedPreferences("com.example.android.project1.RegistrationPreferences", 0);
        String deviceID = prefs.getString("deviceUUID", "0");

        //Check if there's an internet connection
        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = connMgr.getActiveNetworkInfo();

        //If there's an internet connection
        if (netInfo != null && netInfo.isConnected()) {
            //Get the value of the textfields from the UI
            String recepientUserName = enteredRecepient.getText().toString();

            //Send the message info to the server in a background thread
            downloadThread download = new downloadThread();
            //download.execute("http://192.168.1.44:8080/MyFirstServlet/AddNewMessage?senderDeviceID=" + URLEncoder.encode(deviceID) + "&recepientUserName=" + URLEncoder.encode(recepientUserName) + "&message=" + URLEncoder.encode(mText));
            download.execute("http://"+SERVER_IP+":8080/MyFirstServlet/AddNewMessage?senderDeviceID=" + URLEncoder.encode(deviceID) + "&recepientUserName=" + URLEncoder.encode(recepientUserName) + "&message=" + URLEncoder.encode(mText));
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_chat_page, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        if(id == R.id.schedule_message_button)
        {
            DialogFragment newFragment = new PopupMessageDialog();
            //newFragment.show(getSupportFragmentManager(), "timePicker");
        }
        return super.onOptionsItemSelected(item);
    }

    //AsyncTask that will handle the HTTP connections in a background thread
    public class downloadThread extends AsyncTask<String, Void, String> {

        protected String doInBackground(String... urls) {
            String result = null;
            try {
                result = downloadUrl(urls[0]);
            } catch (IOException e) {

            }
            return result;
        }

        protected void onPreExecute() {
        }

        protected void onPostExecute(String result) {
        }

        protected void onProgressUpdate(Void... value) {
        }


        private String downloadUrl(String myurl) throws IOException {
            InputStream is = null;
            try {
                URL url = new URL(myurl);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(10000);
                conn.setConnectTimeout(15000);
                conn.setRequestMethod("GET");
                conn.setDoInput(true);

                conn.connect();
                int response = conn.getResponseCode();
                Log.d("MainActivity", "The response is: " + response);
                publishProgress();

                is = conn.getInputStream();

                int len = 500;
                String result = null;

                Reader reader = new InputStreamReader(is, "UTF-8");
                char[] buffer = new char[len];
                reader.read(buffer);
                result = new String(buffer);

                return result;

            } finally {
                if (is != null) {
                    is.close();
                }
            }
        }//End of downloadUrl method
    }
}
