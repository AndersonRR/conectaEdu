package com.projeto.appescola.modelo;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Exclude;
import com.projeto.appescola.conifg.ConfiguracaoFirebase;
import com.projeto.appescola.helper.UsuarioFirebase;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class Conversa implements Comparable<Conversa> {
    private String idRemetente;
    private String idDestinatario;
    private String ultimaMensagem;
    private Usuario usuarioExibicao; // usuário que aparecerá no remetente
    private String isGroup;
    private Grupo grupo;
    private int mensagensNaoLidas;
    private Date ultimaData;

    public Conversa () {
        this.setIsGroup("false");
        this.setMensagensNaoLidas(0);
        setUltimaData(new Date());
    }

    @Override
    public int compareTo(Conversa outraConversa) { //método responsável por gerenciar ordenação por data
        if (this.getUltimaData().compareTo(outraConversa.getUltimaData()) == 0) {
            return 0;
        }
        if (this.getUltimaData().compareTo(outraConversa.getUltimaData()) == -1) {
            return 1;
        }
        if (this.getUltimaData().compareTo(outraConversa.getUltimaData()) == 1) {
            return -1;
        }
        return 0;
    }

    public void salvar(){
        setUltimaData(new Date());
        DatabaseReference database = ConfiguracaoFirebase.getFirebaseDatabase();
        DatabaseReference conversaRef = database.child("conversas");

        conversaRef.child(this.getIdRemetente())
                .child(this.getIdDestinatario())
                .setValue(this);
    }

    public void atualizar(){
        setUltimaData(new Date());
        String identificadorUsuario   = UsuarioFirebase.getIdentificadorUsuario();
        DatabaseReference database    = ConfiguracaoFirebase.getFirebaseDatabase();
        DatabaseReference conversaRef = database.child("conversas")
                .child(identificadorUsuario)
                .child(getIdDestinatario());

        conversaRef.updateChildren(converterParaMap());

    }

    @Exclude
    public Map<String, Object> converterParaMap(){
        HashMap<String, Object> usuarioMap = new HashMap<>();
        usuarioMap.put("idRemetente", getIdRemetente());
        usuarioMap.put("idDestinatario", getIdDestinatario());
        usuarioMap.put("ultimaMensagem", getUltimaMensagem());
        usuarioMap.put("usuarioExibicao", getUsuarioExibicao());

        return usuarioMap;
    }

    public String getIdRemetente() {
        return idRemetente;
    }

    public void setIdRemetente(String idRemetente) {
        this.idRemetente = idRemetente;
    }

    public String getIdDestinatario() {
        return idDestinatario;
    }

    public void setIdDestinatario(String idDestinatario) {
        this.idDestinatario = idDestinatario;
    }

    public String getUltimaMensagem() {
        return ultimaMensagem;
    }

    public void setUltimaMensagem(String ultimaMensagem) {
        this.ultimaMensagem = ultimaMensagem;
    }

    public Usuario getUsuarioExibicao() {
        return usuarioExibicao;
    }

    public void setUsuarioExibicao(Usuario usuarioExibicao) {
        this.usuarioExibicao = usuarioExibicao;
    }

    public String getIsGroup() {
        return isGroup;
    }

    public void setIsGroup(String isGroup) {
        this.isGroup = isGroup;
    }

    public Grupo getGrupo() {
        return grupo;
    }

    public void setGrupo(Grupo grupo) {
        this.grupo = grupo;
    }

    public int getMensagensNaoLidas() {
        return mensagensNaoLidas;
    }

    public void setMensagensNaoLidas(int mensagensNaoLidas) {
        this.mensagensNaoLidas = mensagensNaoLidas;
    }

    public Date getUltimaData() {
        return ultimaData;
    }

    public void setUltimaData(Date ultimaData) {
        this.ultimaData = ultimaData;
    }
}
