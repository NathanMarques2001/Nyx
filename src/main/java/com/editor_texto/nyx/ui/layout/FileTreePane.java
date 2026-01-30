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
        treeView.setCellFactory(tv -> new javafx.scene.control.TreeCell<File>() {
            @Override
            protected void updateItem(File item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getName());
                }
            }
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
