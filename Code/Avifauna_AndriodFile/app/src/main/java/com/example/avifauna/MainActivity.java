package com.example.avifauna;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.renderscript.Element;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;


import com.example.avifauna.ml.Effecientnetb3BirdsSpecies9840;
import com.example.avifauna.ml.MobilenetV110224Quant;

import org.tensorflow.lite.DataType;
import org.tensorflow.lite.support.image.TensorImage;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.nio.ByteBuffer;

public class MainActivity extends AppCompatActivity {

    Button selectBtn,predictBtn,captureBtn;
    TextView result;
    Bitmap bitmap;
    ImageView imageView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //permissions

        getPermission();
        String[] labels = new String[450];
        int cnt = 0;
        try {
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(getAssets().open("labels.txt")));
            String line = bufferedReader.readLine();
            while(line!=null){
                labels[cnt] = line;
                cnt++;
                line = bufferedReader.readLine();
            }
        }catch(IOException e){
            e.printStackTrace();
        }


        selectBtn = findViewById(R.id.selectBtn);
        predictBtn = findViewById(R.id.predictBtn);
        captureBtn = findViewById(R.id.captureBtn);
        result = findViewById(R.id.result);
        imageView = findViewById(R.id.imageView);

        selectBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(intent,10);
            }
        });

        captureBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(intent,12);
            }
        });

        predictBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {

                    Effecientnetb3BirdsSpecies9840 model = Effecientnetb3BirdsSpecies9840.newInstance(getApplicationContext());

                    // Creates inputs for reference.
                    TensorBuffer inputFeature0 = TensorBuffer.createFixedSize(new int[]{1, 224, 224, 3}, DataType.FLOAT32);
                    bitmap = Bitmap.createScaledBitmap(bitmap,224,224, true);
                    TensorImage ti = new TensorImage(DataType.FLOAT32);
                    ti.load(bitmap);
                    ByteBuffer byteBuffer = ti.getBuffer();

                    inputFeature0.loadBuffer(byteBuffer);


                    // Runs model inference and gets result.
                    Effecientnetb3BirdsSpecies9840.Outputs outputs = model.process(inputFeature0);

                    TensorBuffer outputFeature0 = outputs.getOutputFeature0AsTensorBuffer();

                    result.setText(labels[getMax(outputFeature0.getFloatArray())]);

                    // Releases model resources if no longer used.
                    model.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        });

        if(result.getText().toString()!=""){
            result.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(Intent.ACTION_VIEW,Uri.parse("https://www.google.com/search?q=" + result.getText().toString()));
                    startActivity(intent);
                }
            });
        }

        result.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_VIEW,Uri.parse("https://www.google.com/search?q=" + result.getText().toString()));
                startActivity(intent);
            }
        });
    }

    int getMax(float arr[]){
        int max = 0;
        for(int i=0;i<arr.length;i++){
            if(arr[i]>arr[max]) max = i;
        }
        return max;
    }
    void getPermission(){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            if(checkSelfPermission(Manifest.permission.CAMERA)!= PackageManager.PERMISSION_GRANTED){
                ActivityCompat.requestPermissions(MainActivity.this,new String[]{Manifest.permission.CAMERA},11);
            }
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode == 11){
            if(grantResults.length>0){
                if(grantResults[0]!=PackageManager.PERMISSION_GRANTED){
                    this.getPermission();
                }
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if(requestCode == 10){
            if(data!=null){
                Uri uri = data.getData();
                try {
                    bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(),uri);
                    imageView.setImageBitmap(bitmap);
                }catch (IOException e){
                    e.printStackTrace();
                }

            }
        } else if (requestCode == 12) {
            bitmap = (Bitmap)data.getExtras().get("data");
            imageView.setImageBitmap(bitmap);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}