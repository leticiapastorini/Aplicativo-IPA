package com.example.inventarioperuzzoandroid;

import static com.example.inventarioperuzzoandroid.MainActivity.inventarioBipadosProdutos;
import static com.example.inventarioperuzzoandroid.MainActivity.inventarioBipadosTotal;
// REMOVIDO: import static com.example.inventarioperuzzoandroid.MainActivity.loja;

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
    TextView mValueShowSize;

    // Informações de manuseio interno
    String mTextinputEAN;
    String mTextinputQtd;
    Double mTextinputQtdDouble;
    Button mAdicionarBtn;
    Button mModificarBtn;
    Button mVoltarBtn;
    Double mTextinputQtdDoubleRaw;

    // >>> NOVO: variável local para a loja lida do SharedPreferences
    private String lojaLocal;

    // Informações de path's em geral
    public static File path = Environment.getExternalStorageDirectory();
    public static File fullpath = new File(path, "InventarioPeruzzoAndroid");
    private static String fileNameInventario = "inventario.txt";
    private static File fullPathFileNameInventario = new File(fullpath, fileNameInventario);
    private static String fileNameUtilsInventario = "utilsInventario.txt";
    private static File fullPathFileNameUtilsInventario = new File(fullpath, fileNameUtilsInventario);
    private static String lastQtdMap;
    private static Double lastQtdMapDouble;
    public static String loja = "";

    // Map's do Inventário
    public static Map<String, Double> mapEANQtdInventario;
    static {
        mapEANQtdInventario = new HashMap<>();
    }

    public static Map<String, Double> mapEANQtdInventarioVirgula;
    static {
        mapEANQtdInventarioVirgula = new HashMap<>();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTitle("Inventário");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inventario);

        // Lê o número da loja das SharedPreferences
        SharedPreferences prefs = getSharedPreferences("InventarioPrefs", MODE_PRIVATE);
        lojaLocal = prefs.getString("loja", ""); // valor padrão vazio

        // Lê o conteúdo prévio dos arquivos, se existirem
        readerInventarioTxtToMapInventario();
        readerUtilsTxtToMapInventario();

        // Inicializa as views
        mValueShowLoja = findViewById(R.id.showloja);
        mValueShowInventarioProdutos = findViewById(R.id.showInventarioProdutos);
        mValueShowInventarioTotal = findViewById(R.id.showInventarioTotal);
        mValueShowSize = findViewById(R.id.sizezada);
        mValueShowEAN = findViewById(R.id.showEAN);
        mValueshowQtd = findViewById(R.id.showQtd);
        mInputEAN = findViewById(R.id.inputEAN);
        mInputQtd = findViewById(R.id.inputQtd);
        mAdicionarBtn = findViewById(R.id.adicionarBtn);
        mModificarBtn = findViewById(R.id.modificarBtn);
        mVoltarBtn = findViewById(R.id.voltarBtn);

        // Exibe a loja e os contadores
        mValueShowLoja.setText("Loja: " + lojaLocal);
        mValueShowInventarioProdutos.setText("Produtos: " + inventarioBipadosProdutos.toString());
        mValueShowInventarioTotal.setText("Total: " + inventarioBipadosTotal.toString());

        // Configuração para a ação do teclado no campo de quantidade
        mInputQtd.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER))
                        || (actionId == EditorInfo.IME_ACTION_DONE)) {

                    mTextinputEAN = mInputEAN.getText().toString().trim();
                    mTextinputQtd = mInputQtd.getText().toString().trim();

                    if (mTextinputEAN.isEmpty() && mTextinputQtd.isEmpty()) {
                        Toast.makeText(Inventario.this, "Por favor, preencha os campos.", Toast.LENGTH_SHORT).show();
                    } else if (mTextinputEAN.isEmpty()) {
                        Toast.makeText(Inventario.this, "Por favor, preencha o código corretamente.", Toast.LENGTH_SHORT).show();
                    } else if (mTextinputQtd.isEmpty()) {
                        Toast.makeText(Inventario.this, "Por favor, preencha a quantidade corretamente.", Toast.LENGTH_SHORT).show();
                    } else {
                        inputHandler(mTextinputEAN, mTextinputQtd);
                    }

                    saveInventarioToTxtMapInventario();
                    saveUtilsToTxtMapInventario();

                    mValueShowInventarioProdutos.setText("Produtos: " + inventarioBipadosProdutos.toString());
                    mValueShowInventarioTotal.setText("Total: " + inventarioBipadosTotal.toString());

                    if (mapEANQtdInventario.get(mTextinputEAN) != null) {
                        lastQtdMap = mapEANQtdInventario.get(mTextinputEAN).toString().trim();
                    } else {
                        lastQtdMap = "";
                    }

                    mValueShowEAN.setText("Código: " + mInputEAN.getText().toString().trim());
                    mValueshowQtd.setText("Quantidade: " + lastQtdMap);

                    mInputEAN.getText().clear();
                    mInputQtd.getText().clear();
                }
                return false;
            }
        });

        // Botão Modificar
        mModificarBtn.setOnClickListener(v -> {
            mInputEAN.requestFocus();
            mInputEAN.setPrivateImeOptions("next");
            mInputQtd.setText("-");
            mInputQtd.setSelection(1);
        });

        // Botão Voltar
        mVoltarBtn.setOnClickListener(v -> openMainActivity());

        // Botão Adicionar
        mAdicionarBtn.setOnClickListener(v -> {
            mTextinputEAN = mInputEAN.getText().toString().trim();
            mTextinputQtd = mInputQtd.getText().toString().trim();

            if (mTextinputEAN.isEmpty() && mTextinputQtd.isEmpty()) {
                Toast.makeText(Inventario.this, "Por favor, preencha os campos.", Toast.LENGTH_SHORT).show();
            } else if (mTextinputEAN.isEmpty()) {
                Toast.makeText(Inventario.this, "Por favor, preencha o código corretamente.", Toast.LENGTH_SHORT).show();
            } else if (mTextinputQtd.isEmpty()) {
                Toast.makeText(Inventario.this, "Por favor, preencha a quantidade corretamente.", Toast.LENGTH_SHORT).show();
            } else {
                inputHandler(mTextinputEAN, mTextinputQtd);
            }

            saveInventarioToTxtMapInventario();
            saveUtilsToTxtMapInventario();

            mValueShowInventarioProdutos.setText("Produtos: " + inventarioBipadosProdutos.toString());
            mValueShowInventarioTotal.setText("Total: " + inventarioBipadosTotal.toString());

            if (mapEANQtdInventario.get(mTextinputEAN) != null) {
                lastQtdMap = mapEANQtdInventario.get(mTextinputEAN).toString().trim();
            } else {
                lastQtdMap = "";
            }

            mValueShowEAN.setText("Código: " + mInputEAN.getText().toString().trim());
            mValueshowQtd.setText("Quantidade: " + lastQtdMap);

            mInputEAN.getText().clear();
            mInputQtd.getText().clear();
            mInputEAN.requestFocus();
        });

        mInputEAN.requestFocus();
    }

    private void openMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    private void inputHandler(String mTextinputEAN, String mTextinputQtd) {
        if (mTextinputQtd.equals("-")) {
            mTextinputQtdDouble = 0.0;
        } else if (mTextinputQtd.equals(".")) {
            mTextinputQtdDouble = 0.0;
        } else if (mTextinputQtd.equals("-.")) {
            mTextinputQtdDouble = 0.0;
        } else {
            mTextinputQtdDoubleRaw = Double.parseDouble(mTextinputQtd);
            mTextinputQtdDoubleRaw = Math.round(mTextinputQtdDoubleRaw * 1000.0) / 1000.0;
            mTextinputQtd = String.format("%.3f", mTextinputQtdDoubleRaw);
            mTextinputQtd = mTextinputQtd.replace(",", ".");
            mTextinputQtdDouble = Double.parseDouble(mTextinputQtd);
        }

        inventarioBipadosTotal += 1;
        lastQtdMapDouble = mapEANQtdInventario.get(mTextinputEAN);

        if (!mapEANQtdInventario.containsKey(mTextinputEAN) && (mTextinputQtdDouble <= 0.0) || mTextinputQtd.equals("-")) {
            Toast.makeText(Inventario.this, "Por favor, preencha os campos corretamente.", Toast.LENGTH_SHORT).show();
            lastQtdMap = " ";
            inventarioBipadosTotal -= 1;
        } else if (mapEANQtdInventario.containsKey(mTextinputEAN) && (lastQtdMapDouble + mTextinputQtdDouble) < 0.0) {
            Toast.makeText(Inventario.this, "A quantidade inserida de retirada é menor que a quantidade total. Por favor, preencha corretamente.", Toast.LENGTH_LONG).show();
            inventarioBipadosTotal -= 1;
        } else if (mapEANQtdInventario.containsKey(mTextinputEAN)) {
            if (mapEANQtdInventario.get(mTextinputEAN) + mTextinputQtdDouble < 0.001) {
                mapEANQtdInventario.replace(mTextinputEAN, 0.000);
            } else {
                mTextinputQtdDouble = Math.round(mTextinputQtdDoubleRaw * 1000.0) / 1000.0;
                mapEANQtdInventario.replace(mTextinputEAN,
                        Math.round((mapEANQtdInventario.get(mTextinputEAN) + mTextinputQtdDouble) * 1000.0) / 1000.0);
            }
            lastQtdMap = mapEANQtdInventario.get(mTextinputEAN).toString().trim();
        } else {
            mTextinputQtdDouble = Math.round(mTextinputQtdDoubleRaw * 1000.0) / 1000.0;
            mapEANQtdInventario.put(mTextinputEAN, mTextinputQtdDouble);
            lastQtdMap = mapEANQtdInventario.get(mTextinputEAN).toString().trim();
        }
    }

    public static void readerInventarioTxtToMapInventario() {
        try (BufferedReader br = new BufferedReader(new FileReader(fullPathFileNameInventario))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] lineArray = line.split("\\x3B");
                String eanLine = lineArray[1];
                String qtdLine = lineArray[2];
                if (qtdLine.contains(",")) {
                    qtdLine = qtdLine.replace(",", ".");
                }
                double qtdLineDouble = Double.parseDouble(qtdLine);
                mapEANQtdInventario.put(eanLine, qtdLineDouble);
            }
        } catch (Exception e) {
            return;
        }
    }

    public static void readerUtilsTxtToMapInventario() {
        try (BufferedReader br = new BufferedReader(new FileReader(fullPathFileNameUtilsInventario))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] lineArray = line.split("\\x3B");
                int inventarioBipadosProdutostxt = Integer.parseInt(lineArray[1]);
                int inventarioBipadosTotaltxt = Integer.parseInt(lineArray[3]);
                inventarioBipadosProdutos = inventarioBipadosProdutostxt;
                inventarioBipadosTotal = inventarioBipadosTotaltxt;
            }
        } catch (Exception e) {
            return;
        }
    }

    private void saveInventarioToTxtMapInventario() {
        try {
            mapEANQtdInventarioVirgula.putAll(mapEANQtdInventario);

            File fullpath = new File(Environment.getExternalStorageDirectory(), "InventarioPeruzzoAndroid");
            boolean pastaCriada = fullpath.mkdirs();
            android.util.Log.d("EXPORT", "Pasta criada? " + pastaCriada + " | Caminho: " + fullpath.getAbsolutePath());

            File fullPathFileNameInventario = new File(fullpath, "inventario.txt");

            SharedPreferences prefs = getSharedPreferences("InventarioPrefs", MODE_PRIVATE);
            final String lojaExport = prefs.getString("loja", "");

            FileWriter fw = new FileWriter(fullPathFileNameInventario, false);
            final PrintWriter pw = new PrintWriter(fw);

            for (Map.Entry<String, Double> entry : mapEANQtdInventarioVirgula.entrySet()) {
                String linha = lojaExport + ";" + entry.getKey() + ";" + entry.getValue().toString().replace(".", ",");
                pw.println(linha);
                Log.d("EXPORT", "Salvando arquivo: " + fullPathFileNameInventario.getAbsolutePath());

                android.util.Log.d("EXPORT", "Linha escrita: " + linha);
                Log.d("EXPORT", "Linha escrita: " + linha);

            }

            pw.close();
            android.util.Log.d("EXPORT", "Arquivo exportado com sucesso em: " + fullPathFileNameInventario.getAbsolutePath());

        } catch (IOException e) {
            e.printStackTrace();
            android.util.Log.e("EXPORT", "Erro ao exportar o arquivo", e);
        }
    }
    
    
    

    private void saveUtilsToTxtMapInventario() {
        try {
            fullpath.mkdirs();
            FileWriter fw = new FileWriter(fullPathFileNameUtilsInventario, false);
            PrintWriter pw = new PrintWriter(fw);

            inventarioBipadosProdutos = mapEANQtdInventarioVirgula.size();
            pw.println("Produtos;" + inventarioBipadosProdutos + ";Total;" + inventarioBipadosTotal);
            pw.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
