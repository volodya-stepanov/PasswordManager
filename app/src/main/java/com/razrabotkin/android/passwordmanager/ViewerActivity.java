package com.razrabotkin.android.passwordmanager;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.LoaderManager;
import android.content.ClipData;
import android.content.ClipboardManager;
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
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.PopupMenu;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.razrabotkin.android.passwordmanager.data.PasswordContract;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ViewerActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int EXISTING_PASSWORD_LOADER = 0;
    /**
     * URI контента для существующего пароля (null если это новый пароль)
     */
    private Uri mCurrentCardUri;
    private TextView mNameTextView;
    private TextView mLoginTextView;
    private TextView mPasswordTextView;
    private TextView mWebsiteTextView;
    private TextView mNoteTextView;
    private ImageButton mShowPasswordImageButton;
    private LinearLayout mLinearLayout; // Для Snackbar
    private TextView mChangedAtTextView;
    private ToggleButton mFavoriteToggleButton;
    private ImageView mIconImageView;

    private boolean mShowPassword = true;

    private final int CATEGORY_ID=0;
    Dialog mDialog;

	//private boolean mCardHasChanged = false;

//    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
//        @Override
//        public boolean onTouch(View view, MotionEvent motionEvent) {
//            mCardHasChanged = true;
//            return false;
//        }
//    };

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_viewer);

        // Проверяем интент, запустивший эту операцию,
        // чтобы выяснить, создаём мы новую запись или редактируем существующую.
        Intent intent = getIntent();
        mCurrentCardUri = intent.getData();

        // Если интент НЕ содержит URI контента, мы знаем, что создаём новую карту.
        if (mCurrentCardUri == null) {
            // Это новая карта, поэтому меняем App Bar на "Создание карты"
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

        mNameTextView = (TextView) findViewById(R.id.edit_name);
        mLoginTextView = (TextView) findViewById(R.id.edit_login);
        mPasswordTextView = (TextView) findViewById(R.id.edit_password);
        mWebsiteTextView = (TextView) findViewById(R.id.edit_website);
        mNoteTextView = (TextView) findViewById(R.id.edit_note);
        mShowPasswordImageButton = (ImageButton) findViewById(R.id.show_password_image_button);
        mLinearLayout = (LinearLayout) findViewById(R.id.activity_viewer);
        mChangedAtTextView = (TextView) findViewById(R.id.text_view_changed_at);
        mFavoriteToggleButton = (ToggleButton) findViewById(R.id.toggle_button_favorite);
        mIconImageView = (ImageView) findViewById(R.id.image_view_icon);

//        mNameTextView.setOnTouchListener(mTouchListener);
//        mLoginEditText.setOnTouchListener(mTouchListener);
//        mPasswordEditText.setOnTouchListener(mTouchListener);
//        mWebsiteEditText.setOnTouchListener(mTouchListener);
//        mNoteEditText.setOnTouchListener(mTouchListener);

        if (mCurrentCardUri != null) {
            // Инициализируем загрузчик
            getLoaderManager().initLoader(EXISTING_PASSWORD_LOADER, null, this);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_viewer, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        // Если это новая запись, скрываем пункт меню "Удалить"
        if (mCurrentCardUri == null) {
            MenuItem menuItem = menu.findItem(R.id.action_delete);
            menuItem.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_edit:
                openEditor();
                return true;
            case R.id.action_delete:
                // Отображаем диалог подтверждения удаления
                showDeleteConfirmationDialog();
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Открывает {@link EditorActivity} для данной записи
     */
    private void openEditor() {
        // Запускаем EditorActivity, куда пробрасываем текущий Uri,
        // как это было сделано в MainActivity при запуске этой операции (EditorActivity)
        //TODO: Убрать проверку на нулевой URI, здесь не может быть нулевого URI
        //TODO: Назначить вызываемой операции нужного родителя
        Intent intent = new Intent(ViewerActivity.this, EditorActivity.class);
        intent.setData(mCurrentCardUri);
        startActivity(intent);
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
                mCurrentCardUri,    // URI контента запроса для текущей карты
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
            int changedAtColumnIndex = cursor.getColumnIndex(PasswordContract.PasswordEntry.COLUMN_CHANGED_AT);
            int isFavoriteColumnIndex = cursor.getColumnIndex(PasswordContract.PasswordEntry.COLUMN_IS_FAVORITE);
            int colorColumnIndex = cursor.getColumnIndex(PasswordContract.PasswordEntry.COLUMN_COLOR_INDEX);
            //TODO: Вот здесь нужно преобразовать дату и признак избранности

            // Извлекаем из курсора значение по данному индексу колонки
            String name = cursor.getString(nameColumnIndex);
            String login = cursor.getString(loginColumnIndex);
            String password = cursor.getString(passwordColumnIndex);
            String website = cursor.getString(websiteColumnIndex);
            String note = cursor.getString(noteColumnIndex);
            String changedAt = cursor.getString(changedAtColumnIndex);

            // Признак избранности
            if (cursor.getInt(isFavoriteColumnIndex) == 0) {
                mFavoriteToggleButton.setChecked(false);
            } else {
                mFavoriteToggleButton.setChecked(true);
            }

            //Дата изменения
            // Создаем новый SimpleDateFormat, в котором указываем исходный формат, в котором хранится дата
            SimpleDateFormat format1 = new SimpleDateFormat();
            format1.applyPattern("yyyy-MM-dd HH:mm:ss.sss");

            // Создаем новый SimpleDateFormat, в котором указываем формат, в который необходимо преобразовать дату
            SimpleDateFormat format2 = new SimpleDateFormat();
            format2.applyPattern("dd.MM.yyyy HH:mm");

            Date date1;
            try {
                date1 = format1.parse(changedAt);       // Преобразуем значение из базы данных в дату с использованием формата 1
                String date2 = format2.format(date1);    // Форматируем полученную дату с использованием формата 2 и преобразуе её обратно в строку
                mChangedAtTextView.setText("Изменено: " + date2);   //TODO: Добавить строковый ресурс				// Присваиваем элементу mChangedAtTextView строку с новой датой
            } catch (ParseException e) {
                e.printStackTrace();
            }

            // Цвет иконки
            int colorIndex = cursor.getInt(colorColumnIndex);
            //TODO: Избавиться от повторения этого кода!
            Drawable background = getDrawableByIndex(colorIndex);
            mIconImageView.setBackground(background);

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
        mFavoriteToggleButton.setChecked(false);
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

//    @Override
//    public void onBackPressed() {
//        // Если запись не изменна, продолжаем обработку нажатия кнопки Назад
//        if (!mCardHasChanged) {
//            super.onBackPressed();
//            return;
//        }
//
//        // В противном случае, если есть несохранённые изменения, настраиваем диалог для предупреждения пользователя.
//        // Создаём обработчик нажатия, чтобы обработать подтверждение пользователем отмены изменений.
//        DialogInterface.OnClickListener discardButtonClickListener =
//                new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialogInterface, int i) {
//                        // Пользователь нажал кнопку "Отменить", закрываем текущую операцию.
//                        finish();
//                    }
//                };
//
//        // Показываем диалог о том, что есть несохраненные изменения
//        showUnsavedChangesDialog(discardButtonClickListener);
//    }

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
        if (mCurrentCardUri != null) {
            // Вызываем ContentResolver чтобы удалить запись по данному URI контента.
            // Передаем null в аргументы selection and selection args потому что mCurrentCardUri
            // уже идентифицирует новую запись.
            int rowsDeleted = getContentResolver().delete(mCurrentCardUri, null, null);

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
     * Вызывается при нажатии кнопки с глазом "Показывать пароль"
     *
     * @param view Кнопка, на которую нажали
     */
    public void onShowPasswordImageButtonClick(View view) {
        ImageButton imageButton = (ImageButton) view;
        mShowPassword = !mShowPassword;
        updateTextTypeAndVisibility();
    }

    public void updateTextTypeAndVisibility() {
        //TODO: При отображении/скрытии текстового пароля клавиатура не меняется
        if (mShowPassword) {
            mPasswordTextView.setInputType(InputType.TYPE_CLASS_TEXT);
        } else {
            mPasswordTextView.setInputType(InputType.TYPE_CLASS_TEXT |
                    InputType.TYPE_TEXT_VARIATION_PASSWORD);
        }
    }

    /**
     * Обработчик события нажатия на макет "Логин". Копирует его в буфер обмена и отображает на экране Snackbar с соответствующей надписью
     *
     * @param view Элемент, который был щёлкнут
     */
    public void copyLogin(View view) {
        String login = mLoginTextView.getText().toString();
        copyToClipboard(login);
        makeSnackbar();
    }

    /**
     * Обработчик события нажатия на макет "Пароль". Копирует его в буфер обмена и отображает на экране Snackbar с соответствующей надписью
     *
     * @param view Элемент, который был щёлкнут
     */
    public void copyPassword(View view) {
        String password = mPasswordTextView.getText().toString();
        copyToClipboard(password);
        makeSnackbar();
    }

    /**
     * Обработчик события нажатия на макет "Веб-сайт". Копирует его в буфер обмена и отображает на экране Snackbar с соответствующей надписью
     *
     * @param view Элемент, который был щёлкнут
     */
    public void copyWebsite(View view) {
        String website = mWebsiteTextView.getText().toString();
        copyToClipboard(website);
        makeSnackbar();
    }

    /**
     * Копирует указанный текст в буфер обмена
     *
     * @param text Текст, который необходимо скопировать
     */
    private void copyToClipboard(String text) {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText(text, text);
        clipboard.setPrimaryClip(clip);
    }

    /**
     * Отображает элемент Snackbar с надписью "Текст скопирован"
     */
    private void makeSnackbar() {
        // Создаём snackbar
        Snackbar snackbar = Snackbar.make(mLinearLayout, "Текст скопирован", Snackbar.LENGTH_LONG);

        // Меняем цвет фона. Напрямую для Snackbar этого сделать нельзя, но можно
        // получить элемент View, который представляет собой макет этого Snackbar
        View snackbarView = snackbar.getView();

        // Устанавливаем этому элементу View фоновый цвет
        snackbarView.setBackgroundColor(getResources().getColor(R.color.colorAccent));

        // С атрибутами текста - аналогичная ситуация. Сначала получаем TextView,
        // который отображается на панели
        TextView snackTextView = (TextView) snackbarView.findViewById(android.support.design.R.id.snackbar_text);

        // Затем ему можно установить цвет и размер
        snackTextView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        //snackTextView.setTextSize(16);

        // Показываем snackbar на экране
        snackbar.show();
    }

    /**
     * Вызывается при нажатии кнопки со звездой
     *
     * @param view Элемент, который был щелкнут
     */
    public void onFavoriteButtonClick(View view) {
        boolean isFavorite;

        if (mFavoriteToggleButton.isChecked()) {
            isFavorite = true;
        } else {
            isFavorite = false;
        }

        updateIsFavorite(isFavorite);
    }

    /**
     * Обновляет в базе признак избранности для данной записи
     *
     * @param value Новое значение признака избранности
     */
    private void updateIsFavorite(boolean value) {

        // Создаем объект ContentValues
        ContentValues values = new ContentValues();
        values.put(PasswordContract.PasswordEntry.COLUMN_IS_FAVORITE, value);

        int rowsAffected = getContentResolver().update(mCurrentCardUri, values, null, null);
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
        int rowsAffected = getContentResolver().update(mCurrentCardUri, values, null, null);
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
                    case R.id.action_select_symbol:
                        Intent intent = new Intent(getApplicationContext(), IconActivity.class);
                        startActivityForResult(intent, 1);
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

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Проверяем код запроса и код результата.
        // Если код запроса 1 (то есть, при нажатии на кнопку в MainActivity), и результат - ОК
        if (requestCode == 1 && resultCode == Activity.RESULT_OK){
            // Извлекаем из интента data значение строки CountrycodeActivity.RESULT_COUNTRYCODE.
            // и записываем в строковую переменную imageResourceId.
            int imageResourceId = data.getIntExtra(IconActivity.RESULT_COUNTRYCODE, 0);

            // Выводим на экран сообщение - тост со значением строки imageResourceId.
            //Toast.makeText(this, "You selected countrycode: " + imageResourceId, Toast.LENGTH_LONG).show();
            mIconImageView.setImageResource(imageResourceId);
        }
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
            ViewHolder holder;
            if (convertView == null){ // если не рециркулировано
                convertView = mInflater.inflate(R.layout.color_grid_item, null);
                convertView.setLayoutParams(new GridView.LayoutParams(100, 100));
                holder = new ViewHolder();
                //holder.title = (TextView) convertView.findViewById(R.id.text);
                holder.icon = (ImageView) convertView.findViewById(R.id.image);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
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


}
