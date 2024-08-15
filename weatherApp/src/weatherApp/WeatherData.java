package weatherApp;

public class WeatherData {
    private double temperature;
    private int humidity;
    private double windSpeed;
    private String description;
    private String iconUrl;
    private boolean error;
    private String errorMessage;
    private double lat;
    private double lon;

    // Constructors, getters, and setters

    public WeatherData(double temperature, int humidity, double windSpeed, String description, String iconUrl) {
        this.temperature = temperature;
        this.humidity = humidity;
        this.windSpeed = windSpeed;
        this.description = description;
        this.iconUrl = iconUrl;
        this.error = false;
    }

    public WeatherData(String errorMessage) {
        this.error = true;
        this.errorMessage = errorMessage;
    }

    public double getTemperature() {
        return temperature;
    }

    public int getHumidity() {
        return humidity;
    }

    public double getWindSpeed() {
        return windSpeed;
    }

    public String getDescription() {
        return description;
    }

    public String getIconUrl() {
        return iconUrl;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLon() {
        return lon;
    }

    public void setLon(double lon) {
        this.lon = lon;
    }

    public boolean hasError() {
        return error;
    }

    public String getErrorMessage() {
        return errorMessage;
    }
}

