package br.com.alura.agenda.sync;

import android.content.Context;
import android.util.Log;

import org.greenrobot.eventbus.EventBus;

import br.com.alura.agenda.dao.AlunoDAO;
import br.com.alura.agenda.event.AtualizaListaAlunoEvent;
import br.com.alura.agenda.modelo.dto.AlunosSync;
import br.com.alura.agenda.preferences.AlunoPreferences;
import br.com.alura.agenda.retrofit.RetrofitInicializador;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.internal.EverythingIsNonNull;

public class AlunosSincronizador {

    private final Context context;
    private final AlunoPreferences preferences;
    private EventBus bus = EventBus.getDefault();

    public AlunosSincronizador(Context context) {
        this.context = context;
        preferences = new AlunoPreferences(context);
    }

    /**
     * Caso exista a versão, busca somente os alunos das versões posteriores.
     * Caso contrário, busca todos os alunos.
     */
    public void buscaTodos(){
        if(preferences.temVersao()){
            buscaNovosAlunos(); // Busca somente os alunos das versões mais recentes
        } else {
            buscaTodosAlunos();
        }
    }

    private void buscaNovosAlunos(){
        Call<AlunosSync> call = new RetrofitInicializador().getAlunoService().novos(preferences.getVersao());
        call.enqueue(buscaAlunosCallback());
    }

    private void buscaTodosAlunos() {
        Call<AlunosSync> call = new RetrofitInicializador().getAlunoService().lista();
        call.enqueue(buscaAlunosCallback());
    }

    private Callback<AlunosSync> buscaAlunosCallback() {
        return new Callback<AlunosSync>() {
            @Override
            @EverythingIsNonNull
            public void onResponse(Call<AlunosSync> call, Response<AlunosSync> response) {
                AlunosSync alunosSync = response.body();

                preferences.salvaVersao(alunosSync.getMomentoDaUltimaModificacao());

                AlunoDAO dao = new AlunoDAO(context);
                dao.sincroniza(alunosSync.getAlunos());
                dao.close();

                Log.i("versao", preferences.getVersao());

                bus.post(new AtualizaListaAlunoEvent());
            }

            @Override
            @EverythingIsNonNull
            public void onFailure(Call<AlunosSync> call, Throwable t) {
                Log.e("get_alunos", "FALHOU A BUSCA DOS ALUNOS...", t);
                bus.post(new AtualizaListaAlunoEvent());
            }
        };
    }
}
