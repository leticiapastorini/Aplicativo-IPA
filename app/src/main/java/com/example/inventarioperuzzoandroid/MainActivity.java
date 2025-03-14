package com.example.inventarioperuzzoandroid;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;


//Versão 1.1 Build 03/2022

/* Variações de Modelos/Versões do Android
 Em caso de modificações de versão do android verifique/modifique as interações
 do Environment.getExternalStorageDirectory();  /  System.getenv("EXTERNAL_STORAGE")
 no Inventário/Ruptura e Apagar  E TAMBÉM o AndroidManifest.xml, é necessário ter
 android:requestLegacyExternalStorage="true"   em application
*/


public class MainActivity extends AppCompatActivity {

    //Mudar a String "loja" para a loja desejada
    public static String loja = "";
    public static Integer inventarioBipadosProdutos = 0;
    public static Integer inventarioBipadosTotal = 0;
    public static Integer rupturaBipadosProdutos = 0;
    public static Integer rupturaBipadosTotal = 0;

    Button mGoToInventarioBtn;
    Button mGoToRupturaBtn;
    Button mGoToApagarBtn;
    Button mGoToSobreBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTitle("Inventario Peruzzo Android");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Inicializa as views
        mGoToInventarioBtn = findViewById(R.id.goToInventarioBtn);
        mGoToRupturaBtn = findViewById(R.id.goToRupturaBtn);
        mGoToApagarBtn = findViewById(R.id.goToApagarBtn);
        mGoToSobreBtn = findViewById(R.id.goToSobreBtn);

        //Configura o roteamento dos buttons
        mGoToInventarioBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openInventario();
            }
        });

        mGoToRupturaBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openRuptura();
            }
        });

        mGoToApagarBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openApagar();
            }
        });

        mGoToSobreBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openSobre();
            }
        });


    }

    //Configura a view dos roteamentos dos buttons
    public void openInventario() {
        Intent intent = new Intent(this, Inventario.class);
        startActivity(intent);
    }

    public void openRuptura() {
        Intent intent = new Intent(this, Ruptura.class);
        startActivity(intent);
    }

    public void openApagar() {
        Intent intent = new Intent(this, Apagar.class);
        startActivity(intent);
    }

    public void openSobre() {
        Intent intent = new Intent(this, Sobre.class);
        startActivity(intent);
    }


}