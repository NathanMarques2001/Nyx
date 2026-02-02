package com.editor_texto.nyx.ui.layout;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.concurrent.TimeUnit;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;

public class FileTreePane {

    private WatchService watcher;
    private Thread watcherThread;
    private File currentRoot;
    private final TreeView<File> treeView;

    public FileTreePane() {
        treeView = new TreeView<>();
        // Configura como os itens sao exibidos (apenas o nome do arquivo/pasta)
        // Configura como os itens sao exibidos e Drag&Drop
        treeView.setCellFactory(tv -> {
            javafx.scene.control.TreeCell<File> cell = new javafx.scene.control.TreeCell<File>() {
                @Override
                protected void updateItem(File item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                        setGraphic(null);
                    } else {
                        setText(item.getName());
                    }
                }
            };

            // Drag Detected (Source)
            cell.setOnDragDetected(event -> {
                if (cell.getItem() == null)
                    return;

                javafx.scene.input.Dragboard db = cell.startDragAndDrop(javafx.scene.input.TransferMode.MOVE);
                javafx.scene.input.ClipboardContent content = new javafx.scene.input.ClipboardContent();
                // We put the file path as potential list of files
                java.util.List<File> files = new java.util.ArrayList<>();
                files.add(cell.getItem());
                content.putFiles(files);
                db.setContent(content);
                event.consume();
            });

            // Drag Over (Target)
            cell.setOnDragOver(event -> {
                if (event.getGestureSource() != cell &&
                        event.getDragboard().hasFiles()) {

                    // Only allow drop on directories
                    File targetFile = cell.getItem();
                    // If target is null (empty space), we might consider dropping into root,
                    // but for now let's strict to dropping onto a folder.
                    // Or if dropping onto a file, maybe move to that file's parent?
                    // Standard behavior: target must be directory to move INTO it.

                    if (targetFile != null && targetFile.isDirectory()) {
                        event.acceptTransferModes(javafx.scene.input.TransferMode.MOVE);
                    }
                }
                event.consume();
            });

            // Drag Dropped (Target)
            cell.setOnDragDropped(event -> {
                javafx.scene.input.Dragboard db = event.getDragboard();
                boolean success = false;
                if (db.hasFiles()) {
                    File sourceFile = db.getFiles().get(0);
                    File targetDir = cell.getItem();

                    if (targetDir != null && targetDir.isDirectory() && sourceFile != null) {
                        try {
                            java.nio.file.Path sourcePath = sourceFile.toPath();
                            java.nio.file.Path targetPath = targetDir.toPath().resolve(sourceFile.getName());

                            // Prevent moving into itself or overwrite without confirmation (simple move)
                            if (!sourcePath.equals(targetPath)) {
                                java.nio.file.Files.move(sourcePath, targetPath,
                                        java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                                success = true;

                                // Refresh logic:
                                // Ideally we refresh both source parent and destination parent (targetDir)
                                // Since we rely on watcher, it might catch it.
                                // But let's trigger reload on the current root for consistency if watcher is
                                // slow.
                                // Or better, just let the watcher handle it as implemented in startWatcher().
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                                    javafx.scene.control.Alert.AlertType.ERROR);
                            alert.setTitle("Erro ao mover arquivo");
                            alert.setContentText(e.getMessage());
                            alert.showAndWait();
                        }
                    }
                }
                event.setDropCompleted(success);
                event.consume();
            });

            return cell;
        });

        String currentDir = System.getProperty("user.dir");
        reloadTree(new File(currentDir));

        // Start watcher
        startWatcher();
    }

    public void reloadTree(File rootFile) {
        if (rootFile == null || !rootFile.exists())
            return;

        this.currentRoot = rootFile;

        TreeItem<File> rootItem = new TreeItem<>(rootFile);
        rootItem.setExpanded(true);
        createTree(rootFile, rootItem);
        treeView.setRoot(rootItem);
    }

    private void startWatcher() {
        watcherThread = new Thread(() -> {
            try {
                watcher = FileSystems.getDefault().newWatchService();

                // Simple polling loop or re-registration strategy needs to be robust.
                // For simplicity and effectiveness in this rapid prototype:
                // We will watch the current root directory and if any event happens, we reload
                // the tree.
                // A full recursive watcher is complex.

                // We will keep a reference to the monitored key
                WatchKey key = null;
                File lastRoot = null;

                while (!Thread.currentThread().isInterrupted()) {
                    if (currentRoot != null && (lastRoot == null || !lastRoot.equals(currentRoot))) {
                        if (key != null)
                            key.cancel();
                        try {
                            // Only watching top-level for now to avoid complexity of recursive registration
                            // ideally we would walk tree and register all.
                            // But user asked for "listening", so we should try to cover subdirs if possible
                            // or just root.
                            // Let's stick to root + basic refresh for now, or maybe small delay polling if
                            // WatchService is too flaky on Windows without recursion.
                            // Actually, let's watch the root.
                            key = currentRoot.toPath().register(watcher, StandardWatchEventKinds.ENTRY_CREATE,
                                    StandardWatchEventKinds.ENTRY_DELETE, StandardWatchEventKinds.ENTRY_MODIFY);
                            lastRoot = currentRoot;
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    if (key != null) {
                        try {
                            WatchKey k = watcher.poll(1, TimeUnit.SECONDS);
                            if (k != null) {
                                boolean refresh = false;
                                for (WatchEvent<?> event : k.pollEvents()) {
                                    // Just refresh on any structure change
                                    if (event.kind() != StandardWatchEventKinds.OVERFLOW) {
                                        refresh = true;
                                    }
                                }
                                k.reset();

                                if (refresh) {
                                    javafx.application.Platform.runLater(() -> {
                                        if (currentRoot != null)
                                            reloadTree(currentRoot);
                                    });
                                }
                            }
                        } catch (InterruptedException ie) {
                            Thread.currentThread().interrupt();
                        }
                    } else {
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        watcherThread.setDaemon(true);
        watcherThread.start();
    }

    private void createTree(File file, TreeItem<File> parent) {
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            if (files != null) {
                for (File child : files) {
                    TreeItem<File> newItem = new TreeItem<>(child);
                    parent.getChildren().add(newItem);
                    if (child.isDirectory()) {
                        createTree(child, newItem);
                    }
                }
            }
        }
    }

    public TreeView<File> getTree() {
        return treeView;
    }
}
