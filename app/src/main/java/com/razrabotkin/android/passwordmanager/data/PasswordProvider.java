package com.razrabotkin.android.passwordmanager.data;


import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

/**
 * Created by Володя on 18.07.2017.
 */

/**
 * {@link ContentProvider} для приложения "Менеджер паролей"
 */
public class PasswordProvider extends ContentProvider {

    /** Тэг для сообщений лога */
    public static final String LOG_TAG = PasswordProvider.class.getSimpleName();

    private PasswordDbHelper mDbHelper;

    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    private static final int PASSWORDS = 1;
    private static final int PASSWORD_ID = 2;

    static {
        sUriMatcher.addURI(PasswordContract.CONTENT_AUTHORITY, PasswordContract.PasswordEntry.TABLE_NAME, PASSWORDS);
        sUriMatcher.addURI(PasswordContract.CONTENT_AUTHORITY, PasswordContract.PasswordEntry.TABLE_NAME + "/#", PASSWORD_ID);
    }

    /**
     * Инициализирует провайдер и объект database.
     */
    @Override
    public boolean onCreate() {
        mDbHelper = new PasswordDbHelper(getContext());
        return true;
    }

    /**
     * Выполняет запрос для данного URI, используя данные аргументы projection, selection, selection and sort order.
     */
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {
        // Получаем читаемую базу данных
        SQLiteDatabase database = mDbHelper.getReadableDatabase();

        // В этом курсоре будет храниться результат запроса
        Cursor cursor;

        // Определяем, может ли URI matcher сопоставить URI с конкретным кодом
        int match = sUriMatcher.match(uri);
        switch (match) {
            case PASSWORDS:
                // Для кода PASSWORDS делаем заропс к таблице passwrods непосредственно, используя данные аргументы
                // projection, selection, selection и sort order. Курсор
                // будет содержать несколько строк таблицы паролей.
                cursor = database.query(PasswordContract.PasswordEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            case PASSWORD_ID:
                // Для кода PASSWORD_ID извлекаем ID из URI.
                // Например, для URI "content://com.example.android.pets/pets/3",
                // аргумент selection будет равен "_id=?", а аргумент selectionArgs будет
                // строковым массивом, содержащим ID, равный 3 в этом случае.
                //
                // Для каждого "?" в аргументе selection нужно иметь элемент в selection
                // arguments, которым будет заполнен "?". Так как мы имеем 1 вопросительный знак в аргументе
                // selection, мы имеем 1 строку в строковом массиве selection arguments.
                selection = PasswordContract.PasswordEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };

                // Это выражение выполинт запрос к таблице, где _id равен 3, чтобы вернуть
                // курсор, содержащий эту строку в таблице.
                cursor = database.query(PasswordContract.PasswordEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Cannot query unknown URI " + uri);
        }

        // Устанавливаем URI уведомления для курсора,
        // чтобы знать, для какого URI контента был создан курсор.
        // Если данные этого URI изменятся, мы знаем, что нам нужно обновить курсор.
        cursor.setNotificationUri(getContext().getContentResolver(), uri);

        return cursor;
    }

    /**
     * Вставляет новые данные в провайдер, используя данные ContentValues.
     */
    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case PASSWORDS:
                return insertPassword(uri, contentValues);
            default:
                throw new IllegalArgumentException("Insertion is not supported for " + uri);
        }
    }

    /**
     * Вставляем запись в базу данных с указанными content values. Возвращаем новый URI
     * контента для конкретной строки в базе данных.
     */
    private Uri insertPassword(Uri uri, ContentValues values) {
        // Проверяем данные, записанные в объекте ContentValues
        String name = values.getAsString(PasswordContract.PasswordEntry.COLUMN_NAME);
        if (name == null)
        {
            throw new IllegalArgumentException("Не указано название");
        }

        String login = values.getAsString(PasswordContract.PasswordEntry.COLUMN_LOGIN);
        if (login == null)
        {
            throw new IllegalArgumentException("Не указан логин");
        }

        String password = values.getAsString(PasswordContract.PasswordEntry.COLUMN_PASSWORD);
        if (password == null)
        {
            throw new IllegalArgumentException("Не указан пароль");
        }

        // Получаем записываемую базу данных
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        // Вставляем новую запись в соответствии с заданными значениями
        long id = database.insert(PasswordContract.PasswordEntry.TABLE_NAME, null, values);

        // Если ID равен -1, вставка не удалась. Выводим ошибку в лог и возвращаем null.
        if (id == -1) {
            Log.e(LOG_TAG, "Не удалось выполить вставку для " + uri);
            return null;
        }

        // Уведомляем всех слушателей, что данные этого URI изменились
        getContext().getContentResolver().notifyChange(uri, null);

        // Как только мы узнаем ID новой строки в таблице,
        // возвращаем новый URI с ID, прикрепленным к концу.
        return ContentUris.withAppendedId(uri, id);
    }

    /**
     * Обновляет данные по адресу, указанному с помощью selection and selection arguments, из ContentValues.
     */
    @Override
    public int update(Uri uri, ContentValues contentValues, String selection,
                      String[] selectionArgs) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case PASSWORDS:
                return updatePassword(uri, contentValues, selection, selectionArgs);
            case PASSWORD_ID:
                // Для с лучая с кодом PET_ID, извлекаем ID из URI,
                // чтобы знать, какую строку обновлять. Selection будет равно "_id=?", selection
                // arguments будут строковым массивом, содержащим конкретный ID.
                selection = PasswordContract.PasswordEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };
                return updatePassword(uri, contentValues, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Обновление не поддерживается для " + uri);
        }
    }

    /**
     * Обновляет записи в базе в соответствии с данным объектом content values. Применяет изменения к строками,
     * указанным в параметрах selection и selection arguments (которые могут содержать 0, 1 или более записей).
     * Возвращает количество обновленных строк
     */
    private int updatePassword(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        // Если ключ {@link PasswordEntry#COLUMN_NAME} присутствует,
        // проверяем значение на null.
        if (values.containsKey(PasswordContract.PasswordEntry.COLUMN_NAME)) {
            String name = values.getAsString(PasswordContract.PasswordEntry.COLUMN_NAME);
            if (name == null) {
                throw new IllegalArgumentException("Не указано название");
            }
        }

        // Если ключ {@link PasswordEntry#COLUMN_LOGIN} присутствует, //TODO: Для чего нужны эти ссылки?
        // проверяем значение на null.
        if (values.containsKey(PasswordContract.PasswordEntry.COLUMN_LOGIN)) {
            String login = values.getAsString(PasswordContract.PasswordEntry.COLUMN_LOGIN);
            if (login == null) {
                throw new IllegalArgumentException("Не указан логин");
            }
        }

        // Если ключ {@link PasswordEntry#COLUMN_PASSWORD} присутствует,
        // проверяем значение на null.
        if (values.containsKey(PasswordContract.PasswordEntry.COLUMN_PASSWORD)) {
            String password = values.getAsString(PasswordContract.PasswordEntry.COLUMN_PASSWORD);
            if (password == null) {
                throw new IllegalArgumentException("Не указан пароль");
            }
        }

        // Если нет значений для обновления, ничего не обновляем
        if (values.size() == 0) {
            return 0;
        }

        // Иначе, получаем записываемую базу данных для обновления
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        // Сохраняем количество строк в базе данных, затронутых обновлением, в переменную rowsUpdated
        int rowsUpdated = database.update(PasswordContract.PasswordEntry.TABLE_NAME, values, selection, selectionArgs);

        // Если одна или более строк были обновлены, уведомляем всех слушателей о том, что данные по данному
        // URI поменялись
        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        // Возвращаем количество строк в базе данных, затронутых обновлением
        return rowsUpdated;
    }

    /**
     * Удаляет данные по адресу, указанному с помощью selection and selection arguments.
     */
    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        // Получаем записываемую базу данных
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        // Отслеживаем количество удалённых строк
        int rowsDeleted;

        final int match = sUriMatcher.match(uri);
        switch (match) {
            case PASSWORDS:
                // Уведомляем всех слушателей, что данные в URI контента изменены.
                getContext().getContentResolver().notifyChange(uri, null);

                // Удаляем все строки, которые соответствуют selection и selection args
                rowsDeleted = database.delete(PasswordContract.PasswordEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case PASSWORD_ID:
                // Уведомляем всех слушателей, что данные в URI контента изменены.
                getContext().getContentResolver().notifyChange(uri, null);

                // Удаляем одну строку, определяемую по ID в URI
                selection = PasswordContract.PasswordEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                rowsDeleted = database.delete(PasswordContract.PasswordEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Deletion is not supported for " + uri);
        }

        // Если одна или более строк были удалены, уведомляем всех слушателей о том, что данные по данному
        // URI поменялись
        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        // Возвращаем количество удалённых строк
        return rowsDeleted;
    }

    /**
     * Возвращает MIME-тип данных для URI контента.
     */
    @Override
    public String getType(Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case PASSWORDS:
                return PasswordContract.PasswordEntry.CONTENT_LIST_TYPE;
            case PASSWORD_ID:
                return PasswordContract.PasswordEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalStateException("Unknown URI " + uri + " with match " + match);
        }
    }
}
