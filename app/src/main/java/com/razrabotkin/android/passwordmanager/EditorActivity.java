package com.razrabotkin.android.passwordmanager;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.PopupMenu;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.razrabotkin.android.passwordmanager.data.PasswordContract;

import java.text.SimpleDateFormat;
import java.util.Date;

public class EditorActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    //TODO: Исправить AppBar
    private static final int EXISTING_PASSWORD_LOADER = 0;
    /**
     * URI контента для существующего пароля (null если это новый пароль)
     */
    private Uri mCurrentPasswordUri;
    private EditText mNameEditText;
    private EditText mLoginEditText;
    private EditText mPasswordEditText;
    private EditText mWebsiteEditText;
    private EditText mNoteEditText;
    private ImageView mIconImageView;
    private int mColorIndex = 14;

    private boolean mCardHasChanged = false;

    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mCardHasChanged = true;
            return false;
        }
    };

    private final int CATEGORY_ID=0;
    Dialog mDialog;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        // Проверяем интент, запустивший эту операцию,
        // чтобы выяснить, создаём мы новую запись или редактируем существующую.
        Intent intent = getIntent();
        mCurrentPasswordUri = intent.getData();

        // Если интент НЕ содержит URI контента, мы знаем, что создаём новую карту.
        if (mCurrentPasswordUri == null) {
            // Это новая карта, поэтому меняем App Bar на "Создание карты"
            //TODO: Ещё раз разобраться с правильным назначением заголовка
            //setTitle("Создание карты");
            setTitle("");

            // Лишаем законной силы меню опций чтобы пункт "Удалить" можно было скрыть.
            // (Не имеет смысла удалять запись, которая ещё не создана)
            invalidateOptionsMenu();
        } else {
            // Иначе это существующая карта, поэтому меняем App Bar на "Редактирование карты"-
            //setTitle("Редактирование карты");
            setTitle("");
        }

        mNameEditText = (EditText) findViewById(R.id.edit_name);
        mLoginEditText = (EditText) findViewById(R.id.edit_login);
        mPasswordEditText = (EditText) findViewById(R.id.edit_password);
        mWebsiteEditText = (EditText) findViewById(R.id.edit_website);
        mNoteEditText = (EditText) findViewById(R.id.edit_note);
        mIconImageView = (ImageView) findViewById(R.id.image_view_icon);

        mNameEditText.setOnTouchListener(mTouchListener);
        mLoginEditText.setOnTouchListener(mTouchListener);
        mPasswordEditText.setOnTouchListener(mTouchListener);
        mWebsiteEditText.setOnTouchListener(mTouchListener);
        mNoteEditText.setOnTouchListener(mTouchListener);

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
        String nameString = mNameEditText.getText().toString().trim();
        String loginString = mLoginEditText.getText().toString().trim();
        String passwordString = mPasswordEditText.getText().toString().trim();
        String websiteString = mWebsiteEditText.getText().toString().trim();
        String noteString = mNoteEditText.getText().toString().trim();

        Date currentDate = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.sss");
        String dateString = sdf.format(currentDate);
        //TODO: Если нет изменений, дата обновляться не должна

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
        values.put(PasswordContract.PasswordEntry.COLUMN_CHANGED_AT, dateString);
        values.put(PasswordContract.PasswordEntry.COLUMN_COLOR_INDEX, mColorIndex);

        //TODO: Уьрать эти тосты
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
                    NavUtils.navigateUpFromSameTask(EditorActivity.this);
                    return true;
                }

                // В противном случае, если есть несохранённые изменения, настраиваем диалог для предупреждения пользователя.
                // Создаём обработчик нажатия, чтобы обработать подтверждение пользователем отмены изменений.
                DialogInterface.OnClickListener discardButtonClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // User clicked "Discard" button, navigate to parent activity.
                                NavUtils.navigateUpFromSameTask(EditorActivity.this);
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
                PasswordContract.PasswordEntry.COLUMN_NOTE,
                PasswordContract.PasswordEntry.COLUMN_CHANGED_AT,
                PasswordContract.PasswordEntry.COLUMN_IS_FAVORITE,
                PasswordContract.PasswordEntry.COLUMN_COLOR_INDEX
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
            int colorIndexColumnIndex = cursor.getColumnIndex(PasswordContract.PasswordEntry.COLUMN_COLOR_INDEX);

            // Извлекаем из курсора значение по данному индексу колонки
            String name = cursor.getString(nameColumnIndex);
            String login = cursor.getString(loginColumnIndex);
            String password = cursor.getString(passwordColumnIndex);
            String website = cursor.getString(websiteColumnIndex);
            String note = cursor.getString(noteColumnIndex);

            mColorIndex = cursor.getInt(colorIndexColumnIndex);
            Drawable background = getDrawableByIndex(mColorIndex);
            mIconImageView.setBackground(background);

            // Обновляем представление на экране значениями из базы данных
            mNameEditText.setText(name);
            mLoginEditText.setText(login);
            mPasswordEditText.setText(password);
            mWebsiteEditText.setText(website);
            mNoteEditText.setText(note);
        }
    }

    private Drawable getDrawableByIndex(int index) {
        //TODO: Костыль! Избавиться от повторения этого метода
        switch (index){
            case 0:
                return getDrawable(R.drawable.color_circle_1);
            case 1:
                return getDrawable(R.drawable.color_circle_2);
            case 2:
                return getDrawable(R.drawable.color_circle_3);
            case 3:
                return getDrawable(R.drawable.color_circle_4);
            case 4:
                return getDrawable(R.drawable.color_circle_5);
            case 5:
                return getDrawable(R.drawable.color_circle_6);
            case 6:
                return getDrawable(R.drawable.color_circle_7);
            case 7:
                return getDrawable(R.drawable.color_circle_8);
            case 8:
                return getDrawable(R.drawable.color_circle_9);
            case 9:
                return getDrawable(R.drawable.color_circle_10);
            case 10:
                return getDrawable(R.drawable.color_circle_11);
            case 11:
                return getDrawable(R.drawable.color_circle_12);
            case 12:
                return getDrawable(R.drawable.color_circle_13);
            case 13:
                return getDrawable(R.drawable.color_circle_14);
            case 14:
                return getDrawable(R.drawable.color_circle_15);
            case 15:
                return getDrawable(R.drawable.color_circle_16);
        }
        return null;
    }

    @Override
    public void onLoaderReset(Loader loader) {
        // Очищаем поля
        mNameEditText.setText("");
        mLoginEditText.setText("");
        mPasswordEditText.setText("");
        mWebsiteEditText.setText("");
        mNoteEditText.setText("");
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
        //TODO: Возвращаться надо не к MainActivity, а к ViewerActivity
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
                // User clicked the "Cancel" button, so dismiss the mDialog
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

    /**
     * Обработчик события нажатия на иконку карты. Открывает всплывающее меню для настройки иконки.
     * @param view Элемент, который был щёлкнут
     */
    public void onIconClicked(View view) {
        PopupMenu popupMenu = new PopupMenu(this, mIconImageView);
        popupMenu.getMenuInflater().inflate(R.menu.menu_icon_popup, popupMenu.getMenu());

        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()){
                    case R.id.action_select_color:
                        showDialog(CATEGORY_ID);
                        return true;
                    default:
                        return false;
                }
            }
        });

        popupMenu.show();
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id){
            case CATEGORY_ID:
                AlertDialog.Builder builder;
                Context mContext = this;

                //TODO: Почитать про LayoutInflater
                LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(LAYOUT_INFLATER_SERVICE);
                View layout = inflater.inflate(R.layout.dialog_select_color, (ViewGroup)findViewById(R.id.layout_root));

                final GridView gridView = (GridView) layout.findViewById(R.id.gridview1);
                ImageAdapter imageAdapter = new ImageAdapter(this);
                gridView.setAdapter(imageAdapter);

                gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                        updateColorIndex(position);

                        Drawable background = getDrawableByIndex(position);
                        mIconImageView.setBackground(background);
                        //Toast.makeText(view.getContext(), "Position is " + position, Toast.LENGTH_LONG).show();
                        mDialog.dismiss();
                    }
                });

                builder = new AlertDialog.Builder(mContext);
                builder.setView(layout)
                        .setTitle("Select Color")   //TODO: Поместить в строковые ресурсы
                        .setNegativeButton("Cancel",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        dialogInterface.cancel();
                                    }
                                });
                mDialog = builder.create();

                break;
            default:
                mDialog = null;
        }
        return mDialog;
    }

    public class ImageAdapter extends BaseAdapter {

        private Context mContext;
        private LayoutInflater mInflater;

        public ImageAdapter(Context c){
            mInflater = LayoutInflater.from(c);
            mContext = c;
        }

        @Override
        public int getCount() {
            return mThumbIds.length;
        }

        @Override
        public Object getItem(int i) {
            return null;
        }

        @Override
        public long getItemId(int i) {
            return 0;
        }

        // Создаем ImageView для каждого пункта
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ImageAdapter.ViewHolder holder;
            if (convertView == null){ // если не рециркулировано
                convertView = mInflater.inflate(R.layout.color_grid_item, null);
                convertView.setLayoutParams(new GridView.LayoutParams(100, 100));
                holder = new ImageAdapter.ViewHolder();
                //holder.title = (TextView) convertView.findViewById(R.id.text);
                holder.icon = (ImageView) convertView.findViewById(R.id.image);
                convertView.setTag(holder);
            } else {
                holder = (ImageAdapter.ViewHolder) convertView.getTag();
            }

            holder.icon.setAdjustViewBounds(true);
            holder.icon.setScaleType(ImageView.ScaleType.CENTER_CROP);
            //holder.icon.setPadding(5, 5, 5, 5);

            //holder.title.setText(categoryContent[position]);
            holder.icon.setBackground(getDrawable(mThumbIds[position]));
            return convertView;
        }

        public class ViewHolder {
            TextView title;
            ImageView icon;
        }

        // ссылки на наши иконки
        private Integer[] mThumbIds = {
                R.drawable.color_circle_1,
                R.drawable.color_circle_2,
                R.drawable.color_circle_3,
                R.drawable.color_circle_4,
                R.drawable.color_circle_5,
                R.drawable.color_circle_6,
                R.drawable.color_circle_7,
                R.drawable.color_circle_8,
                R.drawable.color_circle_9,
                R.drawable.color_circle_10,
                R.drawable.color_circle_11,
                R.drawable.color_circle_12,
                R.drawable.color_circle_13,
                R.drawable.color_circle_14,
                R.drawable.color_circle_15,
                R.drawable.color_circle_16
        };
    }

    /**
     * Обновляет в базе индекс цвета иконки пароля
     *
     * @param value Новое значение индекса
     */
    private void updateColorIndex(int value) {

        // Создаем объект ContentValues
        ContentValues values = new ContentValues();
        values.put(PasswordContract.PasswordEntry.COLUMN_COLOR_INDEX, value);

        //TODO: Добавить сообщение в лог или убрать эту переменную
        int rowsAffected = getContentResolver().update(mCurrentPasswordUri, values, null, null);
    }
}
