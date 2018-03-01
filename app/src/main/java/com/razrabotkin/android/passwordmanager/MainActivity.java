package com.razrabotkin.android.passwordmanager;

import android.app.LoaderManager;
import android.app.SearchManager;
import android.content.ContentUris;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.razrabotkin.android.passwordmanager.data.PasswordContract;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, LoaderManager.LoaderCallbacks<Cursor> {

    private static final int PASSWORD_LOADER = 0;

    PasswordCursorAdapter mCursorAdapter;

    private final static String TAG= MainActivity.class.getName().toString();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, EditorActivity.class);
                startActivity(intent);
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        // Находим ListView для заполнения
        ListView cardsListView = (ListView) findViewById(R.id.listview_cards);

        // Находим и устанавливаем empty view на ListView таким образом,
        // что оно отображается, только когда в списке 0 пунктов
        View emptyView = findViewById(R.id.empty_view);
        cardsListView.setEmptyView(emptyView);

        // Настраиваем CursorAdapter
        mCursorAdapter = new PasswordCursorAdapter(this, null);

        // Привязываем CursorAdapter к ListView
        cardsListView.setAdapter(mCursorAdapter);

        // Настраиваем обработчик щелчка
        cardsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                // Создаём новый интент для перехода к {@link ViewerActivity}
                Intent intent = new Intent(MainActivity.this, ViewerActivity.class);

                // Формируем URI контента, который представляет конкретную карту, которую мы щёлкнули,
                // прикрепляя id (переданный в качестве входного параметра в этот метод) к
                // {@link PasswordEntry#CONTENT_URI}.
                // Например, URI будет равен "com.razrabotkin.android.passwordmanager/passwords/2",
                // если мы нажмём на карту с ID = 2.
                Uri currentCardUri = ContentUris.withAppendedId(PasswordContract.PasswordEntry.CONTENT_URI, id);

                // Устанавливаем URI в качестве значения поля Data интента
                intent.setData(currentCardUri);

                // Запускаем {@link ViewerActivity}, чтобы отобразить данные выбранной карты
                startActivity(intent);
            }
        });

        // Инициализируем загрузчик
        getLoaderManager().initLoader(PASSWORD_LOADER, null, this);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

@Override
public boolean onCreateOptionsMenu(Menu menu) {
    // Inflate the menu; this adds items to the action bar if it is present.
    getMenuInflater().inflate(R.menu.main, menu);

    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
        SearchManager manager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
        searchView.setSearchableInfo(manager.getSearchableInfo(getComponentName()));

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

            @Override
            public boolean onQueryTextSubmit(String s) {
                Log.d(TAG, "onQueryTextSubmit ");
                getCardsListByKeyword(s);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                Log.d(TAG, "onQueryTextChange ");
                getCardsListByKeyword(s);
                return false;
            }
        });
    }

    return true;
}

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch (id){
            case R.id.action_settings:
                return true;
            case R.id.action_delete_all_entries:
                deleteAllEntries();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Удаляет все записи из базы данных
     */
    private void deleteAllEntries() {
        int rowsDeleted = getContentResolver().delete(PasswordContract.PasswordEntry.CONTENT_URI, null, null);
        Log.v("CatalogActivity", rowsDeleted + " rows deleted from database");
    }

    // В этом методе обрабатываются щелчки по пунктам navigation drawer
    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_all_cards) {
            selectAll();
        } else if (id == R.id.nav_favorites) {
            selectFavorites();
        }
//         else if (id == R.id.nav_slideshow) {
//
//        } else if (id == R.id.nav_manage) {
//
//        } else if (id == R.id.nav_share) {
//
//        } else if (id == R.id.nav_send) {
//
//        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    /**
     * Осуществляет выборку из базы объектов с признаком избранности
     */
    private void selectFavorites() {
        // Определяем массив projection, который указывает, какие колонки из базы данных
        // мы увидим после этого запроса.
        String[] projection = {
                PasswordContract.PasswordEntry._ID,
                PasswordContract.PasswordEntry.COLUMN_NAME,
                PasswordContract.PasswordEntry.COLUMN_LOGIN,
                PasswordContract.PasswordEntry.COLUMN_IS_FAVORITE
        };

        // Задаём параметр selection, определяющий в запросе условие WHERE
        String selection = PasswordContract.PasswordEntry.COLUMN_IS_FAVORITE + "=1";

        // Выполняем запрос, получая в результате курсор
        Cursor cursor = getContentResolver().query(PasswordContract.PasswordEntry.CONTENT_URI, projection, selection, null, null);

        // Вызываем метод swapCursor с полученным курсором, чтобы список обновился
        mCursorAdapter.swapCursor(cursor);
    }

private void getCardsListByKeyword(String search) {
    // Определяем массив projection, который указывает, какие колонки из базы данных
    // мы увидим после этого запроса.
    String[] projection = {
            PasswordContract.PasswordEntry._ID,
            PasswordContract.PasswordEntry.COLUMN_NAME,
            PasswordContract.PasswordEntry.COLUMN_LOGIN,
            PasswordContract.PasswordEntry.COLUMN_NOTE,
            PasswordContract.PasswordEntry.COLUMN_IS_FAVORITE
    };

    // Задаём параметр selection, определяющий в запросе условие WHERE
    String selection = PasswordContract.PasswordEntry.COLUMN_NAME + " LIKE '%" + search + "%' OR " +
    PasswordContract.PasswordEntry.COLUMN_LOGIN + " LIKE '%" + search + "%' OR " +
    PasswordContract.PasswordEntry.COLUMN_NOTE + " LIKE '%" + search + "%'";

    // Задаём параметр selectionArgs, определяющий значения аргументов
    String[] selectionArgs = {search};

    // Выполняем запрос, получая в результате курсор
    Cursor cursor = getContentResolver().query(PasswordContract.PasswordEntry.CONTENT_URI, projection, selection, null, null);

    // Этот метод предназначен для выделения текста
    mCursorAdapter.searchText(search);

    // Вызываем метод swapCursor с полученным курсором, чтобы список обновился
    mCursorAdapter.swapCursor(cursor);
}

    /**
     * Осуществляет выборку из базы всех записей
     */
    private void selectAll() {
        // Определяем массив projection, который указывает, какие колонки из базы данных
        // мы увидим после этого запроса.
        String[] projection = {
                PasswordContract.PasswordEntry._ID,
                PasswordContract.PasswordEntry.COLUMN_NAME,
                PasswordContract.PasswordEntry.COLUMN_LOGIN,
                PasswordContract.PasswordEntry.COLUMN_NOTE,
                PasswordContract.PasswordEntry.COLUMN_IS_FAVORITE,
                PasswordContract.PasswordEntry.COLUMN_COLOR_INDEX

        };

        // Выполняем запрос, получая в результате курсор
        Cursor cursor = getContentResolver().query(PasswordContract.PasswordEntry.CONTENT_URI, projection, null, null, null);

        // Вызываем метод swapCursor с полученным курсором, чтобы список обновился
        mCursorAdapter.swapCursor(cursor);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        // Определяем массив projection, который указывает, какие колонки из базы данных
        // мы увидим после этого запроса.
        String[] projection = {
                PasswordContract.PasswordEntry._ID,
                PasswordContract.PasswordEntry.COLUMN_NAME,
                PasswordContract.PasswordEntry.COLUMN_LOGIN,
                PasswordContract.PasswordEntry.COLUMN_NOTE,
                PasswordContract.PasswordEntry.COLUMN_IS_FAVORITE,
                PasswordContract.PasswordEntry.COLUMN_COLOR_INDEX
        };

        // Этот загрузчик выполнит метод query в фоновом потоке
        return new CursorLoader(this,
                PasswordContract.PasswordEntry.CONTENT_URI,
                projection,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        // Обновляем {@link PasswordCursorAdapter} новым куросором, содержащим обновленные данные
        mCursorAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // Метод, вызываемый, когда данные должны быть удалены
        mCursorAdapter.swapCursor(null);
    }

}
