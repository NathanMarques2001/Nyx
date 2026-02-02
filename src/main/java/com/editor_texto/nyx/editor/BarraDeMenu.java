package com.editor_texto.nyx.editor;

import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.RadioMenuItem;
import javafx.scene.control.ToggleGroup;

/**
 * Gerencia a barra de menu superior do editor (Arquivo, Editar, Visualizar,
 * etc).
 */
public class BarraDeMenu {
    private final MenuBar barraMenu;

    // Itens de Menu
    private MenuItem novoArquivo;
    private MenuItem abrirArquivo;
    private MenuItem abrirPasta;
    private MenuItem salvarArquivo;
    private MenuItem excluirArquivo;
    private MenuItem sair;
    private MenuItem recortar;
    private MenuItem copiar;
    private MenuItem colar;

    // Tema
    private Menu menuTema;
    private RadioMenuItem temaClaro;
    private RadioMenuItem temaEscuro;
    private ToggleGroup grupoTema;

    private MenuItem executar;
    private MenuItem sobre;

    public BarraDeMenu() {
        barraMenu = new MenuBar();
        barraMenu.getMenus().addAll(criarMenuArquivo(), criarMenuEditar(), criarMenuVisualizar(), criarMenuExecutar(),
                criarMenuAjuda());
    }

    public MenuBar obterBarraMenu() {
        return barraMenu;
    }

    private Menu criarMenuArquivo() {
        Menu menu = new Menu("Arquivo");
        novoArquivo = new MenuItem("Novo Arquivo");
        abrirArquivo = new MenuItem("Abrir arquivo");
        abrirPasta = new MenuItem("Abrir pasta");
        salvarArquivo = new MenuItem("Salvar");
        excluirArquivo = new MenuItem("Excluir");
        sair = new MenuItem("Sair");
        menu.getItems().addAll(novoArquivo, abrirArquivo, abrirPasta, salvarArquivo, excluirArquivo, sair);
        return menu;
    }

    private Menu criarMenuEditar() {
        Menu menu = new Menu("Editar");
        recortar = new MenuItem("Recortar");
        copiar = new MenuItem("Copiar");
        colar = new MenuItem("Colar");
        menu.getItems().addAll(recortar, copiar, colar);
        return menu;
    }

    private Menu criarMenuVisualizar() {
        Menu menu = new Menu("Visualizar");

        menuTema = new Menu("Tema");
        grupoTema = new ToggleGroup();

        temaClaro = new RadioMenuItem("Light");
        temaClaro.setToggleGroup(grupoTema);

        temaEscuro = new RadioMenuItem("Dark");
        temaEscuro.setToggleGroup(grupoTema);

        menuTema.getItems().addAll(temaClaro, temaEscuro);

        menu.getItems().add(menuTema);
        return menu;
    }

    private Menu criarMenuExecutar() {
        Menu menu = new Menu("Executar");
        executar = new MenuItem("Executar");
        menu.getItems().add(executar);
        return menu;
    }

    private Menu criarMenuAjuda() {
        Menu menu = new Menu("Ajuda");
        sobre = new MenuItem("Sobre");
        menu.getItems().add(sobre);
        return menu;
    }

    // Getters
    public MenuItem obterItemNovoArquivo() {
        return novoArquivo;
    }

    public MenuItem obterItemAbrirArquivo() {
        return abrirArquivo;
    }

    public MenuItem obterItemAbrirPasta() {
        return abrirPasta;
    }

    public MenuItem obterItemSalvarArquivo() {
        return salvarArquivo;
    }

    public MenuItem obterItemExcluirArquivo() {
        return excluirArquivo;
    }

    public MenuItem obterItemSair() {
        return sair;
    }

    public MenuItem obterItemRecortar() {
        return recortar;
    }

    public MenuItem obterItemCopiar() {
        return copiar;
    }

    public MenuItem obterItemColar() {
        return colar;
    }

    public RadioMenuItem obterItemTemaClaro() {
        return temaClaro;
    }

    public RadioMenuItem obterItemTemaEscuro() {
        return temaEscuro;
    }

    public MenuItem obterItemExecutar() {
        return executar;
    }

    public MenuItem obterItemSobre() {
        return sobre;
    }
}
