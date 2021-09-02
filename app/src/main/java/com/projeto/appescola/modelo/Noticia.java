package com.projeto.appescola.modelo;

import com.google.firebase.database.DatabaseReference;
import com.projeto.appescola.conifg.ConfiguracaoFirebase;

import java.util.Date;

public class Noticia implements Comparable<Noticia>{
    private String id;
    private String descricao;
    private String imagem;
    private Date dataPublicacao;
    private Usuario autorPublicacao;

    public Noticia () {
        setDataPublicacao(new Date()); //ao criar uma noticia já setamos por default a data atual

        DatabaseReference database = ConfiguracaoFirebase.getFirebaseDatabase();
        DatabaseReference noticiasRef = database.child("noticias");

        this.setId(noticiasRef.push().getKey()); //cria identificador único no firebase e usa ele como id da noticia
    }

    public void salvar(){
        DatabaseReference database = ConfiguracaoFirebase.getFirebaseDatabase();
        DatabaseReference noticiasRef = database.child("noticias");

        noticiasRef.child(this.getId())
                   .setValue(this); //salva o objeto inteiro no firebase
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public String getImagem() {
        return imagem;
    }

    public void setImagem(String imagem) {
        this.imagem = imagem;
    }

    public Date getDataPublicacao() {
        return dataPublicacao;
    }

    public void setDataPublicacao(Date dataPublicacao) {
        this.dataPublicacao = dataPublicacao;
    }

    public Usuario getAutorPublicacao() {
        return autorPublicacao;
    }

    public void setAutorPublicacao(Usuario autorPublicacao) {
        this.autorPublicacao = autorPublicacao;
    }

    @Override
    public int compareTo(Noticia outraNoticia) { //método que permite a ordenação por data
        if (this.getDataPublicacao().compareTo(outraNoticia.getDataPublicacao()) == 0) {
            return 0;
        }
        if (this.getDataPublicacao().compareTo(outraNoticia.getDataPublicacao()) == -1) {
            return 1;
        }
        if (this.getDataPublicacao().compareTo(outraNoticia.getDataPublicacao()) == 1) {
            return -1;
        }
        return 0;
    }
}


