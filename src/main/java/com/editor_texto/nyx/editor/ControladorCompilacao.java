package com.editor_texto.nyx.editor;

import com.editor_texto.nyx.compiler.api.CompiladorLC;
import javafx.scene.control.Alert;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Controlador responsável pela lógica de compilação na interface gráfica.
 */
public class ControladorCompilacao {

    private final PainelEditor painelEditor;
    private final PainelConsole painelConsole;

    public ControladorCompilacao(PainelEditor painelEditor, PainelConsole painelConsole) {
        this.painelEditor = painelEditor;
        this.painelConsole = painelConsole;
    }

    public void aoCompilar() {
        System.out.println("DEBUG: Botão Compilar acionado (ControladorCompilacao).");

        if (painelConsole != null && painelConsole.obterConsole() != null) {
            painelConsole.obterConsole().appendText("\n--------------------------------------------------\n");
            painelConsole.obterConsole().appendText("Iniciando processo de compilação...\n");
        } else {
            System.err.println("ERRO CRÍTICO: PainelConsole não inicializado corretamente.");
        }

        String codigoFonte = painelEditor.obterCodigoAtual();
        File arquivoAtual = painelEditor.obterArquivoAtual();

        System.out.println(
                "DEBUG: Código fonte obtido. Tamanho: " + (codigoFonte != null ? codigoFonte.length() : "null"));
        System.out.println("DEBUG: Arquivo atual: " + (arquivoAtual != null ? arquivoAtual.getAbsolutePath() : "null"));

        if (codigoFonte == null || codigoFonte.trim().isEmpty()) {
            if (painelConsole != null && painelConsole.obterConsole() != null) {
                painelConsole.obterConsole().appendText("[ERRO] Nenhum código fonte para compilar.\n");
            }
            Alert alerta = new Alert(Alert.AlertType.WARNING);
            alerta.setTitle("Aviso");
            alerta.setHeaderText("Nenhum código para compilar");
            alerta.setContentText("O editor está vazio ou o código não foi carregado.");
            alerta.showAndWait();
            return;
        }

        // Determina nome do arquivo
        String nomeArquivo = "output";
        if (arquivoAtual != null) {
            String nome = arquivoAtual.getName();
            int ultimoPonto = nome.lastIndexOf('.');
            if (ultimoPonto > 0) {
                nomeArquivo = nome.substring(0, ultimoPonto);
            } else {
                nomeArquivo = nome;
            }
        }

        // Determina diretório de saída
        // Se o arquivo estiver salvo, usa o diretório dele. Caso contrário, usa um
        // padrão.
        Path diretorioSaida;
        if (arquivoAtual != null && arquivoAtual.getParentFile() != null) {
            diretorioSaida = arquivoAtual.getParentFile().toPath().resolve("out");
        } else {
            String diretorioUsuario = System.getProperty("user.dir");
            diretorioSaida = Paths.get(diretorioUsuario, "out");
        }

        System.out.println("DEBUG: Diretório de saída alvo: " + diretorioSaida.toAbsolutePath());

        if (painelConsole != null && painelConsole.obterConsole() != null) {
            painelConsole.obterConsole().appendText("Arquivo alvo: " + nomeArquivo + "\n");
            painelConsole.obterConsole().appendText("Diretório de saída: " + diretorioSaida.toAbsolutePath() + "\n");
        }

        try {
            CompiladorLC compilador = new CompiladorLC();
            System.out.println("DEBUG: Chamando compilador...");

            com.editor_texto.nyx.compiler.api.ResultadoCompilacao resultado = compilador.compilar(codigoFonte,
                    diretorioSaida);

            if (resultado.isSucesso()) {
                String mensagem = "Compilação concluída! " + resultado.getArquivoAssemblyGerado().toString();
                System.out.println("DEBUG: Sucesso: " + mensagem);

                if (painelConsole != null && painelConsole.obterConsole() != null) {
                    painelConsole.obterConsole().appendText("[SUCESSO] " + mensagem + "\n");
                    for (String aviso : resultado.getAvisos()) {
                        painelConsole.obterConsole().appendText("[AVISO] " + aviso + "\n");
                    }
                }

                Alert alerta = new Alert(Alert.AlertType.INFORMATION);
                alerta.setTitle("Sucesso");
                alerta.setHeaderText("Compilação Concluída");
                alerta.setContentText(mensagem);
                alerta.show();
            } else {
                if (painelConsole != null && painelConsole.obterConsole() != null) {
                    painelConsole.obterConsole().appendText("[FALHA] Erros de compilação:\n");
                    for (com.editor_texto.nyx.compiler.api.ErroCompilacao erro : resultado.getErros()) {
                        painelConsole.obterConsole().appendText(erro.toString() + "\n");
                    }
                }

                StringBuilder sb = new StringBuilder();
                for (com.editor_texto.nyx.compiler.api.ErroCompilacao erro : resultado.getErros()) {
                    sb.append(erro.toString()).append("\n");
                }

                Alert alerta = new Alert(Alert.AlertType.ERROR);
                alerta.setTitle("Erro de Compilação");
                alerta.setHeaderText("Falha ao compilar");
                alerta.setContentText(sb.toString());
                alerta.showAndWait();
            }

        } catch (

        Exception e) {
            System.err.println("DEBUG: Exceção inesperada durante compilação:");
            e.printStackTrace();

            if (painelConsole != null && painelConsole.obterConsole() != null) {
                painelConsole.obterConsole().appendText("[FALHA] Erro de compilação:\n");
                painelConsole.obterConsole().appendText(e.getMessage() + "\n");
            }

            Alert alerta = new Alert(Alert.AlertType.ERROR);
            alerta.setTitle("Erro de Compilação");
            alerta.setHeaderText("Falha ao compilar");
            alerta.setContentText(e.getMessage());
            alerta.showAndWait();
        }

        if (painelConsole != null && painelConsole.obterConsole() != null) {
            painelConsole.obterConsole().appendText("--------------------------------------------------\n");
        }
    }
}
