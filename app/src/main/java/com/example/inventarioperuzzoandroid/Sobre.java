package com.example.inventarioperuzzoandroid;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class Sobre extends AppCompatActivity {

    Button mVoltarBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTitle("Sobre");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sobre);

        //Inicializa a view
        mVoltarBtn = findViewById(R.id.voltarBtn);

        //Funções internas do botão
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

}