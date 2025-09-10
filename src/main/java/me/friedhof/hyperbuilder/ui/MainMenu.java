package me.friedhof.hyperbuilder.ui;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import me.friedhof.hyperbuilder.Game;
import me.friedhof.hyperbuilder.save.SavedWorldInfo;
import me.friedhof.hyperbuilder.save.WorldSaveManager;
import me.friedhof.hyperbuilder.computation.modules.ItemRegistry;

/**
 * Main menu GUI for the Hyperbuilder game.
 * Provides options for creating new worlds, loading existing worlds, and exiting.
 */
public class MainMenu extends JFrame {
    private static final String GAME_TITLE = "Hyperbuilder";
    private static final String VERSION = "1.3.2";
    
    private Game game;
    private WorldSaveManager saveManager;
    
    public MainMenu() {
        this.saveManager = new WorldSaveManager();
        initializeUI();
    }
    
    private void initializeUI() {
        setTitle(GAME_TITLE + " v" + VERSION);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setSize(600, 400);
        setLocationRelativeTo(null);
        setResizable(false);
        
        // Create main panel with background
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());
        mainPanel.setBackground(new Color(45, 45, 45));
        
        // Title panel
        JPanel titlePanel = new JPanel();
        titlePanel.setBackground(new Color(45, 45, 45));
        titlePanel.setBorder(BorderFactory.createEmptyBorder(40, 20, 20, 20));
        
        JLabel titleLabel = new JLabel(GAME_TITLE, SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 36));
        titleLabel.setForeground(Color.WHITE);
        titlePanel.add(titleLabel);
        
        JLabel versionLabel = new JLabel("Version " + VERSION, SwingConstants.CENTER);
        versionLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        versionLabel.setForeground(Color.LIGHT_GRAY);
        titlePanel.add(versionLabel);
        
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        versionLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        // Button panel
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
        buttonPanel.setBackground(new Color(45, 45, 45));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(20, 100, 60, 100));
        
        // Create buttons
        JButton newGameButton = createMenuButton("New Game");
        JButton loadGameButton = createMenuButton("Load Game");
        JButton exitButton = createMenuButton("Exit");
        
        // Add button listeners
        newGameButton.addActionListener(e -> showNewGameDialog());
        loadGameButton.addActionListener(e -> showLoadGameDialog());
        exitButton.addActionListener(e -> System.exit(0));
        
        // Add buttons to panel
        buttonPanel.add(newGameButton);
        buttonPanel.add(Box.createVerticalStrut(15));
        buttonPanel.add(loadGameButton);
        buttonPanel.add(Box.createVerticalStrut(15));
        buttonPanel.add(exitButton);
        
        // Add panels to main panel
        mainPanel.add(titlePanel, BorderLayout.NORTH);
        mainPanel.add(buttonPanel, BorderLayout.CENTER);
        
        add(mainPanel);
    }
    
    private JButton createMenuButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("Arial", Font.BOLD, 18));
        button.setPreferredSize(new Dimension(200, 50));
        button.setMaximumSize(new Dimension(200, 50));
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        // Style the button
        button.setBackground(new Color(70, 130, 180));
        button.setForeground(Color.BLACK);
        button.setBorder(BorderFactory.createRaisedBevelBorder());
        button.setFocusPainted(false);
        
        // Add hover effect
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(new Color(100, 150, 200));
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(new Color(70, 130, 180));
            }
        });
        
        return button;
    }
    
    private void showNewGameDialog() {
        JDialog dialog = new JDialog(this, "Create New World", true);
        dialog.setSize(400, 300);
        dialog.setLocationRelativeTo(this);
        
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(new Color(45, 45, 45));
        GridBagConstraints gbc = new GridBagConstraints();
        
        // World name input
        gbc.gridx = 0; gbc.gridy = 0;
        gbc.insets = new Insets(10, 10, 5, 10);
        JLabel nameLabel = new JLabel("World Name:");
        nameLabel.setForeground(Color.WHITE);
        panel.add(nameLabel, gbc);
        
        gbc.gridy = 1;
        JTextField nameField = new JTextField("New World", 20);
        panel.add(nameField, gbc);
        
        // Seed input
        gbc.gridy = 2;
        gbc.insets = new Insets(15, 10, 5, 10);
        JLabel seedLabel = new JLabel("Seed (optional):");
        seedLabel.setForeground(Color.WHITE);
        panel.add(seedLabel, gbc);
        
        gbc.gridy = 3;
        gbc.insets = new Insets(5, 10, 10, 10);
        JTextField seedField = new JTextField(20);
        panel.add(seedField, gbc);
        
        // Buttons
        gbc.gridy = 4;
        gbc.insets = new Insets(20, 10, 10, 10);
        JPanel buttonPanel = new JPanel();
        buttonPanel.setBackground(new Color(45, 45, 45));
        
        JButton createButton = new JButton("Create");
        JButton cancelButton = new JButton("Cancel");
        
        createButton.addActionListener(e -> {
            String worldName = nameField.getText().trim();
            if (worldName.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Please enter a world name.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            // Check if a world with this name already exists
            if (saveManager.worldExists(worldName)) {
                int result = JOptionPane.showConfirmDialog(dialog, 
                    "A world with the name '" + worldName + "' already exists.\nDo you want to overwrite it?", 
                    "World Already Exists", 
                    JOptionPane.YES_NO_OPTION, 
                    JOptionPane.WARNING_MESSAGE);
                if (result != JOptionPane.YES_OPTION) {
                    return; // User chose not to overwrite
                }
            }
            
            long seed;
            String seedText = seedField.getText().trim();
            if (seedText.isEmpty()) {
                seed = System.currentTimeMillis();
            } else {
                try {
                    seed = Long.parseLong(seedText);
                } catch (NumberFormatException ex) {
                    seed = seedText.hashCode();
                }
            }
            
            dialog.dispose();
            startNewGame(worldName, seed);
        });
        
        cancelButton.addActionListener(e -> dialog.dispose());
        
        buttonPanel.add(createButton);
        buttonPanel.add(cancelButton);
        panel.add(buttonPanel, gbc);
        
        dialog.add(panel);
        dialog.setVisible(true);
    }
    
    private void showLoadGameDialog() {
        List<SavedWorldInfo> savedWorlds = saveManager.getSavedWorlds();
        
        if (savedWorlds.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No saved worlds found.", "Load Game", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        
        JDialog dialog = new JDialog(this, "Load World", true);
        dialog.setSize(500, 400);
        dialog.setLocationRelativeTo(this);
        
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(45, 45, 45));
        
        // Create list model
        DefaultListModel<SavedWorldInfo> listModel = new DefaultListModel<>();
        for (SavedWorldInfo world : savedWorlds) {
            listModel.addElement(world);
        }
        
        JList<SavedWorldInfo> worldList = new JList<>(listModel);
        worldList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        worldList.setBackground(new Color(60, 60, 60));
        worldList.setForeground(Color.WHITE);
        
        // Custom cell renderer
        worldList.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof SavedWorldInfo) {
                    SavedWorldInfo world = (SavedWorldInfo) value;
                    setText("<html><b>" + world.getName() + "</b><br>" +
                           "Seed: " + world.getSeed() + "<br>" +
                           "Created: " + world.getCreationDate() + "</html>");
                }
                if (isSelected) {
                    setBackground(new Color(70, 130, 180));
                } else {
                    setBackground(new Color(60, 60, 60));
                }
                setForeground(Color.WHITE);
                return this;
            }
        });
        
        JScrollPane scrollPane = new JScrollPane(worldList);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        // Button panel
        JPanel buttonPanel = new JPanel();
        buttonPanel.setBackground(new Color(45, 45, 45));
        
        JButton loadButton = new JButton("Load");
        JButton deleteButton = new JButton("Delete");
        JButton cancelButton = new JButton("Cancel");
        
        loadButton.addActionListener(e -> {
            SavedWorldInfo selected = worldList.getSelectedValue();
            if (selected != null) {
                dialog.dispose();
                loadGame(selected);
            } else {
                JOptionPane.showMessageDialog(dialog, "Please select a world to load.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        
        deleteButton.addActionListener(e -> {
            SavedWorldInfo selected = worldList.getSelectedValue();
            if (selected != null) {
                int result = JOptionPane.showConfirmDialog(dialog, 
                    "Are you sure you want to delete \"" + selected.getName() + "\"?", 
                    "Delete World", JOptionPane.YES_NO_OPTION);
                if (result == JOptionPane.YES_OPTION) {
                    saveManager.deleteWorld(selected.getName());
                    listModel.removeElement(selected);
                }
            }
        });
        
        cancelButton.addActionListener(e -> dialog.dispose());
        
        buttonPanel.add(loadButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(cancelButton);
        panel.add(buttonPanel, BorderLayout.SOUTH);
        
        dialog.add(panel);
        dialog.setVisible(true);
    }
    
    private void startNewGame(String worldName, long seed) {
        this.setVisible(false);
        // Use SwingUtilities.invokeLater to ensure proper EDT handling
        SwingUtilities.invokeLater(() -> {
            game = new Game(worldName, seed, this);
            game.init();
            game.start();
        });
    }
    
    private void loadGame(SavedWorldInfo worldInfo) {
        this.setVisible(false);
        // Use SwingUtilities.invokeLater to ensure proper EDT handling
        SwingUtilities.invokeLater(() -> {
            game = new Game(worldInfo, this);
            game.init();
            game.start();
        });
    }
    
    public void showMenu() {
        this.setVisible(true);
    }
    
    public static void main(String[] args) {
        registerContent();
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e) {
                e.printStackTrace();
            }
            
            new MainMenu().setVisible(true);
        });
    }

    public static void registerContent(){
        ItemRegistry.registerDefaultItems();
    }


}