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
//import android.content.ContextWrapper;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.Switch;
import android.widget.TextView;

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

import org.w3c.dom.Text;

import bolts.Continuation;
import bolts.Task;

import java.io.File;
import java.io.IOException;
import java.io.FileOutputStream;
import java.io.OutputStream;

import java.lang.Math;

import java.util.Calendar;


/**
 * A placeholder fragment containing a simple view.
 */
public class DeviceSetupActivityFragment extends Fragment implements ServiceConnection {
    public interface FragmentSettings {
        BluetoothDevice getBtDevice();
    }

    private TextView switchStatus;
    private Switch dataTypeSwitch;
    private TextView AccelAngleText;
    private TextView CombinedAngleText;
    private TextView TimeText;

    private MetaWearBoard metawear = null;
    private FragmentSettings settings;
    private Accelerometer accelerometer;
    private GyroBmi160 gyroscope;
    private Debug debugModule;

    File file;
    private String filename;
    boolean available;
    Context ctx;

    // Initialize Variables
    double accel_raw_x;
    double accel_raw_y;
    double accel_raw_z;
    double gyro_raw_x;
    double gyro_raw_y;
    double gyro_raw_z;
    double previous_accel_raw_x;
    double previous_gyro_raw_x;

    String time_stamp;

    String accel_string_x;
    String accel_string_y;
    String accel_string_z;
    String gyro_string_x;
    String gyro_string_y;
    String gyro_string_z;

    double deltaTime;
    double previousTime;
    double currentTime;
    double totalTime;

    double vertical_sensor_from_accel_0;
    double vertical_sensor_from_accel_1;
    double vertical_sensor_from_accel_2;
    double angular_velocity_body_matrix_0_0;
    double angular_velocity_body_matrix_0_1;
    double angular_velocity_body_matrix_0_2;
    double angular_velocity_body_matrix_1_0;
    double angular_velocity_body_matrix_1_1;
    double angular_velocity_body_matrix_1_2;
    double angular_velocity_body_matrix_2_0;
    double angular_velocity_body_matrix_2_1;
    double angular_velocity_body_matrix_2_2;
    double vertical_sensor_dot_from_gyro_0;
    double vertical_sensor_dot_from_gyro_1;
    double vertical_sensor_dot_from_gyro_2;
    double vertical_sensor_dot_from_accel_0;
    double vertical_sensor_dot_from_accel_1;
    double vertical_sensor_dot_from_accel_2;
    double vertical_sensor_dot_0;
    double vertical_sensor_dot_1;
    double vertical_sensor_dot_2;
    double sensor_axis_sensor_0 = 0;
    double sensor_axis_sensor_1 = 1;
    double sensor_axis_sensor_2 = 0;
    double vertical_sensor_0;
    double vertical_sensor_1;
    double vertical_sensor_2;
    double alpha = 3;
    double inclination_angle;
    double inclination_angle_from_accel;
    double vertical_sensor_from_accel_norm;
    double vertical_sensor_norm;

    int feedback_toggle = 0;
    int motor_status = 0;
    double threshold = 90;
    int calculateInclination = 1;

    // Column Titles
    String csv_raw_entry = "time" + "," + "accelerometer_x" + "," + "accelerometer_y" + "," + "acceleromter_z" + "," + "gyroscope_x" + "," + "gyroscope_y"
            + "," + "gyroscope_z" + "\n" + "sec" + "," + "g" + "," + "g" + "," + "g" + "," + "deg/sec" + "," + "deg/sec" + "," + "deg/sec" + "\n";
    String csv_inclination_entry = "time" + "," + "inclination angle" + "," + "feedback toggle" + "," + "motor status" + "," + "inclination angle from accelerometers"
            + "," + "threshold" + "," + "alpha" + "\n" + "sec" + "," + "deg" + "," + "binary" + "," + "binary" + "," + "deg" + "," + "deg" + "," + " " + "\n";


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
        ctx = owner.getApplicationContext();
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

        switchStatus = (TextView) view.findViewById(R.id.switchStatus);
        AccelAngleText = (TextView) view.findViewById(R.id.AccelAngleText);
        CombinedAngleText = (TextView) view.findViewById(R.id.CombinedAngleText);
        dataTypeSwitch = (Switch) view.findViewById(R.id.dataTypeSwitch);
        TimeText = (TextView) view. findViewById(R.id.TimeText);
        AccelAngleText.setText("Accel Inclination Angle: " + accel_string_x);
        CombinedAngleText.setText("Combined Inclination Angle: " + accel_raw_y);

        //set the switch to ON
        dataTypeSwitch.setChecked(true);
        //attach a listener to check for changes in state
        dataTypeSwitch.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView,
                                         boolean isChecked) {
                if(isChecked){
                    switchStatus.setText("Calculating Inclination Angle");
                    calculateInclination = 1;
                }else{
                    switchStatus.setText("Collecting Raw Data");
                    calculateInclination = 0;
                }
            }
        });



        //check the current state before we display the screen
        if(dataTypeSwitch.isChecked()){
            switchStatus.setText("Calculating Inclination Angle");
            calculateInclination = 1;
        }
        else {
            switchStatus.setText("Collecting Raw Data");
            calculateInclination = 0;
        }

        view.findViewById(R.id.start).setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                accelerometer.acceleration().addRouteAsync(new RouteBuilder() {
                    @Override
                    public void configure(RouteComponent source) {
                        source.stream(new Subscriber() {
                            @Override
                            public void apply(Data data, Object... env) {

                                // Get Timestamp
                                currentTime = System.currentTimeMillis();
                                deltaTime = (currentTime - previousTime)/1000;

                                // Read Accel and Prepare for writing to CSV
                                accel_raw_x = data.value(Acceleration.class).x();
                                accel_raw_y = data.value(Acceleration.class).y();
                                accel_raw_z = data.value(Acceleration.class).z();
                                accel_string_x = Double.toString(accel_raw_x);
                                accel_string_y = Double.toString(accel_raw_y);
                                accel_string_z = Double.toString(accel_raw_z);


                                if (totalTime == 0){
                                    double vertical_sensor_norm_init = Math.sqrt(accel_raw_x * accel_raw_x
                                            + accel_raw_y * accel_raw_y + accel_raw_z  * accel_raw_z);
                                    vertical_sensor_0 = accel_raw_x * 1 / vertical_sensor_norm_init;
                                    vertical_sensor_1 = accel_raw_y * 1 / vertical_sensor_norm_init;
                                    vertical_sensor_2 = accel_raw_z * 1 / vertical_sensor_norm_init;

                                }
                               /* Log.i("accel_x", accel_string_x);
                                Log.i("accel_y", accel_string_y);
                                Log.i("accel_z", accel_string_z);*/
                                if (accel_raw_x != previous_accel_raw_x) {
                                    previous_accel_raw_x = accel_raw_x;
                                    totalTime = totalTime + deltaTime;
                                    time_stamp = Double.toString(totalTime);
                                    previousTime = currentTime;

                                }
                            }
                        });
                    }
                }).continueWith(new Continuation<Route, Void>() {

                    @Override
                    public Void then(Task<Route> task) throws Exception {
                        accelerometer.acceleration().start();
                        accelerometer.start();
                        Log.i("MainActivity", "Running!");
                        currentTime = System.currentTimeMillis();
                        previousTime = currentTime;
                        totalTime = 0;
                        return null;
                    }
                });
                gyroscope.angularVelocity().addRouteAsync(new RouteBuilder() {
                    @Override
                    public void configure(RouteComponent source) {
                        source.stream(new Subscriber() {
                            @Override
                            public void apply(Data data, Object... env) {
                                // Read Gyro and Prepare for writing to CSV
                                gyro_raw_x = data.value(AngularVelocity.class).x();
                                gyro_raw_y = data.value(AngularVelocity.class).y();
                                gyro_raw_z = data.value(AngularVelocity.class).z();
                                gyro_raw_x = Math.toRadians(gyro_raw_x);
                                gyro_raw_y = Math.toRadians(gyro_raw_y);
                                gyro_raw_z = Math.toRadians(gyro_raw_z);

                                gyro_string_x = Double.toString(gyro_raw_x);
                                gyro_string_y = Double.toString(gyro_raw_y);
                                gyro_string_z = Double.toString(gyro_raw_z);


                                if (gyro_raw_x != previous_gyro_raw_x) {
                                    previous_gyro_raw_x = gyro_raw_x;
                                    /*Log.i("gyro_x", gyro_string_x);
                                    Log.i("gyro_y", gyro_string_y);
                                    Log.i("gyro_z", gyro_string_z);*/
                                    if (calculateInclination == 1) {
                                        //// CALCULATE INCLINATION ANGLE /////
                                        vertical_sensor_from_accel_0 = accel_raw_x;
                                        vertical_sensor_from_accel_1 = accel_raw_y;
                                        vertical_sensor_from_accel_2 = accel_raw_z;

                                        // normalize accelerometer estimate
                                        vertical_sensor_from_accel_norm = Math.sqrt(vertical_sensor_from_accel_0 * vertical_sensor_from_accel_0
                                                + vertical_sensor_from_accel_1 * vertical_sensor_from_accel_1 + vertical_sensor_from_accel_2
                                                * vertical_sensor_from_accel_2);

                                        vertical_sensor_from_accel_0 = vertical_sensor_from_accel_0 / vertical_sensor_from_accel_norm;
                                        vertical_sensor_from_accel_1 = vertical_sensor_from_accel_1 / vertical_sensor_from_accel_norm;
                                        vertical_sensor_from_accel_2 = vertical_sensor_from_accel_2 / vertical_sensor_from_accel_norm;

                                        // GYROSCOPE INCLINATION ANGLE
                                        angular_velocity_body_matrix_0_0 = 0;
                                        angular_velocity_body_matrix_0_1 = -gyro_raw_z;
                                        angular_velocity_body_matrix_0_2 = gyro_raw_y;
                                        angular_velocity_body_matrix_1_0 = gyro_raw_z;
                                        angular_velocity_body_matrix_1_1 = 0;
                                        angular_velocity_body_matrix_1_2 = -gyro_raw_x;
                                        angular_velocity_body_matrix_2_0 = -gyro_raw_y;
                                        angular_velocity_body_matrix_2_1 = gyro_raw_x;
                                        angular_velocity_body_matrix_2_2 = 0;

                                        // rotational velocity based on gyroscope readings
                                        vertical_sensor_dot_from_gyro_0 = -(angular_velocity_body_matrix_0_0 * vertical_sensor_0 + angular_velocity_body_matrix_0_1 * vertical_sensor_1 + angular_velocity_body_matrix_0_2 * vertical_sensor_2);
                                        vertical_sensor_dot_from_gyro_1 = -(angular_velocity_body_matrix_1_0 * vertical_sensor_0 + angular_velocity_body_matrix_1_1 * vertical_sensor_1 + angular_velocity_body_matrix_1_2 * vertical_sensor_2);
                                        vertical_sensor_dot_from_gyro_2 = -(angular_velocity_body_matrix_2_0 * vertical_sensor_0 + angular_velocity_body_matrix_2_1 * vertical_sensor_1 + angular_velocity_body_matrix_2_2 * vertical_sensor_2);

                                        // rotational velocity based on difference to accelerometer estimate
                                        vertical_sensor_dot_from_accel_0 = -alpha * (vertical_sensor_0 - vertical_sensor_from_accel_0);
                                        vertical_sensor_dot_from_accel_1 = -alpha * (vertical_sensor_1 - vertical_sensor_from_accel_1);
                                        vertical_sensor_dot_from_accel_2 = -alpha * (vertical_sensor_2 - vertical_sensor_from_accel_2);

                                        // combine the two estimates
                                        vertical_sensor_dot_0 = vertical_sensor_dot_from_gyro_0 + vertical_sensor_dot_from_accel_0;
                                        vertical_sensor_dot_1 = vertical_sensor_dot_from_gyro_1 + vertical_sensor_dot_from_accel_1;
                                        vertical_sensor_dot_2 = vertical_sensor_dot_from_gyro_2 + vertical_sensor_dot_from_accel_2;

                                        // integrate rotational velocity
                                        vertical_sensor_0 = vertical_sensor_0 + deltaTime * vertical_sensor_dot_0;
                                        vertical_sensor_1 = vertical_sensor_1 + deltaTime * vertical_sensor_dot_1;
                                        vertical_sensor_2 = vertical_sensor_2 + deltaTime * vertical_sensor_dot_2;

                                        // normalize after integration
                                        vertical_sensor_norm = Math.sqrt(vertical_sensor_0 * vertical_sensor_0 + vertical_sensor_1 * vertical_sensor_1 + vertical_sensor_2 * vertical_sensor_2);
                                        vertical_sensor_0 = vertical_sensor_0 / vertical_sensor_norm;
                                        vertical_sensor_1 = vertical_sensor_1 / vertical_sensor_norm;
                                        vertical_sensor_2 = vertical_sensor_2 / vertical_sensor_norm;

                                        // calculate inclination angles
                                        inclination_angle = Math.acos(vertical_sensor_0 * sensor_axis_sensor_0 + vertical_sensor_1 * sensor_axis_sensor_1 + vertical_sensor_2 * sensor_axis_sensor_2);
                                        inclination_angle_from_accel = Math.acos(vertical_sensor_from_accel_0 * sensor_axis_sensor_0 + vertical_sensor_from_accel_1 * sensor_axis_sensor_1 + vertical_sensor_from_accel_2 * sensor_axis_sensor_2);
                                        // convert to degrees
                                        inclination_angle = Math.toDegrees(inclination_angle);
                                        inclination_angle_from_accel = Math.toDegrees(inclination_angle_from_accel);

                                        csv_inclination_entry = csv_inclination_entry + time_stamp + "," + inclination_angle + "," + feedback_toggle + "," +
                                                motor_status + "," + inclination_angle_from_accel + "," + threshold + "," + alpha + "," + accel_string_x + "," + accel_string_y + "," + accel_string_z
                                                + "," + gyro_string_x + "," + gyro_string_y + "," + gyro_string_z + "\n";
                                        //AccelAngleText.setText(("Accel Inclination Angle: " + inclination_angle_from_accel));
                                        // CombinedAngleText.setText(("Combined Inclination Angle: " + inclination_angle));
                                    } else {
                                        // Concat all data since start
                                        csv_raw_entry = csv_raw_entry + time_stamp + "," + accel_string_x + "," + accel_string_y + "," + accel_string_z
                                                + "," + gyro_string_x + "," + gyro_string_y + "," + gyro_string_z + "\n";
                                        //AccelAngleText.setText("Accel Inclination Angle: "+ accel_string_x);
                                        //CombinedAngleText.setText("Combined Inclination Angle:  + Raw Data" );
                                    }
                                }
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

        view.findViewById(R.id.stop).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i("MainActivity", "Stopped!");
                accelerometer.stop();
                accelerometer.acceleration().stop();
                gyroscope.stop();
                gyroscope.angularVelocity().stop();
                //metawear.tearDown();
            }
        });

        /*view.findViewById(R.id.sens_reset).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //debugModule.resetAsync();
            }
        });*/

        view.findViewById(R.id.data_save).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (calculateInclination == 1) {
                    filename = java.text.DateFormat.getDateTimeInstance().format(Calendar.getInstance().getTime());
                    filename = filename.replaceAll("[^a-zA-Z0-9]", "_") + "_" + "InclinationAngle" ;
                    file = new File(ctx.getExternalFilesDir(null), filename);
                    try {
                        OutputStream os = new FileOutputStream(file);
                        os.write(csv_inclination_entry.getBytes());
                        os.close();
                        Log.i("MainActivity", "File is created as..." + filename);
                    } catch (IOException e) {
                        Log.i("MainActivity", "File NOT created ...!");
                        e.printStackTrace();
                    }
                }
                else{
                    filename = java.text.DateFormat.getDateTimeInstance().format(Calendar.getInstance().getTime());
                    filename = filename.replaceAll("[^a-zA-Z0-9]", "_") + "_" +"RawData";
                    file = new File(ctx.getExternalFilesDir(null),  filename);
                    try {
                        OutputStream os = new FileOutputStream(file);
                        os.write(csv_raw_entry.getBytes());
                        os.close();
                        Log.i("MainActivity", "File is created as..." + filename);
                    } catch (IOException e) {
                        Log.i("MainActivity", "File NOT created ...!");
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        metawear = ((BtleService.LocalBinder) service).getMetaWearBoard(settings.getBtDevice());
        accelerometer = metawear.getModule(Accelerometer.class);
        accelerometer.configure()
                .odr(25f)      // set sampling frequency
                .commit();

        gyroscope = metawear.getModule(GyroBmi160.class);
        gyroscope.configure().odr(GyroBmi160.OutputDataRate.ODR_25_HZ)
                //.range(GyroBmi160.Range.FSR_125)
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
