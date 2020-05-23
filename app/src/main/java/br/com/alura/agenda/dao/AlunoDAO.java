package br.com.alura.agenda.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import br.com.alura.agenda.modelo.Aluno;

/**
 * Embora tenhamos indicado a versão do banco, o método onUpgrade só é acionado
 * apenas quando mudamos de versão do banco com a qual já existe no celular.
 * <p>
 * Em outras palavras, se o usuário instalar a nossa App pela primeira vez, ou então,
 * desinstalar e instalar novamente, ele terá a tabela que está escrita no onCreate() do
 * SQLite.
 * <p>
 * Sendo assim, precisamos também modificar a tabela do onCreate() para que contenha os
 * mesmos dados das nossas últimas atualizações do banco.
 */
public class AlunoDAO extends SQLiteOpenHelper {

    public AlunoDAO(Context context) {
        super(context, "Agenda", null, 4);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String sql = "CREATE TABLE Alunos (id CHAR(36) PRIMARY KEY, nome TEXT NOT NULL, endereco TEXT, telefone TEXT, site TEXT, nota REAL, caminhoFoto TEXT)";
        db.execSQL(sql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        switch (oldVersion) {
            case 1:
                tratandoMigration1(db);
            case 2:
                tratandoMigration2(db);
            case 3:
                tratandoMigration3(db);
        }

    }

    private void tratandoMigration3(SQLiteDatabase db) {
        String buscaAlunos = "SELECT * FROM Alunos";
        Cursor cursor = db.rawQuery(buscaAlunos, null);
        List<Aluno> alunos = iterarListaAlunos(cursor);
        cursor.close();

        String alterarAlunoID = "UPDATE Alunos SET id=? WHERE id=?";
        for (Aluno aluno : alunos) {
            db.execSQL(alterarAlunoID, new String[]{geraUUID(), aluno.getId()});
        }
    }

    /**
     * A geração do UUID com java é bastante simples por que já existe uma classe que facilita essa
     * criação. O mais legal é que ela já fornece o método necessário para que o UUID seja randômico.
     *
     * @return
     */
    private String geraUUID() {
        return UUID.randomUUID().toString();
    }

    private void tratandoMigration2(SQLiteDatabase db) {
        // CRIANDO TABELA Alunos_novo
        String criandoTabelaNova = "CREATE TABLE Alunos_novo " +
                "(id CHAR(36) PRIMARY KEY, " +
                "nome TEXT NOT NULL, " +
                "endereco TEXT, " +
                "telefone TEXT, " +
                "site TEXT, " +
                "nota REAL, " +
                "caminhoFoto TEXT);";
        db.execSQL(criandoTabelaNova);

        // INSERINDO EM Alunos_novo TUDO O QUE ESTÁ EM Alunos
        String inserindoAlunosNaTabelaNova = "INSERT INTO Alunos_novo " +
                "(id, nome, endereco, telefone, site, nota, caminhoFoto) " +
                "SELECT id, nome, endereco, telefone, site, nota, caminhoFoto FROM Alunos";
        db.execSQL(inserindoAlunosNaTabelaNova);

        // REMOVENDO TABELA Alunos
        String removendoTabelaAntiga = "DROP TABLE Alunos";
        db.execSQL(removendoTabelaAntiga);

        // RENOMEANDO TABELA Alunos_novo PARA Alunos
        String alterandoNomeDaTabelaNova = "ALTER TABLE Alunos_novo RENAME TO Alunos";
        db.execSQL(alterandoNomeDaTabelaNova);
    }

    private void tratandoMigration1(SQLiteDatabase db) {
        String sql = "ALTER TABLE Alunos ADD COLUMN caminhoFoto TEXT";
        db.execSQL(sql);
    }

    public void insere(Aluno aluno) {
        SQLiteDatabase db = getWritableDatabase();

        insereUUIDSeNecessario(aluno);
        ContentValues dados = pegaDadosDoAluno(aluno);

        db.insert("Alunos", null, dados);
    }

    /**
     * UUID == NULL = aluno criado pelo APP
     * UUID != NULL = aluno que veio do servidor
     *
     * @param aluno
     */
    private void insereUUIDSeNecessario(Aluno aluno) {
        if (aluno.getId() == null) {
            aluno.setId(geraUUID());
        }
    }

    @NonNull
    private ContentValues pegaDadosDoAluno(Aluno aluno) {
        ContentValues dados = new ContentValues();
        dados.put("id", aluno.getId());
        dados.put("nome", aluno.getNome());
        dados.put("endereco", aluno.getEndereco());
        dados.put("telefone", aluno.getTelefone());
        dados.put("site", aluno.getSite());
        dados.put("nota", aluno.getNota());
        dados.put("caminhoFoto", aluno.getCaminhoFoto());
        return dados;
    }

    public List<Aluno> buscaAlunos() {
        String sql = "SELECT * from Alunos;";
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery(sql, null);
        List<Aluno> alunos = iterarListaAlunos(c);

        for (Aluno aluno : alunos) {
            Log.i("uuid_aluno", "id: " + aluno.getId());
        }
        c.close();
        return alunos;
    }

    private List<Aluno> iterarListaAlunos(Cursor c) {
        List<Aluno> alunos = new ArrayList<>();
        while (c.moveToNext()) {
            Aluno aluno = new Aluno();
            aluno.setId(c.getString(c.getColumnIndex("id")));
            aluno.setNome(c.getString(c.getColumnIndex("nome")));
            aluno.setEndereco(c.getString(c.getColumnIndex("endereco")));
            aluno.setTelefone(c.getString(c.getColumnIndex("telefone")));
            aluno.setSite(c.getString(c.getColumnIndex("site")));
            aluno.setNota(c.getDouble(c.getColumnIndex("nota")));
            aluno.setCaminhoFoto(c.getString(c.getColumnIndex("caminhoFoto")));
            alunos.add(aluno);
        }
        return alunos;
    }

    public void deleta(Aluno aluno) {
        SQLiteDatabase db = getWritableDatabase();
        String[] params = {aluno.getId()};
        db.delete("Alunos", "id = ?", params);
    }

    public void altera(Aluno aluno) {
        SQLiteDatabase db = getWritableDatabase();

        ContentValues dados = pegaDadosDoAluno(aluno);

        String[] params = {aluno.getId().toString()};
        db.update("Alunos", dados, "id = ?", params);
    }

    public boolean isAluno(String telefone) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM Alunos WHERE telefone = ?", new String[]{telefone});
        int resultados = c.getCount();
        c.close();
        return resultados > 0;
    }

    public void sincroniza(List<Aluno> alunos) {
        for (Aluno aluno : alunos) {
            if (existe(aluno)) {
                if (aluno.estaDesativado()) {
                    deleta(aluno);
                } else {
                    altera(aluno);
                }
            } else if (!aluno.estaDesativado()) { // Condição que evita cadastrar os alunos recém removidos do app
                insere(aluno);
            }
        }
    }

    private boolean existe(Aluno aluno) {
        SQLiteDatabase db = getReadableDatabase();
        String existe = "SELECT id FROM Alunos WHERE id = ?";
        Cursor cursor = db.rawQuery(existe, new String[]{aluno.getId()});
        int quantidade = cursor.getCount();
        return quantidade > 0;
    }
}
