package com.razrabotkin.android.passwordmanager;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by Володя on 04.06.2017.
 */

public class PrefManager {
    SharedPreferences pref;
    SharedPreferences.Editor editor;
    Context _context;

    // Режим Shared Preferences
    int PRIVATE_MODE = 0;

    // Имя файла Shared preferences
    private static final String PREF_NAME = "androidhive-welcome";

    private static final String IS_FIRST_TIME_LAUNCH = "IsFirstTimeLaunch";
    private static final String PASSWORD = "password";

    public PrefManager(Context context) {
        this._context = context;
        pref = _context.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
        editor = pref.edit();
    }

    /**
     * Устанавливает признак того, что приложение запущено первый раз
     * @param isFirstTime Новое значение признака
     */
    public void setFirstTimeLaunch(boolean isFirstTime) {
        editor.putBoolean(IS_FIRST_TIME_LAUNCH, isFirstTime);
        editor.commit();
    }

    /**
     * Получает признак того, что приложение запущено первый раз
     * @return Значение признака
     */
    public boolean isFirstTimeLaunch() {
        return pref.getBoolean(IS_FIRST_TIME_LAUNCH, true);
    }

    /**
     * Записывает в общие настройки главный пароль менеджера паролей
     * @param password Новый пароль
     */
    public void setPassword(String password) {
        editor.putString(PASSWORD, password);
        editor.commit();
    }

    /**
     * Возвращает из общих настроек главный пароль менеджера паролей
     * @return Пароль
     */
    public String getPassword() {
        return pref.getString(PASSWORD, "");
    }
}
