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
    public static final String SERVER = "ec2-52-11-19-67.us-west-2.compute.amazonaws.com";
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

//        network();

        connect();

    }

    void network () {
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... args) {
                try {

                    Log.i(TAG, "Trying to connect to server...");
//                    Toast.makeText(getApplicationContext(), "Trying to connect to server...", Toast.LENGTH_SHORT).show();

                    s = new Socket(InetAddress.getByName("ec2-52-10-25-178.us-west-2.compute.amazonaws.com"), PORT);

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

    void connect() {

        new AsyncTask<Void, Void, String>() {

            String errorMsg = null;

            @Override
            protected String doInBackground(Void... args) {
                Log.i(TAG, "Connect task started");
                try {
                    connection = false;
                    s = new Socket(SERVER, PORT);
                    Log.i(TAG, "Socket created");
                    in = new BufferedReader(new InputStreamReader(
                            s.getInputStream()));
                    out = new PrintWriter(s.getOutputStream());

                    connection = true;
                    Log.i(TAG, "Input and output streams ready");

                } catch (UnknownHostException e1) {
                    errorMsg = e1.getMessage();
                } catch (IOException e1) {
                    errorMsg = e1.getMessage();
                    try {
                        if (out != null) {
                            out.close();
                        }
                        if (s != null) {
                            s.close();
                        }
                    } catch (IOException ignored) {
                    }
                }
                Log.i(TAG, "Connect task finished");
                return errorMsg;
            }

            @Override
            protected void onPostExecute(String errorMsg) {
                if (errorMsg == null) {
                    Toast.makeText(getApplicationContext(),
                            "Connected to server", Toast.LENGTH_SHORT).show();

//                    hideConnectingText();
//                    showLoginControls();

                    // start receiving
                    receive();

                } else {
                    Toast.makeText(getApplicationContext(),
                            "Error: " + errorMsg, Toast.LENGTH_SHORT).show();
                    // can't connect: close the activity
                    finish();
                }
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    void receive() {
        new AsyncTask<Void, String, Void>() {

            @Override
            protected Void doInBackground(Void... args) {
                Log.i(TAG, "Receive task started");
                try {
                    while (connection) {

                        String msg = in.readLine();

                        if (msg == null) { // other side closed the
                            // connection
                            break;
                        }
                        publishProgress(msg);
                    }

                } catch (UnknownHostException e1) {
                    Log.i(TAG, "UnknownHostException in receive task");
                } catch (IOException e1) {
                    Log.i(TAG, "IOException in receive task");
                } finally {
                    connection = false;
                    try {
                        if (out != null)
                            out.close();
                        if (s != null)
                            s.close();
                    } catch (IOException e) {
                    }
                }
                Log.i(TAG, "Receive task finished");
                return null;
            }

            @Override
            protected void onProgressUpdate(String... lines) {
                // the message received from the server is
                // guaranteed to be not null
//                String msg = lines[0];

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


                // if we haven't returned yet, tell the user that we have an unhandled message
                Toast.makeText(getApplicationContext(), "Unhandled message: "+m, Toast.LENGTH_SHORT).show();
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

    boolean send(String msg) {
        if (!connection) {
            Log.i(TAG, "can't send: not connected");
            return false;
        }

        new AsyncTask<String, Void, Boolean>() {

            @Override
            protected Boolean doInBackground(String... msg) {
                Log.i(TAG, "sending: " + msg[0]);
                out.println(msg[0]);
                return out.checkError();
            }

            @Override
            protected void onPostExecute(Boolean error) {
                if (!error) {
//                    Toast.makeText(getApplicationContext(),
//                            "Message sent to server", Toast.LENGTH_SHORT)
//                            .show();
                } else {
                    Toast.makeText(getApplicationContext(),
                            "Error sending message to server",
                            Toast.LENGTH_SHORT).show();
                }
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, msg);

        return true;
    }

    void disconnect() {
        new Thread() {
            @Override
            public void run() {
                if (connection) {
                    connection = false;
                }
                // make sure that we close the output, not the input
                if (out != null) {
                    out.print("BYE");
                    out.flush();
                    out.close();
                }
                // in some rare cases, out can be null, so we need to close the socket itself
                if (s != null)
                    try { s.close();} catch(IOException ignored) {}

                Log.i(TAG, "Disconnect task finished");
            }
        }.start();
    }

//    boolean send(String m) {
//        if (!connection) {
//            return false;
//        }
//
//        new AsyncTask<String, Void, Boolean>() {
//
//            @Override
//            protected Boolean doInBackground(String... m) {
//                Log.i(TAG, "Trying to send message...");
//                out.println(m[0]);
//                return out.checkError();
//            }
//
//            @Override
//            protected void onPostExecute(Boolean e) {}
//        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, m);
//
//        return true;
//    }

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
