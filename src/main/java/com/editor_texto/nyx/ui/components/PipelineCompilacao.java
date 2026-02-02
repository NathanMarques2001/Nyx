package com.editor_texto.nyx.ui.components;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

/**
 * Componente visual que exibe o progresso do pipeline de compilação.
 * Mostra 4 etapas: Léxica, Sintática, Semântica e Geração de Código.
 */
public class PipelineCompilacao extends HBox {

    public enum Fase {
        LEXICA, SINTATICA, SEMANTICA, GERACAO
    }

    private final ItemFase faseLexica;
    private final ItemFase faseSintatica;
    private final ItemFase faseSemantica;
    private final ItemFase faseGeracao;

    public PipelineCompilacao() {
        this.setSpacing(20);
        this.setAlignment(Pos.CENTER_LEFT);
        this.setPadding(new Insets(5, 10, 5, 10));
        // Estilo base discreto
        this.setStyle("-fx-background-color: transparent; -fx-border-color: #cccccc; -fx-border-width: 0 0 1 0;");

        faseLexica = new ItemFase("Análise Léxica");
        faseSintatica = new ItemFase("Análise Sintática");
        faseSemantica = new ItemFase("Análise Semântica");
        faseGeracao = new ItemFase("Geração Assembly");

        this.getChildren().addAll(faseLexica, faseSintatica, faseSemantica, faseGeracao);
    }

    public void resetar() {
        faseLexica.resetar();
        faseSintatica.resetar();
        faseSemantica.resetar();
        faseGeracao.resetar();
    }

    public void marcarEmExecucao(Fase fase) {
        getItem(fase).setEstado(Estado.EXECUTANDO);
    }

    public void marcarSucesso(Fase fase) {
        getItem(fase).setEstado(Estado.SUCESSO);
    }

    public void marcarErro(Fase fase) {
        getItem(fase).setEstado(Estado.ERRO);
    }

    private ItemFase getItem(Fase fase) {
        switch (fase) {
            case LEXICA:
                return faseLexica;
            case SINTATICA:
                return faseSintatica;
            case SEMANTICA:
                return faseSemantica;
            case GERACAO:
                return faseGeracao;
            default:
                throw new IllegalArgumentException("Fase desconhecida");
        }
    }

    // --- Subcomponentes ---

    private enum Estado {
        NEUTRO, EXECUTANDO, SUCESSO, ERRO
    }

    private static class ItemFase extends HBox {
        private final Label lblNome;
        private final Label lblIcone;
        private final Circle indicador;

        public ItemFase(String nome) {
            this.setSpacing(8);
            this.setAlignment(Pos.CENTER);
            this.setPadding(new Insets(5));

            indicador = new Circle(4, Color.GRAY);
            lblNome = new Label(nome);
            lblNome.setFont(Font.font("System", FontWeight.NORMAL, 12));
            lblIcone = new Label("");
            lblIcone.setMinWidth(15);

            this.getChildren().addAll(indicador, lblNome, lblIcone);
            resetar();
        }

        public void resetar() {
            setEstado(Estado.NEUTRO);
        }

        public void setEstado(Estado estado) {
            switch (estado) {
                case NEUTRO:
                    indicador.setFill(Color.GRAY);
                    lblNome.setTextFill(Color.GRAY);
                    lblIcone.setText("");
                    break;
                case EXECUTANDO:
                    indicador.setFill(Color.BLUE); // Cor de destaque ou animada
                    lblNome.setTextFill(Color.web("#039ED3")); // Azul Nyx
                    lblIcone.setText("⏳"); // Ampulheta
                    break;
                case SUCESSO:
                    indicador.setFill(Color.GREEN);
                    lblNome.setTextFill(Color.GREEN);
                    lblIcone.setText("✔");
                    break;
                case ERRO:
                    indicador.setFill(Color.RED);
                    lblNome.setTextFill(Color.RED);
                    lblIcone.setText("❌");
                    break;
            }
        }
    }
}
