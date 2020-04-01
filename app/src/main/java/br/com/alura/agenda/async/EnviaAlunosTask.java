package br.com.alura.agenda.async;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import java.util.List;

import br.com.alura.agenda.converter.AlunoConverter;
import br.com.alura.agenda.dao.AlunoDAO;
import br.com.alura.agenda.modelo.Aluno;
import br.com.alura.agenda.repository.WebClient;

public class EnviaAlunosTask extends AsyncTask<Void, String, String> {

    private final Context context;

    private ProgressDialog dialog;

    public EnviaAlunosTask(Context context) {
        this.context = context;
    }

    @Override
    protected void onPreExecute() {
        // params = context, titulo, mensagem, tempo_indeterminado?, usario_pode_cancelar?
        dialog = ProgressDialog.show(context, "Aguarde", "Enviando alunos...", true, true);
        dialog.show();
    }

    @Override
    protected String doInBackground(Void... voids) {
        AlunoDAO dao = new AlunoDAO(context);
        List<Aluno> alunos = dao.buscaAlunos();
        dao.close();

        AlunoConverter conversor = new AlunoConverter();
        String json = conversor.converter(alunos);

        Log.i("jsonenvio", json);
        WebClient client = new WebClient();
        return client.post(json);
    }

    @Override
    protected void onPostExecute(String resposta) {
        //Log.i("jsonenvio", resposta);
        dialog.dismiss();
        Toast.makeText(context, resposta, Toast.LENGTH_LONG).show();
    }
}
