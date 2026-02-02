package com.editor_texto.nyx.editor;

import com.editor_texto.nyx.compiler.api.CompiladorLC;
import com.editor_texto.nyx.compiler.api.ErroCompilacao;
import com.editor_texto.nyx.compiler.api.GerenciadorJWASM;
import com.editor_texto.nyx.compiler.api.ResultadoAssembler;
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
        Task<ResultadoProcesso> tarefaCompilacao = new Task<>() {
            @Override
            protected ResultadoProcesso call() throws Exception {
                CompiladorLC compilador = new CompiladorLC();
                // Compila .lc -> .asm
                ResultadoCompilacao resCompilacao = compilador.compilar(codigoFonte, diretorioSaidaFinal);

                ResultadoAssembler resAssembler = null;
                // Se compilou com sucesso, roda o assembler .asm -> .obj
                if (resCompilacao.isSucesso()) {
                    GerenciadorJWASM assembler = new GerenciadorJWASM();
                    // O arquivo gerado está em resCompilacao.getArquivoAssemblyGerado()
                    resAssembler = assembler.montar(
                            resCompilacao.getArquivoAssemblyGerado(),
                            diretorioSaidaFinal);
                }

                return new ResultadoProcesso(resCompilacao, resAssembler);
            }
        };

        tarefaCompilacao.setOnSucceeded(e -> {
            ResultadoProcesso resultado = tarefaCompilacao.getValue();
            processarResultado(resultado);
        });

        tarefaCompilacao.setOnFailed(e -> {
            Throwable ex = tarefaCompilacao.getException();
            ex.printStackTrace();
            if (painelConsole != null && painelConsole.obterConsole() != null) {
                painelConsole.obterConsole()
                        .appendText("[FALHA] Erro interno ou exceção: " + ex.getMessage() + "\n");
            }
            if (pipelineCompilacao != null) {
                // Marca erro genérico na primeira fase se explodiu antes
                pipelineCompilacao.marcarErro(Fase.LEXICA);
            }
            Alert alerta = new Alert(Alert.AlertType.ERROR);
            alerta.setTitle("Erro Interno");
            alerta.setHeaderText("Falha na execução");
            alerta.setContentText(ex.getMessage());
            alerta.showAndWait();
        });

        new Thread(tarefaCompilacao).start();
    }

    private void processarResultado(ResultadoProcesso wrapper) {
        ResultadoCompilacao resultado = wrapper.compilacao;
        ResultadoAssembler assembler = wrapper.assembler;

        if (resultado.isSucesso()) {
            // Sucesso na Compilação (LC -> ASM)

            // Verifica sucesso do Assembler (ASM -> OBJ)
            boolean assemblerSucesso = (assembler != null && assembler.isSucesso());

            if (pipelineCompilacao != null) {
                pipelineCompilacao.marcarSucesso(Fase.LEXICA);
                pipelineCompilacao.marcarSucesso(Fase.SINTATICA);
                pipelineCompilacao.marcarSucesso(Fase.SEMANTICA);
                if (assemblerSucesso) {
                    pipelineCompilacao.marcarSucesso(Fase.GERACAO);
                } else {
                    pipelineCompilacao.marcarErro(Fase.GERACAO);
                }
            }

            String mensagem = "Compilação concluída! " + resultado.getArquivoAssemblyGerado().toString();
            if (painelConsole != null && painelConsole.obterConsole() != null) {
                painelConsole.obterConsole().appendText("[SUCESSO] " + mensagem + "\n");
                for (String aviso : resultado.getAvisos()) {
                    painelConsole.obterConsole().appendText("[AVISO] " + aviso + "\n");
                }

                // Logs do Assembler
                if (assembler != null) {
                    painelConsole.obterConsole().appendText("\n--- Execução JWASM ---\n");
                    for (String line : assembler.getStdout()) {
                        painelConsole.obterConsole().appendText(line + "\n");
                    }
                    for (String line : assembler.getStderr()) {
                        painelConsole.obterConsole().appendText("[JWASM ERRO] " + line + "\n");
                    }
                    if (assembler.isSucesso()) {
                        painelConsole.obterConsole().appendText("[SUCESSO] Montagem finalizada.\n");
                    } else {
                        painelConsole.obterConsole().appendText("[FALHA] Erro na montagem do arquivo objeto.\n");
                    }
                }
            }
        } else {
            // Houve erros na compilação LC
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

    // Classe interna para transportar os dois resultados
    private static class ResultadoProcesso {
        public final ResultadoCompilacao compilacao;
        public final ResultadoAssembler assembler;

        public ResultadoProcesso(ResultadoCompilacao compilacao, ResultadoAssembler assembler) {
            this.compilacao = compilacao;
            this.assembler = assembler;
        }
    }
}
