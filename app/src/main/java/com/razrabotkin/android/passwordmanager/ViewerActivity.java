package com.razrabotkin.android.passwordmanager;

import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.razrabotkin.android.passwordmanager.data.PasswordContract;

public class ViewerActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>{

    private static final int EXISTING_PASSWORD_LOADER = 0;
    /**
     * URI контента для существующего пароля (null если это новый пароль)
     */
    private Uri mCurrentPasswordUri;
    private TextView mNameTextView;
    private TextView mLoginTextView;
    private TextView mPasswordTextView;
    private TextView mWebsiteTextView;
    private TextView mNoteTextView;

    private boolean mCardHasChanged = false;

    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mCardHasChanged = true;
            return false;
        }
    };

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_viewer);

        // Проверяем интент, запустивший эту операцию,
        // чтобы выяснить, создаём мы новую запись или редактируем существующую.
        Intent intent = getIntent();
        mCurrentPasswordUri = intent.getData();

        // Если интент НЕ содержит URI контента, мы знаем, что создаём новую карту.
        if (mCurrentPasswordUri == null) {
            // Это новая карта, поэтому меняем App Bar на "Создание карты"
            setTitle("Создание карты");

            // Лишаем законной силы меню опций чтобы пункт "Удалить" можно было скрыть.
            // (Не имеет смысла удалять запись, которая ещё не создана)
            invalidateOptionsMenu();
        } else {
            // Иначе это существующая карта, поэтому меняем App Bar на "Редактирование карты"-
            setTitle("Редактирование карты");
        }

        mNameTextView = (TextView) findViewById(R.id.edit_name);
        mLoginTextView = (TextView) findViewById(R.id.edit_login);
        mPasswordTextView = (TextView) findViewById(R.id.edit_password);
        mWebsiteTextView = (TextView) findViewById(R.id.edit_website);
        mNoteTextView = (TextView) findViewById(R.id.edit_note);

//        mNameTextView.setOnTouchListener(mTouchListener);
//        mLoginEditText.setOnTouchListener(mTouchListener);
//        mPasswordEditText.setOnTouchListener(mTouchListener);
//        mWebsiteEditText.setOnTouchListener(mTouchListener);
//        mNoteEditText.setOnTouchListener(mTouchListener);

        if (mCurrentPasswordUri != null) {
            // Инициализируем загрузчик
            getLoaderManager().initLoader(EXISTING_PASSWORD_LOADER, null, this);
        }
    }

    /**
     * Получает данные, введённые пользователем, и сохраняет их в базу данных
     */
    private void savePassword() {
        // Считываем данные из полей ввода
        // Используем метод trim(), чтобы удалить лишние пробелы
        String nameString = mNameTextView.getText().toString().trim();
        String loginString = mLoginTextView.getText().toString().trim();
        String passwordString = mPasswordTextView.getText().toString().trim();
        String websiteString = mWebsiteTextView.getText().toString().trim();
        String noteString = mNoteTextView.getText().toString().trim();

        if (mCurrentPasswordUri == null &&
                TextUtils.isEmpty(nameString) && TextUtils.isEmpty(loginString) &&
                TextUtils.isEmpty(passwordString) && TextUtils.isEmpty(websiteString) && TextUtils.isEmpty(noteString)) {
            return;
        }

        // Создаем объект ContentValues, в котором имена колонок - ключи,
        // а атрибуты записи - значения из редактора.
        ContentValues values = new ContentValues();
        values.put(PasswordContract.PasswordEntry.COLUMN_NAME, nameString);
        values.put(PasswordContract.PasswordEntry.COLUMN_LOGIN, loginString);
        values.put(PasswordContract.PasswordEntry.COLUMN_PASSWORD, passwordString);
        values.put(PasswordContract.PasswordEntry.COLUMN_WEBSITE, websiteString);
        values.put(PasswordContract.PasswordEntry.COLUMN_NOTE, noteString);

        // Если URI контента текущей записи равно null, добавляем новую запись в базу
        if (mCurrentPasswordUri == null) {
            // Вставляем новую строку в провайдер, возвращаем URI контента.
            Uri newUri = getContentResolver().insert(PasswordContract.PasswordEntry.CONTENT_URI, values);

            // Отображаем сообщение-тост в зависимости от успешности вставки
            if (newUri == null) {
                // Если URI контента равно null, значит, произошла ошибка при вставке.
                Toast.makeText(this, "Произошла ошибка при сохранении", Toast.LENGTH_SHORT).show();
            } else {
                // Иначе вставка прошла успешно
                Toast.makeText(this, "Запись сохранена", Toast.LENGTH_SHORT).show();
            }
        } else {
            // Иначе это существующая запись, поэтому обновляем запись с URI контента равным mCurrentPasswordUri
            // и передаем в Content Values. Передаем null для аргументов selection и selection args,
            // потому что mCurrentPasswordUri уже идентифицирует нужную строку в базе данных,
            // которую мы хотим изменить
            int rowsAffected = getContentResolver().update(mCurrentPasswordUri, values, null, null);

            // Отображаем сообщение-тост в зависимости от успешности вставки
            if (rowsAffected == 0) {
                // Если ни одна строка не была обновлена, значит, возникла ошибка при обновлении.
                Toast.makeText(this, "Произошла ошибка при сохранении", Toast.LENGTH_SHORT).show();
            } else {
                // Иначе обновление прошло успешно
                Toast.makeText(this, "Запись сохранена", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        // Если это новая запись, скрываем пункт меню "Удалить"
        if (mCurrentPasswordUri == null) {
            MenuItem menuItem = menu.findItem(R.id.action_delete);
            menuItem.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_save:
                savePassword();
                finish();
                return true;
            case android.R.id.home:
                // Если запись не изменена, продолжаем навигацию вверх к родительской операции
                // {@link CatalogActivity}.
                if (!mCardHasChanged) {
                    NavUtils.navigateUpFromSameTask(ViewerActivity.this);
                    return true;
                }

                // В противном случае, если есть несохранённые изменения, настраиваем диалог для предупреждения пользователя.
                // Создаём обработчик нажатия, чтобы обработать подтверждение пользователем отмены изменений.
                DialogInterface.OnClickListener discardButtonClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // User clicked "Discard" button, navigate to parent activity.
                                NavUtils.navigateUpFromSameTask(ViewerActivity.this);
                            }
                        };

                // Показываем диалог о том, что есть несохраненные изменения
                showUnsavedChangesDialog(discardButtonClickListener);
                return true;
            case R.id.action_delete:
                // Отображаем диалог подтверждения удаления
                showDeleteConfirmationDialog();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader onCreateLoader(int i, Bundle bundle) {
        // Так как в редакторе отображаются все атрибуты карты, определяем массив projection, который
        // содержит все колонки таблицы паролей.
        String[] projection = {
                PasswordContract.PasswordEntry._ID,
                PasswordContract.PasswordEntry.COLUMN_NAME,
                PasswordContract.PasswordEntry.COLUMN_LOGIN,
                PasswordContract.PasswordEntry.COLUMN_PASSWORD,
                PasswordContract.PasswordEntry.COLUMN_WEBSITE,
                PasswordContract.PasswordEntry.COLUMN_NOTE
        };

        // Этот загрузчик выполнит метод query контент-провайдера в фоновом потоке
        return new CursorLoader(this,   // Контекст родительской операции
                mCurrentPasswordUri,    // URI контента запроса для текущей карты
                projection,             // Колонки, включаемые в курсор-результат
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        // Начинаем с перемещения на первую строку курсора и чтоения его данных
        // (в курсоре должна быть одна строка)
        if (cursor.moveToFirst()) {
            // Находим колонки атрибутов карты, которые нас интересуют
            int nameColumnIndex = cursor.getColumnIndex(PasswordContract.PasswordEntry.COLUMN_NAME);
            int loginColumnIndex = cursor.getColumnIndex(PasswordContract.PasswordEntry.COLUMN_LOGIN);
            int passwordColumnIndex = cursor.getColumnIndex(PasswordContract.PasswordEntry.COLUMN_PASSWORD);
            int websiteColumnIndex = cursor.getColumnIndex(PasswordContract.PasswordEntry.COLUMN_WEBSITE);
            int noteColumnIndex = cursor.getColumnIndex(PasswordContract.PasswordEntry.COLUMN_NOTE);

            // Извлекаем из курсора значение по данному индексу колонки
            String name = cursor.getString(nameColumnIndex);
            String login = cursor.getString(loginColumnIndex);
            String password = cursor.getString(passwordColumnIndex);
            String website = cursor.getString(websiteColumnIndex);
            String note = cursor.getString(noteColumnIndex);

            // Обновляем представление на экране значениями из базы данных
            mNameTextView.setText(name);
            mLoginTextView.setText(login);
            mPasswordTextView.setText(password);
            mWebsiteTextView.setText(website);
            mNoteTextView.setText(note);
        }
    }

    @Override
    public void onLoaderReset(Loader loader) {
        // Очищаем поля
        mNameTextView.setText("");
        mLoginTextView.setText("");
        mPasswordTextView.setText("");
        mWebsiteTextView.setText("");
        mNoteTextView.setText("");
    }

    private void showUnsavedChangesDialog(
            DialogInterface.OnClickListener discardButtonClickListener) {
        // Создаём AlertDialog.Builder и устанавливаем сообщение и обработчики нажатий
        // для положительной и отрицательной кнопок диалога
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Отменить изменения и выйти?");
        builder.setPositiveButton("Отменить", discardButtonClickListener);
        builder.setNegativeButton("Продолжить редактирование", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // Пользователь нажал кнопку "Продолжить редактирование", поэтому закрываем диалог
                // и продолжаем редактирование.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Создаём и отображаем диалог
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    @Override
    public void onBackPressed() {
        // Если запись не изменна, продолжаем обработку нажатия кнопки Назад
        if (!mCardHasChanged) {
            super.onBackPressed();
            return;
        }

        // В противном случае, если есть несохранённые изменения, настраиваем диалог для предупреждения пользователя.
        // Создаём обработчик нажатия, чтобы обработать подтверждение пользователем отмены изменений.
        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // Пользователь нажал кнопку "Отменить", закрываем текущую операцию.
                        finish();
                    }
                };

        // Показываем диалог о том, что есть несохраненные изменения
        showUnsavedChangesDialog(discardButtonClickListener);
    }

    private void showDeleteConfirmationDialog() {
        // Создаём AlertDialog.Builder и устанавливаем сообщение и обработчики нажатий
        // для положительной и отрицательной кнопок
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button, so delete the pet.
                deletePassword();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Cancel" button, so dismiss the dialog
                // and continue editing the pet.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    /**
     * Осуществляет удаление записи из базы данных
     */
    private void deletePassword() {
        // Осуществляем удаление, только если это существующая запись
        if (mCurrentPasswordUri != null) {
            // Вызываем ContentResolver чтобы удалить запись по данному URI контента.
            // Передаем null в аргументы selection and selection args потому что mCurrentPasswordUri
            // уже идентифицирует новую запись.
            int rowsDeleted = getContentResolver().delete(mCurrentPasswordUri, null, null);

            // Отображаем сообщение-тост в зависимости от того, успешно ли удаление
            if (rowsDeleted == 0) {
                // Если ни одна строка не удалена, значит, призошла ошибка при удалении
                Toast.makeText(this, getString(R.string.editor_delete_pet_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Иначе удаление прошло успешно.
                Toast.makeText(this, getString(R.string.editor_delete_pet_successful),
                        Toast.LENGTH_SHORT).show();
            }

            // Завершаем операцию
            finish();
        }
    }
}
