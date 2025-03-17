package com.example.inventarioperuzzoandroid;

import android.os.Environment;
import android.util.Log;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class LogBipagens {

    private static final int MAX_ENTRADAS = 100; // Define o limite de registros no log

    /**
     * Salva a bipagem em um log separado.
     * @param ean Código do produto bipado.
     * @param quantidade Quantidade bipada.
     * @param isInventario Define se a bipagem foi no Inventário (true) ou na Ruptura (false).
     */
    public static void salvarLogBipagem(String ean, double quantidade, boolean isInventario) {
        try {
            File fullpath = new File(Environment.getExternalStorageDirectory(), "InventarioPeruzzoAndroid");
            if (!fullpath.exists()) fullpath.mkdirs();

            // Define o nome do arquivo de log
            File logFile = new File(fullpath, isInventario ? "logs_inventario.txt" : "logs_ruptura.txt");

            // Lê os logs existentes
            List<String> linhas = new ArrayList<>();
            if (logFile.exists()) {
                BufferedReader br = new BufferedReader(new FileReader(logFile));
                String line;
                while ((line = br.readLine()) != null) {
                    linhas.add(line);
                }
                br.close();
            }

            // Adiciona a nova bipagem no log
            String novaEntrada = obterDataHoraAtual() + " - EAN: " + ean + " | Quantidade: " + quantidade;
            linhas.add(novaEntrada);

            // Mantém o tamanho máximo de entradas
            if (linhas.size() > MAX_ENTRADAS) {
                linhas = linhas.subList(linhas.size() - MAX_ENTRADAS, linhas.size());
            }

            // Salva o log atualizado
            PrintWriter pw = new PrintWriter(new FileWriter(logFile, false));
            for (String linha : linhas) {
                pw.println(linha);
            }
            pw.close();

        } catch (IOException e) {
            Log.e("LOG_BIPAGEM", "Erro ao salvar o log de bipagem", e);
        }
    }

    /**
     * Lê o log e retorna as últimas bipagens registradas.
     * @param isInventario Define se é o log do Inventário (true) ou Ruptura (false).
     * @return Lista com as últimas bipagens registradas.
     */
    public static List<String> lerLogBipagens(boolean isInventario) {
        List<String> registros = new ArrayList<>();
        try {
            File logFile = new File(Environment.getExternalStorageDirectory(), "InventarioPeruzzoAndroid/" +
                    (isInventario ? "logs_inventario.txt" : "logs_ruptura.txt"));

            if (!logFile.exists()) return registros;

            BufferedReader br = new BufferedReader(new FileReader(logFile));
            String line;
            while ((line = br.readLine()) != null) {
                registros.add(line);
            }
            br.close();
        } catch (IOException e) {
            Log.e("LOG_BIPAGEM", "Erro ao ler o log de bipagem", e);
        }
        return registros;
    }

    /**
     * Obtém a data e hora atual formatada.
     * @return String da data e hora atual no formato dd/MM/yyyy HH:mm:ss.
     */
    private static String obterDataHoraAtual() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault());
        return sdf.format(new Date());
    }
}
