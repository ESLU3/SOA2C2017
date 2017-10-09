/*********************************************************************************************************
 * Archivo que contine la clase encargada de modificar el grafico del velocimetro en la activity correspondiente
 **********************************************************************************************************/

package com.example.esteban.android_http;

import android.graphics.Color;

import com.cardiomood.android.controls.gauge.BatteryIndicatorGauge;
import com.cardiomood.android.controls.gauge.SpeedometerGauge;

/**
 * Created by esteban on 12/05/2016.
 */
public class Velocimetro
{

    private SpeedometerGauge speedometer;
    BatteryIndicatorGauge batteryindicator;

    public Velocimetro(SpeedometerGauge speedomete)
    {
        speedometer=speedomete;
        //speedomete.setMaxSpeed(50);
       speedometer.setLabelConverter(new SpeedometerGauge.LabelConverter()
       {
            @Override
            public String getLabelFor(double progress, double maxProgress)
            {
                return String.valueOf((int) Math.round(progress));
            }
        });

        speedometer.setMaxSpeed(1024);
        speedometer.setMajorTickStep(100);
        speedometer.setMinorTicks(4);
        speedometer.addColoredRange(0, 700, Color.GREEN);
        speedometer.addColoredRange(700, 900, Color.YELLOW);
        speedometer.addColoredRange(900, 1024, Color.RED);
        speedometer.setSpeed(500, 1000, 300);


    }

    public void modificarVelocimetro(float valor)
    {
        speedometer.setSpeed(valor, 1000, 300);
    }
}
