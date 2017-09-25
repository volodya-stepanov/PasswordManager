package com.razrabotkin.android.passwordmanager;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.razrabotkin.android.passwordmanager.data.PasswordContract;

/**
 * {@link PasswordCursorAdapter} - это адаптер для listView или gridView,
 * который использует {@link Cursor} данных о паролях и их источник. Этот адаптер знает,
 * как создать пункты списка для каждой строки данных в {@link Cursor}.
 */
public class PasswordCursorAdapter extends CursorAdapter {

    /**
     * Создаёт новый {@link PasswordCursorAdapter}.
     *
     * @param context Контекст
     * @param c       Курсор, из которого нужно получать данные
     */
    public PasswordCursorAdapter(Context context, Cursor c) {
        super(context, c, 0 /* flags */);
    }

    /**
     * Создаёт новый пустой пункт списка. Никакие данные ещё не заданы и не привязаны к представлениям.
     *
     * @param context Контекст приложения
     * @param cursor  Курсор, из которого нужно получать данные. Курсор уже
     *                перемещён на нужную позицию.
     * @param parent  Родитель, к которому прикреплен этот пункт списка.
     * @return вновь созданное представление пункта списка.
     */
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
    }

    /**
     * Этот метод привязывает данные о пароле (на конкретной строке, указанной курсором) к данному
     * макету пункта списка.
     *
     * @param view    Существующее представление, возвращённое ранее методом newView()
     * @param context Контекст приложения
     * @param cursor  Курсор, из которого нужно получать данные. Курсор уже
     *                перемещён на нужную позицию.
     */
    @Override
    public void bindView(View view, final Context context, Cursor cursor) {
        // Находим в макете пункта списка конкретные представления, которые мы хотим изменить
        TextView nameTextView = (TextView) view.findViewById(R.id.name);
        TextView loginTextView = (TextView) view.findViewById(R.id.login);
        ToggleButton favoriteToggleButton = (ToggleButton) view.findViewById(R.id.toggle_button_favorite);

        // Находим колонки нужных атрибутов записи
        int idColumnIndex = cursor.getColumnIndex(PasswordContract.PasswordEntry._ID);  // ID нужен для определения URI строки для записи в базу признака избранности
        int nameColumnIndex = cursor.getColumnIndex(PasswordContract.PasswordEntry.COLUMN_NAME);
        int loginColumnIndex = cursor.getColumnIndex(PasswordContract.PasswordEntry.COLUMN_LOGIN);
        int isFavoriteColumnIndex = cursor.getColumnIndex(PasswordContract.PasswordEntry.COLUMN_IS_FAVORITE);

        // Считываем атрибуты из курсора для текущей записи
        final int id = cursor.getInt(idColumnIndex);
        String name = cursor.getString(nameColumnIndex);
        String login = cursor.getString(loginColumnIndex);

        boolean isFavorite;

        if(cursor.getInt(isFavoriteColumnIndex) == 0) {
            isFavorite = false;
        } else {
            isFavorite = true;
        }

        // Если логин - пустая строка или null, используем текст по умолчанию,
        // который говорит "Логин не задан", чтобы поле TextView не было пустым.
        if (TextUtils.isEmpty(login)) {
            login = "Логин не задан";
        }

        // Обновляем элементы TextView атрибутами текущей записи
        nameTextView.setText(name);
        loginTextView.setText(login);
        favoriteToggleButton.setChecked(isFavorite);

        // При нажатии на кнопку со звездой в списке информация должна обновиться в базе
        favoriteToggleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Преобразуем элемент view в ToggleButton и получаем значение isChecked.
                ToggleButton toggleButton = (ToggleButton) view;
                boolean value = toggleButton.isChecked();

                // Формируем URI данной записи
                Uri uri = ContentUris.withAppendedId(PasswordContract.PasswordEntry.CONTENT_URI, id);

                // Создаем объект ContentValues
                ContentValues values = new ContentValues();
                values.put(PasswordContract.PasswordEntry.COLUMN_IS_FAVORITE, value);

                // Обновляем запись в базе
                context.getContentResolver().update(uri, values, null, null);
            }
        });
    }
}