package com.editor_texto.nyx.ui;

import com.editor_texto.nyx.compiler.assembler.ResultadoMontador;
import com.editor_texto.nyx.compiler.ResultadoCompilacao;
import com.editor_texto.nyx.compiler.ErroCompilacao;
import com.editor_texto.nyx.domain.pipeline.ContextoCompilacao;
import com.editor_texto.nyx.domain.pipeline.OuvintePipeline;
import com.editor_texto.nyx.domain.pipeline.PassoGeracaoCodigo;
import com.editor_texto.nyx.domain.pipeline.PassoInicializacao;
import com.editor_texto.nyx.domain.pipeline.PassoLexico;
import com.editor_texto.nyx.domain.pipeline.PassoMontagem;
import com.editor_texto.nyx.domain.pipeline.PassoLigacao;
import com.editor_texto.nyx.domain.pipeline.PassoPipeline;
import com.editor_texto.nyx.domain.pipeline.PassoSemantico;
import com.editor_texto.nyx.domain.pipeline.PassoSintatico;
import com.editor_texto.nyx.domain.pipeline.Pipeline;
import com.editor_texto.nyx.pipeline.PipelineCompilacao;
import com.editor_texto.nyx.pipeline.PipelineCompilacao.Fase;
import com.editor_texto.nyx.sistema.ServicoLog;

import javafx.concurrent.Task;
import javafx.application.Platform;
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

        // Conecta o console ao serviço de logs
        configurarLogs();
    }

    private void configurarLogs() {
        if (this.painelConsole != null && this.painelConsole.obterConsole() != null) {
            ServicoLog.adicionarOuvinte(log -> {
                Platform.runLater(() -> {
                    String prefixo = "";
                    switch (log.tipo) {
                        case INFO:
                            prefixo = "[INFO]";
                            break;
                        case AVISO:
                            prefixo = "[AVISO]";
                            break;
                        case ERRO:
                            prefixo = "[ERRO]";
                            break;
                        case SUCESSO:
                            prefixo = "[SUCESSO]";
                            break;
                    }
                    // Formato simples no console: [TIPO] Mensagem
                    this.painelConsole.obterConsole().appendText(prefixo + " " + log.mensagem + "\n");
                });
            });
        }
    }

    public void aoCompilar() {
        ServicoLog.info("Botão Compilar acionado.");

        // 1. Limpeza e Reset Visual
        painelEditor.limparErros();
        painelErros.limpar();
        if (pipelineCompilacao != null) {
            pipelineCompilacao.resetar();
            pipelineCompilacao.marcarEmExecucao(Fase.LEXICA);
        }

        // Separador visual no log
        if (painelConsole != null && painelConsole.obterConsole() != null) {
            painelConsole.obterConsole().appendText("\n--------------------------------------------------\n");
        }
        ServicoLog.info("Iniciando processo de compilação...");

        String codigoFonte = painelEditor.obterCodigoAtual();
        File arquivoAtual = painelEditor.obterArquivoAtual();

        if (codigoFonte == null || codigoFonte.trim().isEmpty()) {
            ServicoLog.erro("Nenhum código fonte para compilar.");

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

        final String nomeArquivoFinal = nomeArquivo;

        ServicoLog.info("Diretório de saída: " + diretorioSaidaFinal.toString());

        // --- Execução em Background ---
        Task<ResultadoProcesso> tarefaCompilacao = new Task<>() {
            @Override
            protected ResultadoProcesso call() throws Exception {
                // Configura o Pipeline
                Pipeline pipeline = new Pipeline();
                pipeline.adicionarPasso(new PassoInicializacao()); // Garantir que pasta existe
                pipeline.adicionarPasso(new PassoLexico());
                pipeline.adicionarPasso(new PassoSintatico());
                pipeline.adicionarPasso(new PassoSemantico());
                pipeline.adicionarPasso(new PassoGeracaoCodigo(nomeArquivoFinal));
                pipeline.adicionarPasso(new PassoMontagem());
                pipeline.adicionarPasso(new PassoLigacao()); // Linking (OBJ -> EXE)

                // Configura ouvinte para updates visuais
                OuvintePipeline ouvinteVisual = new OuvintePipeline() {
                    @Override
                    public void aoIniciarPasso(PassoPipeline passo) {
                        Platform.runLater(() -> {
                            ServicoLog.info("Iniciando passo: " + passo.getNome());
                        });
                    }

                    @Override
                    public void aoFinalizarPasso(PassoPipeline passo, boolean sucesso) {
                        Platform.runLater(() -> {
                            if (sucesso)
                                ServicoLog.sucesso("Passo concluído: " + passo.getNome());
                            // A cor da bolinha é atualizada no método processarResultado ou logicamente
                            // aqui se quiséssemos mover mais lógica pra cá
                            // Por enquanto mantemos a lógica original de 'marcarSucesso' no final ou
                            // podemos fazer aqui:
                            if (passo instanceof PassoLexico)
                                pipelineCompilacao.marcarSucesso(Fase.LEXICA);
                            if (passo instanceof PassoSintatico)
                                pipelineCompilacao.marcarSucesso(Fase.SINTATICA);
                            if (passo instanceof PassoSemantico)
                                pipelineCompilacao.marcarSucesso(Fase.SEMANTICA);
                            // Geracao só no final
                        });
                    }

                    @Override
                    public void aoFalhar(String erro) {
                        Platform.runLater(() -> ServicoLog.erro("Falha no pipeline: " + erro));
                    }

                    @Override
                    public void aoFinalizar(boolean sucesso) {
                        // Callback final
                    }
                };
                pipeline.setOuvinte(ouvinteVisual);

                ContextoCompilacao contexto = new ContextoCompilacao(codigoFonte, diretorioSaidaFinal);

                // Executa (Sincronamente, já estamos numa thread separada)
                pipeline.executar(contexto);

                // Retorna o resultado acumulado no contexto
                return new ResultadoProcesso(
                        contexto.getResultadoCompilacao(),
                        contexto.getResultadoMontador());
            }
        };

        tarefaCompilacao.setOnSucceeded(e -> {
            ResultadoProcesso resultado = tarefaCompilacao.getValue();
            processarResultado(resultado);
        });

        tarefaCompilacao.setOnFailed(e -> {
            Throwable ex = tarefaCompilacao.getException();
            ex.printStackTrace();
            ServicoLog.erro("Exceção não tratada na thread de compilação: " + ex.getMessage());

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
        ResultadoMontador assembler = wrapper.assembler;

        if (resultado.isSucesso()) {
            // Sucesso na Compilação (LC -> ASM)

            // Verifica sucesso do Assembler (ASM -> OBJ)
            boolean assemblerSucesso = (assembler != null && assembler.obterSucesso());

            // Atualiza UI final
            if (pipelineCompilacao != null) {
                // As fases anteriores já foram marcadas pelo ouvinte, mas garantimos aqui
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
            ServicoLog.sucesso(mensagem);

            for (String aviso : resultado.getAvisos()) {
                ServicoLog.aviso(aviso);
            }

            // Logs do Assembler
            if (assembler != null) {
                ServicoLog.info("--- Execução JWASM ---");
                for (String line : assembler.obterSaida()) {
                    ServicoLog.info("[JWASM] " + line);
                }
                for (String line : assembler.obterErro()) {
                    ServicoLog.erro("[JWASM] " + line);
                }
                if (assembler.obterSucesso()) {
                    ServicoLog.sucesso("Montagem finalizada com sucesso.");
                } else {
                    ServicoLog.erro("Erro na montagem do arquivo objeto.");
                }
            }

        } else {
            // Houve erros na compilação LC
            List<ErroCompilacao> erros = resultado.getErros();

            boolean erroLexico = false;
            boolean erroSintatico = false;
            boolean erroSemantico = false;

            for (ErroCompilacao erro : erros) {
                ServicoLog.erro(String.format("Linha %d, Col %d: [%s] %s",
                        erro.getLinha(), erro.getColuna(), erro.getTipo(), erro.getMensagem()));

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
                // Lógica de fallback para pintar de vermelho se o ouvinte não pintou
                if (erroLexico)
                    pipelineCompilacao.marcarErro(Fase.LEXICA);
                else if (erroSintatico)
                    pipelineCompilacao.marcarErro(Fase.SINTATICA);
                else if (erroSemantico)
                    pipelineCompilacao.marcarErro(Fase.SEMANTICA);
                else
                    pipelineCompilacao.marcarErro(Fase.GERACAO);
            }

            ServicoLog.erro(erros.size() + " erros encontrados na compilação.");

            painelErros.mostrarErros(erros);
            painelEditor.mostrarErros(erros);

            if (!erros.isEmpty()) {
                painelErros.obterTabela().getSelectionModel().selectFirst();
                painelEditor.navegarParaErro(erros.get(0).getLinha(), erros.get(0).getColuna());
                painelErros.obterTabela().requestFocus();
            }
        }
    }

    public void aoExecutar() {
        // Verifica se existe executável gerado
        File arquivoAtual = painelEditor.obterArquivoAtual();
        if (arquivoAtual == null) {
            ServicoLog.erro("Salve o arquivo antes de executar.");
            return;
        }

        // Caminho esperado: out/NomeArquivo.exe
        // Lógica simplificada: assume padrão de saída 'out' no mesmo diretório ou
        // projeto
        String nomeBase = arquivoAtual.getName().replace(".txt", "").replace(".asm", ""); // ajusta extensao se
                                                                                          // necessario
        if (nomeBase.contains("."))
            nomeBase = nomeBase.substring(0, nomeBase.lastIndexOf('.'));

        Path dirSaida = Paths.get(System.getProperty("user.dir"), "out");
        if (arquivoAtual.getParentFile() != null) {
            dirSaida = arquivoAtual.getParentFile().toPath().resolve("out");
        }

        Path exe = dirSaida.resolve(nomeBase + ".exe");

        if (exe.toFile().exists()) {
            com.editor_texto.nyx.sistema.ExecutorPrograma.executar(exe.toString());
        } else {
            ServicoLog.erro("Executável não encontrado. Compile o código primeiro.");
            ServicoLog.info("Procurado em: " + exe.toString());
        }
    }

    // Classe interna para transportar os dois resultados
    private static class ResultadoProcesso {
        public final ResultadoCompilacao compilacao;
        public final ResultadoMontador assembler;

        public ResultadoProcesso(ResultadoCompilacao compilacao, ResultadoMontador assembler) {
            this.compilacao = compilacao;
            this.assembler = assembler;
        }
    }
}
