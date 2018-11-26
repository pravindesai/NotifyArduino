package com.example.batman.notifyarduino;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;


public class MainActivity extends AppCompatActivity {

    EditText editText;
    Button button;
    ListView Listview;
    ArrayList<String> List;
    ArrayAdapter<String> Adapter;

    BluetoothAdapter mBluetoothAdapter;
    BluetoothSocket mmSocket;
    BluetoothDevice mmDevice;
    OutputStream mmOutputStream;
    InputStream mmInputStream;
    volatile boolean stopWorker;
    String appName,Title,text,Pack;
    ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imageView=findViewById(R.id.imageView);
        editText=findViewById(R.id.editText);
        button=findViewById(R.id.button);
        Listview=findViewById(R.id.Listview);
        Toast.makeText(MainActivity.this,"Allow Notification Access to App in Setting.",Toast.LENGTH_LONG).show();


        LocalBroadcastManager.getInstance(this).registerReceiver(onNotice,new IntentFilter("Msg"));

        List=new ArrayList<String>();
        Adapter=new ArrayAdapter<>(this,android.R.layout.simple_expandable_list_item_1,List);
        Listview.setAdapter(Adapter);

        try{
            findBT();
            if (mmDevice != null) {
                openBT();
            }
        }catch(Exception Ex){

        }

         button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mBluetoothAdapter.isEnabled())
                {
                    try {
                        sendData();
                        editText.setText("");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                else{
                    Toast.makeText(MainActivity.this,"No bluetooth Connected",Toast.LENGTH_SHORT).show();
                }

            }
        });
        

        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent in=new Intent(Intent.ACTION_VIEW, Uri.parse("http://twitter.com/pravindesai__"));
                startActivity(in);
            }
        });
    }

    private BroadcastReceiver onNotice=new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {

             Pack=intent.getStringExtra("package");
             Title=intent.getStringExtra("title");
             text=intent.getStringExtra("text");

            PackageManager packageManager= getApplicationContext().getPackageManager();
            try {
                appName = (String) packageManager.getApplicationLabel(packageManager.getApplicationInfo(Pack, PackageManager.GET_META_DATA));
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }

            String Msg="App: "+appName +
                    "\nTitle : "+Title+
                    "\n text : "+text;

         List.add(Msg);
         Adapter.notifyDataSetChanged();

            try {
                sendAppData();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    };


    void findBT()
    {
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if(mBluetoothAdapter == null)
        {
            Toast.makeText(MainActivity.this,"No bluetooth adapter available",Toast.LENGTH_SHORT).show();
        }

        if(!mBluetoothAdapter.isEnabled())
        {
            Intent enableBluetooth = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBluetooth, 0);
        }

        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
        if(pairedDevices.size() > 0)
        {
            for(BluetoothDevice device : pairedDevices)
            {
                if(device.getName().equals("HC-05") || device.getName().equals("HC-06"))
                {
                    mmDevice = device;
                    break;
                }
            }
        }
        Toast.makeText(MainActivity.this,"Device Found",Toast.LENGTH_SHORT).show();

    }

    void openBT() throws IOException
    {
        UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"); //Standard SerialPortService ID
        mmSocket = mmDevice.createRfcommSocketToServiceRecord(uuid);
        mmSocket.connect();
        mmOutputStream = mmSocket.getOutputStream();
        mmInputStream = mmSocket.getInputStream();

        Toast.makeText(MainActivity.this,"Connected...",Toast.LENGTH_SHORT).show();
    }



    void sendData() throws IOException
    {
        String msg = editText.getText().toString();
        mmOutputStream.write(msg.getBytes());

        Toast.makeText(MainActivity.this,"Data Sent",Toast.LENGTH_SHORT).show();
    }

    void sendAppData() throws IOException {

        mmOutputStream.write(appName.getBytes());
        mmOutputStream.write("\n".getBytes());         //send New Line Character
        mmOutputStream.write(Title.getBytes());
        mmOutputStream.write("\n".getBytes());           //send New Line Character
        mmOutputStream.write(text.getBytes());
    }

    void closeBT() throws IOException
    {
        stopWorker = true;
        mmOutputStream.close();
        mmInputStream.close();
        mmSocket.close();
        Toast.makeText(MainActivity.this,"Bluetooth Closed",Toast.LENGTH_SHORT).show();
    }


}
