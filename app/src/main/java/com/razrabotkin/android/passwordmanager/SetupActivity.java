package com.razrabotkin.android.passwordmanager;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

public class SetupActivity extends AppCompatActivity {

    private static final int REQUEST_CODE = 1;
    private PrefManager prefManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);

        setTitle(R.string.setup);
        prefManager = new PrefManager(this);
    }

    /**
     * Вызывается при нажатии на радиокнопку "Создать новую базу данных"
     * @param view Элемент View, на который нажали
     */
    public void createDatabase(View view) {
        Intent intent = new Intent(SetupActivity.this, CreateDatabaseActivity.class);
        startActivityForResult(intent, REQUEST_CODE);   // https://stackoverflow.com/questions/10407159/how-to-manage-startactivityforresult-on-android
    }

    /**
     * Вызывается при нажатии на радиокнопку "Восстановить базу данных из облака"
     * @param view Элемент View, на который нажали
     */
    public void restoreDatabase(View view) {
        Toast toast = Toast.makeText(this, "Данный функционал пока не реализован", Toast.LENGTH_LONG);
        toast.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE){
            if (resultCode == Activity.RESULT_OK){
                showCreateDatabaseAlertDialog();
            }
        }
    }

    /**
     * Выводит на экран диалоговое окно о том, что база данных создана
     */
    private void showCreateDatabaseAlertDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(SetupActivity.this);
        builder
                .setTitle(R.string.app_name)
                .setMessage("Новая база данных создана\n" +
                        "\n" +
                        "Помните свой пароль! Его нельзя сбросить или восстановить. И вы не сможете получить доступ к вашим данным без пароля.")
                .setPositiveButton(R.string.ok,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                showSyncAlertDialog();
                                dialogInterface.cancel();
                            }
                        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    /**
     * Выводит диалоговое окно с предложением синхронизировать с облаком
     */
    private void showSyncAlertDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(SetupActivity.this);
        builder
                .setTitle("Синхронизация")  //TODO: Локализовать
                .setMessage("Настроить синхронизацию с облаком?")
                .setPositiveButton(R.string.ok,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Toast toast = Toast.makeText(getApplicationContext(), "Данный функционал пока не реализован", Toast.LENGTH_LONG);
                        toast.show();
                    }
                })
                .setNegativeButton(R.string.cancel,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        launchHomeScreen();
                        dialogInterface.dismiss();
                    }
                });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void launchHomeScreen() {
        prefManager.setFirstTimeLaunch(false);
        startActivity(new Intent(SetupActivity.this, MainActivity.class));
        finish();
    }
}
