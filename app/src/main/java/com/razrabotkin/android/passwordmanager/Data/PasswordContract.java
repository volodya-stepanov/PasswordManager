package com.razrabotkin.android.passwordmanager.data;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by Володя on 25.06.2017.
 */

public final class PasswordContract {

    public static final String CONTENT_AUTHORITY = "com.razrabotkin.android.passwordmanager";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
    public static final String PATH_PASSWORDS = "passwords";

    private PasswordContract(){}

    public static final class PasswordEntry implements BaseColumns{

        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_PASSWORDS);

        public final static String TABLE_NAME = "passwords";

        public final static String _ID = BaseColumns._ID;
        public final static String COLUMN_NAME = "name";
        public final static String COLUMN_LOGIN = "login";
        public final static String COLUMN_PASSWORD = "password";
        public final static String COLUMN_WEBSITE = "website";
        public final static String COLUMN_NOTE = "note";
        public final static String COLUMN_CHANGED_AT = "changed_at";
        public final static String COLUMN_IS_FAVORITE = "is_favorite";

        /**
         * MIME-тип {@link #CONTENT_URI} для списка паролей.
         */
        public static final String CONTENT_LIST_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_PASSWORDS;

        /**
         * MIME-тип {@link #CONTENT_URI} для одного пароля.
         */
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_PASSWORDS;
    }
}
