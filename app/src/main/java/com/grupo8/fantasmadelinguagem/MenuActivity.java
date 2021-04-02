package com.grupo8.fantasmadelinguagem;

import android.animation.ValueAnimator;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;

import java.util.Locale;

public class MenuActivity extends AppCompatActivity {

    private LinearLayout mLayAcentuacao;
    private LinearLayout mLayPontuacao;
    private LinearLayout mLayCoerencia;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

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

        findViewById(R.id.Splash_Acentuacao_Play).setOnClickListener(v -> startGame(new Intent(MenuActivity.this, AcentuacaoActivity.class)));
        findViewById(R.id.Splash_Pontuacao_Play).setOnClickListener(v -> startGame(new Intent(MenuActivity.this, PontuacaoActivity.class)));
        findViewById(R.id.Splash_Coerencia_Play).setOnClickListener(v -> startGame(new Intent(MenuActivity.this, CoerenciaActivity.class)));

        startSplashAnimations();
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateRecords();
        ImageView ripple = findViewById(R.id.Splash_RoundImage);
        ripple.setVisibility(View.INVISIBLE);
        ViewGroup.LayoutParams params = ripple.getLayoutParams();
        params.height = 1;
        params.width = 1;
        ripple.setLayoutParams(params);
    }

    private void startGame(Intent intent) {
        ImageView ripple = findViewById(R.id.Splash_RoundImage);
        ripple.setVisibility(View.VISIBLE);
        ValueAnimator anim = ValueAnimator.ofInt(0, getResources().getDisplayMetrics().heightPixels * 2);
        anim.addUpdateListener(animation -> {
            ViewGroup.LayoutParams params = ripple.getLayoutParams();
            params.height = (int) animation.getAnimatedValue();
            params.width = (int) animation.getAnimatedValue();
            ripple.setLayoutParams(params);
        });
        anim.setDuration(1000);
        anim.start();
        new Handler().postDelayed(() -> {
            startActivity(intent);
            overridePendingTransition(R.anim.alpha_in, R.anim.alpha_out);
            finish();
        }, 1200);
    }

    private void startSplashAnimations() {
        FrameLayout layoutIcon = findViewById(R.id.Splash_Logo_Icon);
        LinearLayout layoutTitle = findViewById(R.id.Splash_Logo_Title);
        FrameLayout layoutMain = findViewById(R.id.Splash_Logo);
        layoutMain.post(() -> {
            int width = layoutMain.getWidth();
            int height = layoutMain.getHeight();
            ValueAnimator animIconHorizontalExpand = ValueAnimator.ofInt((int) getResources().getDimension(R.dimen.splash_icon_size), width - ((int) getResources().getDimension(R.dimen.splash_header_padding) * 2));
            animIconHorizontalExpand.addUpdateListener(animation -> {
                ViewGroup.LayoutParams newLay = layoutIcon.getLayoutParams();
                newLay.width = (int) animation.getAnimatedValue();
                layoutIcon.setLayoutParams(newLay);
            });
            animIconHorizontalExpand.setDuration(500);

            ValueAnimator animIconVerticalExpand = ValueAnimator.ofInt((int) getResources().getDimension(R.dimen.splash_icon_size), (int) getResources().getDimension(R.dimen.splash_header_height) - ((int) getResources().getDimension(R.dimen.splash_header_padding) * 2));
            animIconVerticalExpand.addUpdateListener(animation -> {
                ViewGroup.LayoutParams newLay = layoutIcon.getLayoutParams();
                newLay.height = (int) animation.getAnimatedValue();
                layoutIcon.setLayoutParams(newLay);
            });
            animIconVerticalExpand.setDuration(500);

            ValueAnimator animTitleAlpha = ValueAnimator.ofFloat(layoutTitle.getAlpha(), 1);
            animTitleAlpha.addUpdateListener(animation -> layoutTitle.setAlpha((float) animation.getAnimatedValue()));
            animTitleAlpha.setDuration(500);

            ValueAnimator animMainVerticalExpand = ValueAnimator.ofInt(height, (int) getResources().getDimension(R.dimen.splash_header_height));
            animMainVerticalExpand.addUpdateListener(animation -> {
                ViewGroup.LayoutParams newLay = layoutMain.getLayoutParams();
                newLay.height = (int) animation.getAnimatedValue();
                layoutMain.setLayoutParams(newLay);
            });
            animMainVerticalExpand.setDuration(500);


            new Handler().postDelayed(animIconHorizontalExpand::start, 1000);
            new Handler().postDelayed(animIconVerticalExpand::start, 1800);
            new Handler().postDelayed(animTitleAlpha::start, 1800);
            new Handler().postDelayed(animMainVerticalExpand::start, 4000);
        });


    }

    private void showAppInfoDialog() {
        View alertContent = getLayoutInflater().inflate(R.layout.dialog_splash_info, findViewById(R.id.Info_Root));
        alertContent.findViewById(R.id.Info_ButtonGithub).setOnClickListener(v -> startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/firminoveras/FantasmaDeLinguagem"))));
        alertContent.findViewById(R.id.Info_LicenceGoogle).setOnClickListener(v -> startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.apache.org/licenses/LICENSE-2.0.txt"))));
        alertContent.findViewById(R.id.Info_LicenceSpotlight).setOnClickListener(v -> startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.apache.org/licenses/LICENSE-2.0.txt"))));
        alertContent.findViewById(R.id.Info_LicenceTagSphere).setOnClickListener(v -> startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.apache.org/licenses/LICENSE-2.0.txt"))));

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

    private void updateRecords() {
        int recordAcentuacao = getSharedPreferences("records", MODE_PRIVATE).getInt("recordAcentuacao", 0);
        int recordPontuacao = getSharedPreferences("records", MODE_PRIVATE).getInt("recordPontuacao", 0);
        int recordCoerencia = getSharedPreferences("records", MODE_PRIVATE).getInt("recordCoerencia", 0);

        ((TextView) findViewById(R.id.Splash_Acentuacao_Record)).setText(String.format(Locale.getDefault(), "%d Pontos", recordAcentuacao));
        ((TextView) findViewById(R.id.Splash_Pontuacao_Record)).setText(String.format(Locale.getDefault(), "%d Pontos", recordPontuacao));
        ((TextView) findViewById(R.id.Splash_Coerencia_Record)).setText(String.format(Locale.getDefault(), "%d Pontos", recordCoerencia));
    }

}