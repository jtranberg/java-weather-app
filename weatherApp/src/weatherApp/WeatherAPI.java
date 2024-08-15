package weatherApp;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import org.json.JSONArray;
import org.json.JSONObject;

//weather App Api. endpoints,icon,forcast, currentweather
/**
 * weather App api. this is the api to connect to openweather api
 * endpoints include: current weather, forecast, and icon url's
 */

public class WeatherAPI {
    private static final String API_KEY = "f7016d740da7c098b97c1e4f547188b7"; // Replace with your actual API key
    private static final String WEATHER_API_URL = "http://api.openweathermap.org/data/2.5/weather?q=%s&units=%s&appid=%s";
    private static final String FORECAST_API_URL = "https://api.openweathermap.org/data/2.5/forecast?q=%s&units=%s&appid=%s";
    private static final String ICON_URL = "http://openweathermap.org/img/wn/%s@2x.png";
    // constructor location and units.
    //get the weather data.
    /**
     * be sure to close your connection and catch those errors.
     * @param location get location
     * @param units time units 
     * @return
     */
    
    //get mathod
    @SuppressWarnings("deprecation")
	public static WeatherData getWeatherData(String location, String units) {
        try {
            String urlString = String.format(WEATHER_API_URL, location, units, API_KEY);
            URL url = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            int responseCode = connection.getResponseCode();
            if (responseCode == 200) {
                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String inputLine;
                StringBuilder content = new StringBuilder();
                while ((inputLine = in.readLine()) != null) {
                    content.append(inputLine);
                }
                in.close();
                connection.disconnect();
                return parseWeatherData(content.toString());
            } else if (responseCode == 404) {
                return new WeatherData("Error: City not found. Please enter a valid location.");
            } else {
                return new WeatherData("Error: Unable to fetch weather data (Code " + responseCode + ")");
            }
        } catch (Exception e) {
            return new WeatherData("Exception: " + e.getMessage());
        }
    }
/**
 * get the forecast data
 * @param location
 * @param units
 * @return
 */
    @SuppressWarnings("deprecation")
	public static String getForecastData(String location, String units) {
        try {
            String urlString = String.format(FORECAST_API_URL, location, units, API_KEY);
            URL url = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            int responseCode = connection.getResponseCode();
            if (responseCode == 200) {
                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String inputLine;
                StringBuilder content = new StringBuilder();
                while ((inputLine = in.readLine()) != null) {
                    content.append(inputLine);
                }
                in.close();
                connection.disconnect();
                return parseForecastData(content.toString());
            } else {
                return "Error: Unable to fetch forecast data (Code " + responseCode + ")";
            }
        } catch (Exception e) {
            return "Exception: " + e.getMessage();
        }
    }
/**
 * pasre the forecast json data
 * @param jsonData data parsing forecast data
 * @return
 */
    private static String parseForecastData(String jsonData) {
        JSONObject jsonObject = new JSONObject(jsonData);
        JSONArray list = jsonObject.getJSONArray("list");

        StringBuilder forecastBuilder = new StringBuilder();
        for (int i = 0; i < list.length(); i++) {
            JSONObject item = list.getJSONObject(i);
            String date = item.getString("dt_txt"); // Extract date and time of forecast
            double temp = item.getJSONObject("main").getDouble("temp");
            String description = item.getJSONArray("weather").getJSONObject(0).getString("description");

            forecastBuilder.append(String.format("Date: %s\nTemperature: %.2f°C\nDescription: %s\n\n", date, temp, description));
        }
        return forecastBuilder.toString();
    }
/**
 * parsing current weather data
 * @param jsonData
 * @return
 */
    private static WeatherData parseWeatherData(String jsonData) {
        JSONObject jsonObject = new JSONObject(jsonData);

        JSONObject main = jsonObject.getJSONObject("main");
        double temperature = main.getDouble("temp");
        int humidity = main.getInt("humidity");

        JSONObject wind = jsonObject.getJSONObject("wind");
        double windSpeed = wind.getDouble("speed");

        JSONObject weather = jsonObject.getJSONArray("weather").getJSONObject(0);
        String weatherDescription = weather.getString("description");
        String iconCode = weather.getString("icon");
        String iconUrl = String.format(ICON_URL, iconCode);

        // Include latitude and longitude for historical data fetching
        double lat = jsonObject.getJSONObject("coord").getDouble("lat");
        double lon = jsonObject.getJSONObject("coord").getDouble("lon");

        WeatherData weatherData = new WeatherData(temperature, humidity, windSpeed, weatherDescription, iconUrl);
        weatherData.setLat(lat);
        weatherData.setLon(lon);

        return weatherData;
    }
}

