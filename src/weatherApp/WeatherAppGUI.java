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

        unitComboBox = new JComboBox<>(new String[] {"Celsius", "Fahrenheit"});
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

    private void showMainPanel() {
        frame.getContentPane().remove(forecastScrollPane);
        frame.getContentPane().add(mainPanel, BorderLayout.CENTER);
        frame.revalidate();
        frame.repaint();
    }

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

    private void showError(String message) {
        JOptionPane.showMessageDialog(frame, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

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


    public static void main(String[] args) {
        SwingUtilities.invokeLater(WeatherAppGUI::new);
    }

    private class BackgroundPanel extends JPanel {
        /**
		 * 
		 */
		private static final long serialVersionUID = 459471842384024179L;

		@Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Calendar now = Calendar.getInstance();
            int hour = now.get(Calendar.HOUR_OF_DAY);
            String imagePath = "";

            if (hour >= 6 && hour < 12) {
                imagePath = "src/images/morning.jpg";
            } else if (hour >= 12 && hour < 18) {
                imagePath = "src/images/afternoon.jpg";
            } else {
                imagePath = "src/images/evening.jpg";
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
