package com.mehmetesen.qrcode;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class Qrgenerate extends MainActivity {

    EditText etInput;
    Button btGenerate;
    ImageView ivOutput;
    Button share;
    Button download;
    OutputStream outputStream;
    private AdView mAdView;
    private InterstitialAd mInterstitialAd;
    private int generate=0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qrgenerate);
        etInput = findViewById(R.id.et_input);
        btGenerate = findViewById(R.id.generate);
        ivOutput = findViewById(R.id.output);
        share = findViewById(R.id.share);
        download = findViewById(R.id.download);
        share.setVisibility(View.INVISIBLE);
        download.setVisibility(View.INVISIBLE);
        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
            }
        });

        mAdView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);
        AdRequest adRequest1 = new AdRequest.Builder().build();
        InterstitialAd.load(this,"ca-app-pub-4026568549714819/4239059223", adRequest,
                new InterstitialAdLoadCallback() {
                    @Override
                    public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                        // The mInterstitialAd reference will be null until
                        // an ad is loaded.
                        mInterstitialAd = interstitialAd;
                    }
                });


    }


    public void generate(View view) {
        if(Network(this)){
            try {
                generate++;
                if(generate ==2 || generate ==3){
                    if (mInterstitialAd != null) {
                        mInterstitialAd.show(Qrgenerate.this);
                    }

                }
                if (etInput.getText() != null) {
                    share.setVisibility(View.VISIBLE);
                    download.setVisibility(View.VISIBLE);

                    String sText = etInput.getText().toString().trim();
                    MultiFormatWriter writer = new MultiFormatWriter();
                    try {
                        BitMatrix matrix = writer.encode(sText, BarcodeFormat.QR_CODE, 350, 350);

                        BarcodeEncoder encoder = new BarcodeEncoder();
                        Bitmap bitmap = encoder.createBitmap(matrix);
                        ivOutput.setImageBitmap(bitmap);

                        InputMethodManager manager = (InputMethodManager) getSystemService(
                                Context.INPUT_METHOD_SERVICE
                        );
                        manager.hideSoftInputFromWindow(etInput.getApplicationWindowToken(), 0);

                    } catch (WriterException e) {
                        e.printStackTrace();
                    }
                }

            } catch (Exception e) {
                download.setVisibility(View.INVISIBLE);
                share.setVisibility(View.INVISIBLE);

                e.printStackTrace();
                Toast.makeText(Qrgenerate.this, "You can't leave it blank ", Toast.LENGTH_SHORT).show();
            }

        }else{
            Toast.makeText(this, " No internet, you can't use it without internet connection", Toast.LENGTH_LONG).show();
        }



    }

    public void share(View view) {
        try{
            Drawable mDrawable = ivOutput.getDrawable();
            Bitmap mbitmap=((BitmapDrawable) mDrawable).getBitmap();
            String path = MediaStore.Images.Media.insertImage(getContentResolver(),mbitmap,"Image Description",null);
            Uri uri =Uri.parse(path);
            Intent share = new Intent(Intent.ACTION_SEND);
            share.setType("image/jpeg");
            share.putExtra(Intent.EXTRA_STREAM,uri);
            startActivity(Intent.createChooser(share, "qr code share "));
        }catch (Exception e){
            e.printStackTrace();
            Toast.makeText(Qrgenerate.this,"you must download first",Toast.LENGTH_LONG).show();
        }


    }

    public void download(View view) {
        if (ContextCompat.checkSelfPermission(Qrgenerate.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(Qrgenerate.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 100);

        }else {
            try {
                if (mInterstitialAd != null) {
                    mInterstitialAd.show(Qrgenerate.this);
                }
                BitmapDrawable drawable = (BitmapDrawable) ivOutput.getDrawable();
                Bitmap bitmap = drawable.getBitmap();
                File filepath = Environment.getExternalStorageDirectory();
                File dir = new File(filepath.getAbsoluteFile() + "/Download/");
                dir.mkdir();
                File file = new File(dir, System.currentTimeMillis() + ".jpg");
                try {
                    outputStream = new FileOutputStream(file);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
                Toast.makeText(Qrgenerate.this, "Qr code saved in Download folder", Toast.LENGTH_LONG).show();
                try {
                    outputStream.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                try {
                    outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode==100 ){
            if(grantResults.length>0 && grantResults[0]==PackageManager.PERMISSION_GRANTED){
                try {
                    if (mInterstitialAd != null) {
                        mInterstitialAd.show(Qrgenerate.this);
                    }
                    BitmapDrawable drawable = (BitmapDrawable) ivOutput.getDrawable();
                    Bitmap bitmap = drawable.getBitmap();
                    File filepath = Environment.getExternalStorageDirectory();
                    File dir = new File(filepath.getAbsoluteFile() + "/Download/");
                    dir.mkdir();
                    File file = new File(dir, System.currentTimeMillis() + ".jpg");
                    try {
                        outputStream = new FileOutputStream(file);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
                    Toast.makeText(Qrgenerate.this,"Qr code saved in Download folder",Toast.LENGTH_LONG).show();
                    try {
                        outputStream.flush();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    try {
                        outputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}