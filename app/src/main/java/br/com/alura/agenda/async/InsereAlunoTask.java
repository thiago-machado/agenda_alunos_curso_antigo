package br.com.alura.agenda.async;

import android.os.AsyncTask;

import br.com.alura.agenda.converter.AlunoConverter;
import br.com.alura.agenda.modelo.Aluno;
import br.com.alura.agenda.repository.WebClient;

public class InsereAlunoTask extends AsyncTask {
    private final Aluno aluno;

    public InsereAlunoTask(Aluno aluno) {
        this.aluno = aluno;
    }

    @Override
    protected Object doInBackground(Object[] objects) {
        String json = new AlunoConverter().converteParaJSONCompleto(aluno);
        new WebClient().insere(json);

        return null;
    }
}
