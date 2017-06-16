package com.razrabotkin.android.passwordmanager;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

public class SetupActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);

        setTitle(R.string.setup);
    }

    /**
     * Вызывается при нажатии на радиокнопку "Создать новую базу данных"
     * @param view Элемент View, на который нажали
     */
    public void createDatabase(View view) {
        Intent intent = new Intent(SetupActivity.this, CreateDatabaseActivity.class);
        startActivity(intent);
    }

    /**
     * Вызывается при нажатии на радиокнопку "Восстановить базу данных из облака"
     * @param view Элемент View, на который нажали
     */
    public void restoreDatabase(View view) {
        Toast toast = Toast.makeText(this, "Данный функционал пока не реализован", Toast.LENGTH_LONG);
        toast.show();
    }
}
