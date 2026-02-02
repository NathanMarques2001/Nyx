package com.editor_texto.nyx.ui.layout;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Optional;

import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.LineNumberFactory;

import javafx.collections.ObservableList;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import org.fxmisc.flowless.VirtualizedScrollPane;

public class EditorPane {
    private TabPane tabPane;

    public EditorPane() {
        tabPane = new TabPane();
    }

    public TabPane getTabPane() {
        return tabPane;
    }

    /**
     * Opens a file in a new tab, or selects it if already open.
     */
    public void openFile(File file) {
        // Check if already open
        for (Tab tab : tabPane.getTabs()) {
            EditorTab editorTab = (EditorTab) tab;
            if (editorTab.getFile() != null && editorTab.getFile().getAbsolutePath().equals(file.getAbsolutePath())) {
                tabPane.getSelectionModel().select(tab);
                return;
            }
        }

        try {
            String content;
            try {
                content = Files.readString(file.toPath());
            } catch (java.nio.charset.MalformedInputException ex) {
                // Fallback to ISO-8859-1 if UTF-8 fails
                content = Files.readString(file.toPath(), java.nio.charset.StandardCharsets.ISO_8859_1);
            }

            EditorTab newTab = new EditorTab(file, content);
            tabPane.getTabs().add(newTab);
            tabPane.getSelectionModel().select(newTab);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Saves the currently selected tab.
     */
    public void saveCurrentTab() {
        Tab selectedTab = tabPane.getSelectionModel().getSelectedItem();
        if (selectedTab instanceof EditorTab) {
            ((EditorTab) selectedTab).save();
        }
    }

    public boolean hasUnsavedChanges() {
        for (Tab tab : tabPane.getTabs()) {
            if (((EditorTab) tab).isDirty()) {
                return true;
            }
        }
        return false;
    }

    public void cut() {
        Tab selectedTab = tabPane.getSelectionModel().getSelectedItem();
        if (selectedTab instanceof EditorTab) {
            ((EditorTab) selectedTab).codeArea.cut();
        }
    }

    public void copy() {
        Tab selectedTab = tabPane.getSelectionModel().getSelectedItem();
        if (selectedTab instanceof EditorTab) {
            ((EditorTab) selectedTab).codeArea.copy();
        }
    }

    public void paste() {
        Tab selectedTab = tabPane.getSelectionModel().getSelectedItem();
        if (selectedTab instanceof EditorTab) {
            ((EditorTab) selectedTab).codeArea.paste();
        }
    }

    /**
     * Tries to close all tabs. Returns true if successful (all closed or user
     * allowed exit),
     * false if cancelled.
     */
    public boolean confirmClose() {
        ObservableList<Tab> tabs = tabPane.getTabs();
        // Create a copy to avoid concurrent modification loops if we close them one by
        // one
        // But for app exit, we just need to check all dirty ones.

        for (Tab tab : tabs) {
            EditorTab editorTab = (EditorTab) tab;
            if (editorTab.isDirty()) {
                tabPane.getSelectionModel().select(tab);
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("Unsaved Changes");
                alert.setHeaderText("Unsaved changes in " + editorTab.getText());
                alert.setContentText("Do you want to save your changes?");

                ButtonType buttonTypeSave = new ButtonType("Save");
                ButtonType buttonTypeDontSave = new ButtonType("Don't Save");
                ButtonType buttonTypeCancel = new ButtonType("Cancel",
                        javafx.scene.control.ButtonBar.ButtonData.CANCEL_CLOSE);

                alert.getButtonTypes().setAll(buttonTypeSave, buttonTypeDontSave, buttonTypeCancel);

                Optional<ButtonType> result = alert.showAndWait();
                if (result.get() == buttonTypeSave) {
                    editorTab.save();
                } else if (result.get() == buttonTypeCancel) {
                    return false; // Stop closing
                }
            }
        }
        return true;
    }

    // Inner class for specialized Tab
    private class EditorTab extends Tab {
        private File file;
        private CodeArea codeArea;
        private boolean isDirty = false;

        public EditorTab(File file, String content) {
            this.file = file;
            this.setText(file != null ? file.getName() : "Untitled");

            codeArea = new CodeArea();
            codeArea.setParagraphGraphicFactory(LineNumberFactory.get(codeArea));

            // Apply highlighting if .lc file
            if (file != null && file.getName().toLowerCase().endsWith(".lc")) {
                codeArea.textProperty().addListener((obs, oldText, newText) -> {
                    codeArea.setStyleSpans(0, LCSyntax.computeHighlighting(newText));
                });
                // Initial highlighting
                codeArea.replaceText(content);
                codeArea.setStyleSpans(0, LCSyntax.computeHighlighting(content));
            } else {
                codeArea.replaceText(content);
            }

            // Listen for changes
            codeArea.textProperty().addListener((obs, oldVal, newVal) -> {
                if (!isDirty && !newVal.equals(content)) { // simple check, ideally check against saved hash
                    setDirty(true);
                }
            });

            // Use VirtualizedScrollPane for better performance with CodeArea
            this.setContent(new VirtualizedScrollPane<>(codeArea));

            this.setOnCloseRequest(e -> {
                if (isDirty) {
                    Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                    alert.setTitle("Unsaved Changes");
                    alert.setHeaderText("Unsaved changes in " + getText());
                    alert.setContentText("Do you want to save your changes?");

                    ButtonType buttonTypeSave = new ButtonType("Save");
                    ButtonType buttonTypeDontSave = new ButtonType("Don't Save");
                    ButtonType buttonTypeCancel = new ButtonType("Cancel",
                            javafx.scene.control.ButtonBar.ButtonData.CANCEL_CLOSE);

                    alert.getButtonTypes().setAll(buttonTypeSave, buttonTypeDontSave, buttonTypeCancel);

                    Optional<ButtonType> result = alert.showAndWait();
                    if (result.isPresent()) {
                        if (result.get() == buttonTypeSave) {
                            save();
                        } else if (result.get() == buttonTypeCancel) {
                            e.consume(); // Cancel close
                        }
                    }
                }
            });
        }

        public File getFile() {
            return file;
        }

        public boolean isDirty() {
            return isDirty;
        }

        public void setDirty(boolean dirty) {
            this.isDirty = dirty;
            if (dirty) {
                if (!getText().endsWith("*"))
                    setText(getText() + "*");
            } else {
                if (getText().endsWith("*"))
                    setText(getText().substring(0, getText().length() - 1));
            }
        }

        public void save() {
            if (file != null) {
                try {
                    Files.writeString(file.toPath(), codeArea.getText());
                    setDirty(false);
                    // Update initial content reference if we want strict dirty checking later
                } catch (IOException e) {
                    e.printStackTrace();
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Error");
                    alert.setHeaderText("Could not save file");
                    alert.setContentText(e.getMessage());
                    alert.showAndWait();
                }
            }
        }
    }
}
