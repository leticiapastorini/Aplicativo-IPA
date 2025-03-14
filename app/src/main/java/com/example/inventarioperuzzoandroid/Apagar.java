package com.example.inventarioperuzzoandroid;

import static com.example.inventarioperuzzoandroid.Inventario.mapEANQtdInventario;
import static com.example.inventarioperuzzoandroid.Inventario.mapEANQtdInventarioVirgula;
import static com.example.inventarioperuzzoandroid.MainActivity.inventarioBipadosProdutos;
import static com.example.inventarioperuzzoandroid.MainActivity.inventarioBipadosTotal;
import static com.example.inventarioperuzzoandroid.MainActivity.rupturaBipadosProdutos;
import static com.example.inventarioperuzzoandroid.MainActivity.rupturaBipadosTotal;
import static com.example.inventarioperuzzoandroid.Ruptura.mapEANQtdRuptura;
import static com.example.inventarioperuzzoandroid.Ruptura.mapEANQtdRupturaVirgula;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class Apagar extends AppCompatActivity {

    Button mVoltarBtn;
    Button mApagarInventarioBtn;
    Button mApagarRupturaBtn;

    // Necessário estar com o manifest devidamente configurado
    // Sendo essa de função vital: android:requestLegacyExternalStorage="true"

    //Informações de path's em geral
    public static File path = new File(System.getenv("EXTERNAL_STORAGE"));
    public static File fullpath = new File(path + "/InventarioPeruzzoAndroid");

    private static String fileNameInventario = "inventario.txt";
    private static File fullPathFileNameInventario = new File(fullpath, fileNameInventario);
    private static String fileNameUtilsInventario = "utilsInventario.txt";
    private static File fullPathFileNameUtilsInventario = new File(fullpath, fileNameUtilsInventario);

    private static String fileNameRuptura = "ruptura.txt";
    private static File fullPathFileNameRuptura = new File(fullpath, fileNameRuptura);
    private static String fileNameUtilsRuptura = "utilsRuptura.txt";
    private static File fullPathFileNameUtilsRuptura = new File(fullpath, fileNameUtilsRuptura);


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTitle("Apagar");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_apagar);

        //Inicializa as views
        mVoltarBtn = findViewById(R.id.voltarBtn);
        mApagarInventarioBtn = findViewById(R.id.apagarInventarioBtn);
        mApagarRupturaBtn = findViewById(R.id.apagarRupturaBtn);

        //Funções internas dos botões
        mApagarInventarioBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                apagarInventarioToTxtMapInventario();
                apagarUtilsToTxtMapInventario();

                mapEANQtdInventario.clear();
                mapEANQtdInventarioVirgula.clear();

                inventarioBipadosProdutos = 0;
                inventarioBipadosTotal = 0;

            }
        });

        mApagarRupturaBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                apagarRupturaToTxtMapRuptura();
                apagarUtilsToTxtMapRuptura();

                mapEANQtdRuptura.clear();
                mapEANQtdRupturaVirgula.clear();

                rupturaBipadosProdutos = 0;
                rupturaBipadosTotal = 0;

            }
        });

        mVoltarBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openMainActivity();
            }
        });


    }

    private void openMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }


    private void apagarInventarioToTxtMapInventario() {
        try {
            try {
                fullpath.mkdirs();

                try {
                    FileWriter fw = new FileWriter(fullPathFileNameInventario, false);
                    PrintWriter pw = new PrintWriter(fw);

                    pw.println();
                    pw.close();

                    Toast.makeText(Apagar.this, "O arquivo inventario.txt foi excluido com sucesso.", Toast.LENGTH_SHORT).show();


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

    private void apagarUtilsToTxtMapInventario() {
        try {
            fullpath.mkdirs();
            try {
                FileWriter fw = new FileWriter(fullPathFileNameUtilsInventario, false);
                PrintWriter pw = new PrintWriter(fw);
                pw.println("");

                pw.close();

            } catch (IOException e) {
                e.printStackTrace();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void apagarRupturaToTxtMapRuptura() {
        try {
            try {
                fullpath.mkdirs();

                try {
                    FileWriter fw = new FileWriter(fullPathFileNameRuptura, false);
                    PrintWriter pw = new PrintWriter(fw);

                    pw.println();
                    pw.close();

                    Toast.makeText(Apagar.this, "O arquivo ruptura.txt foi excluido com sucesso.", Toast.LENGTH_SHORT).show();


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

    private void apagarUtilsToTxtMapRuptura() {
        try {
            fullpath.mkdirs();
            try {
                FileWriter fw = new FileWriter(fullPathFileNameUtilsRuptura, false);
                PrintWriter pw = new PrintWriter(fw);
                pw.println("");

                pw.close();

            } catch (IOException e) {
                e.printStackTrace();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}