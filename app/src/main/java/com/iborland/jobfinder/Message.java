package com.iborland.jobfinder;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

/**
 * Created by iBorland on 12.04.2016.
 */

/*

Класс сообщений. Сделан для удобства работы с сообщениями.

 */
class Message implements Comparable, Parcelable{

    int id, sender_id, recipient_id;
    long date;
    String sender_login, recipient_login, text;

    Message(boolean t){}; // костыль ;)

    public int compareTo(Object obj)
    {
        Message tmp = (Message)obj;
        if(this.id < tmp.id) return 1;
        else if(this.id > tmp.id) return -1;
        return 0;
    }

    public int describeContents() {
        return 0;
    }

    // упаковываем объект в Parcel
    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeInt(id);
        parcel.writeInt(sender_id);
        parcel.writeInt(recipient_id);
        parcel.writeLong(date);
        parcel.writeString(sender_login);
        parcel.writeString(recipient_login);
        parcel.writeString(text);

    }

    public static final Parcelable.Creator<Message> CREATOR = new Parcelable.Creator<Message>() {
        // распаковываем объект из Parcel
        public Message createFromParcel(Parcel in) {
            return new Message(in);
        }

        public Message[] newArray(int size) {
            return new Message[size];
        }
    };

    // конструктор, считывающий данные из Parcel
    private Message(Parcel parcel) {
        id = parcel.readInt();
        sender_id = parcel.readInt();
        recipient_id = parcel.readInt();
        date = parcel.readLong();
        sender_login = parcel.readString();
        recipient_login = parcel.readString();
        text = parcel.readString();
    }
}