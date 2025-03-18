package com.example.inventarioperuzzoandroid;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Map;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

public class InventarioSeguro {

    private Context context;
    private static final String AES_KEY = "SAPCriptoKey123"; // 🔑 Chave segura para o SAP
    private static final String SALT = "SAPCriptoSalt123";

    public InventarioSeguro(Context context) {
        this.context = context;
    }

    // ======================== EXPORTAÇÃO DO INVENTÁRIO (CRIPTOGRAFADO) ======================== //
    public void exportarInventarioProtegido(Map<String, Double> mapEANQtdInventario) {
        exportarArquivoProtegido(mapEANQtdInventario, "inventario_sap.txt");
    }

    // ======================== EXPORTAÇÃO DA RUPTURA (CRIPTOGRAFADO) ======================== //
    public void exportarRupturaProtegida(Map<String, Double> mapEANQtdRuptura) {
        exportarArquivoProtegido(mapEANQtdRuptura, "ruptura_sap.txt");
    }

    // ======================== MÉTODO GENÉRICO PARA EXPORTAR (CRIPTOGRAFADO) ======================== //
    private void exportarArquivoProtegido(Map<String, Double> dados, String nomeArquivoSAP) {
        try {
            SharedPreferences prefs = context.getSharedPreferences("InventarioPrefs", Context.MODE_PRIVATE);
            final String lojaExport = prefs.getString("loja", "");
    
            File pastaOculta = new File(Environment.getExternalStorageDirectory(), "InventarioPeruzzoAndroid/.pasta_protegida");
            if (!pastaOculta.exists()) pastaOculta.mkdirs();
    
            File arquivoSAP = new File(pastaOculta, nomeArquivoSAP);
            PrintWriter pwSap = new PrintWriter(new FileWriter(arquivoSAP, false));
    
            for (Map.Entry<String, Double> entry : dados.entrySet()) {
                String linhaOriginal = lojaExport + ";" + entry.getKey() + ";" + entry.getValue().toString().replace(".", ",");
    
                // 🔒 Criptografando a linha antes de salvar
                String linhaCriptografada = encrypt(linhaOriginal);
                pwSap.println(linhaCriptografada);
            }
    
            pwSap.close();
    
            // 🔹 Atualizar UI na Thread Principal para evitar crash
            if (context instanceof Ruptura) {
                ((Ruptura) context).runOnUiThread(() -> 
                    Toast.makeText(context, "Arquivo '" + nomeArquivoSAP + "' exportado com segurança!", Toast.LENGTH_SHORT).show()
                );
            } else if (context instanceof Inventario) {
                ((Inventario) context).runOnUiThread(() -> 
                    Toast.makeText(context, "Arquivo '" + nomeArquivoSAP + "' exportado com segurança!", Toast.LENGTH_SHORT).show()
                );
            }
    
            Log.d("EXPORT", "Arquivo " + nomeArquivoSAP + " exportado criptografado.");
    
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("EXPORT", "Erro ao exportar arquivo criptografado", e);
        }
    }
    
    // ======================== CRIPTOGRAFIA ======================== //
    private static String encrypt(String data) throws Exception {
        SecureRandom secureRandom = new SecureRandom();
        byte[] iv = new byte[16];
        secureRandom.nextBytes(iv);
        IvParameterSpec ivSpec = new IvParameterSpec(iv);

        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        PBEKeySpec spec = new PBEKeySpec(AES_KEY.toCharArray(), SALT.getBytes(), 65536, 256);
        SecretKey tmp = factory.generateSecret(spec);
        SecretKeySpec secretKey = new SecretKeySpec(tmp.getEncoded(), "AES");

        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivSpec);

        byte[] encrypted = cipher.doFinal(data.getBytes(StandardCharsets.UTF_8));
        byte[] encryptedWithIv = new byte[iv.length + encrypted.length];

        System.arraycopy(iv, 0, encryptedWithIv, 0, iv.length);
        System.arraycopy(encrypted, 0, encryptedWithIv, iv.length, encrypted.length);

        return Base64.encodeToString(encryptedWithIv, Base64.NO_WRAP);
    }
}
