package com.dev.anime;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class VersionCheck extends AsyncTask<Void, Void, String[]> {

    private Context context;
    private String versionUrl;
    private String linkUrl;

    public VersionCheck(Context context, String versionUrl, String linkUrl) {
        this.context = context;
        this.versionUrl = versionUrl;
        this.linkUrl = linkUrl;
    }

    @Override
    protected String[] doInBackground(Void... voids) {
        String latestVersion = fetchContent(versionUrl);
        String downloadLink = fetchContent(linkUrl);
        return new String[] {latestVersion, downloadLink};
    }

    private String fetchContent(String urlString) {
        try {
            URL url = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.connect();

            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder builder = new StringBuilder();
            String line;

            while ((line = reader.readLine()) != null) {
                builder.append(line);
            }

            reader.close();
            return builder.toString().trim();

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    protected void onPostExecute(String[] result) {
        String latestVersion = result[0];
        final String downloadLink = result[1];

        if (latestVersion != null && downloadLink != null) {
            try {
                PackageInfo pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
                String currentVersion = pInfo.versionName;

                if (Float.parseFloat(latestVersion) > Float.parseFloat(currentVersion)) {
                    new AlertDialog.Builder(context)
                            .setTitle("Update Available")
                            .setMessage("A new version of the app is available. Please update to the latest version.")
                            .setPositiveButton("Update", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Intent intent = new Intent(Intent.ACTION_VIEW);
                                    intent.setData(Uri.parse(downloadLink));
                                    context.startActivity(intent);
                                }
                            })
                            .setNegativeButton("Cancel", null)
                            .show();
                }
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
        }
    }
}
