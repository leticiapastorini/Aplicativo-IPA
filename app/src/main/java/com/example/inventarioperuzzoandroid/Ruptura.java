package com.example.inventarioperuzzoandroid;

// REMOVIDO: import static com.example.inventarioperuzzoandroid.MainActivity.loja;
import static com.example.inventarioperuzzoandroid.MainActivity.rupturaBipadosProdutos;
import static com.example.inventarioperuzzoandroid.MainActivity.rupturaBipadosTotal;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.view.KeyEvent;
import android.view.View;
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

public class Ruptura extends AppCompatActivity {

    // Views
    TextView mValueShowLoja;
    EditText mInputEAN;
    EditText mInputQtd;
    TextView mValueShowRupturaProdutos;
    TextView mValueShowRupturaTotal;
    TextView mValueShowEAN;
    TextView mValueshowQtd;
    TextView mValueShowSize;

    // Botões
    Button mAdicionarBtn;
    Button mModificarBtn;
    Button mVoltarBtn;

    // Variáveis de trabalho
    String mTextinputEAN;
    String mTextinputQtd;
    Double mTextinputQtdDouble;
    private String lastQtdMap;
    private Double lastQtdMapDouble;

    // >>>>>> NOVO: vamos guardar aqui o valor da loja lido do SharedPreferences
    private String lojaLocal;

    // Path e arquivos
    public static File path = Environment.getExternalStorageDirectory();
    public static File fullpath = new File(path + "/InventarioPeruzzoAndroid");
    private static String fileNameRuptura = "ruptura.txt";
    private static File fullPathFileNameRuptura = new File(fullpath, fileNameRuptura);
    private static String fileNameUtilsRuptura = "utilsRuptura.txt";
    private static File fullPathFileNameUtilsRuptura = new File(fullpath, fileNameUtilsRuptura);

    // Mapas
    public static Map<String, Double> mapEANQtdRuptura;
    static {
        mapEANQtdRuptura = new HashMap<>();
    }

    public static Map<String, Double> mapEANQtdRupturaVirgula;
    static {
        mapEANQtdRupturaVirgula = new HashMap<>();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTitle("Ruptura");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ruptura);

        // 1) Ler do SharedPreferences o número da loja
        SharedPreferences prefs = getSharedPreferences("InventarioPrefs", MODE_PRIVATE);
        lojaLocal = prefs.getString("loja", ""); // Valor default caso não exista

        // Ler do TXT para preencher os Maps e contadores
        readerRupturaTxtToMapRuptura();
        readerUtilsTxtToMapRuptura();

        // Inicializa as views
        mValueShowLoja = findViewById(R.id.showloja);
        mValueShowRupturaProdutos = findViewById(R.id.showRupturaProdutos);
        mValueShowRupturaTotal = findViewById(R.id.showRupturaTotal);
        //mValueShowSize = findViewById(R.id.sizezada);
        mValueShowEAN = findViewById(R.id.showEAN);
        mValueshowQtd = findViewById(R.id.showQtd);
        mInputEAN = findViewById(R.id.inputEAN);
        mInputQtd = findViewById(R.id.inputQtd);
        mAdicionarBtn = findViewById(R.id.adicionarBtn);
        mModificarBtn = findViewById(R.id.modificarBtn);
        mVoltarBtn = findViewById(R.id.voltarBtn);

        // Mostra loja e contadores na tela
        mValueShowLoja.setText("Loja: " + lojaLocal);
        mValueShowRupturaProdutos.setText("Produtos: " + rupturaBipadosProdutos);
        mValueShowRupturaTotal.setText("Total: " + rupturaBipadosTotal);

        // Ajuste inicial do input
        mInputQtd.setText("1");
        mInputEAN.requestFocus();

        // Ao pressionar Enter em mInputEAN
        mInputEAN.setOnEditorActionListener((TextView v, int actionId, KeyEvent event) -> {
            if ((event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER) ||
                    (actionId == EditorInfo.IME_ACTION_DONE)) {

                mTextinputEAN = mInputEAN.getText().toString().trim();
                mTextinputQtd = mInputQtd.getText().toString().trim();

                // Validação
                if (mTextinputEAN.isEmpty() && mTextinputQtd.isEmpty()) {
                    Toast.makeText(Ruptura.this, "Por favor, preencha os campos.", Toast.LENGTH_SHORT).show();
                } else if (mTextinputEAN.isEmpty()) {
                    Toast.makeText(Ruptura.this, "Por favor, preencha o código corretamente.", Toast.LENGTH_SHORT).show();
                } else if (mTextinputQtd.isEmpty()) {
                    Toast.makeText(Ruptura.this, "Por favor, preencha a quantidade corretamente.", Toast.LENGTH_SHORT).show();
                    if (rupturaBipadosTotal >= 1) {
                        mValueshowQtd.setText("Quantidade: " + lastQtdMap);
                    } else {
                        mValueshowQtd.setText("Quantidade: " + mInputQtd.getText().toString().trim());
                    }
                } else {
                    // Processa
                    inputHandler(mTextinputEAN, mTextinputQtd);
                    saveRupturaToTxtMapRuptura();
                    saveUtilsToTxtMapRuptura();

                    // Atualiza tela
                    mValueShowRupturaProdutos.setText("Produtos: " + rupturaBipadosProdutos);
                    mValueShowRupturaTotal.setText("Total: " + rupturaBipadosTotal);

                    if (mapEANQtdRuptura.get(mTextinputEAN) != null) {
                        lastQtdMap = mapEANQtdRuptura.get(mTextinputEAN).toString().trim();
                    } else {
                        lastQtdMap = " ";
                    }
                    mValueShowEAN.setText("Código: " + mInputEAN.getText().toString().trim());
                    mValueshowQtd.setText("Quantidade: " + lastQtdMap);

                    // Limpa inputs
                    mInputEAN.getText().clear();
                    mInputQtd.getText().clear();
                    mInputQtd.setText("1");
                    mInputEAN.requestFocus();
                }
            }
            return false;
        });

        // Botão Modificar
        mModificarBtn.setOnClickListener(v -> {
            mInputEAN.requestFocus();
            mInputEAN.setPrivateImeOptions("next");
            mInputQtd.setText("-1");
            mInputQtd.setSelection(2);
        });

        // Botão Voltar
        mVoltarBtn.setOnClickListener(v -> openMainActivity());

        // Botão Adicionar
        mAdicionarBtn.setOnClickListener(v -> {
            mTextinputEAN = mInputEAN.getText().toString().trim();
            mTextinputQtd = mInputQtd.getText().toString().trim();

            if (mTextinputEAN.isEmpty() && mTextinputQtd.isEmpty()) {
                Toast.makeText(Ruptura.this, "Por favor, preencha os campos.", Toast.LENGTH_SHORT).show();
            } else if (mTextinputEAN.isEmpty()) {
                Toast.makeText(Ruptura.this, "Por favor, preencha o código corretamente.", Toast.LENGTH_SHORT).show();
            } else if (mTextinputQtd.isEmpty()) {
                Toast.makeText(Ruptura.this, "Por favor, preencha a quantidade corretamente.", Toast.LENGTH_SHORT).show();
                if (rupturaBipadosTotal >= 1) {
                    mValueshowQtd.setText("Quantidade: " + lastQtdMap);
                } else {
                    mValueshowQtd.setText("Quantidade: " + mInputQtd.getText().toString().trim());
                }
            } else {
                inputHandler(mTextinputEAN, mTextinputQtd);
                saveRupturaToTxtMapRuptura();
                saveUtilsToTxtMapRuptura();

                mValueShowRupturaProdutos.setText("Produtos: " + rupturaBipadosProdutos);
                mValueShowRupturaTotal.setText("Total: " + rupturaBipadosTotal);

                if (mapEANQtdRuptura.get(mTextinputEAN) != null) {
                    lastQtdMap = mapEANQtdRuptura.get(mTextinputEAN).toString().trim();
                } else {
                    lastQtdMap = " ";
                }
                mValueShowEAN.setText("Código: " + mTextinputEAN);
                mValueshowQtd.setText("Quantidade: " + lastQtdMap);

                mInputEAN.getText().clear();
                mInputQtd.getText().clear();
                mInputQtd.setText("1");
                mInputEAN.requestFocus();
            }
        });

        // Foco final
        mInputEAN.requestFocus();
    }

    private void openMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    private void inputHandler(String mTextinputEAN, String mTextinputQtd) {
        mTextinputQtdDouble = Double.parseDouble(mTextinputQtd);
        rupturaBipadosTotal += 1;
        lastQtdMapDouble = mapEANQtdRuptura.get(mTextinputEAN);

        // ... (resto da lógica inalterada) ...
        if (!mapEANQtdRuptura.containsKey(mTextinputEAN) && mTextinputEAN.isEmpty() && mTextinputQtdDouble == -1.0) {
            Toast.makeText(Ruptura.this, "Por favor, preencha o código corretamente.", Toast.LENGTH_SHORT).show();
            lastQtdMap = " ";

        } else if (!mapEANQtdRuptura.containsKey(mTextinputEAN) && mTextinputEAN.isEmpty() && mTextinputQtdDouble == 1.0) {
            Toast.makeText(Ruptura.this, "Por favor, preencha o código corretamente.", Toast.LENGTH_SHORT).show();
            lastQtdMap = " ";

        } else if (mapEANQtdRuptura.containsKey(mTextinputEAN) && mTextinputQtdDouble != 1.0 && mapEANQtdRuptura.get(mTextinputEAN) == 0.0) {
            Toast.makeText(Ruptura.this, "O produto já está zerado. Por favor, preencha corretamente.", Toast.LENGTH_SHORT).show();
            lastQtdMap = " ";
            rupturaBipadosTotal -= 1;

        } else if (mapEANQtdRuptura.containsKey(mTextinputEAN) && mTextinputQtdDouble == 1.0 && mapEANQtdRuptura.get(mTextinputEAN) >= 1.0) {
            mapEANQtdRuptura.put(mTextinputEAN, mapEANQtdRuptura.get(mTextinputEAN) + mTextinputQtdDouble);
            lastQtdMap = mapEANQtdRuptura.get(mTextinputEAN).toString().trim();

        } else if (mapEANQtdRuptura.containsKey(mTextinputEAN)) {
            if (lastQtdMapDouble == null) {
                mapEANQtdRuptura.put(mTextinputEAN, mTextinputQtdDouble);
            } else if (mTextinputQtdDouble == 1.0) {
                mapEANQtdRuptura.put(mTextinputEAN, 1.0);
            } else if (mTextinputQtdDouble != 1.0) {
                mapEANQtdRuptura.put(mTextinputEAN, mapEANQtdRuptura.get(mTextinputEAN) + mTextinputQtdDouble);
            }
            lastQtdMap = mapEANQtdRuptura.get(mTextinputEAN).toString().trim();

        } else {
            if (mTextinputQtdDouble == -1.0) {
                Toast.makeText(Ruptura.this, "O produto não foi bipado. Por favor, preencha corretamente.", Toast.LENGTH_SHORT).show();
                lastQtdMap = " ";
                rupturaBipadosTotal -= 1;
            } else {
                mapEANQtdRuptura.put(mTextinputEAN, mTextinputQtdDouble);
                lastQtdMap = mapEANQtdRuptura.get(mTextinputEAN).toString().trim();
            }
        }
    }

    public static void readerRupturaTxtToMapRuptura() {
        // ... (sem mexer na lógica do txt) ...
        try {
            BufferedReader br = new BufferedReader(new FileReader(fullPathFileNameRuptura));
            String line;
            while ((line = br.readLine()) != null) {
                String[] lineArray = line.split("\\x3B");
                String eanLine = lineArray[1];
                String qtdLine = lineArray[2];
                if (qtdLine.contains(",")) {
                    qtdLine = qtdLine.replace(",", ".");
                }
                double qtdLineDouble = Double.parseDouble(qtdLine);
                mapEANQtdRuptura.put(eanLine, qtdLineDouble);
            }
            br.close();
        } catch (Exception e) {
            // Se der erro ou não existir o arquivo, apenas retorna
            return;
        }
    }

    public static void readerUtilsTxtToMapRuptura() {
        // ... (idem, sem alterar)
        try {
            BufferedReader br = new BufferedReader(new FileReader(fullPathFileNameUtilsRuptura));
            String line;
            while ((line = br.readLine()) != null) {
                String[] lineArray = line.split("\\x3B");
                int RupturaBipadosProdutostxt = Integer.parseInt(lineArray[1]);
                int RupturaBipadosTotaltxt = Integer.parseInt(lineArray[3]);
                rupturaBipadosProdutos = RupturaBipadosProdutostxt;
                rupturaBipadosTotal = RupturaBipadosTotaltxt;
            }
            br.close();
        } catch (Exception e) {
            return;
        }
    }

    private void saveRupturaToTxtMapRuptura() {
        // >>> Note que aqui substituí “loja” por “lojaLocal”
        try {
            mapEANQtdRupturaVirgula.putAll(mapEANQtdRuptura);
            fullpath.mkdirs();
            FileWriter fw = new FileWriter(fullPathFileNameRuptura, false);
            PrintWriter pw = new PrintWriter(fw);

            for (Map.Entry<String, Double> entry : mapEANQtdRupturaVirgula.entrySet()) {
                pw.println(lojaLocal + ";" + entry.getKey() + ";" + entry.getValue().toString().replace(".", ","));
            }
            pw.close();

        } catch (Exception e) {
            // Se algo falhar, não muda a formatação, só ignora
            e.printStackTrace();
        }
    }

    private void saveUtilsToTxtMapRuptura() {
        try {
            fullpath.mkdirs();
            FileWriter fw = new FileWriter(fullPathFileNameUtilsRuptura, false);
            PrintWriter pw = new PrintWriter(fw);

            rupturaBipadosProdutos = mapEANQtdRupturaVirgula.size();
            pw.println("Produtos;" + rupturaBipadosProdutos + ";Total;" + rupturaBipadosTotal);

            pw.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
