package com.projeto.appescola.modelo;

import android.support.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Exclude;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FileDownloadTask;
import com.projeto.appescola.activity.ConfiguracoesActivity;
import com.projeto.appescola.conifg.ConfiguracaoFirebase;
import com.projeto.appescola.helper.UsuarioFirebase;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class Usuario implements Serializable {
    //preciamos implementar serializable para poder passar o objeto entre activits
    private String id;
    private String nome;
    private String email;
    private String senha;
    private String foto;

    private static Usuario user;

    public Usuario() {
    }

    public void salvar(){
        DatabaseReference fireBaseRef = ConfiguracaoFirebase.getFirebaseDatabase();
        DatabaseReference usuario = fireBaseRef.child("usuarios").child(getId());
        usuario.setValue(this); // salva o objeto no banco
    }

    public void atualizar(){
        String identificadorUsuario   = UsuarioFirebase.getIdentificadorUsuario();
        DatabaseReference database    = ConfiguracaoFirebase.getFirebaseDatabase();
        DatabaseReference usuariosRef = database.child("usuarios")
                .child(identificadorUsuario);

        usuariosRef.updateChildren(converterParaMap());

    }

    public static Usuario recuperarUsuário(String id){
        DatabaseReference database    = ConfiguracaoFirebase.getFirebaseDatabase();
        DatabaseReference usuariosRef = database.child("usuarios")
                .child(id);
        usuariosRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                user = dataSnapshot.getValue(Usuario.class);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        return user;

    }

    @Exclude
    public Map<String, Object> converterParaMap(){
        HashMap<String, Object> usuarioMap = new HashMap<>();
        usuarioMap.put("email", getEmail());
        usuarioMap.put("nome", getNome());
        usuarioMap.put("foto", getFoto());

        return usuarioMap;
    }

    @Exclude
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @Exclude
    public String getSenha() {
        return senha;
    }

    public void setSenha(String senha) {
        this.senha = senha;
    }

    public String getFoto() {
        return foto;
    }

    public void setFoto(String foto) {
        this.foto = foto;
    }
}
