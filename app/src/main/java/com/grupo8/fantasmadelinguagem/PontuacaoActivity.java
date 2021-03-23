package com.grupo8.fantasmadelinguagem;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.BackgroundColorSpan;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;

import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class PontuacaoActivity extends AppCompatActivity {
    
    private TextView mInsertButton;
    private String[] mAllRoundsPhrases;
    private int mRound = 0;
    private int mCursorIndex = 0;
    private int mWinPoints = 0;
    private TextView mTextDifficult;
    private TextView mTextPhrase;
    private TextView mTextAuthor;
    
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pontuacao);
        mInsertButton = findViewById(R.id.Pontuacao_Insert_Button);
        mTextDifficult = findViewById(R.id.Pontuacao_Text_Nivel_Dificuldade);
        mTextPhrase = findViewById(R.id.Pontuacao_MainText);
        mTextAuthor = findViewById(R.id.Pontuacao_MainTextAuthor);
        
        findViewById(R.id.Pontuacao_Select_Ponto).setOnClickListener(v -> setInsertChar(".", "Ponto", (ImageView) v));
        findViewById(R.id.Pontuacao_Select_Virgula).setOnClickListener(v -> setInsertChar(",", "Vírgula", (ImageView) v));
        findViewById(R.id.Pontuacao_Select_DoisPontos).setOnClickListener(v -> setInsertChar(":", "Dois Pontos", (ImageView) v));
        findViewById(R.id.Pontuacao_PontoEVirgula).setOnClickListener(v -> setInsertChar(";", "Ponto e Vírgula", (ImageView) v));
        findViewById(R.id.Pontuacao_Select_Exclamacao).setOnClickListener(v -> setInsertChar("!", "Exclamação", (ImageView) v));
        findViewById(R.id.Pontuacao_Select_Interrogacao).setOnClickListener(v -> setInsertChar("?", "Integgoração", (ImageView) v));
        findViewById(R.id.Pontuacao_Select_Reticencias).setOnClickListener(v -> setInsertChar("...", "Reticências", (ImageView) v));
        findViewById(R.id.Pontuacao_Select_AspasAbrir).setOnClickListener(v -> setInsertChar("\"", "Aspas Duplas", (ImageView) v));
        findViewById(R.id.Pontuacao_Select_Apostrofo).setOnClickListener(v -> setInsertChar("'", "Aspas", (ImageView) v));
        
        setInsertChar(".", "Ponto", findViewById(R.id.Pontuacao_Select_Ponto));
        
        String[] allEasyPhares = getResources().getStringArray(R.array.pontuacao_easy);
        Random random = new Random();
        List<Integer> indexEasy = random.ints(0, allEasyPhares.length).distinct().limit(2).boxed().collect(Collectors.toList());
        mAllRoundsPhrases = new String[]{
                allEasyPhares[indexEasy.get(0)],
                allEasyPhares[indexEasy.get(1)],
                getResources().getStringArray(R.array.pontuacao_medium)[random.nextInt(getResources().getStringArray(R.array.pontuacao_medium).length)],
                getResources().getStringArray(R.array.pontuacao_hard)[random.nextInt(getResources().getStringArray(R.array.pontuacao_hard).length)],
                getResources().getStringArray(R.array.pontuacao_very_hard)[random.nextInt(getResources().getStringArray(R.array.pontuacao_very_hard).length)]};
        
        findViewById(R.id.Pontuacao_Left_Button).setOnClickListener(v -> {
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
        });
        
        findViewById(R.id.Pontuacao_Right_Button).setOnClickListener(v -> {
            String text = mTextPhrase.getText().toString();
            if (mCursorIndex < text.length() - 1) {
                SpannableString outputString = new SpannableString(text);
                
                if (mCursorIndex + 1 >= text.length()) mCursorIndex = 0;
                mCursorIndex = text.indexOf(' ', mCursorIndex + 1);
                outputString.setSpan(new BackgroundColorSpan(getColor(R.color.white)), mCursorIndex, mCursorIndex + 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                
                mTextPhrase.setText(outputString);
            }
        });
        
        mInsertButton.setOnClickListener(v -> {
            String text = mTextPhrase.getText().toString();
            if(mCursorIndex > 0 && mCursorIndex < text.length() - 1){
                text = (text.substring(0, mCursorIndex) + ((TextView)v).getText()+ (text.substring(mCursorIndex)));
    
                mTextPhrase.setText(text);
            }
        });
        
        startNextRound();
    }
    
    private void startNextRound() {
        mRound++;
        mCursorIndex = 0;
        if (mRound <= 5) {
            mWinPoints = 0;
            if (mRound <= 2) {
                //changeWinPoints(300);
                mTextDifficult.setText(R.string.coerencia_phrase_easy);
            } else if (mRound <= 3) {
                //changeWinPoints(500);
                mTextDifficult.setText(R.string.coerencia_phrase_medium);
            } else if (mRound <= 4) {
                //changeWinPoints(750);
                mTextDifficult.setText(R.string.coerencia_phrase_hard);
            } else {
                //changeWinPoints(1000);
                mTextDifficult.setText(R.string.coerencia_phrase_very_hard);
            }
        }
        
        String roundPhrase = mAllRoundsPhrases[mRound - 1].split(";")[0];
        String roundAuthor = mAllRoundsPhrases[mRound - 1].split(";")[1];
        
        String roundPhraseFormated = removePontuations(roundPhrase);
        
        mTextPhrase.setText(roundPhraseFormated);
        mTextAuthor.setText(roundAuthor);
        
    }
    
    private String removePontuations(String sentence) {
        sentence = sentence.replace(".", "");
        sentence = sentence.replace(",", "");
        sentence = sentence.replace(":", "");
        sentence = sentence.replace(";", "");
        sentence = sentence.replace("!", "");
        sentence = sentence.replace("?", "");
        sentence = sentence.replace("...", "");
        sentence = sentence.replace("\"", "");
        sentence = sentence.replace("'", "");
        if (!sentence.endsWith(" ")) sentence += " ";
        return sentence;
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
    
    
    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(newBase);
        if (newBase.getResources().getConfiguration().fontScale > 0.85f) {
            final Configuration override = new Configuration(newBase.getResources().getConfiguration());
            override.fontScale = 0.85f;
            applyOverrideConfiguration(override);
        }
    }
}