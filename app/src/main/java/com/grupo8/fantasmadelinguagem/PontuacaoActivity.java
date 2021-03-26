package com.grupo8.fantasmadelinguagem;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.app.Dialog;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.BackgroundColorSpan;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;

import com.google.android.material.tabs.TabLayout;

import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.stream.Collectors;

public class PontuacaoActivity extends AppCompatActivity {

    private TextView mTextDifficult;
    private TextView mTextPhrase;
    private TextView mTextAuthor;
    private TextView mTextPoints;
    private TextView mInsertButton;
    private LinearLayout mLoadingLayout;

    private String[] mAllRoundsCorrectPhrases;
    private final String[] mAllEndRoundsCorrectPhrases = {"???", "???", "???", "???", "???"};
    private final String[] mAllEndRoundsAswerPhrases = {"???", "???", "???", "???", "???"};
    private final int[] mAllWinPointsPerRound = {0, 0, 0, 0, 0};
    private int mRound = 0;
    private int mCursorIndex = 0;
    private int mWinPoints = 0;
    private int mPoints = 0;
    private boolean mTipBuyed = false;

    private MediaPlayer mSucessSound;
    private MediaPlayer mFailSound;
    private MediaPlayer mBuySound;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pontuacao);
        getWindow().setBackgroundDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.splash_background, null));

        mInsertButton = findViewById(R.id.Pontuacao_Insert_Button);
        mTextDifficult = findViewById(R.id.Pontuacao_Text_Nivel_Dificuldade);
        mTextPhrase = findViewById(R.id.Pontuacao_MainText);
        mTextAuthor = findViewById(R.id.Pontuacao_MainTextAuthor);
        mTextPoints = findViewById(R.id.Pontuacao_Pontos);
        mLoadingLayout = findViewById(R.id.Pontuacao_Loading_Layout);

        mLoadingLayout.setVisibility(View.VISIBLE);
        ViewGroup.LayoutParams newLayout = mLoadingLayout.getLayoutParams();
        newLayout.height = getResources().getDisplayMetrics().heightPixels * 2;
        newLayout.width = getResources().getDisplayMetrics().heightPixels * 2;
        mLoadingLayout.setLayoutParams(newLayout);

        mSucessSound = MediaPlayer.create(this, R.raw.acentuacao_sucess);
        mFailSound = MediaPlayer.create(this, R.raw.acentuacao_fail);
        mBuySound = MediaPlayer.create(this, R.raw.acentuacao_buy);


        findViewById(R.id.Pontuacao_Select_Ponto).setOnClickListener(v -> setInsertChar(".", "Ponto", (ImageView) v));
        findViewById(R.id.Pontuacao_Select_Virgula).setOnClickListener(v -> setInsertChar(",", "Vírgula", (ImageView) v));
        findViewById(R.id.Pontuacao_Select_DoisPontos).setOnClickListener(v -> setInsertChar(":", "Dois Pontos", (ImageView) v));
        findViewById(R.id.Pontuacao_PontoEVirgula).setOnClickListener(v -> setInsertChar(";", "Ponto e Vírgula", (ImageView) v));
        findViewById(R.id.Pontuacao_Select_Exclamacao).setOnClickListener(v -> setInsertChar("!", "Exclamação", (ImageView) v));
        findViewById(R.id.Pontuacao_Select_Interrogacao).setOnClickListener(v -> setInsertChar("?", "Interrogação", (ImageView) v));

        setInsertChar(".", "Ponto", findViewById(R.id.Pontuacao_Select_Ponto));

        String[] allEasyPhares = getResources().getStringArray(R.array.pontuacao_easy);
        Random random = new Random();
        List<Integer> indexEasy = random.ints(0, allEasyPhares.length).distinct().limit(2).boxed().collect(Collectors.toList());
        mAllRoundsCorrectPhrases = new String[]{
                allEasyPhares[indexEasy.get(0)],
                allEasyPhares[indexEasy.get(1)],
                getResources().getStringArray(R.array.pontuacao_medium)[random.nextInt(getResources().getStringArray(R.array.pontuacao_medium).length)],
                getResources().getStringArray(R.array.pontuacao_hard)[random.nextInt(getResources().getStringArray(R.array.pontuacao_hard).length)],
                getResources().getStringArray(R.array.pontuacao_very_hard)[random.nextInt(getResources().getStringArray(R.array.pontuacao_very_hard).length)]};

        findViewById(R.id.Pontuacao_Left_Button).setOnClickListener(v -> cursorPreviousWord());

        findViewById(R.id.Pontuacao_Right_Button).setOnClickListener(v -> cursorNextWord());

        mInsertButton.setOnClickListener(v -> {
            String text = mTextPhrase.getText().toString();
            if (mCursorIndex > 0 && mCursorIndex < text.length()) {
                text = (text.substring(0, mCursorIndex) + ((TextView) v).getText() + (text.substring(mCursorIndex)));
                mTextPhrase.setText(text);
                cursorNextWord();
            }
        });

        mInsertButton.setOnLongClickListener(v -> {
            showDialogTip();
            return true;
        });

        findViewById(R.id.Pontuacao_Button_Clear).setOnClickListener(v -> clearPontuacoes());

        findViewById(R.id.Pontuacao_Button_Confirm).setOnClickListener(v -> {
            mAllEndRoundsCorrectPhrases[mRound - 1] = mAllRoundsCorrectPhrases[mRound - 1].split("#")[0];
            mAllEndRoundsAswerPhrases[mRound - 1] = mTextPhrase.getText().toString().trim();
            if (mAllRoundsCorrectPhrases[mRound - 1].split("#")[0].trim().equals(mTextPhrase.getText().toString().trim())) {
                mSucessSound.start();
                mPoints += mWinPoints;
                mAllWinPointsPerRound[mRound - 1] = mWinPoints;
                mTextPoints.setText(String.valueOf(mPoints));
                startNextRound();
            } else {
                mFailSound.start();
                if (mWinPoints / 2 > 100) {
                    changeWinPoints(-(mWinPoints / 2));
                } else {
                    endGame(false);
                }
            }
        });
        startNextRound();
        new Handler().postDelayed(() -> {
            ValueAnimator anim = ValueAnimator.ofInt(getResources().getDisplayMetrics().heightPixels * 2, 1);
            anim.addUpdateListener(animation -> {
                ViewGroup.LayoutParams params = mLoadingLayout.getLayoutParams();
                params.height = (int) animation.getAnimatedValue();
                params.width = (int) animation.getAnimatedValue();
                mLoadingLayout.setLayoutParams(params);
            });
            anim.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {

                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    mLoadingLayout.setVisibility(View.GONE);
                }

                @Override
                public void onAnimationCancel(Animator animation) {

                }

                @Override
                public void onAnimationRepeat(Animator animation) {

                }
            });
            anim.setDuration(1000);
            anim.start();
        }, 1000);
        new Handler().postDelayed(() ->  getWindow().setBackgroundDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.splash_background_white, null)), 1500);
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(newBase);
        if (newBase.getResources().getConfiguration().fontScale > 0.85f) {
            final Configuration override = new Configuration(newBase.getResources().getConfiguration());
            override.fontScale = 0.85f;
            applyOverrideConfiguration(override);
        }
    }

    @Override
    public void onBackPressed() {
        View alertContent = getLayoutInflater().inflate(R.layout.dialog_exit_game_confirm, findViewById(R.id.ExitConfirm_Root));
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setView(alertContent);
        Dialog dialog = alert.show();
        dialog.setCancelable(false);
        alertContent.findViewById(R.id.ExitConfirm_No).setOnClickListener(v -> dialog.dismiss());
        alertContent.findViewById(R.id.ExitConfirm_Yes).setOnClickListener(v -> super.onBackPressed());
    }

    private void clearPontuacoes() {
        String sentence = mAllRoundsCorrectPhrases[mRound - 1].split("#")[0];
        sentence = sentence.replace(".", "");
        sentence = sentence.replace(",", "");
        sentence = sentence.replace(":", "");
        sentence = sentence.replace(";", "");
        sentence = sentence.replace("!", "");
        sentence = sentence.replace("?", "");
        if (!sentence.endsWith(" ")) sentence += " ";
        mTextPhrase.setText(sentence);
        mCursorIndex = 0;
        cursorNextWord();
    }

    private void showDialogTip() {
        if (!mTipBuyed) {
            View alertContent = getLayoutInflater().inflate(R.layout.dialog_buy_tip_confirm, findViewById(R.id.BuyConfirm_Root));
            AlertDialog.Builder alert = new AlertDialog.Builder(this);
            alert.setView(alertContent);
            Dialog dialog = alert.show();
            dialog.setCancelable(false);
            alertContent.findViewById(R.id.BuyConfirm_No).setOnClickListener(v -> dialog.dismiss());
            alertContent.findViewById(R.id.BuyConfirm_Yes).setOnClickListener(v -> {
                mBuySound.start();
                dialog.dismiss();
                String[] listTips;
                switch (mInsertButton.getText().charAt(0)) {
                    default:
                        listTips = getResources().getStringArray(R.array.pontuacao_dica_ponto);
                        break;
                    case ',':
                        listTips = getResources().getStringArray(R.array.pontuacao_dica_virgula);
                        break;
                    case ':':
                        listTips = getResources().getStringArray(R.array.pontuacao_dica_doispontos);
                        break;
                    case '!':
                        listTips = getResources().getStringArray(R.array.pontuacao_dica_exclamacao);
                        break;
                    case ';':
                        listTips = getResources().getStringArray(R.array.pontuacao_dica_pontoevirgula);
                        break;
                    case '?':
                        listTips = getResources().getStringArray(R.array.pontuacao_dica_interrogacao);
                        break;
                }

                String[] tip = listTips[new Random().nextInt(listTips.length-1)].split("#");

                View tipAlertContent = getLayoutInflater().inflate(R.layout.dialog_acentuacao_dica, findViewById(R.id.Acentuacao_Dica_Root));
                ((TextView) tipAlertContent.findViewById(R.id.Acentuacao_Dica_Text)).setText(tip[0]);
                ((TextView) tipAlertContent.findViewById(R.id.Acentuacao_Dica_Referencia)).setText(tip[1]);
                AlertDialog.Builder tipAlert = new AlertDialog.Builder(this);
                tipAlert.setView(tipAlertContent);
                tipAlert.show();
                mTipBuyed = true;
                changeWinPoints(-(mWinPoints / 2));
            });
        }
    }

    private void cursorNextWord() {
        String text = mTextPhrase.getText().toString();
        if (mCursorIndex < text.length() - 1) {
            SpannableString outputString = new SpannableString(text);
            if (mCursorIndex + 1 >= text.length()) mCursorIndex = 0;
            mCursorIndex = text.indexOf(' ', mCursorIndex + 1);
            outputString.setSpan(new BackgroundColorSpan(getColor(R.color.white)), mCursorIndex, mCursorIndex + 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            mTextPhrase.setText(outputString);
        }
    }

    private void cursorPreviousWord() {
        if (mCursorIndex > 0) {
            String text = mTextPhrase.getText().toString();
            SpannableString outputString = new SpannableString(text);
            for (int index = mCursorIndex - 1; index >= 0; index--) {
                if (text.charAt(index) == ' ') {
                    mCursorIndex = index;
                    break;
                }
            }
            outputString.setSpan(new BackgroundColorSpan(getColor(R.color.white)), mCursorIndex, mCursorIndex + 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            mTextPhrase.setText(outputString);
        }
    }

    private void startNextRound() {
        mRound++;
        if (mRound <= 5) {
            mWinPoints = 0;
            if (mRound <= 2) {
                changeWinPoints(300);
                mTextDifficult.setText(R.string.coerencia_phrase_easy);
            } else if (mRound <= 3) {
                changeWinPoints(500);
                mTextDifficult.setText(R.string.coerencia_phrase_medium);
            } else if (mRound <= 4) {
                changeWinPoints(750);
                mTextDifficult.setText(R.string.coerencia_phrase_hard);
            } else {
                changeWinPoints(1000);
                mTextDifficult.setText(R.string.coerencia_phrase_very_hard);
            }

            ((ProgressBar)findViewById(R.id.Pontuacao_Progress_Niveis)).setProgress(mRound);
            ((TextView)findViewById(R.id.Pontuacao_Text_Nivel)).setText(String.format(Locale.getDefault(), "Nível %d", mRound));

            clearPontuacoes();
            mTextAuthor.setText(mAllRoundsCorrectPhrases[mRound - 1].split("#")[1]);

            mTipBuyed = false;
        } else {
            endGame(true);
        }
    }

    private void changeWinPoints(int change) {
        mWinPoints += change;
        ((TextView) findViewById(R.id.Pontuacao_Win_Points)).setText(String.valueOf(mWinPoints));
        findViewById(R.id.Pontuacao_Win_Points).startAnimation(AnimationUtils.loadAnimation(this, R.anim.acentuacao_key_pop));
    }

    private void setInsertChar(String character, String name, ImageView selectedImageView) {
        LinearLayout selectLayout = findViewById(R.id.Pontuacao_Select_Layout);
        for (int index = 0; index < selectLayout.getChildCount(); index++) {
            selectLayout.getChildAt(index).setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.bg_double_circle_ripple_unselected, null));
            ((ImageView) selectLayout.getChildAt(index)).setImageTintList(ColorStateList.valueOf(Color.argb(255, 150, 150, 150)));
        }

        selectedImageView.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.bg_double_circle_ripple_selected, null));
        selectedImageView.setImageTintList(ColorStateList.valueOf(getColor(R.color.background)));

        mInsertButton.setText(character);
        mInsertButton.startAnimation(AnimationUtils.loadAnimation(this, R.anim.acentuacao_key_pop));
        ((TextView) findViewById(R.id.Pontuacao_Insert_Button_Description)).setText(name);
    }

    private void endGame(boolean win) {
        mTextDifficult.setEnabled(false);
        mTextPhrase.setEnabled(false);
        mTextAuthor.setEnabled(false);
        mTextPoints.setEnabled(false);
        mInsertButton.setEnabled(false);
        findViewById(R.id.Pontuacao_Button_Confirm).setEnabled(false);
        findViewById(R.id.Pontuacao_Button_Clear).setEnabled(false);

        View alertContent = getLayoutInflater().inflate(R.layout.dialog_pontuacao_end_game, findViewById(R.id.Pontuacao_End_Root));

        ((TextView) alertContent.findViewById(R.id.Pontuacao_End_Text)).setText(win ? "Você chegou até o final!" : "Não foi dessa vez!");

        TabLayout endGameTabs = alertContent.findViewById(R.id.Pontuacao_End_Tabs);
        endGameTabs.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                ((TextView) alertContent.findViewById(R.id.Pontuacao_End_TextOriginal)).setText(mAllEndRoundsCorrectPhrases[tab.getPosition()]);
                ((TextView) alertContent.findViewById(R.id.Pontuacao_End_TextAnswer)).setText(mAllEndRoundsAswerPhrases[tab.getPosition()]);
                ((TextView) alertContent.findViewById(R.id.Pontuacao_End_TabPoints)).setText(String.format(Locale.getDefault(), "%d Pontos", mAllWinPointsPerRound[tab.getPosition()]));
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });

        endGameTabs.selectTab(endGameTabs.getTabAt(0), true);
        ((TextView) alertContent.findViewById(R.id.Pontuacao_End_TextOriginal)).setText(mAllEndRoundsCorrectPhrases[0]);
        ((TextView) alertContent.findViewById(R.id.Pontuacao_End_TextAnswer)).setText(mAllEndRoundsAswerPhrases[0]);
        ((TextView) alertContent.findViewById(R.id.Pontuacao_End_TabPoints)).setText(String.format(Locale.getDefault(), "%d Pontos", mAllWinPointsPerRound[0]));

        ((TextView) alertContent.findViewById(R.id.Pontuacao_End_Points)).setText(String.valueOf(mPoints));

        if (mPoints > getSharedPreferences("records", MODE_PRIVATE).getInt("recordPontuacao", 0)) {
            alertContent.findViewById(R.id.Pontuacao_End_Record).setVisibility(View.VISIBLE);
            getSharedPreferences("records", MODE_PRIVATE).edit().putInt("recordPontuacao", mPoints).apply();
        }

        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setView(alertContent);
        Dialog dialog = alert.show();
        dialog.setCancelable(false);

        alertContent.findViewById(R.id.Pontuacao_End_Close).setOnClickListener(v -> {
            dialog.dismiss();
            finish();
        });

    }

}