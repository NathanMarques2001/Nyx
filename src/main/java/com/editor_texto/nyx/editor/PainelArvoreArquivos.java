package com.editor_texto.nyx.editor;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.concurrent.TimeUnit;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;

/**
 * Painel que exibe a árvore de arquivos e diretórios do projeto.
 * Inclui funcionalidade de Drag & Drop e monitoramento de mudanças no sistema
 * de arquivos.
 */
public class PainelArvoreArquivos {

    private WatchService observador;
    private Thread threadObservadora;
    private File raizAtual;
    private final TreeView<File> arvoreVisual;

    public PainelArvoreArquivos() {
        arvoreVisual = new TreeView<>();
        configurarFabricaDeCelulas();

        String diretorioAtual = System.getProperty("user.dir");
        recarregarArvore(new File(diretorioAtual));

        iniciarObservador();
    }

    private void configurarFabricaDeCelulas() {
        arvoreVisual.setCellFactory(tv -> {
            TreeCell<File> celula = new TreeCell<File>() {
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

            // Detectar Drag (Origem)
            celula.setOnDragDetected(event -> {
                if (celula.getItem() == null)
                    return;

                Dragboard db = celula.startDragAndDrop(TransferMode.MOVE);
                ClipboardContent conteudo = new ClipboardContent();
                java.util.List<File> arquivos = new java.util.ArrayList<>();
                arquivos.add(celula.getItem());
                conteudo.putFiles(arquivos);
                db.setContent(conteudo);
                event.consume();
            });

            // Drag Over (Alvo)
            celula.setOnDragOver(event -> {
                if (event.getGestureSource() != celula && event.getDragboard().hasFiles()) {
                    File arquivoAlvo = celula.getItem();
                    // Apenas permitir soltar em diretórios
                    if (arquivoAlvo != null && arquivoAlvo.isDirectory()) {
                        event.acceptTransferModes(TransferMode.MOVE);
                    }
                }
                event.consume();
            });

            // Drag Dropped (Alvo)
            celula.setOnDragDropped(event -> {
                Dragboard db = event.getDragboard();
                boolean sucesso = false;
                if (db.hasFiles()) {
                    File arquivoOrigem = db.getFiles().get(0);
                    File diretorioAlvo = celula.getItem();

                    if (diretorioAlvo != null && diretorioAlvo.isDirectory() && arquivoOrigem != null) {
                        try {
                            java.nio.file.Path caminhoOrigem = arquivoOrigem.toPath();
                            java.nio.file.Path caminhoAlvo = diretorioAlvo.toPath().resolve(arquivoOrigem.getName());

                            if (!caminhoOrigem.equals(caminhoAlvo)) {
                                java.nio.file.Files.move(caminhoOrigem, caminhoAlvo,
                                        java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                                sucesso = true;
                                // O watcher deve atualizar a árvore, mas podemos forçar recarga se necessário
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                            Alert alerta = new Alert(Alert.AlertType.ERROR);
                            alerta.setTitle("Erro ao Mover Arquivo");
                            alerta.setContentText(e.getMessage());
                            alerta.showAndWait();
                        }
                    }
                }
                event.setDropCompleted(sucesso);
                event.consume();
            });

            return celula;
        });
    }

    /**
     * Recarrega a árvore de arquivos a partir de um diretório raiz.
     *
     * @param arquivoRaiz O diretório raiz a ser exibido.
     */
    public void recarregarArvore(File arquivoRaiz) {
        if (arquivoRaiz == null || !arquivoRaiz.exists())
            return;

        this.raizAtual = arquivoRaiz;

        TreeItem<File> itemRaiz = new TreeItem<>(arquivoRaiz);
        itemRaiz.setExpanded(true);
        criarArvoreRecursiva(arquivoRaiz, itemRaiz);
        arvoreVisual.setRoot(itemRaiz);
    }

    private void iniciarObservador() {
        threadObservadora = new Thread(() -> {
            try {
                observador = FileSystems.getDefault().newWatchService();
                WatchKey chave = null;
                File ultimaRaiz = null;

                while (!Thread.currentThread().isInterrupted()) {
                    // Se a raiz mudou, atualiza o watcher
                    if (raizAtual != null && (ultimaRaiz == null || !ultimaRaiz.equals(raizAtual))) {
                        if (chave != null)
                            chave.cancel();
                        try {
                            // Observa apenas o diretório raiz (para simplificação, sem recursão profunda
                            // por enquanto)
                            chave = raizAtual.toPath().register(observador,
                                    StandardWatchEventKinds.ENTRY_CREATE,
                                    StandardWatchEventKinds.ENTRY_DELETE,
                                    StandardWatchEventKinds.ENTRY_MODIFY);
                            ultimaRaiz = raizAtual;
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    if (chave != null) {
                        try {
                            WatchKey k = observador.poll(1, TimeUnit.SECONDS);
                            if (k != null) {
                                boolean atualizar = false;
                                for (WatchEvent<?> evento : k.pollEvents()) {
                                    if (evento.kind() != StandardWatchEventKinds.OVERFLOW) {
                                        atualizar = true;
                                    }
                                }
                                k.reset();

                                if (atualizar) {
                                    Platform.runLater(() -> {
                                        if (raizAtual != null)
                                            recarregarArvore(raizAtual);
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
        threadObservadora.setDaemon(true);
        threadObservadora.start();
    }

    private void criarArvoreRecursiva(File arquivo, TreeItem<File> pai) {
        if (arquivo.isDirectory()) {
            File[] arquivos = arquivo.listFiles();
            if (arquivos != null) {
                for (File filho : arquivos) {
                    TreeItem<File> novoItem = new TreeItem<>(filho);
                    pai.getChildren().add(novoItem);
                    if (filho.isDirectory()) {
                        criarArvoreRecursiva(filho, novoItem);
                    }
                }
            }
        }
    }

    public TreeView<File> obterArvore() {
        return arvoreVisual;
    }
}
