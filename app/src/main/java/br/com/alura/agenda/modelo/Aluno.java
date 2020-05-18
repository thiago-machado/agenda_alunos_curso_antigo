package br.com.alura.agenda.modelo;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

/*
Ignora os campos que são desconhecidos (não constam na classe)
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Aluno implements Serializable {

    /**
     * Na nossa aplicação Android temos um atributo id, mas no servidor esse atributo se chama idCliente.
     * Como podemos resolver diferença?
     *
     * O jackson tem uma maneira muito simples de resolver esse problema, basta que utilizemos a
     * anotação @JsonProperty fornecendo o nome que será utilizado como chave do JSON para aquele valor.
     */
    //@JsonProperty("idCliente")
    private String id; // AGORA É UM UUID
    private String nome;
    private String endereco;
    private String telefone;
    private String site;
    private Double nota;
    private String caminhoFoto;

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

    public String getEndereco() {
        return endereco;
    }

    public void setEndereco(String endereco) {
        this.endereco = endereco;
    }

    public String getTelefone() {
        return telefone;
    }

    public void setTelefone(String telefone) {
        this.telefone = telefone;
    }

    public String getSite() {
        return site;
    }

    public void setSite(String site) {
        this.site = site;
    }

    public Double getNota() {
        return nota;
    }

    public void setNota(Double nota) {
        this.nota = nota;
    }

    public String getCaminhoFoto() {
        return caminhoFoto;
    }

    public void setCaminhoFoto(String caminhoFoto) {
        this.caminhoFoto = caminhoFoto;
    }

    @Override
    public String toString() {
        return getId() + " - " + getNome();
    }
}
