package com.grupo8.fantasmadelinguagem;

import androidx.appcompat.app.AppCompatActivity;

import android.animation.ValueAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.LinearLayout;

public class MainActivity extends AppCompatActivity {

    LinearLayout mLayAcentuacao;
    LinearLayout mLayPontuacao;
    LinearLayout mLayCoerencia;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mLayAcentuacao=findViewById(R.id.Splash_Acentuacao_Layout);
        mLayPontuacao=findViewById(R.id.Splash_Pontuacao_Layout);
        mLayCoerencia=findViewById(R.id.Splash_Coerencia_Layout);

        findViewById(R.id.Splash_Acentuacao_Play).setOnClickListener(v -> startActivity(new Intent(MainActivity.this, AcentuacaoActivity.class)));
    }

    private void setGameLayoutVisible(LinearLayout layout, boolean visible){
        int maxHeight = layout.getPaddingBottom() + layout.getPaddingTop();
        for(int index = 0; index < layout.getChildCount(); index++){
            maxHeight += layout.getChildAt(index).getHeight();
            maxHeight += ((ViewGroup.MarginLayoutParams)layout.getChildAt(index).getLayoutParams()).topMargin;
            maxHeight += ((ViewGroup.MarginLayoutParams)layout.getChildAt(index).getLayoutParams()).bottomMargin;
        }

        ValueAnimator anim = ValueAnimator.ofInt(layout.getHeight(), visible ? maxHeight : 0);
        anim.addUpdateListener(animation -> {
            ViewGroup.LayoutParams newLay = layout.getLayoutParams();
            newLay.height = (int) animation.getAnimatedValue();
            layout.setLayoutParams(newLay);
        });
        anim.setDuration(300);
        anim.start();
    }
}