package com.mehmetesen.qrcode;

import android.Manifest;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.ChecksumException;
import com.google.zxing.FormatException;
import com.google.zxing.LuminanceSource;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.NotFoundException;
import com.google.zxing.RGBLuminanceSource;
import com.google.zxing.Reader;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.io.FileNotFoundException;
import java.io.InputStream;

public class MainActivity extends AppCompatActivity {
    Button scanBtn;
    Button barcode;
    Button qrCode;
    AlertDialog choosePicture;
    private AdView mAdView;
    private InterstitialAd mInterstitialAd;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        scanBtn = findViewById(R.id.scanBtn);
        barcode = findViewById(R.id.barcode);
        qrCode = findViewById(R.id.generateBtn);
        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
            }
        });

        mAdView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);
        AdRequest adRequest3 = new AdRequest.Builder().build();

        InterstitialAd.load(this,"ca-app-pub-4026568549714819/1834040126", adRequest,
                new InterstitialAdLoadCallback() {
                    @Override
                    public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                        // The mInterstitialAd reference will be null until
                        // an ad is loaded.
                        mInterstitialAd = interstitialAd;
                    }

                });




    }
    public void scan(View view) {
        if(Network(this)){
            choosePicture();
        }else{
            Toast.makeText(this, " No internet, you can't use it without internet connection", Toast.LENGTH_LONG).show();

        }

    }








    public boolean Network(Context context){
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        return connectivityManager.getActiveNetworkInfo() != null && connectivityManager.getActiveNetworkInfo().isConnectedOrConnecting();
    }

    public void choosePicture(){
        AlertDialog.Builder choose = new AlertDialog.Builder(MainActivity.this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.alert_dialog_profile_picture, null);
        choose.setCancelable(false);
        choose.setView(dialogView);
        ImageView camera = dialogView.findViewById(R.id.camera);
        ImageView gallery = dialogView.findViewById(R.id.gallery);
        choosePicture = choose.create();
        choosePicture.show();

        gallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(ContextCompat.checkSelfPermission(MainActivity.this,Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
                    ActivityCompat.requestPermissions(MainActivity.this,new String[] {Manifest.permission.READ_EXTERNAL_STORAGE},400);


                }else{
                    Intent gallery = new Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(gallery,50);
                    choosePicture.dismiss();

                }



            }
        });

        camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                scanCode();
                choosePicture.dismiss();
                Toast.makeText(MainActivity.this, "You can turn on the flash by pressing the volume up button.", Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void scanCode() {

        IntentIntegrator integrator = new IntentIntegrator(this);
        integrator.setCaptureActivity(CaptureAct.class);
        integrator.setOrientationLocked(false);
        integrator.setDesiredBarcodeFormats(IntentIntegrator.ALL_CODE_TYPES);
        integrator.setPrompt("Scanning Code ");
        integrator.initiateScan();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        IntentResult result = IntentIntegrator.parseActivityResult(requestCode,resultCode,data);


        if(requestCode==50){
            if(resultCode == RESULT_OK){
                Uri image=data.getData();
                try {
                    InputStream imageStream = getContentResolver().openInputStream(image);
                    Bitmap selectedImage=BitmapFactory.decodeStream(imageStream);
                    Bitmap bMap=selectedImage;
                    String contents = null;
                    int[] intArray = new int[bMap.getWidth()*bMap.getHeight()];
                    bMap.getPixels(intArray, 0, bMap.getWidth(), 0, 0, bMap.getWidth(), bMap.getHeight());
                    LuminanceSource source = new RGBLuminanceSource(bMap.getWidth(), bMap.getHeight(), intArray);
                    BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
                    Reader reader = new MultiFormatReader();
                    Result result2=reader.decode(bitmap);
                    contents =result2.getText();
                    AlertDialog.Builder Content = new AlertDialog.Builder(MainActivity.this);
                    Content.setTitle(" Qr code Scan Result ");
                    Content.setIcon(R.drawable.ic_baseline_qr_code_scanner_24);
                    Content.setMessage(contents);
                    Content.setCancelable(false);
                    Content.setNeutralButton("Copy", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ClipboardManager manager1 = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                            ClipData data1 = ClipData.newPlainText("result",result2.getText());
                            manager1.setPrimaryClip(data1);
                            Toast.makeText(MainActivity.this, "Copied", Toast.LENGTH_LONG).show();

                        }
                    });
                    Content.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (mInterstitialAd != null) {
                                mInterstitialAd.show(MainActivity.this);
                            }


                        }
                    });
                   AlertDialog ContentRes = Content.create();
                   ContentRes.show();

                    //Toast.makeText(getApplicationContext(), contents, Toast.LENGTH_LONG).show();

                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                    Toast.makeText(MainActivity.this, "went wrong", Toast.LENGTH_LONG).show();
                } catch (FormatException e) {
                    Toast.makeText(MainActivity.this, "went wrong", Toast.LENGTH_LONG).show();
                    e.printStackTrace();
                } catch (ChecksumException e) {
                    Toast.makeText(MainActivity.this, "went wrong", Toast.LENGTH_LONG).show();
                    e.printStackTrace();
                } catch (NotFoundException e) {
                    Toast.makeText(MainActivity.this, "went wrong", Toast.LENGTH_LONG).show();
                    e.printStackTrace();
                }
            }
        }

        

        try{
            if(result != null){


                if(result.getContents() != null){
                    String connect = result.getContents();
                    if(connect.contains("http://")){
                        Uri link=Uri.parse(result.getContents());
                        Intent intent = new Intent(Intent.ACTION_VIEW,link);
                        startActivity(intent);
                    }else if(connect.contains("https://")){
                        Uri link=Uri.parse(result.getContents());
                        Intent intent = new Intent(Intent.ACTION_VIEW,link);
                        startActivity(intent);
                    }else{
                        AlertDialog.Builder builder = new AlertDialog.Builder(this);
                        builder.setCancelable(false);
                        builder.setMessage(result.getContents());
                        builder.setIcon(R.drawable.copy);
                        builder.setTitle("Scanning result");

                        builder.setNeutralButton("Copy", new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                ClipboardManager manager = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                                ClipData data = ClipData.newPlainText("result",result.getContents());
                                manager.setPrimaryClip(data);
                                Toast.makeText(MainActivity.this, "Copied", Toast.LENGTH_LONG).show();
                            }

                        }).setNegativeButton("Finish", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (mInterstitialAd != null) {
                                    mInterstitialAd.show(MainActivity.this);
                                }

                                 finish();
                            }
                        }).setPositiveButton("Scan Again", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (mInterstitialAd != null) {
                                    mInterstitialAd.show(MainActivity.this);

                                }
                                scanCode();


                            }
                        });

                        AlertDialog dialog = builder.create();
                        dialog.show();

                    }




                }else{
                    Toast.makeText(this,"No results",Toast.LENGTH_LONG).show();

                }

            }else{
               
                super.onActivityResult(requestCode,resultCode,data);

            }

        }catch (Exception e){
            e.printStackTrace();
            Toast.makeText(this,"No results",Toast.LENGTH_LONG).show();
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode==400){
            if(grantResults.length>0 && grantResults[0]==PackageManager.PERMISSION_GRANTED){
                Intent gallery = new Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(gallery,50);
                choosePicture.dismiss();


            }

        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    public void Generate(View view){
        Intent intent = new Intent(MainActivity.this,Qrgenerate.class);
        if(Network(this)){
            startActivity(intent);
        }else{
            Toast.makeText(this, " No internet, you can't use it without internet connection", Toast.LENGTH_LONG).show();


        }


    }

    public void Barcode(View view) {
        Intent intent = new Intent(MainActivity.this,Barcode.class);
        if(Network(this)){
            startActivity(intent);
        }else{
            Toast.makeText(this, " No internet, you can't use it without internet connection", Toast.LENGTH_LONG).show();

        }

    }


}