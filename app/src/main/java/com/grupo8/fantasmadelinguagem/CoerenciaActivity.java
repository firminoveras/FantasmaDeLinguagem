package com.grupo8.fantasmadelinguagem;

import android.animation.ValueAnimator;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextPaint;
import android.text.style.BackgroundColorSpan;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.magicgoop.tagsphere.TagSphereView;
import com.magicgoop.tagsphere.item.TagItem;
import com.magicgoop.tagsphere.item.TextTagItem;
import com.magicgoop.tagsphere.utils.EasingFunction;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.stream.Collectors;

public class CoerenciaActivity extends AppCompatActivity {
    
    private TagSphereView mWordsBall;
    private String mRoundCorrectWord;
    private String[] mRoundTips;
    
    private int mPoints = 0;
    private int mWinPoints = 0;
    private boolean mMeaningBuyed = false;
    private boolean mSynonymBuyed = false;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_coerencia);
        
        mWordsBall = findViewById(R.id.Coerencia_Words_Ball);
        TextPaint textPaint = new TextPaint();
        textPaint.setAntiAlias(true);
        textPaint.setColor(Color.WHITE);
        textPaint.setFakeBoldText(true);
        textPaint.setTextSize(getResources().getDimension(R.dimen.coerencia_words_text_size));
        mWordsBall.setTextPaint(textPaint);
        
        startNewRound(25, getResources().getStringArray(R.array.coerencia_sentences)[0]);
        
        mWordsBall.setOnTagTapListener(tagItem -> {
            String selectedText = ((TextTagItem) tagItem).getText();
            TextView textSelectedView = findViewById(R.id.Coerencia_Selected_Word);
            mWordsBall.setEnabled(false);
    
            if(selectedText.toUpperCase().equals(mRoundCorrectWord.toUpperCase())){
                textSelectedView.setBackgroundTintList(ColorStateList.valueOf(getColor(R.color.green)));
            }else{
                textSelectedView.setBackgroundTintList(ColorStateList.valueOf(getColor(R.color.red)));
            }
            
            textSelectedView.setText(selectedText);
            textSelectedView.setAlpha(1.0f);
            ValueAnimator alphaAnim = ValueAnimator.ofFloat(1.0f, 0);
            alphaAnim.addUpdateListener(animation -> textSelectedView.setAlpha((Float) animation.getAnimatedValue()));
            alphaAnim.setDuration(1000);
            
            new Handler().postDelayed(alphaAnim::start, 500);
            new Handler().postDelayed(() -> mWordsBall.setEnabled(true), 1600);
        });
    }
    
    private void startNewRound(int randomWords, String roundText) {
        mMeaningBuyed = false;
        mSynonymBuyed = false;
        
        mWordsBall.clearAllTags();
        
        String[] allRandomWords = getResources().getStringArray(R.array.coerencia_random_words);
        
        List<Integer> randomWordsIndexes = new Random().ints(0, allRandomWords.length).distinct().limit(allRandomWords.length).boxed().collect(Collectors.toList());
        List<TagItem> randomWordsStrings = new ArrayList<>();
        for (int index = 0; index < randomWords; index++) {
            randomWordsStrings.add(new TextTagItem(allRandomWords[randomWordsIndexes.get(index)].toLowerCase()));
        }
        mWordsBall.addTagList(randomWordsStrings);
        
        String[] formatedPhrase = roundText.split(";")[0].split(":");
        mRoundCorrectWord = formatedPhrase[1];
        
        formatedPhrase[1] = "XXXXXXXXXXXXXXX";
        String finalPhrase = formatedPhrase[0] + formatedPhrase[1] + formatedPhrase[2];
        
        SpannableString span = new SpannableString(finalPhrase);
        span.setSpan(new BackgroundColorSpan(getColor(R.color.white)), formatedPhrase[0].length(), formatedPhrase[0].length() + formatedPhrase[1].length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        ((TextView) findViewById(R.id.Coerencia_MainText)).setText(span);
        
        
        ((TextView) findViewById(R.id.Coerencia_MainTextAuthor)).setText(roundText.split(";")[1]);
        mWordsBall.addTag(new TextTagItem(mRoundCorrectWord.toLowerCase()));
        
        mRoundTips = Arrays.copyOfRange(roundText.split(";"), 2, roundText.split(";").length);
        
        updateTips();
        
        mWordsBall.stopAutoRotation();
        mWordsBall.startAutoRotation(-1 + (new Random().nextFloat() * 2), -1 + (new Random().nextFloat() * 2));
    }
    
    private void updateTips() {
        ((TextView) findViewById(R.id.Coerencia_Tip)).setText(String.format(Locale.getDefault(), "CATEGORIA: %s\nSIGNIFICADO: %s\nSINÔNIMOS: %s\n", mRoundTips[0], mMeaningBuyed ? mRoundTips[1] : "???", mSynonymBuyed ? mRoundTips[2] : "???"));
    }
}