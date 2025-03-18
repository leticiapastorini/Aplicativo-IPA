package com.example.inventarioperuzzoandroid;

import static com.example.inventarioperuzzoandroid.MainActivity.inventarioBipadosProdutos;
import static com.example.inventarioperuzzoandroid.MainActivity.inventarioBipadosTotal;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class Inventario extends AppCompatActivity {

    // UI Elements
    TextView mValueShowLoja, mValueShowInventarioProdutos, mValueShowInventarioTotal, mValueShowEAN, mValueshowQtd;
    EditText mInputEAN, mInputQtd;
    Button mAdicionarBtn, mModificarBtn, mMostrarColetasBtn;
    TextView mFinalizarLink;
    private String lojaLocal;

    // File Paths
    public static File path = Environment.getExternalStorageDirectory();
    public static File fullpath = new File(path, "InventarioPeruzzoAndroid");
    private static final String fileNameInventario = "inventario.txt";
    private static final File fullPathFileNameInventario = new File(fullpath, fileNameInventario);
    private static final String fileNameUtilsInventario = "utilsInventario.txt";
    private static final File fullPathFileNameUtilsInventario = new File(fullpath, fileNameUtilsInventario);
    public static Map<String, Double> mapEANQtdInventarioVirgula = new HashMap<>();

    // Maps
    public static Map<String, Double> mapEANQtdInventario = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTitle("Inventário");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inventario);

        SharedPreferences prefs = getSharedPreferences("InventarioPrefs", MODE_PRIVATE);
        lojaLocal = prefs.getString("loja", "");

        // Restaurando os métodos perdidos
        readerInventarioTxtToMapInventario();
        readerUtilsTxtToMapInventario();

        // UI Binding
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
        mFinalizarLink = findViewById(R.id.finalizarLink);

        atualizarUI();

        // Event Listeners
        mAdicionarBtn.setOnClickListener(v -> adicionarProduto());
        mModificarBtn.setOnClickListener(v -> reduzirQuantidade());
        mMostrarColetasBtn.setOnClickListener(v -> mostrarColetas());
        mFinalizarLink.setOnClickListener(v -> finalizarInventario());

        mInputEAN.requestFocus();
    }

    private void adicionarProduto() {
        String ean = mInputEAN.getText().toString().trim();
        String qtdStr = mInputQtd.getText().toString().trim();

        if (ean.isEmpty() || qtdStr.isEmpty()) {
            Toast.makeText(this, "Preencha os campos corretamente!", Toast.LENGTH_SHORT).show();
            return;
        }

        double qtdDouble = Double.parseDouble(qtdStr.replace(",", "."));
        mapEANQtdInventario.put(ean, mapEANQtdInventario.getOrDefault(ean, 0.0) + qtdDouble);

        saveInventarioToTxtMapInventario();
        saveUtilsToTxtMapInventario();

        atualizarUI();
        mInputEAN.getText().clear();
        mInputQtd.getText().clear();
        mInputEAN.requestFocus();
    }

    private void reduzirQuantidade() {
        String ean = mInputEAN.getText().toString().trim();
        String qtdStr = mInputQtd.getText().toString().trim();

        if (ean.isEmpty() || qtdStr.isEmpty()) {
            Toast.makeText(this, "Preencha os campos corretamente!", Toast.LENGTH_SHORT).show();
            return;
        }

        double qtdReduzir = Double.parseDouble(qtdStr.replace(",", "."));
        mapEANQtdInventario.put(ean, Math.max(0, mapEANQtdInventario.getOrDefault(ean, 0.0) - qtdReduzir));

        saveInventarioToTxtMapInventario();
        saveUtilsToTxtMapInventario();

        atualizarUI();
        mInputEAN.getText().clear();
        mInputQtd.getText().clear();
        mInputEAN.requestFocus();
    }

    private void mostrarColetas() {
        StringBuilder coletaStr = new StringBuilder("Coletas:\n");
        for (Map.Entry<String, Double> entry : mapEANQtdInventario.entrySet()) {
            coletaStr.append("EAN: ").append(entry.getKey()).append(" | Quantidade: ").append(entry.getValue()).append("\n");
        }
        Toast.makeText(this, coletaStr.toString(), Toast.LENGTH_LONG).show();
    }

    private void finalizarInventario() {
        saveInventarioToTxtMapInventario();
        saveUtilsToTxtMapInventario();
        Toast.makeText(this, "Inventário finalizado e exportado!", Toast.LENGTH_LONG).show();
    }

    private void saveInventarioToTxtMapInventario() {
        try (PrintWriter pw = new PrintWriter(new FileWriter(fullPathFileNameInventario, false))) {
            for (Map.Entry<String, Double> entry : mapEANQtdInventario.entrySet()) {
                pw.println(lojaLocal + ";" + entry.getKey() + ";" + entry.getValue().toString().replace(".", ","));
            }
        } catch (IOException e) {
            Log.e("INVENTARIO_SAVE", "Erro ao salvar inventário", e);
        }
    }

    private void saveUtilsToTxtMapInventario() {
        try (PrintWriter pw = new PrintWriter(new FileWriter(fullPathFileNameUtilsInventario, false))) {
            pw.println("Produtos;" + mapEANQtdInventario.size() + ";Total;" + inventarioBipadosTotal);
        } catch (IOException e) {
            Log.e("INVENTARIO_SAVE", "Erro ao salvar utilitários", e);
        }
    }

    public static void readerInventarioTxtToMapInventario() {
        mapEANQtdInventario.clear();
        try (BufferedReader br = new BufferedReader(new FileReader(fullPathFileNameInventario))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] lineArray = line.split(";");
                if (lineArray.length >= 3) {
                    mapEANQtdInventario.put(lineArray[1], Double.parseDouble(lineArray[2].replace(",", ".")));
                }
            }
        } catch (Exception e) {
            Log.e("INVENTARIO_READ", "Erro ao ler inventário", e);
        }
    }

    public static void readerUtilsTxtToMapInventario() {
        try (BufferedReader br = new BufferedReader(new FileReader(fullPathFileNameUtilsInventario))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] lineArray = line.split(";");
                if (lineArray.length >= 4) {
                    inventarioBipadosProdutos = Integer.parseInt(lineArray[1]);
                    inventarioBipadosTotal = Integer.parseInt(lineArray[3]);
                }
            }
        } catch (Exception e) {
            Log.e("UTILS_READ", "Erro ao ler utils", e);
        }
    }

    private void atualizarUI() {
        inventarioBipadosProdutos = mapEANQtdInventario.size();
        inventarioBipadosTotal = mapEANQtdInventario.values().stream().mapToInt(Double::intValue).sum();
        mValueShowInventarioProdutos.setText("Produtos: " + inventarioBipadosProdutos);
        mValueShowInventarioTotal.setText("Total: " + inventarioBipadosTotal);
    }
}
