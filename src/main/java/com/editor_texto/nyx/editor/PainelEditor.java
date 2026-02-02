package com.editor_texto.nyx.editor;

import com.editor_texto.nyx.sintaxe.SintaxeLC;
import javafx.collections.ObservableList;
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
     * Salva o conteúdo da aba selecionada atualmente.
     */
    public void salvarAbaAtual() {
        Tab abaSelecionada = painelDeAbas.getSelectionModel().getSelectedItem();
        if (abaSelecionada instanceof AbaEditor) {
            ((AbaEditor) abaSelecionada).salvar();
        }
    }

    public boolean possuiAlteracoesNaoSalvas() {
        for (Tab aba : painelDeAbas.getTabs()) {
            if (((AbaEditor) aba).possuiModificacao()) {
                return true;
            }
        }
        return false;
    }

    public void recortar() {
        Tab abaSelecionada = painelDeAbas.getSelectionModel().getSelectedItem();
        if (abaSelecionada instanceof AbaEditor) {
            ((AbaEditor) abaSelecionada).areaCodigo.cut();
        }
    }

    public void copiar() {
        Tab abaSelecionada = painelDeAbas.getSelectionModel().getSelectedItem();
        if (abaSelecionada instanceof AbaEditor) {
            ((AbaEditor) abaSelecionada).areaCodigo.copy();
        }
    }

    public void colar() {
        Tab abaSelecionada = painelDeAbas.getSelectionModel().getSelectedItem();
        if (abaSelecionada instanceof AbaEditor) {
            ((AbaEditor) abaSelecionada).areaCodigo.paste();
        }
    }

    public String obterCodigoAtual() {
        Tab abaSelecionada = painelDeAbas.getSelectionModel().getSelectedItem();
        if (abaSelecionada instanceof AbaEditor) {
            return ((AbaEditor) abaSelecionada).areaCodigo.getText();
        }
        return "";
    }

    public File obterArquivoAtual() {
        Tab abaSelecionada = painelDeAbas.getSelectionModel().getSelectedItem();
        if (abaSelecionada instanceof AbaEditor) {
            return ((AbaEditor) abaSelecionada).obterArquivo();
        }
        return null;
    }

    /**
     * Tenta fechar todas as abas.
     * 
     * @return true se todas foram fechadas ou o usuário permitiu sair sem salvar,
     *         false se cancelado.
     */
    public boolean confirmarFechamento() {
        ObservableList<Tab> abas = painelDeAbas.getTabs();

        for (Tab aba : abas) {
            AbaEditor abaEditor = (AbaEditor) aba;
            if (abaEditor.possuiModificacao()) {
                painelDeAbas.getSelectionModel().select(aba);

                Alert alerta = new Alert(Alert.AlertType.CONFIRMATION);
                alerta.setTitle("Alterações não salvas");
                alerta.setHeaderText("Alterações não salvas em " + abaEditor.getText());
                alerta.setContentText("Deseja salvar suas alterações?");

                ButtonType btnSalvar = new ButtonType("Salvar");
                ButtonType btnNaoSalvar = new ButtonType("Não Salvar");
                ButtonType btnCancelar = new ButtonType("Cancelar", ButtonBar.ButtonData.CANCEL_CLOSE);

                alerta.getButtonTypes().setAll(btnSalvar, btnNaoSalvar, btnCancelar);

                Optional<ButtonType> resultado = alerta.showAndWait();
                if (resultado.isPresent()) {
                    if (resultado.get() == btnSalvar) {
                        abaEditor.salvar();
                    } else if (resultado.get() == btnCancelar) {
                        return false; // Interrompe o fechamento
                    }
                }
            }
        }
        return true;
    }

    public void navegarParaErro(int linha, int coluna) {
        Tab abaSelecionada = painelDeAbas.getSelectionModel().getSelectedItem();
        if (abaSelecionada instanceof AbaEditor) {
            ((AbaEditor) abaSelecionada).navegarPara(linha, coluna);
        }
    }

    public void mostrarErros(java.util.List<com.editor_texto.nyx.compiler.api.ErroCompilacao> erros) {
        Tab abaSelecionada = painelDeAbas.getSelectionModel().getSelectedItem();
        if (abaSelecionada instanceof AbaEditor) {
            ((AbaEditor) abaSelecionada).destacarErros(erros);
        }
    }

    public void limparErros() {
        Tab abaSelecionada = painelDeAbas.getSelectionModel().getSelectedItem();
        if (abaSelecionada instanceof AbaEditor) {
            ((AbaEditor) abaSelecionada).limparErros();
        }
    }

    // Classe interna para Aba especializada
    private static class AbaEditor extends Tab {
        private final File arquivo;
        private final CodeArea areaCodigo;
        private boolean modificado = false;
        // Armazena erros atuais para tooltip
        private java.util.List<com.editor_texto.nyx.compiler.api.ErroCompilacao> errosAtuais = new java.util.ArrayList<>();
        private final String conteudoOriginal;

        // Armazena tooltips de erro
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

            // Aplicar highlighting se for arquivo .lc
            if (arquivo != null && arquivo.getName().toLowerCase().endsWith(".lc")) {
                areaCodigo.textProperty().addListener((obs, textoVelho, textoNovo) -> {
                    // Recalcula sintaxe apenas. Se houver erro, será limpo ao digitar.
                    limparErros();
                    areaCodigo.setStyleSpans(0, SintaxeLC.calcularRealce(textoNovo));
                });
                // Highlighting inicial
                areaCodigo.replaceText(conteudo);
                areaCodigo.setStyleSpans(0, SintaxeLC.calcularRealce(conteudo));
            } else {
                areaCodigo.replaceText(conteudo);
            }

            // Ouvinte de mudanças para dirty state
            areaCodigo.textProperty().addListener((obs, velho, novo) -> {
                if (!modificado && !novo.equals(conteudoOriginal)) {
                    definirModificado(true);
                }
            });

            // Scroll virtualizado
            this.setContent(new VirtualizedScrollPane<>(areaCodigo));

            // Lógica de Tooltip: Mouse Hover
            areaCodigo.setOnMouseMoved(e -> {
                if (errosAtuais.isEmpty()) {
                    popupErro.hide();
                    return;
                }

                var hit = areaCodigo.hit(e.getX(), e.getY());
                int charIdx = hit.getInsertionIndex();

                var pos = areaCodigo.offsetToPosition(charIdx, org.fxmisc.richtext.model.TwoDimensional.Bias.Forward);
                int linhaMouse = pos.getMajor(); // Índice da linha (parágrafo)

                // Procura erro nesta linha
                com.editor_texto.nyx.compiler.api.ErroCompilacao erroEncontrado = null;
                for (com.editor_texto.nyx.compiler.api.ErroCompilacao erro : errosAtuais) {
                    if ((erro.getLinha() - 1) == linhaMouse) {
                        erroEncontrado = erro;
                        break;
                    }
                }

                if (erroEncontrado != null) {
                    lblErro.setText(erroEncontrado.getTipo() + ": " + erroEncontrado.getMensagem());
                    if (!popupErro.isShowing()) {
                        // Ajusta posição
                        javafx.geometry.Point2D p = areaCodigo.localToScreen(e.getX(), e.getY());
                        popupErro.show(areaCodigo, p.getX() + 10, p.getY() + 10);
                    }
                } else {
                    popupErro.hide();
                }
            });

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
                            salvar();
                        } else if (resultado.get() == btnCancelar) {
                            e.consume(); // Cancela fechamento
                        }
                    }
                }
            });
        }

        public void navegarPara(int linha, int coluna) {
            // Linhas são 0-based no CodeArea, mas 1-based no erro
            int indexLinha = linha - 1;
            if (indexLinha >= 0 && indexLinha < areaCodigo.getParagraphs().size()) {
                areaCodigo.moveTo(indexLinha, 0);
                areaCodigo.requestFollowCaret();
                areaCodigo.requestFocus();
            }
        }

        public void destacarErros(java.util.List<com.editor_texto.nyx.compiler.api.ErroCompilacao> erros) {
            this.errosAtuais.clear();
            this.errosAtuais.addAll(erros);

            for (com.editor_texto.nyx.compiler.api.ErroCompilacao erro : erros) {
                int linha = erro.getLinha() - 1;
                if (linha >= 0 && linha < areaCodigo.getParagraphs().size()) {
                    String textoLinha = areaCodigo.getText(linha);
                    int col = erro.getColuna() - 1;
                    if (col < 0)
                        col = 0;
                    if (col >= textoLinha.length())
                        col = 0; // Fallback para linha inteira se coluna invalida

                    // Tenta identificar o fim do token (espaço ou delimitador)
                    int fimToken = col;
                    while (fimToken < textoLinha.length()) {
                        char c = textoLinha.charAt(fimToken);
                        if (Character.isWhitespace(c) || ";,(){}[].".indexOf(c) >= 0) {
                            if (fimToken == col)
                                fimToken++; // Garante pelo menos 1 char se apontar pro delimitador
                            break;
                        }
                        fimToken++;
                    }
                    // Se não encontrou nada razoável ou coluna era 0, talvez selecionar linha toda?
                    // Mas "col" já ajustamos. Se era 0 vai pegar primeira palavra.
                    // Se col for 0 e queremos linha toda como fallback:
                    if (erro.getColuna() <= 0) {
                        fimToken = textoLinha.length();
                    }

                    int inicio = areaCodigo.getAbsolutePosition(linha, col);
                    int fim = areaCodigo.getAbsolutePosition(linha, fimToken);

                    // Preserva estilos existentes (syntax highlighting) e adiciona o de erro
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
            // Força atualização visual imediata para exibir o sublinhado
            areaCodigo.requestLayout();
        }

        public void limparErros() {
            this.errosAtuais.clear();
            popupErro.hide();
            // Reaplica apenas highlight de sintaxe
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

        public void salvar() {
            if (arquivo != null) {
                try {
                    Files.writeString(arquivo.toPath(), areaCodigo.getText());
                    definirModificado(false);
                } catch (IOException e) {
                    e.printStackTrace();
                    Alert alerta = new Alert(Alert.AlertType.ERROR);
                    alerta.setTitle("Erro");
                    alerta.setHeaderText("Não foi possível salvar o arquivo");
                    alerta.setContentText(e.getMessage());
                    alerta.showAndWait();
                }
            }
        }
    }
}
