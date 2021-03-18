package com.grupo8.fantasmadelinguagem;

import android.animation.ValueAnimator;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;

import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    
    private LinearLayout mLayAcentuacao;
    private LinearLayout mLayPontuacao;
    private LinearLayout mLayCoerencia;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        getWindow().setBackgroundDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.splash_background, null));
        
        mLayAcentuacao = findViewById(R.id.Splash_Acentuacao_Layout);
        mLayPontuacao = findViewById(R.id.Splash_Pontuacao_Layout);
        mLayCoerencia = findViewById(R.id.Splash_Coerencia_Layout);
        
        ((TextView) findViewById(R.id.Splash_Version)).setText(String.format("Versão %s", BuildConfig.VERSION_NAME));
        
        findViewById(R.id.Splash_Info).setOnClickListener(v -> showAppInfoDialog());
        
        updateRecords();
        
        findViewById(R.id.Splash_Acentuacao_Title).setOnClickListener(v -> setGameLayoutVisible(mLayAcentuacao));
        findViewById(R.id.Splash_Pontuacao_Title).setOnClickListener(v -> setGameLayoutVisible(mLayPontuacao));
        findViewById(R.id.Splash_Coerencia_Title).setOnClickListener(v -> setGameLayoutVisible(mLayCoerencia));
        
        findViewById(R.id.Splash_Acentuacao_Play).setOnClickListener(v -> startActivity(new Intent(MainActivity.this, AcentuacaoActivity.class)));
        findViewById(R.id.Splash_Coerencia_Play).setOnClickListener(v -> startActivity(new Intent(MainActivity.this, CoerenciaActivity.class)));
    }
    
    private void showAppInfoDialog() {
        View alertContent = getLayoutInflater().inflate(R.layout.dialog_splash_info, findViewById(R.id.Info_Root));
        alertContent.findViewById(R.id.Info_ButtonGithub).setOnClickListener(v -> startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/firminoveras/FantasmaDeLinguagem"))));
        alertContent.findViewById(R.id.Info_LicenceGoogle).setOnClickListener(v -> startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.apache.org/licenses/LICENSE-2.0.txt"))));
        
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setView(alertContent);
        alert.show();
    }
    
    private void setGameLayoutVisible(LinearLayout layout) {
        for (LinearLayout lay : new LinearLayout[]{mLayAcentuacao, mLayPontuacao, mLayCoerencia}) {
            ValueAnimator anim2 = ValueAnimator.ofInt(lay.getHeight(), 1);
            anim2.addUpdateListener(animation -> {
                if (lay != layout) {
                    ViewGroup.LayoutParams newLay = lay.getLayoutParams();
                    newLay.height = (int) animation.getAnimatedValue();
                    lay.setLayoutParams(newLay);
                }
            });
            anim2.setDuration(300);
            anim2.start();
        }
        
        ValueAnimator anim = ValueAnimator.ofInt(layout.getHeight(), (int) getResources().getDimension(R.dimen.Splash_Game_Layout_Max_Heigth));
        anim.addUpdateListener(animation -> {
            ViewGroup.LayoutParams newLay = layout.getLayoutParams();
            newLay.height = (int) animation.getAnimatedValue();
            layout.setLayoutParams(newLay);
        });
        anim.setDuration(300);
        anim.start();
        
    }
    
    @Override protected void onResume() {
        super.onResume();
        updateRecords();
    }
    
    private void updateRecords() {
        int recordAcentuacao = getSharedPreferences("records", MODE_PRIVATE).getInt("recordAcentuacao", 0);
        int recordPontuacao = getSharedPreferences("records", MODE_PRIVATE).getInt("recordPontuacao", 0);
        int recordCoerencia = getSharedPreferences("records", MODE_PRIVATE).getInt("recordCoerencia", 0);
        
        ((TextView) findViewById(R.id.Splash_Acentuacao_Record)).setText(String.format(Locale.getDefault(), "%d Pontos", recordAcentuacao));
        ((TextView) findViewById(R.id.Splash_Pontuacao_Record)).setText(String.format(Locale.getDefault(), "%d Pontos", recordPontuacao));
        ((TextView) findViewById(R.id.Splash_Coerencia_Record)).setText(String.format(Locale.getDefault(), "%d Pontos", recordCoerencia));
    }
}