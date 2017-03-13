package com.rowsun.lestourydriver;

import android.content.Intent;
import android.net.Uri;
import android.provider.Settings;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;



/**
 * Created by hereshem on 8/13/15.
 */
public class BaseActivity extends AppCompatActivity {

    public View.OnClickListener locationOpenerListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            startActivityForResult(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK), 100);
        }
    };

    public void snackBar(View v, String mesg) {
        Snackbar.make(v, mesg, Snackbar.LENGTH_SHORT).show();
    }

    public void toast(String string) {
        Toast.makeText(getApplicationContext(), string, Toast.LENGTH_SHORT).show();
    }

    public void showSnackBar(String message, String action, View.OnClickListener listener) {
        Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_LONG);
        if (listener != null) {
            snackbar.setAction(TextUtils.isEmpty(action) ? "Ok" : action, listener);
        }
        snackbar.show();
    }

    public void showSnackBar(String message) {
        showSnackBar(message, null, null);
    }

    public boolean isDeepLink(Uri uri) {
        if (uri != null) {
            return ("lestoury").equals(uri.getScheme()) || uri.getHost().contains("lestoury.com");
        }
        return false;
    }

    public boolean isBlank(String string) {
        return TextUtils.isEmpty(string);
    }

    public void startMyActivity(Class<?> myClass) {
        startActivity(new Intent(this, myClass));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void loadToolBar() {
        try {
            Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
            if(toolbar != null) {
                setSupportActionBar(toolbar);
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                getSupportActionBar().setDefaultDisplayHomeAsUpEnabled(true);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}

