package com.nibibibi.dswa2021;

import androidx.appcompat.app.AppCompatActivity;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.StrictMode;
import android.text.Html;
import android.util.Log;
import android.view.WindowManager;
import android.widget.TextView;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Date;
import java.text.DateFormat;
import java.util.Locale;
public class MainActivity extends AppCompatActivity {

    // Open weather map url
    private static final String OPEN_WEATHER_MAP_URL = "http://api.openweathermap.org/data/2.5/weather?q=Minsk&appid=9a3da1c5cea6390a88eb1e82c6f50a37&units=metric&lang=ru";

    // View
    TextView cityField, detailsField, currentTemperatureField, humidityField, pressureField, weatherIcon, updateField;
    Typeface weatherFont;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getSupportActionBar().hide();

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        weatherFont = Typeface.createFromAsset(getApplicationContext().getAssets(), "fonts/weathericons-regular-webfont.ttf");

        // Fields
        cityField = findViewById(R.id.city_field);
        currentTemperatureField = findViewById(R.id.current_temperature_field);
        updateField = findViewById(R.id.updated_field);
        detailsField = findViewById(R.id.details_field);
        humidityField = findViewById(R.id.humidity_field);
        pressureField = findViewById(R.id.pressure_field);
        weatherIcon = findViewById(R.id.weather_icon);
        weatherIcon.setTypeface(weatherFont);

        String[] jsonData = getJSONResponse();

        //Set fields.
        cityField.setText(jsonData[0]);
        detailsField.setText(jsonData[1]);
        currentTemperatureField.setText(jsonData[2]);
        humidityField.setText("Влажность: " + jsonData[3]);
        pressureField.setText("Давление: " + jsonData[4]);
        updateField.setText(jsonData[5]);
        weatherIcon.setText(Html.fromHtml(jsonData[6]));
    }

    // Parsing JSON
    public String[] getJSONResponse(){
        String[]    jsonData = new String[7];
        JSONObject jsonWeather = null;
        try{
            jsonWeather = getJSONWeather();
        }
        catch (Exception e){
            Log.d("Error", "Getting jsonWeather Error", e);
        }

        try{
            if(jsonWeather != null) {
                JSONObject details = jsonWeather.getJSONArray("weather").getJSONObject(0);
                JSONObject main = jsonWeather.getJSONObject("main");
                DateFormat df = DateFormat.getDateInstance();

                String city = jsonWeather.getString("name") + ", " + jsonWeather.getJSONObject("sys").getString("country");
                String description = details.getString("description").toLowerCase(Locale.US);
                String temperature = String.format("%.0f", main.getDouble("temp")) + "°";
                String humidity = main.getString("humidity") + "%";
                String pressure = main.getString("pressure") + " ";
                String updatedOn = df.format(new Date(jsonWeather.getLong("dt") * 1000 ));
                String iconText = setWeatherIcon(details.getInt("id"), jsonWeather.getJSONObject("sys").getLong("sunrise") * 1000, jsonWeather.getJSONObject("sys").getLong("sunset") * 1000);

                jsonData[0] = city;
                jsonData[1] = description;
                jsonData[2] = temperature;
                jsonData[3] = humidity;
                jsonData[4] = pressure;
                jsonData[5] = updatedOn;
                jsonData[6] = iconText;
            }
        }
        catch (Exception e){

        }
        return jsonData;
    }

    // Setting weather icon
    public static String setWeatherIcon(int actualId, long sunrise, long sunset){
        int id = actualId / 100;
        String icon = "";
        if (actualId == 800){
            long currentTime = new java.util.Date().getTime();
            if (currentTime >= sunrise && currentTime < sunset){
                icon = "&#xf00d;";
            }
            else {
                icon = "&#xf02e;";
            }
        }
        else {
            switch (id){
                case 2:
                    icon = "&#xf01e;";
                    break;
                case 3:
                    icon = "&#xf01c;";
                    break;
                case 7:
                    icon = "&#xf014;";
                    break;
                case 8:
                    icon = "&#xf013;";
                    break;
                case 6:
                    icon = "&#xf01b;";
                    break;
                case 5:
                    icon = "&#xf019;";
                    break;
            }
        }
        return icon;
    }

    // Getting JSON
    public static JSONObject getJSONWeather(){
        try{
            HttpURLConnection connection = (HttpURLConnection) (new URL(OPEN_WEATHER_MAP_URL).openConnection());
            connection.setRequestMethod("GET");
            connection.setDoInput(true);
            connection.setDoOutput(true);
            connection.connect();


            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuffer json = new StringBuffer(1024);
            String tmp = "";
            while ((tmp = reader.readLine()) != null){
                json.append(tmp).append("\n");
            }
            reader.close();
            JSONObject data = new JSONObject(json.toString());
            if (data.getInt("cod") != 200){
                return null;
            }
            return data;
        }
        catch (Exception e){
            return null;
        }
    }
}