package com.example.inventarioperuzzoandroid;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

public class InventarioSeguro {

    private Context context;
    private static final String AES_KEY = "0123456789abcdef"; // Chave fixa para criptografia

    public InventarioSeguro(Context context) {
        this.context = context;
    }

    // ======================== EXPORTAÇÃO DE INVENTÁRIO ======================== //

    public void exportarInventarioProtegido(Map<String, Double> mapEANQtdInventario) {
        exportarArquivoProtegido(mapEANQtdInventario, "inventario_colaborador.txt", "inventario_sap.txt");
    }

    // ======================== EXPORTAÇÃO DE RUPTURA ======================== //

    public void exportarRupturaProtegida(Map<String, Double> mapEANQtdRuptura) {
        exportarArquivoProtegido(mapEANQtdRuptura, "ruptura_colaborador.txt", "ruptura_sap.txt");
    }

    // ======================== MÉTODO GENÉRICO PARA EXPORTAR ======================== //

    private void exportarArquivoProtegido(Map<String, Double> dados, String nomeArquivoColaborador, String nomeArquivoSAP) {
        try {
            SharedPreferences prefs = context.getSharedPreferences("InventarioPrefs", Context.MODE_PRIVATE);
            final String lojaExport = prefs.getString("loja", "");

            File fullpath = new File(Environment.getExternalStorageDirectory(), "InventarioPeruzzoAndroid");
            if (!fullpath.exists()) fullpath.mkdirs();

            File pastaOculta = new File(fullpath, ".pasta_protegida");
            if (!pastaOculta.exists()) pastaOculta.mkdirs();

            File noMedia = new File(pastaOculta, ".nomedia");
            try {
                noMedia.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }

            File arquivoColaborador = new File(fullpath, nomeArquivoColaborador);
            PrintWriter pwColaborador = new PrintWriter(new FileWriter(arquivoColaborador, false));

            File arquivoSAP = new File(pastaOculta, nomeArquivoSAP);
            PrintWriter pwSap = new PrintWriter(new FileWriter(arquivoSAP, false));

            for (Map.Entry<String, Double> entry : dados.entrySet()) {
                String linhaOriginal = lojaExport + ";" + entry.getKey() + ";" + entry.getValue().toString().replace(".", ",");

                // Criptografia para SAP
                String linhaCriptografada = encrypt(linhaOriginal, AES_KEY);
                pwSap.println(linhaCriptografada);

                // Codificação Base64 para Colaborador
                String linhaCodificada = Base64.encodeToString(linhaOriginal.getBytes(), Base64.NO_WRAP);
                pwColaborador.println(linhaCodificada);
            }

            pwSap.close();
            pwColaborador.close();

            Toast.makeText(context, "Arquivo exportado com segurança.", Toast.LENGTH_SHORT).show();
            Log.d("EXPORT", "Arquivo " + nomeArquivoColaborador + " e " + nomeArquivoSAP + " exportados com sucesso.");

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(context, "Erro ao exportar arquivo.", Toast.LENGTH_SHORT).show();
            Log.e("EXPORT", "Erro ao exportar arquivos", e);
        }
    }

    // ======================== LEITURA DO ARQUIVO COLABORADOR ======================== //

    public Map<String, String> lerArquivoCodificado(String nomeArquivo) {
        Map<String, String> dados = new HashMap<>();
        try {
            File fullpath = new File(Environment.getExternalStorageDirectory(), "InventarioPeruzzoAndroid");
            File arquivo = new File(fullpath, nomeArquivo);

            if (!arquivo.exists()) {
                Log.e("LEITURA", "Arquivo " + nomeArquivo + " não encontrado.");
                return dados;
            }

            BufferedReader br = new BufferedReader(new FileReader(arquivo));
            String line;

            while ((line = br.readLine()) != null) {
                byte[] decodedBytes = Base64.decode(line, Base64.NO_WRAP);
                String linhaDecodificada = new String(decodedBytes);
                String[] partes = linhaDecodificada.split(";");
                if (partes.length == 3) {
                    dados.put(partes[1], partes[2]);
                }
            }
            br.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return dados;
    }

    // ======================== LEITURA DO ARQUIVO SAP ======================== //

    public String lerArquivoSAP(String nomeArquivo) {
        try {
            File pastaOculta = new File(Environment.getExternalStorageDirectory(), "InventarioPeruzzoAndroid/.pasta_protegida");
            File arquivo = new File(pastaOculta, nomeArquivo);

            if (!arquivo.exists()) {
                Log.e("LEITURA", "Arquivo " + nomeArquivo + " não encontrado.");
                return null;
            }

            BufferedReader br = new BufferedReader(new FileReader(arquivo));
            StringBuilder conteudo = new StringBuilder();
            String line;

            while ((line = br.readLine()) != null) {
                conteudo.append(decrypt(line, AES_KEY)).append("\n");
            }
            br.close();

            return conteudo.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // ======================== CRIPTOGRAFIA E DESCRIPTOGRAFIA ======================== //

    private static String encrypt(String data, String key) throws Exception {
        Key aesKey = new SecretKeySpec(key.getBytes(), "AES");
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, aesKey);
        byte[] encrypted = cipher.doFinal(data.getBytes(StandardCharsets.UTF_8));
        return Base64.encodeToString(encrypted, Base64.NO_WRAP);
    }

    private static String decrypt(String encryptedData, String key) throws Exception {
        Key aesKey = new SecretKeySpec(key.getBytes(), "AES");
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.DECRYPT_MODE, aesKey);
        byte[] decodedBytes = Base64.decode(encryptedData, Base64.NO_WRAP);
        return new String(cipher.doFinal(decodedBytes), StandardCharsets.UTF_8);
    }
}
