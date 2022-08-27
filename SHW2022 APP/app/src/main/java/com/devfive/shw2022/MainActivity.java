package com.devfive.shw2022;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.speech.tts.TextToSpeech;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {
    CustomAdapter adapter;
    ArrayList<DataModel> list;
    Button reset;
    Button purchase;
    EditText input;
    TextView total;
    TextToSpeech tts;
    Button capture;
    File file;
    Uri uri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /* tts */
        tts = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                System.out.println("wtf" + status);
                if (status != android.speech.tts.TextToSpeech.ERROR) {
                    tts.setLanguage(Locale.KOREAN);
                }
            }
        });

        /* 리스트 추가 및 어댑터 연결 */
        list = new ArrayList<>();
        RecyclerView recyclerView = findViewById(R.id.recycleView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        total = findViewById(R.id.total);
        reset = findViewById(R.id.reset);
        purchase = findViewById(R.id.purchase);
        adapter = new CustomAdapter(list, total, purchase, reset);
        recyclerView.setAdapter(adapter);

        /* 리스트 초기화 */
        reset.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(this)
                    .setMessage("초기화 하시겠습니까?")
                    .setPositiveButton("초기화", (dialog, which) -> {
                        adapter.clearData();
                    })
                    .setNegativeButton("취소", (dialog, which) -> {
                        dialog.dismiss();
                    });
            AlertDialog msgDlg = builder.create();
            msgDlg.show();
        });

        /* 결제 버튼 */
        purchase.setOnClickListener(v -> {
            tts(adapter.sum + "원을 결제하시겠습니까?");
            AlertDialog.Builder builder = new AlertDialog.Builder(this)
                    .setTitle("결제 확인")
                    .setMessage(adapter.sum + "원을 결제하시겠습니까?")
                    .setPositiveButton("결제", (dialog, which) -> {
                        Intent intent = new Intent(getApplicationContext(), ResultActivity.class);
                        startActivity(intent);
                        tts("결제가 완료되었습니다.");
                        adapter.clearData();
                    })
                    .setNegativeButton("취소", (dialog, which) -> {
                        dialog.dismiss();
                    });
            AlertDialog msgDlg = builder.create();
            msgDlg.show();
        });

        /* 바코드 읽어들이기 */
        input = findViewById(R.id.input);
        input.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.toString().contains("\n")) {
                    //바코드를 처리해줍니다.
                    String barcode = s.toString();
                    switch (barcode.replace("\n", "")) {
                        case "8801056028855":
                            adapter.addData("콜라", 2000);
                            tts("콜라를 추가했습니다");
                            break;
                        case "8801121764510":
                            adapter.addData("썬업", 1500);
                            tts("썬업를 추가했습니다");
                            break;
                        case "8886467105333":
                            adapter.addData("프링글스", 2500);
                            tts("프링글스를 추가했습니다");
                            break;
                        case "8801223007478":
                            adapter.addData("사이다", 1500);
                            tts("천연사이다를 추가했습니다");
                            break;
                        case "4897036693728":
                            adapter.addData("에너지 드링크", 2000);
                            tts("에너지 드링크를 추가했습니다");
                            break;
                        default:
                            Toast.makeText(MainActivity.this, "인식할 수 없는 바코드입니다", Toast.LENGTH_SHORT).show();
                            System.out.println(barcode);
                    }
                    input.setText(null);
                }
            }
        });
        /* 카메라 촬영 */
        capture = findViewById(R.id.capture);
        capture.setOnClickListener(v -> {
            takePicture();
        });
    }

    ActivityResultLauncher<Intent> mGetContent = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
            (ActivityResultCallback<ActivityResult>) res -> {
                if (res.getResultCode() != RESULT_OK)
                    return;
                Bundle extras = res.getData().getExtras();
                Bitmap imageBitmap = (Bitmap) extras.get("data");
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
                OkHttpClient client = new OkHttpClient();
                String URLString = "http://172.16.30.31:5000/image";
                Request request = new Request.Builder()
                        .addHeader("content-type", "multipart/form-data")
                        .url(URLString)
                        .post(new MultipartBody.Builder().setType(MultipartBody.FORM)
                                .addFormDataPart("file", "img.jpg", RequestBody.create(stream.toByteArray(), MediaType.parse("image/*jpg")))
                                .build())
                        .build();
                System.out.println("POST: calling: " + URLString);
                client.newCall(request).enqueue(new Callback() {
                    @Override
                    public void onFailure(@NonNull Call call, @NonNull IOException e) {
                        e.printStackTrace();
                    }

                    @Override
                    public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                        String d = response.body().string();
                        runOnUiThread(() -> {
                            if (d.contains("can"))
                                adapter.addData("콜라", 2000);
                            else if (d.contains("cell"))
                                adapter.addData("건전지", 1000);
                            else
                                Toast.makeText(MainActivity.this, "제품을 인식하지 못했습니다", Toast.LENGTH_SHORT).show();
                        });

                    }
                });
            });

    public void takePicture() {
        mGetContent.launch(new Intent(MediaStore.ACTION_IMAGE_CAPTURE));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    private File createFile() {
        String filename = "capture.jpg";
        File outFile = new File(getFilesDir(), filename);
        Log.d("Main", "File path : " + outFile.getAbsolutePath());

        return outFile;
    }

    /* tts */
    public void TTSActivity(String str, int flag) {
        String text = null;
        if (flag == 0)
            text = str + "가 추가되었습니다";
        else if (flag == 1)
            text = str + "원을 결제하시겠습니까?";
        else if (flag == 2)
            text = str;
        tts.setPitch(1.0f);
        tts.setSpeechRate(1.0f);
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null);
    }
    public void tts(String str) {
        OkHttpClient client = new OkHttpClient();
        String URLString = "https://translate.google.com/translate_tts?ie=UTF-8&tl=ko-KR&client=tw-ob&q="+str;
        Request request = new Request.Builder()
                .url(URLString)
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                playMp3(response.body().bytes());
            }
        });
    }

    private MediaPlayer mediaPlayer = new MediaPlayer();
    private void playMp3(byte[] mp3SoundByteArray) {
        try {
            File tempMp3 = File.createTempFile("kurchina", "mp3", getCacheDir());
            tempMp3.deleteOnExit();
            FileOutputStream fos = new FileOutputStream(tempMp3);
            fos.write(mp3SoundByteArray);
            fos.close();

            mediaPlayer.reset();
            FileInputStream fis = new FileInputStream(tempMp3);
            mediaPlayer.setDataSource(fis.getFD());

            mediaPlayer.prepare();
            mediaPlayer.start();
        } catch (IOException ex) {
            String s = ex.toString();
            ex.printStackTrace();
        }
    }
}

