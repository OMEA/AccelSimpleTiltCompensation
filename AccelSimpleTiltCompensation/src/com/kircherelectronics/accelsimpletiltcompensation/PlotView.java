package com.kircherelectronics.accelsimpletiltcompensation;

import java.util.ArrayList;
import java.util.LinkedList;

import com.androidplot.xy.BoundaryMode;
import com.androidplot.xy.LineAndPointFormatter;
import com.androidplot.xy.LineAndPointRenderer;
import com.androidplot.xy.SimpleXYSeries;
import com.androidplot.xy.XYPlot;
import android.graphics.Color;

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
 * Acceleration View is responsible for creating and managing all of the plotter
 * components related to Acceleration.
 * 
 * @author Kaleb
 * 
 */
public class PlotView
{
	private int windowSize = 100;

	private double maxRange = 10;
	private double minRange = -10;

	private XYPlot dynamicAPlot;

	private SimpleXYSeries gravityXAxisSeries;
	private SimpleXYSeries gravityYAxisSeries;
	private SimpleXYSeries gravityZAxisSeries;

	private SimpleXYSeries accelXAxisSeries;
	private SimpleXYSeries accelYAxisSeries;
	private SimpleXYSeries accelZAxisSeries;

	private SimpleXYSeries rawXAxisSeries;
	private SimpleXYSeries rawYAxisSeries;
	private SimpleXYSeries rawZAxisSeries;

	private LinkedList<Number> gravityXAxisHistory;
	private LinkedList<Number> gravityYAxisHistory;
	private LinkedList<Number> gravityZAxisHistory;

	private LinkedList<Number> accelXAxisHistory;
	private LinkedList<Number> accelYAxisHistory;
	private LinkedList<Number> accelZAxisHistory;

	private LinkedList<Number> rawXAxisHistory;
	private LinkedList<Number> rawYAxisHistory;
	private LinkedList<Number> rawZAxisHistory;

	private ArrayList<LinkedList<Number>> histories;

	private boolean drawXAxis = true;
	private boolean drawYAxis = true;
	private boolean drawZAxis = true;

	private boolean drawRaw = true;
	private boolean drawGravity = true;
	private boolean drawAccel = true;

	/**
	 * Initialize a new Acceleration View object.
	 * 
	 * @param activity
	 *            the Activity that owns this View.
	 */
	public PlotView(XYPlot dynamicAPlot)
	{
		histories = new ArrayList<LinkedList<Number>>();

		gravityXAxisHistory = new LinkedList<Number>();
		gravityYAxisHistory = new LinkedList<Number>();
		gravityZAxisHistory = new LinkedList<Number>();

		histories.add(gravityXAxisHistory);
		histories.add(gravityYAxisHistory);
		histories.add(gravityZAxisHistory);


		accelXAxisHistory = new LinkedList<Number>();
		accelYAxisHistory = new LinkedList<Number>();
		accelZAxisHistory = new LinkedList<Number>();

		histories.add(accelXAxisHistory);
		histories.add(accelYAxisHistory);
		histories.add(accelZAxisHistory);

		rawXAxisHistory = new LinkedList<Number>();
		rawYAxisHistory = new LinkedList<Number>();
		rawZAxisHistory = new LinkedList<Number>();

		histories.add(rawXAxisHistory);
		histories.add(rawYAxisHistory);
		histories.add(rawZAxisHistory);

		rawXAxisSeries = new SimpleXYSeries("xRaw");
		rawYAxisSeries = new SimpleXYSeries("yRaw");
		rawZAxisSeries = new SimpleXYSeries("zRaw");

		gravityXAxisSeries = new SimpleXYSeries("xGrav");
		gravityYAxisSeries = new SimpleXYSeries("yGrav");
		gravityZAxisSeries = new SimpleXYSeries("zGrav");

		accelXAxisSeries = new SimpleXYSeries("xAccel");
		accelYAxisSeries = new SimpleXYSeries("yAccel");
		accelZAxisSeries = new SimpleXYSeries("zAccel");

		this.dynamicAPlot = dynamicAPlot;

		dynamicAPlot.setRangeBoundaries(minRange, maxRange, BoundaryMode.FIXED);
		dynamicAPlot.setDomainBoundaries(0, windowSize, BoundaryMode.FIXED);

		dynamicAPlot.addSeries(
				rawXAxisSeries,
				LineAndPointRenderer.class,
				new LineAndPointFormatter(Color.rgb(0, 0, 255), Color.rgb(0, 0,
						255), Color.TRANSPARENT));
		dynamicAPlot.addSeries(
				rawYAxisSeries,
				LineAndPointRenderer.class,
				new LineAndPointFormatter(Color.rgb(0, 255, 0), Color.rgb(0,
						255, 0), Color.TRANSPARENT));
		dynamicAPlot.addSeries(
				rawZAxisSeries,
				LineAndPointRenderer.class,
				new LineAndPointFormatter(Color.rgb(255, 0, 0), Color.rgb(255,
						0, 0), Color.TRANSPARENT));

		dynamicAPlot.addSeries(
				gravityXAxisSeries,
				LineAndPointRenderer.class,
				new LineAndPointFormatter(Color.rgb(100, 100, 255), Color.rgb(
						100, 100, 255), Color.TRANSPARENT));
		dynamicAPlot.addSeries(
				gravityYAxisSeries,
				LineAndPointRenderer.class,
				new LineAndPointFormatter(Color.rgb(100, 255, 100), Color.rgb(
						100, 255, 100), Color.TRANSPARENT));
		dynamicAPlot.addSeries(
				gravityZAxisSeries,
				LineAndPointRenderer.class,
				new LineAndPointFormatter(Color.rgb(255, 100, 100), Color.rgb(
						255, 100, 100), Color.TRANSPARENT));

		dynamicAPlot.addSeries(
				accelXAxisSeries,
				LineAndPointRenderer.class,
				new LineAndPointFormatter(Color.rgb(200, 200, 255), Color.rgb(
						200, 200, 255), Color.TRANSPARENT));
		dynamicAPlot.addSeries(
				accelYAxisSeries,
				LineAndPointRenderer.class,
				new LineAndPointFormatter(Color.rgb(200, 255, 200), Color.rgb(
						200, 255, 200), Color.TRANSPARENT));
		dynamicAPlot.addSeries(
				accelZAxisSeries,
				LineAndPointRenderer.class,
				new LineAndPointFormatter(Color.rgb(255, 200, 200), Color.rgb(
						255, 200, 200), Color.TRANSPARENT));

		dynamicAPlot.setDomainStepValue(1);
		dynamicAPlot.setTicksPerRangeLabel(3);
		dynamicAPlot.setDomainLabel(".1/Sec");
		dynamicAPlot.getDomainLabelWidget().pack();
		dynamicAPlot.setRangeLabel("E");
		dynamicAPlot.getRangeLabelWidget().pack();
		dynamicAPlot.disableAllMarkup();
	}

	public double getMaxRange()
	{
		return maxRange;
	}

	public double getMinRange()
	{
		return minRange;
	}

	public int getWindowSize()
	{
		return windowSize;
	}

	public boolean isDrawXAxis()
	{
		return drawXAxis;
	}

	public boolean isDrawYAxis()
	{
		return drawYAxis;
	}

	public boolean isDrawZAxis()
	{
		return drawZAxis;
	}

	public boolean isDrawRaw()
	{
		return drawRaw;
	}

	public boolean isDrawGravity()
	{
		return drawGravity;
	}

	public boolean isDrawAccel()
	{
		return drawAccel;
	}

	public void setDrawXAxis(boolean drawXAxis)
	{
		if (!drawXAxis)
		{
			accelXAxisHistory.removeAll(accelXAxisHistory);
			gravityXAxisHistory.removeAll(gravityXAxisHistory);

			rawXAxisHistory.removeAll(rawXAxisHistory);
		}

		this.drawXAxis = drawXAxis;
	}

	public void setDrawYAxis(boolean drawYAxis)
	{
		if (!drawYAxis)
		{
			accelYAxisHistory.removeAll(accelYAxisHistory);
			gravityYAxisHistory.removeAll(gravityYAxisHistory);
			rawYAxisHistory.removeAll(rawYAxisHistory);
		}

		this.drawYAxis = drawYAxis;
	}

	public void setDrawZAxis(boolean drawZAxis)
	{
		if (!drawZAxis)
		{
			accelZAxisHistory.removeAll(accelZAxisHistory);
			gravityZAxisHistory.removeAll(gravityZAxisHistory);
			rawZAxisHistory.removeAll(rawZAxisHistory);
		}

		this.drawZAxis = drawZAxis;
	}

	public void setDrawRaw(boolean drawRaw)
	{
		if (!drawRaw)
		{
			rawXAxisHistory.removeAll(rawXAxisHistory);
			rawYAxisHistory.removeAll(rawYAxisHistory);
			rawZAxisHistory.removeAll(rawZAxisHistory);
		}

		this.drawRaw = drawRaw;
	}

	public void setDrawGravity(boolean drawGravity)
	{
		if (!drawGravity)
		{
			gravityXAxisHistory.removeAll(gravityXAxisHistory);
			gravityYAxisHistory.removeAll(gravityYAxisHistory);
			gravityZAxisHistory.removeAll(gravityZAxisHistory);
		}

		this.drawGravity = drawGravity;
	}

	public void setDrawAccel(boolean drawAccel)
	{
		if (!drawAccel)
		{
			accelXAxisHistory.removeAll(accelXAxisHistory);
			accelYAxisHistory.removeAll(accelYAxisHistory);
			accelZAxisHistory.removeAll(accelZAxisHistory);
		}

		this.drawAccel = drawAccel;
	}

	public void setDraw(boolean drawWikiLP)
	{
		if (!drawWikiLP)
		{
			gravityXAxisHistory.removeAll(gravityXAxisHistory);
			gravityYAxisHistory.removeAll(gravityYAxisHistory);
			gravityZAxisHistory.removeAll(gravityZAxisHistory);

			accelXAxisHistory.removeAll(accelXAxisHistory);
			accelYAxisHistory.removeAll(accelYAxisHistory);
			accelZAxisHistory.removeAll(accelZAxisHistory);
		}
	}

	public void setMaxRange(double maxRange)
	{
		this.maxRange = maxRange;
		dynamicAPlot.setRangeBoundaries(minRange, maxRange, BoundaryMode.FIXED);
	}

	public void setMinRange(double minRange)
	{
		this.minRange = minRange;
		dynamicAPlot.setRangeBoundaries(minRange, maxRange, BoundaryMode.FIXED);
	}

	public void setWindowSize(int windowSize)
	{
		this.windowSize = windowSize;
	}

	/**
	 * Set the acceleration data.
	 * 
	 * @param ax
	 *            the x-axis.
	 * @param ay
	 *            the y-axis.
	 * @param az
	 *            the z-axis.
	 */
	public void setData(float[] raw, float[] gravity,
			float[] linearAccel)
	{
		for (int i = 0; i < histories.size(); i++)
		{
			enforceWindowLimit(histories.get(i));
		}

		if (drawXAxis)
		{
			if (drawAccel)
			{
				
					accelXAxisHistory.addLast(linearAccel[0]);
			}
			if (drawGravity)
			{
					gravityXAxisHistory.addLast(gravity[0]);
			}
			if (drawRaw)
			{
				rawXAxisHistory.addLast(raw[0]);
			}
		}

		if (drawYAxis)
		{
			if (drawAccel)
			{
					accelYAxisHistory.addLast(linearAccel[1]);
			}
			if (drawGravity)
			{
					gravityYAxisHistory.addLast(gravity[1]);
			}
			if (drawRaw)
			{
				rawYAxisHistory.addLast(raw[1]);
			}
		}

		if (drawZAxis)
		{
			if (drawAccel)
			{
					accelZAxisHistory.addLast(linearAccel[2]);
			}
			if (drawGravity)
			{
					gravityZAxisHistory.addLast(gravity[2]);
			}
			if (drawRaw)
			{
				rawZAxisHistory.addLast(raw[2]);
			}
		}

		accelXAxisSeries.setModel(accelXAxisHistory,
				SimpleXYSeries.ArrayFormat.Y_VALS_ONLY);
		accelYAxisSeries.setModel(accelYAxisHistory,
				SimpleXYSeries.ArrayFormat.Y_VALS_ONLY);
		accelZAxisSeries.setModel(accelZAxisHistory,
				SimpleXYSeries.ArrayFormat.Y_VALS_ONLY);

		gravityXAxisSeries.setModel(gravityXAxisHistory,
				SimpleXYSeries.ArrayFormat.Y_VALS_ONLY);
		gravityYAxisSeries.setModel(gravityYAxisHistory,
				SimpleXYSeries.ArrayFormat.Y_VALS_ONLY);
		gravityZAxisSeries.setModel(gravityZAxisHistory,
				SimpleXYSeries.ArrayFormat.Y_VALS_ONLY);

		rawXAxisSeries.setModel(rawXAxisHistory,
				SimpleXYSeries.ArrayFormat.Y_VALS_ONLY);
		rawYAxisSeries.setModel(rawYAxisHistory,
				SimpleXYSeries.ArrayFormat.Y_VALS_ONLY);
		rawZAxisSeries.setModel(rawZAxisHistory,
				SimpleXYSeries.ArrayFormat.Y_VALS_ONLY);

		dynamicAPlot.redraw();
	}

	private void enforceWindowLimit(LinkedList<Number> data)
	{
		if (data.size() > windowSize)
		{
			data.removeFirst();
		}
	}
}
