package com.example.ethandixius.taxi;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.location.LocationManager;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.apache.http.entity.SerializableEntity;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;


public class MainActivity extends ActionBarActivity {

    public static final int PORT = 2000;
    public static Double lat = 0.0;
    public static Double lon = 0.0;
    public static Double distance = 0.0;


    Button button1 = null;
    Button button2 = null;
    EditText startLat = null;
    EditText startLon = null;
    EditText endLat = null;
    EditText endLon = null;

    BufferedReader in = null;
    PrintWriter out = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//        try {
        network();

            LocationManager lm = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

            LocationListener ll = new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {
                    newLocation(location);
                }

                @Override
                public void onStatusChanged(String provider, int status, Bundle extras) {}

                @Override
                public void onProviderEnabled(String provider) {}

                @Override
                public void onProviderDisabled(String provider) {}
            };

            lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, ll);

            button1 = (Button) this.findViewById(R.id.button1);
            button2 = (Button) this.findViewById(R.id.button2);

            startLat = (EditText) this.findViewById(R.id.editText);
            startLon = (EditText) this.findViewById(R.id.editText2);
            endLat = (EditText) this.findViewById(R.id.editText3);
            endLon = (EditText) this.findViewById(R.id.editText4);


            button1.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    startLat.setText(String.valueOf(lat));
                    startLon.setText(String.valueOf(lon));
                }
            });

            startLat.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startLat.setText("");
                }
            });

            startLon.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startLon.setText("");
                }
            });

            endLat.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    endLat.setText("");
                }
            });

            endLon.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    endLon.setText("");
                }
            });

            button2.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    out.println(startLat.getText() + "," + startLon.getText() + "," + endLat.getText() + "," + endLon.getText());
                }
            });

//
//        } catch (UnknownHostException e) {
//        } catch (IOException e) {}



    }

    void network () {
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... args) {
                try {
                    Socket s = new Socket(InetAddress.getByName("129.59.122.21"), PORT);//"54.149.199.106"), PORT);

                    in = new BufferedReader(new InputStreamReader(s.getInputStream()));
                    out = new PrintWriter(s.getOutputStream());
                } catch (UnknownHostException e) {
                } catch (IOException e) {
                }
                return null;
            }

            @Override
            protected void onPostExecute(String errorMsg) {
                incoming();
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    void incoming() {
        new AsyncTask<Void, String, Void>() {
            @Override
            protected Void doInBackground(Void... args) {
                try {
                    String next;
                    while ((next = in.readLine()) != null) {

                        publishProgress(next);
                    }
                } catch (IOException e) {
                }
                return null;
            }

            @Override
            protected void onProgressUpdate(String... lines) {
                String m = lines[0];
                distance = Double.parseDouble(m);
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }


    public void newLocation(Location l) {
        lat = l.getLatitude();
        lon = l.getLongitude();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
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

        return super.onOptionsItemSelected(item);
    }
}
