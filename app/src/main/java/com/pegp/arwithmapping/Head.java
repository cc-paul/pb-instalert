package com.pegp.arwithmapping;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.VideoView;

public class Head extends AppCompatActivity {
    LinearLayout lnBack,lnHead,lnArm,lnKnee,lnFace,lnChest;
    TextView tvTitle;
    VideoView vdArm1,vdArm2,vdChest1,vdChest2,vdFace1,vdHead1,vdKnee1;

    Intent intent;
    Integer rawId;
    String path;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_head);

        lnBack = findViewById(R.id.lnBack);
        lnHead = findViewById(R.id.lnHead);
        lnArm = findViewById(R.id.lnArm);
        lnKnee = findViewById(R.id.lnKnee);
        lnFace = findViewById(R.id.lnFace);
        lnChest = findViewById(R.id.lnChest);
        tvTitle = findViewById(R.id.tvTitle);
        vdArm1 = findViewById(R.id.vdArm1);
        vdArm2 = findViewById(R.id.vdArm2);
        vdChest1 = findViewById(R.id.vdChest1);
        vdChest2 = findViewById(R.id.vdChest2);
        vdFace1 = findViewById(R.id.vdFace1);
        vdHead1 = findViewById(R.id.vdHead1);
        vdKnee1 = findViewById(R.id.vdKnee1);

        lnBack.setOnClickListener(view -> {
            super.onBackPressed();
        });

        intent  = getIntent();
        Bundle extras = intent.getExtras();

        switch (extras.getString("bodyPart")) {
            case "head" :
                tvTitle.setText("Head");
                lnHead.setVisibility(View.VISIBLE);
                lnArm.setVisibility(View.GONE);
                lnKnee.setVisibility(View.GONE);
                lnFace.setVisibility(View.GONE);
                lnChest.setVisibility(View.GONE);

                vdHead1.setVideoURI(Uri.parse("android.resource://" + getPackageName() + "/" + getResources().getIdentifier("head",  "raw", getPackageName())));
                vdHead1.start();

                break;
            case "arm" :
                tvTitle.setText("Arm");
                lnHead.setVisibility(View.GONE);
                lnArm.setVisibility(View.VISIBLE);
                lnKnee.setVisibility(View.GONE);
                lnFace.setVisibility(View.GONE);
                lnChest.setVisibility(View.GONE);

                vdArm1.setVideoURI(Uri.parse("android.resource://" + getPackageName() + "/" + getResources().getIdentifier("wound",  "raw", getPackageName())));
                vdArm1.start();

                break;
            case "knee" :
                tvTitle.setText("Knee");
                lnHead.setVisibility(View.GONE);
                lnArm.setVisibility(View.GONE);
                lnKnee.setVisibility(View.VISIBLE);
                lnFace.setVisibility(View.GONE);
                lnChest.setVisibility(View.GONE);

                vdKnee1.setVideoURI(Uri.parse("android.resource://" + getPackageName() + "/" + getResources().getIdentifier("knee",  "raw", getPackageName())));
                vdKnee1.start();

                break;
            case "face" :
                tvTitle.setText("Face");
                lnHead.setVisibility(View.GONE);
                lnArm.setVisibility(View.GONE);
                lnKnee.setVisibility(View.GONE);
                lnFace.setVisibility(View.VISIBLE);
                lnChest.setVisibility(View.GONE);

                vdFace1.setVideoURI(Uri.parse("android.resource://" + getPackageName() + "/" + getResources().getIdentifier("face",  "raw", getPackageName())));
                vdFace1.start();

                break;
            case "chest" :
                tvTitle.setText("Chest");
                lnHead.setVisibility(View.GONE);
                lnArm.setVisibility(View.GONE);
                lnKnee.setVisibility(View.GONE);
                lnFace.setVisibility(View.GONE);
                lnChest.setVisibility(View.VISIBLE);

                vdChest1.setVideoURI(Uri.parse("android.resource://" + getPackageName() + "/" + getResources().getIdentifier("chest",  "raw", getPackageName())));
                vdChest1.start();

                break;
        }

        vdArm1.setOnClickListener(view -> {
            if (vdArm1.isPlaying()) {
                vdArm1.pause();
            } else {
                vdArm1.start();
            }
        });

        vdChest1.setOnClickListener(view -> {
            if (vdChest1.isPlaying()) {
                vdChest1.pause();
            } else {
                vdChest1.start();
            }
        });

        vdFace1.setOnClickListener(view -> {
            if (vdFace1.isPlaying()) {
                vdFace1.pause();
            } else {
                vdFace1.start();
            }
        });

        vdHead1.setOnClickListener(view -> {
            if (vdHead1.isPlaying()) {
                vdHead1.pause();
            } else {
                vdHead1.start();
            }
        });

        vdKnee1.setOnClickListener(view -> {
            if (vdKnee1.isPlaying()) {
                vdKnee1.pause();
            } else {
                vdKnee1.start();
            }
        });
    }

    public void openYoutube(View v) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com/@SAFESTEPS"));
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setPackage("com.google.android.youtube");
        startActivity(intent);
    }

    public void getLink(View v) {
        openLink(v.getTag().toString());
    }

    public void openLink(String link) {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(link));
        startActivity(browserIntent);
    }
}