package com.grupo8.fantasmadelinguagem;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.takusemba.spotlight.Target;
import com.takusemba.spotlight.shape.Circle;

public class TargetBuilder {
    private final Target mTarget;

    public TargetBuilder(Context context, View spotView, float size, String title, String message, int layId) {
        FrameLayout layModel = new FrameLayout(context);
        View view = LayoutInflater.from(context).inflate(layId, layModel);
        ((TextView) view.findViewById(R.id.Acentuacao_Tutorial_Title)).setText(title);
        ((TextView) view.findViewById(R.id.Acentuacao_Tutorial_Text)).setText(message);
        mTarget = new Target.Builder()
                .setAnchor(spotView)
                .setShape(new Circle(size))
                .setOverlay(view)
                .build();
    }

    public Target getTarget() {
        return mTarget;
    }
}
