package com.soumani.contactapplication.activity

import android.Manifest
import android.app.Dialog
import android.content.ContentProviderOperation
import android.content.ContentResolver
import android.content.Context


import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

import android.provider.ContactsContract


import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.livinglifetechway.quickpermissions_kotlin.runWithPermissions
import com.livinglifetechway.quickpermissions_kotlin.util.QuickPermissionsOptions
import com.soumani.contactapplication.R
import com.soumani.contactapplication.adapter.Adapter_RecyclerView
import com.soumani.contactapplication.model.Contacts
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {

    private lateinit var adapter: Adapter_RecyclerView
    private var contactList: MutableList<Contacts> = ArrayList()
    private var context: Context?=null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        context = this@MainActivity
        val recycler_view: RecyclerView = findViewById(R.id.rv_contacts)
        val itemDecoration = DividerItemDecoration(this, DividerItemDecoration.VERTICAL)
        recycler_view?.layoutManager = LinearLayoutManager(this)
        recycler_view?.addItemDecoration(itemDecoration)


        adapter = Adapter_RecyclerView(contactList)
        recycler_view.adapter = adapter
        methodWithPermissions()
        showHideProgressBar()
        recycler_view.visibility= View.VISIBLE
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.add_menu,menu)
        return true
    }

    fun methodWithPermissions() = runWithPermissions(Manifest.permission.READ_CONTACTS,Manifest.permission.WRITE_CONTACTS) {
        Toast.makeText(this, "Read and Write Contacts permissions granted", Toast.LENGTH_LONG).show();

        getContact()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId){
            R.id.add_contact_menu ->{
                addDialog()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun saveContact(name:String,phoneno:String){
      var ops:  ArrayList<ContentProviderOperation> = ArrayList()
      ops.add(ContentProviderOperation.newInsert(
          ContactsContract.RawContacts.CONTENT_URI)
          .withValue(ContactsContract.RawContacts.ACCOUNT_TYPE,null)
          .withValue(ContactsContract.RawContacts.ACCOUNT_NAME,null)
          .build()
      )
        ops.add(ContentProviderOperation.newInsert(
            ContactsContract.Data.CONTENT_URI)
            .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID,0)
            .withValue(ContactsContract.Data.MIMETYPE,ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
            .withValue(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME,name)
            .build()
        )
        ops.add(ContentProviderOperation.newInsert(
            ContactsContract.Data.CONTENT_URI)
            .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID,0)
            .withValue(ContactsContract.Data.MIMETYPE,ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
            .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER,phoneno)
            .withValue(ContactsContract.CommonDataKinds.Phone.TYPE,
            ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE)
            .build()
        )

        try{
            getContentResolver().applyBatch(ContactsContract.AUTHORITY,ops)
        }catch (e:Exception){

        }

    }

    private fun getContact() {
       // showProgressBar()
        val resolver: ContentResolver = contentResolver
        val cursor = resolver.query(
            ContactsContract.Contacts.CONTENT_URI,
            null,
            null,
            null,
            ContactsContract.Contacts.DISPLAY_NAME)

        if (cursor != null) {
            if ( cursor.count > 0) {
                while (cursor.moveToNext()) {

                    val id = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID))
                    val name =
                        cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME))
                    val phoneNumber =
                        cursor.getString((cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))).toInt()
                    var contact = Contacts(name, phoneNumber.toString())

                    if (name != null && phoneNumber > 0) {
                        contact.contactName = name
                        val cursorPhone = contentResolver.query(
                            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                            null,
                            ContactsContract.CommonDataKinds.Phone.CONTACT_ID + "=?",
                            arrayOf(id),
                            null
                        )

                        if (cursorPhone != null) {
                            if (cursorPhone.count > 0) {
                                while (cursorPhone.moveToNext()) {
                                    val phoneNumValue = cursorPhone.getString(
                                        cursorPhone.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                                    )

                                    contact.contactNo = phoneNumValue

                                }
                            }
                        }
                        if (cursorPhone != null) {
                            cursorPhone.close()
                        }

                        Log.e(contact.contactName, contact.contactNo)
                        contactList.add(contact)
                    }

                }
            }
        }
        adapter?.notifyDataSetChanged()

        if (cursor != null) {
            cursor.close()
        }
            }


        fun showHideProgressBar(){
            val progressBar = findViewById(R.id.progress_Bar) as ProgressBar
            progressBar.visibility = if(progressBar.visibility == View.VISIBLE){
                View.GONE
            } else{
                View.VISIBLE
            }
        }

        fun addDialog(){
            val customAddDialog = context?.let { Dialog(it) }
            if (customAddDialog != null) {
                customAddDialog.setContentView(R.layout.dialog_add_contact)

                customAddDialog.window?.setLayout(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
                customAddDialog.setCancelable(false)
                val addName = customAddDialog.findViewById(R.id.et_name) as EditText
                val addPhone = customAddDialog.findViewById(R.id.et_phone) as EditText
                val addBtn = customAddDialog.findViewById(R.id.btn_add) as Button
                val cancelBtn = customAddDialog.findViewById(R.id.btn_cancel) as Button
                addBtn.isClickable= false

                cancelBtn.setOnClickListener {
                    customAddDialog.dismiss()
                }

                addName.text.isNotEmpty().apply {
                    addPhone.text.isNotEmpty().apply {
                        addBtn.isEnabled = true
                        addBtn.isClickable=true
                        addBtn.setOnClickListener {
                            val name = addName.text.toString()
                            val phone = addPhone.text.toString()
                            var contact = Contacts(name, phone)
                            contact.contactName = name
                            contact.contactNo = phone
                            contactList.add(contact)
                            adapter?.notifyDataSetChanged()
                            customAddDialog.dismiss()
                            saveContact(name,phone)

                        }

                        Toast.makeText(context, "Fill the phone no", Toast.LENGTH_SHORT).show()
                    }
                    Toast.makeText(context, "Fill the name", Toast.LENGTH_SHORT).show()
                }
                customAddDialog.show()
            }

        }

}

