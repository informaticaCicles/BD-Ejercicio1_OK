package android.bd.app.bd_ejercicio1;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    final static int STATE_NONE = 0;
    final static int STATE_NEW = 1;
    final static int STATE_EDIT = 2;

    SimpleAdapter adapter = null;
    List<HashMap<String, String>> contactList = null;

    EditText etName = null;
    EditText etEmail = null;
    EditText etPhone = null;
    ImageButton ibSend = null;
    ImageButton ibCall = null;

    int state = STATE_NONE;
    int itemSelected = 0;

    SQLiteDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ContactosSQLiteHelper cslh = new ContactosSQLiteHelper(this, "DBContactos", null, 5);

        db = cslh.getWritableDatabase();

        etName = (EditText) findViewById(R.id.etName);
        etEmail = (EditText) findViewById(R.id.etEmail);
        etPhone = (EditText) findViewById(R.id.etPhone);
        ibSend = (ImageButton) findViewById(R.id.bSend);
        ibCall = (ImageButton) findViewById(R.id.bCall);

        disableEdition();

        ListView list = (ListView) findViewById(R.id.lvAgenda);
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                enableEdition();
                etName.setText(contactList.get(position).get("name"));
                etEmail.setText(contactList.get(position).get("email"));
                etPhone.setText(contactList.get(position).get("phone"));

                itemSelected = position;

                state = STATE_EDIT;
                supportInvalidateOptionsMenu();
            }
        });

        contactList = new ArrayList<HashMap<String, String>>();

        adapter = new SimpleAdapter(
                this,
                contactList,
                R.layout.list_item, new String[]{"name", "email", "phone"},
                new int[]{R.id.tvName, R.id.tvEmail, R.id.tvPhone}
        );
        list.setAdapter(adapter);

        cargarContactos();
    }


    public void cargarContactos(){
        Cursor c = db.rawQuery("SELECT nombre, email, phone FROM Contactos", null);

        if (c != null && c.moveToFirst()) {

            do {

                HashMap<String, String> contact = new HashMap<String, String>();
                contact.put("name", c.getString(0));
                contact.put("email", c.getString(1));
                contact.put("phone", c.getString(2));
                contactList.add(contact);

            } while (c.moveToNext());
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);

        switch(state) {
            case STATE_NONE:
                menu.findItem(R.id.action_new).setVisible(true);
                menu.findItem(R.id.action_save).setVisible(false);
                menu.findItem(R.id.action_clear).setVisible(false);
                menu.findItem(R.id.action_delete).setVisible(false);
                break;
            case STATE_NEW:
                menu.findItem(R.id.action_new).setVisible(false);
                menu.findItem(R.id.action_save).setVisible(true);
                menu.findItem(R.id.action_clear).setVisible(true);
                menu.findItem(R.id.action_delete).setVisible(false);
                break;
            case STATE_EDIT:
                menu.findItem(R.id.action_new).setVisible(false);
                menu.findItem(R.id.action_save).setVisible(true);
                menu.findItem(R.id.action_clear).setVisible(true);
                menu.findItem(R.id.action_delete).setVisible(true);
                break;
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch(item.getItemId()) {

            case R.id.action_new:
                enableEdition();
                clearEdition();
                state = STATE_NEW;
                supportInvalidateOptionsMenu();
                return true;

            case R.id.action_save:

                if (etName.getText().toString().equalsIgnoreCase("")) {
                    Toast.makeText(this, R.string.name_required, Toast.LENGTH_SHORT).show();
                }
                else {
                    HashMap<String, String> contact = new HashMap<String, String>();
                    contact.put("name", etName.getText().toString());
                    contact.put("email", etEmail.getText().toString());
                    contact.put("phone", etPhone.getText().toString());

                    if (state == STATE_NEW) {
                        contactList.add(contact);

                        ContentValues data = new ContentValues();
                        data.put("nombre", etName.getText().toString());
                        data.put("email", etEmail.getText().toString());
                        data.put("phone", etPhone.getText().toString());

                        db.insert("Contactos", null, data);
                    }
                    else if (state == STATE_EDIT) {
                        String currentName = contactList.get(itemSelected).get("name");
                        contactList.set(itemSelected, contact);

                        ContentValues data = new ContentValues();
                        data.put("nombre", etName.getText().toString());
                        data.put("email", etEmail.getText().toString());
                        data.put("phone", etPhone.getText().toString());

                        db.update("Contactos", data, "nombre = ?", new String[]{ currentName });
                    }
                    adapter.notifyDataSetChanged();

                    clearEdition();
                    disableEdition();
                    state = STATE_NONE;
                    supportInvalidateOptionsMenu();
                }
                return true;

            case R.id.action_clear:
                if (state == STATE_NEW) {
                    clearEdition();
                }
                else if (state == STATE_EDIT) {
                    clearEdition();
                    disableEdition();
                    state = STATE_NONE;
                    supportInvalidateOptionsMenu();
                }
                return true;

            case R.id.action_delete:
                db.delete("Contactos", "nombre = ?", new String[]{etName.getText().toString()});

                contactList.remove(itemSelected);
                adapter.notifyDataSetChanged();
                clearEdition();
                disableEdition();
                state = STATE_NONE;
                supportInvalidateOptionsMenu();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    private void enableEdition() {
        etName.setEnabled(true);
        etEmail.setEnabled(true);
        etPhone.setEnabled(true);
        ibSend.setEnabled(true);
        ibCall.setEnabled(true);
    }

    private void disableEdition() {
        etName.setEnabled(false);
        etEmail.setEnabled(false);
        etPhone.setEnabled(false);
        ibSend.setEnabled(false);
        ibCall.setEnabled(false);
    }

    private void clearEdition() {
        etName.setText("");
        etEmail.setText("");
        etPhone.setText("");
    }

    public void onClickButton(View v) {
        Intent intent = new Intent();
        switch (v.getId()) {
            case R.id.bSend:
                intent.setAction(Intent.ACTION_SEND);
                intent.setData(Uri.parse("mailto:"));
                intent.setType("text/plain");
                intent.putExtra(Intent.EXTRA_EMAIL, new String[]{etEmail.getText().toString()});
                startActivity(Intent.createChooser(intent, "Send email..."));
                break;
            case R.id.bCall:
                intent.setAction(Intent.ACTION_CALL);
                intent.setData(Uri.parse("tel:" + etPhone.getText().toString()));
                startActivity(intent);
                break;
        }
    }
}

