package com.rasgo.compa.feature;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.airbnb.lottie.LottieAnimationView;
import com.rasgo.compa.R;
import com.rasgo.compa.feature.auth.LoginActivity;

public class SplashActivity extends AppCompatActivity {

    LottieAnimationView lottieAnimationView;
    ImageView letrasView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_splash);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        lottieAnimationView = findViewById(R.id.lottieAnimation);
        letrasView=findViewById(R.id.letter);

        letrasView.animate().translationY(-60).setDuration(1000).setStartDelay(1000);
//        lottieAnimationView.animate().translationY(1800).setDuration(1000).setStartDelay(1000);
        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                startActivity(intent);
                finish();
            }
        }, 3500);
    }
}