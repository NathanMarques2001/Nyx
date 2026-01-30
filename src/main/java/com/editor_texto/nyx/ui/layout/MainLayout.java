package com.editor_texto.nyx.ui.layout;

import javafx.scene.layout.BorderPane;
import com.editor_texto.nyx.ui.components.MenuBarTop;
import java.io.File;
import javafx.scene.control.TreeItem;

public class MainLayout {

    private BorderPane borderPane;
    private EditorPane editorPane;

    public MainLayout() {
        // Inicializa o layout
        borderPane = new BorderPane();
        // Adiciona a barra de menu
        MenuBarTop menuBarTop = new MenuBarTop();
        borderPane.setTop(menuBarTop.getMenuBar());

        // Adiciona o painel de arquivos
        FileTreePane fileTreePane = new FileTreePane();
        borderPane.setLeft(fileTreePane.getTree());

        // Adiciona o editor
        editorPane = new EditorPane();
        borderPane.setCenter(editorPane.getTabPane());

        // --- LOGICA DE EVENTOS ---

        // Listener para abrir arquivos via FileTree
        fileTreePane.getTree().getSelectionModel().selectedItemProperty()
                .addListener((observable, oldValue, newValue) -> {
                    if (newValue != null && newValue.getValue().isFile()) {
                        editorPane.openFile(newValue.getValue());
                    }
                });

        // Menu Arquivo > Salvar
        // Adicionando acelerador Ctrl+S
        menuBarTop.getSaveFileItem().setAccelerator(javafx.scene.input.KeyCombination.keyCombination("Ctrl+S"));
        menuBarTop.getSaveFileItem().setOnAction(e -> {
            editorPane.saveCurrentTab();
        });

        // Menu Arquivo > Novo Arquivo
        menuBarTop.getNewFileItem().setOnAction(e -> {
            javafx.scene.control.TextInputDialog dialog = new javafx.scene.control.TextInputDialog("novo_arquivo.txt");
            dialog.setTitle("Novo Arquivo");
            dialog.setHeaderText("Criar um novo arquivo");
            dialog.setContentText("Nome do arquivo:");

            java.util.Optional<String> result = dialog.showAndWait();
            result.ifPresent(name -> {
                // Get current root or fallback to user.dir
                // Currently FileTreePane tracks root but doesn't expose it well, accessing
                // private valid root logic implies using what we set or lastOpened.
                // Let's assume root is accessible via preference or standard fallback.
                // A better design would be exposing getCurrentRoot from FileTreePane.
                // For now, let's look at where the tree is pointed.
                // Or easier: prompt user where to save IF no folder open?
                // Actually, let's create in the 'active' folder (root of tree).

                File root = new File(System.getProperty("user.dir")); // Default
                TreeItem<File> rootItem = fileTreePane.getTree().getRoot();
                if (rootItem != null && rootItem.getValue() != null) {
                    root = rootItem.getValue();
                }

                File newFile = new File(root, name);
                try {
                    if (newFile.createNewFile()) {
                        // File created
                        // Reload tree strictly? Timer might pick it up, but let's force it for UX
                        // responsiveness
                        fileTreePane.reloadTree(root);
                        editorPane.openFile(newFile);
                    } else {
                        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                                javafx.scene.control.Alert.AlertType.WARNING);
                        alert.setTitle("Erro");
                        alert.setHeaderText("Arquivo ja existe");
                        alert.showAndWait();
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            });
        });

        // Menu Arquivo > Abrir Arquivo
        menuBarTop.getOpenFileItem().setOnAction(e -> {
            javafx.stage.FileChooser fileChooser = new javafx.stage.FileChooser();
            fileChooser.setTitle("Abrir Arquivo");
            java.io.File file = fileChooser.showOpenDialog(borderPane.getScene().getWindow());
            if (file != null) {
                editorPane.openFile(file);
            }
        });

        // Menu Arquivo > Abrir Pasta
        menuBarTop.getOpenFolderItem().setOnAction(e -> {
            javafx.stage.DirectoryChooser directoryChooser = new javafx.stage.DirectoryChooser();
            directoryChooser.setTitle("Abrir Pasta");
            java.io.File directory = directoryChooser.showDialog(borderPane.getScene().getWindow());
            if (directory != null) {
                fileTreePane.reloadTree(directory);
                // Salvar preferencia
                java.util.prefs.Preferences prefs = java.util.prefs.Preferences.userNodeForPackage(MainLayout.class);
                prefs.put("lastOpenedFolder", directory.getAbsolutePath());
            }
        });

        // Carregar a ultima pasta aberta
        java.util.prefs.Preferences prefs = java.util.prefs.Preferences.userNodeForPackage(MainLayout.class);
        String lastOpenedFolder = prefs.get("lastOpenedFolder", null);
        if (lastOpenedFolder != null) {
            java.io.File folder = new java.io.File(lastOpenedFolder);
            if (folder.exists() && folder.isDirectory()) {
                fileTreePane.reloadTree(folder);
            }
        }

        // Menu Arquivo > Sair
        menuBarTop.getExitItem().setOnAction(e -> {
            // Check for unsaved changes before exiting
            if (editorPane.confirmClose()) {
                javafx.application.Platform.exit();
            }
        });

        // Menu Editar
        menuBarTop.getCutItem().setOnAction(e -> editorPane.cut());
        menuBarTop.getCopyItem().setOnAction(e -> editorPane.copy());
        menuBarTop.getPasteItem().setOnAction(e -> editorPane.paste());

        // Menu Ajuda > Sobre
        menuBarTop.getAboutItem().setOnAction(e -> {
            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                    javafx.scene.control.Alert.AlertType.INFORMATION);
            alert.setTitle("Sobre o Nyx");
            alert.setHeaderText("Nyx Editor");
            alert.setContentText("Um editor de texto simples feito em JavaFX.");
            alert.showAndWait();
        });
        // Adiciona o console
        borderPane.setBottom(new ConsolePane().getConsole());
    }

    // Retorna o layout
    public BorderPane getBorderPane() {
        return borderPane;
    }

    public EditorPane getEditorPane() {
        return editorPane;
    }
}
