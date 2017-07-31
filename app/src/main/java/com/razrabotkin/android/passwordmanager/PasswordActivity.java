package com.razrabotkin.android.passwordmanager;

import android.annotation.TargetApi;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;

public class PasswordActivity extends AppCompatActivity {
    private static final int PASSWORD_TYPE_TEXT = 0;
    private static final int PASSWORD_TYPE_NUMERIC = 1;
    private int mPasswordType = PASSWORD_TYPE_TEXT;

    private boolean mShowPassword = false;

    private EditText mPasswordView;
    private PrefManager prefManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_password);
        setupActionBar();

        mPasswordView = (EditText) findViewById(R.id.password);

        prefManager = new PrefManager(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.password, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.action_keyboard:
                if (mPasswordType == PASSWORD_TYPE_TEXT) {
                    item.setIcon(getResources().getDrawable(R.drawable.ic_keyboard_white_24dp));
                    mPasswordType = PASSWORD_TYPE_NUMERIC;
                } else {
                    item.setIcon(getResources().getDrawable(R.drawable.ic_dialpad_white_24dp));
                    mPasswordType = PASSWORD_TYPE_TEXT;
                }
                updateTextTypeAndVisibility();
                return true;
            case R.id.action_gopro:
                Intent intent = new Intent(PasswordActivity.this, GoProActivity.class);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Set up the {@link android.app.ActionBar}, if the API is available.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void setupActionBar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            // Show the Up button in the action bar.
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    private boolean isPasswordValid(String password) {
        //TODO: Replace this with your own logic
        return password.length() > 4;
    }

    /**
     * Вызывается при нажатии на кнопку "ОК"
     * @param view Кнопка, на которую нажали
     */
    public void onOk(View view) {
        if (mPasswordView.getText().toString().length() > 0) {
            String inputPassword = mPasswordView.getText().toString();
            String savedPassword = prefManager.getPassword();
            if (inputPassword.equals(savedPassword)) {
                launchHomeScreen();
            } else {
                clearInputLayout();
                showErrorAlertDialog(getString(R.string.password_incorrect));
            }
        }
    }

    /**
     * Выводит на экран диалоговое окно с иконкой предупреждения и указанным текстом
     * @param message Текст сообщения
     */
    private void showErrorAlertDialog(String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(PasswordActivity.this);
        builder
                .setMessage(message)
                .setTitle(R.string.error)
                .setNegativeButton(R.string.ok,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.cancel();
                            }
                        })
                .setIcon(R.drawable.ic_warning);
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    /**
     * Выполняет настройку макета для первого ввода пароля
     */
    private void clearInputLayout() {
        mPasswordView.setText("");
    }

        /**
     * Вызывается при установке/снятии флажка "Показывать пароль"
     * @param view Флажок, на который нажали
     */
    public void onShowPasswordCheckBoxClick(View view) {
        CheckBox checkBox = (CheckBox) view;
        mShowPassword = checkBox.isChecked();
        updateTextTypeAndVisibility();
    }

    public void updateTextTypeAndVisibility() {
        //TODO: При отображении/скрытии текстового пароля клавиатура не меняется
        if (mShowPassword){
            if (mPasswordType == PASSWORD_TYPE_TEXT) {
                mPasswordView.setInputType(InputType.TYPE_CLASS_TEXT);
            } else {
                mPasswordView.setInputType(InputType.TYPE_CLASS_NUMBER);
            }
        } else {
            if (mPasswordType == PASSWORD_TYPE_TEXT) {
                mPasswordView.setInputType(InputType.TYPE_CLASS_TEXT |
                        InputType.TYPE_TEXT_VARIATION_PASSWORD);
            } else {
                mPasswordView.setInputType(InputType.TYPE_CLASS_NUMBER |
                        InputType.TYPE_NUMBER_VARIATION_PASSWORD);
            }
        }
        mPasswordView.setSelection(mPasswordView.getText().length());
    }

    private void launchHomeScreen() {
        prefManager.setFirstTimeLaunch(false);
        startActivity(new Intent(PasswordActivity.this, MainActivity.class));
        finish();
    }
}
