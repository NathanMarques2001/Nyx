package com.editor_texto.nyx.editor;

import com.editor_texto.nyx.compiler.api.CompiladorLC;
import com.editor_texto.nyx.compiler.api.ErroCompilacao;
import com.editor_texto.nyx.compiler.api.ResultadoCompilacao;
import com.editor_texto.nyx.compiler.api.TipoErro;
import com.editor_texto.nyx.ui.components.PipelineCompilacao;
import com.editor_texto.nyx.ui.components.PipelineCompilacao.Fase;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.control.Alert;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * Controlador responsável pela lógica de compilação na interface gráfica.
 */
public class ControladorCompilacao {

    private final PainelEditor painelEditor;
    private final PainelConsole painelConsole;
    private final PainelErros painelErros;
    private final PipelineCompilacao pipelineCompilacao;

    public ControladorCompilacao(PainelEditor painelEditor, PainelConsole painelConsole, PainelErros painelErros,
            PipelineCompilacao pipelineCompilacao) {
        this.painelEditor = painelEditor;
        this.painelConsole = painelConsole;
        this.painelErros = painelErros;
        this.pipelineCompilacao = pipelineCompilacao;
    }

    public void aoCompilar() {
        System.out.println("DEBUG: Botão Compilar acionado (ControladorCompilacao).");

        // 1. Limpeza e Reset Visual
        painelEditor.limparErros();
        painelErros.limpar();
        if (pipelineCompilacao != null) {
            pipelineCompilacao.resetar();
            pipelineCompilacao.marcarEmExecucao(Fase.LEXICA);
        }

        if (painelConsole != null && painelConsole.obterConsole() != null) {
            painelConsole.obterConsole().appendText("\n--------------------------------------------------\n");
            painelConsole.obterConsole().appendText("Iniciando processo de compilação...\n");
        }

        String codigoFonte = painelEditor.obterCodigoAtual();
        File arquivoAtual = painelEditor.obterArquivoAtual();

        if (codigoFonte == null || codigoFonte.trim().isEmpty()) {
            if (painelConsole != null && painelConsole.obterConsole() != null) {
                painelConsole.obterConsole().appendText("[ERRO] Nenhum código fonte para compilar.\n");
            }
            if (pipelineCompilacao != null)
                pipelineCompilacao.marcarErro(Fase.LEXICA);

            Alert alerta = new Alert(Alert.AlertType.WARNING);
            alerta.setTitle("Aviso");
            alerta.setHeaderText("Nenhum código para compilar");
            alerta.setContentText("O editor está vazio ou o código não foi carregado.");
            alerta.showAndWait();
            return;
        }

        // Determina nome e diretório
        String nomeArquivo = "output";
        Path diretorioSaidaFinal;

        if (arquivoAtual != null) {
            String nome = arquivoAtual.getName();
            int ultimoPonto = nome.lastIndexOf('.');
            if (ultimoPonto > 0) {
                nomeArquivo = nome.substring(0, ultimoPonto);
            } else {
                nomeArquivo = nome;
            }
            if (arquivoAtual.getParentFile() != null) {
                diretorioSaidaFinal = arquivoAtual.getParentFile().toPath().resolve("out");
            } else {
                diretorioSaidaFinal = Paths.get(System.getProperty("user.dir"), "out");
            }
        } else {
            diretorioSaidaFinal = Paths.get(System.getProperty("user.dir"), "out");
        }

        // --- Execução em Background ---
        Task<ResultadoCompilacao> tarefaCompilacao = new Task<>() {
            @Override
            protected ResultadoCompilacao call() throws Exception {
                CompiladorLC compilador = new CompiladorLC();
                // Simula um pequeno delay para que o usuário perceba "Executando"
                // para fases muito rápidas, se desejar. Mas vamos deixar rapido por enquanto.
                // Thread.sleep(500);
                return compilador.compilar(codigoFonte, diretorioSaidaFinal);
            }
        };

        tarefaCompilacao.setOnSucceeded(e -> {
            ResultadoCompilacao resultado = tarefaCompilacao.getValue();
            processarResultado(resultado);
        });

        tarefaCompilacao.setOnFailed(e -> {
            Throwable ex = tarefaCompilacao.getException();
            ex.printStackTrace();
            if (painelConsole != null && painelConsole.obterConsole() != null) {
                painelConsole.obterConsole()
                        .appendText("[FALHA] Erro interno no compilador: " + ex.getMessage() + "\n");
            }
            if (pipelineCompilacao != null) {
                // Se falhou com exceção, marca erro na fase atual (Léxica é o chute inicial)
                pipelineCompilacao.marcarErro(Fase.LEXICA);
            }
            Alert alerta = new Alert(Alert.AlertType.ERROR);
            alerta.setTitle("Erro Interno");
            alerta.setHeaderText("Falha ao invocar compilador");
            alerta.setContentText(ex.getMessage());
            alerta.showAndWait();
        });

        new Thread(tarefaCompilacao).start();
    }

    private void processarResultado(ResultadoCompilacao resultado) {
        if (resultado.isSucesso()) {
            // Sucesso TOTAL
            if (pipelineCompilacao != null) {
                pipelineCompilacao.marcarSucesso(Fase.LEXICA);
                pipelineCompilacao.marcarSucesso(Fase.SINTATICA);
                pipelineCompilacao.marcarSucesso(Fase.SEMANTICA);
                pipelineCompilacao.marcarSucesso(Fase.GERACAO);
            }

            String mensagem = "Compilação concluída! " + resultado.getArquivoAssemblyGerado().toString();
            if (painelConsole != null && painelConsole.obterConsole() != null) {
                painelConsole.obterConsole().appendText("[SUCESSO] " + mensagem + "\n");
                for (String aviso : resultado.getAvisos()) {
                    painelConsole.obterConsole().appendText("[AVISO] " + aviso + "\n");
                }
            }
        } else {
            // Houve erros. Precisamos identificar onde parou.
            List<ErroCompilacao> erros = resultado.getErros();

            boolean erroLexico = false;
            boolean erroSintatico = false;
            boolean erroSemantico = false;

            for (ErroCompilacao erro : erros) {
                switch (erro.getTipo()) {
                    case LEXICO:
                        erroLexico = true;
                        break;
                    case SINTATICO:
                        erroSintatico = true;
                        break;
                    case SEMANTICO:
                        erroSemantico = true;
                        break;
                    default:
                        break;
                }
            }

            if (pipelineCompilacao != null) {
                if (erroLexico) {
                    pipelineCompilacao.marcarErro(Fase.LEXICA);
                } else {
                    pipelineCompilacao.marcarSucesso(Fase.LEXICA);
                    // Passou léxico, verifica sintático
                    if (erroSintatico) {
                        pipelineCompilacao.marcarErro(Fase.SINTATICA);
                    } else {
                        pipelineCompilacao.marcarSucesso(Fase.SINTATICA);
                        // Passou sintático, verifica semântico
                        if (erroSemantico) {
                            pipelineCompilacao.marcarErro(Fase.SEMANTICA);
                        } else {
                            // Se chegou aqui e tem erro, mas não é nenhum dos anteriores,
                            // pode ser erro de geração ou 'OUTRO'.
                            pipelineCompilacao.marcarSucesso(Fase.SEMANTICA);
                            pipelineCompilacao.marcarErro(Fase.GERACAO);
                        }
                    }
                }
            }

            if (painelConsole != null && painelConsole.obterConsole() != null) {
                painelConsole.obterConsole()
                        .appendText("[FALHA] " + erros.size() + " erros encontrados.\n");
            }

            painelErros.mostrarErros(erros);
            painelEditor.mostrarErros(erros);

            if (!erros.isEmpty()) {
                painelErros.obterTabela().getSelectionModel().selectFirst();
                painelEditor.navegarParaErro(erros.get(0).getLinha(), erros.get(0).getColuna());
                painelErros.obterTabela().requestFocus();
            }
        }

        if (painelConsole != null && painelConsole.obterConsole() != null) {
            painelConsole.obterConsole().appendText("--------------------------------------------------\n");
        }
    }
}
