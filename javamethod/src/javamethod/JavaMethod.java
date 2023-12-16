package javamethod;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

public class JavaMethod {

    private final JFrame frame;
    private final JTextArea outputTextArea;
    private final JCheckBox includeSubdirectoriesCheckBox;
    private String overallLongestWord = "";
    private String overallShortestWord = "";

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new JavaMethod().createAndShowGUI());
    }

    public JavaMethod() {
        frame = new JFrame("Word Statistics App");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        outputTextArea = new JTextArea(20, 40);
        outputTextArea.setEditable(false);

        includeSubdirectoriesCheckBox = new JCheckBox("Include Subdirectories", true);

        JButton analyzeButton = new JButton("Analyze Directory");
        analyzeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                analyzeDirectory();
            }
        });

        JPanel panel = new JPanel();
        panel.add(includeSubdirectoriesCheckBox);
        panel.add(analyzeButton);

        frame.getContentPane().setLayout(new BorderLayout());
        frame.getContentPane().add(panel, BorderLayout.NORTH);
        frame.getContentPane().add(new JScrollPane(outputTextArea), BorderLayout.CENTER);

        frame.pack();
        frame.setLocationRelativeTo(null);
    }

    private void analyzeDirectory() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

        int result = fileChooser.showOpenDialog(frame);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedDirectory = fileChooser.getSelectedFile();
            boolean includeSubdirectories = includeSubdirectoriesCheckBox.isSelected();
            Map<String, WordStats> wordStatsMap = processDirectory(selectedDirectory, includeSubdirectories);
            displayWordStatistics(wordStatsMap);
        } else {
            outputTextArea.append("Invalid directory or directory not selected.\n");
        }
    }

    private Map<String, WordStats> processDirectory(File directory, boolean includeSubdirectories) {
        Map<String, WordStats> wordStatsMap = new HashMap<>();

        try {
            Files.walk(directory.toPath())
                    .filter(path -> {
                        if (includeSubdirectories) {
                            return path.toFile().isFile() && path.toString().endsWith(".txt");
                        } else {
                            return path.toFile().isFile() && path.getParent().equals(directory.toPath());
                        }
                    })
                    .forEach(path -> {
                        String fileName = path.toFile().getName();
                        try {
                            String content = new String(Files.readAllBytes(path));
                            WordStats stats = countWords(content);

                            // Update overall shortest and longest words
                            updateOverallWords(stats);

                            wordStatsMap.put(fileName, stats);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    });
        } catch (IOException e) {
            e.printStackTrace();
        }

        return wordStatsMap;
    }

    private WordStats countWords(String content) {
        String[] words = content.split("\\s+");
        int wordCount = words.length;
        int isCount = countSubstring(content, "is");
        int areCount = countSubstring(content, "are");
        int youCount = countSubstring(content, "you");
        String longestWord = findLongestWord(words);
        String shortestWord = findShortestWord(words);
        return new WordStats(wordCount, isCount, areCount, youCount, longestWord, shortestWord);
    }

    private int countSubstring(String content, String substring) {
        int lastIndex = 0;
        int count = 0;

        while (lastIndex != -1) {
            lastIndex = content.indexOf(substring, lastIndex);

            if (lastIndex != -1) {
                count++;
                lastIndex += substring.length();
            }
        }
        return count;
    }

    private String findLongestWord(String[] words) {
        String longestWord = "";
        for (String word : words) {
            if (word.length() > longestWord.length()) {
                longestWord = word;
            }
        }
        return longestWord;
    }

    private String findShortestWord(String[] words) {
        if (words.length == 0) {
            return "";
        }

        String shortestWord = words[0];
        for (String word : words) {
            if (word.length() < shortestWord.length()) {
                shortestWord = word;
            }
        }
        return shortestWord;
    }

    private void updateOverallWords(WordStats stats) {
        String currentLongest = stats.getLongestWord();
        String currentShortest = stats.getShortestWord();

        if (currentLongest.length() > overallLongestWord.length()) {
            overallLongestWord = currentLongest;
        }

        if (overallShortestWord.isEmpty() || currentShortest.length() < overallShortestWord.length()) {
            overallShortestWord = currentShortest;
        }
    }

    private void displayWordStatistics(Map<String, WordStats> wordStatsMap) {
        outputTextArea.setText("Word Statistics:\n");

        for (Map.Entry<String, WordStats> entry : wordStatsMap.entrySet()) {
            String fileName = entry.getKey();
            WordStats stats = entry.getValue();
            outputTextArea.append(fileName + "\n");
            outputTextArea.append("Word count: " + stats.getWordCount() + "\n");
            outputTextArea.append("Is count: " + stats.getIsCount() + "\n");
            outputTextArea.append("Are count: " + stats.getAreCount() + "\n");
            outputTextArea.append("You count: " + stats.getYouCount() + "\n");
            outputTextArea.append("Longest word: " + stats.getLongestWord() + "\n");
            outputTextArea.append("Shortest word: " + stats.getShortestWord() + "\n\n");
        }

        // Display overall shortest and longest words
        outputTextArea.append("\nOverall Statistics:\n");
        outputTextArea.append("Overall Longest Word: " + overallLongestWord + "\n");
        outputTextArea.append("Overall Shortest Word: " + overallShortestWord + "\n");
    }

    private static class WordStats {
        private final int wordCount;
        private final int isCount;
        private final int areCount;
        private final int youCount;
        private final String longestWord;
        private final String shortestWord;

        public WordStats(int wordCount, int isCount, int areCount, int youCount, String longestWord, String shortestWord) {
            this.wordCount = wordCount;
            this.isCount = isCount;
            this.areCount = areCount;
            this.youCount = youCount;
            this.longestWord = longestWord;
            this.shortestWord = shortestWord;
        }

        public int getWordCount() {
            return wordCount;
        }

        public int getIsCount() {
            return isCount;
        }

        public int getAreCount() {
            return areCount;
        }

        public int getYouCount() {
            return youCount;
        }

        public String getLongestWord() {
            return longestWord;
        }

        public String getShortestWord() {
            return shortestWord;
        }
    }

    private void createAndShowGUI() {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e) {
                e.printStackTrace();
            }

            frame.setVisible(true);
        });
    }
}