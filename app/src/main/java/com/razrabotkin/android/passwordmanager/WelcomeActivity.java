package com.razrabotkin.android.passwordmanager;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class WelcomeActivity extends AppCompatActivity {

    //TODO: Разобраться с этими двумя
    private ViewPager viewPager;
    private MyViewPagerAdapter myViewPagerAdapter;

    //TODO: Возможно, этих двоих нужно будет засунуть в сами макеты слайдов
    private LinearLayout dotsLayout;
    private TextView[] dots;

    // В этом массиве хранятся id четырёх макетов слайдов
    private int[] layouts;
    private Button btnNext;
    private PrefManager prefManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Проверяем, запущено ли приложение в первый раз, перед вызовом setContentView()
        // Если метод isFirstTimeLaunch() возвращает ложь, вызываем метод, запускающий главную операцию, и завершаем текущую
        prefManager = new PrefManager(this);
        if (!prefManager.isFirstTimeLaunch()) {
            launchHomeScreen();
            finish();   //TODO: Попробовать убрать эту строку, т.к. она вызывается в методе launchHomeScreen();
        }

        setContentView(R.layout.activity_welcome);

        // Устанавливаем заголовок ActionBar
        setTitle(R.string.title_activity_welcome);

        viewPager = (ViewPager) findViewById(R.id.view_pager);
        dotsLayout = (LinearLayout) findViewById(R.id.layoutDots);
        //btnSkip = (Button) findViewById(R.id.btn_skip);
        btnNext = (Button) findViewById(R.id.btn_next);


        // Добавляем макеты всех приветственных слайдов
        layouts = new int[]{
                R.layout.welcome_slide1,
                R.layout.welcome_slide2,
                R.layout.welcome_slide3,
                R.layout.welcome_slide4};

        // Добавляем вниз точки
        addBottomDots(0);

        myViewPagerAdapter = new MyViewPagerAdapter();
        viewPager.setAdapter(myViewPagerAdapter);
        viewPager.addOnPageChangeListener(viewPagerPageChangeListener);

        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Проверяем, не последняя ли это страница
                // Если последняя, будет запущена следующая операция - настройка
                int current = getItem(+1);
                if (current < layouts.length) {
                    // Переходим на домашний экран
                    viewPager.setCurrentItem(current);
                } else {
                    launchSetupScreen();
                }
            }
        });
    }

    /**
     * Добавляет ряд точек внизу для текущей страницы
     * @param currentPage Номер текущей страницы
     */
    private void addBottomDots(int currentPage) {
        // Ряд точек представлен текстовой строкой, содержащей указанное количество спецсимволов HTML в виде кружочка ("•").
        dots = new TextView[layouts.length];

        // Удаляем все элементы View из ViewGroup
        dotsLayout.removeAllViews();

        // Обходим классическим циклом for от 0 до количества слайдов, где теперь представляем каждую точку в виде отдельного элемента TextView.
        // Устанавливаем ей текст (вышеупомянутый спецсимвол), размер и цвет шрифта, после чего добавляем этот TextView с точкой в горизонтальный LinearLayout
        for (int i = 0; i < dots.length; i++) {
            dots[i] = new TextView(this);
            dots[i].setText(Html.fromHtml("&#8226;"));
            dots[i].setTextSize(35);
            dots[i].setTextColor(getResources().getColor(R.color.dot_dark));
            dotsLayout.addView(dots[i]);
        }

        // Если длина массива больше нуля, устанавливаем точке текущей страницы активный цвет
        if (dots.length > 0)
            dots[currentPage].setTextColor(getResources().getColor(R.color.dot_light));
    }

    private int getItem(int i) {
        return viewPager.getCurrentItem() + i;
    }

    private void launchHomeScreen() {
        prefManager.setFirstTimeLaunch(false);
        startActivity(new Intent(WelcomeActivity.this, MainActivity.class));
        finish();
    }

    private void launchSetupScreen() {
        //prefManager.setFirstTimeLaunch(false);
        Intent intent = new Intent(WelcomeActivity.this, SetupActivity.class);
        startActivity(intent);
        finish();
    }

    //  viewpager change listener
    ViewPager.OnPageChangeListener viewPagerPageChangeListener = new ViewPager.OnPageChangeListener() {

        @Override
        public void onPageSelected(int position) {
            addBottomDots(position);

            // Меняем текст кнопки 'ДАЛЕЕ' / 'НАСТРОЙКА'
            if (position == layouts.length - 1) {
                // Последняя страница. Меняем текст кнопки на "НАСТРОЙКА"
                btnNext.setText(getString(R.string.setup));
                //btnSkip.setVisibility(View.GONE);
            } else {
                // Продолжаем листать
                btnNext.setText(getString(R.string.next));
                //btnSkip.setVisibility(View.VISIBLE);
            }
        }

        @Override
        public void onPageScrolled(int arg0, float arg1, int arg2) {

        }

        @Override
        public void onPageScrollStateChanged(int arg0) {

        }
    };

    /**
     * View pager adapter
     */
    public class MyViewPagerAdapter extends PagerAdapter {
        private LayoutInflater layoutInflater;

        public MyViewPagerAdapter() {
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            View view = layoutInflater.inflate(layouts[position], container, false);
            container.addView(view);

            return view;
        }

        @Override
        public int getCount() {
            return layouts.length;
        }

        @Override
        public boolean isViewFromObject(View view, Object obj) {
            return view == obj;
        }


        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            View view = (View) object;
            container.removeView(view);
        }
    }
}
