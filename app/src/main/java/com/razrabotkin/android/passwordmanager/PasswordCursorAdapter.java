package com.razrabotkin.android.passwordmanager;

import android.content.Context;
import android.database.Cursor;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

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
    public void bindView(View view, Context context, Cursor cursor) {
        // Находим в макете пункта списка конкретные представления, которые мы хотим изменить
        TextView nameTextView = (TextView) view.findViewById(R.id.name);
        TextView loginTextView = (TextView) view.findViewById(R.id.login);

        // Находим колонки нужных атрибутов записи
        int nameColumnIndex = cursor.getColumnIndex(PasswordContract.PasswordEntry.COLUMN_NAME);
        int loginColumnIndex = cursor.getColumnIndex(PasswordContract.PasswordEntry.COLUMN_LOGIN);

        // Считываем атрибуты из курсора для текущей записи
        String name = cursor.getString(nameColumnIndex);
        String login = cursor.getString(loginColumnIndex);

        // Если логин - пустая строка или null, используем текст по умолчанию,
        // который говорит "Логин не задан", чтобы поле TextView не было пустым.
        if (TextUtils.isEmpty(login)) {
            login = "Логин не задан";
        }

        // Обновляем элементы TextView атрибутами текущей записи
        nameTextView.setText(name);
        loginTextView.setText(login);
    }
}