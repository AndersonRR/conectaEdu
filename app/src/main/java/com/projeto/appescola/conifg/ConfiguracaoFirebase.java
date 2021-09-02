package com.projeto.appescola.conifg;

import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class ConfiguracaoFirebase {

    private static DatabaseReference database;
    private static FirebaseAuth auth;
    private static StorageReference storage;   /*FirebaseStorage*/

    //retornar a instância do FirebaseDatabase
    public static DatabaseReference getFirebaseDatabase() {
        if (database == null) {
            //FirebaseDatabase.getInstance().setPersistenceEnabled(true);
            Log.d("Success", "Data Persistence Enabled");
            database = FirebaseDatabase.getInstance().getReference();
        }
        return database;
    }

    //retornar a instancia do FirebaseAuth (autenticador)
    public static FirebaseAuth getFirebaseAutenticacao(){
        if (auth == null){
            auth = FirebaseAuth.getInstance();
        }
        return auth;
    }

    public static StorageReference getFirebaseStorage(){
        if (storage == null){
            storage = FirebaseStorage.getInstance().getReference();
        }
        return storage;
    }
}
