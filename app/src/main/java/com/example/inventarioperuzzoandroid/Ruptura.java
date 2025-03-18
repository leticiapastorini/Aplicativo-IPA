package com.example.inventarioperuzzoandroid;

import android.util.Log;

import static com.example.inventarioperuzzoandroid.MainActivity.loja;
import static com.example.inventarioperuzzoandroid.MainActivity.rupturaBipadosProdutos;
import static com.example.inventarioperuzzoandroid.MainActivity.rupturaBipadosTotal;

import android.content.Intent;
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

    //Informações que interagem com o usuário
    TextView mValueShowLoja;
    EditText mInputEAN;
    EditText mInputQtd;
    TextView mValueShowRupturaProdutos;
    TextView mValueShowRupturaTotal;
    TextView mValueShowEAN;
    TextView mValueshowQtd;
    TextView mValueShowSize;


    //Informações de manuseio interno
    String mTextinputEAN;
    String mTextinputQtd;
    Double mTextinputQtdDouble;
    Button mAdicionarBtn;
    Button mModificarBtn;
    Button mVoltarBtn;


    //Informações de path's em geral
    public static File path = Environment.getExternalStorageDirectory();
    public static File fullpath = new File(path + "/InventarioPeruzzoAndroid");
    private static String fileNameRuptura = "ruptura.txt";
    private static File fullPathFileNameRuptura = new File(fullpath, fileNameRuptura);
    private static String fileNameUtilsRuptura = "utilsRuptura.txt";
    private static File fullPathFileNameUtilsRuptura = new File(fullpath, fileNameUtilsRuptura);
    private static String lastQtdMap;
    private static Double lastQtdMapDouble;


    //Map's da Ruptura
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

        readerRupturaTxtToMapRuptura();
        readerUtilsTxtToMapRuptura();


        //Inicializa as views
        mValueShowLoja = findViewById(R.id.showloja);
        mValueShowRupturaProdutos = findViewById(R.id.showRupturaProdutos);
        mValueShowRupturaTotal = findViewById(R.id.showRupturaTotal);
        
        mValueShowEAN = findViewById(R.id.showEAN);
        mValueshowQtd = findViewById(R.id.showQtd);
        mInputEAN = findViewById(R.id.inputEAN);
        mInputQtd = findViewById(R.id.inputQtd);
        mAdicionarBtn = findViewById(R.id.adicionarBtn);
        mModificarBtn = findViewById(R.id.modificarBtn);
        mVoltarBtn = findViewById(R.id.voltarBtn);


        mValueShowLoja.setText("Loja: " + loja);
        mValueShowRupturaProdutos.setText("Produtos: " + rupturaBipadosProdutos.toString());
        mValueShowRupturaTotal.setText("Total: " + rupturaBipadosTotal.toString());
        //mValueShowSize.setText("Size: " + mapEANQtdRuptura.size());

        mInputQtd.setText("1");
        mInputEAN.requestFocus();


        //Adição através da tecla enter
        mInputEAN.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) || (actionId == EditorInfo.IME_ACTION_DONE)) {
                    //code here
                    //Toast.makeText(Ruptura.this, "Teste com sucesso.", Toast.LENGTH_SHORT).show();

                    mTextinputEAN = mInputEAN.getText().toString().trim();
                    mTextinputQtd = mInputQtd.getText().toString().trim();


                    //Validação
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
                    }


                    // Salvamento dos arquivos no txt
                    
    saveRupturaToTxtMapRuptura();
    Log.d("RUPTURA_SAVE", "Ruptura salva corretamente no TXT.");
    
                    saveUtilsToTxtMapRuptura();


                    // Atualizar as informações na view do usuário
                    mValueShowRupturaProdutos.setText("Produtos: " + rupturaBipadosProdutos.toString());
                    mValueShowRupturaTotal.setText("Total: " + rupturaBipadosTotal.toString());
                    //mValueShowSize.setText("Size: " + mapEANQtdRuptura.size());

                    if (mapEANQtdRuptura.get(mTextinputEAN) != null) {
                        lastQtdMap = (mapEANQtdRuptura.get(mTextinputEAN).toString().trim());
                    } else {
                        lastQtdMap = " ";
                    }

                    mValueShowEAN.setText("Código: " + mInputEAN.getText().toString().trim());
                    mValueshowQtd.setText("Quantidade: " + lastQtdMap);


                    mInputEAN.getText().clear();
                    mInputQtd.getText().clear();
                    mInputQtd.setText("1");

                    mInputEAN.requestFocus();


                }

                mInputEAN.requestFocus();
                return false;
            }
        });


        //Funções internas dos botões
        mModificarBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mInputEAN.requestFocus();
                mInputEAN.setPrivateImeOptions("next");
                mInputQtd.setText("-1");
                mInputQtd.setSelection(2);
            }
        });

        mVoltarBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openMainActivity();
            }
        });

        mAdicionarBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                mTextinputEAN = mInputEAN.getText().toString().trim();
                mTextinputQtd = mInputQtd.getText().toString().trim();


                //Validação
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
                }


                
    saveRupturaToTxtMapRuptura();
    Log.d("RUPTURA_SAVE", "Ruptura salva corretamente no TXT.");
    
                saveUtilsToTxtMapRuptura();


                mValueShowRupturaProdutos.setText("Produtos: " + rupturaBipadosProdutos.toString());
                mValueShowRupturaTotal.setText("Total: " + rupturaBipadosTotal.toString());
                //mValueShowSize.setText("Size: " + mapEANQtdRuptura.size());


                if (mapEANQtdRuptura.get(mTextinputEAN) != null) {
                    lastQtdMap = (mapEANQtdRuptura.get(mTextinputEAN).toString().trim());
                } else {
                    lastQtdMap = " ";
                }

                mValueShowEAN.setText("Código: " + mInputEAN.getText().toString().trim());
                mValueshowQtd.setText("Quantidade: " + lastQtdMap);


                mInputEAN.getText().clear();
                mInputQtd.getText().clear();
                mInputQtd.setText("1");

                mInputEAN.requestFocus();

            }
        });

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


        if (mapEANQtdRuptura.containsKey(mTextinputEAN) == false && mTextinputEAN.isEmpty() && mTextinputQtdDouble == -1.0) {
            Toast.makeText(Ruptura.this, "Por favor, preencha o código corretamente.", Toast.LENGTH_SHORT).show();

            lastQtdMap = " ";

        } else if (mapEANQtdRuptura.containsKey(mTextinputEAN) == false && mTextinputEAN.isEmpty() && mTextinputQtdDouble == 1.0) {
            Toast.makeText(Ruptura.this, "Por favor, preencha o código corretamente.", Toast.LENGTH_SHORT).show();

            lastQtdMap = " ";

        } else if (mapEANQtdRuptura.containsKey(mTextinputEAN) && mTextinputQtdDouble != 1.0 && mapEANQtdRuptura.get(mTextinputEAN) == 0.0) {
            Toast.makeText(Ruptura.this, "O produto já está zerado. Por favor, preencha corretamente.", Toast.LENGTH_SHORT).show();

            lastQtdMap = " ";
            rupturaBipadosTotal -= 1;


        } else if (mapEANQtdRuptura.containsKey(mTextinputEAN) && mTextinputQtdDouble == 1.0 && mapEANQtdRuptura.get(mTextinputEAN) >= 1.0) {
//            Toast.makeText(Ruptura.this, "O produto já foi adicionado. Por favor, preencha corretamente.", Toast.LENGTH_SHORT).show();

            mapEANQtdRuptura.put(mTextinputEAN, (mapEANQtdRuptura.get(mTextinputEAN) + mTextinputQtdDouble));
            lastQtdMap = (mapEANQtdRuptura.get(mTextinputEAN).toString().trim());

//            lastQtdMap = " ";
//            rupturaBipadosTotal -= 1;


            //Reinserção de valores zerados manualmente
        } else if (mapEANQtdRuptura.containsKey(mTextinputEAN)) {

            if (lastQtdMapDouble == null) {
                mapEANQtdRuptura.put(mTextinputEAN, mTextinputQtdDouble);

            } else if (mTextinputQtdDouble == 1.0) {
                mapEANQtdRuptura.put(mTextinputEAN, 1.0);

            }
            //else if (mTextinputQtdDouble != 1.0) {
            //mapEANQtdRuptura.put(mTextinputEAN, 0.0);
            //}

            else if (mTextinputQtdDouble != 1.0) {
                mapEANQtdRuptura.put(mTextinputEAN, (mapEANQtdRuptura.get(mTextinputEAN) + mTextinputQtdDouble));
            }

            lastQtdMap = (mapEANQtdRuptura.get(mTextinputEAN).toString().trim());


        } else {

            if (mTextinputQtdDouble == -1.0) {
                Toast.makeText(Ruptura.this, "O produto não foi bipado. Por favor, preencha corretamente.", Toast.LENGTH_SHORT).show();

                lastQtdMap = " ";
                rupturaBipadosTotal -= 1;


            } else {
                mapEANQtdRuptura.put(mTextinputEAN, mTextinputQtdDouble);
                lastQtdMap = (mapEANQtdRuptura.get(mTextinputEAN).toString().trim());

            }
        }


    }


    public static void readerRupturaTxtToMapRuptura() {
        try {
            BufferedReader br = new BufferedReader(new FileReader(fullPathFileNameRuptura));
            String line;

            while ((line = br.readLine()) != null) {
                String lineTemp = line;
                String[] lineArray = lineTemp.split("\\x3B");

                String eanLine = lineArray[1];
                String qtdLine = lineArray[2];

                if (qtdLine.contains(",")) {
                    qtdLine = qtdLine.replace(",", ".");
                }

                double qtdLineDouble = Double.parseDouble(qtdLine);


                mapEANQtdRuptura.put(eanLine, qtdLineDouble);
            }
            br.close();
            return;

        } catch (Exception e) {
            return;
        }
    }

    public static void readerUtilsTxtToMapRuptura() {
        try {
            BufferedReader br = new BufferedReader(new FileReader(fullPathFileNameUtilsRuptura));
            String line;

            while ((line = br.readLine()) != null) {
                String lineTemp = line;
                String[] lineArray = lineTemp.split("\\x3B");


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
        try {

            mapEANQtdRupturaVirgula.putAll(mapEANQtdRuptura);


            try {
                fullpath.mkdirs();

                try {
                    FileWriter fw = new FileWriter(fullPathFileNameRuptura, false);
                    PrintWriter pw = new PrintWriter(fw);


                    mapEANQtdRupturaVirgula.entrySet().forEach(entry -> {
                        pw.println(loja + ";" + (entry.getKey()) + ";" + entry.getValue().toString().replace(".", ","));
                    });

                    pw.close();

                } catch (IOException e) {
                    e.printStackTrace();
                }


            } catch (Exception e) {
                Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }


        } catch (Exception e) {
            return;
        }
    }

    private void saveUtilsToTxtMapRuptura() {
        try {
            fullpath.mkdirs();
            try {
                FileWriter fw = new FileWriter(fullPathFileNameUtilsRuptura, false);
                PrintWriter pw = new PrintWriter(fw);

                rupturaBipadosProdutos = mapEANQtdRupturaVirgula.size();
                pw.println("Produtos;" + rupturaBipadosProdutos + ";Total;" + rupturaBipadosTotal);

                pw.close();

            } catch (IOException e) {
                e.printStackTrace();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}