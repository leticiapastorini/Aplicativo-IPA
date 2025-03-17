package com.example.inventarioperuzzoandroid;

import static com.example.inventarioperuzzoandroid.MainActivity.rupturaBipadosProdutos;
import static com.example.inventarioperuzzoandroid.MainActivity.rupturaBipadosTotal;
import static com.example.inventarioperuzzoandroid.Ruptura.mapEANQtdRupturaVirgula;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class Ruptura extends AppCompatActivity {

    // Views
    TextView mValueShowLoja, mValueShowRupturaProdutos, mValueShowRupturaTotal, mValueShowEAN, mValueshowQtd, mFinalizarRuptura;
    EditText mInputEAN, mInputQtd;
    Button mAdicionarBtn, mModificarBtn, mMostrarColetasBtn;

    // Variáveis
    private String lojaLocal;
    private String lastQtdMap;
    private Double lastQtdMapDouble;
    private Double mTextinputQtdDouble;

    // Caminho dos arquivos
    public static File path = Environment.getExternalStorageDirectory();
    public static File fullpath = new File(path + "/InventarioPeruzzoAndroid");
    private static final String fileNameRuptura = "ruptura.txt";
    private static final File fullPathFileNameRuptura = new File(fullpath, fileNameRuptura);
    private static final String fileNameUtilsRuptura = "utilsRuptura.txt";
    private static final File fullPathFileNameUtilsRuptura = new File(fullpath, fileNameUtilsRuptura);
    public static Map<String, Double> mapEANQtdRupturaVirgula = new HashMap<>();

    // Mapas para armazenar os produtos em ruptura
    public static Map<String, Double> mapEANQtdRuptura = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTitle("Ruptura");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ruptura);

        // Ler loja salva no SharedPreferences
        SharedPreferences prefs = getSharedPreferences("InventarioPrefs", MODE_PRIVATE);
        lojaLocal = prefs.getString("loja", "");

        // Ler dados dos arquivos
        readerRupturaTxtToMapRuptura();
        readerUtilsTxtToMapRuptura();

        // Inicialização das Views
        mValueShowLoja = findViewById(R.id.showloja);
        mValueShowRupturaProdutos = findViewById(R.id.showRupturaProdutos);
        mValueShowRupturaTotal = findViewById(R.id.showRupturaTotal);
        mValueShowEAN = findViewById(R.id.showEAN);
        mValueshowQtd = findViewById(R.id.showQtd);
        mInputEAN = findViewById(R.id.inputEAN);
        mInputQtd = findViewById(R.id.inputQtd);
        mAdicionarBtn = findViewById(R.id.adicionarBtn);
        mModificarBtn = findViewById(R.id.modificarBtn);
        mMostrarColetasBtn = findViewById(R.id.mostrarColetasBtn);
        mFinalizarRuptura = findViewById(R.id.finalizarRupturaLink);

        // Exibir loja e contadores na tela
        mValueShowLoja.setText("Loja: " + lojaLocal);
        mValueShowRupturaProdutos.setText("Produtos: " + rupturaBipadosProdutos);
        mValueShowRupturaTotal.setText("Total: " + rupturaBipadosTotal);

        // Ajustar campo de quantidade
        mInputQtd.setText("1");
        mInputQtd.setFocusable(false);
        mInputQtd.setEnabled(false);

        // Configuração de botões
        mMostrarColetasBtn.setOnClickListener(v -> mostrarColetas());

        mFinalizarRuptura.setOnClickListener(v -> {
            saveRupturaToTxtMapRuptura();
            saveUtilsToTxtMapRuptura();
            InventarioSeguro inventarioSeguro = new InventarioSeguro(this);
            inventarioSeguro.exportarRupturaProtegida(mapEANQtdRuptura);
            Toast.makeText(this, "Ruptura finalizada e exportada!", Toast.LENGTH_LONG).show();
        });

        mInputEAN.setOnEditorActionListener((v, actionId, event) -> {
            if ((event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER) || (actionId == EditorInfo.IME_ACTION_DONE)) {
                processarEntrada();
            }
            return false;
        });

        mAdicionarBtn.setOnClickListener(v -> processarEntrada());

        mModificarBtn.setOnClickListener(v -> {
            mInputQtd.setText("-1");
            mInputQtd.setSelection(2);
        });


        mInputEAN.requestFocus();
    }

    private void processarEntrada() {
        String ean = mInputEAN.getText().toString().trim();
        String qtd = "1"; // Quantidade sempre fixa como 1

        if (ean.isEmpty()) {
            Toast.makeText(this, "Por favor, insira um código de barras!", Toast.LENGTH_SHORT).show();
            return;
        }

        inputHandler(ean, qtd);
        saveRupturaToTxtMapRuptura();
        saveUtilsToTxtMapRuptura();

        mValueShowRupturaProdutos.setText("Produtos: " + rupturaBipadosProdutos);
        mValueShowRupturaTotal.setText("Total: " + rupturaBipadosTotal);
        mValueShowEAN.setText("Código: " + ean);
        mValueshowQtd.setText("Quantidade: " + mapEANQtdRuptura.getOrDefault(ean, 0.0));

        mInputEAN.getText().clear();
        mInputQtd.setText("1");
        mInputEAN.requestFocus();
    }

    private void mostrarColetas() {
        if (mapEANQtdRuptura.isEmpty()) {
            Toast.makeText(this, "Nenhum produto em ruptura encontrado.", Toast.LENGTH_SHORT).show();
            return;
        }

        StringBuilder coletaStr = new StringBuilder("Produtos em Ruptura:\n");
        for (Map.Entry<String, Double> entry : mapEANQtdRuptura.entrySet()) {
            coletaStr.append("EAN: ").append(entry.getKey())
                     .append(" | Quantidade: ").append(entry.getValue()).append("\n");
        }

        Toast.makeText(this, coletaStr.toString(), Toast.LENGTH_LONG).show();
    }

    private void inputHandler(String ean, String qtd) {
        mTextinputQtdDouble = Double.parseDouble(qtd);
        mapEANQtdRuptura.put(ean, mapEANQtdRuptura.getOrDefault(ean, 0.0) + mTextinputQtdDouble);
        rupturaBipadosTotal++;
    }
    private void saveUtilsToTxtMapRuptura() {
        try {
            fullpath.mkdirs();
            FileWriter fw = new FileWriter(fullPathFileNameUtilsRuptura, false);
            PrintWriter pw = new PrintWriter(fw);
    
            rupturaBipadosProdutos = mapEANQtdRuptura.size();
            pw.println("Produtos;" + rupturaBipadosProdutos + ";Total;" + rupturaBipadosTotal);
            pw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    private void readerRupturaTxtToMapRuptura() {
        try (BufferedReader br = new BufferedReader(new FileReader(fullPathFileNameRuptura))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] lineArray = line.split(";");
                mapEANQtdRuptura.put(lineArray[1], Double.parseDouble(lineArray[2].replace(",", ".")));
            }
        } catch (Exception ignored) {}
    }

    private void readerUtilsTxtToMapRuptura() {
        try (BufferedReader br = new BufferedReader(new FileReader(fullPathFileNameUtilsRuptura))) {
            String[] lineArray = br.readLine().split(";");
            rupturaBipadosProdutos = Integer.parseInt(lineArray[1]);
            rupturaBipadosTotal = Integer.parseInt(lineArray[3]);
        } catch (Exception ignored) {}
    }

    private void saveRupturaToTxtMapRuptura() {
        try (PrintWriter pw = new PrintWriter(new FileWriter(fullPathFileNameRuptura, false))) {
            for (Map.Entry<String, Double> entry : mapEANQtdRuptura.entrySet()) {
                pw.println(lojaLocal + ";" + entry.getKey() + ";" + entry.getValue().toString().replace(".", ","));
            }
        } catch (IOException ignored) {}
    }
}
