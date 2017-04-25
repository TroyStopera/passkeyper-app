package com.passkeyper.android;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.passkeyper.android.adapter.EntryAdapter;
import com.passkeyper.android.prefs.UserPreferences;
import com.passkeyper.android.util.SnackbarUndoDelete;
import com.passkeyper.android.vaultmodel.EntryRecord;

import java.util.Collection;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, EntryAdapter.OnActionListener, SnackbarUndoDelete.SnackBarDeleteListener<EntryRecord>, EntryAdapter.OnEntryExpandedListener {

    public static final int EDIT_REQUEST_CODE = 24;

    private UserPreferences mUserPreferences;
    private RecyclerView mEntryRecyclerView;
    private EntryAdapter mEntryAdapter;
    private SearchView mSearchView;
    private SnackbarUndoDelete<EntryRecord> mSnackbarUndoDelete;

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer != null && drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else if (mSearchView != null && !mSearchView.isIconified()) {
            mSearchView.setIconified(true);
        } else if (mEntryAdapter != null && mEntryAdapter.hasExpandedEntry()) {
            mEntryAdapter.collapseSelected();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        MenuItem searchItem = menu.findItem(R.id.action_search);
        mSearchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        mSearchView.setOnQueryTextListener(mEntryAdapter);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_sort_alpha_asc:
                mUserPreferences.setSortOrder(EntryAdapter.SortOrder.AtoZ);
                mEntryAdapter.setSortOrder(EntryAdapter.SortOrder.AtoZ);
                return true;
            case R.id.action_sort_alpha_desc:
                mUserPreferences.setSortOrder(EntryAdapter.SortOrder.ZtoA);
                mEntryAdapter.setSortOrder(EntryAdapter.SortOrder.ZtoA);
                return true;
            case R.id.action_sort_chron_asc:
                mUserPreferences.setSortOrder(EntryAdapter.SortOrder.OldestFirst);
                mEntryAdapter.setSortOrder(EntryAdapter.SortOrder.OldestFirst);
                return true;
            case R.id.action_sort_chron_desc:
                mUserPreferences.setSortOrder(EntryAdapter.SortOrder.NewestFirst);
                mEntryAdapter.setSortOrder(EntryAdapter.SortOrder.NewestFirst);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onEditClicked(EntryRecord record) {
        Intent intent = new Intent(this, EditEntry.class);
        intent.putExtra(EditEntry.ENTRY_RECORD_EXTRA_KEY, record);
        startActivityForResult(intent, EDIT_REQUEST_CODE);
    }

    @Override
    public void onDeleteClicked(EntryRecord record) {
        mEntryAdapter.remove(record);
        mSnackbarUndoDelete.addUndoable(record);
    }

    @Override
    public void onDelete(Collection<EntryRecord> entryRecords) {
        for (EntryRecord entryRecord : entryRecords)
            AppVault.get().getManager().delete(entryRecord);
    }

    @Override
    public void onUndo(Collection<EntryRecord> entryRecords) {
        mEntryAdapter.addAll(entryRecords);
    }

    @Override
    public void onEntryExpanded(EntryRecord record, int pos) {
        mEntryRecyclerView.getLayoutManager().scrollToPosition(pos);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == EDIT_REQUEST_CODE) {
            if (resultCode == EditEntry.RESULT_ENTRY_CREATED)
                mEntryAdapter.add((EntryRecord) data.getParcelableExtra(EditEntry.ENTRY_RECORD_EXTRA_KEY));
            else if (resultCode == EditEntry.RESULT_ENTRY_DELETED)
                mEntryAdapter.remove((EntryRecord) data.getParcelableExtra(EditEntry.ENTRY_RECORD_EXTRA_KEY));
            else if (resultCode == EditEntry.RESULT_ENTRY_UPDATED) mEntryAdapter.collapseSelected();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mUserPreferences = UserPreferences.get(this);
        mSnackbarUndoDelete = new SnackbarUndoDelete<>(
                findViewById(R.id.main_activity_root),
                getString(R.string.main_entry_deleted),
                getString(R.string.main_entries_deleted),
                this
        );

        mEntryRecyclerView = (RecyclerView) findViewById(R.id.vault_recycler_view);
        mEntryAdapter = new EntryAdapter(this);
        mEntryAdapter.setSortOrder(mUserPreferences.getSortOrder());
        mEntryAdapter.setOnClickListener(this);
        mEntryAdapter.setOnEntryExpandedListener(this);
        mEntryRecyclerView.setAdapter(mEntryAdapter);
        mEntryRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(mEntryRecyclerView.getContext(), getResources().getConfiguration().orientation);
        mEntryRecyclerView.addItemDecoration(dividerItemDecoration);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        findViewById(R.id.fab).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, EditEntry.class);
                startActivityForResult(intent, EDIT_REQUEST_CODE);
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.nav_drawer_open, R.string.nav_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }

    @Override
    protected void onPause() {
        mSnackbarUndoDelete.forceDismissSnackbar();
        mEntryAdapter.collapseSelected();
        super.onPause();
    }

}
