package com.editor_texto.nyx.ui;

import com.editor_texto.nyx.ui.sintaxe.SintaxeLC;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import org.fxmisc.flowless.VirtualizedScrollPane;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.LineNumberFactory;

import java.io.File;
import java.io.IOException;
import java.nio.charset.MalformedInputException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Optional;

/**
 * Gerencia as abas de edição de texto.
 * Contém a lógica de abrir, salvar, verificar alterações e aplicar syntax
 * highlighting.
 */
public class PainelEditor {
    private final TabPane painelDeAbas;

    public PainelEditor() {
        painelDeAbas = new TabPane();
    }

    public TabPane obterPainelDeAbas() {
        return painelDeAbas;
    }

    /**
     * Abre um arquivo em uma nova aba, ou seleciona a aba se já estiver aberta.
     * 
     * @param arquivo O arquivo a ser aberto.
     */
    public void abrirArquivo(File arquivo) {
        // Verifica se já está aberto
        for (Tab aba : painelDeAbas.getTabs()) {
            AbaEditor abaEditor = (AbaEditor) aba;
            if (abaEditor.obterArquivo() != null
                    && abaEditor.obterArquivo().getAbsolutePath().equals(arquivo.getAbsolutePath())) {
                painelDeAbas.getSelectionModel().select(aba);
                return;
            }
        }

        try {
            String conteudo;
            try {
                conteudo = Files.readString(arquivo.toPath());
            } catch (MalformedInputException ex) {
                // Fallback para ISO-8859-1 se UTF-8 falhar
                conteudo = Files.readString(arquivo.toPath(), StandardCharsets.ISO_8859_1);
            }

            AbaEditor novaAba = new AbaEditor(arquivo, conteudo);
            painelDeAbas.getTabs().add(novaAba);
            painelDeAbas.getSelectionModel().select(novaAba);
        } catch (Exception e) {
            e.printStackTrace();
            Alert alerta = new Alert(Alert.AlertType.ERROR);
            alerta.setTitle("Erro");
            alerta.setHeaderText("Erro ao abrir arquivo");
            alerta.setContentText(e.getMessage());
            alerta.showAndWait();
        }
    }

    /**
     * Cria uma nova aba "Sem Título".
     */
    public void criarNovaAba() {
        AbaEditor novaAba = new AbaEditor(null, "");
        painelDeAbas.getTabs().add(novaAba);
        painelDeAbas.getSelectionModel().select(novaAba);
        novaAba.areaCodigo.requestFocus();
    }

    /**
     * Salva o conteúdo da aba selecionada atualmente.
     */
    public void salvarAbaAtual() {
        Tab abaSelecionada = painelDeAbas.getSelectionModel().getSelectedItem();
        if (abaSelecionada instanceof AbaEditor) {
            ((AbaEditor) abaSelecionada).salvar();
        }
    }

    /**
     * Abre diálogo de "Salvar Como" para a aba atual.
     */
    public void salvarAbaComo() {
        Tab abaSelecionada = painelDeAbas.getSelectionModel().getSelectedItem();
        if (abaSelecionada instanceof AbaEditor) {
            ((AbaEditor) abaSelecionada).salvarComo();
        }
    }

    /**
     * Tenta fechar todas as abas, pedindo confirmação se necessário.
     * 
     * @return true se todas foram fechadas (ou não havia nada sujo), false se
     *         cancelado.
     */
    public boolean confirmarFechamento() {
        for (Tab aba : painelDeAbas.getTabs()) {
            // Dispara o evento de fechamento da aba
            if (aba.getOnCloseRequest() != null) {
                // Simula o evento, se cancelado, retorna false
                // Nota: Isso é simplificado. Idealmente, deve-se chamar a lógica interna.
                // Aqui apenas verificamos se está sujo e chamamos a lógica da aba.
                if (aba instanceof AbaEditor) {
                    AbaEditor editor = (AbaEditor) aba;
                    if (editor.possuiModificacao()) {
                        painelDeAbas.getSelectionModel().select(aba);
                        // Dispara alerta manualmente pois não estamos em um Event real
                        Alert alerta = new Alert(Alert.AlertType.CONFIRMATION);
                        alerta.setTitle("Alterações não salvas");
                        alerta.setHeaderText("Alterações não salvas em " + editor.getText());
                        alerta.setContentText("Deseja salvar suas alterações?");

                        ButtonType btnSalvar = new ButtonType("Salvar");
                        ButtonType btnNaoSalvar = new ButtonType("Não Salvar");
                        ButtonType btnCancelar = new ButtonType("Cancelar", ButtonBar.ButtonData.CANCEL_CLOSE);

                        alerta.getButtonTypes().setAll(btnSalvar, btnNaoSalvar, btnCancelar);

                        Optional<ButtonType> resultado = alerta.showAndWait();
                        if (resultado.isPresent()) {
                            if (resultado.get() == btnSalvar) {
                                if (!editor.salvar())
                                    return false;
                            } else if (resultado.get() == btnCancelar) {
                                return false;
                            }
                        }
                    }
                }
            }
        }
        return true;
    }

    public File obterArquivoAtual() {
        Tab aba = painelDeAbas.getSelectionModel().getSelectedItem();
        if (aba instanceof AbaEditor) {
            return ((AbaEditor) aba).obterArquivo();
        }
        return null;
    }

    public String obterCodigoAtual() {
        Tab aba = painelDeAbas.getSelectionModel().getSelectedItem();
        if (aba instanceof AbaEditor) {
            return ((AbaEditor) aba).areaCodigo.getText();
        }
        return "";
    }

    public void limparErros() {
        Tab aba = painelDeAbas.getSelectionModel().getSelectedItem();
        if (aba instanceof AbaEditor) {
            ((AbaEditor) aba).limparErros();
        }
    }

    public void mostrarErros(java.util.List<com.editor_texto.nyx.compiler.ErroCompilacao> erros) {
        Tab aba = painelDeAbas.getSelectionModel().getSelectedItem();
        if (aba instanceof AbaEditor) {
            ((AbaEditor) aba).destacarErros(erros);
        }
    }

    public void navegarParaErro(int linha, int coluna) {
        Tab aba = painelDeAbas.getSelectionModel().getSelectedItem();
        if (aba instanceof AbaEditor) {
            ((AbaEditor) aba).navegarPara(linha, coluna);
        }
    }

    public void recortar() {
        Tab aba = painelDeAbas.getSelectionModel().getSelectedItem();
        if (aba instanceof AbaEditor) {
            ((AbaEditor) aba).areaCodigo.cut();
        }
    }

    public void copiar() {
        Tab aba = painelDeAbas.getSelectionModel().getSelectedItem();
        if (aba instanceof AbaEditor) {
            ((AbaEditor) aba).areaCodigo.copy();
        }
    }

    public void colar() {
        Tab aba = painelDeAbas.getSelectionModel().getSelectedItem();
        if (aba instanceof AbaEditor) {
            ((AbaEditor) aba).areaCodigo.paste();
        }
    }

    // AbaEditor modifications below

    // Classe interna para Aba especializada
    private static class AbaEditor extends Tab {
        private File arquivo; // Mutável
        private final CodeArea areaCodigo;
        private boolean modificado = false;
        // Armazena erros atuais para tooltip
        private java.util.List<com.editor_texto.nyx.compiler.ErroCompilacao> errosAtuais = new java.util.ArrayList<>();
        private String conteudoOriginal;

        private final javafx.stage.Popup popupErro = new javafx.stage.Popup();

        public AbaEditor(File arquivo, String conteudo) {
            this.arquivo = arquivo;
            this.conteudoOriginal = conteudo;
            this.setText(arquivo != null ? arquivo.getName() : "Sem Título");

            areaCodigo = new CodeArea();
            areaCodigo.setParagraphGraphicFactory(LineNumberFactory.get(areaCodigo));

            // Configura popup
            javafx.scene.control.Label lblErro = new javafx.scene.control.Label();
            lblErro.setStyle(
                    "-fx-background-color: #ffcccc; -fx-text-fill: black; -fx-padding: 5; -fx-border-color: red; -fx-border-width: 1; -fx-font-size: 12px;");
            popupErro.getContent().add(lblErro);

            // Highlighting inicial e listener
            configurarHighlighting(arquivo != null ? arquivo.getName() : "");

            areaCodigo.replaceText(conteudo);

            // Ouvinte de mudanças para dirty state
            areaCodigo.textProperty().addListener((obs, velho, novo) -> {
                if (!modificado && !novo.equals(conteudoOriginal)) {
                    definirModificado(true);
                }
            });

            this.setContent(new VirtualizedScrollPane<>(areaCodigo));

            configurarTooltip(lblErro);

            this.setOnCloseRequest(e -> {
                if (modificado) {
                    Alert alerta = new Alert(Alert.AlertType.CONFIRMATION);
                    alerta.setTitle("Alterações não salvas");
                    alerta.setHeaderText("Alterações não salvas em " + getText());
                    alerta.setContentText("Deseja salvar suas alterações?");

                    ButtonType btnSalvar = new ButtonType("Salvar");
                    ButtonType btnNaoSalvar = new ButtonType("Não Salvar");
                    ButtonType btnCancelar = new ButtonType("Cancelar", ButtonBar.ButtonData.CANCEL_CLOSE);

                    alerta.getButtonTypes().setAll(btnSalvar, btnNaoSalvar, btnCancelar);

                    Optional<ButtonType> resultado = alerta.showAndWait();
                    if (resultado.isPresent()) {
                        if (resultado.get() == btnSalvar) {
                            if (!salvar()) {
                                e.consume(); // Falhou ao salvar
                            }
                        } else if (resultado.get() == btnCancelar) {
                            e.consume(); // Cancela fechamento
                        }
                    }
                }
            });
        }

        private void configurarHighlighting(String nomeArquivo) {
            // Aplica highlight inicial
            if (nomeArquivo.toLowerCase().endsWith(".lc") || nomeArquivo.isEmpty()) {
                aplicarHighlight();
            }

            // Reage a mudanças no texto para atualizar o highlight
            // Usando textProperty listener simples para garantir compatibilidade
            // e evitar problemas com dependências transitivas desconhecidas.
            areaCodigo.textProperty().addListener((obs, velho, novo) -> {
                if (nomeArquivo.toLowerCase().endsWith(".lc") || nomeArquivo.isEmpty()) {
                    aplicarHighlight();
                }
            });
        }

        private void aplicarHighlight() {
            try {
                areaCodigo.setStyleSpans(0, SintaxeLC.calcularRealce(areaCodigo.getText()));
            } catch (Exception e) {
                // Ignora erros de highlight enquanto digita para não travar
            }
        }

        private void configurarTooltip(javafx.scene.control.Label lblErro) {
            areaCodigo.setOnMouseMoved(e -> {
                if (errosAtuais.isEmpty()) {
                    popupErro.hide();
                    return;
                }

                var hit = areaCodigo.hit(e.getX(), e.getY());
                int charIdx = hit.getInsertionIndex();

                var pos = areaCodigo.offsetToPosition(charIdx, org.fxmisc.richtext.model.TwoDimensional.Bias.Forward);
                int linhaMouse = pos.getMajor();

                com.editor_texto.nyx.compiler.ErroCompilacao erroEncontrado = null;
                for (com.editor_texto.nyx.compiler.ErroCompilacao erro : errosAtuais) {
                    if ((erro.getLinha() - 1) == linhaMouse) {
                        erroEncontrado = erro;
                        break;
                    }
                }

                if (erroEncontrado != null) {
                    lblErro.setText(erroEncontrado.getTipo() + ": " + erroEncontrado.getMensagem());
                    if (!popupErro.isShowing()) {
                        javafx.geometry.Point2D p = areaCodigo.localToScreen(e.getX(), e.getY());
                        popupErro.show(areaCodigo, p.getX() + 10, p.getY() + 10);
                    }
                } else {
                    popupErro.hide();
                }
            });
        }

        public void navegarPara(int linha, int coluna) {
            int indexLinha = linha - 1;
            int indexCol = coluna - 1;
            if (indexLinha >= 0 && indexLinha < areaCodigo.getParagraphs().size()) {
                areaCodigo.moveTo(indexLinha, Math.max(0, indexCol));
                areaCodigo.requestFollowCaret();
                areaCodigo.requestFocus();
            }
        }

        public void destacarErros(java.util.List<com.editor_texto.nyx.compiler.ErroCompilacao> erros) {
            this.errosAtuais.clear();
            this.errosAtuais.addAll(erros);

            for (com.editor_texto.nyx.compiler.ErroCompilacao erro : erros) {
                int linha = erro.getLinha() - 1;
                if (linha >= 0 && linha < areaCodigo.getParagraphs().size()) {
                    String textoLinha = areaCodigo.getText(linha);
                    int col = erro.getColuna() - 1;
                    if (col < 0)
                        col = 0;
                    if (col >= textoLinha.length())
                        col = 0;

                    int fimToken = col;
                    while (fimToken < textoLinha.length()) {
                        char c = textoLinha.charAt(fimToken);
                        if (Character.isWhitespace(c) || ";,(){}[].".indexOf(c) >= 0) {
                            if (fimToken == col)
                                fimToken++;
                            break;
                        }
                        fimToken++;
                    }
                    if (erro.getColuna() <= 0) {
                        fimToken = textoLinha.length();
                    }

                    int inicio = areaCodigo.getAbsolutePosition(linha, col);
                    int fim = areaCodigo.getAbsolutePosition(linha, fimToken);

                    if (inicio < fim) {
                        org.fxmisc.richtext.model.StyleSpans<java.util.Collection<String>> spansOriginal = areaCodigo
                                .getStyleSpans(inicio, fim);
                        org.fxmisc.richtext.model.StyleSpans<java.util.Collection<String>> novosSpans = spansOriginal
                                .mapStyles(styles -> {
                                    java.util.List<String> novaLista = new java.util.ArrayList<>(styles);
                                    if (!novaLista.contains("erro-compilacao")) {
                                        novaLista.add("erro-compilacao");
                                    }
                                    return novaLista;
                                });
                        areaCodigo.setStyleSpans(inicio, novosSpans);
                    }
                }
            }
            areaCodigo.requestLayout();
        }

        public void limparErros() {
            this.errosAtuais.clear();
            popupErro.hide();
            if (arquivo != null && arquivo.getName().toLowerCase().endsWith(".lc")) {
                areaCodigo.setStyleSpans(0, SintaxeLC.calcularRealce(areaCodigo.getText()));
            } else {
                areaCodigo.clearStyle(0, areaCodigo.getLength());
            }
        }

        public File obterArquivo() {
            return arquivo;
        }

        public boolean possuiModificacao() {
            return modificado;
        }

        public void definirModificado(boolean modificado) {
            this.modificado = modificado;
            if (modificado) {
                if (!getText().endsWith("*"))
                    setText(getText() + "*");
            } else {
                if (getText().endsWith("*"))
                    setText(getText().substring(0, getText().length() - 1));
            }
        }

        public boolean salvar() {
            if (arquivo == null) {
                return salvarComo();
            } else {
                try {
                    Files.writeString(arquivo.toPath(), areaCodigo.getText());
                    conteudoOriginal = areaCodigo.getText();
                    definirModificado(false);
                    return true;
                } catch (IOException e) {
                    e.printStackTrace();
                    mostrarErroSalvar(e.getMessage());
                    return false;
                }
            }
        }

        public boolean salvarComo() {
            javafx.stage.FileChooser fileChooser = new javafx.stage.FileChooser();
            fileChooser.setTitle("Salvar Como");
            fileChooser.getExtensionFilters()
                    .add(new javafx.stage.FileChooser.ExtensionFilter("Arquivos Nyx (*.lc)", "*.lc"));
            fileChooser.getExtensionFilters()
                    .add(new javafx.stage.FileChooser.ExtensionFilter("Todos os Arquivos", "*.*"));

            if (arquivo != null && arquivo.getParentFile() != null) {
                fileChooser.setInitialDirectory(arquivo.getParentFile());
                fileChooser.setInitialFileName(arquivo.getName());
            } else {
                fileChooser.setInitialFileName("sem_titulo.lc");
            }

            File novoArquivo = fileChooser.showSaveDialog(this.getTabPane().getScene().getWindow());
            if (novoArquivo != null) {
                this.arquivo = novoArquivo;
                this.setText(novoArquivo.getName());
                if (novoArquivo.getName().toLowerCase().endsWith(".lc")) {
                    areaCodigo.setStyleSpans(0, SintaxeLC.calcularRealce(areaCodigo.getText()));
                }
                return salvar();
            }
            return false;
        }

        private void mostrarErroSalvar(String msg) {
            Alert alerta = new Alert(Alert.AlertType.ERROR);
            alerta.setTitle("Erro");
            alerta.setHeaderText("Não foi possível salvar o arquivo");
            alerta.setContentText(msg);
            alerta.showAndWait();
        }
    }
}
