package mx.e2g.agenda;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends ActionBarActivity {

    EditText txt_name, txt_phone, txt_email, txt_address;
    List<Contact> Contacts = new ArrayList<Contact>();
    ImageView imgViewContact;
    ListView contactListView;
    Uri imageUri = Uri.parse("android.resource://mx.e2g.agenda/" + R.drawable.default_user);
    DatabaseHandler dbHandler;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        txt_name = (EditText) findViewById(R.id.txtName);
        txt_phone = (EditText) findViewById(R.id.txtPhone);
        txt_email = (EditText) findViewById(R.id.txtEmail);
        txt_address = (EditText) findViewById(R.id.txtAddress);
        contactListView = (ListView) findViewById(R.id.contactListView);
        imgViewContact = (ImageView) findViewById(R.id.imgViewContact);
        dbHandler = new DatabaseHandler(getApplicationContext());

        TabHost tabHost = (TabHost) findViewById(R.id.tabHost);

        tabHost.setup();

        TabHost.TabSpec tabSpec = tabHost.newTabSpec("creator");
        tabSpec.setContent(R.id.tabCrear);
        tabSpec.setIndicator("Creator");
        tabHost.addTab(tabSpec);

        tabSpec = tabHost.newTabSpec("list");
        tabSpec.setContent(R.id.tabList);
        tabSpec.setIndicator("List");
        tabHost.addTab(tabSpec);

        final Button btn_add = (Button) findViewById(R.id.btnAdd);
        // Add an action listener
        btn_add.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Add growl message text

                Contact contact = new Contact(dbHandler.getContactsCount(), txt_name.getText().toString(), txt_phone.getText().toString(), txt_email.getText().toString(), txt_address.getText().toString(), imageUri);
                if (dbHandler.existContactById(contact) == true){
                    Toast.makeText(getApplicationContext(),"No se guardo, el contacto '" + txt_name.getText().toString() + "' ya existe!!!", Toast.LENGTH_SHORT).show();
                } else {
                    dbHandler.createContact(contact);
                    Contacts.add(contact);
                /* Contacts.add(new Contact(0,txt_name.getText().toString(),txt_phone.getText().toString(),txt_email.getText().toString(),txt_address.getText().toString(), imageUri)); */
               /* Ya no es necesario porque el Adapter se encarga de llenar el array
                *populateList();
                */
                    cleanText();
                    Toast.makeText(getApplicationContext(), "El contacto '" + txt_name.getText().toString() + "' se creo con éxito", Toast.LENGTH_SHORT).show();
                }
            }
        });

        txt_name.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                btn_add.setEnabled(!txt_name.getText().toString().trim().isEmpty());

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        /* ADD action listener  */
        imgViewContact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /* An Intent is a messaging object you can use to request an action from another app component. */
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent,"Selecciona una imagen"),1);
            }
        });

        /*
        List<Contact> existContacts = dbHandler.getAllContacts();
        int contactCount = dbHandler.getContactsCount();

        for (int i=0; i < contactCount; i++){
            Contacts.add(existContacts.get(i));
        }
        * la instruccion siguiente sustituye todas las anteriores
        */

        if (dbHandler.getContactsCount() > 0)
        Contacts.addAll(dbHandler.getAllContacts());

        populateList();

    }

    public void onActivityResult(int reqCode, int resCode, Intent data){
        if (resCode == RESULT_OK){
            if (reqCode == 1) {
                imageUri = data.getData();
                imgViewContact.setImageURI(data.getData());
            }
        }
    }

    private void populateList(){
        ArrayAdapter<Contact> adapter = new  ContactListAdapter();
        contactListView.setAdapter(adapter);
        /*
        * setListViewHeightBasedOnItems(contactListView);
        * Esto no es necesario si se pone la opcion
        * en el parametro layout.height=fill_parent
        */
    }

    private void cleanText(){
        txt_name.setText("");
        txt_phone.setText("");
        txt_email.setText("");
        txt_address.setText("");
        imgViewContact.setImageURI(imageUri);
        imgViewContact.refreshDrawableState();
        txt_name.setFocusable(true);
    }

    private class ContactListAdapter extends ArrayAdapter<Contact> {
        public ContactListAdapter(){
            //ojo, R.layout para hacer referencia a las ventanas o paneles
            super(MainActivity.this, R.layout.listview_item, Contacts);
        }

        @Override
        public View getView(int position, View view, ViewGroup parent){

            if (view == null)
                view = getLayoutInflater().inflate(R.layout.listview_item, parent, false);

            Contact currentContact = Contacts.get(position);

            TextView name = (TextView) view.findViewById(R.id.contactName);
            TextView phone = (TextView) view.findViewById(R.id.contactPhone);
            TextView email = (TextView) view.findViewById(R.id.contactEmail);
            TextView address = (TextView) view.findViewById(R.id.contactAddress);
            ImageView imgViewContact = (ImageView) view.findViewById(R.id.ivContactImg);

            name.setText(currentContact.getName());
            phone.setText(currentContact.getPhone());
            email.setText(currentContact.getEmail());
            address.setText(currentContact.getAddress());
            imgViewContact.setImageURI(currentContact.getImgUri());

            return view;
        }

    }

    /**
     * Sets ListView height dynamically based on the height of the items.
     *
     * @param listView to be resized
     * @return true if the listView is successfully resized, false otherwise
     */
    public static boolean setListViewHeightBasedOnItems(ListView listView) {

        ListAdapter listAdapter = listView.getAdapter();
        if (listAdapter != null) {

            int numberOfItems = listAdapter.getCount();

            // Get total height of all items.
            int totalItemsHeight = 0;
            for (int itemPos = 0; itemPos < numberOfItems; itemPos++) {
                View item = listAdapter.getView(itemPos, null, listView);
                item.measure(0, 0);
                totalItemsHeight += item.getMeasuredHeight();
            }

            // Get total height of all item dividers.
            int totalDividersHeight = listView.getDividerHeight() *
                    (numberOfItems - 1);

            // Set list height.
            ViewGroup.LayoutParams params = listView.getLayoutParams();
            params.height = totalItemsHeight + totalDividersHeight;
            listView.setLayoutParams(params);
            listView.requestLayout();

            return true;

        } else {
            return false;
        }

    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
