package br.com.alura.agenda.preferences;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Estamos persistindo os nossos dados com o SQLite, mas não faz sentido criarmos uma tabela para
 * guardar apenas uma informação, SQLite é mais para entidades.
 *
 * Felizmente o Android tem diversas opções de armazenamento, as Storage Options.
 * E a opção chamada de Shared Preferences se aplica a nossa situação de armazenar um dado primitivo.
 * O Shared Preferences é um framework feito para armazenar os tipos primitivos.
 *
 * Por meio do Shared Preferences podemos salvar valores primitivos de uma maneira objetiva,
 * isto é, sem precisar de uma estrutura de tabela e campos como fazemos no SQLite.
 *
 * Essa classe será resposável pelo Shared Preferences. Sua função é guardar informações de
 * Shared Preferences para aluno.
 */
public class AlunoPreferences {

    private static final String ALUNO_PREFERENCES = "br.com.alura.agenda.preferences.AlunoPreferences";
    private static final String CHAVE_DADO_VERSAO = "DADO_VERSAO";
    private Context context;

    public AlunoPreferences(Context context){
        this.context = context;
    }

    /**
     * A partir do variável preferences chamamos o método preferences.edit();.
     * Esse método retorna um objeto do tipo SharedPreferences.Editor que guardaremos em um
     * variável chamada editor. Esse objeto retornado é o próprio editor do Shared Preferences.
     *
     * Com o editor em mãos, podemos enviar para o Shared Preferences a informação que queremos.
     * Existe diversas opções para armazenar tipos primitivos, mas como queremos armazenar uma
     * string, então vamos utilizar o editor.putString() passando dois argumentos, uma chave e
     * um valor. Como chave, utilizaremos a string "versao_do_dado" e o valor é a própria
     * variável versao.
     *
     * @param versao
     */
    public void salvaVersao(String versao){
        SharedPreferences preferences = getSharedPreferences();
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(CHAVE_DADO_VERSAO, versao);
        editor.apply(); // salva a informação em SharedPreferences
    }

    /**
     * Para acessarmos o Shared Preferences na nova classe, precisamo de uma referência do Context.
     *
     * Precisamos passar como argumento uma string que vai ser o nome do conjunto do Shared Preferences
     * que queremos acessar, no caso como é apenas a classe AlunoPreferences que vai acessar esse
     * conjunto, então passaremos como chave "br.com.alura.agenda.preferences.AlunoPreferences".
     *
     * Qual o motivo de passar o contexto do pacote e o nome da classe como chave?
     * Caso alguma outra classe, ou biblioteca, que faz uso do Shared Preferences, isso evita que
     * elas acessem o nosso conjunto, já que ele vai estar usando outro tipo de chave.
     *
     * Agora precisamos colocar o módulo, que também é acessado por meio do context também.
     * Então vamos passar como segundo argumento o context.MODE_PRIVATE.
     * Escolhemos privado para apenas a nossa aplicação tenha acesso.
     *
     * @return
     */
    private SharedPreferences getSharedPreferences(){
        return context.getSharedPreferences(ALUNO_PREFERENCES, Context.MODE_PRIVATE);
    }

    public String getVersao(){
        SharedPreferences preferences = getSharedPreferences();
        return preferences.getString(CHAVE_DADO_VERSAO, "");
    }

    public boolean temVersao(){
        return !getVersao().isEmpty();
    }
}
