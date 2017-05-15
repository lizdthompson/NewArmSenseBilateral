/*
 * Copyright 2015 MbientLab Inc. All rights reserved.
 *
 * IMPORTANT: Your use of this Software is limited to those specific rights
 * granted under the terms of a software license agreement between the user who
 * downloaded the software, his/her employer (which must be your employer) and
 * MbientLab Inc, (the "License").  You may not use this Software unless you
 * agree to abide by the terms of the License which can be found at
 * www.mbientlab.com/terms . The License limits your use, and you acknowledge,
 * that the  Software may not be modified, copied or distributed and can be used
 * solely and exclusively in conjunction with a MbientLab Inc, product.  Other
 * than for the foregoing purpose, you may not use, reproduce, copy, prepare
 * derivative works of, modify, distribute, perform, display or sell this
 * Software and/or its documentation for any purpose.
 *
 * YOU FURTHER ACKNOWLEDGE AND AGREE THAT THE SOFTWARE AND DOCUMENTATION ARE
 * PROVIDED "AS IS" WITHOUT WARRANTY OF ANY KIND, EITHER EXPRESS OR IMPLIED,
 * INCLUDING WITHOUT LIMITATION, ANY WARRANTY OF MERCHANTABILITY, TITLE,
 * NON-INFRINGEMENT AND FITNESS FOR A PARTICULAR PURPOSE. IN NO EVENT SHALL
 * MBIENTLAB OR ITS LICENSORS BE LIABLE OR OBLIGATED UNDER CONTRACT, NEGLIGENCE,
 * STRICT LIABILITY, CONTRIBUTION, BREACH OF WARRANTY, OR OTHER LEGAL EQUITABLE
 * THEORY ANY DIRECT OR INDIRECT DAMAGES OR EXPENSES INCLUDING BUT NOT LIMITED
 * TO ANY INCIDENTAL, SPECIAL, INDIRECT, PUNITIVE OR CONSEQUENTIAL DAMAGES, LOST
 * PROFITS OR LOST DATA, COST OF PROCUREMENT OF SUBSTITUTE GOODS, TECHNOLOGY,
 * SERVICES, OR ANY CLAIMS BY THIRD PARTIES (INCLUDING BUT NOT LIMITED TO ANY
 * DEFENSE THEREOF), OR OTHER SIMILAR COSTS.
 *
 * Should you have any questions regarding your right to use this Software,
 * contact MbientLab Inc, at www.mbientlab.com.
 */

package com.mbientlab.metawear.tutorial.starter;


import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.content.ComponentName;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.mbientlab.metawear.Data;
import com.mbientlab.metawear.data.Acceleration;
import com.mbientlab.metawear.data.AngularVelocity;

import com.mbientlab.metawear.MetaWearBoard;
import com.mbientlab.metawear.Route;
import com.mbientlab.metawear.Subscriber;
import com.mbientlab.metawear.android.BtleService;

import com.mbientlab.metawear.builder.RouteBuilder;
import com.mbientlab.metawear.builder.RouteComponent;

import com.mbientlab.metawear.module.Accelerometer;
import com.mbientlab.metawear.module.GyroBmi160;
import com.mbientlab.metawear.module.Debug;

import bolts.Continuation;
import bolts.Task;

import java.io.File;
import java.io.IOException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.regex.Pattern;

import android.os.Environment;


/**
 * A placeholder fragment containing a simple view.
 */
public class DeviceSetupActivityFragment extends Fragment implements ServiceConnection {
    public interface FragmentSettings {
        BluetoothDevice getBtDevice();
    }

    private MetaWearBoard metawear = null;
    private FragmentSettings settings;
    private Accelerometer accelerometer;
    private GyroBmi160 gyroscope;
    private Debug debugModule;

    File file;
    private String filename = "newfile";
    boolean available;


    Float accel_raw_x;
    Float accel_raw_y;
    Float accel_raw_z;
    Float gyro_raw_x;
    Float gyro_raw_y;
    Float gyro_raw_z;

    String accel_string_x;
    String accel_string_y;
    String accel_string_z;
    String gyro_string_x;
    String gyro_string_y;
    String gyro_string_z;

    String accel_csv_x;
    String accel_csv_y;
    String accel_csv_z;
    String gyro_csv_x;
    String gyro_csv_y;
    String gyro_csv_z;

    String csv_entry = "time" + "," + "accelerometer_x" + "," + "accelerometer_y" + "," + "acceleromter_z" + "," + "gyroscope_x" + "," + "gyroscope_y" + "," + "gyroscope_z" + "\n";

    public DeviceSetupActivityFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Activity owner= getActivity();
        if (!(owner instanceof FragmentSettings)) {
            throw new ClassCastException("Owning activity must implement the FragmentSettings interface");
        }

        settings= (FragmentSettings) owner;
        owner.getApplicationContext().bindService(new Intent(owner, BtleService.class), this, Context.BIND_AUTO_CREATE);
        Context ctx = owner.getApplicationContext();

        String state = Environment.getExternalStorageState();
            if (Environment.MEDIA_MOUNTED.equals(state)) {

                available = true;
                Log.i("MainActivity", "External storage available, yay!");
            }
            else{
                available = false;
                Log.i("MainActivity", "External storage not available :(");
            }
        file = new File(ctx.getExternalFilesDir(null),filename);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        ///< Unbind the service when the activity is destroyed
        getActivity().getApplicationContext().unbindService(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setRetainInstance(true);
        return inflater.inflate(R.layout.fragment_device_setup, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        view.findViewById(R.id.acc_start).setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                accelerometer.acceleration().addRouteAsync(new RouteBuilder() {
                    @Override
                    public void configure(RouteComponent source) {
                        source.stream(new Subscriber() {
                            @Override
                            public void apply(Data data, Object... env) {

                                accel_raw_x = data.value(Acceleration.class).x();
                                accel_raw_y = data.value(Acceleration.class).y();
                                accel_raw_z = data.value(Acceleration.class).z();

                                // Prepare for writing to CSV
                                accel_string_x = accel_raw_x.toString();
                                accel_string_y = accel_raw_y.toString();
                                accel_string_z = accel_raw_z.toString();

                                //accel_csv_x = accel_csv_x + accel_string_x + ",\n";
                                //accel_csv_y = accel_csv_y + accel_string_y + ",\n";
                                //accel_csv_z = accel_csv_z + accel_string_z + ",\n";

                                // Write to serial port
                                //Log.i("MainActivity", accel_raw_x.toString());
                                //Log.i("MainActivity", "Running!");

                            }
                        });
                    }
                }).continueWith(new Continuation<Route, Void>() {

                    @Override
                    public Void then(Task<Route> task) throws Exception {
                        accelerometer.acceleration().start();
                        accelerometer.start();
                        return null;
                    }
                });
                gyroscope.angularVelocity().addRouteAsync(new RouteBuilder() {
                    @Override
                    public void configure(RouteComponent source) {
                        source.stream(new Subscriber() {
                            @Override
                            public void apply(Data data, Object... env) {
                                String gyro_entry = data.value(AngularVelocity.class).toString();

                                gyro_raw_x = data.value(AngularVelocity.class).x();
                                gyro_raw_y = data.value(AngularVelocity.class).y();
                                gyro_raw_z = data.value(AngularVelocity.class).z();

                                // Prepare for writing to CSV
                                gyro_string_x = gyro_raw_x.toString();
                                gyro_string_y = gyro_raw_y.toString();
                                gyro_string_z = gyro_raw_z.toString();

                                //gyro_csv_x = gyro_csv_x + gyro_string_x + ",\n";
                                //gyro_csv_y = gyro_csv_y + gyro_string_y + ",\n";
                                //gyro_csv_z = gyro_csv_z + gyro_string_z + ",\n";
                                //Log.i("MainActivity", gyro_entry);
                                //csv_entry = accel_string_x +  accel_string_y +  accel_string_z + ",\n";
                                csv_entry = csv_entry + accel_string_x + "," + accel_string_y + "," + accel_string_z + "," + gyro_string_x + "," + gyro_string_y + "," + gyro_string_z + "\n";
                            }
                        });
                    }
                }).continueWith(new Continuation<Route, Void>() {
                    @Override
                    public Void then(Task<Route> task) throws Exception {
                        gyroscope.angularVelocity().start();
                        gyroscope.start();
                        return null;
                    }
                });
            }
        });

        view.findViewById(R.id.acc_stop).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                accelerometer.stop();
                accelerometer.acceleration().stop();
                gyroscope.stop();
                gyroscope.angularVelocity().stop();
                //metawear.tearDown();
            }
        });

        view.findViewById(R.id.sens_reset).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //debugModule.resetAsync();

            }
        });

        view.findViewById(R.id.data_save).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    OutputStream os = new FileOutputStream(file);
                    //csv_entry = accel_csv_x + accel_csv_y + accel_csv_z + gyro_csv_x + gyro_csv_y + gyro_csv_z;
                    os.write(csv_entry.getBytes());
                    os.close();
                    Log.i("MainActivity", "File is created!" + filename);
                } catch (IOException e) {
                    Log.i("MainActivity", "File NOT created ...!");
                    e.printStackTrace();
                }
                }
        });
    }

        @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        metawear = ((BtleService.LocalBinder) service).getMetaWearBoard(settings.getBtDevice());
        accelerometer = metawear.getModule(Accelerometer.class);
        accelerometer.configure()
                .odr(50f)      // set sampling frequency
                .commit();

        gyroscope = metawear.getModule(GyroBmi160.class);
        gyroscope.configure().odr(GyroBmi160.OutputDataRate.ODR_50_HZ)
                .range(GyroBmi160.Range.FSR_125)
                .commit();
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {

    }

    /**
     * Called when the app has reconnected to the board
     */
    public void reconnected() { }


}
