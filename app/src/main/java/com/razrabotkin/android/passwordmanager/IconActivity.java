package com.razrabotkin.android.passwordmanager;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import com.razrabotkin.android.passwordmanager.data.IconAdapter;

import java.util.ArrayList;
import java.util.List;

public class IconActivity extends ListActivity {

    // Константа для хранения названия строки, возвращаемой как результат выполнения операции
    public static String RESULT_COUNTRYCODE = "iconcode";

    // Массив, в котором будут храниться коды иконок
    public String[] iconcodes;

    // Массив, в котором будут храниться id иконок
    public int[] iconids;

    // Массив, в который будут помещены изображения из ресурсов
    //private TypedArray imgs;

    // Список иконок для отображения в GridView
    private List<Icon> iconList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_icon);

        // Заполняем список, выводимый в ListView, из массивов, хранимых в ресурсах.
        populateIconList();

        // Инициализируем адаптер
        ArrayAdapter<Icon> adapter = new IconAdapter(this, iconList);

        // Устанавливаем этот адаптер списку
        setListAdapter(adapter);

        //TODO: Шо такое getListView
        // Устанавливаем сриску обработчик нажатия на пункт
        getListView().setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                // Извлекаем страну из списка по номеру нажатой позиции
                Icon c = iconList.get(position);

                // Создаем интент результата
                Intent returnIntent = new Intent();

                // Добавляем в этот интент строку с кодом страны
                returnIntent.putExtra(RESULT_COUNTRYCODE, c.getImageResourceId());

                // Устанавливаем операции интент результата
                setResult(RESULT_OK, returnIntent);

                // После работы с TypedArray вызываем метод recycle()
                //imgs.recycle();

                // Завершаем операцию
                finish();
            }
        });
    }

    /**
     * Извлекает из ресурсов массивы названий стран, кодов стран и изображений, и наполняет ими список стран для отображения в ListView
     */
    private void populateIconList() {

        // Инициализируем список стран для отображения
        iconList = new ArrayList<>();

        // Получаем строковые массивы кодов иконок из соответствующих строковых ресурсов
        iconcodes = getResources().getStringArray(R.array.icon_codes);

        iconids = new int[]{
                R.drawable.ic_twitter,
                R.drawable.ic_twitter,
                R.drawable.ic_twitter,
                R.drawable.ic_twitter,
        };

        //imgs = getResources().obtainTypedArray(R.array.icon_images);
        for (int i = 0; i < iconcodes.length; i++){
            iconList.add(new IconActivity.Icon(iconcodes[i], iconids[i]));
        }
    }

    /**
     * Класс для передачи иконки в диалог выбора иконки. //TODO: Возмонжо, его можно будет заменить на какой-нибудь примитивный тип, а может быть, добавить туда ещё поля
     */
    public class Icon{
        private int mImageResourceId;
        private String mCode;

        public Icon(String code, int imageResourceId){
            mCode = code;
            mImageResourceId = imageResourceId;
        }

        public int getImageResourceId(){
            return mImageResourceId;
        }

        public String getCode(){
            return mCode;
        }
    }

//    public class Icon{
//        private String code;
//        private Drawable image;
//
//        public Icon(String code, Drawable image){
//            this.image = image;
//            this.code = code;
//        }
//
//        public Drawable getImage(){
//            return image;
//        }
//
//        public String getCode(){
//            return code;
//        }
//    }
}
