package Main;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.util.*;
import java.util.List;

public class RegistryEditor extends JFrame {
    private static final String REG_PATH = "HKEY_LOCAL_MACHINE\\SOFTWARE\\WOW6432Node\\SXP\\Settings\\Install";
    private static final String REG_KEY = "SysPath";

    private JComboBox<String> pathDropdown;
    private JLabel statusLabel;
    private JButton selectFileButton;
    private JButton applyButton;
    private File selectedFile;

    public RegistryEditor() {
        setTitle("Registry Editor");
        setSize(450, 250);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new FlowLayout());

        // Button zum Laden der Config-Datei
        selectFileButton = new JButton("Config-Datei auswählen");
        selectFileButton.addActionListener(e -> selectConfigFile());

        // Dropdown (wird erst nach Dateiauswahl befüllt)
        pathDropdown = new JComboBox<>();
        pathDropdown.setEnabled(false);

        // Button zum Ändern der Registry (deaktiviert, bis Datei gewählt wurde)
        applyButton = new JButton("SysPath ändern");
        applyButton.setEnabled(false);
        applyButton.addActionListener(e -> updateRegistry());

        // Status-Anzeige
        statusLabel = new JLabel("Bitte eine Config-Datei auswählen.");

        // GUI-Elemente hinzufügen
        add(selectFileButton);
        add(new JLabel("Neuer SysPath:"));
        add(pathDropdown);
        add(applyButton);
        add(statusLabel);

        setVisible(true);
    }

    private void selectConfigFile() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Wähle die Config-Datei");
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            selectedFile = fileChooser.getSelectedFile();
            List<String> paths = readPathsFromFile(selectedFile);
            if (!paths.isEmpty()) {
                pathDropdown.removeAllItems();
                for (String path : paths) {
                    pathDropdown.addItem(path);
                }
                pathDropdown.setEnabled(true);
                applyButton.setEnabled(true);
                statusLabel.setText("Config geladen: " + selectedFile.getName());
            } else {
                statusLabel.setText("Fehler: Datei enthält keine gültigen Pfade.");
            }
        }
    }

    private List<String> readPathsFromFile(File file) {
        List<String> paths = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                paths.add(line.trim());
            }
        } catch (IOException e) {
            statusLabel.setText("Fehler beim Lesen der Datei.");
        }
        return paths;
    }

    private void updateRegistry() {
        String selectedPath = (String) pathDropdown.getSelectedItem();
        if (selectedPath != null) {
            String command = "reg add " + REG_PATH + " /v " + REG_KEY + " /t REG_SZ /d \"" + selectedPath + "\" /f";
            System.out.println(command);
            try {
                Process process = Runtime.getRuntime().exec(new String[]{"cmd.exe", "/c", command});
                process.waitFor();
                if (process.exitValue() == 0) {
                    statusLabel.setText("Erfolgreich geändert auf: " + selectedPath);
                } else {
                    statusLabel.setText("Fehler beim Schreiben in die Registry.");
                }
            } catch (Exception e) {
                statusLabel.setText("Registry-Fehler: " + e.getMessage());
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new RegistryEditor());
    }
}
