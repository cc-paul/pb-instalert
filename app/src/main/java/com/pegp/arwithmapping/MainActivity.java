package com.pegp.arwithmapping;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.LinearLayout;

import cn.pedant.SweetAlert.SweetAlertDialog;
import pl.droidsonroids.gif.GifImageView;

public class MainActivity extends AppCompatActivity {
    LinearLayout lnBack;
    GifImageView imgHead,imgFace,imgChest,imgKnee,imgArm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        lnBack = findViewById(R.id.lnBack);
        imgHead = findViewById(R.id.imgHead);
        imgFace = findViewById(R.id.imgFace);
        imgChest = findViewById(R.id.imgChest);
        imgKnee = findViewById(R.id.imgKnee);
        imgArm = findViewById(R.id.imgArm);

        lnBack.setOnClickListener(view -> {
            super.onBackPressed();
        });

        new SweetAlertDialog(this, SweetAlertDialog.WARNING_TYPE)
                .setTitleText("Tutorial")
                .setContentText("Select any of the point to see the tutorial")
                .setConfirmText("Okay")
                .show();

        imgHead.setOnClickListener(view -> {
            Intent gotoDetails = new Intent(MainActivity.this, Head.class);
            gotoDetails.putExtra("bodyPart","head");
            startActivity(gotoDetails);
        });

        imgFace.setOnClickListener(view -> {
            Intent gotoDetails = new Intent(MainActivity.this, Head.class);
            gotoDetails.putExtra("bodyPart","face");
            startActivity(gotoDetails);
        });

        imgChest.setOnClickListener(view -> {
            Intent gotoDetails = new Intent(MainActivity.this, Head.class);
            gotoDetails.putExtra("bodyPart","chest");
            startActivity(gotoDetails);
        });

        imgKnee.setOnClickListener(view -> {
            Intent gotoDetails = new Intent(MainActivity.this, Head.class);
            gotoDetails.putExtra("bodyPart","knee");
            startActivity(gotoDetails);
        });

        imgArm.setOnClickListener(view -> {
            Intent gotoDetails = new Intent(MainActivity.this, Head.class);
            gotoDetails.putExtra("bodyPart","arm");
            startActivity(gotoDetails);
        });
    }
}