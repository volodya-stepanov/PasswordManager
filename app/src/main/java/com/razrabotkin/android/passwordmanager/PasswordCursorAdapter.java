package com.razrabotkin.android.passwordmanager;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.BackgroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
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
String mTextToSearch = null;

    private final static String TAG= PasswordCursorAdapter.class.getName().toString();

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
        ImageView iconImageView = (ImageView) view.findViewById(R.id.image_view_icon);

        // Находим колонки нужных атрибутов записи
        int idColumnIndex = cursor.getColumnIndex(PasswordContract.PasswordEntry._ID);  // ID нужен для определения URI строки для записи в базу признака избранности
        int nameColumnIndex = cursor.getColumnIndex(PasswordContract.PasswordEntry.COLUMN_NAME);
        int loginColumnIndex = cursor.getColumnIndex(PasswordContract.PasswordEntry.COLUMN_LOGIN);
        int isFavoriteColumnIndex = cursor.getColumnIndex(PasswordContract.PasswordEntry.COLUMN_IS_FAVORITE);
        int noteColumnIndex = cursor.getColumnIndex(PasswordContract.PasswordEntry.COLUMN_NOTE);
        int colorIndexColumnIndex = cursor.getColumnIndex(PasswordContract.PasswordEntry.COLUMN_COLOR_INDEX);

        // Считываем атрибуты из курсора для текущей записи
        final int id = cursor.getInt(idColumnIndex);
        String name = cursor.getString(nameColumnIndex);
        String login = cursor.getString(loginColumnIndex);
        String note = cursor.getString(noteColumnIndex);

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

        int colorIndex = cursor.getInt(colorIndexColumnIndex);
        Drawable background = getDrawableByIndex(context, colorIndex);
        iconImageView.setBackground(background);

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

        SpannableString nameSpannableString = findTextInTextView(context, name);

        if (nameSpannableString != null){
            nameTextView.setText(nameSpannableString);
        }

        SpannableString loginSpannableString = findTextInTextView(context, login);

        if (loginSpannableString != null){
            loginTextView.setText(loginSpannableString);
        }

        SpannableString noteSpannableString = findTextInTextView(context, note);

        if (noteSpannableString != null){
            loginTextView.setText(noteSpannableString);
        }

    }

    private Drawable getDrawableByIndex(Context context, int index) {
        //TODO: Костыль! Избавиться от повторения этого метода
        switch (index){
            case 0:
                return context.getDrawable(R.drawable.color_circle_1);
            case 1:
                return context.getDrawable(R.drawable.color_circle_2);
            case 2:
                return context.getDrawable(R.drawable.color_circle_3);
            case 3:
                return context.getDrawable(R.drawable.color_circle_4);
            case 4:
                return context.getDrawable(R.drawable.color_circle_5);
            case 5:
                return context.getDrawable(R.drawable.color_circle_6);
            case 6:
                return context.getDrawable(R.drawable.color_circle_7);
            case 7:
                return context.getDrawable(R.drawable.color_circle_8);
            case 8:
                return context.getDrawable(R.drawable.color_circle_9);
            case 9:
                return context.getDrawable(R.drawable.color_circle_10);
            case 10:
                return context.getDrawable(R.drawable.color_circle_11);
            case 11:
                return context.getDrawable(R.drawable.color_circle_12);
            case 12:
                return context.getDrawable(R.drawable.color_circle_13);
            case 13:
                return context.getDrawable(R.drawable.color_circle_14);
            case 14:
                return context.getDrawable(R.drawable.color_circle_15);
            case 15:
                return context.getDrawable(R.drawable.color_circle_16);
        }
        return null;
    }

// Передаем текст из MainActivity
public void searchText(String text) {
    this.mTextToSearch = text;
}

private SpannableString findTextInTextView(Context context, String string){
    // Поиск и подсветка по искомому слову
    // Перекрываемая (spannable) строка для подсветки искомых слов
    SpannableString spannableStringSearch = null;

    // Эта переменная введена для того, чтобы зафиксировать, нашёлся ли результат в данной строке.
    // Если не нашёлся, spannableStringSearch присваиваем значение null.
    // Иначе в любом случае возвращается эта строка (даже если в ней ничего не выделено),
    // и, например, если результат найден в логине, но не найден в заметках, то всё равно
    // в подзаголовке отобразятся заметки
    boolean matcherFind = false;

    if ((mTextToSearch != null) && (!mTextToSearch.isEmpty())) {
        // Сюда в качестве параметра передаётся строка, по которой будет выполняться поиск
        spannableStringSearch = new SpannableString(string);

        // Собираем шаблон входного текста // TODO: Ознакомиться с этим классом подробнее
        Pattern pattern = Pattern.compile(mTextToSearch, Pattern.CASE_INSENSITIVE);

        // Передаём этот шаблон сопоставителю, чтобы найти соответствующий текст в имени
        Matcher matcher = pattern.matcher(string);

        // TODO: Зачем это сдесь? Чтобы обнулить?
        spannableStringSearch.setSpan(
                new BackgroundColorSpan(Color.TRANSPARENT),
                0,
                spannableStringSearch.length(),
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        while (matcher.find()) {

            matcherFind = true;

            // Подсвечиваем все соответствующие слова в курсоре
            spannableStringSearch.setSpan(
                    new BackgroundColorSpan(
                            context.getResources().getColor(R.color.selection_yellow)),
                    matcher.start(),
                    matcher.end(),
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
    }

    if (!matcherFind){
        spannableStringSearch = null;
    }

    return spannableStringSearch;
}
}