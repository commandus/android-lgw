package com.commandus.lgw;

import androidx.appcompat.app.AppCompatActivity;

import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.widget.TextView;

import com.commandus.lgw.databinding.ActivityHelpBinding;

import java.io.InputStream;

public class HelpActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        com.commandus.lgw.databinding.ActivityHelpBinding binding = ActivityHelpBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        TextView textViewHelp = binding.textViewHelp;

        String content = "";
        try {
            InputStream strmIn = getResources().openRawResource(R.raw.help);
            byte[] b = new byte[strmIn.available()];
            strmIn.read(b);
            content = new String(b);
        } catch (Exception e) {
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            textViewHelp.setText(Html.fromHtml(content, Html.FROM_HTML_MODE_LEGACY));
        } else
            textViewHelp.setText(Html.fromHtml(content));
    }
}