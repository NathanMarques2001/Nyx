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

    // Classe interna para Aba especializada
    private static class AbaEditor extends Tab {
        private final File arquivo;
        private final CodeArea areaCodigo;
        private boolean modificado = false;
        private final String conteudoOriginal; // Referência opcional para check estrito, usado simplificado aqui

        public AbaEditor(File arquivo, String conteudo) {
            this.arquivo = arquivo;
            this.conteudoOriginal = conteudo; // Não usado profundamente aqui, mas bom ter
            this.setText(arquivo != null ? arquivo.getName() : "Sem Título");

            areaCodigo = new CodeArea();
            areaCodigo.setParagraphGraphicFactory(LineNumberFactory.get(areaCodigo));

            // Aplicar highlighting se for arquivo .lc
            if (arquivo != null && arquivo.getName().toLowerCase().endsWith(".lc")) {
                areaCodigo.textProperty().addListener((obs, textoVelho, textoNovo) -> {
                    areaCodigo.setStyleSpans(0, SintaxeLC.calcularRealce(textoNovo));
                });
                // Highlighting inicial
                areaCodigo.replaceText(conteudo);
                areaCodigo.setStyleSpans(0, SintaxeLC.calcularRealce(conteudo));
            } else {
                areaCodigo.replaceText(conteudo);
            }

            // Ouvinte de mudanças
            areaCodigo.textProperty().addListener((obs, velho, novo) -> {
                if (!modificado && !novo.equals(conteudoOriginal)) {
                    definirModificado(true);
                }
            });

            // Scroll virtualizado para performance
            this.setContent(new VirtualizedScrollPane<>(areaCodigo));

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
