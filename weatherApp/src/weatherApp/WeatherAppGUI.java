package weatherApp;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EtchedBorder;

/**
 * The WeatherAppGUI class provides a graphical user interface (GUI) for displaying 
 * weather information based on a user's input location. It fetches data from a weather 
 * API, displays the current weather and forecast, and allows the user to view their 
 * search history.
 * This app has a dynamic backgroung which changes whith the time of day.
 * Showing weather data and a weather Icon. 
 * https://github.com/jtranberg/java-weather-app.git
 * @author Jay Tranberg
 * @since 2024-08-13
 */
public class WeatherAppGUI {
    private JFrame frame;
    private JTextField locationField;
    private JLabel weatherInfoLabel;
    private JLabel weatherIconLabel;
    private JPanel mainPanel;
    private JPanel forecastPanel;
    private JScrollPane forecastScrollPane;
    private JComboBox<String> unitComboBox;
    private JButton viewHistoryButton;
    private List<String> searchHistory;

    /**
     * Constructs the WeatherAppGUI and initializes the GUI components.
     */
    public WeatherAppGUI() {
        frame = new JFrame("My Weather App");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(600, 500);
        frame.setLayout(new BorderLayout());

        searchHistory = new ArrayList<>();

        mainPanel = new BackgroundPanel();
        mainPanel.setLayout(new BorderLayout());

        JPanel inputPanel = new JPanel();
        inputPanel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        locationField = new JTextField(50);
        locationField.setBorder(new EtchedBorder(15));
        JButton fetchButton = new JButton("Get Weather");
        JButton showForecastButton = new JButton("Show Forecast");
        viewHistoryButton = new JButton("View History");

        unitComboBox = new JComboBox<>(new String[]{"Celsius", "Fahrenheit"});
        unitComboBox.setSelectedItem("Celsius");
        unitComboBox.setPreferredSize(new Dimension(80, unitComboBox.getPreferredSize().height));
        unitComboBox.setBorder(new EtchedBorder(15));

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0.5;
        inputPanel.add(new JLabel("Enter Location:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 4.0;
        gbc.gridwidth = 1;
        inputPanel.add(locationField, gbc);

        gbc.gridx = 2;
        gbc.weightx = 0.0;
        inputPanel.add(fetchButton, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = .3;
        gbc.gridwidth = 1;
        inputPanel.add(unitComboBox, gbc);

        gbc.gridx = 2;
        gbc.weightx = 0.0;
        gbc.gridwidth = 1;
        inputPanel.add(showForecastButton, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 1.0;
        gbc.gridwidth = 3;
        inputPanel.add(viewHistoryButton, gbc);

        weatherInfoLabel = new JLabel();
        weatherInfoLabel.setHorizontalAlignment(SwingConstants.CENTER);
        weatherInfoLabel.setForeground(Color.LIGHT_GRAY);
        weatherIconLabel = new JLabel();
        weatherIconLabel.setHorizontalAlignment(SwingConstants.CENTER);

        mainPanel.add(inputPanel, BorderLayout.NORTH);
        mainPanel.add(weatherInfoLabel, BorderLayout.CENTER);
        mainPanel.add(weatherIconLabel, BorderLayout.SOUTH);

        forecastPanel = new JPanel(new BorderLayout());
        JLabel forecastLabel = new JLabel();
        forecastLabel.setHorizontalAlignment(SwingConstants.CENTER);
        forecastLabel.setForeground(Color.LIGHT_GRAY);
        JButton backButton = new JButton("Back");
        backButton.addActionListener(e -> showMainPanel());

        forecastPanel.add(forecastLabel, BorderLayout.CENTER);
        forecastPanel.add(backButton, BorderLayout.SOUTH);

        forecastScrollPane = new JScrollPane(forecastPanel);
        forecastScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        forecastScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

        frame.add(mainPanel, BorderLayout.CENTER);

        fetchButton.addActionListener(e -> fetchAndDisplayWeather());
        showForecastButton.addActionListener(e -> showForecast());
        viewHistoryButton.addActionListener(e -> showSearchHistory());

        frame.setVisible(true);
    }

    /**
     * Fetches weather data from the WeatherAPI and updates the GUI to display the weather information.
     * If the location is invalid or the data fetch fails, an error message is shown.
     */
    @SuppressWarnings("deprecation")
    private void fetchAndDisplayWeather() {
        String location = locationField.getText().trim();
        if (location.isEmpty()) {
            showError("Please enter city, country.");
            return;
        }

        searchHistory.add(location);

        String unit = unitComboBox.getSelectedItem().toString().equals("Celsius") ? "metric" : "imperial";

        try {
            WeatherData weatherData = WeatherAPI.getWeatherData(location, unit);

            if (weatherData.hasError()) {
                weatherInfoLabel.setText("<html><font color='black'>" + weatherData.getErrorMessage() + "</font></html>");
                weatherIconLabel.setIcon(null);
            } else {
                weatherInfoLabel.setText(String.format(
                    "<html><font color='white', size='6'>Temperature: %.2f%s<br>Humidity: %d%%<br>Wind Speed: %.2f m/s<br>Conditions: %s</font></html>",
                    weatherData.getTemperature(),
                    unit.equals("metric") ? "°C" : "°F",
                    weatherData.getHumidity(),
                    weatherData.getWindSpeed(),
                    weatherData.getDescription()
                ));

                try {
                    String iconUrlStr = weatherData.getIconUrl();
                    System.out.println("Icon URL: " + iconUrlStr);  // Debugging line
                    URL iconUrl = new URL(iconUrlStr);

                    BufferedImage iconImage = ImageIO.read(iconUrl);
                    if (iconImage != null) {
                        ImageIcon icon = new ImageIcon(iconImage);  // No scaling for testing
                        SwingUtilities.invokeLater(() -> {
                            weatherIconLabel.setIcon(icon);
                            weatherIconLabel.revalidate();
                            weatherIconLabel.repaint();
                        });
                    } else {
                        weatherIconLabel.setText("Icon image is null.");
                    }
                } catch (MalformedURLException ex) {
                    weatherIconLabel.setText("Invalid icon URL.");
                    ex.printStackTrace();
                } catch (Exception ex) {
                    weatherIconLabel.setText("Icon not available.");
                    ex.printStackTrace();
                }
            }
        } catch (Exception e) {
            showError("Failed to fetch weather data. Please try again later.");
            e.printStackTrace();
        }
    }

    /**
     * Fetches and displays the weather forecast for the entered location.
     * If the location is invalid, an error message is shown.
     */
    private void showForecast() {
        String location = locationField.getText().trim();
        if (location.isEmpty()) {
            showError("Please enter a location.");
            return;
        }

        String unit = unitComboBox.getSelectedItem().toString().equals("Celsius") ? "metric" : "imperial";
        try {
            String forecastData = WeatherAPI.getForecastData(location, unit);

            JLabel forecastLabel = (JLabel) forecastPanel.getComponent(0);
            forecastLabel.setText("<html><font color='lightgrey'>" + forecastData.replaceAll("\n", "<br>") + "</font></html>");

            frame.getContentPane().remove(mainPanel);
            frame.getContentPane().add(forecastScrollPane, BorderLayout.CENTER);
            frame.revalidate();
            frame.repaint();
        } catch (Exception e) {
            showError("Failed to fetch forecast data. Please try again later.");
        }
    }

    /**
     * Displays the main panel with the input fields and weather information.
     */
    private void showMainPanel() {
        frame.getContentPane().remove(forecastScrollPane);
        frame.getContentPane().add(mainPanel, BorderLayout.CENTER);
        frame.revalidate();
        frame.repaint();
    }

    /**
     * Displays the search history in a dialog. Allows the user to select a previous location 
     * to fetch its weather information.
     */
    private void showSearchHistory() {
        if (searchHistory.isEmpty()) {
            JOptionPane.showMessageDialog(frame, "No search history available.", "History", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        JList<String> historyList = new JList<>(searchHistory.toArray(new String[0]));
        historyList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        historyList.setVisibleRowCount(10);

        JScrollPane historyScrollPane = new JScrollPane(historyList);
        historyScrollPane.setPreferredSize(new Dimension(300, 200));

        int result = JOptionPane.showConfirmDialog(frame, historyScrollPane, "Search History", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result == JOptionPane.OK_OPTION) {
            String selectedLocation = historyList.getSelectedValue();
            if (selectedLocation != null) {
                locationField.setText(selectedLocation);
                fetchAndDisplayWeather();
            }
        }
    }

    /**
     * Displays an error message in a dialog.
     * 
     * @param message the error message to display
     */
    private void showError(String message) {
        JOptionPane.showMessageDialog(frame, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    /**
     * Sets a dynamic background image for the given panel based on the current time of day.
     * 
     * @param panel the panel to set the background image on
     */
    @SuppressWarnings("unused")
    private void setDynamicBackground(JPanel panel) {
        panel.setOpaque(false); // Ensure panel is transparent

        try {
            String imagePath = null;
            BufferedImage bgImage = ImageIO.read(new File(imagePath));
            panel.setLayout(new BorderLayout());

            // Create a custom JPanel to handle painting the background image
            JPanel backgroundPanel = new JPanel(new BorderLayout()) {
                private static final long serialVersionUID = 1L;

                @Override
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    if (bgImage != null) {
                        g.drawImage(bgImage, 0, 0, getWidth(), getHeight(), this);
                    }
                }
            };

            panel.add(backgroundPanel, BorderLayout.CENTER);

            // Ensure the background panel is resized and painted correctly
            backgroundPanel.setSize(panel.getSize());
            panel.revalidate();
            panel.repaint();

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Background image not found.");
        }
    }

    /**
     * The main method to launch the WeatherAppGUI application.
     * 
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(WeatherAppGUI::new);
    }

    /**
     * The BackgroundPanel class provides a custom JPanel with a dynamic background image 
     * that changes based on the current time of day (morning, afternoon, evening).
     */
    private class BackgroundPanel extends JPanel {
        private static final long serialVersionUID = 459471842384024179L;

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Calendar now = Calendar.getInstance();
            int hour = now.get(Calendar.HOUR_OF_DAY);
            String imagePath = "";

            if (hour >= 4 && hour < 8) {
                imagePath = "src/images/morning.jpg";
            } else if (hour >= 8 && hour < 12) {
                imagePath = "src/images/afternoon.jpg";
            } else if (hour >= 12 && hour < 18) {
                imagePath = "src/images/evening.jpg";
            }else {
                imagePath = "src/images/latenight.jpg";
            }

            try {
                BufferedImage bgImage = ImageIO.read(new File(imagePath));
                g.drawImage(bgImage, 0, 0, getWidth(), getHeight(), this);
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("Background image not found.");
            }
        }
    }
}
