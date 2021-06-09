package com.soumani.contactapplication.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.soumani.contactapplication.R
import com.soumani.contactapplication.model.Contacts

class Adapter_RecyclerView(private val contactList:MutableList<Contacts>): RecyclerView.Adapter<Adapter_RecyclerView.ContactViewHolder>(){


    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): Adapter_RecyclerView.ContactViewHolder {
       val view = LayoutInflater.from(parent.context).inflate(R.layout.child_recyclerview,parent,false)
        return ContactViewHolder(view)
    }

    override fun getItemCount(): Int {
       return contactList.size
    }

    override fun onBindViewHolder(holder: Adapter_RecyclerView.ContactViewHolder, position: Int) {
        val contact = contactList[position]
        holder.contactName!!.text = contact.contactName
        holder.contactNo!!.text = contact.contactNo.toString()
    }

    class ContactViewHolder(itemView: View):RecyclerView.ViewHolder(itemView) {
        var contactName:TextView? = itemView.findViewById(R.id.tv_contactName) as TextView
        var contactNo:TextView? = itemView.findViewById(R.id.tv_contactNo) as TextView

    }

}