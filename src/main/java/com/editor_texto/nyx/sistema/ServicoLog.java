package com.editor_texto.nyx.sistema;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Serviço centralizado de logs.
 * Permite que qualquer componente do sistema emita logs sem conhecer a UI.
 * A UI (PainelConsole) se inscreve para receber esses logs.
 */
public class ServicoLog {

    public enum TipoLog {
        INFO, AVISO, ERRO, SUCESSO
    }

    public static class LogItem {
        public final LocalDateTime dataHora;
        public final TipoLog tipo;
        public final String mensagem;

        public LogItem(TipoLog tipo, String mensagem) {
            this.dataHora = LocalDateTime.now();
            this.tipo = tipo;
            this.mensagem = mensagem;
        }

        @Override
        public String toString() {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
            return String.format("[%s] [%s] %s", dataHora.format(formatter), tipo, mensagem);
        }
    }

    private static final List<Consumer<LogItem>> ouvintes = new ArrayList<>();

    // Adiciona um ouvinte para receber novos logs
    public static void adicionarOuvinte(Consumer<LogItem> ouvinte) {
        synchronized (ouvintes) {
            ouvintes.add(ouvinte);
        }
    }

    public static void removerOuvinte(Consumer<LogItem> ouvinte) {
        synchronized (ouvintes) {
            ouvintes.remove(ouvinte);
        }
    }

    // Métodos utilitários para logar
    public static void info(String mensagem) {
        emitirLog(TipoLog.INFO, mensagem);
    }

    public static void aviso(String mensagem) {
        emitirLog(TipoLog.AVISO, mensagem);
    }

    public static void erro(String mensagem) {
        emitirLog(TipoLog.ERRO, mensagem);
    }

    public static void sucesso(String mensagem) {
        emitirLog(TipoLog.SUCESSO, mensagem);
    }

    private static void emitirLog(TipoLog tipo, String mensagem) {
        LogItem item = new LogItem(tipo, mensagem);

        // Dispara os ouvintes (sempre na Thread JavaFX se for atualização de UI,
        // mas aqui mantemos agnóstico. O assinante que decida se precisa de
        // Platform.runLater
        // OU, para segurança, podemos garantir que o log seja despachado de forma
        // segura).

        // Decisão: logs podem vir de threads de background (Pipeline).
        // O ouvinte (UI) provavelmente precisa rodar na FX Thread.
        // Vamos deixar o ouvinte tratar isso ou rodar aqui?
        // Para simplificar a vida de quem implementa a UI, vamos usar Platform.runLater
        // se houver contexto FX.
        // Mas como essa classe é 'sistema', melhor deixar o ouvinte decidir.

        synchronized (ouvintes) {
            for (Consumer<LogItem> ouvinte : ouvintes) {
                try {
                    ouvinte.accept(item);
                } catch (Exception e) {
                    System.err.println("Erro ao entregar log: " + e.getMessage());
                }
            }
        }

        // Também imprime no stdout para debug
        System.out.println(item.toString());
    }
}
