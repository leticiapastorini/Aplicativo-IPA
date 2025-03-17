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
import androidx.appcompat.app.AlertDialog;
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
    TextView mFinalizarLink;

    // Variáveis internas
    String mTextinputEAN;
    String mTextinputQtd;
    Double mTextinputQtdDouble;
    Double mTextinputQtdDoubleRaw;
    private String lojaLocal;

    // Controle de tempo para evitar bipagens rápidas
    private long ultimaBipagem = 0;
    private static final long TEMPO_MINIMO_ENTRE_BIPAGENS = 1500; // 1.5 segundos

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
        // Ação para o botão "Diminuir"
        mModificarBtn.setOnClickListener(v -> {
            String ean = mInputEAN.getText().toString().trim();
            String qtdStr = mInputQtd.getText().toString().trim();

            // Verifica se o EAN foi inserido
            if (ean.isEmpty()) {
                Toast.makeText(this, "Por favor, insira um código de barras!", Toast.LENGTH_SHORT).show();
                return;
            }

            // Verifica se a quantidade foi inserida
            if (qtdStr.isEmpty()) {
                Toast.makeText(this, "Por favor, insira a quantidade a ser reduzida!", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                double qtdReduzir = Double.parseDouble(qtdStr.replace(",", "."));
                if (qtdReduzir <= 0) {
                    Toast.makeText(this, "A quantidade deve ser maior que zero!", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Verifica se o produto existe no inventário
                if (mapEANQtdInventario.containsKey(ean)) {
                    double qtdAtual = mapEANQtdInventario.get(ean);

                    // Só reduz se houver quantidade suficiente
                    if (qtdAtual >= qtdReduzir) {
                        mapEANQtdInventario.put(ean, qtdAtual - qtdReduzir);
                        Toast.makeText(this, "Quantidade reduzida com sucesso!", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Quantidade insuficiente para reduzir!", Toast.LENGTH_SHORT).show();
                    }

                } else {
                    Toast.makeText(this, "Produto não encontrado no inventário!", Toast.LENGTH_SHORT).show();
                }

                // Atualiza a exibição da quantidade na tela
                mValueshowQtd.setText("Quantidade: " + mapEANQtdInventario.getOrDefault(ean, 0.0));

                // Limpa os campos
                mInputEAN.getText().clear();
                mInputQtd.getText().clear();
                mInputEAN.requestFocus();

            } catch (NumberFormatException e) {
                Toast.makeText(this, "Quantidade inválida!", Toast.LENGTH_SHORT).show();
            }
        });



// Ação para o botão "Mostrar Coletas"
mMostrarColetasBtn.setOnClickListener(v -> mostrarColetas());

        // Botão Adicionar com confirmação
        mAdicionarBtn.setOnClickListener(v -> {
            mTextinputEAN = mInputEAN.getText().toString().trim();
            mTextinputQtd = mInputQtd.getText().toString().trim();

            if (mTextinputEAN.isEmpty() || mTextinputQtd.isEmpty()) {
                Toast.makeText(Inventario.this, "Preencha os campos corretamente!", Toast.LENGTH_SHORT).show();
                return;
            }

            // Exibir alerta antes de adicionar um novo código ao inventário
            new AlertDialog.Builder(Inventario.this)
                .setTitle("Confirmação")
                .setMessage("Deseja realmente adicionar o código " + mTextinputEAN + " com quantidade " + mTextinputQtd + "?")
                .setPositiveButton("Sim", (dialog, which) -> {
                    inputHandler(mTextinputEAN, mTextinputQtd);
                    saveInventarioToTxtMapInventario();
                    saveUtilsToTxtMapInventario();
                })
                .setNegativeButton("Cancelar", (dialog, which) -> dialog.dismiss())
                .show();
        });

        // Definir evento de clique no link "Finalizar"
        mFinalizarLink.setOnClickListener(v -> {
            saveInventarioToTxtMapInventario();
            saveUtilsToTxtMapInventario();
            InventarioSeguro inventarioSeguro = new InventarioSeguro(this);
            inventarioSeguro.exportarInventarioProtegido(mapEANQtdInventario);
            Toast.makeText(this, "Inventário finalizado e exportado!", Toast.LENGTH_LONG).show();
        });

        mInputEAN.requestFocus();
    }

    public static void readerInventarioTxtToMapInventario() {
        try (BufferedReader br = new BufferedReader(new FileReader(fullPathFileNameInventario))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] lineArray = line.split(";");
                if (lineArray.length >= 3) {
                    mapEANQtdInventario.put(lineArray[1], Double.parseDouble(lineArray[2].replace(",", ".")));
                }
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
    // Método para exibir coletas
    private void mostrarColetas() {
        StringBuilder coletaStr = new StringBuilder("Coletas:\n");
        for (Map.Entry<String, Double> entry : mapEANQtdInventario.entrySet()) {
            coletaStr.append("EAN: ").append(entry.getKey())
                    .append(" | Quantidade: ").append(entry.getValue()).append("\n");
        }
        Toast.makeText(this, coletaStr.toString(), Toast.LENGTH_LONG).show();
    }


    private void inputHandler(String ean, String qtd) {
        long agora = System.currentTimeMillis();
    
        // 🚨 Proteção contra bipagem muito rápida
        if (agora - ultimaBipagem < TEMPO_MINIMO_ENTRE_BIPAGENS) {
            Toast.makeText(this, "Bipagem muito rápida! Aguarde um momento.", Toast.LENGTH_SHORT).show();
            return;
        }
    
        // 🚨 Validação de código
        if (ean.isEmpty() || ean.length() < 8) {
            Toast.makeText(this, "Código de barras inválido!", Toast.LENGTH_SHORT).show();
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
    
            ultimaBipagem = agora;
    
            mInputEAN.getText().clear();
            mInputQtd.getText().clear();
            mInputEAN.requestFocus();
        } catch (Exception e) {
            Toast.makeText(this, "Erro ao processar entrada.", Toast.LENGTH_SHORT).show();
        }
    }
    
}