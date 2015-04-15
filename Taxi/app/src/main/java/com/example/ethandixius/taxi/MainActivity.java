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
import android.widget.TextView;
import android.widget.Toast;
import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;


public class MainActivity extends ActionBarActivity {

    public static final String TAG = "MainActivity.java";

    public static final int PORT = 2000;
    public static Double lat = 0.0;
    public static Double lon = 0.0;
    public static Double distance = 0.0;
    public static Double time = 0.0;

    Button curLoc = null;
    Button calculate = null;
    EditText startLat = null;
    EditText startLon = null;
    EditText endLat = null;
    EditText endLon = null;

    TextView output = null;

    Socket s = null;
    BufferedReader in = null;
    PrintWriter out = null;
    boolean connection = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//        try {

//        try {
//            s = new Socket(InetAddress.getByName("54.149.199.106"), PORT);
//
//            in = new BufferedReader(new InputStreamReader(s.getInputStream()));
//            out = new PrintWriter(s.getOutputStream());
//
//            Toast.makeText(getApplicationContext(), "Connected to server", Toast.LENGTH_LONG).show();
//        } catch (UnknownHostException e) {
//            Toast.makeText(getApplicationContext(), "Problem connecting to server...", Toast.LENGTH_LONG).show();
//        } catch (IOException e) {
//            Toast.makeText(getApplicationContext(), "Problem setting up I/O", Toast.LENGTH_LONG).show();
//
//        }

//        incoming();

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

            curLoc = (Button) this.findViewById(R.id.button1);
            calculate = (Button) this.findViewById(R.id.calculate);

            startLat = (EditText) this.findViewById(R.id.editText);
            startLon = (EditText) this.findViewById(R.id.editText2);
            endLat = (EditText) this.findViewById(R.id.editText3);
            endLon = (EditText) this.findViewById(R.id.editText4);

            output = (TextView) this.findViewById(R.id.output);

            curLoc.setOnClickListener(new View.OnClickListener() {

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

            calculate.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    send(startLat.getText() + "," + startLon.getText() + "," + endLat.getText() + "," + endLon.getText());
                }
            });

//
//        } catch (UnknownHostException e) {
//        } catch (IOException e) {}

        network();


    }

    void network () {
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... args) {
                try {

                    Log.i(TAG, "Trying to connect to server...");
//                    Toast.makeText(getApplicationContext(), "Trying to connect to server...", Toast.LENGTH_SHORT).show();

                    s = new Socket(InetAddress.getByName("ec2-52-10-72-68.us-west-2.compute.amazonaws.com"), PORT);

                    Log.i(TAG, "SOCKET CREATED");

                    in = new BufferedReader(new InputStreamReader(s.getInputStream()));
                    out = new PrintWriter(s.getOutputStream());
                    connection = true;

                    Log.i(TAG, "CONNECTED TO SERVER!!!!!!!!!!!!!!!!!!!!!!!!");
//                    Toast.makeText(getApplicationContext(), "Connected to server", Toast.LENGTH_LONG).show();
                } catch (UnknownHostException e) {
                    Log.i(TAG, "~~~~~~~~~~~~~ UnknownHostException ~~~~~~~~~~");
//                    Toast.makeText(getApplicationContext(), "Problem connecting to server...", Toast.LENGTH_LONG).show();
                } catch (IOException e) {
                    Log.i(TAG, "~~~~~~~~~~~~~~~~~ IOException ~~~~~~~~~~~~~~~~~");
//                    Toast.makeText(getApplicationContext(), "Problem setting up I/O", Toast.LENGTH_LONG).show();

                }
                return null;
            }

            @Override
            protected void onPostExecute(String errorMsg) {
//                Log.i(TAG, "RECEIVING MESSAGES.");
                incoming();
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    void incoming() {
        new AsyncTask<Void, String, Void>() {
            @Override
            protected Void doInBackground(Void... args) {
                try {
                    while (connection) {
                        Log.i(TAG, "INCOMING MESSAGE...");
                        String next;
                        next = in.readLine();
                        if (next != null) {
                            publishProgress(next);
                        }
                        else {
                            break;
                        }
                    }
                } catch (IOException e) {
                }
                return null;
            }

            @Override
            protected void onProgressUpdate(String... lines) {
                String m = lines[0];

                String[] data = m.split(",");
                if (data.length == 1) {
                    output.setText(data[0]);
                }
                else {
                    distance = Double.parseDouble(data[0]);
                    time = Double.parseDouble(data[1]);

                    output.setText("Distance: " + distance + "\nTime: " + time);
                }
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    boolean send(String m) {
        if (!connection) {
            return false;
        }

        new AsyncTask<String, Void, Boolean>() {

            @Override
            protected Boolean doInBackground(String... m) {
                Log.i(TAG, "Trying to send message...");
                out.println(m[0]);
                return out.checkError();
            }

            @Override
            protected void onPostExecute(Boolean e) {}
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, m);

        return true;
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
