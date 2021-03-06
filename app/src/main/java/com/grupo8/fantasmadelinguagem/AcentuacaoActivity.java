package com.grupo8.fantasmadelinguagem;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.speech.tts.TextToSpeech;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;

import com.takusemba.spotlight.Spotlight;
import com.takusemba.spotlight.Target;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

public class AcentuacaoActivity extends AppCompatActivity implements TextToSpeech.OnInitListener {

    private final int[] mAllWinPointsPerRound = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
    private final String[] mAllRoundsCorrectAswers = {"???", "???", "???", "???", "???", "???", "???", "???", "???", "???"};
    private int mRound = 0;
    private int mPoints = 0;
    private int mWinPoints = 0;
    private boolean mTipBuyed = false;

    private Timer timer;
    private String[] roundWords;
    private TextToSpeech mSpeak;

    private TextView mTextWinPoints;
    private TextView mTextRound;
    private TextView mTextDifficult;
    private TextView mResposta;
    private ImageView mBuyTipButton;
    private ProgressBar mRoundProgress;
    private LinearLayout mLoadingLayout;

    private MediaPlayer mSucessSound;
    private MediaPlayer mFailSound;
    private MediaPlayer mBuySound;
    private MediaPlayer mPassSound;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_acentuacao);
        getWindow().setBackgroundDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.splash_background, null));

        mTextWinPoints = findViewById(R.id.Acentuacao_Pontos);
        mTextRound = findViewById(R.id.Acentuacao_Text_Nivel);
        mTextDifficult = findViewById(R.id.Acentuacao_Text_Nivel_Dificuldade);
        mRoundProgress = findViewById(R.id.Acentuacao_Progress_Niveis);
        mResposta = findViewById(R.id.Acentuacao_Text_Resposta);
        mLoadingLayout = findViewById(R.id.Acentuacao_Loading_Layout);
        mBuyTipButton = findViewById(R.id.Acentuacao_Button_Buy_Tip);

        mLoadingLayout.setVisibility(View.VISIBLE);
        ViewGroup.LayoutParams newLayout = mLoadingLayout.getLayoutParams();
        newLayout.height = getResources().getDisplayMetrics().heightPixels * 2;
        newLayout.width = getResources().getDisplayMetrics().heightPixels * 2;
        mLoadingLayout.setLayoutParams(newLayout);

        mSucessSound = MediaPlayer.create(this, R.raw.acentuacao_sucess);
        mFailSound = MediaPlayer.create(this, R.raw.acentuacao_fail);
        mBuySound = MediaPlayer.create(this, R.raw.acentuacao_buy);
        mPassSound = MediaPlayer.create(this, R.raw.acentuacao_pass);

        mSpeak = new TextToSpeech(this, this);
        mSpeak.setLanguage(new Locale("pt", "br"));
        mSpeak.setPitch((float) 0.8);
        mSpeak.setSpeechRate((float) 0.8);

        String[] allEasyWords = getResources().getStringArray(R.array.acentuacao_easy);
        String[] allMediumWords = getResources().getStringArray(R.array.acentuacao_medium);
        String[] allHardWord = getResources().getStringArray(R.array.acentuacao_hard);
        String[] allVeryHardWords = getResources().getStringArray(R.array.acentuacao_very_hard);
        Random random = new Random();
        List<Integer> indexEasy = random.ints(0, allEasyWords.length).distinct().limit(3).boxed().collect(Collectors.toList());
        List<Integer> indexMedium = random.ints(0, allMediumWords.length).distinct().limit(3).boxed().collect(Collectors.toList());
        List<Integer> indexHard = random.ints(0, allHardWord.length).distinct().limit(3).boxed().collect(Collectors.toList());
        roundWords = new String[]{
                allEasyWords[indexEasy.get(0)].toUpperCase(),
                allEasyWords[indexEasy.get(1)].toUpperCase(),
                allEasyWords[indexEasy.get(2)].toUpperCase(),
                allMediumWords[indexMedium.get(0)].toUpperCase(),
                allMediumWords[indexMedium.get(1)].toUpperCase(),
                allMediumWords[indexMedium.get(2)].toUpperCase(),
                allHardWord[indexHard.get(0)].toUpperCase(),
                allHardWord[indexHard.get(1)].toUpperCase(),
                allHardWord[indexHard.get(2)].toUpperCase(),
                allVeryHardWords[random.nextInt(allVeryHardWords.length)].toUpperCase()};

        findViewById(R.id.Acentuacao_Button_Pass).setOnClickListener(v -> {
            if (mPoints >= 500) {
                ((Vibrator) getSystemService(Context.VIBRATOR_SERVICE)).vibrate(new long[]{50, 100, 100, 300}, -1);
                mPassSound.start();
                mPoints -= 500;
                mTextWinPoints.setText(String.valueOf(mPoints));
                startNextRound();
            }
        });

        mBuyTipButton.setOnClickListener(v -> showTip());

        Handler handler = new Handler();
        findViewById(R.id.Acentuacao_Speak_Button).setOnClickListener(v -> {
            v.setEnabled(false);

            mSpeak.speak(roundWords[mRound - 1], TextToSpeech.QUEUE_FLUSH, null, null);
            findViewById(R.id.Acentuacao_Speak_Button_Icon).startAnimation(AnimationUtils.loadAnimation(this, R.anim.acentuacao_speak_anim));
            if (mWinPoints >= 50 && timer != null) changeWinPoints(-50);
            if (timer == null) {
                timer = new Timer(false);
                timer.scheduleAtFixedRate(new TimerTask() {
                    @Override
                    public void run() {
                        handler.post(() -> {
                            if (mWinPoints > 100) {
                                mWinPoints--;
                                ((TextView) findViewById(R.id.Acentuacao_Pontos_Valendo)).setText(String.format(Locale.getDefault(), "%d Pontos", mWinPoints));
                                findViewById(R.id.Acentuacao_Pontos_Valendo).startAnimation(AnimationUtils.loadAnimation(AcentuacaoActivity.this, R.anim.acentuacao_key_pop));
                            }
                        });
                    }
                }, 1000, 1000);
            }
            v.startAnimation(AnimationUtils.loadAnimation(this, R.anim.acentuacao_button));
            new Handler().postDelayed(() -> findViewById(R.id.Acentuacao_Speak_Button_Back1).startAnimation(AnimationUtils.loadAnimation(this, R.anim.acentuacao_button)), 50);
            new Handler().postDelayed(() -> findViewById(R.id.Acentuacao_Speak_Button_Back2).startAnimation(AnimationUtils.loadAnimation(this, R.anim.acentuacao_button)), 120);
            new Handler().postDelayed(() -> v.setEnabled(true), 1000);
        });

        findViewById(R.id.Acentuacao_Keyboard_Confirm).setOnClickListener(v -> {
            if (mResposta.getText().toString().length() > 0) {
                if (roundWords[mRound - 1].equals(mResposta.getText().toString())) {
                    ((Vibrator) getSystemService(Context.VIBRATOR_SERVICE)).vibrate(500);
                    mPoints += mWinPoints;
                    mTextWinPoints.setText(String.valueOf(mPoints));
                    mAllWinPointsPerRound[mRound - 1] = mWinPoints;
                    mSucessSound.start();
                    mAllRoundsCorrectAswers[mRound - 1] = roundWords[mRound - 1];
                    startNextRound();
                } else {
                    ((Vibrator) getSystemService(Context.VIBRATOR_SERVICE)).vibrate(new long[]{50, 100, 100, 500}, -1);
                    if (mWinPoints > 100) {
                        mFailSound.start();
                        changeWinPoints(-100);
                    } else {
                        endGame(false);
                    }
                }
            }
        });

        findViewById(R.id.Acentuacao_HowPlay).setOnClickListener(v -> showHowToPlay());

        initKeyboard();
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
        new Handler().postDelayed(() -> getWindow().setBackgroundDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.splash_background_white, null)), 1500);
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
        alertContent.findViewById(R.id.ExitConfirm_Yes).setOnClickListener(v -> {
            getWindow().setBackgroundDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.splash_background, null));
            dialog.dismiss();
            mLoadingLayout.setVisibility(View.VISIBLE);
            ValueAnimator anim = ValueAnimator.ofInt(1, getResources().getDisplayMetrics().heightPixels * 2);
            anim.addUpdateListener(animation -> {
                ViewGroup.LayoutParams params = mLoadingLayout.getLayoutParams();
                params.height = (int) animation.getAnimatedValue();
                params.width = (int) animation.getAnimatedValue();
                mLoadingLayout.setLayoutParams(params);
            });
            anim.setDuration(1000);
            anim.start();
            new Handler().postDelayed(() -> {
                super.onBackPressed();
                startActivity(new Intent(AcentuacaoActivity.this, MenuActivity.class));
                overridePendingTransition(R.anim.alpha_in, R.anim.alpha_out);
            }, 1000);
        });
    }

    @Override
    public void onInit(int status) {
        if(status == TextToSpeech.ERROR){
            View alertContent = getLayoutInflater().inflate(R.layout.dialog_error_game, findViewById(R.id.Error_Root));
            AlertDialog.Builder alert = new AlertDialog.Builder(this);
            alert.setView(alertContent);
            Dialog dialog = alert.show();
            dialog.setCancelable(false);
            alertContent.findViewById(R.id.Error_Confirm).setOnClickListener(v -> {
                getWindow().setBackgroundDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.splash_background, null));
                dialog.dismiss();
                mLoadingLayout.setVisibility(View.VISIBLE);
                ValueAnimator anim = ValueAnimator.ofInt(1, getResources().getDisplayMetrics().heightPixels * 2);
                anim.addUpdateListener(animation -> {
                    ViewGroup.LayoutParams params = mLoadingLayout.getLayoutParams();
                    params.height = (int) animation.getAnimatedValue();
                    params.width = (int) animation.getAnimatedValue();
                    mLoadingLayout.setLayoutParams(params);
                });
                anim.setDuration(1000);
                anim.start();
                new Handler().postDelayed(() -> {
                    super.onBackPressed();
                    startActivity(new Intent(AcentuacaoActivity.this, MenuActivity.class));
                    overridePendingTransition(R.anim.alpha_in, R.anim.alpha_out);
                }, 1000);
            });
        }
    }


    private void initKeyboard() {
        LinearLayout[] keyboardLines = {
                (LinearLayout) ((LinearLayout) findViewById(R.id.Acentuacao_Keyboard)).getChildAt(0),
                (LinearLayout) ((LinearLayout) findViewById(R.id.Acentuacao_Keyboard)).getChildAt(1),
                (LinearLayout) ((LinearLayout) findViewById(R.id.Acentuacao_Keyboard)).getChildAt(2)
        };

        AtomicBoolean longPress = new AtomicBoolean(false);
        AtomicReference<String> vogal = new AtomicReference<>("");
        final Handler handler = new Handler();
        Runnable mLongPressed = () -> {
            longPress.set(true);
            ((LinearLayout) findViewById(R.id.Acentuacao_Keyboard_Acentos)).removeAllViews();
            String[] variations = {};
            switch (vogal.get()) {
                case "A":
                    variations = new String[]{"??", "??", "??", "??", "A"};
                    break;
                case "E":
                    variations = new String[]{"??", "??", "??", "E"};
                    break;
                case "I":
                    variations = new String[]{"??", "??", "??", "I"};
                    break;
                case "O":
                    variations = new String[]{"??", "??", "??", "??", "O"};
                    break;
                case "U":
                    variations = new String[]{"??", "??", "??", "U"};
                    break;
                case "C":
                    variations = new String[]{"C", "??"};
                    break;
            }

            LinearLayout.LayoutParams layParams = new LinearLayout.LayoutParams((int) getResources().getDimension(R.dimen.acencuatao_key_size), (int) getResources().getDimension(R.dimen.acencuatao_key_size));
            int margin = (int) getResources().getDimension(R.dimen.acencuatao_key_margin);
            layParams.setMargins(margin, margin, margin, margin);
            for (String variation : variations) {
                TextView button = new TextView(this);
                button.setText(variation);
                button.setLayoutParams(layParams);
                button.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.bg_acentuacao_key_disabled, null));
                button.setGravity(Gravity.CENTER);
                button.setPadding(margin * 3, margin * 3, margin * 3, margin * 3);
                button.setTextSize(16);
                button.setOnClickListener(v -> {
                    ((Vibrator) getSystemService(Context.VIBRATOR_SERVICE)).vibrate(50);
                    mResposta.setText(mResposta.getText().toString().concat(variation));
                    ((LinearLayout) findViewById(R.id.Acentuacao_Keyboard_Acentos)).removeAllViews();
                });
                ((LinearLayout) findViewById(R.id.Acentuacao_Keyboard_Acentos)).addView(button);
            }
        };

        final View.OnTouchListener keyListener = (v, event) -> {
            v.setAlpha(event.getAction() == MotionEvent.ACTION_DOWN ? 0.5f : 1f);
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                ((LinearLayout) findViewById(R.id.Acentuacao_Keyboard_Acentos)).removeAllViews();
                longPress.set(false);
                if (((TextView) v).getText().toString().equals("A") ||
                        ((TextView) v).getText().toString().equals("E") ||
                        ((TextView) v).getText().toString().equals("C") ||
                        ((TextView) v).getText().toString().equals("I") ||
                        ((TextView) v).getText().toString().equals("O") ||
                        ((TextView) v).getText().toString().equals("U")) {
                    handler.postDelayed(mLongPressed, ViewConfiguration.getLongPressTimeout());
                    vogal.set(((TextView) v).getText().toString());
                }

                v.startAnimation(AnimationUtils.loadAnimation(this, R.anim.acentuacao_key_pop));
                ((Vibrator) getSystemService(Context.VIBRATOR_SERVICE)).vibrate(50);
                v.performClick();
            } else if (event.getAction() == MotionEvent.ACTION_UP) {
                if (!longPress.get()) {
                    longPress.set(false);
                    handler.removeCallbacks(mLongPressed);
                    mResposta.setText(mResposta.getText().toString().concat(((TextView) v).getText().toString()));
                }
            }
            return true;
        };

        for (LinearLayout keyboardLine : keyboardLines) {
            for (int index = 0; index < keyboardLine.getChildCount(); index++) {
                if (keyboardLine.getChildAt(index) instanceof TextView)
                    keyboardLine.getChildAt(index).setOnTouchListener(keyListener);
            }
        }

        findViewById(R.id.Acentuacao_Keyboard_Backspace).setOnTouchListener((v, event) -> {
            v.setAlpha(event.getAction() == MotionEvent.ACTION_DOWN ? 0.5f : 1f);
            if (mResposta.getText().length() > 0) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    v.startAnimation(AnimationUtils.loadAnimation(this, R.anim.acentuacao_key_pop));
                    ((Vibrator) getSystemService(Context.VIBRATOR_SERVICE)).vibrate(50);
                    mResposta.setText(mResposta.getText().toString().substring(0, mResposta.getText().length() - 1));
                    v.performClick();
                }
            }
            return true;
        });
    }

    private void showTip() {
        if (!mTipBuyed) {
            View alertContent = getLayoutInflater().inflate(R.layout.dialog_buy_tip_confirm, findViewById(R.id.BuyConfirm_Root));
            AlertDialog.Builder alert = new AlertDialog.Builder(this);
            alert.setView(alertContent);
            Dialog dialog = alert.show();
            dialog.setCancelable(false);
            alertContent.findViewById(R.id.BuyConfirm_No).setOnClickListener(v -> dialog.dismiss());
            alertContent.findViewById(R.id.BuyConfirm_Yes).setOnClickListener(v -> {
                int tipId;
                if (roundWords[mRound - 1].toUpperCase().contains("??") ||
                        roundWords[mRound - 1].toUpperCase().contains("??") ||
                        roundWords[mRound - 1].toUpperCase().contains("??") ||
                        roundWords[mRound - 1].toUpperCase().contains("??") ||
                        roundWords[mRound - 1].toUpperCase().contains("??"))
                    tipId = R.array.acentuacao_tip_agudo;
                else if (roundWords[mRound - 1].toUpperCase().contains("??") ||
                        roundWords[mRound - 1].toUpperCase().contains("??") ||
                        roundWords[mRound - 1].toUpperCase().contains("??") ||
                        roundWords[mRound - 1].toUpperCase().contains("??") ||
                        roundWords[mRound - 1].toUpperCase().contains("??"))
                    tipId = R.array.acentuacao_tip_crase;
                else if (roundWords[mRound - 1].toUpperCase().contains("??") ||
                        roundWords[mRound - 1].toUpperCase().contains("??") ||
                        roundWords[mRound - 1].toUpperCase().contains("??") ||
                        roundWords[mRound - 1].toUpperCase().contains("??") ||
                        roundWords[mRound - 1].toUpperCase().contains("??"))
                    tipId = R.array.acentuacao_tip_circunflexo;
                else if (roundWords[mRound - 1].toUpperCase().contains("??") ||
                        roundWords[mRound - 1].toUpperCase().contains("??"))
                    tipId = R.array.acentuacao_tip_til;
                else tipId = R.array.acentuacao_tip_generic;
                String[] tip = getResources().getStringArray(tipId)[new Random().nextInt(getResources().getStringArray(tipId).length - 1)].split("#");

                View tipAlertContent = getLayoutInflater().inflate(R.layout.dialog_acentuacao_dica, findViewById(R.id.Acentuacao_Dica_Root));
                ((TextView) tipAlertContent.findViewById(R.id.Acentuacao_Dica_Text)).setText(tip[0]);
                ((TextView) tipAlertContent.findViewById(R.id.Acentuacao_Dica_Referencia)).setText(tip[1]);
                AlertDialog.Builder tipAlert = new AlertDialog.Builder(this);
                tipAlert.setView(tipAlertContent);
                tipAlert.show();

                mBuySound.start();
                dialog.dismiss();
                mTipBuyed = true;
                mBuyTipButton.setEnabled(false);
                mBuyTipButton.setAlpha(0.5f);
                changeWinPoints(-(mWinPoints / 2));
            });
        }
    }

    private void changeWinPoints(int change) {
        mWinPoints += change;
        ((TextView) findViewById(R.id.Acentuacao_Pontos_Valendo)).setText(String.format(Locale.getDefault(), "%d Pontos", mWinPoints));

        ((TextView) findViewById(R.id.Acentuacao_SpreakCount)).setText(String.format("%s %s pontos", change >= 0 ? "+" : "", change));
        findViewById(R.id.Acentuacao_SpreakCount).setBackgroundTintList(ColorStateList.valueOf(getColor(change >= 0 ? R.color.green : R.color.red)));

        ValueAnimator anim = ValueAnimator.ofFloat(1.0f, 0);
        anim.addUpdateListener(animation -> findViewById(R.id.Acentuacao_SpreakCount).setAlpha((Float) animation.getAnimatedValue()));
        anim.setDuration(1000);
        anim.start();
    }

    private void startNextRound() {
        if (timer != null) timer.cancel();
        timer = null;
        mResposta.setText("");
        mRound++;
        if (mRound <= 10) {
            if (mRound <= 3) {
                mTextDifficult.setText(R.string.acentuacao_easy_word);
            } else if (mRound <= 6) {
                mTextDifficult.setText(R.string.acentuacao_medium_word);
            } else if (mRound <= 9) {
                mTextDifficult.setText(R.string.acentuacao_hard_word);
            } else {
                mTextDifficult.setText(R.string.acentuacao_veryhard_word);
            }

            mBuyTipButton.setEnabled(true);
            mBuyTipButton.setAlpha(1.0f);
            mTipBuyed = false;

            mWinPoints = 300 + (mRound * 100);

            mRoundProgress.setProgress(mRound);
            mTextRound.setText(String.format(Locale.getDefault(), "N??vel %d", mRound));
            ((TextView) findViewById(R.id.Acentuacao_Pontos_Valendo)).setText(String.format(Locale.getDefault(), "%d Pontos", mWinPoints));
        } else {
            endGame(true);
        }

    }

    private void showHowToPlay() {
        String[] helpStrings = getResources().getStringArray(R.array.tutorial_acentuacao);
        List<Target> targetList = new ArrayList<>();

        targetList.add(new TargetBuilder(this, findViewById(R.id.Acentuacao_Text_Nivel_Dificuldade), 120f, helpStrings[0].split("#")[0], helpStrings[0].split("#")[1], R.layout.tutorial_acentuacao).getTarget());
        targetList.add(new TargetBuilder(this, findViewById(R.id.Acentuacao_Speak_Button), 200f, helpStrings[1].split("#")[0], helpStrings[1].split("#")[1], R.layout.tutorial_acentuacao).getTarget());
        targetList.add(new TargetBuilder(this, findViewById(R.id.Acentuacao_Pontos_Valendo), 120f, helpStrings[2].split("#")[0], helpStrings[2].split("#")[1], R.layout.tutorial_acentuacao).getTarget());
        targetList.add(new TargetBuilder(this, findViewById(R.id.Acentuacao_Text_Resposta), 140f, helpStrings[3].split("#")[0], helpStrings[3].split("#")[1], R.layout.tutorial_acentuacao).getTarget());
        targetList.add(new TargetBuilder(this, findViewById(R.id.Acentuacao_Pontos), 200f, helpStrings[4].split("#")[0], helpStrings[4].split("#")[1], R.layout.tutorial_acentuacao).getTarget());
        targetList.add(new TargetBuilder(this, findViewById(R.id.Acentuacao_Button_Buy_Tip), 100f, helpStrings[5].split("#")[0], helpStrings[5].split("#")[1], R.layout.tutorial_acentuacao).getTarget());
        targetList.add(new TargetBuilder(this, findViewById(R.id.Acentuacao_Keyboard_Confirm), 100f, helpStrings[6].split("#")[0], helpStrings[6].split("#")[1], R.layout.tutorial_acentuacao).getTarget());
        targetList.add(new TargetBuilder(this, findViewById(R.id.Acentuacao_Button_Pass), 100f, helpStrings[7].split("#")[0], helpStrings[7].split("#")[1], R.layout.tutorial_acentuacao).getTarget());

        Spotlight spotlight = new Spotlight.Builder(this)
                .setTargets(targetList)
                .setBackgroundColor(getColor(R.color.spotlightBackground))
                .setDuration(1000L)
                .setAnimation(new DecelerateInterpolator(2f))
                .setContainer(findViewById(R.id.Acentuacao_Root))
                .build();

        spotlight.start();

        for (int index = 0; index < targetList.size(); index++)
            Objects.requireNonNull(targetList.get(index).getOverlay()).findViewById(R.id.Acentuacao_Tutorial_Next).setOnClickListener(v -> spotlight.next());
    }

    private void endGame(boolean win) {
        findViewById(R.id.Acentuacao_Keyboard_Confirm).setEnabled(false);

        View alertContent = getLayoutInflater().inflate(R.layout.dialog_acentuacao_end_game, findViewById(R.id.Acentuacao_End_Root));

        ((TextView) alertContent.findViewById(R.id.Acentuacao_End_Text)).setText(win ? "Voc?? chegou at?? o final!" : "N??o foi dessa vez!");

        ((TextView) alertContent.findViewById(R.id.Acentuacao_End_Word1)).setText(mAllRoundsCorrectAswers[0]);
        ((TextView) alertContent.findViewById(R.id.Acentuacao_End_Word2)).setText(mAllRoundsCorrectAswers[1]);
        ((TextView) alertContent.findViewById(R.id.Acentuacao_End_Word3)).setText(mAllRoundsCorrectAswers[2]);
        ((TextView) alertContent.findViewById(R.id.Acentuacao_End_Word4)).setText(mAllRoundsCorrectAswers[3]);
        ((TextView) alertContent.findViewById(R.id.Acentuacao_End_Word5)).setText(mAllRoundsCorrectAswers[4]);
        ((TextView) alertContent.findViewById(R.id.Acentuacao_End_Word6)).setText(mAllRoundsCorrectAswers[5]);
        ((TextView) alertContent.findViewById(R.id.Acentuacao_End_Word7)).setText(mAllRoundsCorrectAswers[6]);
        ((TextView) alertContent.findViewById(R.id.Acentuacao_End_Word8)).setText(mAllRoundsCorrectAswers[7]);
        ((TextView) alertContent.findViewById(R.id.Acentuacao_End_Word9)).setText(mAllRoundsCorrectAswers[8]);
        ((TextView) alertContent.findViewById(R.id.Acentuacao_End_Word10)).setText(mAllRoundsCorrectAswers[9]);

        ((TextView) alertContent.findViewById(R.id.Acentuacao_End_Point1)).setText(String.valueOf(mAllWinPointsPerRound[0]));
        ((TextView) alertContent.findViewById(R.id.Acentuacao_End_Point2)).setText(String.valueOf(mAllWinPointsPerRound[1]));
        ((TextView) alertContent.findViewById(R.id.Acentuacao_End_Point3)).setText(String.valueOf(mAllWinPointsPerRound[2]));
        ((TextView) alertContent.findViewById(R.id.Acentuacao_End_Point4)).setText(String.valueOf(mAllWinPointsPerRound[3]));
        ((TextView) alertContent.findViewById(R.id.Acentuacao_End_Point5)).setText(String.valueOf(mAllWinPointsPerRound[4]));
        ((TextView) alertContent.findViewById(R.id.Acentuacao_End_Point6)).setText(String.valueOf(mAllWinPointsPerRound[5]));
        ((TextView) alertContent.findViewById(R.id.Acentuacao_End_Point7)).setText(String.valueOf(mAllWinPointsPerRound[6]));
        ((TextView) alertContent.findViewById(R.id.Acentuacao_End_Point8)).setText(String.valueOf(mAllWinPointsPerRound[7]));
        ((TextView) alertContent.findViewById(R.id.Acentuacao_End_Point9)).setText(String.valueOf(mAllWinPointsPerRound[8]));
        ((TextView) alertContent.findViewById(R.id.Acentuacao_End_Point10)).setText(String.valueOf(mAllWinPointsPerRound[9]));

        ((TextView) alertContent.findViewById(R.id.Acentuacao_End_Points)).setText(String.valueOf(mPoints));

        if (mPoints > getSharedPreferences("records", MODE_PRIVATE).getInt("recordAcentuacao", 0)) {
            alertContent.findViewById(R.id.Acentuacao_End_Record).setVisibility(View.VISIBLE);
            getSharedPreferences("records", MODE_PRIVATE).edit().putInt("recordAcentuacao", mPoints).apply();
        }

        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setView(alertContent);
        Dialog dialog = alert.show();
        dialog.setCancelable(false);

        alertContent.findViewById(R.id.Acentuacao_End_Close).setOnClickListener(v -> {
            dialog.dismiss();
            startActivity(new Intent(AcentuacaoActivity.this, MenuActivity.class));
            finish();
        });
    }

}