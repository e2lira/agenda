package mx.e2g.agenda;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
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

    /* Declaración para el número de control para el menu */
    private static final int EDIT = 0, DELETE = 1;

    private static final String TAG = "MainActivity";

    EditText txt_name, txt_phone, txt_email, txt_address;
    List<Contact> Contacts = new ArrayList<Contact>();
    ImageView imgViewContact;
    ListView contactListView;
    Uri imageUri = Uri.parse("android.resource://mx.e2g.agenda/" + R.drawable.default_user);
    DatabaseHandler dbHandler;
    int longClickedItemIdex;
    ArrayAdapter<Contact> contactAdapter;
    boolean editMode = false;
    Button btn_add;

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

        /*
            Inicio menu flotante a la lista
         */
        registerForContextMenu(contactListView);

        contactListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener(){

            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                longClickedItemIdex = position;
                Log.d(TAG, " contactListView --- AdapterView.OnItemLongClickListener,  position : " + String.valueOf(position));
                return false;
            }
        });

        /*
            Fin menu flotante
         */

        TabHost tabHost = (TabHost) findViewById(R.id.tabHost);

        tabHost.setup();

        TabHost.TabSpec tabSpec = tabHost.newTabSpec("creator");
        tabSpec.setContent(R.id.tabCrear);
        tabSpec.setIndicator("Crear");
        tabHost.addTab(tabSpec);

        tabSpec = tabHost.newTabSpec("list");
        tabSpec.setContent(R.id.tabList);
        tabSpec.setIndicator("Lista");
        tabHost.addTab(tabSpec);

         btn_add = (Button) findViewById(R.id.btnAdd);
        // Add an action listener
        btn_add.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Add growl message text

                if (editMode == false) {
                    Contact contact = new Contact(dbHandler.getContactsCount(), txt_name.getText().toString(), txt_phone.getText().toString(), txt_email.getText().toString(), txt_address.getText().toString(), imageUri);
                    if (dbHandler.existContactByName(contact) == true) {
                        Toast.makeText(getApplicationContext(), "No se guardo, el contacto '" + txt_name.getText().toString() + "' ya existe!!!", Toast.LENGTH_SHORT).show();
                    } else {
                        dbHandler.createContact(contact);
                        Contacts.add(contact);
                /* Contacts.add(new Contact(0,txt_name.getText().toString(),txt_phone.getText().toString(),txt_email.getText().toString(),txt_address.getText().toString(), imageUri)); */
               /* Ya no es necesario porque el Adapter se encarga de llenar el array
                *populateList();
                */
                        // Tenemos que notificar que hubo un cambio en la lista
                        contactAdapter.notifyDataSetChanged();
                        cleanText();
                        Toast.makeText(getApplicationContext(), "El contacto '" + contact.getName().toString() + "' se creo con éxito", Toast.LENGTH_SHORT).show();
                    }
                } else {

                    Contact contact = Contacts.get(longClickedItemIdex);
                    Log.d(TAG, " Update name: " + contact.getName());

                    contact.setName(txt_name.getText().toString());
                    contact.setPhone(txt_phone.getText().toString());
                    contact.setEmail(txt_email.getText().toString());
                    contact.setAddress(txt_address.getText().toString());
                    contact.setImgUri(imageUri);
                    dbHandler.updateContact(contact);
                    /* Me falta acutalizar el list Contacts */
                    Contacts.remove(longClickedItemIdex);
                    Contacts.add(contact);
                    contactAdapter.notifyDataSetChanged();
                    cleanText();
                    Toast.makeText(getApplicationContext(), "El contacto '" + contact.getName().toString() + "' se modifico con éxito", Toast.LENGTH_SHORT).show();
                    editMode = false;
                    btn_add.setText("CREAR CONTACTO");
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

    /*
        Metodo para la creación del menú
     */
    public void onCreateContextMenu(ContextMenu menu, View view, ContextMenu.ContextMenuInfo menuInfo){
        super.onCreateContextMenu(menu,view,menuInfo);

        menu.setHeaderIcon(R.drawable.pencil_icon);
        menu.setHeaderTitle("Contact Options");
        menu.add(menu.NONE, EDIT, menu.NONE, "Editar contacto");
        menu.add(menu.NONE, DELETE, menu.NONE, "Borrar contacto");

    }

    public boolean onContextItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case EDIT:
                // TODO: Implement edit contact
                Contact contact = Contacts.get(longClickedItemIdex);
                txt_name.setText(contact.getName());
                txt_phone.setText(contact.getPhone());
                txt_email.setText(contact.getEmail());
                txt_address.setText(contact.getAddress());
                txt_name.setFocusable(true);
                btn_add.setText("ACTUALIZAR CONTACTO");
                editMode = true;
                break;
            case DELETE:

                dbHandler.deleteContact(Contacts.get(longClickedItemIdex));
                Contacts.remove(longClickedItemIdex);
                contactAdapter.notifyDataSetChanged();
                Toast.makeText(getApplicationContext(), "El contacto se elimino con éxito", Toast.LENGTH_SHORT).show();
                break;
        }

        return super.onContextItemSelected(item);
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
        contactAdapter = new  ContactListAdapter();
        contactListView.setAdapter(contactAdapter);
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
        imgViewContact.setImageResource(R.drawable.default_user);
        imageUri = Uri.parse("android.resource://mx.e2g.agenda/" + R.drawable.default_user);
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
