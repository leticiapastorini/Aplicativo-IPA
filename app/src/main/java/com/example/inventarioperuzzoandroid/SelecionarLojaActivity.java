package com.example.inventarioperuzzoandroid;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

public class SelecionarLojaActivity extends AppCompatActivity {
    private EditText editLoja;
    private Button buttonConfirmarLoja;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.selecionar_loja);
        
        editLoja = findViewById(R.id.editLoja);
        buttonConfirmarLoja = findViewById(R.id.buttonConfirmarLoja);

        buttonConfirmarLoja.setOnClickListener(view -> {
            String lojaDigitada = editLoja.getText().toString().trim();
            if (!lojaDigitada.isEmpty()) {
                SharedPreferences prefs = getSharedPreferences("InventarioPrefs", MODE_PRIVATE);
                SharedPreferences.Editor editor = prefs.edit();
                editor.putString("loja", lojaDigitada);
                editor.apply();
                Intent intent = new Intent(SelecionarLojaActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            } else {
                editLoja.setError("Digite um número de loja");
            }
        });
    }
}


