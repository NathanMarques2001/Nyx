package com.editor_texto.nyx.ui;

import javafx.scene.layout.BorderPane;
import javafx.scene.control.TreeItem;
import javafx.stage.FileChooser;
import javafx.stage.DirectoryChooser;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.application.Platform;

import java.io.File;
import java.util.Optional;
import java.util.prefs.Preferences;

import com.editor_texto.nyx.pipeline.PipelineCompilacao;

/**
 * Layout principal da aplicação, orquestrando a disposição dos painéis e o
 * tratamento de eventos globais.
 */
public class LayoutPrincipal {

    private BorderPane painelPrincipal;
    private PainelEditor painelEditor;
    private PipelineCompilacao pipelineCompilacao;

    public LayoutPrincipal() {
        // Inicializa o layout
        painelPrincipal = new BorderPane();

        // Adiciona a barra de menu
        BarraDeMenu barraDeMenu = new BarraDeMenu();
        painelPrincipal.setTop(barraDeMenu.obterBarraMenu());

        // Adiciona o painel de arquivos
        PainelArvoreArquivos painelArvoreArquivos = new PainelArvoreArquivos();
        painelPrincipal.setLeft(painelArvoreArquivos.obterArvore());

        // Adiciona o editor
        painelEditor = new PainelEditor();
        painelPrincipal.setCenter(painelEditor.obterPainelDeAbas());

        // Pipeline Compilação
        pipelineCompilacao = new PipelineCompilacao();

        // Adiciona a area inferior (Console + Erros)
        javafx.scene.control.TabPane painelInferior = new javafx.scene.control.TabPane();

        // Tab Console
        PainelConsole painelConsole = new PainelConsole();
        javafx.scene.control.Tab tabConsole = new javafx.scene.control.Tab("Console", painelConsole.obterConsole());
        tabConsole.setClosable(false);

        // Tab Erros
        PainelErros painelErros = new PainelErros();
        javafx.scene.control.Tab tabErros = new javafx.scene.control.Tab("Erros", painelErros.obterPainel());
        tabErros.setClosable(false);

        painelInferior.getTabs().addAll(tabConsole, tabErros);
        painelInferior.setPrefHeight(200);

        // Container inferior (Pipeline + Tabs)
        javafx.scene.layout.VBox containerInferior = new javafx.scene.layout.VBox();
        containerInferior.getChildren().addAll(pipelineCompilacao, painelInferior);

        painelPrincipal.setBottom(containerInferior);

        // --- LÓGICA DE EVENTOS ---

        // Configura navegação do painel de erros
        painelErros.definirAcaoNavegacao(erro -> {
            painelEditor.navegarParaErro(erro.getLinha(), erro.getColuna());
        });

        // Ouvinte para abrir arquivos via Árvore
        painelArvoreArquivos.obterArvore().getSelectionModel().selectedItemProperty()
                .addListener((observable, valorVelho, valorNovo) -> {
                    if (valorNovo != null && valorNovo.getValue().isFile()) {
                        painelEditor.abrirArquivo(valorNovo.getValue());
                    }
                });

        // Menu Arquivo > Salvar (Ctrl+S)
        barraDeMenu.obterItemSalvarArquivo().setAccelerator(javafx.scene.input.KeyCombination.keyCombination("Ctrl+S"));
        barraDeMenu.obterItemSalvarArquivo().setOnAction(e -> {
            painelEditor.salvarAbaAtual();
        });

        // Menu Arquivo > Salvar Como
        barraDeMenu.obterItemSalvarComoArquivo()
                .setAccelerator(javafx.scene.input.KeyCombination.keyCombination("Ctrl+Shift+S"));
        barraDeMenu.obterItemSalvarComoArquivo().setOnAction(e -> {
            painelEditor.salvarAbaComo();
        });

        // Menu Arquivo > Novo Arquivo
        barraDeMenu.obterItemNovoArquivo().setAccelerator(javafx.scene.input.KeyCombination.keyCombination("Ctrl+N"));
        barraDeMenu.obterItemNovoArquivo().setOnAction(e -> {
            painelEditor.criarNovaAba();
        });

        // Menu Arquivo > Abrir Arquivo
        barraDeMenu.obterItemAbrirArquivo().setAccelerator(javafx.scene.input.KeyCombination.keyCombination("Ctrl+O"));
        barraDeMenu.obterItemAbrirArquivo().setOnAction(e -> {
            FileChooser seletorArquivo = new FileChooser();
            seletorArquivo.setTitle("Abrir Arquivo");
            File arquivo = seletorArquivo.showOpenDialog(painelPrincipal.getScene().getWindow());
            if (arquivo != null) {
                painelEditor.abrirArquivo(arquivo);
            }
        });

        // Menu Arquivo > Abrir Pasta
        barraDeMenu.obterItemAbrirPasta()
                .setAccelerator(javafx.scene.input.KeyCombination.keyCombination("Ctrl+Shift+O"));
        barraDeMenu.obterItemAbrirPasta().setOnAction(e -> {
            DirectoryChooser seletorDiretorio = new DirectoryChooser();
            seletorDiretorio.setTitle("Abrir Pasta");
            File diretorio = seletorDiretorio.showDialog(painelPrincipal.getScene().getWindow());
            if (diretorio != null) {
                painelArvoreArquivos.recarregarArvore(diretorio);
                // Salvar preferência
                Preferences prefs = Preferences.userNodeForPackage(LayoutPrincipal.class);
                prefs.put("ultimaPastaAberta", diretorio.getAbsolutePath());
            }
        });

        // Menu Arquivo > Excluir
        barraDeMenu.obterItemExcluirArquivo().setOnAction(e -> excluirArquivoSelecionado(painelArvoreArquivos));

        // Tecla Delete na Árvore
        painelArvoreArquivos.obterArvore().setOnKeyPressed(e -> {
            if (e.getCode() == javafx.scene.input.KeyCode.DELETE) {
                excluirArquivoSelecionado(painelArvoreArquivos);
            }
        });

        // Carregar a última pasta aberta
        Preferences prefs = Preferences.userNodeForPackage(LayoutPrincipal.class);
        String ultimaPastaAberta = prefs.get("ultimaPastaAberta", null);
        if (ultimaPastaAberta != null) {
            File pasta = new File(ultimaPastaAberta);
            if (pasta.exists() && pasta.isDirectory()) {
                painelArvoreArquivos.recarregarArvore(pasta);
            }
        }

        // Menu Visualizar > Tema
        barraDeMenu.obterItemTemaClaro().setOnAction(e -> definirTema("Light"));
        barraDeMenu.obterItemTemaEscuro().setOnAction(e -> definirTema("Dark"));

        // Menu Arquivo > Sair
        barraDeMenu.obterItemSair().setOnAction(e -> {
            if (painelEditor.confirmarFechamento()) {
                Platform.exit();
            }
        });

        // Menu Editar
        barraDeMenu.obterItemRecortar().setAccelerator(javafx.scene.input.KeyCombination.keyCombination("Ctrl+X"));
        barraDeMenu.obterItemRecortar().setOnAction(e -> painelEditor.recortar());
        barraDeMenu.obterItemCopiar().setAccelerator(javafx.scene.input.KeyCombination.keyCombination("Ctrl+C"));
        barraDeMenu.obterItemCopiar().setOnAction(e -> painelEditor.copiar());
        barraDeMenu.obterItemColar().setAccelerator(javafx.scene.input.KeyCombination.keyCombination("Ctrl+V"));
        barraDeMenu.obterItemColar().setOnAction(e -> painelEditor.colar());

        // Menu Ajuda > Sobre
        barraDeMenu.obterItemSobre().setOnAction(e -> {
            Alert alerta = new Alert(Alert.AlertType.INFORMATION);
            alerta.setTitle("Sobre o Nyx");
            alerta.setHeaderText("Editor Nyx");
            alerta.setContentText("Um editor de texto profissional feito em JavaFX.");
            alerta.showAndWait();
        });

        // --- CONTROLADORES EXTRAS ---
        // Controlador de Compilação
        ControladorCompilacao controladorCompilacao = new ControladorCompilacao(painelEditor, painelConsole,
                painelErros, pipelineCompilacao);

        // Menu Executar > Compilar (F5)
        barraDeMenu.obterItemCompilar().setAccelerator(javafx.scene.input.KeyCombination.keyCombination("F5"));
        barraDeMenu.obterItemCompilar().setOnAction(e -> {
            controladorCompilacao.aoCompilar();
        });

        // Menu Executar > Executar (F6)
        barraDeMenu.obterItemExecutar().setAccelerator(javafx.scene.input.KeyCombination.keyCombination("F6"));
        barraDeMenu.obterItemExecutar().setOnAction(e -> {
            controladorCompilacao.aoExecutar();
        });

        // Tenta carregar o último arquivo aberto
        carregarArquivoAnterior();
    }

    public BorderPane obterPainelPrincipal() {
        return painelPrincipal;
    }

    public PainelEditor obterPainelEditor() {
        return painelEditor;
    }

    public void aplicarTemaAtual() {
        Preferences prefs = Preferences.userNodeForPackage(LayoutPrincipal.class);
        String temaSalvo = prefs.get("temaAplicacao", "Light");
        aplicarTema(temaSalvo);
    }

    private void definirTema(String tema) {
        Preferences prefs = Preferences.userNodeForPackage(LayoutPrincipal.class);
        prefs.put("temaAplicacao", tema);
        aplicarTema(tema);
    }

    private void aplicarTema(String tema) {
        if (painelPrincipal.getScene() == null)
            return; // Proteção se chamado antes da scene ser setada

        painelPrincipal.getScene().getStylesheets().clear();
        String caminhoCss = "";
        if ("Dark".equalsIgnoreCase(tema)) {
            caminhoCss = "/styles/dark.css";
        } else {
            caminhoCss = "/styles/light.css";
        }

        java.net.URL urlCss = getClass().getResource(caminhoCss);
        if (urlCss != null) {
            painelPrincipal.getScene().getStylesheets().add(urlCss.toExternalForm());
        } else {
            System.err.println("Não foi possível encontrar o arquivo de estilo: " + caminhoCss);
        }
    }

    private void excluirArquivoSelecionado(PainelArvoreArquivos painelArvore) {
        TreeItem<File> itemSelecionado = painelArvore.obterArvore().getSelectionModel().getSelectedItem();
        if (itemSelecionado == null)
            return;

        File arquivoParaExcluir = itemSelecionado.getValue();
        if (arquivoParaExcluir == null || !arquivoParaExcluir.exists())
            return;

        Alert alerta = new Alert(Alert.AlertType.CONFIRMATION);
        alerta.setTitle("Excluir Arquivo");
        alerta.setHeaderText("Você tem certeza que deseja excluir este arquivo?");
        alerta.setContentText("Esta ação é permanente: " + arquivoParaExcluir.getName());

        Optional<ButtonType> resultado = alerta.showAndWait();
        if (resultado.isPresent() && resultado.get() == ButtonType.OK) {
            try {
                if (arquivoParaExcluir.delete()) {
                    // Atualiza a árvore recarregando a raiz
                    TreeItem<File> itemRaiz = painelArvore.obterArvore().getRoot();
                    if (itemRaiz != null && itemRaiz.getValue() != null) {
                        painelArvore.recarregarArvore(itemRaiz.getValue());
                    }
                } else {
                    Alert alertaErro = new Alert(Alert.AlertType.ERROR);
                    alertaErro.setTitle("Erro");
                    alertaErro.setHeaderText("Não foi possível excluir o arquivo");
                    alertaErro.setContentText("Verifique permissões ou se o arquivo está em uso.");
                    alertaErro.showAndWait();
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    public void salvarPreferencias() {
        Preferences prefs = Preferences.userNodeForPackage(LayoutPrincipal.class);
        File arquivoAtual = painelEditor.obterArquivoAtual();
        if (arquivoAtual != null) {
            prefs.put("ultimoArquivoAberto", arquivoAtual.getAbsolutePath());
        } else {
            prefs.remove("ultimoArquivoAberto");
        }
    }

    public void carregarArquivoAnterior() {
        Preferences prefs = Preferences.userNodeForPackage(LayoutPrincipal.class);
        String caminho = prefs.get("ultimoArquivoAberto", null);
        if (caminho != null) {
            File arquivo = new File(caminho);
            if (arquivo.exists() && arquivo.isFile()) {
                painelEditor.abrirArquivo(arquivo);
            }
        }
    }
}
