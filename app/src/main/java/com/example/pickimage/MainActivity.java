package com.example.pickimage;

import static com.example.pickimage.R.id.textViewResult;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;

import java.io.IOException;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    CardView cameraCard, photoCard;
    ImageView imageView;
    private TextView textViewResult;
    private final int CAM_REQ=1000;
    private final int IMG_REQ=2000;
    Uri imageUrl;
    private TextRecognizer textRecognizer;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
        cameraCard.setOnClickListener(this);

        photoCard.setOnClickListener(this);


    }

    public void init(){
        textViewResult = findViewById(R.id.textViewResult);
        imageView=findViewById(R.id.imageView);
        cameraCard=findViewById(R.id.cameraCard);
        photoCard=findViewById(R.id.photosCard);
        textRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode==RESULT_OK){
            if(requestCode==CAM_REQ){
                Bitmap camBitmap=(Bitmap) data.getExtras().get("data");
                imageView.setImageBitmap(camBitmap);
                // Виконання розпізнавання тексту
                recognizeTextFromImage(camBitmap);
            }
            else if(requestCode==IMG_REQ){
                imageUrl=data.getData();
                imageView.setImageURI(imageUrl);
                try {
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUrl);
                    // Виконання розпізнавання тексту
                    recognizeTextFromImage(bitmap);
                } catch (IOException e) {
                    Toast.makeText(this, "Помилка завантаження зображення", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private void recognizeTextFromImage(Bitmap bitmap) {
        // Перетворення Bitmap в InputImage
        InputImage image = InputImage.fromBitmap(bitmap, 0);

        // Обробка зображення та отримання результату
        textRecognizer.process(image)
                .addOnSuccessListener(visionText -> {
                    StringBuilder resultText = new StringBuilder();
                    for (Text.TextBlock block : visionText.getTextBlocks()) {
                        resultText.append(block.getText()).append("\n");
                    }
                    textViewResult.setText(resultText.toString());
                    Log.d("MLKit", "Розпізнаний текст: " + resultText);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(MainActivity.this, "Помилка розпізнавання тексту", Toast.LENGTH_SHORT).show();
                    Log.e("MLKit", "Помилка розпізнавання тексту: ", e);
                });
    }

    @Override
    public void onClick(View view) {
        if(view.getId()==R.id.photosCard){
            getPhoto();
        }else if(view.getId()==R.id.cameraCard){
            startCamera();
        }
    }

    public void startCamera(){
        Intent cameraIntent=new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(cameraIntent, CAM_REQ);
    }

    public void getPhoto(){
        Intent photoIntent=new Intent(Intent.ACTION_PICK);
        photoIntent.setData(MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(photoIntent,IMG_REQ);
    }
}