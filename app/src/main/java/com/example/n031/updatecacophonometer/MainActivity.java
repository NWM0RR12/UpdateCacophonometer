package com.example.n031.updatecacophonometer;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.webkit.URLUtil;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;


public class MainActivity extends AppCompatActivity {

    //url used to obtain the apk
    private String apk_url = "https://cacophony.org.nz/sites/default/files/Cacophonometer-0.949-release.apk";
    private CheckRootPrivileges crp;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        crp = new CheckRootPrivileges();
        crp.execute();
    }

    //return the version of the cacophonometer
    public String getVersion(){
        try {
            //Gets the package info
            PackageInfo pInfo = getPackageManager().getPackageInfo("nz.co.cacophonoy.cacophonometerlite", 0);
            //saves information of the  version name
            String version= pInfo.versionName;
            return version;

        }
        catch(PackageManager.NameNotFoundException e){
        }
        //handles return method
        return null;
    }



    public void updateOnClick(View v){
        TextView display = (TextView) findViewById(R.id.txtDisplay);

        //Gets the file name from the sitring variable called apk_url
        String fileName = URLUtil.guessFileName(apk_url,null,MimeTypeMap.getFileExtensionFromUrl(apk_url));
        //fileName = Cacophonometer-0.949-release.apk
        //splits the string by "-"
        String[] split = fileName.split("-");
        String urlVersion =split[1];
        //urlVersion = 0.949

       String deviceVersion = getVersion();





        double latestVersion=Double.parseDouble(urlVersion);
        double currentVersion=Double.parseDouble(deviceVersion);

        //double latestVersion =1;
        //double currentVersion=0;
        if(deviceVersion.equals(null)==false) {
            if (latestVersion > currentVersion) {
                Toast.makeText(this, "Newer version avaliable update started", Toast.LENGTH_LONG).show();
                DownloadTask downloadTask = new DownloadTask();
                downloadTask.execute(apk_url);
                Toast.makeText(this, "Apk installed", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, "Cacophonometer version is up to date", Toast.LENGTH_LONG).show();
            }

            installedApps();

            Intent launchApp = getPackageManager().getLaunchIntentForPackage("nz.co.cacophonoy.cacophonometerlite");
            startActivity(launchApp);
        }


    }

    //not needed for the project but prints to log all the package names
    public  void installedApps()
    {
        List<PackageInfo> packList = getPackageManager().getInstalledPackages(0);
        for (int i=0; i < packList.size(); i++)
        {
            PackageInfo packInfo = packList.get(i);
            if (  (packInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0)
            {
                String appName = packInfo.applicationInfo.loadLabel(getPackageManager()).toString();
                Log.e("App № " + Integer.toString(i), appName);
            }
        }
    }


    //https://stackoverflow.com/questions/29188557/download-file-with-asynctask
    public class DownloadTask extends AsyncTask<String, Integer, String> {

        //prints a message to the log
        @Override
        protected void onPostExecute(String s) {
            Log.e("Download Message",s);
        }

        //Downloading file in background thread
        @Override
        protected String doInBackground(String... params) {
            //first index of the a
            String path = params[0];
            int file_length = 0;

            try {

                //Opens the url which is passed in the params array
                URL url = new URL(path);
                URLConnection urlConnection = url.openConnection();
                urlConnection.connect();

                String fileName = URLUtil.guessFileName(apk_url,null,
                        MimeTypeMap.getFileExtensionFromUrl(apk_url));

                //gets the download directory of the android device
                File dir = Environment.getExternalStorageDirectory();
                File newFile = new File(dir,"download");
                newFile.mkdir();
                File apkfile = new File(newFile, fileName);

                if(apkfile.exists()){
                    apkfile.delete();
                }


                //required variables to write to the file
                InputStream inputStream = new BufferedInputStream(url.openStream());
                OutputStream outputStream = new FileOutputStream(apkfile);

                byte[] data = new byte[1024];


                int total=0;
                int count=0;

                //writes the apk to the phone
                while((count=inputStream.read(data))!=-1){
                    total+=count;
                    outputStream.write(data,0,count);
                }

                outputStream.flush();
                outputStream.close();
                inputStream.close();


            }
            catch(MalformedURLException e){
                e.printStackTrace();
            }
            catch(IOException e){
                e.printStackTrace();
            }

            InstallAPK(Environment.getExternalStorageDirectory() + "/download/" + "Cacophonometer-0.949-release.apk");



            return "Download Complete";


        }


    }

//https://stackoverflow.com/questions/26926274/install-android-apk-without-prompt
    //this method will be passed in the location of the apk
    //creates and calls a package command
    //to install the downloaded apk
    public static void InstallAPK(String filename){
        File file = new File(filename);
        if(file.exists()){
            try {
                String command;
                //adb does not work on gt galaxy ace trend 2 plus
                // = "adb install -r " + filename;
                //package manager command
                command = "pm install -r " + filename;
                Process proc = Runtime.getRuntime().exec(new String[] { "su", "-c", command });
                proc.waitFor();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}
