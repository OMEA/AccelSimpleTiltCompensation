package com.kircherelectronics.accelsimpletiltcompensation;

import com.androidplot.xy.XYPlot;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.app.Activity;
import android.app.Dialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.ToggleButton;
import android.widget.SeekBar.OnSeekBarChangeListener;

/*
 * Copyright 2013, Kircher Electronics
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * Plots the acceleration in the x, y and z axes. The linear acceleration is determined
 * by subtracting the acceleration from itself when it is determined that the device
 * is not experiencing linear acceleration.
 * 
 * @author Kaleb@KircherElectronics
 * @version 1.0
 */
public class TiltCompensationActivity extends Activity implements
		SensorEventListener, OnSeekBarChangeListener, OnTouchListener
{

	private double alpha = 0.5;
	private float timestampAccel;
	private float timestampAccelOld;

	private float timestampMag;
	private float timestampMagOld;

	private float dt;

	// Raw accelerometer data
	private float[] inputAccel = new float[3];
	private float[] inputMag = new float[3];
	// The gravity components of the acceleration signal.
	private float[] components = new float[3];

	private String tag = "Sensor Rotation";

	private SensorManager sensorManager;

	private double threshold = 1.05;

	private MeanFilter meanFilterMagnitude;

	private int accelerationCount = 0;
	private int countThreshold = 5;

	// Touch to zoom constants
	private float distance = 0;
	private float zoom = 10;

	// The Acceleration View
	private PlotView plotView;

	private TextView tvAlpha;
	private TextView tvWindow;
	private TextView tvSamplePeroid;
	private TextView tvUpdateFrequency;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_tilt_compensation);

		meanFilterMagnitude = new MeanFilter();
		meanFilterMagnitude.setWindowSize(20);

		plotView = new PlotView(
				(XYPlot) this.findViewById(R.id.dynamicLinePlot));
		plotView.setMaxRange(zoom);
		plotView.setMinRange(-zoom);

		SeekBar sbw = (SeekBar) findViewById(R.id.sliderWindow);
		sbw.setProgress(countThreshold);
		sbw.setOnSeekBarChangeListener(this);

		SeekBar sba = (SeekBar) findViewById(R.id.sliderAlpha);
		sba.setProgress(500);
		sba.setOnSeekBarChangeListener(this);

		tvAlpha = (TextView) findViewById(R.id.alpha);
		tvAlpha.setText("Alpha: " + Double.toString(alpha));

		tvWindow = (TextView) findViewById(R.id.window);
		tvWindow.setText("Count Threshold: " + Integer.toString(countThreshold));

		tvSamplePeroid = (TextView) findViewById(R.id.samplePeriod);
		tvSamplePeroid.setText(Float.toString(0));

		tvUpdateFrequency = (TextView) findViewById(R.id.updateFrequency);
		tvUpdateFrequency.setText(Float.toString(0));

		sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_tilt_compensation, menu);
		return true;
	}

	@Override
	protected void onResume()
	{
		super.onResume();

		// Register for sensor updates.
		sensorManager.registerListener(this,
				sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
				SensorManager.SENSOR_DELAY_FASTEST);

		sensorManager.registerListener(this,
				sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD),
				SensorManager.SENSOR_DELAY_FASTEST);
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void onSensorChanged(SensorEvent event)
	{
		if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER)
		{
			timestampAccel = event.timestamp;

			// Make sure the timestamp for the event has changed.
			if (timestampAccel != timestampAccelOld)
			{
				// Get a local copy of the sensor values
				System.arraycopy(event.values, 0, inputAccel, 0,
						event.values.length);
			}

		}

		if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD)
		{
			timestampMag = event.timestamp;

			// Make sure the timestamp for the event has changed.
			if (timestampMag != timestampMagOld)
			{
				// Get a local copy of the sensor values
				System.arraycopy(event.values, 0, inputMag, 0,
						event.values.length);

				// Find the sample period (between updates).
				// Convert from nanoseconds to seconds
				dt = (timestampMag - timestampMagOld) / 1000000000.0f;

				tvSamplePeroid.setText("Sample Peroid: " + Float.toString(dt));
				tvUpdateFrequency.setText("Update Frequency: "
						+ Float.toString(1 / dt));

				float magnitude = (float) (Math.sqrt(Math.pow(inputAccel[0], 2)
						+ Math.pow(inputAccel[1], 2)
						+ Math.pow(inputAccel[2], 2)) / SensorManager.GRAVITY_EARTH);

				// Dynamically calculate a threshold for detecting linear
				// acceleration
				// from the magnitude.
				if (magnitude <= threshold && magnitude > 0.95)
				{
					double mean = meanFilterMagnitude.filterFloat(magnitude);

					// Use a weighted average to "push" the threshold
					// towards the mean, plus a small constant.
					threshold += (alpha * (mean - threshold)) + 0.01;

					accelerationCount++;
				} else
				{
					accelerationCount = 0;
				}
				
				Log.d(tag, "Mag: " + Double.toString(magnitude));
				Log.d(tag, "Thresh: " + Double.toString(threshold));

				// There are certain singularities in the calculation
				// of the magnitude that can cause the magnitude to be
				// equal to gravity despite linear acceleration. We filter
				// out singularities by assuming they won't occur more than
				// a defined number of times in a row. A smaller threshold
				// count will result in faster response, but a greater
				// likelihood of a singularity passing though the filter and
				// distorting the estimation.
				if (accelerationCount >= countThreshold)
				{
					// Find the gravity component of the X-axis
					components[0] = inputAccel[0];

					// Find the gravity component of the Y-axis
					components[1] = inputAccel[1];

					// Find the gravity component of the Z-axis
					components[2] = inputAccel[2];
				}

				float[] tiltAccel = new float[3];

				// Subtract the gravity component of the signal
				// from the input acceleration signal to get the
				// tilt compensated output.
				tiltAccel[0] = inputAccel[0] - components[0];
				tiltAccel[1] = inputAccel[1] - components[1];
				tiltAccel[2] = inputAccel[2] - components[2];

				plotView.setData(inputAccel, components, tiltAccel);
			}

		}

		this.timestampAccelOld = this.timestampAccel;
		this.timestampMagOld = this.timestampMag;
	}

	@Override
	public void onProgressChanged(SeekBar seekBar, int progress,
			boolean fromUser)
	{
		if (seekBar.equals((SeekBar) findViewById(R.id.sliderWindow)))
		{
			countThreshold = progress;
			tvWindow.setText("Time Constant: "
					+ Integer.toString(countThreshold));
		}
		if (seekBar.equals((SeekBar) findViewById(R.id.sliderAlpha)))
		{
			alpha = progress / 1000.0f;
			tvAlpha.setText("Alpha: " + Double.toString(alpha));
		}
	}

	@Override
	public boolean onTouch(View v, MotionEvent e)
	{
		Log.d(tag, "test");
		// MotionEvent reports input details from the touch screen
		// and other input controls.
		float newDist = 0;

		switch (e.getAction())
		{

		case MotionEvent.ACTION_MOVE:

			// pinch to zoom
			if (e.getPointerCount() == 2)
			{
				if (distance == 0)
				{
					distance = fingerDist(e);
				}

				newDist = fingerDist(e);

				zoom *= distance / newDist;

				plotView.setMaxRange(zoom);
				plotView.setMinRange(-zoom);

				distance = newDist;
			}
		}

		return false;
	}

	/**
	 * Event Handling for Individual menu item selected Identify single menu
	 * item by it's id
	 * */
	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{

		switch (item.getItemId())
		{

		case R.id.menu_settings:
			showSettingsDialog();
			return true;

		default:
			return super.onOptionsItemSelected(item);
		}
	}

	/**
	 * Build a setting dialog and display it.
	 */
	private void showSettingsDialog()
	{
		final Dialog setOffsetDialog = new Dialog(this);
		setOffsetDialog.setTitle("Offset Calibration");
		setOffsetDialog.setCancelable(true);
		setOffsetDialog.setCanceledOnTouchOutside(true);
		LayoutInflater inflater = (LayoutInflater) this
				.getSystemService(LAYOUT_INFLATER_SERVICE);

		View layout = inflater.inflate(R.layout.settings,
				(ViewGroup) findViewById(R.id.settings_dialog_root_element));
		setOffsetDialog.setContentView(layout);

		Button doneButton = (Button) layout.findViewById(R.id.done_button);
		doneButton.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View view)
			{
				setOffsetDialog.cancel();
			}
		});

		final ToggleButton xAxisToggleButton = (ToggleButton) layout
				.findViewById(R.id.xaxistogglebutton);
		xAxisToggleButton.setChecked(plotView.isDrawXAxis());

		xAxisToggleButton.setOnClickListener(new OnClickListener()
		{
			public void onClick(View v)
			{
				if (xAxisToggleButton.isChecked())
				{
					plotView.setDrawXAxis(true);
				} else
				{
					plotView.setDrawXAxis(false);
				}
			}
		});

		final ToggleButton yAxisToggleButton = (ToggleButton) layout
				.findViewById(R.id.yaxistogglebutton);
		yAxisToggleButton.setChecked(plotView.isDrawYAxis());
		yAxisToggleButton.setOnClickListener(new OnClickListener()
		{
			public void onClick(View v)
			{
				if (yAxisToggleButton.isChecked())
				{
					plotView.setDrawYAxis(true);
				} else
				{
					plotView.setDrawYAxis(false);
				}
			}
		});

		final ToggleButton zAxisToggleButton = (ToggleButton) layout
				.findViewById(R.id.zaxistogglebutton);
		zAxisToggleButton.setChecked(plotView.isDrawZAxis());
		zAxisToggleButton.setOnClickListener(new OnClickListener()
		{
			public void onClick(View v)
			{
				if (zAxisToggleButton.isChecked())
				{
					plotView.setDrawZAxis(true);
				} else
				{
					plotView.setDrawZAxis(false);
				}
			}
		});

		final ToggleButton rawToggleButton = (ToggleButton) layout
				.findViewById(R.id.rawtogglebutton);
		rawToggleButton.setChecked(plotView.isDrawRaw());
		rawToggleButton.setOnClickListener(new OnClickListener()
		{
			public void onClick(View v)
			{
				if (rawToggleButton.isChecked())
				{
					plotView.setDrawRaw(true);
				} else
				{
					plotView.setDrawRaw(false);
				}
			}
		});

		final ToggleButton gravityToggleButton = (ToggleButton) layout
				.findViewById(R.id.gravitytogglebutton);
		gravityToggleButton.setChecked(plotView.isDrawGravity());
		gravityToggleButton.setOnClickListener(new OnClickListener()
		{
			public void onClick(View v)
			{
				if (gravityToggleButton.isChecked())
				{
					plotView.setDrawGravity(true);
				} else
				{
					plotView.setDrawGravity(false);
				}
			}
		});

		final ToggleButton accelToggleButton = (ToggleButton) layout
				.findViewById(R.id.linearacceltogglebutton);
		accelToggleButton.setChecked(plotView.isDrawAccel());
		accelToggleButton.setOnClickListener(new OnClickListener()
		{
			public void onClick(View v)
			{
				if (accelToggleButton.isChecked())
				{
					plotView.setDrawAccel(true);
				} else
				{
					plotView.setDrawAccel(false);
				}
			}
		});

		setOffsetDialog.show();
	}

	private final float fingerDist(MotionEvent event)
	{
		float x = event.getX(0) - event.getX(1);
		float y = event.getY(0) - event.getY(1);
		return (float) Math.sqrt(x * x + y * y);
	}

	@Override
	public void onStartTrackingTouch(SeekBar seekBar)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar)
	{
		// TODO Auto-generated method stub

	}

}