package com.example.inventarioperuzzoandroid;

import static com.example.inventarioperuzzoandroid.MainActivity.inventarioBipadosProdutos;
import static com.example.inventarioperuzzoandroid.MainActivity.inventarioBipadosTotal;

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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import android.util.Log;

public class Inventario extends AppCompatActivity {

    // Informações que interagem com o usuário
    TextView mValueShowLoja;
    EditText mInputEAN;
    EditText mInputQtd;
    TextView mValueShowInventarioProdutos;
    TextView mValueShowInventarioTotal;
    TextView mValueShowEAN;
    TextView mValueshowQtd;
    Button mAdicionarBtn;
    Button mModificarBtn;
    Button mMostrarColetasBtn;
    TextView mFinalizarLink; // Alterado de Button para TextView

    // Variáveis internas
    String mTextinputEAN;
    String mTextinputQtd;
    Double mTextinputQtdDouble;
    Double mTextinputQtdDoubleRaw;
    private String lojaLocal;

    // Caminhos dos arquivos
    public static File path = Environment.getExternalStorageDirectory();
    public static File fullpath = new File(path, "InventarioPeruzzoAndroid");
    private static String fileNameInventario = "inventario.txt";
    private static File fullPathFileNameInventario = new File(fullpath, fileNameInventario);
    private static String fileNameUtilsInventario = "utilsInventario.txt";
    private static File fullPathFileNameUtilsInventario = new File(fullpath, fileNameUtilsInventario);

    // Map de coletas
    public static Map<String, Double> mapEANQtdInventario = new HashMap<>();
    public static Map<String, Double> mapEANQtdInventarioVirgula = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTitle("Inventário");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inventario);

        // Lê a loja salva
        SharedPreferences prefs = getSharedPreferences("InventarioPrefs", MODE_PRIVATE);
        lojaLocal = prefs.getString("loja", "");

        // Lê o conteúdo prévio salvo
        readerInventarioTxtToMapInventario();
        readerUtilsTxtToMapInventario();

        // Inicializa as views
        mValueShowLoja = findViewById(R.id.showloja);
        mValueShowInventarioProdutos = findViewById(R.id.showInventarioProdutos);
        mValueShowInventarioTotal = findViewById(R.id.showInventarioTotal);
        mValueShowEAN = findViewById(R.id.showEAN);
        mValueshowQtd = findViewById(R.id.showQtd);
        mInputEAN = findViewById(R.id.inputEAN);
        mInputQtd = findViewById(R.id.inputQtd);
        mAdicionarBtn = findViewById(R.id.adicionarBtn);
        mModificarBtn = findViewById(R.id.modificarBtn);
        mMostrarColetasBtn = findViewById(R.id.mostrarColetasBtn);
       // No onCreate(), inicialize a variável:
    mFinalizarLink = findViewById(R.id.finalizarLink);

        // Exibe a loja e contadores
        mValueShowLoja.setText("Loja: " + lojaLocal);
        mValueShowInventarioProdutos.setText("Produtos: " + inventarioBipadosProdutos);
        mValueShowInventarioTotal.setText("Total: " + inventarioBipadosTotal);

        // Evento ao inserir quantidade
        mInputQtd.setOnEditorActionListener((v, actionId, event) -> {
            if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER))
                    || (actionId == EditorInfo.IME_ACTION_DONE)) {
                inputHandler(mInputEAN.getText().toString().trim(), mInputQtd.getText().toString().trim());
            }
            return false;
        });

        // Botão Adicionar
        mAdicionarBtn.setOnClickListener(v -> {
            inputHandler(mInputEAN.getText().toString().trim(), mInputQtd.getText().toString().trim());
        });

        // Botão Modificar
        mModificarBtn.setOnClickListener(v -> {
            mInputEAN.requestFocus();
            mInputQtd.setText("-");
            mInputQtd.setSelection(1);
        });

        // Botão Mostrar Coletas
        mMostrarColetasBtn.setOnClickListener(v -> {
            mostrarColetas();
        });

        // Definir evento de clique no link
        mFinalizarLink.setOnClickListener(v -> {
            saveInventarioToTxtMapInventario();
            saveUtilsToTxtMapInventario();
            InventarioSeguro inventarioSeguro = new InventarioSeguro(this);
            inventarioSeguro.exportarInventarioProtegido(mapEANQtdInventario);
            Toast.makeText(this, "Inventário finalizado e exportado!", Toast.LENGTH_LONG).show();
        });
        mInputEAN.requestFocus();
    }

    private void mostrarColetas() {
        StringBuilder coletaStr = new StringBuilder("Coletas:\n");
        for (Map.Entry<String, Double> entry : mapEANQtdInventario.entrySet()) {
            coletaStr.append("EAN: ").append(entry.getKey())
                    .append(" | Quantidade: ").append(entry.getValue()).append("\n");
        }
        Toast.makeText(this, coletaStr.toString(), Toast.LENGTH_LONG).show();
    }

    public static void readerInventarioTxtToMapInventario() {
        try (BufferedReader br = new BufferedReader(new FileReader(fullPathFileNameInventario))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] lineArray = line.split(";");
                mapEANQtdInventario.put(lineArray[1], Double.parseDouble(lineArray[2].replace(",", ".")));
            }
        } catch (Exception ignored) {}
    }

    public static void readerUtilsTxtToMapInventario() {
        try {
            BufferedReader br = new BufferedReader(new FileReader(fullPathFileNameUtilsInventario));
            String line;
            while ((line = br.readLine()) != null) {
                String[] lineArray = line.split(";");
                if (lineArray.length >= 4) {
                    inventarioBipadosProdutos = Integer.parseInt(lineArray[1]);
                    inventarioBipadosTotal = Integer.parseInt(lineArray[3]);
                }
            }
            br.close();
        } catch (Exception ignored) {}
    }

    private void saveInventarioToTxtMapInventario() {
        try (PrintWriter pw = new PrintWriter(new FileWriter(fullPathFileNameInventario, false))) {
            for (Map.Entry<String, Double> entry : mapEANQtdInventario.entrySet()) {
                pw.println(lojaLocal + ";" + entry.getKey() + ";" + entry.getValue().toString().replace(".", ","));
            }
        } catch (IOException ignored) {}
    }

    private void saveUtilsToTxtMapInventario() {
        try (PrintWriter pw = new PrintWriter(new FileWriter(fullPathFileNameUtilsInventario, false))) {
            inventarioBipadosProdutos = mapEANQtdInventario.size();
            pw.println("Produtos;" + inventarioBipadosProdutos + ";Total;" + inventarioBipadosTotal);
        } catch (IOException ignored) {}
    }
    private void inputHandler(String ean, String qtd) {
        if (ean.isEmpty() || qtd.isEmpty()) {
            Toast.makeText(this, "Preencha todos os campos.", Toast.LENGTH_SHORT).show();
            return;
        }
    
        try {
            mTextinputQtdDoubleRaw = Double.parseDouble(qtd.replace(",", "."));
            mTextinputQtdDouble = Math.round(mTextinputQtdDoubleRaw * 1000.0) / 1000.0;
    
            mapEANQtdInventario.put(ean, mapEANQtdInventario.getOrDefault(ean, 0.0) + mTextinputQtdDouble);
            inventarioBipadosTotal++;
            inventarioBipadosProdutos = mapEANQtdInventario.size();
    
            mValueShowInventarioProdutos.setText("Produtos: " + inventarioBipadosProdutos);
            mValueShowInventarioTotal.setText("Total: " + inventarioBipadosTotal);
            mValueShowEAN.setText("Código: " + ean);
            mValueshowQtd.setText("Quantidade: " + mapEANQtdInventario.get(ean));
    
            mInputEAN.getText().clear();
            mInputQtd.getText().clear();
            mInputEAN.requestFocus();
        } catch (Exception e) {
            Toast.makeText(this, "Erro ao processar entrada.", Toast.LENGTH_SHORT).show();
        }
    }
    
}
