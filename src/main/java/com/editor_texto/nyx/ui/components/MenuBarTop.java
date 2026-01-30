package com.editor_texto.nyx.ui.components;

import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;

public class MenuBarTop {
    private MenuBar menuBar;

    // Menu Items references
    private MenuItem newFile;
    private MenuItem openFile;
    private MenuItem openFolder;
    private MenuItem saveFile;
    private MenuItem deleteFile;
    private MenuItem exit;
    private MenuItem cut;
    private MenuItem copy;
    private MenuItem paste;
    private MenuItem theme;
    private MenuItem run;
    private MenuItem about;

    public MenuBarTop() {
        menuBar = new MenuBar();
        menuBar.getMenus().addAll(fileMenu(), editMenu(), viewMenu(), executarMenu(), helpMenu());
    }

    public MenuBar getMenuBar() {
        return menuBar;
    }

    private final Menu fileMenu() {
        Menu fileMenu = new Menu("Arquivo");
        newFile = new MenuItem("Novo Arquivo");
        openFile = new MenuItem("Abrir arquivo");
        openFolder = new MenuItem("Abrir pasta");
        saveFile = new MenuItem("Salvar");
        deleteFile = new MenuItem("Excluir");
        exit = new MenuItem("Sair");
        fileMenu.getItems().addAll(newFile, openFile, openFolder, saveFile, deleteFile, exit);
        return fileMenu;
    }

    private final Menu editMenu() {
        Menu editMenu = new Menu("Editar");
        cut = new MenuItem("Recortar");
        copy = new MenuItem("Copiar");
        paste = new MenuItem("Colar");
        editMenu.getItems().addAll(cut, copy, paste);
        return editMenu;
    }

    private final Menu viewMenu() {
        Menu viewMenu = new Menu("Visualizar");
        theme = new MenuItem("Tema");
        viewMenu.getItems().add(theme);
        return viewMenu;
    }

    private final Menu executarMenu() {
        Menu executarMenu = new Menu("Executar");
        run = new MenuItem("Executar");
        executarMenu.getItems().add(run);
        return executarMenu;
    }

    private final Menu helpMenu() {
        Menu helpMenu = new Menu("Ajuda");
        about = new MenuItem("Sobre");
        helpMenu.getItems().add(about);
        return helpMenu;
    }

    // Getters for items
    public MenuItem getNewFileItem() {
        return newFile;
    }

    public MenuItem getOpenFileItem() {
        return openFile;
    }

    public MenuItem getOpenFolderItem() {
        return openFolder;
    }

    public MenuItem getSaveFileItem() {
        return saveFile;
    }

    public MenuItem getDeleteItem() {
        return deleteFile;
    }

    public MenuItem getExitItem() {
        return exit;
    }

    public MenuItem getCutItem() {
        return cut;
    }

    public MenuItem getCopyItem() {
        return copy;
    }

    public MenuItem getPasteItem() {
        return paste;
    }

    public MenuItem getThemeItem() {
        return theme;
    }

    public MenuItem getRunItem() {
        return run;
    }

    public MenuItem getAboutItem() {
        return about;
    }
}
