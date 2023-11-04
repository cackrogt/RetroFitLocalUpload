package com.example.retrofitupdatelocal;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.webkit.URLUtil;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    Button btnUpload, btnMulUpload, btnPickImage, btnPickVideo, btnSaveUrl;
    String mediaPath, mediaPath1;
    ImageView imgView;
    String[] mediaColumns = {MediaStore.Video.Media._ID};
    ProgressDialog progressDialog;
    TextView str1, str2;
    int image, video;
    EditText serverUrlInput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Uploading...");

        btnUpload = (Button) findViewById(R.id.upload);
        btnMulUpload = (Button) findViewById(R.id.uploadMultiple);
        btnPickImage = (Button) findViewById(R.id.pick_img);
        btnPickVideo = (Button) findViewById(R.id.pick_vdo);
        btnSaveUrl = (Button) findViewById(R.id.save_server_url_btn);
        imgView = (ImageView) findViewById(R.id.preview);
        str1 = (TextView) findViewById(R.id.filename1);
        str2 = (TextView) findViewById(R.id.filename2);
        serverUrlInput = (EditText) findViewById(R.id.server_url_edittext);

        String saved_url_key = getString(R.string.saved_server_url_key);
        SharedPreferences sharedPref = getSharedPreferences(saved_url_key,MODE_PRIVATE);
        boolean hasSavedUrlKey = sharedPref.contains(saved_url_key);
        if(hasSavedUrlKey) {
            serverUrlInput.setText(getURLFromSharedPreferences(saved_url_key,sharedPref));
        }

        btnSaveUrl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String inputUrl = String.valueOf(serverUrlInput.getText());
                String savedUrl = getURLFromSharedPreferences(saved_url_key,sharedPref);
                if(savedUrl!=null && savedUrl.equalsIgnoreCase(inputUrl.toLowerCase())) {
                    showToast("Same Url entered",Toast.LENGTH_LONG);
                    return;
                }
                if(URLUtil.isValidUrl(inputUrl) && (URLUtil.isHttpsUrl(inputUrl) ||
                        URLUtil.isHttpsUrl(inputUrl))) {
                    SharedPreferences.Editor editor =  sharedPref.edit();
                    editor.putString(saved_url_key,inputUrl);
                    editor.apply();
                    showToast("Server URL saved",Toast.LENGTH_SHORT);
                }
                else {
                    showToast("Invalid URL",Toast.LENGTH_SHORT);
                }
            }
        });

        btnUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadFile(getURLFromSharedPreferences(saved_url_key,sharedPref));
            }
        });

        btnMulUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadMultipleFiles(getURLFromSharedPreferences(saved_url_key,sharedPref));
            }
        });

        btnPickImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent galleryIntent = new Intent(Intent.ACTION_PICK,
                        android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(galleryIntent, 0);
            }
        });

        // Video must be low in Memory or need to be compressed before uploading...
        btnPickVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent galleryIntent = new Intent(Intent.ACTION_PICK,
                        android.provider.MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(galleryIntent, 1);
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        try {
            // When an Image is picked
            if (requestCode == 0 && resultCode == RESULT_OK && null != data) {

                // Get the Image from data
                Uri selectedImage = data.getData();
                String[] filePathColumn = {MediaStore.Images.Media.DATA};

                Cursor cursor = getContentResolver().query(selectedImage, filePathColumn, null, null, null);
                assert cursor != null;
                cursor.moveToFirst();

                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                mediaPath = cursor.getString(columnIndex);
                str1.setText(mediaPath);
                // Set the Image in ImageView for Previewing the Media
                imgView.setImageBitmap(BitmapFactory.decodeFile(mediaPath));
                image = 1; video = 0;
                cursor.close();

            } // When an Video is picked
            else if (requestCode == 1 && resultCode == RESULT_OK && null != data) {

                // Get the Video from data
                Uri selectedVideo = data.getData();
                String[] filePathColumn = {MediaStore.Video.Media.DATA};

                Cursor cursor = getContentResolver().query(selectedVideo, filePathColumn, null, null, null);
                assert cursor != null;
                cursor.moveToFirst();

                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);

                mediaPath1 = cursor.getString(columnIndex);
                str2.setText(mediaPath1);
                // Set the Video Thumb in ImageView Previewing the Media
                imgView.setImageBitmap(getThumbnailPathForLocalFile(MainActivity.this, selectedVideo));
                image = 0; video = 1;
                cursor.close();

            } else {
                Toast.makeText(this, "You haven't picked Image/Video", Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            Toast.makeText(this, "Something went wrong", Toast.LENGTH_LONG).show();
        }

    }

    // Providing Thumbnail For Selected Image
    public Bitmap getThumbnailPathForLocalFile(Activity context, Uri fileUri) {
        long fileId = getFileId(context, fileUri);
        return MediaStore.Video.Thumbnails.getThumbnail(context.getContentResolver(),
                fileId, MediaStore.Video.Thumbnails.MICRO_KIND, null);
    }

    // Getting Selected File ID
    public long getFileId(Activity context, Uri fileUri) {
        Cursor cursor = context.managedQuery(fileUri, mediaColumns, null, null, null);
        if (cursor.moveToFirst()) {
            int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID);
            return cursor.getInt(columnIndex);
        }
        return 0;
    }

    private String getMimeType(String path) {
        String extension = path.substring(path.lastIndexOf(".") + 1);
        if(extension == null || extension == ""){
            return MimeTypeMap.getSingleton().getMimeTypeFromExtension("mp4");
        }
        return MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
    }

    ProgressDialog progress;

    // Uploading Image/Video
    private void uploadFile(String serverUrl) {
        if(serverUrl == null || serverUrl.isEmpty()) {
            showToast("Server Url is not valid.",Toast.LENGTH_SHORT);
            return;
        }
        progressDialog.show();

        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {

                // Map is used to multipart the file using okhttp3.RequestBody
                File file;
                //we should be able to remove this, i am just sleep deprived at this point.
                if(video == 1 && image == 0) {
                    file = new File(mediaPath1);
                }
                else{
                    file = new File(mediaPath);
                }

                String content_type  = getMimeType(file.getPath());
                String file_path = file.getAbsolutePath();
                // Parsing any Media type file and if we dont find it we return mp4
                RequestBody file_body = RequestBody.create(file, MediaType.parse(content_type));


                RequestBody requestBody = RequestBody.create(MediaType.parse("*/*"), file);
                MultipartBody.Part fileToUpload = MultipartBody.Part.createFormData("file", file.getName(), requestBody);

                ApiConfig getResponse = AppConfig.getRetrofit(serverUrl).create(ApiConfig.class);
                Call<ServerResponse> call = getResponse.uploadFile(fileToUpload, file_body);

                call.enqueue(new Callback<ServerResponse>() {
                    @Override
                    public void onResponse(Call<ServerResponse> call, Response<ServerResponse> response) {
                        ServerResponse serverResponse = response.body();
                        if (serverResponse != null) {
                            if (serverResponse.getSuccess()) {
                                Toast.makeText(getApplicationContext(), serverResponse.getMessage(), Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(getApplicationContext(), serverResponse.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            assert serverResponse != null;
                            Log.v("Response", serverResponse.toString());
                        }
                        progressDialog.dismiss();
                    }

                    @Override
                    public void onFailure(Call<ServerResponse> call, Throwable t) {

                    }
                });
            }
        });
        t.start();
    }

    // Uploading Image/Video
    private void uploadMultipleFiles(String serverUrl) {
        if(serverUrl == null || serverUrl.isEmpty()) {
            showToast("Server Url is not valid.",Toast.LENGTH_SHORT);
            return;
        }
        progressDialog.show();

        // Map is used to multipart the file using okhttp3.RequestBody
        File file = new File(mediaPath);
        File file1 = new File(mediaPath1);

        // Parsing any Media type file
        RequestBody requestBody1 = RequestBody.create(MediaType.parse("*/*"), file);
        RequestBody requestBody2 = RequestBody.create(MediaType.parse("*/*"), file1);

        MultipartBody.Part fileToUpload1 = MultipartBody.Part.createFormData("file1", file.getName(), requestBody1);
        MultipartBody.Part fileToUpload2 = MultipartBody.Part.createFormData("file2", file1.getName(), requestBody2);

        ApiConfig getResponse = AppConfig.getRetrofit(serverUrl).create(ApiConfig.class);
        Call<ServerResponse> call = getResponse.uploadMulFile(fileToUpload1, fileToUpload2);
        call.enqueue(new Callback<ServerResponse>() {
            @Override
            public void onResponse(Call<ServerResponse> call, Response<ServerResponse> response) {
                ServerResponse serverResponse = response.body();
                if (serverResponse != null) {
                    if (serverResponse.getSuccess()) {
                        showToast(serverResponse.getMessage(),Toast.LENGTH_SHORT);
                    } else {
                        showToast(serverResponse.getMessage(),Toast.LENGTH_SHORT);
                    }
                } else {
                    assert serverResponse != null;
                    Log.v("Response", serverResponse.toString());
                }
                progressDialog.dismiss();
            }

            @Override
            public void onFailure(Call<ServerResponse> call, Throwable t) {

            }
        });
    }

    private void showToast(String text, int duration) {
        Toast.makeText(getApplicationContext(), text, duration).show();
    }

    private String getURLFromSharedPreferences(String key, SharedPreferences prefs) {
        if(key == null || key.isEmpty()) {
            return "";
        }
        return prefs.getString(key,null);
    }
}