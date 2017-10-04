package com.razrabotkin.android.passwordmanager;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.BackgroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.razrabotkin.android.passwordmanager.data.PasswordContract;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * {@link PasswordCursorAdapter} - это адаптер для listView или gridView,
 * который использует {@link Cursor} данных о паролях и их источник. Этот адаптер знает,
 * как создать пункты списка для каждой строки данных в {@link Cursor}.
 */
public class PasswordCursorAdapter extends CursorAdapter {

    // Строка, которую нужно искать и подсвечивать
    String textToSearch = null;

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

        // Поиск и подсветка по искомому слову
        // Перекрываемая (spannable) строка для подсветки искомых слов
        SpannableString spannableStringSearch = null;

        if ((textToSearch != null) && (!textToSearch.isEmpty())) {
            // Сюда в качестве параметра передаётся строка, по которой будет выполняться поиск
            spannableStringSearch = new SpannableString(name);

            // Собираем шаблон входного текста // TODO: Ознакомиться с этим классом подробнее
            Pattern pattern = Pattern.compile(textToSearch, Pattern.CASE_INSENSITIVE);

            // Передаём этот шаблон сопоставителю, чтобы найти соответствующий текст в имени
            Matcher matcher = pattern.matcher(name);

            // TODO: Зачем это сдесь? Чтобы обнулить?
            spannableStringSearch.setSpan(new BackgroundColorSpan(Color.TRANSPARENT), 0, spannableStringSearch.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

            while (matcher.find()) {

                //highlight all matching words in cursor with white background(since i have a colorfull background image)
                spannableStringSearch.setSpan(new BackgroundColorSpan(
                                Color.YELLOW), matcher.start(), matcher.end(),
                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
        }

        if(spannableStringSearch != null) {

            // Если поиск выполнен, устанавливаем spannable строку в textView
            nameTextView.setText(spannableStringSearch);
        } else {

            // иначе устанавливаем обычный текст из курсора
            nameTextView.setText(name);
        }
    }

    // Передаем текст из MainActivity
    public void searchText(String text) {
        this.textToSearch = text;
    }
}