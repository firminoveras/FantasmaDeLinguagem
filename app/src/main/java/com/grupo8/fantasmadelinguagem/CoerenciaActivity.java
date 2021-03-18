package com.grupo8.fantasmadelinguagem;

import android.animation.ValueAnimator;
import android.app.Dialog;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextPaint;
import android.text.style.BackgroundColorSpan;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.magicgoop.tagsphere.TagSphereView;
import com.magicgoop.tagsphere.item.TagItem;
import com.magicgoop.tagsphere.item.TextTagItem;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.stream.Collectors;

public class CoerenciaActivity extends AppCompatActivity {
    
    
    private String[] mRoundTips;
    private String[] mAllRoundsPhrases;
    private String mRoundCorrectWord;
    private int randomWords = 0;
    private int mRound = 0;
    private int mPoints = 0;
    private int mWinPoints = 0;
    private boolean mMeaningBuyed = false;
    private boolean mSynonymBuyed = false;
    private final int[] mRoundPoints = {0, 0, 0, 0, 0};
    private final String[] mRoundWords = {"???", "???", "???", "???", "???"};
    
    private TagSphereView mWordsBall;
    private ProgressBar mRoundProgress;
    private TextView mTextDifficult;
    private TextView mTextRound;
    private TextView mTextWinPoints;
    private TextView mErroPenality;
    
    private MediaPlayer mSucessSound;
    private MediaPlayer mFailSound;
    private MediaPlayer mBuySound;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_coerencia);
        
        mTextDifficult = findViewById(R.id.Coerencia_Text_Nivel_Dificuldade);
        mRoundProgress = findViewById(R.id.Coerencia_Progress_Niveis);
        mTextRound = findViewById(R.id.Coerencia_Text_Nivel);
        mTextWinPoints = findViewById(R.id.Coerencia_Pontos);
        mErroPenality = findViewById(R.id.Coerencia_Label_LosePoints);
        
        mSucessSound = MediaPlayer.create(this, R.raw.acentuacao_sucess);
        mFailSound = MediaPlayer.create(this, R.raw.acentuacao_fail);
        mBuySound = MediaPlayer.create(this, R.raw.acentuacao_buy);
        
        mWordsBall = findViewById(R.id.Coerencia_Words_Ball);
        TextPaint textPaint = new TextPaint();
        textPaint.setAntiAlias(true);
        textPaint.setColor(Color.WHITE);
        textPaint.setFakeBoldText(true);
        textPaint.setTextSize(getResources().getDimension(R.dimen.coerencia_words_text_size));
        mWordsBall.setTextPaint(textPaint);
        
        String[] allEasyPhares = getResources().getStringArray(R.array.coerencia_sentences_easy);
        Random random = new Random();
        List<Integer> indexEasy = random.ints(0, allEasyPhares.length).distinct().limit(2).boxed().collect(Collectors.toList());
        mAllRoundsPhrases = new String[]{
                allEasyPhares[indexEasy.get(0)],
                allEasyPhares[indexEasy.get(1)],
                getResources().getStringArray(R.array.coerencia_sentences_medium)[random.nextInt(getResources().getStringArray(R.array.coerencia_sentences_medium).length)],
                getResources().getStringArray(R.array.coerencia_sentences_hard)[random.nextInt(getResources().getStringArray(R.array.coerencia_sentences_hard).length)],
                getResources().getStringArray(R.array.coerencia_sentences_very_hard)[random.nextInt(getResources().getStringArray(R.array.coerencia_sentences_very_hard).length)]};
        
        mWordsBall.setOnTagTapListener(tagItem -> {
            String selectedText = ((TextTagItem) tagItem).getText();
            TextView textSelectedView = findViewById(R.id.Coerencia_Selected_Word);
            mWordsBall.setEnabled(false);
            
            if (selectedText.toUpperCase().equals(mRoundCorrectWord.toUpperCase())) {
                textSelectedView.setBackgroundTintList(ColorStateList.valueOf(getColor(R.color.green)));
                ((Vibrator) getSystemService(Context.VIBRATOR_SERVICE)).vibrate(500);
                mPoints += mWinPoints;
                mTextWinPoints.setText(String.valueOf(mPoints));
                mRoundPoints[mRound - 1] = mWinPoints;
                mRoundWords[mRound - 1] = mRoundCorrectWord;
                mSucessSound.start();
                startNextRound();
            } else {
                textSelectedView.setBackgroundTintList(ColorStateList.valueOf(getColor(R.color.red)));
                if (mWinPoints > (10 * randomWords)) {
                    ((Vibrator) getSystemService(Context.VIBRATOR_SERVICE)).vibrate(new long[]{50, 100, 100, 500}, -1);
                    mFailSound.start();
                    changeWinPoints(-(10 * randomWords));
                    mErroPenality.setAlpha(1.0f);
                    ValueAnimator alphaAnim = ValueAnimator.ofFloat(1.0f, 0);
                    alphaAnim.addUpdateListener(animation -> mErroPenality.setAlpha((Float) animation.getAnimatedValue()));
                    alphaAnim.setDuration(1000);
                    alphaAnim.start();
                } else {
                    endGame(false);
                }
            }
            
            textSelectedView.setText(selectedText);
            textSelectedView.setAlpha(1.0f);
            ValueAnimator alphaAnim = ValueAnimator.ofFloat(1.0f, 0);
            alphaAnim.addUpdateListener(animation -> textSelectedView.setAlpha((Float) animation.getAnimatedValue()));
            alphaAnim.setDuration(1000);
            
            new Handler().postDelayed(alphaAnim::start, 500);
            new Handler().postDelayed(() -> mWordsBall.setEnabled(true), 1600);
        });
        
        findViewById(R.id.Coerencia_Button_Tip1).setOnClickListener(v -> {
            v.setEnabled(false);
            v.setAlpha(0.5f);
            mBuySound.start();
            mMeaningBuyed = true;
            updateTips();
            changeWinPoints((int) -(mWinPoints * 0.3));
        });
        
        findViewById(R.id.Coerencia_Button_Tip2).setOnClickListener(v -> {
            v.setEnabled(false);
            v.setAlpha(0.5f);
            mBuySound.start();
            mSynonymBuyed = true;
            updateTips();
            changeWinPoints((int) -(mWinPoints * 0.5));
        });
        
        startNextRound();
    }
    
    private void startNextRound() {
        mRound++;
        if (mRound <= 5) {
            mWinPoints = 0;
            if (mRound <= 2) {
                randomWords = 10;
                changeWinPoints(300);
                mTextDifficult.setText(R.string.coerencia_phrase_easy);
            } else if (mRound <= 3) {
                randomWords = 15;
                changeWinPoints(500);
                mTextDifficult.setText(R.string.coerencia_phrase_medium);
            } else if (mRound <= 4) {
                randomWords = 20;
                changeWinPoints(750);
                mTextDifficult.setText(R.string.coerencia_phrase_hard);
            } else {
                randomWords = 25;
                changeWinPoints(1000);
                mTextDifficult.setText(R.string.coerencia_phrase_very_hard);
            }
            
            findViewById(R.id.Coerencia_Button_Tip1).setEnabled(true);
            findViewById(R.id.Coerencia_Button_Tip2).setEnabled(true);
            findViewById(R.id.Coerencia_Button_Tip1).setAlpha(1.0f);
            findViewById(R.id.Coerencia_Button_Tip2).setAlpha(1.0f);
            
            mWordsBall.clearAllTags();
            mRoundProgress.setProgress(mRound);
            mTextRound.setText(String.format(Locale.getDefault(), "Nível %d", mRound));
            mMeaningBuyed = false;
            mSynonymBuyed = false;
            
            mErroPenality.setText(String.valueOf(-(10 * randomWords)));
            
            String[] allRandomWords = getResources().getStringArray(R.array.coerencia_random_words);
            
            List<Integer> randomWordsIndexes = new Random().ints(0, allRandomWords.length).distinct().limit(allRandomWords.length).boxed().collect(Collectors.toList());
            List<TagItem> randomWordsStrings = new ArrayList<>();
            for (int index = 0; index < randomWords; index++) {
                randomWordsStrings.add(new TextTagItem(allRandomWords[randomWordsIndexes.get(index)].toLowerCase()));
            }
            mWordsBall.addTagList(randomWordsStrings);
            
            String[] formatedPhrase = mAllRoundsPhrases[mRound - 1].split(";")[0].split(":");
            mRoundCorrectWord = formatedPhrase[1];
            formatedPhrase[1] = "XXXXXXXXXXXXXXX";
            String finalPhrase = formatedPhrase[0] + formatedPhrase[1] + formatedPhrase[2];
            
            SpannableString span = new SpannableString(finalPhrase);
            span.setSpan(new BackgroundColorSpan(getColor(R.color.white)), formatedPhrase[0].length(), formatedPhrase[0].length() + formatedPhrase[1].length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            ((TextView) findViewById(R.id.Coerencia_MainText)).setText(span);
            
            ((TextView) findViewById(R.id.Coerencia_MainTextAuthor)).setText(mAllRoundsPhrases[mRound - 1].split(";")[1]);
            mWordsBall.addTag(new TextTagItem(mRoundCorrectWord.toLowerCase()));
            
            mRoundTips = Arrays.copyOfRange(mAllRoundsPhrases[mRound - 1].split(";"), 2, mAllRoundsPhrases[mRound - 1].split(";").length);
            
            updateTips();
            
            mWordsBall.stopAutoRotation();
            mWordsBall.startAutoRotation(-1 + (new Random().nextFloat() * 2), -1 + (new Random().nextFloat() * 2));
        } else {
            endGame(true);
        }
        
    }
    
    private void updateTips() {
        String newTips = String.format(Locale.getDefault(), "CATEGORIA: %s\nSIGNIFICADO: %s\nSINÔNIMOS: %s\n", mRoundTips[0], mMeaningBuyed ? mRoundTips[1] : "???", mSynonymBuyed ? mRoundTips[2] : "???");
        new Handler().postDelayed(() -> ((TextView) findViewById(R.id.Coerencia_Tip)).setText(newTips), 200);
        findViewById(R.id.Coerencia_Tip_Lay).startAnimation(AnimationUtils.loadAnimation(this, R.anim.layout_flip));
    }
    
    private void changeWinPoints(int change) {
        mWinPoints += change;
        ((TextView) findViewById(R.id.Coerencia_Win_Points)).setText(String.valueOf(mWinPoints));
        ((TextView) findViewById(R.id.Coerencia_Win_Points)).startAnimation(AnimationUtils.loadAnimation(this, R.anim.acentuacao_key_pop));
    }
    
    private void endGame(boolean win) {
        mWordsBall.clearAllTags();
        ((TextView) findViewById(R.id.Coerencia_Win_Points)).setText("-");
        
        View alertContent = getLayoutInflater().inflate(R.layout.dialog_coerencia_end_game, findViewById(R.id.Coerencia_End_Root));
        
        ((TextView) alertContent.findViewById(R.id.Coerencia_End_Text)).setText(win ? "Você chegou até o final!" : "Não foi dessa vez!");
        
        ((TextView) alertContent.findViewById(R.id.Coerencia_End_Word1)).setText(mRoundWords[0]);
        ((TextView) alertContent.findViewById(R.id.Coerencia_End_Word2)).setText(mRoundWords[1]);
        ((TextView) alertContent.findViewById(R.id.Coerencia_End_Word3)).setText(mRoundWords[2]);
        ((TextView) alertContent.findViewById(R.id.Coerencia_End_Word4)).setText(mRoundWords[3]);
        ((TextView) alertContent.findViewById(R.id.Coerencia_End_Word5)).setText(mRoundWords[4]);
        
        ((TextView) alertContent.findViewById(R.id.Coerencia_End_Point1)).setText(String.valueOf(mRoundPoints[0]));
        ((TextView) alertContent.findViewById(R.id.Coerencia_End_Point2)).setText(String.valueOf(mRoundPoints[1]));
        ((TextView) alertContent.findViewById(R.id.Coerencia_End_Point3)).setText(String.valueOf(mRoundPoints[2]));
        ((TextView) alertContent.findViewById(R.id.Coerencia_End_Point4)).setText(String.valueOf(mRoundPoints[3]));
        ((TextView) alertContent.findViewById(R.id.Coerencia_End_Point5)).setText(String.valueOf(mRoundPoints[4]));
        
        ((TextView) alertContent.findViewById(R.id.Coerencia_End_Points)).setText(String.valueOf(mPoints));
        
        if (mPoints > getSharedPreferences("records", MODE_PRIVATE).getInt("recordCoerencia", 0)) {
            alertContent.findViewById(R.id.Coerencia_End_Record).setVisibility(View.VISIBLE);
            getSharedPreferences("records", MODE_PRIVATE).edit().putInt("recordCoerencia", mPoints).apply();
        }
        
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setView(alertContent);
        Dialog dialog = alert.show();
        dialog.setCancelable(false);
        
        alertContent.findViewById(R.id.Coerencia_End_Close).setOnClickListener(v -> {
            dialog.dismiss();
            finish();
        });
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