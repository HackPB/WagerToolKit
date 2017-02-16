package com.codigree.wagertoolkit;

import android.Manifest;
import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.ContentObserver;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DecimalFormat;


public class MainActivity extends AppCompatActivity {

/**Ask storage permission for android 6.0 or sup */
    public boolean isStoragePermissionGranted() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                return true;
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                return false;
            }
        } else { //permission is automatically granted on sdk<23 upon installation
            return true;
        }
    }



/**Function for toast message */
    Toast toast = null;
    public void mToast(String message){

            if (toast != null) {
                toast.cancel();
                toast = null;
                //recreate();
            } else {
                toast = Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT);
                toast.show();
                //recreate();
            }

    }



/**Function to know if wagertool is installed */
    public boolean isAppInstalled(String packageName) {
        PackageManager pm = getPackageManager();
        boolean installed;
        try {
            pm.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES);
            installed = true;
        } catch (PackageManager.NameNotFoundException e) {
            installed = false;
        }
        return installed;
    }



/**Function to show buttons */
    public void showButtons(){
        //Button fisrtButton = (Button) findViewById(R.id.firstButton);
        Button secondButton = (Button) findViewById(R.id.secondButton);

        File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/Wagertool.apk");
            if (isAppInstalled("KeepItSoft.Wagertool.Phone")){
                //fourthButton.setVisibility(View.VISIBLE);
                secondButton.setEnabled(true);
                Drawable img = ActivityCompat.getDrawable(getApplicationContext(), R.mipmap.launch);
                secondButton.setCompoundDrawablesWithIntrinsicBounds(img,null,null,null);
                secondButton.setText(MainActivity.this.getString(R.string.launch));
                secondButton.setOnClickListener(new View.OnClickListener(){
                    @Override
                    public void onClick(View view) {
                        fourthButtonClick();
                    }
                });
                //leftButton.setImageResource(R.drawable.remove);

            }else if (file.exists() && !isAppInstalled("KeepItSoft.Wagertool.Phone") && !isNonPlayAppAllowed){
                Drawable img = ActivityCompat.getDrawable(getApplicationContext(), R.mipmap.install);
                secondButton.setCompoundDrawablesWithIntrinsicBounds(img,null,null,null);
                secondButton.setText(MainActivity.this.getString(R.string.install));
                secondButton.setOnClickListener(new View.OnClickListener(){
                    @Override
                    public void onClick(View view) {
                        thirdButtonClick();
                    }
                });


            }else if (file.exists()&& !isNonPlayAppAllowed){
                Drawable img = ActivityCompat.getDrawable(getApplicationContext(), R.mipmap.install);
                secondButton.setCompoundDrawablesWithIntrinsicBounds(img,null,null,null);
                secondButton.setText(MainActivity.this.getString(R.string.install));
                secondButton.setOnClickListener(new View.OnClickListener(){
                    @Override
                    public void onClick(View view) {
                        thirdButtonClick();
                    }
                });

            }else if (!isNonPlayAppAllowed) {
                Drawable img = ActivityCompat.getDrawable(getApplicationContext(), R.mipmap.download);
                secondButton.setCompoundDrawablesWithIntrinsicBounds(img,null,null,null);
                secondButton.setText(MainActivity.this.getString(R.string.get));
                secondButton.setOnClickListener(new View.OnClickListener(){
                    @Override
                    public void onClick(View view) {
                        secondButtonClick(view);
                    }
                });
            }else{
                secondButton.setCompoundDrawablesWithIntrinsicBounds(null,null,null,null);
            }


    }



/**to delete file in download folder onclick */
    public void delete (){
        File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/Wagertool.apk");
        file.delete();
        recreate();
    }



/**Function to install wager */
    public void launchWager(){
        Intent LaunchIntent = getPackageManager()
                .getLaunchIntentForPackage("KeepItSoft.Wagertool.Phone");
        startActivity(LaunchIntent);
        recreate();
    }



/**Function to verify unknownsources */
    boolean isNonPlayAppAllowed = false;
    public void sources (){
        final Button firstButton = (Button) findViewById(R.id.firstButton);
        final Button secondButton = (Button) findViewById(R.id.secondButton);
        try {
            isNonPlayAppAllowed = Settings.Secure.getInt(getContentResolver(), Settings.Secure.INSTALL_NON_MARKET_APPS) == 0;
        } catch (Settings.SettingNotFoundException e) {
            e.printStackTrace();
        }
        if (isNonPlayAppAllowed) {
            firstButton.setEnabled(true);
            firstButton.setVisibility(View.VISIBLE);
            Drawable img = ActivityCompat.getDrawable(getApplicationContext(), R.mipmap.arrow);
            firstButton.setCompoundDrawablesWithIntrinsicBounds(img,null,null,null);
            //img.setColorFilter(0xffff0000, PorterDuff.Mode.MULTIPLY); -- if we want to change the color of the image in left
            secondButton.setEnabled(false);
            secondButton.setCompoundDrawablesWithIntrinsicBounds(null,null,null,null);

        }else {
            firstButton.setEnabled(false);
            firstButton.setCompoundDrawablesWithIntrinsicBounds(null,null,null,null);
            firstButton.setVisibility(View.GONE);
            secondButton.setEnabled(true);
        }

    /**verification of changes in unknown sources */
        ContentObserver observer = new ContentObserver(null) {
            @Override
            public void onChange(boolean selfChange) {
                super.onChange(selfChange);

                int enabled = Settings.System.getInt(getContentResolver(), Settings.Secure.INSTALL_NON_MARKET_APPS, 0);

                if (enabled==1) {
                    recreate();
                    firstButton.setEnabled(false);
                    firstButton.setCompoundDrawablesWithIntrinsicBounds(null,null,null,null);
                    firstButton.setVisibility(View.GONE);
                    secondButton.setEnabled(true);

                }else {
                    recreate();
                    firstButton.setEnabled(true);
                    firstButton.setVisibility(View.VISIBLE);
                    Drawable img = ActivityCompat.getDrawable(getApplicationContext(), R.mipmap.arrow);
                    firstButton.setCompoundDrawablesWithIntrinsicBounds(img,null,null,null);
                    secondButton.setEnabled(false);
                    secondButton.setCompoundDrawablesWithIntrinsicBounds(null,null,null,null);
                    /**TEST */
                }
                showButtons();
            }

            /*Only if we need notifications about the state of sources
             *   @Override
             *   public boolean deliverSelfNotifications() {
             *   return true;
            }*/
        };

        Uri setting = Settings.System.getUriFor(Settings.Secure.INSTALL_NON_MARKET_APPS);
        getContentResolver().registerContentObserver(setting, false, observer);

    }



/**Function file size */
    public static String formatSize(long size) {
        String hrSize = null;

        double b = size;
        double k = size/1024.0;
        double m = ((size/1024.0)/1024.0);
        double g = (((size/1024.0)/1024.0)/1024.0);
        double t = ((((size/1024.0)/1024.0)/1024.0)/1024.0);

        DecimalFormat dec = new DecimalFormat("0.0");

        if ( t>1 ) {
            hrSize = dec.format(t).concat(" TB");
        } else if ( g>1 ) {
            hrSize = dec.format(g).concat(" GB");
        } else if ( m>1 ) {
            hrSize = dec.format(m).concat(" MB");
        } else if ( k>1 ) {
            hrSize = dec.format(k).concat(" KB");
        } else {
            hrSize = dec.format(b).concat(" Bytes");
        }

        return hrSize;
    }

    public String fsize = " aprox. 89 MB";
        public class DownloadTask extends AsyncTask<String, Void, String> {

            @Override
            protected String doInBackground(String... urls) {

                try {
                    URL url = new URL(urls[0]);
                    HttpURLConnection urlConnection = (HttpURLConnection)url.openConnection();
                    urlConnection.connect();

                    int file_size = urlConnection.getContentLength();
                    fsize = formatSize(file_size);
                }
                catch(Exception e) {
                    e.printStackTrace();
                    return "Failed";
                }

                return null;
            }

        }

    public String fileSize () {

        DownloadTask task = new DownloadTask();
        try {
            task.execute("https://www.wagertool.com/media/files/download/Wagertool.apk").get();
            Log.i("asaber", fsize);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }



/**Function to store and launch video */
    public void launchVideo (){
        //File sdCard = Environment.getExternalStorageDirectory(); -- To store in another location other than downloads
        File sdCard = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        //File dir = new File (sdCard.getAbsolutePath()+ "/raw");
        File video = new File (sdCard.getAbsolutePath()+ "/demo.mp4");
        //if(!dir.isDirectory()) dir.mkdirs();
        if(!video.isFile()){
            InputStream input = getResources().openRawResource(R.raw.demo);
            int size;
            try{
               size = input.available();
                byte[] buffer = new byte[size];
                input.read(buffer);
                input.close();
                FileOutputStream fileos = new FileOutputStream(new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),"demo.mp4"));
                fileos.write(buffer);
                fileos.close();

            }catch (IOException e){
                e.printStackTrace();
            }
        }
        File videofile = new File (sdCard,"demo.mp4");
        Intent intent = new Intent(Intent.ACTION_VIEW);
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        String ext = videofile.getName().substring(videofile.getName().lastIndexOf(".") + 1);
        String type = mime.getMimeTypeFromExtension(ext);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            Uri contentUri = FileProvider.getUriForFile(getApplicationContext(), "com.codigree.wagertoolkit.fileProvider", videofile);
            intent.setDataAndType(contentUri, type);
            startActivity(intent);
            recreate();
        } else {

        intent.setDataAndType(Uri.fromFile(videofile), "video/mp4");
        startActivity(intent);}


    }



/**Function to check connection and type of internet connection */
    public void checkConnection () {
        final ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        final ProgressDialog progress;


                if (activeNetworkInfo != null){
                    //internet
                    if (activeNetworkInfo.getType() == ConnectivityManager.TYPE_WIFI) {
                        //wifi
                        download();
                    }else if (activeNetworkInfo.getType() == ConnectivityManager.TYPE_MOBILE){
                        //mobile data
                        progress = ProgressDialog.show(this,
                                MainActivity.this.getString(R.string.ConnectingServer),
                                MainActivity.this.getString(R.string.wait), true); //message to display till return of file size

                        new  Thread(new Runnable() {
                            @Override
                            public void run() {

                                fileSize();

                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        progress.dismiss();
                                        new AlertDialog.Builder(MainActivity.this)
                                                .setIcon(android.R.drawable.ic_dialog_alert)
                                                .setTitle(MainActivity.this.getString(R.string.Warning))
                                                .setMessage(String.format(MainActivity.this.getString(R.string.NoWifi), fsize))
                                                .setPositiveButton(MainActivity.this.getString(R.string.Yes), new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public  void onClick (DialogInterface dialog, int which) {
                                                        download();
                                                    }
                                                })
                                                .setNegativeButton(MainActivity.this.getString(R.string.ActivateWifi), new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void  onClick (DialogInterface dialog, int which) {
                                                        startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
                                                    }
                                                })
                                                .show();
                                    }
                                });
                            }
                        }).start();

                    }

                } else {
                    //no internet
                    new AlertDialog.Builder(this)
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .setTitle(MainActivity.this.getString(R.string.Warning))
                            .setMessage(MainActivity.this.getString(R.string.NoInternet))
                            .setPositiveButton(MainActivity.this.getString(R.string.Yes), new DialogInterface.OnClickListener() {
                                @Override
                                public  void onClick (DialogInterface dialog, int which) {
                                    startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
                                }
                            })
                            .setNegativeButton(MainActivity.this.getString(R.string.No), null)
                            .show();
                }

    }


/**Function to download file and store it */
    DownloadManager manager;
    ProgressDialog mProgressDialog;

    public  void download(){


        manager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
        String url = MainActivity.this.getString(R.string.app_url);
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url))
                                                .setDescription(MainActivity.this.getString(R.string.notification_description))
                                                .setTitle(MainActivity.this.getString(R.string.app_name));
        request.allowScanningByMediaScanner();
        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI | DownloadManager.Request.NETWORK_MOBILE);
        request.setAllowedOverRoaming(false);
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "Wagertool.apk");
        final File file = new File (Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/Wagertool.apk");
        final Uri uri = Uri.parse("file://" + String.valueOf(file));
        //request.setDestinationUri(uri);

        final long downloadId = manager.enqueue(request);

        IntentFilter filter = new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE);


        mProgressDialog = new ProgressDialog(MainActivity.this);
        mProgressDialog.setMessage(MainActivity.this.getString(R.string.Downloading));
        mProgressDialog.setProgress(0);
        //mProgressDialog.setIndeterminate(true);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        mProgressDialog.setCancelable(false);
        mProgressDialog.setButton(DialogInterface.BUTTON_NEGATIVE, MainActivity.this.getString(R.string.Cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                manager.remove(downloadId);
            }});

        //code to view progress - with try and catch because of cancel button, or else it will throw an error because value of int will be 0
        new Thread(new Runnable() {

            @Override
            public void run() {

                boolean downloading = true;
                try {
                    while (downloading) {

                        DownloadManager.Query q = new DownloadManager.Query();
                        q.setFilterById(downloadId); //filter by id which you have received when requesting download from download manager
                        Cursor cursor = manager.query(q);
                        cursor.moveToFirst();

                        int bytes_downloaded = cursor.getInt(cursor
                                .getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR));
                        int bytes_total = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES));

                        if (cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)) == DownloadManager.STATUS_SUCCESSFUL) {
                            downloading = false;
                        }

                        final int dl_progress = (int) ((bytes_downloaded * 100l) / bytes_total);

                        runOnUiThread(new Runnable() {

                            @Override
                            public void run() {

                                mProgressDialog.setProgress(dl_progress);

                            }
                        });

                        cursor.close();
                    }
                }catch (Exception e){
                    Log.i("erro", "erro so that cancel button works");
                }
            }
        }).start();
            mProgressDialog.show();


       final BroadcastReceiver onComplete = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                mProgressDialog.dismiss();

                if (file.exists()) {
                    Intent install = new Intent(Intent.ACTION_VIEW);
                    MimeTypeMap mime = MimeTypeMap.getSingleton();
                    String ext = file.getName().substring(file.getName().lastIndexOf(".") + 1);
                    String type = mime.getMimeTypeFromExtension(ext);

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        install.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        Uri contentUri = FileProvider.getUriForFile(getApplicationContext(), "com.codigree.wagertoolkit.fileProvider", file);
                        install.setDataAndType(contentUri, type);
                        startActivity(install);
                        recreate();
                    } else {

                        install.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        install.setDataAndType(uri, type);
                        startActivity(install);
                        recreate();
                    }
                    unregisterReceiver(this);


                } else {
                    mToast(MainActivity.this.getString(R.string.downloaError));
                    unregisterReceiver(this);
                    mProgressDialog.dismiss();

                }
            }
        };
        registerReceiver(onComplete, filter);
    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        isStoragePermissionGranted();
        showButtons();
        sources();
        ImageView logo = (ImageView) findViewById(R.id.wagertoolkit);
        logo.requestFocus();

    }

    @Override
    protected void onResume (){
        super.onResume();
        sources();
        showButtons();
        //mToast("Resume");
    }

    @Override
    public void onPause () {
        super.onPause();
        if(toast != null)
            toast.cancel();
    }

    @Override
    public void onDestroy (){
        super.onDestroy();
        //Toast.makeText(getApplicationContext(), "Destroy" , Toast.LENGTH_SHORT).show();

    }



    public void firstButtonClick (View view) {
        if (isNonPlayAppAllowed) {
            mToast(MainActivity.this.getString(R.string.takingTo));
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    startActivity(new Intent(android.provider.Settings.ACTION_SECURITY_SETTINGS));
                }
            },2000);
        }

    }

    public void secondButtonClick (View view){
        checkConnection();

    }

    public void thirdButtonClick () {
        File file = new File (Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/Wagertool.apk");
        Uri uri = Uri.parse("file://" + String.valueOf(file)); //the string "file://" is added to work in samsung or others firmwares
        if (file.exists()) {
            Intent install = new Intent(Intent.ACTION_VIEW);
            MimeTypeMap mime = MimeTypeMap.getSingleton();
            String ext = file.getName().substring(file.getName().lastIndexOf(".") + 1);
            String type = mime.getMimeTypeFromExtension(ext);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                install.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                Uri contentUri = FileProvider.getUriForFile(getApplicationContext(), "com.codigree.wagertoolkit.fileProvider", file);
                install.setDataAndType(contentUri, type);
                startActivity(install);
                recreate();
            } else {
                install.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                install.setDataAndType(uri, type);
                startActivity(install);
                recreate();
            }
        }
    }


    public void fourthButtonClick (){
        launchWager();
        delete();
    }


    public void videoClick (View view) {
        launchVideo();

    }

    public void userGuideClick (View view) {
        Intent intent = new Intent(getApplicationContext(), UserGuideActivity.class);
        startActivity(intent);

    }

}


