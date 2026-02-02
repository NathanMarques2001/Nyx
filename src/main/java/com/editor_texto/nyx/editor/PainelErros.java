package com.editor_texto.nyx.editor;

import com.editor_texto.nyx.compiler.api.ErroCompilacao;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.util.List;
import java.util.function.Consumer;

/**
 * Painel responsável por exibir a lista de erros de compilação.
 */
public class PainelErros {

    private final TableView<ErroCompilacao> tabelaErros;
    private final ObservableList<ErroCompilacao> listaErros;
    private Consumer<ErroCompilacao> acaoNavegacao;

    public PainelErros() {
        tabelaErros = new TableView<>();
        listaErros = FXCollections.observableArrayList();
        tabelaErros.setItems(listaErros);

        configurarColunas();
        configurarEventos();

        VBox.setVgrow(tabelaErros, Priority.ALWAYS);
    }

    private void configurarColunas() {
        TableColumn<ErroCompilacao, String> colTipo = new TableColumn<>("Tipo");
        colTipo.setCellValueFactory(new PropertyValueFactory<>("tipo"));
        colTipo.setPrefWidth(100);

        TableColumn<ErroCompilacao, Integer> colLinha = new TableColumn<>("Linha");
        colLinha.setCellValueFactory(new PropertyValueFactory<>("linha"));
        colLinha.setPrefWidth(60);

        TableColumn<ErroCompilacao, Integer> colColuna = new TableColumn<>("Coluna");
        colColuna.setCellValueFactory(new PropertyValueFactory<>("coluna"));
        colColuna.setPrefWidth(60);

        TableColumn<ErroCompilacao, String> colMensagem = new TableColumn<>("Mensagem");
        colMensagem.setCellValueFactory(new PropertyValueFactory<>("mensagem"));
        colMensagem.setPrefWidth(500);

        tabelaErros.getColumns().addAll(colTipo, colLinha, colColuna, colMensagem);
        tabelaErros.setPlaceholder(new javafx.scene.control.Label("Nenhum erro encontrado."));
    }

    private void configurarEventos() {
        tabelaErros.setRowFactory(tv -> {
            TableRow<ErroCompilacao> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (!row.isEmpty() && event.getClickCount() == 2 && acaoNavegacao != null) {
                    ErroCompilacao erro = row.getItem();
                    acaoNavegacao.accept(erro);
                }
            });
            return row;
        });
    }

    public void definirAcaoNavegacao(Consumer<ErroCompilacao> acao) {
        this.acaoNavegacao = acao;
    }

    public void mostrarErros(List<ErroCompilacao> erros) {
        listaErros.setAll(erros);
    }

    public void limpar() {
        listaErros.clear();
    }

    public TableView<ErroCompilacao> obterTabela() {
        return tabelaErros;
    }

    public VBox obterPainel() {
        // Encapsula em VBox se necessário, ou retorna a tabela
        VBox box = new VBox(tabelaErros);
        return box;
    }
}
